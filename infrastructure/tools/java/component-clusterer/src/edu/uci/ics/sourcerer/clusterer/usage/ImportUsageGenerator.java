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

import java.io.IOException;
import java.util.logging.Level;

import edu.uci.ics.sourcerer.db.queries.DatabaseAccessor;
import edu.uci.ics.sourcerer.db.util.DatabaseConnection;
import edu.uci.ics.sourcerer.model.db.ImportFqn;
import edu.uci.ics.sourcerer.util.io.FileUtils;
import edu.uci.ics.sourcerer.util.io.Property;
import edu.uci.ics.sourcerer.util.io.properties.StringProperty;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class ImportUsageGenerator {
  public static final Property<String> IMPORT_USAGE_LISTING = new StringProperty("import-usage-listing", "import-usage-listing.txt", "List of all the import statements.");
  
  public static void generateImportUsageListing() {
    class Acc extends DatabaseAccessor {
      public Acc(DatabaseConnection connection) {
        super(connection);
      }
      
      public Iterable<ImportFqn> getImportFqns() {
        return joinQueries.getImportFqns();
      }
    }
    
    DatabaseConnection conn = new DatabaseConnection();
    if (conn.open()) {
      Acc accessor = null;
      try {
        accessor = new Acc(conn);
        FileUtils.writeLineFile(accessor.getImportFqns(), IMPORT_USAGE_LISTING);
      } catch (IOException e) {
        logger.log(Level.SEVERE, "Error writing to file.", e);
      } finally {
        FileUtils.close(accessor);
      }
    }
  }
}
