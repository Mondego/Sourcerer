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

import edu.uci.ics.sourcerer.clusterer.db.ClustererDatabaseAccessor;
import edu.uci.ics.sourcerer.model.db.SlightlyLessLimitedEntityDB;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class FqnStats {
  private ClustererDatabaseAccessor db;
  private FqnTree fqns;
  
  public FqnStats(ClustererDatabaseAccessor db) {
    this.db = db;
  }
  
  public void gatherFqnStats() {
    fqns = new FqnTree();
    
    logger.info("Reading entities from the database...");
    int count = 0;
    int reportCount = 1;
    for (SlightlyLessLimitedEntityDB entity : db.getEntityFqns()) {
      fqns.addFqn(entity.getFqn(), entity.getProjectID());
      if (++count % reportCount == 0) {
        if (reportCount < 100000) {
          reportCount *= 10;
        }
        logger.info("  " + count + " entities processed");
      }
    }
    
    logger.info("Writing FQN tree to disk");
    fqns.writeToDisk();
    
    logger.info("Done!");
  }
  
  public void loadFqnStats() {
    fqns = new FqnTree();
    fqns.readFromDisk();
  }
}
