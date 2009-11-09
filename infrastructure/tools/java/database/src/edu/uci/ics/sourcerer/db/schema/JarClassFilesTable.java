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

import edu.uci.ics.sourcerer.db.util.KeyInsertBatcher;
import edu.uci.ics.sourcerer.db.util.QueryExecutor;
import edu.uci.ics.sourcerer.model.extracted.FileEX;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class JarClassFilesTable {
  private JarClassFilesTable() {}
  
  public static final String TABLE = "jar_class_files";
  /*  
   *  +-------------+-----------------+-------+--------+
   *  | Column name | Type            | Null? | Index? |
   *  +-------------+-----------------+-------+--------+
   *  | file_id     | SERIAL          | No    | Yes    |
   *  | name        | VARCHAR(1024)   | No    | Yes    |
   *  | path        | VARCHAR(1024)   | No    | No     |
   *  | jar_id      | BIGINT UNSIGNED | No    | Yes    |
   *  +-------------+-----------------+-------+--------+
   */
  
  // ---- CREATE ----
  public static void createTable(QueryExecutor executor) {
    executor.createTable(TABLE,
        "file_id SERIAL",
        "name VARCHAR(1024) BINARY NOT NULL",
        "path VARCHAR(1024) BINARY NOT NULL",
        "jar_id BIGINT UNSIGNED NOT NULL",
        "INDEX(name(48))",
        "INDEX(jar_id)");
  }
  
  // ---- INSERT ----
  public static <T> KeyInsertBatcher<T> getKeyInsertBatcher(QueryExecutor executor, KeyInsertBatcher.KeyProcessor<T> processor) {
    return executor.getKeyInsertBatcher(TABLE, processor);
  }
  
  private static String getInsertValue(String name, String relativePath, String jarID) {
    return SchemaUtils.getSerialInsertValue(
        SchemaUtils.convertNotNullVarchar(name),
        SchemaUtils.convertNotNullVarchar(relativePath),
        SchemaUtils.convertNotNullNumber(jarID));
  }
  
  public static <T> void insert(KeyInsertBatcher<T> batcher, FileEX file, String jarID, T pairing) {
    batcher.addValue(getInsertValue(file.getName(), file.getRelativePath(), jarID), pairing);
  }
  
  // ---- DELETE ----
  public static void deleteByJarID(QueryExecutor executor, String jarID) {
    executor.delete(TABLE, "jar_id=" + jarID);
  }
}
