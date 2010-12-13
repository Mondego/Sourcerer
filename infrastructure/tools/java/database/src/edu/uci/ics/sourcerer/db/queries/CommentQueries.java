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

import static edu.uci.ics.sourcerer.db.schema.CommentsTable.*;

import java.sql.ResultSet;
import java.sql.SQLException;

import edu.uci.ics.sourcerer.db.util.QueryExecutor;
import edu.uci.ics.sourcerer.db.util.ResultTranslator;
import edu.uci.ics.sourcerer.model.db.LocationDB;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public final class CommentQueries extends Queries {
  private static final ResultTranslator<LocationDB> LOCATION_TRANSLATOR = new ResultTranslator<LocationDB>(TABLE, FILE_ID, OFFSET, LENGTH) {
    @Override
    public LocationDB translate(ResultSet result) throws SQLException {
      return new LocationDB(FILE_ID.convertFromDB(result.getString(1)), OFFSET.convertFromDB(result.getString(2)), LENGTH.convertFromDB(result.getString(3)));
    }
  };
  
  protected CommentQueries(QueryExecutor executor) {
    super(executor);
  }
  
  public LocationDB getLocationByCommentID(Integer commentID) {
    return executor.selectSingle(COMMENT_ID.getEquals(commentID), LOCATION_TRANSLATOR); 
  }
}
