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

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class LibraryImportsTable {
  private LibraryImportsTable() {}
  
  public static final String TABLE = "library_imports";
  /*  
   *  +-------------+-------------------+-------+--------+
   *  | Column name | Type              | Null? | Index? |
   *  +-------------+-------------------+-------+--------+
   *  | static      | BOOELAN           | No    | No     |
   *  | on_demand   | BOOLEAN           | No    | No     |
   *  | leid        | BIGINT UNSIGNED   | No    | Yes    |
   *  | library_id  | BIGINT UNSIGNED   | No    | Yes    |
   *  | lclass_fid  | BIGINT UNSIGNED   | No    | Yes    |
   *  | offset      | INT UNSIGNED      | No    | No     |
   *  | length      | INT UNSIGNED      | No    | No     |
   *  +-------------+-------------------+-------+--------+
   */
  
  // ---- CREATE ----
  public static void createTable(QueryExecutor executor) {
    executor.createTable(TABLE, 
        "static BOOLEAN NOT NULL",
        "on_demand BOOLEAN NOT NULL",
        "leid BIGINT UNSIGNED NOT NULL",
        "library_id BIGINT UNSIGNED NOT NULL",
        "lclass_fid BIGINT UNSIGNED NOT NULL",
        "offset INT UNSIGNED NOT NULL",
        "length INT UNSIGNED NOT NULL",
        "INDEX(leid)",
        "INDEX(library_id)",
        "INDEX(lclass_fid)");
  }
  
  // ---- INSERT ----
  public static InsertBatcher getInsertBatcher(QueryExecutor executor) {
    return executor.getInsertBatcher(TABLE);
  }
  
  private static String getInsertValue(boolean isStatic, boolean onDemand, String leid, String libraryID, String libraryClassFileID, String offset, String length) {
    return SchemaUtils.getInsertValue(
        SchemaUtils.convertBoolean(isStatic),
        SchemaUtils.convertBoolean(onDemand),
        SchemaUtils.convertNotNullNumber(leid),
        SchemaUtils.convertNotNullNumber(libraryID),
        SchemaUtils.convertNotNullNumber(libraryClassFileID),
        SchemaUtils.convertOffset(offset),
        SchemaUtils.convertLength(length));
  }
  
  public static void insert(InsertBatcher batcher, boolean isStatic, boolean onDemand, String leid, String libraryID, String libraryClassFileID, String offset, String length) {
    batcher.addValue(getInsertValue(isStatic, onDemand, leid, libraryID, libraryClassFileID, offset, length));
  }
}
