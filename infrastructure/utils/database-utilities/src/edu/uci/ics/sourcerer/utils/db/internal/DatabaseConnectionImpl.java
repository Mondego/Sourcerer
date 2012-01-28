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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;

import edu.uci.ics.sourcerer.utils.db.DatabaseConnectionFactory;
import edu.uci.ics.sourcerer.utils.db.DatabaseConnection;
import edu.uci.ics.sourcerer.utils.db.QueryExecutor;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
class DatabaseConnectionImpl implements DatabaseConnection {
  private Connection connection;
  
  DatabaseConnectionImpl() {}
  
  @Override
  public boolean open() {
    try {
      connection = DriverManager.getConnection(DatabaseConnectionFactory.DATABASE_URL.getValue(), DatabaseConnectionFactory.DATABASE_USER.getValue(), DatabaseConnectionFactory.DATABASE_PASSWORD.getValue());
      return true;
    } catch (SQLException e) {
      logger.log(Level.SEVERE, "Exception opening connection", e);
      connection = null;
      return false;
    }
  }
  
  @Override
  public void close() {
    try {
      if (connection != null) {
        connection.close();
      }
    } catch (SQLException e) {
      logger.log(Level.SEVERE, "Exception closing connection", e);
    }
  }
  
  @Override
  public QueryExecutor getExecutor() {
    if (connection == null) {
      throw new IllegalStateException("Must open connection first.");
    } else {
      return QueryExecutorImpl.make(connection);
    }
  }
}
