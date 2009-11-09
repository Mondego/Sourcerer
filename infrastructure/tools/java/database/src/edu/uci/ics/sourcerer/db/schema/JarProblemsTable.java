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
import edu.uci.ics.sourcerer.model.Problem;
import edu.uci.ics.sourcerer.model.extracted.ProblemEX;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class JarProblemsTable {
  private JarProblemsTable() {}
  
  public static final String TABLE = "jar_problems";
  /*  
   *  +--------------+-----------------+-------+--------+
   *  | Column name  | Type            | Null? | Index? |
   *  +--------------+-----------------+-------+--------+
   *  | problem_type | ENUM(values)    | No    | Yes    |
   *  | error_code   | INT UNSIGNED    | No    | Yes    |
   *  | message      | VARCHAR(1024)   | No    | No     |
   *  | jar_id       | BIGINT UNSIGNED | No    | Yes    |
   *  | jclass_fid   | BIGINT UNSIGNED | No    | Yes    |
   *  +--------------+-----------------+-------+--------+
   */
  
  // ---- CREATE ----
  public static void createTable(QueryExecutor executor) {
    executor.createTable(TABLE, 
        "problem_type " + SchemaUtils.getEnumCreate(Problem.values()) + " NOT NULL",
        "error_code INT UNSIGNED NOT NULL",
        "message VARCHAR(1024) BINARY NOT NULL",
        "jar_id BIGINT UNSIGNED NOT NULL",
        "jclass_fid BIGINT UNSIGNED NOT NULL",
        "INDEX(problem_type)",
        "INDEX(error_code)",
        "INDEX(jar_id)",
        "INDEX(jclass_fid)");
  }
  
  // ---- INSERT ----
  public static InsertBatcher getInsertBatcher(QueryExecutor executor) {
    return executor.getInsertBatcher(TABLE);
  }
  
  private static String getInsertValue(Problem type, String errorCode, String message, String jarID, String jarClassFileID) {
    return SchemaUtils.getInsertValue(
        SchemaUtils.convertNotNullVarchar(type.name()),
        SchemaUtils.convertNotNullNumber(errorCode),
        SchemaUtils.convertNotNullVarchar(message),
        SchemaUtils.convertNotNullNumber(jarID),
        SchemaUtils.convertNotNullNumber(jarClassFileID));
  }
  
  public static void insert(InsertBatcher batcher, ProblemEX problem, String jarID, String jarClassFileID) {
    batcher.addValue(getInsertValue(problem.getType(), problem.getErrorCode(), problem.getMessage(), jarID, jarClassFileID));
  }
  
  // ---- DELETE ----
  public static void deleteByJarID(QueryExecutor executor, String jarID) {
    executor.delete(TABLE, "jar_id=" + jarID);
  }
}
