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
import edu.uci.ics.sourcerer.tools.java.repo.model.extracted.ExtractedJavaProject;
import edu.uci.ics.sourcerer.util.Nullerator;
import edu.uci.ics.sourcerer.utils.db.sql.ConstantCondition;
import edu.uci.ics.sourcerer.utils.db.sql.SelectQuery;
import edu.uci.ics.sourcerer.utils.db.sql.SetStatement;
import edu.uci.ics.sourcerer.utils.db.sql.TypedQueryResult;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
class ProjectEntitiesImporter extends EntitiesImporter {
  private Nullerator<ExtractedJavaProject> projects;
  
  protected ProjectEntitiesImporter(Nullerator<ExtractedJavaProject> projects) {
    super("Importing Project Entities");
    this.projects = projects;
  }
  
  @Override
  public void doImport() {
    try (SelectQuery projectState = exec.createSelectQuery(ProjectsTable.TABLE)) {
      projectState.addSelect(ProjectsTable.HASH);
      projectState.addSelect(ProjectsTable.PROJECT_ID);
      ConstantCondition<String> equalsPath = ProjectsTable.PATH.compareEquals();
      projectState.andWhere(equalsPath);
      
      SetStatement updateState = exec.createSetStatement(ProjectsTable.TABLE);
      updateState.addAssignment(ProjectsTable.HASH, ProjectState.END_ENTITY.name());
      ConstantCondition<Integer> equalsID = ProjectsTable.PROJECT_ID.compareEquals();
      updateState.andWhere(equalsID);
      
      ExtractedJavaProject project;
      while ((project = projects.next()) != null) {
        String name = project.getProperties().NAME.getValue();
        task.start("Importing " + name + "'s entities (" + project.getLocation().toString() + ")");
        
        Integer projectID = null;
        
        task.start("Verifying import suitability");
        boolean shouldImport = true;
        if (project.getProperties().EXTRACTED.getValue() != null && project.getProperties().EXTRACTED.getValue()) {
          equalsPath.setValue(project.getLocation().toString());
          TypedQueryResult result = projectState.select();
          if (result.next()) {
            ProjectState state = ProjectState.parse(result.getResult(ProjectsTable.HASH));
            if (state == null || state == ProjectState.END_ENTITY || state == ProjectState.END_STRUCTURAL) {
              task.report("Entity import already completed... skipping");
              shouldImport = false;
            } else {
              projectID = result.getResult(ProjectsTable.PROJECT_ID);
              task.start("Deleting incomplete import");
              deleteProjectContents(projectID);
              task.finish();
            }
          } else if (project.getProperties().NAME.getValue() == null) {
              // Verify that it has a name
              task.report("Null name... skipping");
              shouldImport = false;
          }
        } else {
          task.report("Extraction not completed... skipping");
          shouldImport = false;
        }
        task.finish();
        
        if (shouldImport) {
          if (projectID == null) {
            task.start("Inserting project");
            projectID = exec.insertWithKey(createInsert(project));
            task.finish();
          }
          
          ReaderBundle reader = ReaderBundle.create(project.getExtractionDir().toFile(), project.getCompressedFile().toFile());
          
          insert(reader, projectID);
          
          equalsID.setValue(projectID);
          updateState.execute();
        }
        
        task.finish();
      }
    }
  }
}
