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
import edu.uci.ics.sourcerer.model.Comment;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public final class CommentsTable extends DatabaseTable {
  public static final String TABLE = "comments";
  
  public CommentsTable(QueryExecutor executor, TableLocker locker) {
    super(executor, locker, TABLE);
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
 
  public static final Column<Integer> COMMENT_ID = IntColumn.getSerial("comment_id", TABLE);
  public static final Column<Comment> COMMENT_TYPE = new EnumColumn<Comment>("comment_type", TABLE, Comment.getValues(), false) {
    @Override
    public Comment convertFromDB(String value) {
      return Comment.valueOf(value);
    }
  };
  public static final Column<Integer> CONTAINING_EID = IntColumn.getOptionalID("containing_eid", TABLE).addIndex();
  public static final Column<Integer> FOLLOWING_EID = IntColumn.getOptionalID("following_eid", TABLE).addIndex();
  public static final Column<Integer> PROJECT_ID = IntColumn.getID("project_id", TABLE).addIndex();
  public static final Column<Integer> FILE_ID = IntColumn.getID("file_id", TABLE).addIndex();
  public static final Column<Integer> OFFSET = IntColumn.getUnsignedIntNotNull("offset", TABLE);
  public static final Column<Integer> LENGTH = IntColumn.getUnsignedIntNotNull("length", TABLE);
  
  // ---- CREATE ----
  public void createTable() {
    executor.createTable(table, 
        COMMENT_ID,
        COMMENT_TYPE,
        CONTAINING_EID,
        FOLLOWING_EID,
        PROJECT_ID,
        FILE_ID,
        OFFSET,
        LENGTH);
  }

  // ---- INSERT ----
  private String getInsertValue(Comment type, Integer containing, Integer following, Integer projectID, Integer fileID, Integer offset, Integer length) {
    return buildSerialInsertValue(
        COMMENT_TYPE.convertToDB(type),
        CONTAINING_EID.convertToDB(containing),
        FOLLOWING_EID.convertToDB(following),
        PROJECT_ID.convertToDB(projectID),
        FILE_ID.convertToDB(fileID),
        OFFSET.convertToDB(offset),
        LENGTH.convertToDB(length));
   }
  
  public void insertJavadoc(Integer eid, Integer projectID, Integer fileID, Integer offset, Integer length) {
    inserter.addValue(getInsertValue(Comment.JAVADOC, null, eid, projectID, fileID, offset, length));
  }
  
  public void insertUnassociatedJavadoc(Integer projectID, Integer fileID, Integer offset, Integer length) {
    inserter.addValue(getInsertValue(Comment.JAVADOC, null, null, projectID, fileID, offset, length));
  }
  
  public void insertComment(Comment type, Integer projectID, Integer fileID, Integer offset, Integer length) {
    inserter.addValue(getInsertValue(type, null, null, projectID, fileID, offset, length));
  }
  
  // ---- DELETE ----
  public void deleteByProjectID(Integer projectID) {
    executor.delete(table, PROJECT_ID.getEquals(projectID));
  }
}
