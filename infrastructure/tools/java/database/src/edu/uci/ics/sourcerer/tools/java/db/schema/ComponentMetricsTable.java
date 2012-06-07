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

import edu.uci.ics.sourcerer.tools.java.model.types.ComponentMetric;
import edu.uci.ics.sourcerer.utils.db.Insert;
import edu.uci.ics.sourcerer.utils.db.sql.Column;
import edu.uci.ics.sourcerer.utils.db.sql.DatabaseTable;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class ComponentMetricsTable extends DatabaseTable {
  /*
   *               component-metrics table
   * +---------------+-----------------+-------+--------+
   * | Column name   | Type            | Null? | Index? |
   * +---------------+-----------------+-------+--------+
   * | component_id  | BIGINT UNSIGNED | No    | Yes    |
   * | metric_type   | ENUM(values)    | No    | No     |
   * | value         | INT             | No    | No     |
   * +---------------+-----------------+-------+--------+   
   */
  
  public static final ComponentMetricsTable TABLE = new ComponentMetricsTable();
  
  public static final Column<Integer> COMPONENT_ID = TABLE.addIDColumn("component_id", false).addIndex();
  public static final Column<ComponentMetric> METRIC_TYPE = TABLE.addEnumColumn("metric_type", ComponentMetric.values(), false);
  public static final Column<Integer> VALUE = TABLE.addIntColumn("value", false, false);
  
  private ComponentMetricsTable() {
    super("component_metrics");
  }
  
  public static Insert createInsert(Integer componentID, ComponentMetric metric, Integer value) {
    return TABLE.createInsert(COMPONENT_ID.to(componentID), METRIC_TYPE.to(metric), VALUE.to(value));
  }
}
