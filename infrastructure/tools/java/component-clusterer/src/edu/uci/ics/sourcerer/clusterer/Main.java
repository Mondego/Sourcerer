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
import edu.uci.ics.sourcerer.clusterer.file.FileClusterer;
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
  
  public static final Command GENERATE_COMPARISON_FILES =
    new Command("generate-comparison-files", "Performs basic directory comparison.")
      .setProperties(Properties.INPUT, DirectoryClusterer.DIRECTORY_LISTING, DirectoryClusterer.MINIMUM_DIR_SIZE, DirectoryClusterer.POPULAR_DISCARD, DirectoryClusterer.MATCHED_DIRECTORIES, DirectoryClusterer.MATCHED_FILES, DirectoryClusterer.POPULAR_NAMES, TablePrettyPrinter.CSV_MODE);
  
  public static final Command COMPILE_STATISTICS =
    new Command("compile-stats", "Compile statistics from directory comparison.")
      .setProperties(Properties.INPUT, DirectoryClusterer.MATCHED_DIRECTORIES, DirectoryClusterer.MATCHED_FILES);
  
  public static final Command INTERACTIVE_RESULTS =
    new Command("interactive-results", "Interactive results viewer.")
      .setProperties(Properties.INPUT, DirectoryClusterer.DIRECTORY_LISTING, DirectoryClusterer.MINIMUM_DIR_SIZE, DirectoryClusterer.POPULAR_DISCARD);
  
  public static final Command GENERATE_FILE_LISTING =
      new Command("generate-file-listing", "Generates the file listing file.").
        setProperties(AbstractRepository.INPUT_REPO, FileClusterer.FILE_LISTING);
  
  public static void main(String[] args) {
    PropertyManager.initializeProperties(args);
    Logging.initializeLogger();
    
    Command command = PropertyManager.getCommand(GENERATE_DIRECTORY_LISTING, GENERATE_COMPARISON_FILES, COMPILE_STATISTICS, INTERACTIVE_RESULTS, GENERATE_FILE_LISTING);
    
    if (command == GENERATE_DIRECTORY_LISTING) {
      DirectoryClusterer.generateDirectoryListing();
    } else if (command == GENERATE_COMPARISON_FILES) {
      DirectoryClusterer.generateComparisonFiles();
    } else if (command == COMPILE_STATISTICS) {
      DirectoryClusterer.compileStatistics();
    } else if (command == INTERACTIVE_RESULTS) {
      DirectoryClusterer.interactiveResultsViewer();
    } else if (command == GENERATE_FILE_LISTING) {
      FileClusterer.generateFileListing();
    }
  }
}
