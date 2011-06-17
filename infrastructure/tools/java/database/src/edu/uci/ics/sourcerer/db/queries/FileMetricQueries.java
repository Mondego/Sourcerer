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

import static edu.uci.ics.sourcerer.db.schema.FileMetricsTable.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

import edu.uci.ics.sourcerer.db.util.QueryExecutor;
import edu.uci.ics.sourcerer.db.util.ResultTranslator;
import edu.uci.ics.sourcerer.model.db.FileMetricDB;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class FileMetricQueries extends Queries {
  private ResultTranslator<FileMetricDB> FILE_METRIC_TRANSLATOR = new ResultTranslator<FileMetricDB>(TABLE, PROJECT_ID, FILE_ID, METRIC_TYPE, VALUE) {
    @Override
    public FileMetricDB translate(ResultSet result) throws SQLException {
      return new FileMetricDB(
          PROJECT_ID.convertFromDB(result.getString(1)),
          FILE_ID.convertFromDB(result.getString(2)),
          METRIC_TYPE.convertFromDB(result.getString(3)), 
          VALUE.convertFromDB(result.getString(4)));
    }
  };
  
  protected FileMetricQueries(QueryExecutor executor) {
    super(executor);
  }
  
  public Collection<FileMetricDB> getMetricsByProjectID(Integer projectID) {
    return executor.select(PROJECT_ID.getEquals(projectID), FILE_METRIC_TRANSLATOR);
  }
}
