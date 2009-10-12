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

import java.io.File;
import java.util.Set;


import edu.uci.ics.sourcerer.repo.base.Repository;
import edu.uci.ics.sourcerer.repo.maven.MavenCrawler;
import edu.uci.ics.sourcerer.repo.maven.MavenDownloader;
import edu.uci.ics.sourcerer.repo.maven.MavenCrawlStats;
import edu.uci.ics.sourcerer.repo.maven.MavenToRepositoryAdder;
import edu.uci.ics.sourcerer.util.io.Logging;
import edu.uci.ics.sourcerer.util.io.Property;
import edu.uci.ics.sourcerer.util.io.PropertyManager;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class Main {
  public static void main(String[] args) {
    PropertyManager.initializeProperties(args);
    Logging.initializeLogger();
    PropertyManager properties = PropertyManager.getProperties();
    if (properties.isSet(Property.CREATE_JAR_INDEX)) {
      File repo = new File(properties.getValue(Property.REPO_ROOT));
      Repository.createJarIndex(repo);
     } else if (properties.isSet(Property.AGGREGATE_JAR_FILES)) {
       File repo = properties.getValueAsFile(Property.REPO_ROOT);
       Repository.aggregateJarFiles(repo);
     } else if (properties.isSet(Property.CLEAN_REPOSITORY)) {
       File repo = properties.getValueAsFile(Property.REPO_ROOT);
      Repository.deleteCompressedRepository(repo);
    } else if (properties.isSet(Property.MIGRATE_REPOSITORY)) {
      File repo = properties.getValueAsFile(Property.REPO_ROOT);
      Set<String> completed = Logging.initializeResumeLogger();
      File source = new File(properties.getValue(Property.INPUT));
      Repository.migrateRepository(source, repo, completed);
    } else if (properties.isSet(Property.CRAWL_MAVEN)) {
      MavenCrawler.getDownloadLinks();
    } else if (properties.isSet(Property.DOWNLOAD_MAVEN)) {
      MavenDownloader.downloadLinks();
    } else if (properties.isSet(Property.ADD_MAVEN_JARS)) {
      MavenToRepositoryAdder.addToRepository();
    } else if (properties.isSet(Property.MAVEN_STATS)) {
      MavenCrawlStats.crawlStats();
    }
  }
}
