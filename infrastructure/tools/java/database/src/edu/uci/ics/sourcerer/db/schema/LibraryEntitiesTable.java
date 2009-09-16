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
import edu.uci.ics.sourcerer.model.extracted.EntityEX;

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
   *  +-------------+-----------------+-------+--------+
   */
  
  // ---- CREATE ----
  public static void createTable(QueryExecutor executor) {
    executor.createTable(TABLE,
        "entity_id SERIAL",
        "entity_type " + SchemaUtils.getEnumCreate(Entity.getLibraryValues()) + " NOT NULL",
        "fqn VARCHAR(2048) BINARY NOT NULL",
        "modifiers INT UNSIGNED",
        "multi INT UNSIGNED",
        "library_id BIGINT UNSIGNED NOT NULL",
        "INDEX(entity_type)",
        "INDEX(fqn(48))",
        "INDEX(library_id)");
  }
  
  // ---- INSERT ----
  public static InsertBatcher getInsertBatcher(QueryExecutor executor) {
    return executor.getInsertBatcher(TABLE);
  }
  
  public static <T> KeyInsertBatcher<T> getKeyInsertBatcher(QueryExecutor executor, KeyInsertBatcher.KeyProcessor<T> processor) {
    return executor.getKeyInsertBatcher(TABLE, processor);
  }
  
  private static String getInsertValue(Entity type, String fqn, String mods, String multi, String libraryID) {
    return "(NULL, " +
    		"'" + type.name() + "'," +
				SchemaUtils.convertNotNullVarchar(fqn) + "," +
				SchemaUtils.convertNumber(mods) + "," + 
				SchemaUtils.convertNumber(multi) + "," + 
				SchemaUtils.convertNotNullNumber(libraryID) + ")";
  }
  
  public static <T> void insert(KeyInsertBatcher<T> batcher, EntityEX entity, String libraryID, T pairing) {
    batcher.addValue(getInsertValue(entity.getType(), entity.getFqn(), entity.getMods(), "NULL", libraryID), pairing);
  }
  
  public static <T> void insertPackage(KeyInsertBatcher<T> batcher, String pkg, String libraryID, T pairing) {
    batcher.addValue(getInsertValue(Entity.PACKAGE, pkg, "NULL", "NULL", libraryID), pairing);
  }
  
  public static String insertParam(QueryExecutor executor, String name, String mods, String position, String libraryID) {
    return executor.insertSingleWithKey(TABLE, getInsertValue(Entity.PARAMETER, name, mods, position, libraryID));
  }
  
  public static void insertPrimitive(InsertBatcher batcher, String name, String libraryID) {
    batcher.addValue(getInsertValue(Entity.PRIMITIVE, name, "NULL", "NULL", libraryID));
  }
  
  public static String insertArray(QueryExecutor executor, String fqn, int dimensions, String libraryID) {
    return executor.insertSingleWithKey(TABLE, getInsertValue(Entity.ARRAY, fqn, "NULL", Integer.toString(dimensions), libraryID));
  }
  
  public static String insert(QueryExecutor executor, Entity type, String fqn, String libraryID) {
    return executor.insertSingleWithKey(TABLE, getInsertValue(type, fqn, "NULL", "NULL", libraryID));
  }
  
  // ---- SELECT ----
  public static String getEntityIDByFqn(QueryExecutor executor, String fqn) {
    return executor.executeSingle("SELECT entity_id FROM " + TABLE + " WHERE fqn='" + fqn + "';"); 
  }
}
