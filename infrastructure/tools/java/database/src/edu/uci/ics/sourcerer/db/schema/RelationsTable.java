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
import edu.uci.ics.sourcerer.model.Relation;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public final class RelationsTable extends DatabaseTable {
  protected RelationsTable(QueryExecutor executor, TableLocker locker) {
    super(executor, locker, "relations", true);
  }
  
  /*  
   *  +---------------+-----------------+-------+--------+
   *  | Column name   | Type            | Null? | Index? |
   *  +---------------+-----------------+-------+--------+
   *  | relation_id   | SERIAL          | No    | Yes    |
   *  | relation_type | ENUM(values)    | No    | Yes    |
   *  | lhs_eid       | BIGINT UNSIGNED | No    | Yes    |
   *  | rhs_eid       | BIGINT UNSIGNED | No    | Yes    |
   *  | internal      | BOOLEAN         | Yes   | Yes    |
   *  | project_id    | BIGINT UNSIGNED | No    | Yes    |
   *  | file_id       | BIGINT UNSIGNED | Yes   | Yes    |
   *  | offset        | INT UNSIGNED    | Yes   | No     |
   *  | length        | INT UNSIGNED    | Yes   | No     |
   *  +---------------+-----------------+-------+--------+
   */
  
  // ---- CREATE ----
  public void createTable() {
    executor.createTable(name,
        "relation_id SERIAL",
        "relation_type " + getEnumCreate(Relation.values()) + " NOT NULL",
        "lhs_eid BIGINT UNSIGNED NOT NULL",
        "rhs_eid BIGINT UNSIGNED NOT NULL",
        "internal BOOLEAN",
        "project_id BIGINT UNSIGNED NOT NULL",
        "file_id BIGINT UNSIGNED",
        "offset INT UNSIGNED",
        "length INT UNSIGNED",
        "INDEX(relation_type)",
        "INDEX(lhs_eid)",
        "INDEX(rhs_leid)",
        "INDEX(rhs_jeid)",
        "INDEX(rhs_eid)",
        "INDEX(project_id)",
        "INDEX(file_id)");
  }
  
  // ---- INSERT ----
  private String getInsertValue(Relation type, String lhsEid, String rhsEid, Boolean internal, String projectID, String fileID, String offset, String length) {
    return buildSerialInsertValue(
        convertNotNullVarchar(type.name()),
        convertNotNullNumber(lhsEid),
        convertNotNullNumber(rhsEid),
        convertBoolean(internal),
        convertNotNullNumber(projectID), 
        convertNumber(fileID),
        convertOffset(offset), 
        convertLength(length));
  }
  
  public void insert(Relation type, String lhsEid, String rhsEid, Boolean internal, String projectID) {
    batcher.insert(getInsertValue(type, lhsEid, rhsEid, internal, projectID, null, null, null));
  }
  
  public void insert(Relation type, String lhsEid, String rhsEid, Boolean internal, String projectID, String fileID, String offset, String length) {
    batcher.insert(getInsertValue(type, lhsEid, rhsEid, internal, projectID, fileID, offset, length));
  }
  
  // ---- DELETE ----
  public void deleteByProjectID(String projectID) {
    executor.delete(name, "project_id=" + projectID);
  }
  
  // ---- SELECT ----
}
