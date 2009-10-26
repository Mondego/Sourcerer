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
package edu.uci.ics.sourcerer.db.tools;

import static edu.uci.ics.sourcerer.db.util.DatabaseConnection.DATABASE_PASSWORD;
import static edu.uci.ics.sourcerer.db.util.DatabaseConnection.DATABASE_URL;
import static edu.uci.ics.sourcerer.db.util.DatabaseConnection.DATABASE_USER;
import static edu.uci.ics.sourcerer.repo.AbstractRepository.INPUT_REPO;
import edu.uci.ics.sourcerer.db.util.DatabaseConnection;
import edu.uci.ics.sourcerer.util.io.Logging;
import edu.uci.ics.sourcerer.util.io.Property;
import edu.uci.ics.sourcerer.util.io.PropertyManager;
import edu.uci.ics.sourcerer.util.io.properties.BooleanProperty;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class Main {
  public static final Property<Boolean> INITIALIZE_DATABASE = new BooleanProperty("initialize-database", false, "Database", "Clean and initialize the database, inserting all library entities/relations.");
  public static final Property<Boolean> ADD_JARS = new BooleanProperty("add-jars", false, "Database", "Adds extracted jars to the database.");
  public static final Property<Boolean> ADD_PROJECTS = new BooleanProperty("add-projects", false, "Database", "Adds extracted projects to the database.");
  
  public static void main(String[] args) {
    PropertyManager.initializeProperties(args);
    Logging.initializeLogger();

    PropertyManager.registerAndVerify(DATABASE_URL, DATABASE_USER, DATABASE_PASSWORD);
    DatabaseConnection connection = new DatabaseConnection();
    connection.open();

    if (INITIALIZE_DATABASE.getValue()) {
      PropertyManager.registerAndVerify(INITIALIZE_DATABASE, INPUT_REPO);
      InitializeDatabase tool = new InitializeDatabase(connection);
      tool.initializeDatabase();
    } else if (ADD_JARS.getValue()) {
      PropertyManager.registerAndVerify(ADD_JARS, INPUT_REPO);
      AddJars tool = new AddJars(connection);
      tool.addJars();
    } else if (ADD_PROJECTS.getValue()) {
      PropertyManager.registerAndVerify(ADD_PROJECTS, INPUT_REPO);
       AddProjects tool = new AddProjects(connection);
       tool.addProjects();
    } else {
      PropertyManager.registerUsedProperties(INITIALIZE_DATABASE, ADD_JARS, ADD_PROJECTS);
      PropertyManager.printUsage();
    }

    connection.close();
  }
}
