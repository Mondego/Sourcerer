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
package edu.uci.ics.sourcerer.tools.java.db;

import edu.uci.ics.sourcerer.tools.java.db.importer.DatabaseInitializer;
import edu.uci.ics.sourcerer.tools.java.db.importer.ParallelDatabaseImporter;
import edu.uci.ics.sourcerer.tools.java.repo.model.JavaRepositoryFactory;
import edu.uci.ics.sourcerer.util.io.FileUtils;
import edu.uci.ics.sourcerer.util.io.arguments.Command;
import edu.uci.ics.sourcerer.utils.db.DatabaseConnectionFactory;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class Main {
  public static final Command INITIALIZE_DB =
    new Command("initialize-db", "Clean and initialize the database.") {
      protected void action() {
        DatabaseInitializer.initializeDatabase();
      }
    }.setProperties(
        DatabaseConnectionFactory.DATABASE_URL, 
        DatabaseConnectionFactory.DATABASE_USER, 
        DatabaseConnectionFactory.DATABASE_PASSWORD);
  
  public static final Command ADD_JAVA_LIBRARIES = 
    new Command("add-libraries", "Adds extracted Java libraries to the database.") {
      protected void action() {
        ParallelDatabaseImporter.importJavaLibraries();
      }
    }.setProperties(
        DatabaseConnectionFactory.DATABASE_URL, 
        DatabaseConnectionFactory.DATABASE_USER, 
        DatabaseConnectionFactory.DATABASE_PASSWORD, 
        JavaRepositoryFactory.INPUT_REPO,
        ParallelDatabaseImporter.THREAD_COUNT,
        FileUtils.TEMP_DIR);
  
  public static final Command ADD_JARS = 
    new Command("add-jars", "Adds extracted jars to the database.") {
      protected void action() {
        ParallelDatabaseImporter.importJarFiles();
      }
    }.setProperties(
        DatabaseConnectionFactory.DATABASE_URL, 
        DatabaseConnectionFactory.DATABASE_USER, 
        DatabaseConnectionFactory.DATABASE_PASSWORD, 
        JavaRepositoryFactory.INPUT_REPO,
        ParallelDatabaseImporter.THREAD_COUNT,
        FileUtils.TEMP_DIR);
  
  public static final Command ADD_PROJECTS = 
    new Command("add-projects", "Adds extracted projects to the database.") {
      protected void action() {
        ParallelDatabaseImporter.importProjects();
      }
    }.setProperties(
        DatabaseConnectionFactory.DATABASE_URL, 
        DatabaseConnectionFactory.DATABASE_USER, 
        DatabaseConnectionFactory.DATABASE_PASSWORD, 
        JavaRepositoryFactory.INPUT_REPO,
        ParallelDatabaseImporter.THREAD_COUNT,
        FileUtils.TEMP_DIR);
  
  public static final Command INTERACTIVE_FILE_ACCESSOR = 
    new Command("interactive-file-accessor", "Interactive test of the file accessor.") {
      protected void action() {
//        FileAccessor.testConsole();
      }
    }.setProperties(
        DatabaseConnectionFactory.DATABASE_URL, 
        DatabaseConnectionFactory.DATABASE_USER, 
        DatabaseConnectionFactory.DATABASE_PASSWORD, 
        JavaRepositoryFactory.INPUT_REPO); 
  
  public static final Command MOOSE_EXPORTER =
    new Command("moose-exporter", "Moose exporter!") {
      protected void action() {
//        FamixExporter.writeFamixModelToFile();
    }
  }.setProperties(
      DatabaseConnectionFactory.DATABASE_URL, 
      DatabaseConnectionFactory.DATABASE_USER, 
      DatabaseConnectionFactory.DATABASE_PASSWORD 
//      FamixExporter.PROJECT_ID,
//      FamixExporter.FAMIX_FILE.asOutput()
      );
  
  public static void main(String[] args) {
    Command.execute(args, Main.class);
  }
}
