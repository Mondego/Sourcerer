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
public class FqnVersionsTable extends DatabaseTable {
  /*  
   *                 fqn_versions table
   *  +----------------+-----------------+-------+--------+
   *  | Column name    | Type            | Null? | Index? |
   *  +----------------+-----------------+-------+--------+
   *  | fqn_version_id | SERIAL          | No    | Yes    |
   *  | fqn_id         | BIGINT UNSIGNED | No    | Yes    |
   *  | fingerprint    | VARCHAR(8192)   | Yes   | No     |
   *  +----------------+-----------------+-------+--------+
   */
  
  public static final FqnVersionsTable TABLE = new FqnVersionsTable();
  
  public static final Column<Integer> FQN_VERSION_ID = TABLE.addSerialColumn("fqn_version_id");
  public static final Column<Integer> FQN_ID = TABLE.addIDColumn("fqn_id", false).addIndex();
  public static final StringColumn FINGERPRINT = TABLE.addVarcharColumn("fingerprint", 8192, true);
  
  private FqnVersionsTable() {
    super("fqn_versions");
  }
  
  // ---- INSERT ----
  public static Insert createInsert(Integer fqnID, String fingerprint) {
    return TABLE.makeInsert(FQN_ID.to(fqnID), FINGERPRINT.to(fingerprint));
  }
}
