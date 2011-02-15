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
package edu.uci.ics.sourcerer.repo.stats;

import edu.uci.ics.sourcerer.repo.base.Repository;
import edu.uci.ics.sourcerer.repo.extracted.ExtractedRepository;
import edu.uci.ics.sourcerer.util.io.Property;
import edu.uci.ics.sourcerer.util.io.properties.StringProperty;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class RepositoryStatistics {
  public static final Property<String> JAR_STATS_FILE = new StringProperty("jar-stats-file", "jar-stats.txt", "File containing repository jar statistics.");
  public static void printJarStatistics(Repository repo) {}
  
  public static final Property<String> PROJECT_SIZES_FILE = new StringProperty("project-sizes-file", "project-sizes.txt", "File containing project size information");
  public static void printProjectSizes(Repository repo) {}
  
  public static final Property<String> PROJECT_NAMES_FILE = new StringProperty("project-names-file", "project-names.txt", "File containing the names of the projects in the repository.");
  public static void printProjectNames(Repository repo) {}
  public static void printProjectNames(ExtractedRepository repo) {}
}
