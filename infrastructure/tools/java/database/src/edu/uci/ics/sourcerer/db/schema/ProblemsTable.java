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
import edu.uci.ics.sourcerer.db.util.TableLocker;
import edu.uci.ics.sourcerer.model.Problem;
import edu.uci.ics.sourcerer.model.extracted.ProblemEX;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public final class ProblemsTable extends DatabaseTable {
  protected ProblemsTable(QueryExecutor executor, TableLocker locker) {
    super(executor, locker, "problems", true);
  }
  
  /*  
   *  +--------------+-----------------+-------+--------+
   *  | Column name  | Type            | Null? | Index? |
   *  +--------------+-----------------+-------+--------+
   *  | problem_type | ENUM(values)    | No    | Yes    |
   *  | error_code   | INT UNSIGNED    | No    | Yes    |
   *  | message      | VARCHAR(1024)   | No    | No     |
   *  | project_id   | BIGINT UNSIGNED | No    | Yes    |
   *  | file_id      | BIGINT UNSIGNED | No    | Yes    |
   *  +--------------+-----------------+-------+--------+
   */
  
  // ---- CREATE ----
  public void createTable() {
    executor.createTable(name, 
        "problem_type " + getEnumCreate(Problem.values()) + " NOT NULL",
        "error_code INT UNSIGNED NOT NULL",
        "message VARCHAR(1024) BINARY NOT NULL",
        "project_id BIGINT UNSIGNED NOT NULL",
        "file_id BIGINT UNSIGNED NOT NULL",
        "INDEX(problem_type)",
        "INDEX(error_code)",
        "INDEX(project_id)",
        "INDEX(file_id)");
  }
  
  // ---- INSERT ----
  private String getInsertValue(Problem type, String errorCode, String message, String projectID, String fileID) {
    return buildInsertValue(
        convertNotNullVarchar(type.name()),
        convertNotNullNumber(errorCode),
        convertNotNullVarchar(message),
        convertNotNullNumber(projectID),
        convertNotNullNumber(fileID));
  }
  
  public void insert(ProblemEX problem, String projectID, String fileID) {
    batcher.addValue(getInsertValue(problem.getType(), problem.getErrorCode(), problem.getMessage(), projectID, fileID));
  }
  
  // ---- DELETE ----
  public void deleteByProjectID(String projectID) {
    executor.delete(name, "project_id=" + projectID);
  }
}
