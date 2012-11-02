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

import edu.uci.ics.sourcerer.tools.java.component.identifier.RepositoryGenerator;
import edu.uci.ics.sourcerer.tools.java.component.model.jar.Fingerprint;
import edu.uci.ics.sourcerer.tools.java.component.model.repo.ComponentRepository;
import edu.uci.ics.sourcerer.tools.java.db.exported.ComponentVerifier;
import edu.uci.ics.sourcerer.tools.java.db.importer.ComponentImporter;
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
    
  public static final Command CLEAN_EXTRACTION_DATA =
    new Command("clean-extraction-data", "Clean the extraction data from the db.") {
      protected void action() {
        DatabaseInitializer.cleanExtractionData();
      }
    }.setProperties(
        DatabaseConnectionFactory.DATABASE_URL, 
        DatabaseConnectionFactory.DATABASE_USER, 
        DatabaseConnectionFactory.DATABASE_PASSWORD);
  
  public static final Command CLEAN_CRAWLED_DATA =
      new Command("clean-crawled-data", "Clean the crawled extraction data from the db.") {
        protected void action() {
          DatabaseInitializer.cleanCrawledData();
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
        ParallelDatabaseImporter.STRUCTURAL_ONLY,
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
        ParallelDatabaseImporter.STRUCTURAL_ONLY,
        FileUtils.TEMP_DIR);
  
  public static final Command ADD_FILTER_JARS = 
      new Command("add-filter-jars", "Adds extracted jars to the database.") {
        protected void action() {
          ParallelDatabaseImporter.importFilterJarFiles();
        }
      }.setProperties(
          DatabaseConnectionFactory.DATABASE_URL, 
          DatabaseConnectionFactory.DATABASE_USER, 
          DatabaseConnectionFactory.DATABASE_PASSWORD,
          JavaRepositoryFactory.INPUT_REPO,
          ParallelDatabaseImporter.THREAD_COUNT,
          ParallelDatabaseImporter.STRUCTURAL_ONLY,
          ParallelDatabaseImporter.JAR_FILTER,
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
        ParallelDatabaseImporter.STRUCTURAL_ONLY,
        FileUtils.TEMP_DIR);
  
  public static final Command ADD_COMPONENTS =
    new Command("add-components", "Identifies and adds components to the database.") {
      protected void action() {
        ComponentRepository repo = RepositoryGenerator.generateArtifactRepository();
        ComponentImporter.importComponents(repo);
      }
    }.setProperties(
        JavaRepositoryFactory.INPUT_REPO,
        Fingerprint.FINGERPRINT_MODE,
        RepositoryGenerator.JAR_FILTER_FILE,
        DatabaseConnectionFactory.DATABASE_URL, 
        DatabaseConnectionFactory.DATABASE_USER, 
        DatabaseConnectionFactory.DATABASE_PASSWORD,
        FileUtils.TEMP_DIR);
    
  public static final Command ADD_BC_METRICS =
    new Command("add-bc-metrics", "Adds bytecode metrics to the database.") {
      protected void action() {
        ParallelDatabaseImporter.addBytecodeMetrics();
      }
  }.setProperties(
      JavaRepositoryFactory.INPUT_REPO,
      DatabaseConnectionFactory.DATABASE_URL,
      DatabaseConnectionFactory.DATABASE_USER,
      DatabaseConnectionFactory.DATABASE_PASSWORD,
      ParallelDatabaseImporter.THREAD_COUNT,
      FileUtils.TEMP_DIR);
  
  public static final Command ADD_FB_METRICS =
      new Command("add-fb-metrics", "Adds findbugs metrics to the database.") {
        protected void action() {
          ParallelDatabaseImporter.addFindBugs();
        }
    }.setProperties(
        JavaRepositoryFactory.INPUT_REPO,
        DatabaseConnectionFactory.DATABASE_URL,
        DatabaseConnectionFactory.DATABASE_USER,
        DatabaseConnectionFactory.DATABASE_PASSWORD,
        ParallelDatabaseImporter.THREAD_COUNT,
        FileUtils.TEMP_DIR);
    
  public static final Command COMPUTE_COMPONENT_FIT =
    new Command("compute-component-fit", "Computes how well the identified components match the maven classification.") {
      @Override
      protected void action() {
        ComponentVerifier.computeJaccard();
      }
    }.setProperties(
        DatabaseConnectionFactory.DATABASE_URL, 
        DatabaseConnectionFactory.DATABASE_USER, 
        DatabaseConnectionFactory.DATABASE_PASSWORD,
        ComponentVerifier.JACCARD_TABLE,
        ComponentVerifier.FRAGMENTED_TABLE,
        ComponentVerifier.COMBINED_TABLE,
        ComponentVerifier.FRAGMENTED_AND_COMBINED_TABLE,
        ComponentVerifier.JACCARD_LOG,
        ComponentVerifier.IMPERFECT_JACCARD_LOG);

//  public static final Command ADD_TYPE_POPULARITY =
//    new Command("add-type-popularity", "Adds the type popularity to the database.") {
//      @Override
//      protected void action() {
//        TypePopularityImporter.importTypePopularity();
//      }
//    }.setProperties(
//        JavaRepositoryFactory.INPUT_REPO,
//        DatabaseConnectionFactory.DATABASE_URL,
//        DatabaseConnectionFactory.DATABASE_USER, 
//        DatabaseConnectionFactory.DATABASE_PASSWORD);
    
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
