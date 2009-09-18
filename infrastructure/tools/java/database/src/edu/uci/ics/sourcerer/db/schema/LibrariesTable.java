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
public final class LibrariesTable {
  private LibrariesTable() {}
  
  public static final String TABLE = "libraries";
  /*  
   *  +-------------+---------------+-------+--------+
   *  | Column name | Type          | Null? | Index? |
   *  +-------------+---------------+-------+--------+
   *  | library_id  | SERIAL        | No    | Yes    |
   *  | name        | VARCHAR(1024) | No    | No     |
   *  +-------------+---------------+-------+--------+
   */
  
  // ---- CREATE ----
  public static void createTable(QueryExecutor executor) {
    executor.createTable(TABLE,
        "library_id SERIAL",
        "name VARCHAR(1024) BINARY NOT NULL");
  }
  
  // ---- INSERT ----
  private static String getInsertValue(String name) {
    return "(NULL," + SchemaUtils.convertNotNullVarchar(name) + ")";
  }
  
  public static String insert(QueryExecutor executor, String name) {
    return executor.insertSingleWithKey(TABLE, getInsertValue(name));
  }
  
  // ---- SELECT ----
  public static String getLibraryCount(QueryExecutor executor) {
    return executor.getRowCount(TABLE);
  }
  
  public static String getLibraryIDByName(QueryExecutor executor, String name) {
    return executor.selectSingle(TABLE, "library_id", "name ='" + name +"'");
  }
}
