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
package edu.uci.ics.sourcerer.clusterer;

import edu.uci.ics.sourcerer.clusterer.dir.DirectoryClusterer;
import edu.uci.ics.sourcerer.repo.general.AbstractRepository;
import edu.uci.ics.sourcerer.util.io.Command;
import edu.uci.ics.sourcerer.util.io.Logging;
import edu.uci.ics.sourcerer.util.io.Properties;
import edu.uci.ics.sourcerer.util.io.PropertyManager;
import edu.uci.ics.sourcerer.util.io.TablePrettyPrinter;

/**
 * @author Joel Ossher (jossher@uci.edu)
 *
 */
public class Main {
  public static final Command GENERATE_DIRECTORY_LISTING =
      new Command("generate-dir-listing", "Generates the directory listing file.")
        .setProperties(AbstractRepository.INPUT_REPO, DirectoryClusterer.DIRECTORY_LISTING);
  
  public static final Command PERFORM_COMPARISON =
    new Command("perform-comparison", "Performs basic directory comparison.")
      .setProperties(Properties.INPUT, DirectoryClusterer.DIRECTORY_LISTING, DirectoryClusterer.MINIMUM_DIR_SIZE, DirectoryClusterer.POPULAR_DISCARD, TablePrettyPrinter.CSV_MODE);
  
  public static final Command INTERACTIVE_RESULTS =
    new Command("interactive-results", "Interactive results viewer.")
      .setProperties(Properties.INPUT, DirectoryClusterer.DIRECTORY_LISTING, DirectoryClusterer.MINIMUM_DIR_SIZE, DirectoryClusterer.POPULAR_DISCARD);
  
  public static void main(String[] args) {
    PropertyManager.initializeProperties(args);
    Logging.initializeLogger();
    
    Command command = PropertyManager.getCommand(GENERATE_DIRECTORY_LISTING, PERFORM_COMPARISON, INTERACTIVE_RESULTS);
    
    if (command == GENERATE_DIRECTORY_LISTING) {
      DirectoryClusterer.generateDirectoryListing();
    } else if (command == PERFORM_COMPARISON) {
      DirectoryClusterer.performComparison();
    } else if (command == INTERACTIVE_RESULTS) {
      DirectoryClusterer.interactiveResultsViewer();
    }
  }
}
