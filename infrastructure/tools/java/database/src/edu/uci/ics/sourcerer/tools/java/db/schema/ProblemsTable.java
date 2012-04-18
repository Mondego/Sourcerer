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
package edu.uci.ics.sourcerer.tools.java.db.schema;

import edu.uci.ics.sourcerer.tools.java.model.extracted.ProblemEX;
import edu.uci.ics.sourcerer.tools.java.model.types.Problem;
import edu.uci.ics.sourcerer.utils.db.Insert;
import edu.uci.ics.sourcerer.utils.db.sql.Column;
import edu.uci.ics.sourcerer.utils.db.sql.DatabaseTable;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public final class ProblemsTable extends DatabaseTable {
  /*  
   *                    problems table
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
  public static final ProblemsTable TABLE = new ProblemsTable();
  
  public static final Column<Problem> PROBLEM_TYPE = TABLE.addEnumColumn("problem_type", Problem.values(), false).addIndex();
  public static final Column<Integer> ERROR_CODE = TABLE.addIntColumn("error_code", true, false).addIndex();
  public static final Column<String> MESSAGE = TABLE.addVarcharColumn("message", 1024, false);
  public static final Column<Integer> PROJECT_ID = TABLE.addIDColumn("project_id", false).addIndex();
  public static final Column<Integer> FILE_ID = TABLE.addIDColumn("file_id", false).addIndex();
  
  private ProblemsTable() {
    super("problems");
  }
  
  // ---- INSERT ----
  private static Insert makeInsert(Problem type, Integer errorCode, String message, Integer projectID, Integer fileID) {
    return TABLE.createInsert(
        PROBLEM_TYPE.to(type),
        ERROR_CODE.to(errorCode),
        MESSAGE.to(message),
        PROJECT_ID.to(projectID),
        FILE_ID.to(fileID));
  }
  
  public static Insert makeInsert(ProblemEX problem, Integer projectID, Integer fileID) {
    return makeInsert(problem.getType(), problem.getErrorCode(), problem.getMessage(), projectID, fileID);
  }
//  
//  public void insert(ProblemEX problem, Integer projectID, Integer fileID) {
//    inserter.addValue(getInsertValue(problem, projectID, fileID));
//  }
//  
//  // ---- DELETE ----
//  public void deleteByProjectID(Integer projectID) {
//    executor.delete(table, PROJECT_ID.getEquals(projectID));
//  }
}
