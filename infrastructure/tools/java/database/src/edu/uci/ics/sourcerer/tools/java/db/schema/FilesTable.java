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
package edu.uci.ics.sourcerer.tools.java.db.schema;

import edu.uci.ics.sourcerer.tools.java.model.extracted.FileEX;
import edu.uci.ics.sourcerer.tools.java.model.types.File;
import edu.uci.ics.sourcerer.utils.db.Insert;
import edu.uci.ics.sourcerer.utils.db.sql.Column;
import edu.uci.ics.sourcerer.utils.db.sql.DatabaseTable;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public final class FilesTable extends DatabaseTable {
  /* 
   *                    files table 
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
  public static final FilesTable TABLE = new FilesTable();
  
  public static final Column<Integer> FILE_ID = TABLE.addSerialColumn("file_id");
  public static final Column<File> FILE_TYPE = TABLE.addEnumColumn("file_type", File.values(), false).addIndex();
  public static final Column<String> NAME = TABLE.addVarcharColumn("name", 1024, false).addIndex(48);
  public static final Column<String> PATH = TABLE.addVarcharColumn("path", 1024, true);
  public static final Column<String> HASH = TABLE.addVarcharColumn("hash", 32, true).addIndex();
  public static final Column<Integer> PROJECT_ID = TABLE.addIDColumn("project_id", false).addIndex();
  
  private FilesTable() {
    super("files");
  }
  // ---- INSERT ----
  private static Insert createInsert(File type, String name, String relativePath, String hash, Integer projectID) {
    return TABLE.createInsert(
        FILE_TYPE.to(type),
        NAME.to(name),
        PATH.to(relativePath),
        HASH.to(hash),
        PROJECT_ID.to(projectID));
  }
  
  public static Insert createInsert(FileEX file, Integer projectID) {
    if (file.getType() == File.JAR) {
      return
          createInsert(
              File.JAR, 
              file.getName(), 
              null, // jars don't have relative paths
              file.getHash(), 
              projectID);
    } else {
      return
          createInsert(
              file.getType(), 
              file.getName(), 
              file.getPath(),
              null, // non-jars don't have hashes 
              projectID);
    }
  }
  
//  public void insert(FileEX file, Integer projectID) {
//    inserter.addValue(getInsertValue(file, projectID));
//  }
//  
//  // ---- DELETE ----
//  public void deleteByProjectID(Integer projectID) {
//    executor.delete(table, PROJECT_ID.getEquals(projectID));
//  }
}
