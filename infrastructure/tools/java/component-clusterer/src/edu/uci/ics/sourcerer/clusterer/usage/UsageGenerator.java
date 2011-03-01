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
package edu.uci.ics.sourcerer.clusterer.usage;

import static edu.uci.ics.sourcerer.util.io.Logging.logger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;

import edu.uci.ics.sourcerer.db.queries.DatabaseAccessor;
import edu.uci.ics.sourcerer.db.util.DatabaseConnection;
import edu.uci.ics.sourcerer.model.db.MediumEntityDB;
import edu.uci.ics.sourcerer.util.io.FileUtils;
import edu.uci.ics.sourcerer.util.io.Properties;
import edu.uci.ics.sourcerer.util.io.Property;
import edu.uci.ics.sourcerer.util.io.TablePrettyPrinter;
import edu.uci.ics.sourcerer.util.io.properties.IntegerProperty;
import edu.uci.ics.sourcerer.util.io.properties.StringProperty;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class UsageGenerator {
  public static final Property<String> FQN_USAGE_LISTING = new StringProperty("fqn-usage-listing", "fqn-usage-listing.txt", "Listing of externally used FQNs.");
  public static final Property<Integer> TOP_COUNT = new IntegerProperty("top-count", 1000, "Number of top items to compute.");
  public static final Property<String> TOP_REFERENCED_FRAGMENTS_FILE = new StringProperty("top-referenced-fragments-file", "top-referenced-fragments-file.txt", "File name for the list of the top referenced fragments.");
  
  private static class UsageDatabaseAccessor extends DatabaseAccessor {
    public UsageDatabaseAccessor(DatabaseConnection connection) {
      super (connection);
    }
    
    public Iterable<MediumEntityDB> getUsedExternalFQNs() {
      return joinQueries.getUsedExternalFQNs();
    }
  }
  
  public static void generateFqnUsageListing() {
    DatabaseConnection conn = new DatabaseConnection();
    if (conn.open()) {
      UsageDatabaseAccessor accessor = null;
      BufferedWriter bw = null;
      try {
        accessor = new UsageDatabaseAccessor(conn);
        bw = new BufferedWriter(new FileWriter(new File(Properties.OUTPUT.getValue(), FQN_USAGE_LISTING.getValue())));
        int count = 0;
        for (MediumEntityDB entity : accessor.getUsedExternalFQNs()) {
          if (++count % 10000 == 0) {
            logger.info(count + " rows written");
          }
          bw.write(entity.getFqn() + " " + entity.getEntityID() + " " + entity.getType() + "\n");
        }
      } catch (IOException e) {
        logger.log(Level.SEVERE, "Error writing to fine.", e);
      } finally {
        FileUtils.close(accessor);
        FileUtils.close(bw);
      }
    }
  }
  
  public static void createFqnUsageTree() {
    BufferedReader br = null;
    try {
      br = FileUtils.getBufferedReader(FQN_USAGE_LISTING);
      logger.info("Building tree...");
      FqnTree tree = new FqnTree();
      int count = 0;
      for (String line = br.readLine(); line != null; line = br.readLine()) {
        if (++count % 10000 == 0) {
          logger.info(count + " FQNs added...");
        }
        String[] parts = line.split(" ");
        tree.addFqn(parts[0], Integer.parseInt(parts[1]));
      }
      logger.info(count + " FQNs added...");
      logger.info("Writing tree to disk...");
      tree.writeToDisk();
      logger.info("Done!");
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Unable to read usage listing.", e);
    } finally {
      FileUtils.close(br);
    }
  }
  
  public static void printTopReferencedFragments() {
    FqnTree tree = new FqnTree();
    tree.readFromDisk();
    FqnFragment[] fragments = tree.getTopReferencedFragments(TOP_COUNT.getValue());
    TablePrettyPrinter printer = TablePrettyPrinter.getTablePrettyPrinter(TOP_REFERENCED_FRAGMENTS_FILE);
    printer.beginTable(2);
    printer.addHeader("Top FQN Fragments by Number of References");
    printer.addDividerRow();
    printer.addRow("FQN Fragment", "Count");
    printer.addDividerRow();
    for (FqnFragment fragment : fragments) {
      printer.beginRow();
      printer.addCell(fragment.getFqn());
      printer.addCell(fragment.getReferenceCount());
    }
    printer.addDividerRow();
    printer.endTable();
    printer.close();
  }
}
