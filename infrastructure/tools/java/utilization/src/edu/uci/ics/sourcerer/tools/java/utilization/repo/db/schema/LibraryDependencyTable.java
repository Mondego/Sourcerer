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
public class LibraryDependencyTable extends DatabaseTable{
  /*
   *                 library_dep table
   * +-------------+-----------------+-------+--------+
   * | Column name | Type            | Null? | Index? |
   * +-------------+-----------------+-------+--------+
   * | source_lid  | BIGINT UNSIGNED | No    | Yes    |
   * | target_lid  | BIGINT UNSIGNED | No    | Yes    |
   * +-------------+-----------------+-------+--------+   
   */
  public static final LibraryDependencyTable TABLE = new LibraryDependencyTable();
  
  public static final Column<Integer> SOURCE_LID = TABLE.addIDColumn("source_lid", false).addIndex();
  public static final Column<Integer> TARGET_LID = TABLE.addIDColumn("target_lid", false).addIndex();
  
  private LibraryDependencyTable() {
    super("library_dep");
  }
  
  // ---- INSERT ----
  public static Insert createInsert(Integer sourceLID, Integer targetLID) {
    return TABLE.makeInsert(SOURCE_LID.to(sourceLID), TARGET_LID.to(targetLID));
  }
}
