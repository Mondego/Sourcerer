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

import edu.uci.ics.sourcerer.db.util.KeyInsertBatcher;
import edu.uci.ics.sourcerer.db.util.QueryExecutor;
import edu.uci.ics.sourcerer.db.util.ResultTranslator;
import edu.uci.ics.sourcerer.model.Entity;
import edu.uci.ics.sourcerer.model.db.TypedEntityID;
import edu.uci.ics.sourcerer.model.extracted.EntityEX;

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
   *  +-------------+-----------------+-------+--------+
   */
  
  // ---- CREATE ----
  public static void createTable(QueryExecutor executor) {
    executor.createTable(TABLE,
        "entity_id SERIAL",
        "entity_type " + SchemaUtils.getEnumCreate(Entity.getJarValues()) + " NOT NULL",
        "fqn VARCHAR(2048) BINARY NOT NULL",
        "modifiers INT UNSIGNED",
        "multi INT UNSIGNED",
        "jar_id BIGINT UNSIGNED NOT NULL",
        "length INT UNSIGNED",
        "INDEX(entity_type)",
        "INDEX(fqn(48))",
        "INDEX(jar_id)");
  }
  
  // ---- INSERT ----
  public static <T> KeyInsertBatcher<T> getKeyInsertBatcher(QueryExecutor executor, KeyInsertBatcher.KeyProcessor<T> processor) {
    return executor.getKeyInsertBatcher(TABLE, processor);
  }
  
  private static String getInsertValue(Entity type, String fqn, String mods, String multi, String jarID) {
    return "(NULL, " +
    		"'" + type.name() + "'," +
				SchemaUtils.convertNotNullVarchar(fqn) + "," +
				SchemaUtils.convertNumber(mods) + "," +
				SchemaUtils.convertNumber(multi) + "," +
				SchemaUtils.convertNotNullNumber(jarID) + ")";
  }
  
  public static <T> void insert(KeyInsertBatcher<T> batcher, EntityEX entity, String jarID, T pairing) {
    batcher.addValue(getInsertValue(entity.getType(), entity.getFqn(), entity.getMods(), "NULL", jarID), pairing);
  }
  
  public static <T> void insertPackage(KeyInsertBatcher<T> batcher, String pkg, String jarID, T pairing) {
    batcher.addValue(getInsertValue(Entity.PACKAGE, pkg, "NULL", "NULL", jarID), pairing);
  }
  
  public static String insertParam(QueryExecutor executor, String name, String mods, String position, String libraryID) {
    return executor.insertSingleWithKey(TABLE, getInsertValue(Entity.PARAMETER, name, mods, position, libraryID));
  }
  
  public static String insertArray(QueryExecutor executor, String fqn, int dimensions, String jarID) {
    return executor.insertSingleWithKey(TABLE, getInsertValue(Entity.ARRAY, fqn, "NULL", Integer.toString(dimensions), jarID));
  }
  
  public static String insert(QueryExecutor executor, Entity type, String name, String jarID) {
    return executor.insertSingleWithKey(TABLE, getInsertValue(type, name, "NULL", "NULL", jarID));
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
  
  public static Collection<TypedEntityID> getEntityIDsByFqn(QueryExecutor executor, String fqn, String inClause) {
    if (inClause == null) {
      return null;
    } else {
      return executor.select(TABLE, "entity_id", "fqn='" + fqn + "' AND entity_type<>'UNKNOWN' AND jar_id IN " + inClause, TRANSLATOR_TEID);
    }
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
