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
import edu.uci.ics.sourcerer.model.extracted.FileEX;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public final class FilesTable {
  private FilesTable() {}
  
  public static final String TABLE = "files";
  /*  
   *  +-------------+-----------------+-------+--------+
   *  | Column name | Type            | Null? | Index? |
   *  +-------------+-----------------+-------+--------+
   *  | file_id     | SERIAL          | No    | Yes    |
   *  | name        | VARCHAR(1024)   | No    | Yes    |
   *  | path        | VARCHAR(1024)   | No    | No     |
   *  | project_id  | BIGINT UNSIGNED | No    | Yes    |
   *  +-------------+-----------------+-------+--------+
   */
  
  // ---- CREATE ----
  public static void createTable(QueryExecutor executor) {
    executor.createTable(TABLE,
        "file_id SERIAL",
        "name VARCHAR(1024) BINARY NOT NULL",
        "path VARCHAR(1024) BINARY NOT NULL",
        "project_id BIGINT UNSIGNED NOT NULL",
        "INDEX(name(48))",
        "INDEX(project_id");
  }
  
  // ---- INSERT ----
  public static <T> KeyInsertBatcher<T> getKeyInsertBatcher(QueryExecutor executor, KeyInsertBatcher.KeyProcessor<T> processor) {
    return executor.getKeyInsertBatcher(TABLE, processor);
  }
  
  private static String getInsertValue(String name, String relativePath, String projectID) {
    return "(NULL," +
    		SchemaUtils.convertNotNullVarchar(name) + "," +
    		SchemaUtils.convertNotNullVarchar(relativePath) + "," +
    		SchemaUtils.convertNotNullNumber(projectID) + ")";
  }
  
  public static <T> void insert(KeyInsertBatcher<T> batcher, FileEX file, String projectID, T pairing) {
    batcher.addValue(getInsertValue(file.getName(), file.getRelativePath(), projectID), pairing);
  }
  
  // ---- DELETE ----
  public static void deleteByProjectID(QueryExecutor executor, String projectID) {
    executor.delete(TABLE, "project_id=" + projectID);
  }
  
  // ---- SELECT ----
//  public static final ResultTranslator<FileDB> TRANSLATOR = new ResultTranslator<FileDB>() {
//    @Override
//    public FileDB translate(ResultSet result) throws SQLException {
//      return new FileDB(result.getString(1), result.getString(2), result.getString(3));
//    }
//  };
//  
//  public static String getNameByFid(QueryExecutor executor, String fid) {
//    return executor.executeSingle("SELECT name FROM files WHERE file_id=" + fid + ";");
//  }
//  
//  public static FileDB getFileByFileID(QueryExecutor executor, String fileID) {
//    return executor.executeSingle("SELECT file_id, name, error_count FROM files WHERE file_id=" + fileID + ";", TRANSLATOR);
//  }
//  
//  public static String getProjectsWithFiles(QueryExecutor executor) {
//    return executor.executeSingle("SELECT COUNT(DISTINCT project_id) FROM files;");
//  }
}
