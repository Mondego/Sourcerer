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
package edu.uci.ics.sourcerer.db.queries;

import java.io.Closeable;

import edu.uci.ics.sourcerer.tools.java.db.queries.ProjectQueries;
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
import edu.uci.ics.sourcerer.util.db.DatabaseConnection;
import edu.uci.ics.sourcerer.utils.db.QueryExecutor;
import edu.uci.ics.sourcerer.utils.db.TableLocker;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public abstract class DatabaseAccessor implements Closeable {
  protected QueryExecutor executor;
  protected TableLocker locker;

  protected ProjectsTable projectsTable;
  protected ProjectMetricsTable projectMetricsTable;
  protected FilesTable filesTable;
  protected FileMetricsTable fileMetricsTable;
  protected ImportsTable importsTable;
  protected ProblemsTable problemsTable;
  protected CommentsTable commentsTable;
  protected EntitiesTable entitiesTable;
  protected EntityMetricsTable entityMetricsTable;
  protected RelationsTable relationsTable;

  protected ProjectQueries projectQueries;
  protected ProjectMetricQueries projectMetricQueries;
  protected FileQueries fileQueries;
  protected FileMetricQueries fileMetricQueries;
  protected ImportQueries importQueries;
  protected CommentQueries commentQueries;
  protected EntityQueries entityQueries;
  protected EntityMetricQueries entityMetricQueries;
  protected RelationQueries relationQueries;
  
  protected JoinQueries joinQueries;
  
  protected DatabaseAccessor(DatabaseConnection connection) {
    init(connection);
  }
  
  protected DatabaseAccessor() {}
  
  protected void init(DatabaseConnection connection) {
    executor = new QueryExecutor(connection.getConnection());
    locker = executor.getTableLocker();
    
    projectsTable = new ProjectsTable(executor, locker);
    projectMetricsTable = new ProjectMetricsTable(executor, locker);
    filesTable = new FilesTable(executor, locker);
    fileMetricsTable = new FileMetricsTable(executor, locker);
    importsTable = new ImportsTable(executor, locker);
    problemsTable = new ProblemsTable(executor, locker);
    commentsTable = new CommentsTable(executor, locker);
    entitiesTable = new EntitiesTable(executor, locker);
    entityMetricsTable = new EntityMetricsTable(executor, locker);
    relationsTable = new RelationsTable(executor, locker);
    
    projectQueries = new ProjectQueries(executor);
    projectMetricQueries = new ProjectMetricQueries(executor);
    fileQueries = new FileQueries(executor);
    fileMetricQueries = new FileMetricQueries(executor);
    importQueries = new ImportQueries(executor);
    commentQueries = new CommentQueries(executor);
    entityQueries = new EntityQueries(executor);
    entityMetricQueries = new EntityMetricQueries(executor);
    relationQueries = new RelationQueries(executor);
    
    joinQueries = new JoinQueries(executor);
  }
  
  public void close() {
    executor.close();
  }
  
  public void closeConnection() {
    executor.closeConnection();
  }
  
  protected void lock() {
    locker.lock();
  }
  
  protected void unlock() {
    locker.unlock();
  }
  
  protected void deleteByProject(Integer projectID) {
    // Delete the files
    filesTable.deleteByProjectID(projectID);
    
    // Delete the problems
    problemsTable.deleteByProjectID(projectID);
    
    // Delete the imports
    importsTable.deleteByProjectID(projectID);
    
    // Delete the comments
    commentsTable.deleteByProjectID(projectID);
    
    // Delete the entities
    entitiesTable.deleteByProjectID(projectID);
    
    // Delete the relations
    relationsTable.deleteByProjectID(projectID);
    
    // Delete the project
    projectsTable.deleteProject(projectID);
  }
}
