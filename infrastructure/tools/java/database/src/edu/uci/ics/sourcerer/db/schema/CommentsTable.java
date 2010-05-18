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
import edu.uci.ics.sourcerer.model.Comment;
import edu.uci.ics.sourcerer.model.db.LocationDB;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public final class CommentsTable extends DatabaseTable {
  protected CommentsTable(QueryExecutor executor, TableLocker locker) {
    super(executor, locker, "comments");
  }

  /*  
   *  +----------------+-----------------+-------+--------+
   *  | Column name    | Type            | Null? | Index? |
   *  +----------------+-----------------+-------+--------+
   *  | comment_id     | SERIAL          | No    | Yes    |
   *  | comment_type   | ENUM(values)    | No    | No     |
   *  | containing_eid | BIGINT UNSIGNED | Yes   | Yes    |
   *  | following_eid  | BIGINT UNSIGNED | Yes   | Yes    |
   *  | project_id     | BIGINT UNSIGNED | No    | Yes    |
   *  | file_id        | BIGINT UNSIGNED | No    | Yes    |
   *  | offset         | INT UNSIGNED    | No    | No     |
   *  | length         | INT UNSIGNED    | No    | No     |
   *  +----------------+-----------------+-------+--------+
   */
 
  // ---- CREATE ----
  public void createTable() {
    executor.createTable(name, 
        "comment_id SERIAL",
        "comment_type " + getEnumCreate(Comment.getValues()),
        "containing_eid BIGINT UNSIGNED",
        "following_eid BIGINT UNSIGNED",
        "project_id BIGINT UNSIGNED NOT NULL",
        "file_id BIGINT UNSIGNED NOT NULL",
        "offset INT UNSIGNED NOT NULL",
        "length INT UNSIGNED NOT NULL",
        "INDEX(containing_eid)",
        "INDEX(following_eid)",
        "INDEX(project_id)",
        "INDEX(file_id)");
  }

  // ---- INSERT ----
  private String getInsertValue(Comment type, String containing, String following, String projectID, String fileID, String offset, String length) {
    return buildSerialInsertValue(
        convertNotNullVarchar(type.name()),
        convertNumber(containing),
        convertNumber(following),
        convertNotNullNumber(projectID),
        convertNotNullNumber(fileID),
        convertNotNullNumber(offset),
        convertNotNullNumber(length));
   }
  
  public void insertJavadoc(String eid, String projectID, String fileID, String offset, String length) {
    inserter.addValue(getInsertValue(Comment.JAVADOC, null, eid, projectID, fileID, offset, length));
  }
  
  public void insertUnassociatedJavadoc(String projectID, String fileID, String offset, String length) {
    inserter.addValue(getInsertValue(Comment.JAVADOC, null, null, projectID, fileID, offset, length));
  }
  
  public void insertComment(Comment type, String projectID, String fileID, String offset, String length) {
    inserter.addValue(getInsertValue(type, null, null, projectID, fileID, offset, length));
  }
  
  // ---- DELETE ----
  public void deleteByProjectID(String projectID) {
    executor.delete(name, "project_id=" + projectID);
  }
  
  // ---- SELECT ----
  public LocationDB getLocationByCommentID(String commentID) {
    return executor.selectSingle(name, EntitiesTable.LOCATION_RESULT_TRANSLATOR.getSelect(), "comment_id=" + commentID, EntitiesTable.LOCATION_RESULT_TRANSLATOR); 
  }
}
