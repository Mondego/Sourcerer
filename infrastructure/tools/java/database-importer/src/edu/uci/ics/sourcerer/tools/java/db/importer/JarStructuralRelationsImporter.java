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

import java.util.Collections;

import edu.uci.ics.sourcerer.tools.java.db.importer.resolver.JavaLibraryTypeModel;
import edu.uci.ics.sourcerer.tools.java.db.importer.resolver.UnknownEntityCache;
import edu.uci.ics.sourcerer.tools.java.db.schema.ProjectsTable;
import edu.uci.ics.sourcerer.tools.java.db.schema.ProjectsTable.ProjectState;
import edu.uci.ics.sourcerer.tools.java.model.extracted.io.ReaderBundle;
import edu.uci.ics.sourcerer.tools.java.repo.model.extracted.ExtractedJarFile;
import edu.uci.ics.sourcerer.util.Nullerator;
import edu.uci.ics.sourcerer.utils.db.sql.Assignment;
import edu.uci.ics.sourcerer.utils.db.sql.ConstantCondition;
import edu.uci.ics.sourcerer.utils.db.sql.SelectQuery;
import edu.uci.ics.sourcerer.utils.db.sql.SetStatement;
import edu.uci.ics.sourcerer.utils.db.sql.TypedQueryResult;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
class JarStructuralRelationsImporter extends StructuralRelationsImporter {
  private Nullerator<ExtractedJarFile > jars;
  
  protected JarStructuralRelationsImporter(Nullerator<ExtractedJarFile> jars, JavaLibraryTypeModel javaModel, UnknownEntityCache unknowns) {
    super("Importing Jar Structural Relations", javaModel, unknowns);
    this.jars = jars;
  }
  
  @Override
  public void doImport() {
    initializeQueries();
    try (SelectQuery projectState = exec.createSelectQuery(ProjectsTable.TABLE)) {
      projectState.addSelect(ProjectsTable.PATH);
      projectState.addSelect(ProjectsTable.PROJECT_ID);
      ConstantCondition<String> equalsHash = ProjectsTable.HASH.compareEquals();
      projectState.andWhere(equalsHash);
      
      SetStatement updateState = exec.createSetStatement(ProjectsTable.TABLE);
      Assignment<String> stateValue = updateState.addAssignment(ProjectsTable.PATH);
      ConstantCondition<Integer> equalsID = ProjectsTable.PROJECT_ID.compareEquals();
      updateState.andWhere(equalsID);
      
      ExtractedJarFile jar;
      while ((jar = jars.next()) != null) {
        String name = jar.getProperties().NAME.getValue();
        task.start("Importing " + name + "'s structural relations");
        
        task.start("Verifying import suitability");
        Integer projectID = null;
        if (jar.getProperties().EXTRACTED.getValue()) {
          equalsHash.setValue(jar.getProperties().HASH.getValue());
          TypedQueryResult result = projectState.select();
          if (result.next()) {
            ProjectState state = ProjectState.parse(result.getResult(ProjectsTable.PATH));
            if (state == null || state == ProjectState.END_STRUCTURAL) {
              task.report("Entity import already completed... skipping");
            } else if (state == ProjectState.END_ENTITY) {
              projectID = result.getResult(ProjectsTable.PROJECT_ID);
            } else {
              task.report("Project not in correct state (" + state + ")... skipping");
            }
          }
        } else {
          task.report("Extraction not completed... skipping");
        }
        task.finish();
        
        if (projectID != null) {
          equalsID.setValue(projectID);
          stateValue.setValue(ProjectState.BEGIN_STRUCTURAL.name());
          updateState.execute();
          
          ReaderBundle reader = ReaderBundle.create(jar.getExtractionDir().toFile(), jar.getCompressedFile().toFile());
          insert(reader, projectID, Collections.<Integer>emptyList());
          
          stateValue.setValue(ProjectState.END_STRUCTURAL.name());
          updateState.execute();
        }
        
        task.finish();
      }
    }
  } 
}
