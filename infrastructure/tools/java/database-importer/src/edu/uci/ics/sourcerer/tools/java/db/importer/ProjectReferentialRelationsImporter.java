/* 
 * Sourcerer: an infrastructure for large-scale source code analysis.
 * Copyright (C) by contributors. See CONTRIBUTORS.txt for full list.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package edu.uci.ics.sourcerer.tools.java.db.importer;

import static edu.uci.ics.sourcerer.util.io.logging.Logging.logger;

import java.util.Collection;
import java.util.HashSet;

import edu.uci.ics.sourcerer.tools.java.db.importer.resolver.JavaLibraryTypeModel;
import edu.uci.ics.sourcerer.tools.java.db.importer.resolver.UnknownEntityCache;
import edu.uci.ics.sourcerer.tools.java.db.schema.ProjectsTable;
import edu.uci.ics.sourcerer.tools.java.db.schema.ProjectsTable.ProjectState;
import edu.uci.ics.sourcerer.tools.java.model.extracted.UsedJarEX;
import edu.uci.ics.sourcerer.tools.java.model.extracted.io.ReaderBundle;
import edu.uci.ics.sourcerer.tools.java.repo.model.extracted.ExtractedJavaProject;
import edu.uci.ics.sourcerer.util.Nullerator;
import edu.uci.ics.sourcerer.utils.db.sql.Assignment;
import edu.uci.ics.sourcerer.utils.db.sql.ConstantCondition;
import edu.uci.ics.sourcerer.utils.db.sql.SelectQuery;
import edu.uci.ics.sourcerer.utils.db.sql.SetStatement;
import edu.uci.ics.sourcerer.utils.db.sql.TypedQueryResult;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class ProjectReferentialRelationsImporter extends ReferentialRelationsImporter {
  private Nullerator<ExtractedJavaProject> projects;
  
  protected ProjectReferentialRelationsImporter(Nullerator<ExtractedJavaProject> projects, JavaLibraryTypeModel javaModel, UnknownEntityCache unknowns) {
    super("Importing Jar Referential Relations", javaModel, unknowns);
    this.projects = projects;
  }
  
  @Override
  public void doImport() {
    try (SelectQuery projectState = exec.createSelectQuery(ProjectsTable.TABLE);
         SelectQuery findUsedJar = exec.createSelectQuery(ProjectsTable.TABLE)) {
      projectState.addSelect(ProjectsTable.HASH);
      projectState.addSelect(ProjectsTable.PROJECT_ID);
      ConstantCondition<String> equalsPath = ProjectsTable.PATH.compareEquals();
      projectState.andWhere(equalsPath);
      
      findUsedJar.addSelect(ProjectsTable.PROJECT_ID);
      ConstantCondition<String> equalsHash = ProjectsTable.HASH.compareEquals();
      findUsedJar.andWhere(equalsHash);
      
      SetStatement updateState = exec.createSetStatement(ProjectsTable.TABLE);
      Assignment<String> stateValue = updateState.addAssignment(ProjectsTable.HASH);
      ConstantCondition<Integer> equalsID = ProjectsTable.PROJECT_ID.compareEquals();
      updateState.andWhere(equalsID);
      
      ExtractedJavaProject project;
      while ((project = projects.next()) != null) {
        String name = project.getProperties().NAME.getValue();
        task.start("Importing " + name + "'s referential relations (" + project.getLocation().toString() + ")");
        
        task.start("Verifying import suitability");
        Integer projectID = null;
        if (project.getProperties().EXTRACTED.getValue() != null && project.getProperties().EXTRACTED.getValue()) {
          equalsPath.setValue(project.getLocation().toString());
          TypedQueryResult result = projectState.select();
          if (result.next()) {
            ProjectState state = ProjectState.parse(result.getResult(ProjectsTable.HASH));
            if (state == null) {
              task.report("Entity import already completed... skipping");
            } else if (state == ProjectState.END_STRUCTURAL) {
              projectID = result.getResult(ProjectsTable.PROJECT_ID);
            } else {
              task.report("Project not in correct state (" + state + ")... skipping");
            }
          }
        } else {
          task.report("Extraction not completed... skipping");
        }
        task.finish();
        
        if (projectID == null) {
          task.report("Unable to locate project... skipping");
        } else {
          equalsID.setValue(projectID);
          stateValue.setValue(ProjectState.BEGIN_REFERENTIAL.name());
          updateState.execute();
          
          ReaderBundle reader = ReaderBundle.create(project.getExtractionDir().toFile(), project.getCompressedFile().toFile());
          
          Collection<Integer> usedJars = new HashSet<>();
          for (UsedJarEX used : reader.getTransientUsedJars()) {
            equalsHash.setValue(used.getHash());
            Integer jarID = findUsedJar.select().toSingleton(ProjectsTable.PROJECT_ID, true);
            if (jarID == null) {
              logger.severe("Missing project for jar: " + used.getHash());
            } else {
              usedJars.add(jarID);
            }
          }
          insert(reader, projectID, usedJars);
          
          stateValue.setValue(null);
          updateState.execute();
        }
        
        task.finish();
      }
    }
  }
}
