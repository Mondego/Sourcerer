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
import edu.uci.ics.sourcerer.tools.java.db.schema.RelationsTable;
import edu.uci.ics.sourcerer.tools.java.model.types.Location;
import edu.uci.ics.sourcerer.util.io.FileUtils;
import edu.uci.ics.sourcerer.util.io.logging.Logging;
import edu.uci.ics.sourcerer.util.io.logging.TaskProgressLogger;
import edu.uci.ics.sourcerer.utils.db.ParallelDatabaseRunnable;
import edu.uci.ics.sourcerer.utils.db.sql.DeleteStatement;
import edu.uci.ics.sourcerer.utils.db.sql.SelectQuery;
import edu.uci.ics.sourcerer.utils.db.sql.TypedQueryResult;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public abstract class DatabaseImporter extends ParallelDatabaseRunnable {
  private String taskName;
  
  protected File tempDir;
  protected TaskProgressLogger task;
  
  protected Map<String, Integer> fileMap;
  
  protected DatabaseImporter(String taskName) {
    this.taskName = taskName;
    
    fileMap = new HashMap<>();
  }
  
  @Override
  public final void action() {
    init();
    
    task = TaskProgressLogger.get();
    task.start(taskName);
    doImport();
    task.finish();
    cleanup();
  }
  
  protected void init() {
    Logging.addThreadLogger();
    tempDir = FileUtils.getTempDir();
  }
  
  protected void cleanup() {
    Logging.removeThreadLogger();
    tempDir.delete();
  }
  
  protected abstract void doImport();
  
  protected final void loadFileMap(Integer projectID) {
    task.start("Populating file map", "files loaded");
    
    try (SelectQuery query = exec.createSelectQuery(FilesTable.TABLE)) {
      query.addSelect(FilesTable.FILE_ID, FilesTable.PATH);
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
          logger.severe("File map contains: ");
          for (String s : fileMap.keySet()) {
            logger.severe("  " + s);
          }
        }
        return fileID;
      }
    }
  }
  
  protected final void deleteProjectContents(Integer projectID) {
    DeleteStatement delete = null;
    
    delete = exec.createDeleteStatement(ProjectMetricsTable.TABLE);
    delete.andWhere(ProjectMetricsTable.PROJECT_ID.compareEquals(projectID));
    delete.execute();
    
    delete = exec.createDeleteStatement(FilesTable.TABLE);
    delete.andWhere(FilesTable.PROJECT_ID.compareEquals(projectID));
    delete.execute();
    
    delete = exec.createDeleteStatement(FileMetricsTable.TABLE);
    delete.andWhere(FileMetricsTable.PROJECT_ID.compareEquals(projectID));
    delete.execute(); 
    
    delete = exec.createDeleteStatement(EntitiesTable.TABLE);
    delete.andWhere(EntitiesTable.PROJECT_ID.compareEquals(projectID));
    delete.execute();
    
    delete = exec.createDeleteStatement(EntityMetricsTable.TABLE);
    delete.andWhere(EntityMetricsTable.PROJECT_ID.compareEquals(projectID));
    delete.execute();
    
    delete = exec.createDeleteStatement(CommentsTable.TABLE);
    delete.andWhere(CommentsTable.PROJECT_ID.compareEquals(projectID));
    delete.execute();
    
    delete = exec.createDeleteStatement(ImportsTable.TABLE);
    delete.andWhere(ImportsTable.PROJECT_ID.compareEquals(projectID));
    delete.execute();
    
    delete = exec.createDeleteStatement(ProblemsTable.TABLE);
    delete.andWhere(ProblemsTable.PROJECT_ID.compareEquals(projectID));
    delete.execute();
    
    delete = exec.createDeleteStatement(RelationsTable.TABLE);
    delete.andWhere(RelationsTable.PROJECT_ID.compareEquals(projectID));
    delete.execute();
  }
}
