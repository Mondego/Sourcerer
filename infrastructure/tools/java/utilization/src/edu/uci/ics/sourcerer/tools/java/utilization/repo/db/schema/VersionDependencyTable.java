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
package edu.uci.ics.sourcerer.tools.java.utilization.repo.db.schema;

import edu.uci.ics.sourcerer.utils.db.Insert;
import edu.uci.ics.sourcerer.utils.db.sql.Column;
import edu.uci.ics.sourcerer.utils.db.sql.DatabaseTable;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class VersionDependencyTable extends DatabaseTable{
  /*
   *                 version_dep table
   * +-------------+-----------------+-------+--------+
   * | Column name | Type            | Null? | Index? |
   * +-------------+-----------------+-------+--------+
   * | source_vid  | BIGINT UNSIGNED | No    | Yes    |
   * | target_vid  | BIGINT UNSIGNED | No    | Yes    |
   * +-------------+-----------------+-------+--------+   
   */
  public static final VersionDependencyTable TABLE = new VersionDependencyTable();
  
  public static final Column<Integer> SOURCE_VID = TABLE.addIDColumn("source_vid", false).addIndex();
  public static final Column<Integer> TARGET_VID = TABLE.addIDColumn("target_vid", false).addIndex();
  
  private VersionDependencyTable() {
    super("version_dep");
  }
  
  // ---- INSERT ----
  public static Insert createInsert(Integer sourceVID, Integer targetVID) {
    return TABLE.makeInsert(SOURCE_VID.to(sourceVID), TARGET_VID.to(targetVID));
  }
}
