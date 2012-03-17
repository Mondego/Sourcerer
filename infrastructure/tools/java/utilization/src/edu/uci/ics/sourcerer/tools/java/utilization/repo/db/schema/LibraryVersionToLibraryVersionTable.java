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
public class LibraryVersionToLibraryVersionTable extends DatabaseTable{
  /*
   *      library_version_to_library_version table
   * +-------------+-----------------+-------+--------+
   * | Column name | Type            | Null? | Index? |
   * +-------------+-----------------+-------+--------+
   * | source_id   | BIGINT UNSIGNED | No    | Yes    |
   * | target_id   | BIGINT UNSIGNED | No    | Yes    |
   * +-------------+-----------------+-------+--------+   
   */
  public static final LibraryVersionToLibraryVersionTable TABLE = new LibraryVersionToLibraryVersionTable();
  
  public static final Column<Integer> SOURCE_ID = TABLE.addIDColumn("source_id", false).addIndex();
  public static final Column<Integer> TARGET_ID = TABLE.addIDColumn("target_id", false).addIndex();
  
  private LibraryVersionToLibraryVersionTable() {
    super("library_version_to_library_version");
  }
  
  // ---- INSERT ----
  public static Insert createInsert(Integer sourceID, Integer targetID) {
    return TABLE.makeInsert(SOURCE_ID.to(sourceID), TARGET_ID.to(targetID));
  }
}
