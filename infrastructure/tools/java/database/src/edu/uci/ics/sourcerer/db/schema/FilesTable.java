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
import edu.uci.ics.sourcerer.db.util.TableLocker;
import edu.uci.ics.sourcerer.model.File;
import edu.uci.ics.sourcerer.model.extracted.FileEX;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public final class FilesTable extends DatabaseTable {
  protected FilesTable(QueryExecutor executor, TableLocker locker) {
    super(executor, locker, "files", false);
  }
  /*  
   *  +-------------+-----------------+-------+--------+
   *  | Column name | Type            | Null? | Index? |
   *  +-------------+-----------------+-------+--------+
   *  | file_id     | SERIAL          | No    | Yes    |
   *  | file_type   | ENUM(values)    | No    | Yes    |
   *  | name        | VARCHAR(1024)   | No    | Yes    |
   *  | path        | VARCHAR(1024)   | Yes   | No     |
   *  | hash        | VARCHAR(32)     | Yes   | Yes    |
   *  | project_id  | BIGINT UNSIGNED | No    | Yes    |
   *  +-------------+-----------------+-------+--------+
   */
  
  // ---- CREATE ----
  public void createTable() {
    executor.createTable(name,
        "file_id SERIAL",
        "file_type " + getEnumCreate(File.values()) + " NOT NULL",
        "name VARCHAR(1024) BINARY NOT NULL",
        "path VARCHAR(1024) BINARY",
        "hash VARCHAR(32) BINARY",
        "project_id BIGINT UNSIGNED NOT NULL",
        "INDEX(file_type)",
        "INDEX(name(48))",
        "INDEX(hash)",
        "INDEX(project_id)");
  }
  
  // ---- INSERT ----
  private String getInsertValue(File type, String name, String relativePath, String hash, String projectID) {
    return buildSerialInsertValue(
        convertNotNullVarchar(type.name()),
        convertNotNullVarchar(name),
    		convertVarchar(relativePath),
    		convertVarchar(hash),
    		convertNotNullNumber(projectID));
  }
  
  public <T> void insert(KeyInsertBatcher<T> batcher, FileEX file, String projectID, T pairing) {
    if (file.getType() == File.JAR) {
      batcher.addValue(
          getInsertValue(
              File.JAR, 
              file.getName(), 
              null, // jars don't have relative paths
              file.getHash(), 
              projectID), 
          pairing);
    } else {
      batcher.addValue(
          getInsertValue(
              file.getType(), 
              file.getName(), 
              file.getPath(),
              null, // non-jars don't have hashes 
              projectID), 
          pairing);
    }
  }
  
  // ---- DELETE ----
  public void deleteByProjectID(String projectID) {
    executor.delete(name, "project_id=" + projectID);
  }
  
  // ---- SELECT ----
  public String getFilePathByFileID(String fileID) {
    return executor.selectSingle(name, "path", "file_id=" + fileID);
  }
}
