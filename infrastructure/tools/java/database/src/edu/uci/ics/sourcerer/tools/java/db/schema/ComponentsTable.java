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

import edu.uci.ics.sourcerer.tools.java.utilization.model.cluster.Cluster;
import edu.uci.ics.sourcerer.tools.java.utilization.model.cluster.ClusterVersion;
import edu.uci.ics.sourcerer.tools.java.utilization.repo.Library;
import edu.uci.ics.sourcerer.tools.java.utilization.repo.LibraryVersion;
import edu.uci.ics.sourcerer.utils.db.Insert;
import edu.uci.ics.sourcerer.utils.db.sql.Column;
import edu.uci.ics.sourcerer.utils.db.sql.DatabaseTable;
import edu.uci.ics.sourcerer.utils.db.sql.StringColumn;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class ComponentsTable extends DatabaseTable {
  /*
   *                  components table
   * +---------------+-----------------+-------+--------+
   * | Column name   | Type            | Null? | Index? |
   * +---------------+-----------------+-------+--------+
   * | component_id  | SERIAL          | No    | Yes    |
   * | type          | ENUM(values)    | No    | Yes    |
   * | name          | VARCHAR(128)    | Yes   | Yes    |
   * +---------------+-----------------+-------+--------+   
   */  
  public static final ComponentsTable TABLE = new ComponentsTable();
  
  public static final Column<Integer> COMPONENT_ID = TABLE.addSerialColumn("cluster_id");
  public static final Column<Component> TYPE = TABLE.addEnumColumn("type", Component.values(), false).addIndex();
  public static final StringColumn NAME = TABLE.addVarcharColumn("name", 128, true).addIndex(48);
  
  private ComponentsTable() {
    super("components");
  }
  
  // ---- INSERT ----
  public static Insert createInsert(Component type, String name) {
    return TABLE.createInsert(TYPE.to(type), NAME.to(name));
  }
  
  public static Insert createInsert(Cluster cluster) {
    return createInsert(Component.CLUSTER, null);
  }
  
  public static Insert createInsert(ClusterVersion cluster) {
    return createInsert(Component.CLUSTER_VERSION, null);
  }
  
  public static Insert createInsert(Library library) {
    return createInsert(Component.LIBRARY, null);
  }
  
  public static Insert createInsert(LibraryVersion library) {
    return createInsert(Component.LIBRARY_VERSION, null);
  }
}
