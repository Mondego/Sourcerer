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

import static edu.uci.ics.sourcerer.util.io.Logging.logger;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import edu.uci.ics.sourcerer.tools.java.db.schema.CommentsTable;
import edu.uci.ics.sourcerer.tools.java.db.schema.EntitiesTable;
import edu.uci.ics.sourcerer.tools.java.db.schema.EntityMetricsTable;
import edu.uci.ics.sourcerer.tools.java.db.schema.FileMetricsTable;
import edu.uci.ics.sourcerer.tools.java.db.schema.FilesTable;
import edu.uci.ics.sourcerer.tools.java.db.schema.ImportsTable;
import edu.uci.ics.sourcerer.tools.java.db.schema.ProblemsTable;
import edu.uci.ics.sourcerer.tools.java.db.schema.ProjectMetricsTable;
import edu.uci.ics.sourcerer.tools.java.db.schema.ProjectsTable;
import edu.uci.ics.sourcerer.tools.java.db.schema.RelationsTable;
import edu.uci.ics.sourcerer.tools.java.model.types.Location;
import edu.uci.ics.sourcerer.util.io.FileUtils;
import edu.uci.ics.sourcerer.util.io.Logging;
import edu.uci.ics.sourcerer.util.io.TaskProgressLogger;
import edu.uci.ics.sourcerer.utils.db.ParallelDatabaseRunnable;
import edu.uci.ics.sourcerer.utils.db.sql.DeleteStatement;
import edu.uci.ics.sourcerer.utils.db.sql.SelectQuery;
import edu.uci.ics.sourcerer.utils.db.sql.TypedQueryResult;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public abstract class NewDatabaseImporter extends ParallelDatabaseRunnable {
  private String taskName;
  
  protected File tempDir;
  protected TaskProgressLogger task;
  
  protected Map<String, Integer> fileMap;
  
  protected NewDatabaseImporter(String taskName) {
    this.taskName = taskName;
    tempDir = FileUtils.getTempDir();
    fileMap = new HashMap<>();
  }
  
  @Override
  public final void action() {
    init();
    task = new TaskProgressLogger();
    task.start(taskName);
    doImport();
    task.finish();
    cleanup();
  }
  
  protected void init() {
    Logging.addThreadLogger();
  }
  
  protected void cleanup() {
    Logging.removeThreadLogger();
  }
  
  protected abstract void doImport();
  
  protected final void loadFileMap(Integer projectID) {
    task.start("Populating file map", "files loaded");
    
    try (SelectQuery query = exec.makeSelectQuery(FilesTable.TABLE)) {
      query.addSelects(FilesTable.FILE_ID, FilesTable.PATH);
      query.andWhere(
          FilesTable.FILE_TYPE.compareNotEquals(edu.uci.ics.sourcerer.tools.java.model.types.File.JAR).and(
          FilesTable.PROJECT_ID.compareEquals(projectID)));
      
      TypedQueryResult result = query.select();
      fileMap = new HashMap<>();
      while (result.next()) {
        fileMap.put(result.getResult(FilesTable.PATH), result.getResult(FilesTable.FILE_ID));
        task.progress();
      }
    }
    task.finish();
  }
  
  protected final Integer getFileID(Location location) {
    if (location == null) {
      return null;
    } else {
      String path = location.getPath();
      if (path == null) {
        path = location.getClassFile();
      }
      if (path == null) {
        return null;
      } else {
        Integer fileID = fileMap.get(path);
        if (fileID == null) {
          logger.log(Level.SEVERE, "Unknown file: " + location);
        }
        return fileID;
      }
    }
  }
  
  protected final void deleteProject(Integer projectID) {
    DeleteStatement delete = exec.makeDeleteStatement(ProjectsTable.TABLE);
    delete.andWhere(ProjectsTable.PROJECT_ID.compareEquals(projectID));
    delete.execute();
    
    delete = exec.makeDeleteStatement(ProjectMetricsTable.TABLE);
    delete.andWhere(ProjectMetricsTable.PROJECT_ID.compareEquals(projectID));
    delete.execute();
    
    delete = exec.makeDeleteStatement(FilesTable.TABLE);
    delete.andWhere(FilesTable.PROJECT_ID.compareEquals(projectID));
    delete.execute();
    
    delete = exec.makeDeleteStatement(FileMetricsTable.TABLE);
    delete.andWhere(FileMetricsTable.PROJECT_ID.compareEquals(projectID));
    delete.execute(); 
    
    delete = exec.makeDeleteStatement(EntitiesTable.TABLE);
    delete.andWhere(EntitiesTable.PROJECT_ID.compareEquals(projectID));
    delete.execute();
    
    delete = exec.makeDeleteStatement(EntityMetricsTable.TABLE);
    delete.andWhere(EntityMetricsTable.PROJECT_ID.compareEquals(projectID));
    delete.execute();
    
    delete = exec.makeDeleteStatement(CommentsTable.TABLE);
    delete.andWhere(CommentsTable.PROJECT_ID.compareEquals(projectID));
    delete.execute();
    
    delete = exec.makeDeleteStatement(ImportsTable.TABLE);
    delete.andWhere(ImportsTable.PROJECT_ID.compareEquals(projectID));
    delete.execute();
    
    delete = exec.makeDeleteStatement(ProblemsTable.TABLE);
    delete.andWhere(ProblemsTable.PROJECT_ID.compareEquals(projectID));
    delete.execute();
    
    delete = exec.makeDeleteStatement(RelationsTable.TABLE);
    delete.andWhere(RelationsTable.PROJECT_ID.compareEquals(projectID));
    delete.execute();
  }
}
