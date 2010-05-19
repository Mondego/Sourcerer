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

import java.io.Closeable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;

import edu.uci.ics.sourcerer.util.io.Property;
import edu.uci.ics.sourcerer.util.io.properties.StringProperty;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class DatabaseConnection implements Closeable {
  public static final Property<String> DATABASE_URL = new StringProperty("database-url", "jdbc:mysql://tagus.ics.uci.edu:3306/sourcerer", "Database", "Url of the database.");
  public static final Property<String> DATABASE_USER = new StringProperty("database-user", "Database", "Database user account to use when connecting.");
  public static final Property<String> DATABASE_PASSWORD = new StringProperty("database-password", "", "Database", "Password for the user account.");
  
  private Connection connection;
  
  public DatabaseConnection() {
    // Register the JDBC driver for MySQL
    try {
      Class.forName("com.mysql.jdbc.Driver");
    } catch (ClassNotFoundException e) {
      logger.log(Level.SEVERE, "Exception registering driver", e);
    }
  }
  
  public boolean open() {
    try {
      connection = DriverManager.getConnection(DATABASE_URL.getValue(), DATABASE_USER.getValue(), DATABASE_PASSWORD.getValue());
      return true;
    } catch (SQLException e) {
      logger.log(Level.SEVERE, "Exception opening connection", e);
      return false;
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
