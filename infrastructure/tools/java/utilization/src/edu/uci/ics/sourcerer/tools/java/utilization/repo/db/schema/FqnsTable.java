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

import edu.uci.ics.sourcerer.tools.java.utilization.model.jar.VersionedFqnNode;
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
   *  | cluster_id  | BIGINT UNSIGNED | No    | Yes    |
   *  | type        | ENUM(values)    | No    | No     |
   *  +-------------+-----------------+-------+--------+
   */
  
  public static final FqnsTable TABLE = new FqnsTable();
  
  public static final Column<Integer> FQN_ID = TABLE.addSerialColumn("fqn_id");
  public static final StringColumn FQN = TABLE.addVarcharColumn("fqn", 8192, false).addIndex(48);
  public static final Column<Integer> CLUSTER_ID = TABLE.addIDColumn("cluster_id", false).addIndex();
  public static final Column<ClusterFqnType> TYPE = TABLE.addEnumColumn("type", ClusterFqnType.values(), false);
  
  private FqnsTable() {
    super("fqns");
  }
  
  // ---- INSERT ----
  private static Insert createInsert(String fqn, Integer clusterID, ClusterFqnType type) {
    return TABLE.makeInsert(FQN.to(fqn), CLUSTER_ID.to(clusterID), TYPE.to(type));
  }
  
  public static Insert createInsert(VersionedFqnNode fqn, Integer clusterID, ClusterFqnType type) {
    return createInsert(fqn.getFqn(), clusterID, type);
  }
}
