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
public final class UsedJarsTable {
  private UsedJarsTable() {}
  
  public static final String TABLE = "used_jars";
  /*  
   *  +-------------+-----------------+-------+--------+
   *  | Column name | Type            | Null? | Index? |
   *  +-------------+-----------------+-------+--------+
   *  | used_jar_id | BIGINT UNSIGNED | No    | Yes    |
   *  | jar_id      | BIGINT UNSIGNED | Yes   | Yes    |
   *  | project_id  | BIGINT UNSIGNED | No    | Yes    |
   *  +-------------+-----------------+-------+--------+
   */
  
  // ---- LOCK ----
  public static String getReadLock() {
    return SchemaUtils.getReadLock(TABLE);
  }
  
  public static String getWriteLock() {
    return SchemaUtils.getWriteLock(TABLE);
  }
  
  // ---- CREATE ----
  public static void createTable(QueryExecutor executor) {
    executor.createTable(TABLE,
        "used_jar_id BIGINT UNSIGNED NOT NULL",
        "jar_id BIGINT UNSIGNED",
        "project_id BIGINT UNSIGNED",
        "INDEX(used_jar_id)",
        "INDEX(jar_id)",
        "INDEX(project_id)");
  }
  
  // ---- INSERT ----
  private static String getInsertValue(String usedJarID, String jarID, String projectID) {
    return SchemaUtils.getInsertValue(
        SchemaUtils.convertNotNullNumber(usedJarID),
        SchemaUtils.convertNumber(jarID),
        SchemaUtils.convertNumber(projectID));
  }
  
  public static void insert(QueryExecutor executor, String usedJarID, String jarID, String projectID) {
    executor.insertSingle(TABLE, getInsertValue(usedJarID, jarID, projectID));
  }
  
  // ---- DELETE ----
  public static void deleteByJarID(QueryExecutor executor, String jarID) {
    executor.delete(TABLE, "jar_id=" + jarID);
  }
  
  // ---- SELECT ----
//  public static int getProjectsUsingCount(QueryExecutor executor, String jarIDs) {
//    return executor.executeSingleInt("SELECT COUNT(DISTINCT project_id) FROM jar_uses WHERE jar_id IN " + jarIDs + ";");
//  }
//  
//  public static int getProjectsReallyUsingCount(QueryExecutor executor, String jarIDs) {
//    return executor.executeSingleInt("SELECT COUNT(DISTINCT project_id) FROM relations INNER JOIN jar_entities on rhs_jeid=entity_id AND jar_id IN " + jarIDs + ";");
//  }
//  
//  public static Collection<String> getUsedJars(QueryExecutor executor, String projectID) {
//    return executor.execute("SELECT jar_id FROM jar_uses WHERE project_id= " + projectID + ";", ResultTranslator.SIMPLE_RESULT_TRANSLATOR);
//  }
}
