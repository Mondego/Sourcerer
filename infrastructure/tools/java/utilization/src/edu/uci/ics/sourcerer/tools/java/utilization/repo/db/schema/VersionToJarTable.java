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
public class VersionToJarTable extends DatabaseTable {
  /*
   *             version_to_jar table
   * +-------------+-----------------+-------+--------+
   * | Column name | Type            | Null? | Index? |
   * +-------------+-----------------+-------+--------+
   * | version_id  | BIGINT UNSIGNED | No    | Yes    |
   * | jar_id      | BIGINT UNSIGNED | No    | Yes    |
   * +-------------+-----------------+-------+--------+   
   */
  public static final VersionToJarTable TABLE = new VersionToJarTable();
  
  public static final Column<Integer> VERSION_ID = TABLE.addIDColumn("version_id", false).addIndex();
  public static final Column<Integer> JAR_ID = TABLE.addIDColumn("jar_id", false).addIndex();
    
  private VersionToJarTable() {
    super("version_to_jar");
  }
  
  // ---- INSERT ----
  public static Insert createInsert(Integer versionID, Integer jarID) {
    return TABLE.makeInsert(VERSION_ID.to(versionID), JAR_ID.to(jarID));
  }
}
