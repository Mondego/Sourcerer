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
import edu.uci.ics.sourcerer.utils.db.sql.StringColumn;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class JarsTable extends DatabaseTable {
  /*
   *                 libraries table
   * +-------------+-----------------+-------+--------+
   * | Column name | Type            | Null? | Index? |
   * +-------------+-----------------+-------+--------+
   * | jar_id      | SERIAL          | No    | Yes    |
   * | name        | VARCHAR(128)    | No    | Yes    |
   * | hash        | VARCHAR(32)     | No    | Yes    |
   * +-------------+-----------------+-------+--------+   
   */
  public static final JarsTable TABLE = new JarsTable();
  
  public static final Column<Integer> JAR_ID = TABLE.addSerialColumn("jar_id");
  public static final StringColumn NAME = TABLE.addVarcharColumn("name", 128, false).addIndex(48);
  public static final Column<String> HASH = TABLE.addVarcharColumn("hash", 32, false).addIndex();
  
  private JarsTable() {
    super("jars");
  }
  
  // ---- INSERT ----
  public static Insert createInsert(String name, String hash) {
    return TABLE.makeInsert(NAME.to(name), HASH.to(hash));
  }
}
