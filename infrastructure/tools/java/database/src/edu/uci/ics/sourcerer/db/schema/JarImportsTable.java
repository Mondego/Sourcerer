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

import edu.uci.ics.sourcerer.db.util.InsertBatcher;
import edu.uci.ics.sourcerer.db.util.QueryExecutor;
import edu.uci.ics.sourcerer.model.db.TypedEntityID;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class JarImportsTable {
  private JarImportsTable() {}
  
  public static final String TABLE = "jar_imports";
  /*  
   *  +-------------+-------------------+-------+--------+
   *  | Column name | Type              | Null? | Index? |
   *  +-------------+-------------------+-------+--------+
   *  | static      | BOOLEAN           | No    | No     |
   *  | on_demand   | BOOLEAN           | No    | No     |
   *  | leid        | BIGINT UNSIGNED   | Yes   | Yes    |
   *  | jeid        | BIGINT UNSIGNED   | Yes   | Yes    |
   *  | jar_id      | BIGINT UNSIGNED   | No    | Yes    |
   *  | jclass_fid  | BIGINT UNSIGNED   | No    | Yes    |
   *  | offset      | INT UNSIGNED      | No    | No     |
   *  | length      | INT UNSIGNED      | No    | No     |
   *  +-------------+-------------------+-------+--------+
   */
  
  // ---- CREATE ----
  public static void createTable(QueryExecutor executor) {
    executor.createTable(TABLE, 
        "static BOOLEAN NOT NULL",
        "on_demand BOOLEAN NOT NULL",
        "leid BIGINT UNSIGNED",
        "jeid BIGINT UNSIGNED",
        "jar_id BIGINT UNSIGNED NOT NULL",
        "jclass_fid BIGINT UNSIGNED NOT NULL",
        "offset INT UNSIGNED NOT NULL",
        "length INT UNSIGNED NOT NULL",
        "INDEX(leid)",
        "INDEX(jeid)",
        "INDEX(jar_id)",
        "INDEX(jclass_fid)");
  }
  
  // ---- DELETE ---- 
  public static void deleteByJarID(QueryExecutor executor, String jarID) {
    executor.delete(TABLE, "jar_id=" + jarID);
  }
  
  // ---- INSERT ----
  public static InsertBatcher getInsertBatcher(QueryExecutor executor) {
    return executor.getInsertBatcher(TABLE);
  }
  
  private static String getInsertValue(boolean isStatic, boolean onDemand, TypedEntityID eid, String jarID, String jarClassFileID, String offset, String length) {
    return SchemaUtils.getInsertValue(
        SchemaUtils.convertBoolean(isStatic),
        SchemaUtils.convertBoolean(onDemand),
        SchemaUtils.convertJarTypedEntityID(eid),
        SchemaUtils.convertNotNullNumber(jarID),
        SchemaUtils.convertNotNullNumber(jarClassFileID),
        SchemaUtils.convertOffset(offset),
        SchemaUtils.convertLength(length));
  }
  
  public static void insert(InsertBatcher batcher, boolean isStatic, boolean onDemand, TypedEntityID eid, String jarID, String jarClassFileID, String offset, String length) {
    batcher.addValue(getInsertValue(isStatic, onDemand, eid, jarID, jarClassFileID, offset, length));
  }
}
