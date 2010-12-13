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
import edu.uci.ics.sourcerer.db.util.columns.BooleanColumn;
import edu.uci.ics.sourcerer.db.util.columns.Column;
import edu.uci.ics.sourcerer.db.util.columns.IntColumn;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public final class ImportsTable extends DatabaseTable {
  public static final String TABLE = "imports";
  public ImportsTable(QueryExecutor executor, TableLocker locker) {
    super(executor, locker, TABLE);
  }
  /*  
   *  +-------------+-------------------+-------+--------+
   *  | Column name | Type              | Null? | Index? |
   *  +-------------+-------------------+-------+--------+
   *  | static      | BOOLEAN           | No    | No     |
   *  | on_demand   | BOOLEAN           | No    | No     |
   *  | eid         | BIGINT UNSIGNED   | No    | Yes    |
   *  | project_id  | BIGINT UNSIGNED   | No    | Yes    |
   *  | file_id     | BIGINT UNSIGNED   | No    | Yes    |
   *  | offset      | INT UNSIGNED      | No    | No     |
   *  | length      | INT UNSIGNED      | No    | No     |
   *  +-------------+-------------------+-------+--------+
   */
  
  public static final Column<Boolean> STATIC = BooleanColumn.getBooleanNotNull("static", TABLE);
  public static final Column<Boolean> ON_DEMAND = BooleanColumn.getBooleanNotNull("on_demand", TABLE);
  public static final Column<Integer> EID = IntColumn.getID("eid", TABLE).addIndex();
  public static final Column<Integer> PROJECT_ID = IntColumn.getID("project_id", TABLE).addIndex();
  public static final Column<Integer> FILE_ID = IntColumn.getID("file_id", TABLE).addIndex();
  public static final Column<Integer> OFFSET = IntColumn.getUnsignedIntNotNull("offset", TABLE);
  public static final Column<Integer> LENGTH = IntColumn.getUnsignedIntNotNull("length", TABLE);
  
  // ---- CREATE ----
  public void createTable() {
    executor.createTable(table, 
        STATIC,
        ON_DEMAND,
        EID,
        PROJECT_ID,
        FILE_ID,
        OFFSET,
        LENGTH);
  }
  
  // ---- INSERT ----
  private String getInsertValue(boolean isStatic, boolean onDemand, Integer eid, Integer projectID, Integer fileID, Integer offset, Integer length) {
    return buildInsertValue(
        STATIC.convertToDB(isStatic),
        ON_DEMAND.convertToDB(onDemand),
        EID.convertToDB(eid),
        PROJECT_ID.convertToDB(projectID),
        FILE_ID.convertToDB(fileID),
        OFFSET.convertToDB(offset),
        LENGTH.convertToDB(length));
  }
  
  public void insert(boolean isStatic, boolean onDemand, Integer eid, Integer projectID, Integer fileID, Integer offset, Integer length) {
    inserter.addValue(getInsertValue(isStatic, onDemand, eid, projectID, fileID, offset, length));
  }
  
  // ---- DELETE ----
  public void deleteByProjectID(Integer projectID) {
    executor.delete(table, PROJECT_ID.getEquals(projectID));
  }
}
