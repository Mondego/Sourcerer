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
package edu.uci.ics.sourcerer.utils.db;

import edu.uci.ics.sourcerer.util.io.arguments.Argument;
import edu.uci.ics.sourcerer.util.io.arguments.StringArgument;
import edu.uci.ics.sourcerer.utils.db.internal.InternalDatabaseConnectionFactory;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public abstract class DatabaseConnectionFactory {
  public static final Argument<String> DATABASE_URL = new StringArgument("database-url", "Url of the database.");
  public static final Argument<String> DATABASE_USER = new StringArgument("database-user", "Database user account to use when connecting.");
  public static final Argument<String> DATABASE_PASSWORD = new StringArgument("database-password", null, "Password for the user account.");
  
  public static final DatabaseConnectionFactory INSTANCE = new InternalDatabaseConnectionFactory();
  
  public abstract DatabaseConnection create();
}
