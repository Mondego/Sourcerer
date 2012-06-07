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

import edu.uci.ics.sourcerer.tools.java.model.types.Metric;
import edu.uci.ics.sourcerer.utils.db.Insert;
import edu.uci.ics.sourcerer.utils.db.sql.Column;
import edu.uci.ics.sourcerer.utils.db.sql.DatabaseTable;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class ProjectMetricsTable extends DatabaseTable {
  /*  
   *                project_metrics table
   *  +--------------+-----------------+-------+--------+
   *  | Column name  | Type            | Null? | Index? |
   *  +--------------+-----------------+-------+--------+
   *  | project_id   | BIGINT UNISNGED | No    | Yes    | 
   *  | metric_type  | ENUM(values)    | No    | No     | 
   *  | value        | INT             | No    | No     | 
   *  +--------------+-----------------+-------+--------+
   */
  public static final ProjectMetricsTable TABLE = new ProjectMetricsTable();

  public static final Column<Integer> PROJECT_ID = TABLE.addIDColumn("project_id", false).addIndex();
  public static final Column<Metric> METRIC_TYPE = TABLE.addEnumColumn("metric_type", Metric.values(), false);
  public static final Column<Integer> VALUE = TABLE.addIntColumn("value", false, false);
  
  private ProjectMetricsTable() {
    super("project_metrics");
  }
  
  public static Insert createInsert(Integer projectID, Metric metric, Integer value) {
    return TABLE.createInsert(PROJECT_ID.to(projectID), METRIC_TYPE.to(metric), VALUE.to(value));
  }
}
