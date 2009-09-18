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

import edu.uci.ics.sourcerer.db.util.InsertBatcher;
import edu.uci.ics.sourcerer.db.util.QueryExecutor;
import edu.uci.ics.sourcerer.model.Comment;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public final class CommentsTable {
  private CommentsTable() {}
  
  public static final String TABLE = "comments";
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
  public static void createTable(QueryExecutor executor) {
    executor.createTable(TABLE, 
        "comment_id SERIAL",
        "comment_type " + SchemaUtils.getEnumCreate(Comment.getValues()),
        "containing_eid BIGINT UNSIGNED",
        "following_eid BIGINT UNSIGNED",
        "project_id BIGINT UNSIGNED NOT NULL",
        "file_id BIGINT UNSIGNED NOT NULL",
        "offset INT UNSIGNED NOT NULL",
        "length INT UNSIGNED NOT NULL",
        "INDEX(containing_eid",
        "INDEX(following_eid",
        "INDEX(project_id)",
        "INDEX(file_id)");
  }

  // ---- INSERT ----
  public static InsertBatcher getInsertBatcher(QueryExecutor executor) {
    return executor.getInsertBatcher(TABLE);
  }

  private static String getInsertValue(Comment type, String containing, String following, String projectID, String fileID, String offset, String length) {
    return SchemaUtils.getSerialInsertValue(
        SchemaUtils.convertNotNullVarchar(type.name()),
        SchemaUtils.convertNumber(containing),
        SchemaUtils.convertNumber(following),
        SchemaUtils.convertNotNullNumber(projectID),
        SchemaUtils.convertNotNullNumber(fileID),
        SchemaUtils.convertNotNullNumber(offset),
        SchemaUtils.convertNotNullNumber(length));
   }
  
  public static void insertJavadoc(InsertBatcher batcher, String eid, String projectID, String fileID, String offset, String length) {
    batcher.addValue(getInsertValue(Comment.JAVADOC, null, eid, projectID, fileID, offset, length));
  }
  
  public static void insertUnassociatedJavadoc(InsertBatcher batcher, String projectID, String fileID, String offset, String length) {
    batcher.addValue(getInsertValue(Comment.JAVADOC, null, null, projectID, fileID, offset, length));
  }
  
  public static void insertComment(InsertBatcher batcher, Comment type, String projectID, String fileID, String offset, String length) {
    batcher.addValue(getInsertValue(type, null, null, projectID, fileID, offset, length));
  }
  
  // ---- DELETE ----
  public static void deleteByProjectID(QueryExecutor executor, String projectID) {
    executor.delete(TABLE, "project_id=" + projectID);
  }
}
