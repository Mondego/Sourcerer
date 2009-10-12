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
package edu.uci.ics.sourcerer.db.schema;

import edu.uci.ics.sourcerer.db.util.QueryExecutor;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public final class ProjectsTable {
  private ProjectsTable() {}
  
  public static String TABLE = "projects";
  /*  
   *  +-------------+---------------+-------+--------+
   *  | Column name | Type          | Null? | Index? |
   *  +-------------+---------------+-------+--------+
   *  | project_id  | SERIAL        | No    | Yes    |
   *  | name        | VARCHAR(1024) | No    | Yes    |
   *  | version     | VARCHAR(128)  | Yes   | No     |
   *  | path        | VARCHAR(256)  | No    | No     |
   *  +-------------+---------------+-------+--------+
   */
  
  // ---- CREATE ----
  public static void createTable(QueryExecutor executor) {
    executor.createTable(TABLE,
        "project_id SERIAL",
        "name VARCHAR(1024) BINARY NOT NULL",
        "version VARCHER(128) BINARY",
        "path VARCHAR(256) BINARY NOT NULL",
        "INDEX(name(48))");
  }
  
  // ---- INSERT ----
  private static String getInsertValue(String name, String path) {
    return "(NULL,'" + SchemaUtils.convertNotNullVarchar(name) + "',NULL,'" + SchemaUtils.convertNotNullVarchar(path) + "')";
  }
  
  public static String insert(QueryExecutor executor, String name, String path) {
    return executor.insertSingleWithKey(TABLE, getInsertValue(name, path));
  }
  
  // ---- DELETE ----
  public static void deleteProject(QueryExecutor executor, String projectID) {
    // Delete the files
    FilesTable.deleteByProjectID(executor, projectID);
    
    // Delete the problems
    ProblemsTable.deleteByProjectID(executor, projectID);
    
    // Delete the imports
    ImportsTable.deleteByProjectID(executor, projectID);
    
    // Delete the entities
    EntitiesTable.deleteByProjectID(executor, projectID);
    
    // Delete the relations
    RelationsTable.deleteByProjectID(executor, projectID);
        
    // Delete the comments
    CommentsTable.deleteByProjectID(executor, projectID);
    
    // Delete the project
    executor.delete(TABLE, "project_id=" + projectID);
  }
  
  // ---- SELECT ----
  public static String getProjectCount(QueryExecutor executor) {
    return executor.getRowCount(TABLE);
  }
  
  public static String getProjectIDByName(QueryExecutor executor, String name) {
    return executor.selectSingle(TABLE, "project_id", "name='" + name + "'");
  }
  
//  
//  public static String getJarProject(QueryExecutor executor, String jarName) {
//    String query = "SELECT projects.project_id FROM projects WHERE name = '" + jarName + "' AND path IS NULL;";
//    QueryResult result = executor.execute(query);
//    if (result.next()) {
//      String eid = result.getString(1);
//      if (result.next()) {
//        logger.log(Level.SEVERE, "There should not be two identically named jar projects: " + jarName);
//        return null; 
//      }
//      return eid;
//    } else {
//      logger.log(Level.SEVERE, "Missing a referenced jar: " + jarName);
//      return null;
//    }
//  }
//  
//  public static void updateProjectPath(QueryExecutor executor, String projectID, String path) {
//    String query = "UPDATE projects SET path = '" + path + "' WHERE project_id = " + projectID;
//    executor.execute(query);
//  }
// 

//  
//  public static String addJarProjectToDatabase(QueryExecutor executor, String name) {
//    return addProjectToDatabase(executor, Type.JAR.name(), name, null);
//  }
//  
//  public static String addJarUnionProjectToDatabase(QueryExecutor executor, String name) {
//    return addProjectToDatabase(executor, Type.JAR_UNION.name(), name, null);
//  }
//  
//  public static String addJavaLibraryProjectToDatabase(QueryExecutor executor, String name) {
//    return addProjectToDatabase(executor, Type.LIBRARY.name(), name, "NULL");
//  }
//  
//  private static String addProjectToDatabase(QueryExecutor executor, String type, String name, String path) {
//    String query = "INSERT INTO projects " +
//      "VALUES(NULL, " + 
//      "'" + type + "', " +
//      "'" + name + "', " + 
//      (path == null ? "NULL" : ("'" + path + "'")) +
//      ");";
//    QueryResult result = executor.executeUpdateWithKeys(query);
//    if (result.next()) {
//      return result.getString(1);
//    } else {
//      logger.log(Level.SEVERE, "Unable to read project_id in add");
//      return "NULL";
//    }
//  }
}
