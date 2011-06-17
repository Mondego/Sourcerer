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
package edu.uci.ics.sourcerer.db.schema;

import edu.uci.ics.sourcerer.db.util.QueryExecutor;
import edu.uci.ics.sourcerer.db.util.TableLocker;
import edu.uci.ics.sourcerer.db.util.columns.Column;
import edu.uci.ics.sourcerer.db.util.columns.EnumColumn;
import edu.uci.ics.sourcerer.db.util.columns.IntColumn;
import edu.uci.ics.sourcerer.model.metrics.Metric;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class EntityMetricsTable extends DatabaseTable {
  public static final String TABLE = "entity_metrics";
  
  public EntityMetricsTable(QueryExecutor executor, TableLocker locker) {
    super(executor, locker, TABLE);
  }
  
  /*  
   *  +--------------+-----------------+-------+--------+
   *  | Column name  | Type            | Null? | Index? |
   *  +--------------+-----------------+-------+--------+
   *  | project_id   | BIGINT UNISNGED | No    | Yes    |
   *  | file_id      | BIGINT UNISNGED | No    | Yes    |
   *  | entity_id    | BIGINT UNISNGED | No    | Yes    | 
   *  | metric_type  | ENUM(values)    | No    | No     | 
   *  | value        | INT             | No    | No     | 
   *  +--------------+-----------------+-------+--------+
   */
  
  public static final Column<Integer> PROJECT_ID = IntColumn.getID("project_id", TABLE).addIndex();
  public static final Column<Integer> FILE_ID = IntColumn.getID("file_id", TABLE).addIndex();
  public static final Column<Integer> ENTITY_ID = IntColumn.getID("entity_id", TABLE).addIndex();
  public static final Column<Metric> METRIC_TYPE = new EnumColumn<Metric>("metric_type", TABLE, Metric.values(), false) {
    @Override
    public Metric convertFromDB(String value) {
      return Metric.valueOf(value);
    }
  };
  public static final Column<Integer> VALUE = IntColumn.getInt("value", TABLE);
  
  // ---- CREATE ----
  @Override
  public void createTable() {
    executor.createTable(table, 
      PROJECT_ID,
      FILE_ID,
      ENTITY_ID,
      METRIC_TYPE,
      VALUE);
  }
  
  public void insert(Integer projectID, Integer fileID, Integer entityID, Metric metric, Integer value) {
    inserter.addValue(buildInsertValue(PROJECT_ID.convertToDB(projectID), FILE_ID.convertToDB(fileID), ENTITY_ID.convertToDB(entityID), METRIC_TYPE.convertToDB(metric), VALUE.convertToDB(value)));
  }
  
  //---- DELETE ----
  public void deleteByProjectID(Integer projectID) {
    executor.delete(table, PROJECT_ID.getEquals(projectID));
  }
}
