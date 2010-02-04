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
import static edu.uci.ics.sourcerer.repo.general.AbstractRepository.INPUT_REPO;
import static edu.uci.ics.sourcerer.util.io.Logging.logger;
import edu.uci.ics.sourcerer.db.util.DatabaseConnection;
import edu.uci.ics.sourcerer.util.io.Logging;
import edu.uci.ics.sourcerer.util.io.Property;
import edu.uci.ics.sourcerer.util.io.PropertyManager;
import edu.uci.ics.sourcerer.util.io.properties.BooleanProperty;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class Main {
  public static final Property<Boolean> INITIALIZE_DB = new BooleanProperty("initialize-db", false, "Database", "Clean and initialize the database.");
  public static final Property<Boolean> ADD_JAVA_LIBRARIES = new BooleanProperty("add-libraries", false, "Database", "Adds extracted Java libraries to the database.");
  public static final Property<Boolean> ADD_JARS = new BooleanProperty("add-jars", false, "Database", "Adds extracted jars to the database.");
  public static final Property<Boolean> ADD_PROJECTS = new BooleanProperty("add-projects", false, "Database", "Adds extracted projects to the database.");
  
  public static void main(String[] args) {
    PropertyManager.initializeProperties(args);
    Logging.initializeLogger();

    PropertyManager.registerAndVerify(DATABASE_URL, DATABASE_USER, DATABASE_PASSWORD);
    DatabaseConnection connection = new DatabaseConnection();
    connection.open();

    if (INITIALIZE_DB.getValue()) {
      DatabaseImporter importer = new DatabaseImporter(connection);
      importer.initializeDatabase();
    } else if (ADD_JAVA_LIBRARIES.getValue()) {
      PropertyManager.registerAndVerify(ADD_JAVA_LIBRARIES, INPUT_REPO);
      DatabaseImporter importer = new DatabaseImporter(connection);
      importer.importJavaLibraries();
    } else if (ADD_JARS.getValue()) {
      PropertyManager.registerAndVerify(ADD_JARS, INPUT_REPO);
      DatabaseImporter importer = new DatabaseImporter(connection);
      importer.importJarFiles();
    } else if (ADD_PROJECTS.getValue()) {
      PropertyManager.registerAndVerify(ADD_PROJECTS, INPUT_REPO);
      DatabaseImporter importer = new DatabaseImporter(connection);
      importer.importProjects();
    } else {
      logger.info("No action selected");
      PropertyManager.registerUsedProperties(INITIALIZE_DB, ADD_JARS, ADD_PROJECTS);
      PropertyManager.printUsage();
    }

    connection.close();
  }
}
