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

import edu.uci.ics.sourcerer.tools.java.utilization.repo.LibraryVersion;
import edu.uci.ics.sourcerer.utils.db.Insert;
import edu.uci.ics.sourcerer.utils.db.sql.Column;
import edu.uci.ics.sourcerer.utils.db.sql.DatabaseTable;
import edu.uci.ics.sourcerer.utils.db.sql.StringColumn;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class VersionsTable extends DatabaseTable {
  /*
   *              versions table
   * +-------------+-----------------+-------+--------+
   * | Column name | Type            | Null? | Index? |
   * +-------------+-----------------+-------+--------+
   * | version_id  | SERIAL          | No    | Yes    |
   * | name        | VARCHAR(128)    | Yes   | Yes    |
   * | library_id  | BIGINT UNSIGNED | No    | Yes    |
   * +-------------+-----------------+-------+--------+   
   */
  public static final VersionsTable TABLE = new VersionsTable();
  
  public static final Column<Integer> VERSION_ID = TABLE.addSerialColumn("version_id");
  public static final StringColumn NAME = TABLE.addVarcharColumn("name", 128, true).addIndex(48);
  public static final Column<Integer> LIBRARY_ID = TABLE.addIDColumn("library_id", false).addIndex();
  
  private VersionsTable() {
    super("versions");
  }
  
  // ---- INSERT ----
  private static Insert createInsert(String name, Integer libraryID) {
    return TABLE.makeInsert(NAME.to(name), LIBRARY_ID.to(libraryID));
  }
  
  public static Insert createInsert(LibraryVersion version, Integer libraryID) {
    return createInsert((String) null, libraryID);
  }
}
