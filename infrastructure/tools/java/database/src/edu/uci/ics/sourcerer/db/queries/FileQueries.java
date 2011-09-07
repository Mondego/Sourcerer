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
package edu.uci.ics.sourcerer.db.queries;

import static edu.uci.ics.sourcerer.tools.java.db.schema.FilesTable.FILE_ID;
import static edu.uci.ics.sourcerer.tools.java.db.schema.FilesTable.FILE_TYPE;
import static edu.uci.ics.sourcerer.tools.java.db.schema.FilesTable.HASH;
import static edu.uci.ics.sourcerer.tools.java.db.schema.FilesTable.NAME;
import static edu.uci.ics.sourcerer.tools.java.db.schema.FilesTable.PATH;
import static edu.uci.ics.sourcerer.tools.java.db.schema.FilesTable.PROJECT_ID;
import static edu.uci.ics.sourcerer.tools.java.db.schema.FilesTable.TABLE;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;

import edu.uci.ics.sourcerer.model.File;
import edu.uci.ics.sourcerer.model.db.FileDB;
import edu.uci.ics.sourcerer.utils.db.QueryExecutor;
import edu.uci.ics.sourcerer.utils.db.QueryResult;
import edu.uci.ics.sourcerer.utils.db.ResultTranslator;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class FileQueries extends Queries {
  private ResultTranslator<FileDB> FILE_TRANSLATOR = new ResultTranslator<FileDB>(TABLE, FILE_ID, FILE_TYPE, NAME, PATH, HASH, PROJECT_ID) {
    @Override
    public FileDB translate(ResultSet result) throws SQLException {
      return new FileDB(
          FILE_ID.convertFromDB(result.getString(1)), 
          FILE_TYPE.convertFromDB(result.getString(2)), 
          NAME.convertFromDB(result.getString(3)), 
          PATH.convertFromDB(result.getString(4)), 
          HASH.convertFromDB(result.getString(5)), 
          PROJECT_ID.convertFromDB(result.getString(6)));
    }
  };
  
  protected FileQueries(QueryExecutor executor) {
    super(executor);
  }

  public Collection<FileDB> getFilesByProjectID(Integer projectID) {
    return executor.select(PROJECT_ID.getEquals(projectID), FILE_TRANSLATOR);
  }
  
  public FileDB getByFileID(Integer fileID) {
    return executor.selectSingle(FILE_ID.getEquals(fileID), FILE_TRANSLATOR);
  }
  
  public void populateFileMap(Map<String, Integer> fileMap, Integer projectID) {
    QueryResult result = executor.execute("SELECT " + FILE_ID.getName() + "," + PATH.getName() + " FROM " + TABLE + " WHERE " + FILE_TYPE.getNequals(File.JAR) + " AND " + PROJECT_ID.getEquals(projectID));
    while (result.next()) {
      fileMap.put(PATH.convertFromDB(result.getString(2)), FILE_ID.convertFromDB(result.getString(1)));
    }
  }
}
