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
public final class JarUsesTable {
  private JarUsesTable() {}
  
  public static final String TABLE = "jar_uses";
  /*  
   *  +-------------+-----------------+-------+--------+
   *  | Column name | Type            | Null? | Index? |
   *  +-------------+-----------------+-------+--------+
   *  | jar_id      | BIGINT UNSIGNED | No    | Yes    |
   *  | project_id  | BIGINT UNSIGNED | No    | Yes    |
   *  +-------------+-----------------+-------+--------+
   */
  
  // ---- CREATE ----
  public static void createTable(QueryExecutor executor) {
    executor.createTable(TABLE,
        "jar_id BIGINT UNSIGNED NOT NULL",
        "project_id BIGINT UNSIGNED NOT NULL",
        "INDEX(jar_id)",
        "INDEX(project_id)");
  }
  
  // ---- INSERT ----
  public static void insert(QueryExecutor executor, String jarID, String projectID) {
    executor.insertSingleWithKey(TABLE, "(" + SchemaUtils.convertNotNullNumber(jarID) + "," + SchemaUtils.convertNotNullNumber(projectID) + ")");
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
