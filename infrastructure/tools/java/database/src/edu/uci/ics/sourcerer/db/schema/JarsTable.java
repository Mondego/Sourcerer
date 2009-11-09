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
   *  | path        | VARCHAR(1024) | Yes   | No     |
   *  | version     | VARCHAR(128)  | Yes   | No     |
   *  | has_source  | BOOLEAN       | No    | No     |
   *  +-------------+---------------+-------+--------+
   */
  
  // ---- CREATE ----
  public static void createTable(QueryExecutor executor) {
    executor.createTable(TABLE,
        "jar_id SERIAL",
        "hash VARCHAR(32) BINARY NOT NULL",
        "name VARCHAR(1024) BINARY NOT NULL",
        "path VARCHAR(1024) BINARY",
        "version VARCHAR(1024) BINARY",
        "has_source BOOLEAN NOT NULL",
        "INDEX(hash)",
        "INDEX(name(48))");
  }
  
  // ---- INSERT ----
  private static String getInsertValue(String hash, String name, String path, String version, boolean hasSource) {
    return SchemaUtils.getSerialInsertValue(
        SchemaUtils.convertNotNullVarchar(hash),
				SchemaUtils.convertNotNullVarchar(name),
				SchemaUtils.convertVarchar(path),
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
    JarUsesTable.deleteByJarID(executor, jarID);
    
    // Delete the jar
    executor.delete(TABLE, "jar_id=" + jarID);
  }
  
  // ---- SELECT ----
  public static String getJarIDByHash(QueryExecutor executor, String hash) {
    return executor.selectSingle(TABLE, "jar_id", "hash='" + hash + "'");
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
