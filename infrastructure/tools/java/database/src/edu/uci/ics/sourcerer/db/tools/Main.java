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

import static edu.uci.ics.sourcerer.db.tools.ParallelDatabaseImporter.THREAD_COUNT;
import static edu.uci.ics.sourcerer.db.util.DatabaseConnection.DATABASE_PASSWORD;
import static edu.uci.ics.sourcerer.db.util.DatabaseConnection.DATABASE_URL;
import static edu.uci.ics.sourcerer.db.util.DatabaseConnection.DATABASE_USER;
import static edu.uci.ics.sourcerer.repo.general.AbstractRepository.INPUT_REPO;
import static edu.uci.ics.sourcerer.repo.general.AbstractRepository.JARS_DIR;
import static edu.uci.ics.sourcerer.repo.general.AbstractRepository.JAR_FILTER;
import static edu.uci.ics.sourcerer.repo.general.AbstractRepository.OUTPUT_REPO;
import static edu.uci.ics.sourcerer.repo.general.AbstractRepository.PROJECT_FILTER;
import static edu.uci.ics.sourcerer.repo.general.AbstractRepository.JAR_INDEX_FILE;
import edu.uci.ics.sourcerer.db.util.DatabaseConnection;
import edu.uci.ics.sourcerer.util.io.Command;
import edu.uci.ics.sourcerer.util.io.Logging;
import edu.uci.ics.sourcerer.util.io.PropertyManager;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class Main {
  public static final Command INITIALIZE_DB =
      new Command("initialize-db", "Clean and initialize the database.")
          .setProperties(DATABASE_URL, DATABASE_USER, DATABASE_PASSWORD);
  
  public static final Command ADD_JAVA_LIBRARIES = 
      new Command("add-libraries", "Adds extracted Java libraries to the database.")
          .setProperties(DATABASE_URL, DATABASE_USER, DATABASE_PASSWORD, INPUT_REPO, THREAD_COUNT);
  
  public static final Command ADD_JARS = 
      new Command("add-jars", "Adds extracted jars to the database.")
          .setProperties(DATABASE_URL, DATABASE_USER, DATABASE_PASSWORD, INPUT_REPO, JARS_DIR, JAR_INDEX_FILE, JAR_FILTER, THREAD_COUNT);
  
  public static final Command ADD_PROJECTS = 
      new Command("add-projects", "Adds extracted projects to the database.")
          .setProperties(DATABASE_URL, DATABASE_USER, DATABASE_PASSWORD, INPUT_REPO, PROJECT_FILTER, THREAD_COUNT);
  
  public static final Command INTERACTIVE_FILE_ACCESSOR = 
      new Command("interactive-file-accessor", "Interactive test of the file accessor.")
          .setProperties(DATABASE_URL, DATABASE_USER, DATABASE_PASSWORD, INPUT_REPO, OUTPUT_REPO); 
  
  public static void main(String[] args) {
    PropertyManager.initializeProperties(args);
    Logging.initializeLogger();

    Command command = PropertyManager.getCommand(INITIALIZE_DB, ADD_JAVA_LIBRARIES, ADD_JARS, ADD_PROJECTS, INTERACTIVE_FILE_ACCESSOR);
    
    DatabaseConnection connection = new DatabaseConnection();
    connection.open();

    if (command == INITIALIZE_DB) {
      ParallelDatabaseImporter.initializeDatabase();
    } else if (command == ADD_JAVA_LIBRARIES) {
      ParallelDatabaseImporter.importJavaLibraries();
    } else if (command == ADD_JARS) {
      ParallelDatabaseImporter.importJarFiles();
    } else if (command == ADD_PROJECTS) {
      ParallelDatabaseImporter.importProjects();
    } else if (command == INTERACTIVE_FILE_ACCESSOR) {
      FileAccessor.testConsole();
    }

    connection.close();
  }
}
