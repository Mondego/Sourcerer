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
import edu.uci.ics.sourcerer.model.Relation;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public final class LibraryRelationsTable {
  private LibraryRelationsTable() {}
  
  public static final String TABLE = "library_relations";
  /*  
   *  +---------------+-----------------+-------+--------+
   *  | Column name   | Type            | Null? | Index? |
   *  +---------------+-----------------+-------+--------+
   *  | relation_id   | SERIAL          | No    | Yes    |
   *  | relation_type | ENUM(values)    | No    | Yes    |
   *  | lhs_leid      | BIGINT UNSIGNED | No    | Yes    |
   *  | rhs_leid      | BIGINT UNSIGNED | No    | Yes    |
   *  | library_id    | BIGINT UNSIGNED | No    | Yes    |
   *  | lclass_fid    | BIGINT UNSIGNED | Yes   | Yes    |
   *  | offset        | INT UNSIGNED    | Yes   | No     |
   *  | length        | INT UNSIGNED    | Yes   | No     |
   *  +---------------+-----------------+-------+--------+
   */
  
  // ---- CREATE ----
  public static void createTable(QueryExecutor executor) {
    executor.createTable(TABLE,
        "relation_id SERIAL",
        "relation_type " + SchemaUtils.getEnumCreate(Relation.values()) + " NOT NULL",
        "lhs_leid BIGINT UNSIGNED NOT NULL",
        "rhs_leid BIGINT UNSIGNED NOT NULL",
        "library_id BIGINT UNSIGNED NOT NULL",
        "lclass_fid BIGINT UNSIGNED",
        "offset INT UNSIGNED",
        "length INT UNSIGNED",
        "INDEX(relation_type)",
        "INDEX(lhs_leid)",
        "INDEX(rhs_leid)",
        "INDEX(library_id)",
        "INDEX(lclass_fid)");
  }
  
  // ---- INSERT ----
  public static InsertBatcher getInsertBatcher(QueryExecutor executor) {
    return executor.getInsertBatcher(TABLE);
  }

  private static String getInsertValue(Relation type, String lhsEid, String rhsEid, String libraryID, String libraryClassFileID, String offset, String length) {
    return SchemaUtils.getSerialInsertValue(
        SchemaUtils.convertNotNullVarchar(type.name()),
        SchemaUtils.convertNotNullNumber(lhsEid),
        SchemaUtils.convertNotNullNumber(rhsEid), 
        SchemaUtils.convertNotNullNumber(libraryID),
        SchemaUtils.convertNumber(libraryClassFileID), 
        SchemaUtils.convertOffset(offset),
        SchemaUtils.convertLength(length));
  }
  
  public static void insert(InsertBatcher batcher, Relation type, String lhsEid, String rhsEid, String libraryID) {
    batcher.addValue(getInsertValue(type, lhsEid, rhsEid, libraryID, null, null, null));
  }
  
  public static void insert(InsertBatcher batcher, Relation type, String lhsEid, String rhsEid, String libraryID, String libraryClassFileID, String offset, String length) {
    batcher.addValue(getInsertValue(type, lhsEid, rhsEid, libraryID, libraryClassFileID, offset, length));
  }
}
