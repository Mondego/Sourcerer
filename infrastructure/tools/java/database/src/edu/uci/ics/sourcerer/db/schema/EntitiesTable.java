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

import edu.uci.ics.sourcerer.db.util.KeyInsertBatcher;
import edu.uci.ics.sourcerer.db.util.QueryExecutor;
import edu.uci.ics.sourcerer.model.Entity;
import edu.uci.ics.sourcerer.model.LocalVariable;
import edu.uci.ics.sourcerer.model.extracted.EntityEX;
import edu.uci.ics.sourcerer.model.extracted.LocalVariableEX;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public final class EntitiesTable {
  private EntitiesTable() {}
  
  public static final String TABLE = "entities";
  /*  
   *  +-------------+-----------------+-------+--------+
   *  | Column name | Type            | Null? | Index? |
   *  +-------------+-----------------+-------+--------+
   *  | entity_id   | SERIAL          | No    | Yes    |
   *  | entity_type | ENUM(values)    | No    | Yes    |
   *  | fqn         | VARCHAR(2048)   | No    | Yes    |
   *  | modifiers   | INT UNSIGNED    | Yes   | No     |
   *  | multi       | INT UNSIGNED    | Yes   | No     |
   *  | project_id  | BIGINT UNSIGNED | No    | Yes    |
   *  | file_id     | BIGINT UNSIGNED | Yes   | Yes    |
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
        "project_id BIGINT UNSIGNED NOT NULL",
        "file_id BIGINT UNSIGNED",
        "offset INT UNSIGNED",
        "length INT UNSIGNED",
        "INDEX(entity_type)",
        "INDEX(fqn(48))",
        "INDEX(project_id)",
        "INDEX(file_id)");
  }
  
  // ---- INSERT ----
  public static <T> KeyInsertBatcher<T> getKeyInsertBatcher(QueryExecutor executor, KeyInsertBatcher.KeyProcessor<T> processor) {
    return executor.getKeyInsertBatcher(TABLE, processor);
  }
  
  private static String getInsertValue(Entity type, String fqn, String modifiers, String multi, String projectID, String fileID, String offset, String length) {
    return SchemaUtils.getSerialInsertValue(
        SchemaUtils.convertNotNullVarchar(type.name()),
        SchemaUtils.convertNotNullVarchar(fqn),
        SchemaUtils.convertNumber(modifiers),
        SchemaUtils.convertNumber(multi),
        SchemaUtils.convertNotNullNumber(projectID),
        SchemaUtils.convertNumber(fileID),
        SchemaUtils.convertOffset(offset),
        SchemaUtils.convertLength(length));
  }
  
  public static <T> void insert(KeyInsertBatcher<T> batcher, EntityEX entity, String projectID, String fileID, T pairing) {
    batcher.addValue(getInsertValue(entity.getType(), entity.getFqn(), entity.getMods(), null, projectID, fileID, entity.getStartPosition(), entity.getLength()), pairing);
  }
  
  public static String insert(QueryExecutor executor, Entity type, String fqn, String projectID) {
    return executor.insertSingleWithKey(TABLE, getInsertValue(type, fqn, null, null, projectID, null, null, null));
  }
  
  public static String insertArray(QueryExecutor executor, String fqn, int size, String projectID) {
    return executor.insertSingleWithKey(TABLE, getInsertValue(Entity.ARRAY, fqn, null, "" + size, projectID, null, null, null));
  }
  
  public static String insertLocalVariable(QueryExecutor executor, LocalVariableEX var, String projectID, String fileID) {
    Entity type = null;
    if (var.getType() == LocalVariable.LOCAL) {
      type = Entity.LOCAL_VARIABLE;
    } else if (var.getType() == LocalVariable.PARAM) {
      type = Entity.PARAMETER;
    }
    return executor.insertSingleWithKey(TABLE, getInsertValue(type, var.getName(), var.getModifiers(), var.getPosition(), projectID, fileID, var.getStartPos(), var.getLength()));
  }
  
  // ---- DELETE ----
  public static void deleteByProjectID(QueryExecutor executor, String projectID) {
    executor.delete(TABLE, "project_id=" + projectID);
  }
  
  // ---- SELECT ----
  public static String getFileIDByEid(QueryExecutor executor, String eid) {
    return executor.selectSingle(TABLE, "file_id", "entity_id="+eid);
  }
//  public static final ResultTranslator<EntityDB> TRANSLATOR = new ResultTranslator<EntityDB>() {
//    @Override
//    public EntityDB translate(ResultSet result) throws SQLException {
//      return new EntityDB(TypedEntityID.getSourceEntityID(result.getString(1)), Entity.valueOf(result.getString(2)), result.getString(3), result.getString(4));
//    }
//  };
//  
//  public static final ResultTranslator<Entity> TRANSLATOR_TYPE = new ResultTranslator<Entity>() {
//    @Override
//    public Entity translate(ResultSet result) throws SQLException {
//      return Entity.valueOf(result.getString(1));
//    }
//  };
//  
//  public static Pair<String, String> getLocationByEid(QueryExecutor executor, String eid) {
//    return executor.executeSingle("SELECT offset, length FROM entities WHERE entity_id=" + eid + ";", ResultTranslator.PAIR_RESULT_TRANSLATOR);
//  }
//  
//  public static int getMaxEntityID(QueryExecutor executor) {
//    return executor.executeSingleInt("SELECT MAX(entity_id) FROM entities;");
//  }
//  
//  public static Entity getEntityTypeByEid(QueryExecutor executor, String eid) {
//    return executor.executeSingle("SELECT entity_type FROM entities WHERE entity_id=" + eid + ";", TRANSLATOR_TYPE);
//  }
//  
//  public static int getEntityCountByFileID(QueryExecutor executor, String fileID) {
//    return executor.executeSingleInt("SELECT COUNT(*) FROM entities WHERE file_id=" + fileID + ";");
//  }
//  
//  public static int getCountByFileID(QueryExecutor executor, Entity entityType, String fileID) {
//    return executor.executeSingleInt("SELECT COUNT(*) FROM entities WHERE file_id=" + fileID + " AND entity_type='" + entityType.name() + "';");
//  }
//  
//  
//  public static FileDB getFileByEid(QueryExecutor executor, String eid) {
//    return executor.executeSingle("SELECT files.file_id, files.name, error_count FROM entities INNER JOIN files ON files.file_id=entities.file_id WHERE entity_id=" + eid + ";", FilesTable.TRANSLATOR);
//  }
//  
//  public static String getProjectByEid(QueryExecutor executor, String eid) {
//    return executor.executeSingle("SELECT entities.project_id FROM entities WHERE entity_id=" + eid + ";");
//  }
//  
//  public static EntityDB getEntityByEid(QueryExecutor executor, String eid) {
//    return executor.executeSingle("SELECT entity_id, entity_type, fqn, modifiers FROM entities WHERE entity_id=" + eid + ";", TRANSLATOR);
//  }
//  
//  public static String getEntityFqnByID(QueryExecutor executor, String id) {
//    return executor.executeSingle("SELECT fqn FROM entities WHERE entity_id=" + id + ";");
//  }
//  
//  public static String getEidByFqn(QueryExecutor executor, String fqn, String projectID) {
//    return executor.executeSingle("SELECT entity_id FROM entities WHERE fqn='" + fqn + "' AND project_id=" + projectID + ";");
//  }
//  
//  public static EntityDB getEntityByFqn(QueryExecutor executor, String fqn, String projectID) {
//    return executor.executeSingle("SELECT entity_id, entity_type, fqn, modifiers FROM entities WHERE fqn='" + fqn + "' AND project_id=" + projectID + ";", TRANSLATOR);
//  }
//  
//  public static Collection<EntityDB> getEidBySimilarFqn(QueryExecutor executor, String fqn, String projectID) {
//    return executor.execute("SELECT entity_id, entity_type, fqn, modifiers FROM entities WHERE fqn LIKE '" + fqn + "' AND project_id=" + projectID + ";", TRANSLATOR);
//  }
//  
//  public static IterableResult<String> getEntityFQNs(QueryExecutor executor) {
//    return executor.executeStreamed("SELECT fqn FROM entities;", ResultTranslator.SIMPLE_RESULT_TRANSLATOR);
//  }
}
