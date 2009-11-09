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

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public final class JarRelationsTable{
  private JarRelationsTable() {}
  
  public static final String TABLE = "jar_relations";
  /*  
   *  +---------------+-----------------+-------+--------+
   *  | Column name   | Type            | Null? | Index? |
   *  +---------------+-----------------+-------+--------+
   *  | relation_id   | SERIAL          | No    | Yes    |
   *  | relation_type | ENUM(values)    | No    | Yes    |
   *  | lhs_jeid      | BIGINT UNSIGNED | No    | Yes    |
   *  | rhs_leid      | BIGINT UNSIGNED | Yes   | Yes    |
   *  | rhs_jeid      | BIGINT UNSIGNED | Yes   | Yes    |
   *  | jclass_fid    | BIGINT UNSIGNED | No    | Yes    |
   *  | jfile_id      | BIGINT UNSIGNED | Yes   | Yes    |
   *  | offset        | INT UNSIGNED    | Yes   | No     |
   *  | length        | INT UNSIGNED    | Yes   | No     |
   *  +---------------+-----------------+-------+--------+
   */
  
  // ---- CREATE ----
  public static void createTable(QueryExecutor executor) {
    executor.createTable(TABLE,
        "relation_id SERIAL",
        "relation_type " + SchemaUtils.getEnumCreate(Relation.values()) + " NOT NULL",
        "lhs_jeid BIGINT UNSIGNED NOT NULL",
        "rhs_leid BIGINT UNSIGNED",
        "rhs_jeid BIGINT UNSIGNED",
        "jar_id BIGINT UNSIGNED NOT NULL",
        "jclass_fid BIGINT UNSIGNED",
        "offset INT UNSIGNED",
        "length INT UNSIGNED",
        "INDEX(relation_type)",
        "INDEX(lhs_jeid)",
        "INDEX(rhs_leid)",
        "INDEX(rhs_jeid)",
        "INDEX(jar_id)",
        "INDEX(jclass_fid)");
  }
  
  // ---- INSERT ----
  public static InsertBatcher getInsertBatcher(QueryExecutor executor) {
    return executor.getInsertBatcher(TABLE);
  }
  
  private static String getInsertValue(Relation type, String lhsEid, TypedEntityID rhsEid, String jarID, String jarClassFileID, String offset, String length) {
    return SchemaUtils.getSerialInsertValue(
        SchemaUtils.convertNotNullVarchar(type.name()),
        SchemaUtils.convertNotNullNumber(lhsEid),
        SchemaUtils.convertJarTypedEntityID(rhsEid),
        SchemaUtils.convertNotNullNumber(jarID),
        SchemaUtils.convertNumber(jarClassFileID),
        SchemaUtils.convertOffset(offset),
        SchemaUtils.convertLength(length));
  }
  
  
  public static void insert(InsertBatcher batcher, Relation type, String lhsEid, TypedEntityID rhsEid, String jarID) {
    batcher.addValue(getInsertValue(type, lhsEid, rhsEid, jarID, null, null, null));
  }
  
  public static void insert(InsertBatcher batcher, Relation type, String lhsEid, TypedEntityID rhsEid, String jarID, String jarClassFileID, String offset, String length) {
    batcher.addValue(getInsertValue(type, lhsEid, rhsEid, jarID, jarClassFileID, offset, length));
  }
  
  // ---- DELETE ----
  public static void deleteByJarID(QueryExecutor executor, String jarID) {
    executor.delete(TABLE, "jar_id=" + jarID);
  }
  
  // ---- SELECT ----
//  public static final ResultTranslator<TypedEntityID> TRANSLATOR_EID = new ResultTranslator<TypedEntityID>() {
//    @Override
//    public TypedEntityID translate(ResultSet result) throws SQLException {
//      if (result.getString(1) != null) {
//        return TypedEntityID.getJarEntityID(result.getString(1));
//      } else if (result.getString(2) != null){
//        return TypedEntityID.getLibraryEntityID(result.getString(2));
//      } else {
//        return null;
//      }
//    }
//  };
//  
//  public static EntityDB getTargetEntityBySource(QueryExecutor executor, Relation relationType, String lhsEid) {
//    return executor.executeSingle("SELECT entity_id, entity_type, fqn, modifiers FROM jar_relations INNER JOIN jar_entities ON rhs_jeid=entity_id WHERE relation_type='" + relationType.name() + "' AND lhs_jeid=" + lhsEid + ";", JarEntitiesTable.TRANSLATOR);
//  }
//  
//  public static Collection<EntityDB> getTargetEntitiesBySource(QueryExecutor executor, Relation relationType, String lhsEid) {
//    return executor.execute("SELECT entity_id, entity_type, fqn, modifiers FROM jar_relations INNER JOIN jar_entities ON rhs_jeid=entity_id WHERE relation_type='" + relationType.name() + "' AND lhs_jeid=" + lhsEid + ";", JarEntitiesTable.TRANSLATOR);
//  }
//  
//  public static Collection<EntityDB> getSourceEntitiesByTarget(QueryExecutor executor, Relation relationType, String rhsEid) {
//    return executor.execute("SELECT entity_id, entity_type, fqn, modifiers FROM jar_relations INNER JOIN jar_entities ON lhs_jeid=entity_id WHERE relation_type='" + relationType.name() + "' AND rhs_jeid=" + rhsEid + ";", JarEntitiesTable.TRANSLATOR);
//  }
//  
//  public static TypedEntityID getTargetBySource(QueryExecutor executor, Relation relationType, String lhsEid) {
//    return executor.executeSingle("SELECT rhs_jeid, rhs_leid FROM jar_relations WHERE relation_type='" + relationType.name() + "' AND lhs_jeid=" + lhsEid + ";", TRANSLATOR_EID);
//  }
//  
//  public static Collection<TypedEntityID> getTargetsBySource(QueryExecutor executor, Relation relationType, String lhsEid) {
//    return executor.execute("SELECT rhs_jeid, rhs_leid FROM jar_relations WHERE relation_type='" + relationType.name() + "' AND lhs_jeid=" + lhsEid + ";", TRANSLATOR_EID);
//  }
}
