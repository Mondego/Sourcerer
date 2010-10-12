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
package edu.uci.ics.sourcerer.repo;

import static edu.uci.ics.sourcerer.repo.base.Repository.SPLIT_SIZE;
import static edu.uci.ics.sourcerer.repo.extracted.ExtractedRepository.EXTRACTION_STATS_FILE;
import static edu.uci.ics.sourcerer.repo.general.AbstractRepository.INPUT_REPO;
import static edu.uci.ics.sourcerer.repo.general.AbstractRepository.JARS_DIR;
import static edu.uci.ics.sourcerer.repo.general.AbstractRepository.OUTPUT_REPO;
import static edu.uci.ics.sourcerer.repo.general.AbstractRepository.PROJECT_NAMES_FILE;
import static edu.uci.ics.sourcerer.repo.general.JarIndex.JAR_INDEX_FILE;
import static edu.uci.ics.sourcerer.util.io.TablePrettyPrinter.CSV_MODE;

import java.util.Set;

import edu.uci.ics.sourcerer.repo.base.Repository;
import edu.uci.ics.sourcerer.repo.extracted.ExtractedRepository;
import edu.uci.ics.sourcerer.util.io.Command;
import edu.uci.ics.sourcerer.util.io.FileUtils;
import edu.uci.ics.sourcerer.util.io.Logging;
import edu.uci.ics.sourcerer.util.io.PropertyManager;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class Main {
  private static final Command AGGREGATE_JAR_FILES =
      new Command("aggregate-jar-files",
          "Collects all the project jar files into the jars directory.")
          .setProperties(INPUT_REPO, JARS_DIR);

  private static final Command CREATE_JAR_INDEX =
      new Command("create-jar-index", "Creates a jar index for all the jars in the jars directory.")
          .setProperties(INPUT_REPO, JARS_DIR, JAR_INDEX_FILE);

  private static final Command PRINT_JAR_STATS =
      new Command("print-jar-stats", "Prints statistics regarding the jars in the repository.")
          .setProperties(INPUT_REPO, JARS_DIR, JAR_INDEX_FILE);      

  private static final Command PRINT_PROJECT_NAMES =
      new Command("print-project-names", "Prints the names of all the projects in the repository.")
          .setProperties(PROJECT_NAMES_FILE, CSV_MODE)
          .setConditionalProperties(INPUT_REPO, OUTPUT_REPO);
  
  private static final Command MIGRATE_REPOSITORY =
      new Command("migrate-repository", "Migrates (while compressing) the input repository to the target repository.")
          .setProperties(INPUT_REPO, OUTPUT_REPO, JARS_DIR, JAR_INDEX_FILE);

  private static final Command CLEAN_JAR_MANIFESTS = 
      new Command("clean-jar-manifests", "Removes Class-Path entries from manifest files.")
          .setProperties(INPUT_REPO, JARS_DIR, JAR_INDEX_FILE);
  
//  private static final Property<Boolean> CRAWL_MAVEN = new BooleanProperty("crawl-maven", false,
//      "Repository Manager", "Crawls the target maven repository.");
//  private static final Property<Boolean> DOWNLOAD_MAVEN = new BooleanProperty("download-maven",
//      false, "Repository Manager", "Downloads the jar file links retreived from a maven crawl.");
//  private static final Property<Boolean> MAVEN_STATS = new BooleanProperty("maven-stats", false,
//      "Repository Manager", "Gets some statistics on the links retreived from a maven crawl.");
  private static final Command EXTRACTION_STATS = 
        new Command("extraction-stats", "Get extraction stats.")
            .setProperties(INPUT_REPO, EXTRACTION_STATS_FILE);
//  private static final Property<Boolean> CLONE_EXTRACTED_REPOSITORY = new BooleanProperty(
//      "clone-extracted", false, "Repository Manager",
//      "Copies the property files into a new repository.");
//  private static final Property<Boolean> GENERATE_JAR_FILTER = new BooleanProperty(
//      "generate-jar-filter", false, "Repository Manager",
//      "Generate a jar filter list from a project filter list");
  private static final Command SPLIT_PROJECTS = 
      new Command("split-projects", "Split the projects into filter lists for easy parallelization.")
          .setProperties(INPUT_REPO, SPLIT_SIZE);

  public static void main(String[] args) {
    PropertyManager.initializeProperties(args);
    Logging.initializeLogger();
    Command command = PropertyManager.getCommand(
        AGGREGATE_JAR_FILES, 
        CREATE_JAR_INDEX,
        PRINT_JAR_STATS,
        PRINT_PROJECT_NAMES,
        MIGRATE_REPOSITORY,
        CLEAN_JAR_MANIFESTS,
        EXTRACTION_STATS,
        SPLIT_PROJECTS);
    if (command == AGGREGATE_JAR_FILES) {
      Repository repo = Repository.getRepository(INPUT_REPO.getValue(), FileUtils.getTempDir());
      repo.aggregateJarFiles();
    } else if (command == CREATE_JAR_INDEX) {
      Repository repo = Repository.getRepository(INPUT_REPO.getValue());
      repo.createJarIndex();
    } else if (command == PRINT_JAR_STATS) {
      Repository repo = Repository.getRepository(INPUT_REPO.getValue());
      repo.printJarStats();
    } else if (command == PRINT_PROJECT_NAMES) {
      if (INPUT_REPO.hasValue()) {
        Repository repo = Repository.getRepository(INPUT_REPO.getValue());
        repo.printProjectNames();
      } else if (OUTPUT_REPO.hasValue()) {
        ExtractedRepository repo = ExtractedRepository.getRepository(OUTPUT_REPO.getValue());
        repo.printProjectNames();
      } else {
        throw new IllegalStateException("This should have been caught by the property manager");
      }
    } else if (command == MIGRATE_REPOSITORY) {
      Set<String> completed = Logging.initializeResumeLogger();
      Repository.migrateRepository(INPUT_REPO.getValue(), OUTPUT_REPO.getValue(), completed);
    } else if (command == CLEAN_JAR_MANIFESTS) {
      Repository.getRepository(INPUT_REPO.getValue()).getJarIndex().cleanManifestFiles();
//    } else if (CRAWL_MAVEN.getValue()) {
//      PropertyManager.registerAndVerify(CRAWL_MAVEN, MAVEN_URL, LINKS_FILE);
//      MavenCrawler.getDownloadLinks();
//    } else if (DOWNLOAD_MAVEN.getValue()) {
//      PropertyManager.registerAndVerify(DOWNLOAD_MAVEN, INPUT, INPUT_REPO, LINKS_FILE, MAVEN_URL);
//      MavenDownloader.downloadLinks();
//    } else if (MAVEN_STATS.getValue()) {
//      PropertyManager.registerAndVerify(MAVEN_STATS, INPUT, LINKS_FILE, MAVEN_URL);
//      MavenCrawlStats.crawlStats();
    } else if (command == EXTRACTION_STATS) {
      ExtractedRepository.getRepository().computeExtractionStats();
//    } else if (CLONE_EXTRACTED_REPOSITORY.getValue()) {
//      PropertyManager.registerAndVerify(INPUT_REPO, OUTPUT_REPO);
//      ExtractedRepository.getRepository().cloneProperties(
//          ExtractedRepository.getRepository(OUTPUT_REPO.getValue()));
    } else if (command == SPLIT_PROJECTS) {
      Repository.getRepository(INPUT_REPO.getValue()).splitProjectsForFilterList();
//    } else if (GENERATE_JAR_FILTER.getValue()) {
//      PropertyManager.registerResumeLoggingProperties();
//      PropertyManager.registerAndVerify(INPUT_REPO, AbstractRepository.PROJECT_FILTER);
//      Set<String> completed = Logging.initializeResumeLogger();
//      Repository.getRepository(INPUT_REPO.getValue(), FileUtils.getTempDir())
//          .generateJarFilterList(completed);
    }
  }
}
