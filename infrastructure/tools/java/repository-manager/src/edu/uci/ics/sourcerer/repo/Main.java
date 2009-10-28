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

import static edu.uci.ics.sourcerer.repo.general.AbstractRepository.INPUT_REPO;
import static edu.uci.ics.sourcerer.repo.general.AbstractRepository.OUTPUT_REPO;
import static edu.uci.ics.sourcerer.repo.general.JarIndex.JAR_INDEX_FILE;
import static edu.uci.ics.sourcerer.repo.maven.MavenCrawler.LINKS_FILE;
import static edu.uci.ics.sourcerer.repo.maven.MavenCrawler.MAVEN_URL;
import static edu.uci.ics.sourcerer.util.io.Properties.INPUT;

import java.util.Set;

import edu.uci.ics.sourcerer.repo.base.Repository;
import edu.uci.ics.sourcerer.repo.maven.MavenCrawlStats;
import edu.uci.ics.sourcerer.repo.maven.MavenCrawler;
import edu.uci.ics.sourcerer.repo.maven.MavenDownloader;
import edu.uci.ics.sourcerer.util.io.Logging;
import edu.uci.ics.sourcerer.util.io.Property;
import edu.uci.ics.sourcerer.util.io.PropertyManager;
import edu.uci.ics.sourcerer.util.io.properties.BooleanProperty;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class Main {
  private static final Property<Boolean> CREATE_JAR_INDEX = new BooleanProperty("create-jar-index", false, "Repository Manager", "Creates index of the jars in the target repository.");
  private static final Property<Boolean> PRINT_JAR_STATS = new BooleanProperty("print-jar-stats", false, "Repository Manager", "Prints statistics regarding the jars in the repository.");
  private static final Property<Boolean> AGGREGATE_JAR_FILES = new BooleanProperty("aggregate-jar-files", false, "Repository Manager", "Aggregates the jar files of the target repository.");
  private static final Property<Boolean> CLEAN_REPOSITORY = new BooleanProperty("clean-repository", false, "Repository Manager", "Deletes the compressed portion of the target repository.");
  private static final Property<Boolean> MIGRATE_REPOSITORY = new BooleanProperty("migrate-repository", false, "Repository Manager",
      "Migrates (while compressing) the input repository to the target repository.");
  private static final Property<Boolean> CLEAN_JAR_MANIFESTS = new BooleanProperty("clean-jar-manifests", false, "Repository Manager", "Removes Class-Path entries from manifest files.");
  private static final Property<Boolean> CRAWL_MAVEN = new BooleanProperty("crawl-maven", false, "Repository Manager", "Crawls the target maven repository.");
  private static final Property<Boolean> DOWNLOAD_MAVEN = new BooleanProperty("download-maven", false, "Repository Manager", "Downloads the jar file links retreived from a maven crawl.");
  private static final Property<Boolean> MAVEN_STATS = new BooleanProperty("maven-stats", false, "Repository Manager", "Gets some statistics on the links retreived from a maven crawl.");

  public static void main(String[] args) {
    PropertyManager.initializeProperties(args);
    Logging.initializeLogger();
    if (CREATE_JAR_INDEX.getValue()) {
      PropertyManager.registerAndVerify(CREATE_JAR_INDEX, INPUT_REPO, JAR_INDEX_FILE);
      Repository repo = Repository.getRepository(INPUT_REPO.getValue());
      repo.createJarIndex();
    } else if (PRINT_JAR_STATS.getValue()) {
      PropertyManager.registerAndVerify(PRINT_JAR_STATS, INPUT_REPO);
      Repository repo = Repository.getRepository(INPUT_REPO.getValue());
      repo.printJarStats();
    } else if (AGGREGATE_JAR_FILES.getValue()) {
      PropertyManager.registerAndVerify(AGGREGATE_JAR_FILES, INPUT_REPO);
      Repository repo = Repository.getRepository(INPUT_REPO.getValue());
      repo.aggregateJarFiles();
    } else if (CLEAN_REPOSITORY.getValue()) {
      PropertyManager.registerAndVerify(CLEAN_REPOSITORY, INPUT_REPO);
      Repository.deleteCompressedRepository(INPUT_REPO.getValue());
    } else if (MIGRATE_REPOSITORY.getValue()) {
      PropertyManager.registerResumeLoggingProperties();
      PropertyManager.registerAndVerify(MIGRATE_REPOSITORY, INPUT_REPO, OUTPUT_REPO, JAR_INDEX_FILE);
      Set<String> completed = Logging.initializeResumeLogger();
      Repository.migrateRepository(INPUT_REPO.getValue(), OUTPUT_REPO.getValue(), completed);
    } else if (CLEAN_JAR_MANIFESTS.getValue()) {
      PropertyManager.registerAndVerify(CLEAN_JAR_MANIFESTS, INPUT_REPO);
      Repository.getRepository(INPUT_REPO.getValue()).getJarIndex().cleanManifestFiles();
    } else if (CRAWL_MAVEN.getValue()) {
      PropertyManager.registerAndVerify(CRAWL_MAVEN, MAVEN_URL, LINKS_FILE);
      MavenCrawler.getDownloadLinks();
    } else if (DOWNLOAD_MAVEN.getValue()) {
      PropertyManager.registerAndVerify(DOWNLOAD_MAVEN, INPUT, INPUT_REPO, LINKS_FILE, MAVEN_URL);
      MavenDownloader.downloadLinks();
    } else if (MAVEN_STATS.getValue()) {
      PropertyManager.registerAndVerify(MAVEN_STATS, INPUT, LINKS_FILE, MAVEN_URL);
      MavenCrawlStats.crawlStats();
    } else {
      PropertyManager.registerUsedProperties(CREATE_JAR_INDEX, PRINT_JAR_STATS, AGGREGATE_JAR_FILES, CLEAN_REPOSITORY, MIGRATE_REPOSITORY, CRAWL_MAVEN, DOWNLOAD_MAVEN, MAVEN_STATS);
      PropertyManager.printUsage();
    }
  }
}
