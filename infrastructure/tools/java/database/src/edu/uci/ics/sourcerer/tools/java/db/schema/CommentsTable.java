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

import edu.uci.ics.sourcerer.tools.java.model.types.Comment;
import edu.uci.ics.sourcerer.utils.db.Insert;
import edu.uci.ics.sourcerer.utils.db.sql.Column;
import edu.uci.ics.sourcerer.utils.db.sql.DatabaseTable;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public final class CommentsTable extends DatabaseTable {
  /*  
   *                     comments table 
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
  public static final CommentsTable TABLE = new CommentsTable();
  
  public static final Column<Integer> COMMENT_ID = TABLE.addSerialColumn("comment_id");
  public static final Column<Comment> COMMENT_TYPE = TABLE.addEnumColumn("comment_type", Comment.getValues(), false);
  public static final Column<Integer> CONTAINING_EID = TABLE.addIDColumn("containing_eid", true).addIndex();
  public static final Column<Integer> FOLLOWING_EID = TABLE.addIDColumn("following_eid", true).addIndex();
  public static final Column<Integer> PROJECT_ID = TABLE.addIDColumn("project_id", false).addIndex();
  public static final Column<Integer> FILE_ID = TABLE.addIDColumn("file_id", false).addIndex();
  public static final Column<Integer> OFFSET = TABLE.addIntColumn("offset", true, false);
  public static final Column<Integer> LENGTH = TABLE.addIntColumn("length", true, false);
  
  private CommentsTable() {
    super("comments");
  }

  // ---- INSERT ----
  private static Insert makeRowInsert(Comment type, Integer containing, Integer following, Integer projectID, Integer fileID, Integer offset, Integer length) {
    return TABLE.createInsert(
        COMMENT_TYPE.to(type),
        CONTAINING_EID.to(containing),
        FOLLOWING_EID.to(following),
        PROJECT_ID.to(projectID),
        FILE_ID.to(fileID),
        OFFSET.to(offset),
        LENGTH.to(length));
   }
  
  public static Insert makeJavadocInsert(Integer eid, Integer projectID, Integer fileID, Integer offset, Integer length) {
    return makeRowInsert(Comment.JAVADOC, null, eid, projectID, fileID, offset, length);
  }
  
  public static Insert makeCommentInsert(Comment type, Integer projectID, Integer fileID, Integer offset, Integer length) {
    return makeRowInsert(type, null, null, projectID, fileID, offset, length);
  }
  
  // ---- DELETE ----
//  public void deleteByProjectID(Integer projectID) {
//    executor.delete(table, PROJECT_ID.getEquals(projectID));
//  }
}
