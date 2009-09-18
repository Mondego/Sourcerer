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
package edu.uci.ics.sourcerer.db.util;

import static edu.uci.ics.sourcerer.util.io.Logging.logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;

import edu.uci.ics.sourcerer.util.io.Property;
import edu.uci.ics.sourcerer.util.io.PropertyManager;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class DatabaseConnection {
  private Connection connection;
  
  public DatabaseConnection() {
    // Register the JDBC driver for MySQL
    try {
      Class.forName("com.mysql.jdbc.Driver");
    } catch (ClassNotFoundException e) {
      logger.log(Level.SEVERE, "Exception registering driver", e);
    }
  }
  
  public void open() {
    PropertyManager properties = PropertyManager.getProperties();
    try {
      connection = DriverManager.getConnection(properties.getValue(Property.DATABASE_URL), properties
          .getValue(Property.DATABASE_USER), properties.getValue(Property.DATABASE_PASSWORD));
    } catch (SQLException e) {
      logger.log(Level.SEVERE, "Exception opening connection", e);
    }
  }
  
  public Connection getConnection() {
    return connection;
  }

  public void close() {
    try {
      connection.close();
    } catch (SQLException e) {
      logger.log(Level.SEVERE, "Exception closing connection", e);
    }
  }
}
