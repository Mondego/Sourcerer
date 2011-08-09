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

import edu.uci.ics.sourcerer.db.queries.InlineDatabaseAccessor;
import edu.uci.ics.sourcerer.util.io.FileUtils;
import edu.uci.ics.sourcerer.util.io.arguments.IOFileArgumentFactory;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class ImportUsageGenerator {
  public static final IOFileArgumentFactory IMPORT_USAGE_LISTING = new IOFileArgumentFactory("import-usage-listing", "import-usage-listing.txt", "List of all the import statements.");
  
  public static void generateImportUsageListing() {
    new InlineDatabaseAccessor() {
      @Override
      public void action() {
        try {
          FileUtils.writeLineFile(joinQueries.getImportFqns(), IMPORT_USAGE_LISTING);
        } catch (IOException e) {
          logger.log(Level.SEVERE, "Error writing file.", e);
        }
      }
    }.execute();
  }
}
