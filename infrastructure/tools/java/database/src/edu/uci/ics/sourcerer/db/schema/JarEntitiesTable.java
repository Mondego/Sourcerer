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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;

import edu.uci.ics.sourcerer.db.util.InsertBatcher;
import edu.uci.ics.sourcerer.db.util.KeyInsertBatcher;
import edu.uci.ics.sourcerer.db.util.QueryExecutor;
import edu.uci.ics.sourcerer.db.util.ResultTranslator;
import edu.uci.ics.sourcerer.model.Entity;
import edu.uci.ics.sourcerer.model.LocalVariable;
import edu.uci.ics.sourcerer.model.db.TypedEntityID;
import edu.uci.ics.sourcerer.model.extracted.EntityEX;
import edu.uci.ics.sourcerer.model.extracted.LocalVariableEX;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public final class JarEntitiesTable {
  private JarEntitiesTable() {}
  
  public static final String TABLE = "jar_entities";
  /*  
   *  +-------------+-----------------+-------+--------+
   *  | Column name | Type            | Null? | Index? |
   *  +-------------+-----------------+-------+--------+
   *  | entity_id   | SERIAL          | No    | Yes    |
   *  | entity_type | ENUM(values)    | No    | Yes    |
   *  | fqn         | VARCHAR(2048)   | No    | Yes    |
   *  | modifiers   | INT UNSIGNED    | Yes   | No     |
   *  | multi       | INT UNSIGNED    | Yes   | No     |
   *  | jar_id      | BIGINT UNSIGNED | No    | Yes    |
   *  | jclass_fid  | BIGINT UNSIGNED | Yes   | Yes    |
   *  | offset      | INT UNSIGNED    | Yes   | No     |
   *  | length      | INT UNSIGNED    | Yes   | No     | 
   *  +-------------+-----------------+-------+--------+
   */
  
  //---- LOCK ----
  public static String getReadLock() {
    return SchemaUtils.getReadLock(TABLE);
  }
  
  public static String getWriteLock() {
    return SchemaUtils.getWriteLock(TABLE);
  }
  
  // ---- CREATE ----
  public static void createTable(QueryExecutor executor) {
    executor.createTable(TABLE,
        "entity_id SERIAL",
        "entity_type " + SchemaUtils.getEnumCreate(Entity.values()) + " NOT NULL",
        "fqn VARCHAR(2048) BINARY NOT NULL",
        "modifiers INT UNSIGNED",
        "multi INT UNSIGNED",
        "jar_id BIGINT UNSIGNED NOT NULL",
        "jclass_fid BIGINT UNSIGNED",
        "offset INT UNSIGNED",
        "length INT UNSIGNED",
        "INDEX(entity_type)",
        "INDEX(fqn(48))",
        "INDEX(jar_id)",
        "INDEX(jclass_fid)");
  }
  
  // ---- INSERT ----
  public static InsertBatcher getInsertBatcher(QueryExecutor executor) {
    return executor.getInsertBatcher(TABLE);
  }
  
  public static <T> KeyInsertBatcher<T> getKeyInsertBatcher(QueryExecutor executor, KeyInsertBatcher.KeyProcessor<T> processor) {
    return executor.getKeyInsertBatcher(TABLE, processor);
  }
  
  private static String getInsertValue(Entity type, String fqn, String mods, String multi, String jarID, String jarClassFileID, String offset, String length) {
    return SchemaUtils.getSerialInsertValue(
    		SchemaUtils.convertNotNullVarchar(type.name()),
				SchemaUtils.convertNotNullVarchar(fqn),
				SchemaUtils.convertNumber(mods),
				SchemaUtils.convertNumber(multi),
				SchemaUtils.convertNotNullNumber(jarID),
				SchemaUtils.convertNumber(jarClassFileID),
				SchemaUtils.convertOffset(offset), 
				SchemaUtils.convertLength(length));
  }
  
  public static void insert(InsertBatcher batcher, EntityEX entity, String jarID) {
    batcher.addValue(getInsertValue(entity.getType(), entity.getFqn(), entity.getMods(), null, jarID, null, null, null));
  }
  
  public static <T> void insert(KeyInsertBatcher<T> batcher, EntityEX entity, String jarID, T pairing) {
    batcher.addValue(getInsertValue(entity.getType(), entity.getFqn(), entity.getMods(), null, jarID, null, null, null), pairing);
  }
  
  public static void insert(InsertBatcher batcher, EntityEX entity, String jarID, String jarClassFileID) {
    batcher.addValue(getInsertValue(entity.getType(), entity.getFqn(), entity.getMods(), null, jarID, jarClassFileID, entity.getStartPosition(), entity.getLength()));
  }
  
  public static <T> void insert(KeyInsertBatcher<T> batcher, EntityEX entity, String jarID, String jarClassFileID, T pairing) {
    batcher.addValue(getInsertValue(entity.getType(), entity.getFqn(), entity.getMods(), null, jarID, jarClassFileID, entity.getStartPosition(), entity.getLength()), pairing);
  }
  
  public static String insertLocal(QueryExecutor executor, LocalVariableEX var, String jarID) {
    Entity type = null;
    if (var.getType() == LocalVariable.LOCAL) {
      type = Entity.LOCAL_VARIABLE;
    } else if (var.getType() == LocalVariable.PARAM) {
      type = Entity.PARAMETER;
    }
    return executor.insertSingleWithKey(TABLE, getInsertValue(type, var.getName(), var.getModifiers(), var.getPosition(), jarID, null, null, null));
  }
  
  public static String insertLocal(QueryExecutor executor, LocalVariableEX local, String jarID, String jarClassFileID) {
    Entity type = null;
    if (local.getType() == LocalVariable.LOCAL) {
      type = Entity.LOCAL_VARIABLE;
    } else if (local.getType() == LocalVariable.PARAM) {
      type = Entity.PARAMETER;
    }
    return executor.insertSingleWithKey(TABLE, getInsertValue(type, local.getName(), local.getModifiers(), local.getPosition(), jarID, jarClassFileID, local.getStartPos(), local.getLength()));
  }
  
  public static String insertArray(QueryExecutor executor, String fqn, int dimensions, String jarID) {
    return executor.insertSingleWithKey(TABLE, getInsertValue(Entity.ARRAY, fqn, null, Integer.toString(dimensions), jarID, null, null, null));
  }
  
  public static String insert(QueryExecutor executor, Entity type, String name, String jarID) {
    return executor.insertSingleWithKey(TABLE, getInsertValue(type, name, null, null, jarID, null, null, null));
  }
  
  // ---- DELETE ----
  public static void deleteByJarID(QueryExecutor executor, String jarID) {
    executor.delete(TABLE, "jar_id=" + jarID);
  }
  
  // ---- SELECT ----
  public static final ResultTranslator<TypedEntityID> TRANSLATOR_TEID = new ResultTranslator<TypedEntityID>() {
    @Override
    public TypedEntityID translate(ResultSet result) throws SQLException {
      return TypedEntityID.getJarEntityID(result.getString(1));
    }
  };
  
  public static Collection<TypedEntityID> getFilteredEntityIDsByFqn(QueryExecutor executor, String fqn, String inClause) {
    if (inClause == null) {
      return Collections.emptySet();
    } else {
      return executor.select(TABLE, "entity_id", "fqn='" + fqn + "' AND entity_type NOT IN ('UNKNOWN','LOCAL_VARIABLE','INITIALIZER','PARAMETER','ARRAY','DUPLICATE') AND jar_id IN " + inClause, TRANSLATOR_TEID);
    }
  }
  
  public static String getEntityIDByFqn(QueryExecutor executor, String fqn, String jarID) {
    return executor.selectSingle(TABLE, "entity_id", "fqn='" + fqn + "' AND jar_id=" + jarID);
  }
  
  public static Collection<String> getJarIDsByFqn(QueryExecutor executor, String fqn) {
    return executor.select(TABLE, "jar_id", "fqn='" + fqn + "'", ResultTranslator.SIMPLE_RESULT_TRANSLATOR);
  }
  
  public static Collection<String> getJarIDsByPackage(QueryExecutor executor, String fqn) {
    return executor.select(TABLE, "jar_id", "fqn LIKE '" + fqn + ".%' AND entity_type='PACKAGE'" , ResultTranslator.SIMPLE_RESULT_TRANSLATOR);
  }
  
  public static Collection<String> getJarIDsByFuzzyFqn(QueryExecutor executor, String fqn) {
    return executor.select(TABLE, "jar_id", "fqn LIKE '" + fqn.replace('.', '_') + "'", ResultTranslator.SIMPLE_RESULT_TRANSLATOR);
  }
//  public static final ResultTranslator<EntityDB> TRANSLATOR = new ResultTranslator<EntityDB>() {
//    @Override
//    public EntityDB translate(ResultSet result) throws SQLException {
//      return new EntityDB(TypedEntityID.getJarEntityID(result.getString(1)), Entity.valueOf(result.getString(2)), result.getString(3), result.getString(4));
//    }
//  };
//  
//  
//  public static String getJarByEid(QueryExecutor executor, String eid) {
//    return executor.executeSingle("SELECT jar_id FROM jar_entities WHERE entity_id=" + eid + ";");
//  }
//  
//  public static String getProjectsWithEntities(QueryExecutor executor) {
//    return executor.executeSingle("SELECT COUNT(DISTINCT jar_id) FROM jar_entities;");
//  }
//  
//  public static String getEntityIDByFqn(QueryExecutor executor, String fqn, String inClause) {
//    if (inClause == null) {
//      return null;
//    } else {
//      return executor.executeSingle("SELECT entity_id FROM jar_entities WHERE fqn = '" + fqn + "' AND entity_type <> 'UNKNOWN' AND jar_id IN " + inClause + ";");
//    }
//  }
//  
//  
//  public static String getEntityIDByFqnAndProject(QueryExecutor executor, String fqn, String projectID) {
//    return executor.executeSingle("SELECT entity_id FROM jar_entities INNER JOIN jar_uses ON jar_entities.jar_id=jar_uses.jar_id WHERE fqn='" + fqn +"' AND project_id=" + projectID + ";");
//  }
//  
//  public static Collection<TypedEntityID> getEntityIDsByFqnAndProject(QueryExecutor executor, String fqn, String projectID) {
//    return executor.execute("SELECT entity_id FROM jar_entities INNER JOIN jar_uses ON jar_entities.jar_id=jar_uses.jar_id WHERE fqn='" + fqn +"' AND project_id=" + projectID + ";", TRANSLATOR_TEID);
//  }
//  
//  public static EntityDB getEntityByID(QueryExecutor executor, String id) {
//    return executor.executeSingle("SELECT entity_id, entity_type, fqn, modifiers FROM jar_entities WHERE entity_id=" + id + ";", TRANSLATOR);
//  }
//  
//  public static String getEntityFqnByID(QueryExecutor executor, String id) {
//    return executor.executeSingle("SELECT fqn FROM jar_entities WHERE entity_id=" + id + ";");
//  }
//  
//  public static IterableResult<String> getJarEntityFQNs(QueryExecutor executor) {
//    return executor.executeStreamed("SELECT fqn FROM jar_entities;", ResultTranslator.SIMPLE_RESULT_TRANSLATOR);
//  }
}
