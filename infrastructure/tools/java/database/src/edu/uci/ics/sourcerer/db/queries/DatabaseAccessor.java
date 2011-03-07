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

import edu.uci.ics.sourcerer.db.schema.CommentsTable;
import edu.uci.ics.sourcerer.db.schema.EntitiesTable;
import edu.uci.ics.sourcerer.db.schema.FilesTable;
import edu.uci.ics.sourcerer.db.schema.ImportsTable;
import edu.uci.ics.sourcerer.db.schema.ProblemsTable;
import edu.uci.ics.sourcerer.db.schema.ProjectsTable;
import edu.uci.ics.sourcerer.db.schema.RelationsTable;
import edu.uci.ics.sourcerer.db.util.DatabaseConnection;
import edu.uci.ics.sourcerer.db.util.QueryExecutor;
import edu.uci.ics.sourcerer.db.util.TableLocker;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public abstract class DatabaseAccessor implements Closeable {
  protected final QueryExecutor executor;
  protected final TableLocker locker;

  protected final ProjectsTable projectsTable;
  protected final FilesTable filesTable;
  protected final ImportsTable importsTable;
  protected final ProblemsTable problemsTable;
  protected final CommentsTable commentsTable;
  protected final EntitiesTable entitiesTable;
  protected final RelationsTable relationsTable;

  protected final ProjectQueries projectQueries;
  protected final FileQueries fileQueries;
  protected final ImportQueries importQueries;
  protected final CommentQueries commentQueries;
  protected final EntityQueries entityQueries;
  protected final RelationQueries relationQueries;
  
  protected final JoinQueries joinQueries;
  
  protected DatabaseAccessor(DatabaseConnection connection) {
    executor = new QueryExecutor(connection.getConnection());
    locker = executor.getTableLocker();
    
    projectsTable = new ProjectsTable(executor, locker);
    filesTable = new FilesTable(executor, locker);
    importsTable = new ImportsTable(executor, locker);
    problemsTable = new ProblemsTable(executor, locker);
    commentsTable = new CommentsTable(executor, locker);
    entitiesTable = new EntitiesTable(executor, locker);
    relationsTable = new RelationsTable(executor, locker);
    
    projectQueries = new ProjectQueries(executor);
    fileQueries = new FileQueries(executor);
    importQueries = new ImportQueries(executor);
    commentQueries = new CommentQueries(executor);
    entityQueries = new EntityQueries(executor);
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
