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
import edu.uci.ics.sourcerer.model.db.TypedEntityID;
import edu.uci.ics.sourcerer.model.db.TypedEntityID.Type;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public final class RelationsTable {
  private RelationsTable() {}
  
  public static final String TABLE = "relations";
  /*  
   *  +---------------+-----------------+-------+--------+
   *  | Column name   | Type            | Null? | Index? |
   *  +---------------+-----------------+-------+--------+
   *  | relation_id   | SERIAL          | No    | Yes    |
   *  | relation_type | ENUM(values)    | No    | Yes    |
   *  | lhs_eid       | BIGINT UNSIGNED | No    | Yes    |
   *  | rhs_leid      | BIGINT UNSIGNED | Yes   | Yes    |
   *  | rhs_jeid      | BIGINT UNSIGNED | Yes   | Yes    |
   *  | rhs_eid       | BIGINT UNSIGNED | Yes   | Yes    |
   *  | project_id    | BIGINT UNSIGNED | No    | Yes    |
   *  | file_id       | BIGINT UNSIGNED | Yes   | Yes    |
   *  | offset        | INT UNSIGNED    | Yes   | No     |
   *  | length        | INT UNSIGNED    | Yes   | No     |
   *  +---------------+-----------------+-------+--------+
   */
  
  // ---- CREATE ----
  public static void createTable(QueryExecutor executor) {
    executor.createTable(TABLE,
        "relation_id SERIAL",
        "relation_type " + SchemaUtils.getEnumCreate(Relation.values()) + " NOT NULL",
        "lhs_eid BIGINT UNSIGNED NOT NULL",
        "rhs_leid BIGINT UNSIGNED",
        "rhs_jeid BIGINT UNSIGNED",
        "rhs_eid BIGINT UNSIGNED",
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
  public static InsertBatcher getInsertBatcher(QueryExecutor executor) {
    return executor.getInsertBatcher(TABLE);
  }
  
  private static String getInsertValue(Relation type, String lhsEid, TypedEntityID rhsEid, String projectID, String fileID, String offset, String length) {
    return "(NULL," +
        SchemaUtils.convertNotNullVarchar(type.name()) + "," +
        SchemaUtils.convertNotNullNumber(lhsEid) + "," +
        (rhsEid.getType() == Type.LIBRARY ? (rhsEid.getID() + ",NULL,NULL,") : (rhsEid.getType() == Type.JAR ? ("NULL," + rhsEid.getID() + ",NULL,") : ("NULL,NULL," + rhsEid.getID() + ","))) +
        SchemaUtils.convertNotNullNumber(projectID) + "," + 
        SchemaUtils.convertOffset(offset) + "," + 
        SchemaUtils.convertLength(length) + ")";
  }
  
  public static void insert(InsertBatcher batcher, Relation type, String lhsEid, TypedEntityID rhsEid, String projectID, String fileID, String offset, String length) {
    batcher.addValue(getInsertValue(type, lhsEid, rhsEid, projectID, fileID, offset, length));
  }
  
  public static void insert(InsertBatcher batcher, Relation type, String lhsEid, TypedEntityID rhsEid, String projectID) {
    batcher.addValue(getInsertValue(type, lhsEid, rhsEid, projectID, null, null, null));
  }
    
  // ---- DELETE ----
  public static void deleteByProjectID(QueryExecutor executor, String projectID) {
    executor.delete(TABLE, "project_id=" + projectID);
  }
  
  // ---- SELECT ----
//  public static final ResultTranslator<TypedEntityID> TRANSLATOR_TARGET = new ResultTranslator<TypedEntityID>() {
//    @Override
//    public TypedEntityID translate(ResultSet result) throws SQLException {
//      if (result.getString(1) != null) {
//        return TypedEntityID.getSourceEntityID(result.getString(1));
//      } else if (result.getString(2) != null) {
//        return TypedEntityID.getJarEntityID(result.getString(2));
//      } else if (result.getString(3) != null) {
//        return TypedEntityID.getLibraryEntityID(result.getString(3));
//      } else {
//        return null;
//      }
//    }
//  };
//  
//  public static final ResultTranslator<TypedEntityID> TRANSLATOR_SOURCE = new ResultTranslator<TypedEntityID>() {
//    @Override
//    public TypedEntityID translate(ResultSet result) throws SQLException {
//      return TypedEntityID.getSourceEntityID(result.getString(1));
//    }
//  };
//  
//  public static String getRhsEidByLhsEidSingle(QueryExecutor executor, Relation relationType, String lhsEid) {
//    return executor.executeSingle("SELECT rhs_eid FROM relations WHERE relation_type='" + relationType.name() + "' AND lhs_eid=" + lhsEid + ";");
//  }
//  
//  public static EntityDB getTargetEntityBySource(QueryExecutor executor, Relation relationType, String lhsEid) {
//    return executor.executeSingle("SELECT entity_id, entity_type, fqn, modifiers FROM relations INNER JOIN entities ON rhs_eid=entity_id WHERE relation_type='" + relationType.name() + "' AND lhs_eid=" + lhsEid + ";", EntitiesTable.TRANSLATOR);
//  }
//  
//  public static TypedEntityID getTargetBySource(QueryExecutor executor, Relation relationType, String lhsEid) {
//    return executor.executeSingle("SELECT rhs_eid, rhs_jeid, rhs_leid FROM relations WHERE relation_type='" + relationType.name() + "' AND lhs_eid=" + lhsEid + ";", TRANSLATOR_TARGET);
//  }
//  
//  public static Collection<TypedEntityID> getTargetsBySource(QueryExecutor executor, Relation relationType, String lhsEid) {
//    return executor.execute("SELECT rhs_eid, rhs_jeid, rhs_leid FROM relations WHERE relation_type='" + relationType.name() + "' AND lhs_eid=" + lhsEid + ";", TRANSLATOR_TARGET);
//  }
//  
//  public static Collection<EntityDB> getTargetEntitiesBySource(QueryExecutor executor, Relation relationType, String lhsEid) {
//    return executor.execute("SELECT entity_id, entity_type, fqn, modifiers FROM relations INNER JOIN entities ON rhs_eid=entity_id WHERE relation_type='" + relationType.name() + "' AND lhs_eid=" + lhsEid + ";", EntitiesTable.TRANSLATOR);
//  }
//  
//  public static Collection<EntityDB> getSourceEntitiesByTarget(QueryExecutor executor, Relation relationType, String rhsEid) {
//    return executor.execute("SELECT entity_id, entity_type, fqn, modifiers FROM relations INNER JOIN entities ON lhs_eid=entity_id WHERE relation_type='" + relationType.name() + "' AND rhs_eid=" + rhsEid + ";", EntitiesTable.TRANSLATOR);
//  }
//  
//  public static Collection<TypedEntityID> getSourcesByTargetAndType(QueryExecutor executor, Relation relationType, String rhsEid, Entity lhsType) {
//    return executor.execute("SELECT entity_id FROM relations INNER JOIN entities ON lhs_eid=entity_id WHERE relation_type='" + relationType.name() + "' AND entity_type='" + lhsType.name() + "' AND rhs_eid=" + rhsEid + ";", TRANSLATOR_SOURCE);
//  }
//  
//  public static Collection<TypedEntityID> getLhsEidByRhsEid(QueryExecutor executor, Relation relationType, String rhsEid) {
//    return executor.execute("SELECT lhs_eid FROM relations WHERE relation_type='" + relationType.name() + "' AND rhs_eid=" + rhsEid + ";", TRANSLATOR_SOURCE);
//  }
}
