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

import edu.uci.ics.sourcerer.tools.java.model.types.Location;
import edu.uci.ics.sourcerer.utils.db.Insert;
import edu.uci.ics.sourcerer.utils.db.sql.Column;
import edu.uci.ics.sourcerer.utils.db.sql.DatabaseTable;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public final class ImportsTable extends DatabaseTable {
  /* 
   *                    imports table 
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
  public static final ImportsTable TABLE = new ImportsTable();
  
  public static final Column<Boolean> STATIC = TABLE.addBooleanColumn("static", false);
  public static final Column<Boolean> ON_DEMAND = TABLE.addBooleanColumn("on_demand", false);
  public static final Column<Integer> EID = TABLE.addIDColumn("eid", false).addIndex();
  public static final Column<Integer> PROJECT_ID = TABLE.addIDColumn("project_id", false).addIndex();
  public static final Column<Integer> FILE_ID = TABLE.addIDColumn("file_id", false).addIndex();
  public static final Column<Integer> OFFSET = TABLE.addIntColumn("offset", true, false);
  public static final Column<Integer> LENGTH = TABLE.addIntColumn("length", true, false);

  private ImportsTable() {
    super("imports");
  }
  
  // ---- INSERT ----
  private static Insert makeInsert(boolean isStatic, boolean onDemand, Integer eid, Integer projectID, Integer fileID, Integer offset, Integer length) {
    return TABLE.createInsert(
        STATIC.to(isStatic),
        ON_DEMAND.to(onDemand),
        EID.to(eid),
        PROJECT_ID.to(projectID),
        FILE_ID.to(fileID),
        OFFSET.to(offset),
        LENGTH.to(length));
  }
  
  public static Insert makeInsert(boolean isStatic, boolean onDemand, Integer eid, Integer projectID) {
    return makeInsert(isStatic, onDemand, eid, projectID, null, null, null);
  }
  
  public static Insert makeInsert(boolean isStatic, boolean onDemand, Integer eid, Integer projectID, Integer fileID, Location loc) {
    return makeInsert(isStatic, onDemand, eid, projectID, fileID, loc.getOffset(), loc.getLength());
  }
}
