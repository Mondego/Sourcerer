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
import edu.uci.ics.sourcerer.model.Relation;
import edu.uci.ics.sourcerer.model.RelationClass;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public final class RelationsTable extends DatabaseTable {
  public static final String TABLE = "relations";
  
  public RelationsTable(QueryExecutor executor, TableLocker locker) {
    super(executor, locker, TABLE);
  }
  
  /*  
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
  
  public static final Column<Integer> RELATION_ID = IntColumn.getSerial("relation_id", TABLE);
  public static final Column<Relation> RELATION_TYPE = new EnumColumn<Relation>("relation_type", TABLE, Relation.values(), false) {
    @Override
    public Relation convertFromDB(String value) {
      return Relation.valueOf(value);
    }
  }.addIndex();
  public static final Column<RelationClass> RELATION_CLASS = new EnumColumn<RelationClass>("relation_class", TABLE, RelationClass.values(), false) {
    @Override
    public RelationClass convertFromDB(String value) {
      return RelationClass.valueOf(value);
    }
  };
  public static final Column<Integer> LHS_EID = IntColumn.getID("lhs_eid", TABLE).addIndex();
  public static final Column<Integer> RHS_EID = IntColumn.getID("rhs_eid", TABLE).addIndex();
  public static final Column<Integer> PROJECT_ID = IntColumn.getID("project_id", TABLE).addIndex();
  public static final Column<Integer> FILE_ID = IntColumn.getOptionalID("file_id", TABLE).addIndex();
  public static final Column<Integer> OFFSET = IntColumn.getUnsignedInt("offset", TABLE);
  public static final Column<Integer> LENGTH = IntColumn.getUnsignedInt("length", TABLE);
  
  // ---- CREATE ----
  public void createTable() {
    executor.createTable(table,
        RELATION_ID,
        RELATION_TYPE,
        RELATION_CLASS,
        LHS_EID,
        RHS_EID,
        PROJECT_ID,
        FILE_ID,
        OFFSET,
        LENGTH);
  }
  
  // ---- INSERT ----
  private String getInsertValue(Relation type, RelationClass klass, Integer lhsEid, Integer rhsEid, Integer projectID, Integer fileID, Integer offset, Integer length) {
    return buildSerialInsertValue(
        RELATION_TYPE.convertToDB(type),
        RELATION_CLASS.convertToDB(klass),
        LHS_EID.convertToDB(lhsEid),
        RHS_EID.convertToDB(rhsEid),
        PROJECT_ID.convertToDB(projectID),
        FILE_ID.convertToDB(fileID),
        OFFSET.convertToDB(offset),
        LENGTH.convertToDB(length));
  }
  
  public void insert(Relation type, RelationClass klass, Integer lhsEid, Integer rhsEid, Integer projectID) {
    inserter.addValue(getInsertValue(type, klass, lhsEid, rhsEid, projectID, null, null, null));
  }
  
  public void insert(Relation type, RelationClass klass, Integer lhsEid, Integer rhsEid, Integer projectID, Integer fileID, Integer offset, Integer length) {
    inserter.addValue(getInsertValue(type, klass, lhsEid, rhsEid, projectID, fileID, offset, length));
  }
  
  // ---- DELETE ----
  public void deleteByProjectID(Integer projectID) {
    executor.delete(table, PROJECT_ID.getEquals(projectID));
  }
}
