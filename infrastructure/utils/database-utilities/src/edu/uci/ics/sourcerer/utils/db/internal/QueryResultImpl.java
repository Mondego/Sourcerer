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
package edu.uci.ics.sourcerer.utils.db.internal;

import static edu.uci.ics.sourcerer.util.io.logging.Logging.logger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;

import edu.uci.ics.sourcerer.utils.db.sql.QueryResult;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class QueryResultImpl implements QueryResult {
  private ResultSet results;
  
  protected QueryResultImpl(ResultSet results) {
    this.results = results;
  }
  
  public boolean next() {
    try {
      return results.next();
    } catch (SQLException e) {
      logger.log(Level.SEVERE, "Error in next", e);
      return false;
    }
  }
  
  public String getString(int column) {
    try {
      return results.getString(column);
    } catch (SQLException e) {
      logger.log(Level.SEVERE, "Unable to get column value", e);
      return null;
    }
  }
  
  public int getInt(int column) {
    try {
      return results.getInt(column);
    } catch (SQLException e) {
      logger.log(Level.SEVERE, "Unable to get column value", e);
      return -1;
    }
  }
  
  public boolean getBoolean(int column) {
    try {
      return results.getBoolean(column);
    } catch (SQLException e) {
      logger.log(Level.SEVERE, "Unable to get column value", e);
      return false;
    }
  }
  
  public boolean isNull(int column) {
    try {
      return results.getString(column) == null;
    } catch (SQLException e) {
      logger.log(Level.SEVERE, "Unable to get column value", e);
      return true;
    }
  }
}
