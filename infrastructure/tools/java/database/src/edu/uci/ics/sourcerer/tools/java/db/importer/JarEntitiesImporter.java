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
import edu.uci.ics.sourcerer.tools.java.model.extracted.io.ReaderBundle;
import edu.uci.ics.sourcerer.tools.java.repo.model.extracted.ExtractedJarFile;
import edu.uci.ics.sourcerer.util.Nullerator;
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
      projectState.addSelect(ProjectsTable.PATH);
      projectState.addSelect(ProjectsTable.PROJECT_ID);
      ConstantCondition<String> equalsHash = ProjectsTable.HASH.compareEquals();
      projectState.andWhere(equalsHash);
      
      SetStatement updateState = exec.createSetStatement(ProjectsTable.TABLE);
      updateState.addAssignment(ProjectsTable.PATH, Stage.END_ENTITY.name());
      ConstantCondition<Integer> equalsID = ProjectsTable.PROJECT_ID.compareEquals();
      updateState.andWhere(equalsID);
      
      ExtractedJarFile jar;
      while ((jar = jars.next()) != null) {
        String name = jar.getProperties().NAME.getValue();
        task.start("Importing " + name + "'s entities");
        
        task.start("Verifying import suitability");
        boolean shouldImport = true;
        if (jar.getProperties().EXTRACTED.getValue()) {
          equalsHash.setValue(jar.getProperties().HASH.getValue());
          TypedQueryResult result = projectState.select();
          if (result.next()) {
            Stage state = Stage.parse(result.getResult(ProjectsTable.PATH));
            if (state == null || state == Stage.END_ENTITY || state == Stage.END_STRUCTURAL) {
              task.report("Entity import already completed... skipping");
              shouldImport = false;
            } else {
              task.start("Deleting incomplete import");
              deleteProject(result.getResult(ProjectsTable.PROJECT_ID));
            }
          }
        } else {
          task.report("Extraction not completed... skipping");
          shouldImport = false;
        }
        task.finish();
        
        if (shouldImport) {
          task.start("Inserting project");
          Integer projectID = exec.insertWithKey(ProjectsTable.TABLE.createInsert(jar));
          task.finish();
          
          ReaderBundle reader = new ReaderBundle(jar.getExtractionDir().toFile());
          
          insert(reader, projectID);
          
          equalsID.setValue(projectID);
          updateState.execute();
        }
        
        task.finish();
      }
    }
  }
}
