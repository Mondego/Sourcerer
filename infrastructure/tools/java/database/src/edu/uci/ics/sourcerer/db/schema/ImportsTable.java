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
import edu.uci.ics.sourcerer.db.util.QueryExecutor;
import edu.uci.ics.sourcerer.model.db.TypedEntityID;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public final class ImportsTable {
  private ImportsTable() {}
  
  public static final String TABLE = "imports";
  /*  
   *  +-------------+-------------------+-------+--------+
   *  | Column name | Type              | Null? | Index? |
   *  +-------------+-------------------+-------+--------+
   *  | static      | ENUM('STATIC')    | Yes   | No     |
   *  | on_demand   | ENUM('ON_DEMAND') | Yes   | No     |
   *  | leid        | BIGINT UNSIGNED   | Yes   | Yes    |
   *  | jeid        | BIGINT UNSIGNED   | Yes   | Yes    |
   *  | eid         | BIGINT UNSIGNED   | Yes   | Yes    |
   *  | project_id  | BIGINT UNSIGNED   | No    | Yes    |
   *  | file_id     | BIGINT UNSIGNED   | No    | Yes    |
   *  +-------------+-------------------+-------+--------+
   */
  
  // ---- CREATE ----
  public static void createTable(QueryExecutor executor) {
    executor.createTable(TABLE, 
        "static ENUM('STATIC')",
        "on_demand ENUM('ON_DEMAND')",
        "leid BIGINT UNSIGNED",
        "jeid BIGINT UNSIGNED",
        "eid BIGINT UNSIGNED",
        "project_id BIGINT UNSIGNED NOT NULL",
        "file_id BIGINT UNSIGNED NOT NULL",
        "INDEX(leid)",
        "INDEX(jeid)",
        "INDEX(eid)",
        "INDEX(project_id)",
        "INDEX(file_id)");
  }
  
  // ---- INSERT ----
  public static InsertBatcher getInsertBatcher(QueryExecutor executor) {
    return executor.getInsertBatcher(TABLE);
  }
  
  private static String getInsertValue(boolean isStatic, boolean onDemand, TypedEntityID eid, String fileID, String projectID) {
    return "(" +
      (isStatic ? "'STATIC'" : "NULL") + "," +
      (onDemand ? "'ON_DEMAND'" : "NULL") + "," +
      (eid.getType() == TypedEntityID.Type.LIBRARY? (eid.getID() + ",NULL,NULL,") : (eid.getType() == TypedEntityID.Type.JAR ? ("NULL," + eid.getID() + ",NULL,") : ("NULL,NULL," + eid.getID() + ","))) +
      SchemaUtils.convertNotNullNumber(fileID) + "," +
      SchemaUtils.convertNotNullNumber(projectID) + ")";
  }
  
  public static void insert(InsertBatcher batcher, boolean isStatic, boolean onDemand, TypedEntityID eid, String fileID, String projectID) {
    batcher.addValue(getInsertValue(isStatic, onDemand, eid, fileID, projectID));
  }
  
  // ---- DELETE ----
  public static void deleteByProjectID(QueryExecutor executor, String projectID) {
    executor.delete(TABLE, "project_id=" + projectID);
  }
  
  // ---- SELECT ----
//  public static final ResultTranslator<ImportDB> TRANSLATOR = new ResultTranslator<ImportDB>() {
//    @Override
//    public ImportDB translate(ResultSet result) throws SQLException {
//      TypedEntityID target = null;
//      if (result.getString(4) != null) {
//        target = TypedEntityID.getSourceEntityID(result.getString(4));
//      } else if (result.getString(5) != null) {
//        target = TypedEntityID.getJarEntityID(result.getString(5));
//      } else if (result.getString(6) != null) {
//        target = TypedEntityID.getLibraryEntityID(result.getString(6));
//      } else {
//        return null;
//      }
//
//      return new ImportDB(result.getString(1), result.getString(2) != null, result.getString(3) != null, target);
//    }
//  };
//  
//  public static Collection<ImportDB> getImports(QueryExecutor executor, String fileID) {
//    return executor.execute("SELECT file_id, static, on_demand, eid, jeid, leid FROM imports WHERE file_id=" + fileID + ";", TRANSLATOR);
//  }
}
