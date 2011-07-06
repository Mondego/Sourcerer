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

import edu.uci.ics.sourcerer.model.Problem;
import edu.uci.ics.sourcerer.model.extracted.ProblemEX;
import edu.uci.ics.sourcerer.util.db.columns.Column;
import edu.uci.ics.sourcerer.util.db.columns.EnumColumn;
import edu.uci.ics.sourcerer.util.db.columns.IntColumn;
import edu.uci.ics.sourcerer.util.db.columns.StringColumn;
import edu.uci.ics.sourcerer.utils.db.DatabaseTable;
import edu.uci.ics.sourcerer.utils.db.QueryExecutor;
import edu.uci.ics.sourcerer.utils.db.TableLocker;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public final class ProblemsTable extends DatabaseTable {
  public static final String TABLE = "problems";
  public ProblemsTable(QueryExecutor executor, TableLocker locker) {
    super(executor, locker, TABLE);
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
  
  public static final Column<Problem> PROBLEM_TYPE = new EnumColumn<Problem>("problem_type", TABLE, Problem.values(), false) {
    @Override
    public Problem convertFromDB(String value) {
      return Problem.valueOf(value);
    }
  }.addIndex();
  public static final Column<Integer> ERROR_CODE = IntColumn.getUnsignedInt("error_code", TABLE).addIndex();
  public static final Column<String> MESSAGE = StringColumn.getVarchar1024NotNull("message", TABLE);
  public static final Column<Integer> PROJECT_ID = IntColumn.getID("project_id", TABLE).addIndex();
  public static final Column<Integer> FILE_ID = IntColumn.getID("file_id", TABLE).addIndex();
  
  // ---- CREATE ----
  public void createTable() {
    executor.createTable(table, 
        PROBLEM_TYPE,
        ERROR_CODE,
        MESSAGE,
        PROJECT_ID,
        FILE_ID);
  }
  
  // ---- INSERT ----
  private String getInsertValue(Problem type, Integer errorCode, String message, Integer projectID, Integer fileID) {
    return buildInsertValue(
        PROBLEM_TYPE.convertToDB(type),
        ERROR_CODE.convertToDB(errorCode),
        MESSAGE.convertToDB(message),
        PROJECT_ID.convertToDB(projectID),
        FILE_ID.convertToDB(fileID));
  }
  
  private String getInsertValue(ProblemEX problem, Integer projectID, Integer fileID) {
    return getInsertValue(problem.getType(), problem.getErrorCode(), problem.getMessage(), projectID, fileID);
  }
  
  public void insert(ProblemEX problem, Integer projectID, Integer fileID) {
    inserter.addValue(getInsertValue(problem, projectID, fileID));
  }
  
  // ---- DELETE ----
  public void deleteByProjectID(Integer projectID) {
    executor.delete(table, PROJECT_ID.getEquals(projectID));
  }
}
