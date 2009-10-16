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

import static edu.uci.ics.sourcerer.repo.AbstractRepository.REPO_ROOT;
import static edu.uci.ics.sourcerer.util.io.Properties.INPUT;
import static edu.uci.ics.sourcerer.repo.JarIndex.JAR_INDEX_FILE;
import static edu.uci.ics.sourcerer.repo.maven.MavenCrawler.*;

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
  private static final Property<Boolean> AGGREGATE_JAR_FILES = new BooleanProperty("aggregate-jar-files", false, "Repository Manager", "Aggregates the jar files of the target repository.");
  private static final Property<Boolean> CLEAN_REPOSITORY = new BooleanProperty("clean-repository", false, "Repository Manager", "Deletes the compressed portion of the target repository.");
  private static final Property<Boolean> MIGRATE_REPOSITORY = new BooleanProperty("migrate-repository", false, "Repository Manager",
      "Migrates (while compressing) the input repository to the target repository.");
  private static final Property<Boolean> CRAWL_MAVEN = new BooleanProperty("crawl-maven", false, "Repository Manager", "Crawls the target maven repository.");
  private static final Property<Boolean> DOWNLOAD_MAVEN = new BooleanProperty("download-maven", false, "Repository Manager", "Downloads the jar file links retreived from a maven crawl.");
  private static final Property<Boolean> MAVEN_STATS = new BooleanProperty("maven-stats", false, "Repository Manager", "Gets some statistics on the links retreived from a maven crawl.");

  public static void main(String[] args) {
    PropertyManager.registerLoggingProperties();
    PropertyManager.initializeProperties(args);
    Logging.initializeLogger();
    if (CREATE_JAR_INDEX.getValue()) {
      PropertyManager.registerUsedProperties(CREATE_JAR_INDEX, REPO_ROOT, JAR_INDEX_FILE);
      PropertyManager.verifyUsage();
      Repository.createJarIndex(REPO_ROOT.getValue());
    } else if (AGGREGATE_JAR_FILES.getValue()) {
      PropertyManager.registerUsedProperties(AGGREGATE_JAR_FILES, REPO_ROOT);
      PropertyManager.verifyUsage();
      Repository.aggregateJarFiles(REPO_ROOT.getValue());
    } else if (CLEAN_REPOSITORY.getValue()) {
      PropertyManager.registerUsedProperties(CLEAN_REPOSITORY, REPO_ROOT);
      PropertyManager.verifyUsage();
      Repository.deleteCompressedRepository(REPO_ROOT.getValue());
    } else if (MIGRATE_REPOSITORY.getValue()) {
      PropertyManager.registerUsedProperties(MIGRATE_REPOSITORY, INPUT, REPO_ROOT, JAR_INDEX_FILE);
      PropertyManager.registerResumeLoggingProperties();
      PropertyManager.verifyUsage();
      Set<String> completed = Logging.initializeResumeLogger();
      Repository.migrateRepository(INPUT.getValue(), REPO_ROOT.getValue(), completed);
    } else if (CRAWL_MAVEN.getValue()) {
      PropertyManager.registerUsedProperties(CRAWL_MAVEN, MAVEN_URL, LINKS_FILE);
      PropertyManager.verifyUsage();
      MavenCrawler.getDownloadLinks();
    } else if (DOWNLOAD_MAVEN.getValue()) {
      PropertyManager.registerUsedProperties(DOWNLOAD_MAVEN, INPUT, LINKS_FILE, MAVEN_URL);
      PropertyManager.verifyUsage();
      MavenDownloader.downloadLinks();
    } else if (MAVEN_STATS.getValue()) {
      PropertyManager.registerUsedProperties(MAVEN_STATS, INPUT, LINKS_FILE, MAVEN_URL);
      PropertyManager.verifyUsage();
      MavenCrawlStats.crawlStats();
    } else {
      PropertyManager.registerUsedProperties(CREATE_JAR_INDEX, AGGREGATE_JAR_FILES, CLEAN_REPOSITORY, MIGRATE_REPOSITORY, CRAWL_MAVEN, DOWNLOAD_MAVEN, MAVEN_STATS);
      PropertyManager.printUsage();
    }
  }
}
