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

import edu.uci.ics.sourcerer.model.File;
import edu.uci.ics.sourcerer.model.extracted.FileEX;
import edu.uci.ics.sourcerer.util.db.columns.Column;
import edu.uci.ics.sourcerer.util.db.columns.EnumColumn;
import edu.uci.ics.sourcerer.util.db.columns.IntColumn;
import edu.uci.ics.sourcerer.util.db.columns.StringColumn;
import edu.uci.ics.sourcerer.utils.db.DatabaseTable;
import edu.uci.ics.sourcerer.utils.db.QueryExecutor;
import edu.uci.ics.sourcerer.utils.db.TableLocker;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public final class FilesTable extends DatabaseTable {
  public static final String TABLE = "files";
  
  public FilesTable(QueryExecutor executor, TableLocker locker) {
    super(executor, locker, TABLE);
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
  
  public static final Column<Integer> FILE_ID = IntColumn.getSerial("file_id", TABLE);
  public static final Column<File> FILE_TYPE = new EnumColumn<File>("file_type", TABLE, File.values(), false) {
    @Override
    public File convertFromDB(String value) {
      return File.valueOf(value);
    }
  }.addIndex();
  public static final Column<String> NAME = StringColumn.getVarchar1024NotNull("name", TABLE).addIndex(48);
  public static final Column<String> PATH = StringColumn.getVarchar1024("path", TABLE);
  public static final Column<String> HASH = StringColumn.getVarchar32("hash", TABLE).addIndex();
  public static final Column<Integer> PROJECT_ID = IntColumn.getID("project_id", TABLE).addIndex();
  
  // ---- CREATE ----
  public void createTable() {
    executor.createTable(table,
        FILE_ID,
        FILE_TYPE,
        NAME,
        PATH,
        HASH,
        PROJECT_ID);
  }
  
  // ---- INSERT ----
  private String getInsertValue(File type, String name, String relativePath, String hash, Integer projectID) {
    return buildSerialInsertValue(
        FILE_TYPE.convertToDB(type),
        NAME.convertToDB(name),
        PATH.convertToDB(relativePath),
        HASH.convertToDB(hash),
        PROJECT_ID.convertToDB(projectID));
  }
  
  private String getInsertValue(FileEX file, Integer projectID) {
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
  
  public void insert(FileEX file, Integer projectID) {
    inserter.addValue(getInsertValue(file, projectID));
  }
  
  // ---- DELETE ----
  public void deleteByProjectID(Integer projectID) {
    executor.delete(table, PROJECT_ID.getEquals(projectID));
  }
}
