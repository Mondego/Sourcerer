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
package edu.uci.ics.sourcerer.repo.maven;

import static edu.uci.ics.sourcerer.util.io.Logging.logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;

import edu.uci.ics.sourcerer.util.io.PropertyOld;
import edu.uci.ics.sourcerer.util.io.PropertyManager;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class MavenCrawlStats {
  public static void crawlStats() {
    try {
      PropertyManager properties = PropertyManager.getProperties();
      File dir = properties.getValueAsFile(PropertyOld.INPUT);
      File input = new File(dir, properties.getValue(PropertyOld.LINKS_FILE));
//      File output = new File(dir, "fixed-" + properties.getValue(PropertyOld.LINKS_FILE));
      String baseUrl = properties.getValue(PropertyOld.MAVEN_URL);
      if (baseUrl.endsWith("/")) {
        baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
      }
      BufferedReader br = new BufferedReader(new FileReader(input));
//      BufferedWriter bw = new BufferedWriter(new FileWriter(output));
      int count = 0, jarCount = 0;
      for (String line = br.readLine(); line != null; line = br.readLine()) {
        if (line.startsWith(baseUrl)) {
          count++;
//          bw.write(line.substring(baseUrl.length()) + "\n");
          if (line.endsWith(".jar")) {
            jarCount++;
          }
        } else {
          logger.severe("Unexpected line: " + line);
        }
      }
      br.close();
//      bw.close();
      
//      input.renameTo(new File(dir, input.getName() + "-old"));
//      output.renameTo(input);
//      logger.info("Converted " + count + " lines and found " + jarCount + " jars");
      logger.info("Found " + count + " lines and found " + jarCount + " jars");
//      logger.info("Done!");
    } catch (IOException e) {
//      logger.log(Level.SEVERE, "Unable to fix maven links", e);
      logger.log(Level.SEVERE, "Unable to get maven stats", e);
    }
  }
}
