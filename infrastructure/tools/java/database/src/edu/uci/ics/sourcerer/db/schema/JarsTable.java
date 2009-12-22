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

import edu.uci.ics.sourcerer.db.util.QueryExecutor;
import edu.uci.ics.sourcerer.db.util.ResultTranslator;
import edu.uci.ics.sourcerer.model.db.JarDB;
import edu.uci.ics.sourcerer.repo.extracted.ExtractedJar;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public final class JarsTable {
  private JarsTable() {}
  
  public static final String TABLE = "jars";
  /*  
   *  +-------------+---------------+-------+--------+
   *  | Column name | Type          | Null? | Index? |
   *  +-------------+---------------+-------+--------+
   *  | jar_id      | SERIAL        | No    | Yes    |
   *  | hash        | VARCHAR(32)   | No    | Yes    |
   *  | name        | VARCHAR(1024) | No    | Yes    |
   *  | group       | VARCHAR(1024) | Yes   | Yes    |
   *  | version     | VARCHAR(128)  | Yes   | No     |
   *  | has_source  | BOOLEAN       | No    | No     |
   *  +-------------+---------------+-------+--------+
   */
  
  // ---- LOCK ----
  public static String getReadLock() {
    return SchemaUtils.getReadLock(TABLE);
  }
  
  public static String getWriteLock() {
    return SchemaUtils.getWriteLock(TABLE);
  }
  
  // ---- CREATE ----
  public static void createTable(QueryExecutor executor) {
    executor.createTable(TABLE,
        "jar_id SERIAL",
        "hash VARCHAR(32) BINARY NOT NULL",
        "name VARCHAR(1024) BINARY NOT NULL",
        "group VARCHAR(1024) BINARY",
        "version VARCHAR(1024) BINARY",
        "has_source BOOLEAN NOT NULL",
        "INDEX(hash)",
        "INDEX(name(48))",
        "INDEX(group(48))");
  }
  
  // ---- INSERT ----
  private static String getInsertValue(String hash, String name, String group, String version, boolean hasSource) {
    return SchemaUtils.getSerialInsertValue(
        SchemaUtils.convertNotNullVarchar(hash),
				SchemaUtils.convertNotNullVarchar(name),
				SchemaUtils.convertVarchar(group),
				SchemaUtils.convertVarchar(version),
				SchemaUtils.convertBoolean(hasSource));
  }
  
  public static String insert(QueryExecutor executor, ExtractedJar jar) {
    return executor.insertSingleWithKey(TABLE, getInsertValue(jar.getHash(), jar.getName(), jar.getGroup(), jar.getVersion(), jar.hasSource()));
  }
  
  // ---- DELETE ----
  public static void deleteJar(QueryExecutor executor, String jarID) {
    // Delete the class files
    JarClassFilesTable.deleteByJarID(executor, jarID);
    
    // Delete the problems
    JarProblemsTable.deleteByJarID(executor, jarID);
    
    // Delete the imports
    JarImportsTable.deleteByJarID(executor, jarID);
    
    // Delete the entities
    JarEntitiesTable.deleteByJarID(executor, jarID);
    
    // Delete the relations
    JarRelationsTable.deleteByJarID(executor, jarID);
    
    // Delete the comments
    JarCommentsTable.deleteByJarID(executor, jarID);
    
    // Delete the uses
    UsedJarsTable.deleteByJarID(executor, jarID);
    
    // Delete the jar
    executor.delete(TABLE, "jar_id=" + jarID);
  }
  
  // ---- SELECT ----
  public static final ResultTranslator<JarDB> JAR_RESULT_TRANSLATOR = new ResultTranslator<JarDB>() {
    @Override
    public JarDB translate(ResultSet result) throws SQLException {
      String jarID = result.getString(1);
      String hash = result.getString(2);
      String name = result.getString(3);
      String group = result.getString(4);
      String version = result.getString(5);
      boolean hasSource = result.getBoolean(6);
      return new JarDB(jarID, hash, name, group, version, hasSource);
    }
    
    public String getSelect() {
      return "jar_id,hash,name,path,version,has_source";
    }
  };
  
  
  public static String getJarIDByHash(QueryExecutor executor, String hash) {
    return executor.selectSingle(TABLE, "jar_id", "hash='" + hash + "'");
  }
  
  public static String getHashByID(QueryExecutor executor, String jarID) {
    return executor.selectSingle(TABLE, "hash", "jar_id=" + jarID);
  }
  
  public static JarDB getJarByJarID(QueryExecutor executor, String jarID) {
    return executor.selectSingle(TABLE, JAR_RESULT_TRANSLATOR.getSelect(), "jar_id=" + jarID, JAR_RESULT_TRANSLATOR);
  }
  
//  public static String getJarCount(QueryExecutor executor) {
//    return executor.getRowCount(TABLE);
//  }
//  
//  public static String getJarIDByName(QueryExecutor executor, String name) {
//    return executor.selectSingle(TABLE, "jar_id", "name='" + name + "'");
//  }
//  
}
