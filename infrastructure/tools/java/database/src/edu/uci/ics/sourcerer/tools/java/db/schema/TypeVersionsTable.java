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

import edu.uci.ics.sourcerer.utils.db.Insert;
import edu.uci.ics.sourcerer.utils.db.sql.Column;
import edu.uci.ics.sourcerer.utils.db.sql.DatabaseTable;
import edu.uci.ics.sourcerer.utils.db.sql.StringColumn;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class TypeVersionsTable extends DatabaseTable {
  /*  
   *                 type_versions table
   *  +------------------+-----------------+-------+--------+
   *  | Column name      | Type            | Null? | Index? |
   *  +------------------+-----------------+-------+--------+
   *  | type_version_id  | SERIAL          | No    | Yes    |
   *  | type_id          | BIGINT UNSIGNED | No    | Yes    |
   *  | fingerprint      | VARCHAR(8192)   | Yes   | No     |
   *  +------------------+-----------------+-------+--------+
   */
  
  public static final TypeVersionsTable TABLE = new TypeVersionsTable();
  
  public static final Column<Integer> TYPE_VERSION_ID = TABLE.addSerialColumn("type_version_id");
  public static final Column<Integer> TYPE_ID = TABLE.addIDColumn("type_id", false).addIndex();
  public static final StringColumn FINGERPRINT = TABLE.addVarcharColumn("fingerprint", 8192, true);
  
  private TypeVersionsTable() {
    super("type_versions");
  }
  
  // ---- INSERT ----
  public static Insert createInsert(Integer typeID, String fingerprint) {
    return TABLE.createInsert(TYPE_ID.to(typeID), FINGERPRINT.to(fingerprint));
  }
}
