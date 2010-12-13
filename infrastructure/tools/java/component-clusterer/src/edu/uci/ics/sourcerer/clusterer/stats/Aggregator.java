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
package edu.uci.ics.sourcerer.clusterer.stats;

import static edu.uci.ics.sourcerer.util.io.Logging.logger;

import edu.uci.ics.sourcerer.clusterer.dir.DirectoryClusterer;
import edu.uci.ics.sourcerer.clusterer.hash.HashingClusterer;
import edu.uci.ics.sourcerer.util.io.Property;
import edu.uci.ics.sourcerer.util.io.TablePrettyPrinter;
import edu.uci.ics.sourcerer.util.io.properties.StringProperty;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class Aggregator {
  public static final Property<String> GENERAL_STATISTICS = new StringProperty("general-statistics", "general-statistics.txt", "General statistics for all cloning methods.");
  
  public static void computeGeneralStats() {
    Matching dir80 = DirectoryClusterer.getMatching80();
    MatchingStatistics dir80stats = dir80.getStatistics();
    Matching hashing = HashingClusterer.getMatching();
    MatchingStatistics hashingStats = hashing.getStatistics();
    
    TablePrettyPrinter printer = TablePrettyPrinter.getTablePrettyPrinter(GENERAL_STATISTICS);
    printer.beginTable(3);
    printer.addDividerRow();
    printer.addRow("", "Dir 80", "Hashing");
    printer.addDividerRow();
    
    // Total number of files
    printer.beginRow();
    printer.addCell("Total Files");
    printer.addCell(dir80stats.getTotalFiles());
    printer.addCell(hashingStats.getTotalFiles());
    
    // Total number of projects
    printer.beginRow();
    printer.addCell("Total Projects");
    printer.addCell(dir80stats.getTotalProjects());
    printer.addCell(hashingStats.getTotalProjects());
    
    // Eliminate exact duplicates within projects
    printer.beginRow();
    printer.addCell("Project Unique Files");
    printer.addCell(dir80stats.getProjectUniqueFiles());
    printer.addCell(hashingStats.getProjectUniqueFiles());
    
    // Eliminate exact duplicates globally
    printer.beginRow();
    printer.addCell("Global Unique Files");
    printer.addCell(dir80stats.getGlobalUniqueFiles());
    printer.addCell(hashingStats.getGlobalUniqueFiles());
    
    // Files that only occur in one place
    printer.beginRow();
    printer.addCell("Singleton Files");
    printer.addCell(dir80stats.getSingletonFiles());
    printer.addCell(hashingStats.getSingletonFiles());
    
    // Unique files that occur in more than one project
    printer.beginRow();
    printer.addCell("Unique Duplicate Files");
    printer.addCell(dir80stats.getUniqueDuplicateFiles());
    printer.addCell(hashingStats.getUniqueDuplicateFiles());
    
    // Total count of files that occur in more than one project
    printer.beginRow();
    printer.addCell("Total Duplicate Files");
    printer.addCell(dir80stats.getTotalDuplicateFiles());
    printer.addCell(hashingStats.getTotalDuplicateFiles());
    
    // Percent of files that occur in more than one project
    // Total Duplicate Files / Project Unique Files
    printer.beginRow();
    printer.addCell("Duplication Rate");
    printer.addCell(dir80stats.getDupRate());
    printer.addCell(hashingStats.getDupRate());
    
    // Average number of times a duplicate file appears
    printer.beginRow();
    printer.addCell("Occurance Rate");
    printer.addCell(dir80stats.getDupOccuranceRate());
    printer.addCell(hashingStats.getDupOccuranceRate());
    
    // Average percent of duplication at a project level
    printer.beginRow();
    printer.addCell("Project Duplication Rate");
    printer.addCell(dir80stats.getProjectDupRate());
    printer.addCell(hashingStats.getProjectDupRate());
    
    printer.addDividerRow();
    printer.endTable();
    printer.close();
    
    logger.info("Done!");
  }
}
