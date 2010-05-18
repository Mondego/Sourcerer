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
import java.util.Map;

import edu.uci.ics.sourcerer.db.util.QueryExecutor;
import edu.uci.ics.sourcerer.db.util.QueryResult;
import edu.uci.ics.sourcerer.db.util.ResultTranslator;
import edu.uci.ics.sourcerer.db.util.TableLocker;
import edu.uci.ics.sourcerer.model.File;
import edu.uci.ics.sourcerer.model.db.FileDB;
import edu.uci.ics.sourcerer.model.extracted.FileEX;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public final class FilesTable extends DatabaseTable {
  protected FilesTable(QueryExecutor executor, TableLocker locker) {
    super(executor, locker, "files");
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
  
  private String getInsertValue(FileEX file, String projectID) {
    if (file.getType() == File.JAR) {
      return
          getInsertValue(
              File.JAR, 
              file.getName(), 
              null, // jars don't have relative paths
              file.getHash(), 
              projectID);
    } else {
      return
          getInsertValue(
              file.getType(), 
              file.getName(), 
              file.getPath(),
              null, // non-jars don't have hashes 
              projectID);
    }
  }
  
  public void insert(FileEX file, String projectID) {
    inserter.addValue(getInsertValue(file, projectID));
  }
  
  // ---- DELETE ----
  public void deleteByProjectID(String projectID) {
    executor.delete(name, "project_id=" + projectID);
  }
  
  // ---- SELECT ----
  private ResultTranslator<FileDB> FILE_TRANSLATOR = new ResultTranslator<FileDB>() {
    @Override
    public FileDB translate(ResultSet result) throws SQLException {
      return new FileDB(result.getString(1), File.valueOf(result.getString(2)), result.getString(3), result.getString(4), result.getString(5), result.getString(6));
    }

    @Override
    public String getSelect() {
      return "file_id,file_type,name,path,hash,project_id";
    }
  };
  
  public FileDB getFileByFileID(String fileID) {
    return executor.selectSingle(name, FILE_TRANSLATOR.getSelect(), "file_id=" + fileID, FILE_TRANSLATOR);
  }
  
  public void populateFileMap(Map<String, String> fileMap, String projectID) {
    QueryResult result = executor.execute("SELECT file_id,path FROM " + name + " WHERE file_type <> 'JAR' AND project_id=" + projectID);
    while (result.next()) {
      fileMap.put(result.getString(2), result.getString(1));
    }
  }
}
