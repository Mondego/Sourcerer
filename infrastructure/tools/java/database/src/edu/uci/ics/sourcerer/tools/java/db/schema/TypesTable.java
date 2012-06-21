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

import edu.uci.ics.sourcerer.tools.java.model.types.Type;
import edu.uci.ics.sourcerer.utils.db.Insert;
import edu.uci.ics.sourcerer.utils.db.sql.Column;
import edu.uci.ics.sourcerer.utils.db.sql.DatabaseTable;
import edu.uci.ics.sourcerer.utils.db.sql.StringColumn;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class TypesTable extends DatabaseTable {
  /*  
   *                       types table
   *  +---------------+-----------------+-------+--------+
   *  | Column name   | Type            | Null? | Index? |
   *  +---------------+-----------------+-------+--------+
   *  | type_id       | SERIAL          | No    | Yes    |
   *  | type          | ENUM(values)    | No    | No     |
   *  | fqn           | VARCHAR(8192)   | No    | Yes    |
   *  | component_id  | BIGINT UNSIGNED | No    | Yes    |
   *  +---------------+-----------------+-------+--------+
   */
  
  public static final TypesTable TABLE = new TypesTable();
  
  public static final Column<Integer> TYPE_ID = TABLE.addSerialColumn("type_id");
  public static final Column<Type> TYPE = TABLE.addEnumColumn("type", Type.values(), false);
  public static final StringColumn FQN = TABLE.addVarcharColumn("fqn", 8192, false).addIndex(48);
  public static final Column<Integer> COMPONENT_ID = TABLE.addIDColumn("component_id", false).addIndex();
  
  
  private TypesTable() {
    super("types");
  }
  
  // ---- INSERT ----
  public static Insert createInsert(Type type, String fqn, Integer componentID) {
    return TABLE.createInsert(TYPE.to(type), FQN.to(fqn), COMPONENT_ID.to(componentID));
  }
}
