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
import edu.uci.ics.sourcerer.util.Averager;
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
   *  | sum          | FLOAT           | Yes   | No     |
   *  | mean         | FLOAT           | Yes   | No     |
   *  | median       | FLOAT           | Yes   | No     |
   *  | min          | FLOAT           | Yes   | No     |
   *  | max          | FLOAT           | Yes   | No     |
   *  +--------------+-----------------+-------+--------+
   */
  public static final ProjectMetricsTable TABLE = new ProjectMetricsTable();

  public static final Column<Integer> PROJECT_ID = TABLE.addIDColumn("project_id", false).addIndex();
  public static final Column<Metric> METRIC_TYPE = TABLE.addEnumColumn("metric_type", Metric.values(), false);
  public static final Column<Double> SUM = TABLE.addDoubleColumn("sum", 23, 3, true);
  public static final Column<Double> MEAN = TABLE.addDoubleColumn("mean", 23, 3, true);
  public static final Column<Double> MEDIAN = TABLE.addDoubleColumn("median", 23, 3, true);
  public static final Column<Double> MIN = TABLE.addDoubleColumn("min", 23, 3, true);
  public static final Column<Double> MAX = TABLE.addDoubleColumn("max", 23, 3, true);
  
  private ProjectMetricsTable() {
    super("project_metrics");
  }
  
  public static Insert createInsert(Integer projectID, Metric metric, Double sum, Double mean, Double median, Double min, Double max) {
    return TABLE.createInsert(
        PROJECT_ID.to(projectID), 
        METRIC_TYPE.to(metric), 
        SUM.to(sum),
        MEAN.to(mean),
        MEDIAN.to(median),
        MIN.to(min),
        MAX.to(max));
  }
  
  public static Insert createInsert(Integer projectID, Metric metric, Averager<Double> avg) {
    return createInsert(projectID, metric, avg.getSum(), avg.getMean(), avg.getMedian(), avg.getMin(), avg.getMax());
  }
}
