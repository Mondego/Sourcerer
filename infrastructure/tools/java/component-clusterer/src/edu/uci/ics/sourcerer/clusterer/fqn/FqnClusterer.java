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
package edu.uci.ics.sourcerer.clusterer.fqn;

import static edu.uci.ics.sourcerer.util.io.Logging.logger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;

import edu.uci.ics.sourcerer.clusterer.stats.FileCluster;
import edu.uci.ics.sourcerer.clusterer.stats.Matching;
import edu.uci.ics.sourcerer.db.queries.DatabaseAccessor;
import edu.uci.ics.sourcerer.db.util.DatabaseConnection;
import edu.uci.ics.sourcerer.model.db.FileFqn;
import edu.uci.ics.sourcerer.util.Helper;
import edu.uci.ics.sourcerer.util.io.FileUtils;
import edu.uci.ics.sourcerer.util.io.Properties;
import edu.uci.ics.sourcerer.util.io.Property;
import edu.uci.ics.sourcerer.util.io.properties.StringProperty;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class FqnClusterer {
  public static final Property<String> FQN_FILE_LISTING = new StringProperty("fqn-file-listing", "fqn-file-listing.txt", "List of all the files (and their FQNs) in the repository.");
  
  private static class FqnDatabaseAccessor extends DatabaseAccessor {
    public FqnDatabaseAccessor(DatabaseConnection connection) {
      super(connection);
    }
    
    
    public Iterable<FileFqn> getFileFqns() {
      return joinQueries.getFileFqns();
    }
  }
  
  public static void generateFileListing() {
    DatabaseConnection conn = new DatabaseConnection();
    if (conn.open()) {
      FqnDatabaseAccessor accessor = null;
      BufferedWriter bw = null;
      try {
        accessor = new FqnDatabaseAccessor(conn);
        bw = new BufferedWriter(new FileWriter(new File(Properties.OUTPUT.getValue(), FQN_FILE_LISTING.getValue())));
        logger.info("Issuing database query...");
        int count = 0;
        for (FileFqn file : accessor.getFileFqns()) {
          if (++count % 10000 == 0) {
            logger.info(count + " rows written");
          }
          bw.write(file.getProject() + " " + file.getProjectID() + " " + file.getPath() + " " + file.getFileID() + " " + file.getFqn() + " " + file.getEntityID() + "\n");
        }
        logger.info("Done!");
      } catch (IOException e) {
        logger.log(Level.SEVERE, "Error writing to file", e);
      } finally {
        FileUtils.close(accessor);
        FileUtils.close(bw);
      }
    }
  }
  
  public static Matching getMatching() {
    logger.info("Processing fqn file listing...");
    
    BufferedReader br = null;
    try {
      br = new BufferedReader(new FileReader(new File(Properties.INPUT.getValue(), FQN_FILE_LISTING.getValue())));
      
      Matching matching = new Matching();
      
      Map<String, FileCluster> files = Helper.newHashMap();
      for (String line = br.readLine(); line != null; line = br.readLine()) {
        String[] parts = line.split(" ");
        if (parts.length == 6) {
          FileCluster cluster = files.get(parts[4]);
          if (cluster == null) {
            cluster = new FileCluster();
            files.put(parts[4], cluster);
            matching.addCluster(cluster);
          }
          cluster.addFile(parts[0], parts[2]);
        } else {
          logger.log(Level.SEVERE, "Invalid line: " + line);
        }
      }
      
      return matching;
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Error in reading file listing.", e);
      return null;
    } finally {
      FileUtils.close(br);
    }
  }
}
