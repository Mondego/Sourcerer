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

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public final class ImportsTable extends DatabaseTable {
  protected ImportsTable(QueryExecutor executor, TableLocker locker) {
    super(executor, locker, "imports", true);
  }
  /*  
   *  +-------------+-------------------+-------+--------+
   *  | Column name | Type              | Null? | Index? |
   *  +-------------+-------------------+-------+--------+
   *  | static      | BOOLEAN           | No    | No     |
   *  | on_demand   | BOOLEAN           | No    | No     |
   *  | eid         | BIGINT UNSIGNED   | No    | Yes    |
   *  | project_id  | BIGINT UNSIGNED   | No    | Yes    |
   *  | file_id     | BIGINT UNSIGNED   | No    | Yes    |
   *  | offset      | INT UNSIGNED      | No    | No     |
   *  | length      | INT UNSIGNED      | No    | No     |
   *  +-------------+-------------------+-------+--------+
   */
  
  // ---- CREATE ----
  public void createTable() {
    executor.createTable(name, 
        "static BOOLEAN NOT NULL",
        "on_demand BOOLEAN NOT NULL",
        "eid BIGINT UNSIGNED NOT NULL",
        "project_id BIGINT UNSIGNED NOT NULL",
        "file_id BIGINT UNSIGNED NOT NULL",
        "offset INT UNSIGNED NOT NULL",
        "length INT UNSIGNED NOT NULL",
        "INDEX(eid)",
        "INDEX(project_id)",
        "INDEX(file_id)");
  }
  
  // ---- INSERT ----
  private String getInsertValue(boolean isStatic, boolean onDemand, String eid, String projectID, String fileID, String offset, String length) {
    return buildInsertValue(
        convertNotNullBoolean(isStatic),
        convertNotNullBoolean(onDemand),
        convertNotNullNumber(eid),
        convertNotNullNumber(projectID),
        convertNotNullNumber(fileID),
        convertOffset(offset),
        convertLength(length));
  }
  
  public void insert(boolean isStatic, boolean onDemand, String eid, String projectID, String fileID, String offset, String length) {
    batcher.addValue(getInsertValue(isStatic, onDemand, eid, projectID, fileID, offset, length));
  }
  
  // ---- DELETE ----
  public void deleteByProjectID(String projectID) {
    executor.delete(name, "project_id=" + projectID);
  }
}
