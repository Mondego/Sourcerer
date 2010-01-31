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
import edu.uci.ics.sourcerer.db.util.KeyInsertBatcher;
import edu.uci.ics.sourcerer.db.util.QueryExecutor;
import edu.uci.ics.sourcerer.model.Entity;
import edu.uci.ics.sourcerer.model.LocalVariable;
import edu.uci.ics.sourcerer.model.extracted.EntityEX;
import edu.uci.ics.sourcerer.model.extracted.LocalVariableEX;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public final class LibraryEntitiesTable {
  private LibraryEntitiesTable() {}
  
  public static final String TABLE = "library_entities";
  /*  
   *  +-------------+-----------------+-------+--------+
   *  | Column name | Type            | Null? | Index? |
   *  +-------------+-----------------+-------+--------+
   *  | entity_id   | SERIAL          | No    | Yes    |
   *  | entity_type | ENUM(values)    | No    | Yes    |
   *  | fqn         | VARCHAR(2048)   | No    | Yes    |
   *  | modifiers   | INT UNSIGNED    | Yes   | No     |
   *  | multi       | INT UNSIGNED    | Yes   | No     |
   *  | library_id  | BIGINT UNSIGNED | No    | Yes    |
   *  | lclass_fid  | BIGINT UNSIGNED | Yes   | Yes    |
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
        "library_id BIGINT UNSIGNED NOT NULL",
        "lclass_fid BIGINT UNSIGNED",
        "offset INT UNSIGNED",
        "length INT UNSIGNED",
        "INDEX(entity_type)",
        "INDEX(fqn(48))",
        "INDEX(library_id)",
        "INDEX(lclass_fid)");
  }
  
  // ---- INSERT ----
  public static InsertBatcher getInsertBatcher(QueryExecutor executor) {
    return executor.getInsertBatcher(TABLE);
  }
  
  public static <T> KeyInsertBatcher<T> getKeyInsertBatcher(QueryExecutor executor, KeyInsertBatcher.KeyProcessor<T> processor) {
    return executor.getKeyInsertBatcher(TABLE, processor);
  }
  
  private static String getInsertValue(Entity type, String fqn, String mods, String multi, String libraryID, String libraryClassFileID, String offset, String length) {
    return SchemaUtils.getSerialInsertValue(
    		SchemaUtils.convertNotNullVarchar(type.name()),
				SchemaUtils.convertNotNullVarchar(fqn),
				SchemaUtils.convertNumber(mods), 
				SchemaUtils.convertNumber(multi), 
				SchemaUtils.convertNotNullNumber(libraryID),
				SchemaUtils.convertNumber(libraryClassFileID),
				SchemaUtils.convertOffset(offset),
				SchemaUtils.convertLength(length));
  }
  
  public static <T> void insert(KeyInsertBatcher<T> batcher, EntityEX entity, String libraryID, T pairing) {
    batcher.addValue(getInsertValue(entity.getType(), entity.getFqn(), entity.getMods(), null, libraryID, null, null, null), pairing);
  }
  
  public static <T> void insert(KeyInsertBatcher<T> batcher, EntityEX entity, String libraryID, String libraryClassFileID, T pairing) {
    batcher.addValue(getInsertValue(entity.getType(), entity.getFqn(), entity.getMods(), null, libraryID, libraryClassFileID, entity.getStartPosition(), entity.getLength()), pairing);
  }
    
  public static String insertLocal(QueryExecutor executor, LocalVariableEX local, String libraryID) {
    Entity type = null;
    if (local.getType() == LocalVariable.LOCAL) {
      type = Entity.LOCAL_VARIABLE;
    } else if (local.getType() == LocalVariable.PARAM) {
      type = Entity.PARAMETER;
    }
    return executor.insertSingleWithKey(TABLE, getInsertValue(type, local.getName(), local.getModifiers(), local.getPosition(), libraryID, null, null, null));
  }
  
  public static String insertLocal(QueryExecutor executor, LocalVariableEX local, String libraryID, String libraryClassFileID) {
    Entity type = null;
    if (local.getType() == LocalVariable.LOCAL) {
      type = Entity.LOCAL_VARIABLE;
    } else if (local.getType() == LocalVariable.PARAM) {
      type = Entity.PARAMETER;
    }
    return executor.insertSingleWithKey(TABLE, getInsertValue(type, local.getName(), local.getModifiers(), local.getPosition(), libraryID, libraryClassFileID, local.getStartPos(), local.getLength()));
  }
  
  public static void insertPrimitive(KeyInsertBatcher<String> batcher, String name, String libraryID) {
    batcher.addValue(getInsertValue(Entity.PRIMITIVE, name, null, null, libraryID, null, null, null), name);
  }
  
  public static String insertArray(QueryExecutor executor, String fqn, int dimensions, String libraryID) {
    return executor.insertSingleWithKey(TABLE, getInsertValue(Entity.ARRAY, fqn, "NULL", Integer.toString(dimensions), libraryID, null, null, null));
  }
  
  public static String insert(QueryExecutor executor, Entity type, String fqn, String libraryID) {
    return executor.insertSingleWithKey(TABLE, getInsertValue(type, fqn, null, null, libraryID, null, null, null));
  }
  
  // ---- SELECT ----
  public static String getEntityIDByFqn(QueryExecutor executor, String fqn) {
    return executor.executeSingle("SELECT entity_id FROM " + TABLE + " WHERE fqn='" + fqn + "';"); 
  }
  
  public static String getFilteredEntityIDByFqn(QueryExecutor executor, String fqn) {
    return executor.selectSingle(TABLE, "entity_id", "fqn='" + fqn +"' AND entity_type NOT IN ('UNKNOWN','LOCAL_VARIABLE','INITIALIZER','PARAMETER','ARRAY','DUPLICATE')");
  }
}
