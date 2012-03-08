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
public class ClusterToJarTable extends DatabaseTable {
  /*
   *             cluster_to_jar table
   * +-------------+-----------------+-------+--------+
   * | Column name | Type            | Null? | Index? |
   * +-------------+-----------------+-------+--------+
   * | cluster_id  | BIGINT UNSIGNED | No    | Yes    |
   * | jar_id      | BIGINT UNSIGNED | No    | Yes    |
   * +-------------+-----------------+-------+--------+   
   */
  public static final ClusterToJarTable TABLE = new ClusterToJarTable();
  
  public static final Column<Integer> CLUSTER_ID = TABLE.addIDColumn("cluster_id", false).addIndex();
  public static final Column<Integer> JAR_ID = TABLE.addIDColumn("jar_id", false).addIndex();
    
  private ClusterToJarTable() {
    super("cluster_to_jar");
  }
  
  // ---- INSERT ----
  public static Insert createInsert(Integer clusterID, Integer jarID) {
    return TABLE.makeInsert(CLUSTER_ID.to(clusterID), JAR_ID.to(jarID));
  }
}
