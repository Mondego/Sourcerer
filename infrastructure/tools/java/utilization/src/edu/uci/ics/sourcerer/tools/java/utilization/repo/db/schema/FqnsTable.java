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
public class FqnsTable extends DatabaseTable {
  /*  
   *                       fqns table
   *  +-------------+-----------------+-------+--------+
   *  | Column name | Type            | Null? | Index? |
   *  +-------------+-----------------+-------+--------+
   *  | fqn_id      | SERIAL          | No    | Yes    |
   *  | fqn         | VARCHAR(8192)   | No    | Yes    |
   *  +-------------+-----------------+-------+--------+
   */
  
  public static final FqnsTable TABLE = new FqnsTable();
  
  public static final Column<Integer> FQN_ID = TABLE.addSerialColumn("fqn_id");
  public static final StringColumn FQN = TABLE.addVarcharColumn("fqn", 8192, false).addIndex(48);
  
  private FqnsTable() {
    super("fqns");
  }
  
  // ---- INSERT ----
  public static Insert createInsert(String fqn) {
    return TABLE.makeInsert(FQN.to(fqn));
  }
}
