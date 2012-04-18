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

import edu.uci.ics.sourcerer.tools.java.model.types.Location;
import edu.uci.ics.sourcerer.tools.java.model.types.Relation;
import edu.uci.ics.sourcerer.tools.java.model.types.RelationClass;
import edu.uci.ics.sourcerer.utils.db.Insert;
import edu.uci.ics.sourcerer.utils.db.sql.Column;
import edu.uci.ics.sourcerer.utils.db.sql.DatabaseTable;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public final class RelationsTable extends DatabaseTable {
  /*  
   *                     relations table
   *  +----------------+-----------------+-------+--------+
   *  | Column name    | Type            | Null? | Index? |
   *  +----------------+-----------------+-------+--------+
   *  | relation_id    | SERIAL          | No    | Yes    |
   *  | relation_type  | ENUM(values)    | No    | Yes    |
   *  | relation_class | ENUM(values)    | No    | No     |
   *  | lhs_eid        | BIGINT UNSIGNED | No    | Yes    |
   *  | rhs_eid        | BIGINT UNSIGNED | No    | Yes    |
   *  | project_id     | BIGINT UNSIGNED | No    | Yes    |
   *  | file_id        | BIGINT UNSIGNED | Yes   | Yes    |
   *  | offset         | INT UNSIGNED    | Yes   | No     |
   *  | length         | INT UNSIGNED    | Yes   | No     |
   *  +----------------+-----------------+-------+--------+
   */
  public static final RelationsTable TABLE = new RelationsTable();
  
  public static final Column<Integer> RELATION_ID = TABLE.addSerialColumn("relation_id");
  public static final Column<Relation> RELATION_TYPE = TABLE.addEnumColumn("relation_type", Relation.values(), false).addIndex();
  public static final Column<RelationClass> RELATION_CLASS = TABLE.addEnumColumn("relation_class", RelationClass.values(), false);
  public static final Column<Integer> LHS_EID = TABLE.addIDColumn("lhs_eid", false).addIndex();
  public static final Column<Integer> RHS_EID = TABLE.addIDColumn("rhs_eid", false).addIndex();
  public static final Column<Integer> PROJECT_ID = TABLE.addIDColumn("project_id", false).addIndex();
  public static final Column<Integer> FILE_ID = TABLE.addIDColumn("file_id", true).addIndex();
  public static final Column<Integer> OFFSET = TABLE.addIntColumn("offset", true, true);
  public static final Column<Integer> LENGTH = TABLE.addIntColumn("length", true, true);
 
  private RelationsTable() {
    super("relations");
  }
  
  // ---- INSERT ----
  private static Insert makeInsert(Relation type, RelationClass klass, Integer lhsEid, Integer rhsEid, Integer projectID, Integer fileID, Integer offset, Integer length) {
    return TABLE.createInsert(
        RELATION_TYPE.to(type),
        RELATION_CLASS.to(klass),
        LHS_EID.to(lhsEid),
        RHS_EID.to(rhsEid),
        PROJECT_ID.to(projectID),
        FILE_ID.to(fileID),
        OFFSET.to(offset),
        LENGTH.to(length));
  }
  
  public static Insert makeInsert(Relation type, RelationClass klass, Integer lhsEid, Integer rhsEid, Integer projectID) {
    return makeInsert(type, klass, lhsEid, rhsEid, projectID, null, null, null);
  }
  
  public static Insert makeInsert(Relation type, RelationClass klass, Integer lhsEid, Integer rhsEid, Integer projectID, Integer fileID, Location location) {
    return makeInsert(type, klass, lhsEid, rhsEid, projectID, fileID, location.getOffset(), location.getLength());
  }
  
  public static Insert makeInsert(Relation type, RelationClass klass, Integer lhsEid, Integer rhsEid, Integer projectID, Integer fileID) {
    return makeInsert(type, klass, lhsEid, rhsEid, projectID, fileID, null, null);
  }
//  
//  // ---- DELETE ----
//  public void deleteByProjectID(Integer projectID) {
//    executor.delete(table, PROJECT_ID.getEquals(projectID));
//  }
}
