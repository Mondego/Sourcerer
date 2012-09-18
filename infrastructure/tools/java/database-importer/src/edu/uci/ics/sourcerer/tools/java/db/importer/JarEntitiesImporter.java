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
class JarEntitiesImporter extends EntitiesImporter {
  private Nullerator<ExtractedJarFile> jars;
  
  protected JarEntitiesImporter(Nullerator<ExtractedJarFile> jars) {
    super("Importing Jar Entities");
    this.jars = jars;
  }
  
  @Override
  public void doImport() {
    try (SelectQuery projectState = exec.createSelectQuery(ProjectsTable.TABLE)) {
      projectState.addSelect(ProjectsTable.PATH, ProjectsTable.PROJECT_ID);
      ConstantCondition<String> equalsHash = ProjectsTable.HASH.compareEquals();
      projectState.andWhere(equalsHash);
      
      SetStatement updateState = exec.createSetStatement(ProjectsTable.TABLE);
      Assignment<String> ass = updateState.addAssignment(ProjectsTable.PATH);
      ConstantCondition<Integer> equalsID = ProjectsTable.PROJECT_ID.compareEquals();
      updateState.andWhere(equalsID);
      
      ExtractedJarFile jar;
      while ((jar = jars.next()) != null) {
        String name = jar.getProperties().NAME.getValue();
        task.start("Importing " + name + "'s entities");
        
        Integer projectID = null;
        
        task.start("Verifying import suitability");
        boolean shouldImport = true;
        if (jar.getProperties().EXTRACTED.getValue()) {
          equalsHash.setValue(jar.getProperties().HASH.getValue());
          TypedQueryResult result = projectState.select();
          if (result.next()) {
            ProjectState state = ProjectState.parse(result.getResult(ProjectsTable.PATH));
            if (state == null || state == ProjectState.END_ENTITY || state == ProjectState.END_STRUCTURAL) {
              task.report("Entity import already completed... skipping");
              shouldImport = false;
            } else {
              projectID = result.getResult(ProjectsTable.PROJECT_ID);
              if (state != ProjectState.COMPONENT) {
                task.start("Deleting incomplete import");
                deleteProjectContents(result.getResult(ProjectsTable.PROJECT_ID));

                task.finish();
              }
            } 
          }
        } else {
          task.report("Extraction not completed... skipping");
          shouldImport = false;
        }
        task.finish();
        
        if (shouldImport) {
          if (projectID == null) {
            task.start("Inserting project");
            projectID = exec.insertWithKey(createInsert(jar));
            equalsID.setValue(projectID);
            task.finish();
          } else {
            equalsID.setValue(projectID);
            ass.setValue(ProjectState.BEGIN_ENTITY.name());
            updateState.execute();
          }
          
          ReaderBundle reader = ReaderBundle.create(jar.getExtractionDir().toFile(), jar.getCompressedFile().toFile());
          
          insert(reader, projectID);
          
          ass.setValue(ProjectState.END_ENTITY.name());
          updateState.execute();
        }
        
        task.finish();
      }
    }
  }
}
