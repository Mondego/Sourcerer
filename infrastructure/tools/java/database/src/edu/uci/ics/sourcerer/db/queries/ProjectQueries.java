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

import static edu.uci.ics.sourcerer.db.schema.ProjectsTable.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

import edu.uci.ics.sourcerer.db.schema.ProjectsTable;
import edu.uci.ics.sourcerer.model.Project;
import edu.uci.ics.sourcerer.model.db.LargeProjectDB;
import edu.uci.ics.sourcerer.model.db.SmallProjectDB;
import edu.uci.ics.sourcerer.util.db.BasicResultTranslator;
import edu.uci.ics.sourcerer.util.db.QueryExecutor;
import edu.uci.ics.sourcerer.util.db.ResultTranslator;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class ProjectQueries extends Queries {
  private ResultTranslator<SmallProjectDB> SMALL_PROJECT_TRANSLATOR = new ResultTranslator<SmallProjectDB>(TABLE, PROJECT_ID, PROJECT_TYPE, PATH, HASH) {
    @Override
    public SmallProjectDB translate(ResultSet result) throws SQLException {
      return new SmallProjectDB(
          PROJECT_ID.convertFromDB(result.getString(1)),
          PROJECT_TYPE.convertFromDB(result.getString(2)),
          PATH.convertFromDB(result.getString(3)), 
          HASH.convertFromDB(result.getString(4)));
    }
  };
  
  private ResultTranslator<LargeProjectDB> LARGE_PROJECT_TRANSLATOR = new ResultTranslator<LargeProjectDB>(TABLE, PROJECT_ID, PROJECT_TYPE, NAME, DESCRIPTION, VERSION, GROUP, PATH, HASH, HAS_SOURCE) {
    @Override
    public LargeProjectDB translate(ResultSet result) throws SQLException {
      return new LargeProjectDB(
          PROJECT_ID.convertFromDB(result.getString(1)),
          PROJECT_TYPE.convertFromDB(result.getString(2)), 
          NAME.convertFromDB(result.getString(3)),
          DESCRIPTION.convertFromDB(result.getString(4)),
          VERSION.convertFromDB(result.getString(5)),
          GROUP.convertFromDB(result.getString(6)),
          PATH.convertFromDB(result.getString(7)),
          HASH.convertFromDB(result.getString(8)), 
          HAS_SOURCE.convertFromDB(result.getString(9)));
    }
  };
  
  protected ProjectQueries(QueryExecutor executor) {
    super(executor);
  }
  
  public Integer getProjectIDByPath(String path) {
    return executor.selectSingleInt(ProjectsTable.TABLE, PROJECT_ID.getName(), PATH.getEquals(path));
  }
  
  public Integer getProjectIDByHash(String hash) {
    return executor.selectSingleInt(ProjectsTable.TABLE, PROJECT_ID.getName(), HASH.getEquals(hash));
  }
  
  public Integer getProjectIDByName(String name) {
    return executor.selectSingleInt(ProjectsTable.TABLE, PROJECT_ID.getName(), NAME.getEquals(name));
  }
  
  public Integer getPrimitiveProjectID() {
    return executor.selectSingleInt(ProjectsTable.TABLE, PROJECT_ID.getName(), NAME.getEquals(PRIMITIVES_PROJECT));
  }
  
  public Integer getUnknownsProjectID() {
    return executor.selectSingleInt(ProjectsTable.TABLE, PROJECT_ID.getName(), NAME.getEquals(UNKNOWNS_PROJECT));
  }
    
  public Collection<SmallProjectDB> getSmallByType(Project type) {
    return executor.select(PROJECT_TYPE.getEquals(type), SMALL_PROJECT_TRANSLATOR);
  }
  
  public Collection<Integer> getProjectIDsByType(Project type) {
    return executor.select(ProjectsTable.TABLE, PROJECT_ID.getName(), PROJECT_TYPE.getEquals(type), BasicResultTranslator.SIMPLE_INT_TRANSLATOR);
  }
  
  public SmallProjectDB getSmallByHash(String hash) {
    return executor.selectSingle(HASH.getEquals(hash), SMALL_PROJECT_TRANSLATOR);
  }
  
  public SmallProjectDB getSmallByPath(String path) {
    return executor.selectSingle(PATH.getEquals(path), SMALL_PROJECT_TRANSLATOR);
  }
  
  public LargeProjectDB getLargeByProjectID(Integer projectID) {
    return executor.selectSingle(PROJECT_ID.getEquals(projectID), LARGE_PROJECT_TRANSLATOR);
  }
  
  public String getHashByProjectID(Integer projectID) {
    return executor.selectSingle(TABLE, HASH.getName(), PROJECT_ID.getEquals(projectID));
  }
}
