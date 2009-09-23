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

import static edu.uci.ics.sourcerer.util.io.Logging.RESUME;
import static edu.uci.ics.sourcerer.util.io.Logging.logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URL;
import java.util.Set;
import java.util.logging.Level;

import edu.uci.ics.sourcerer.repo.base.Repository;
import edu.uci.ics.sourcerer.util.io.FileUtils;
import edu.uci.ics.sourcerer.util.io.Logging;
import edu.uci.ics.sourcerer.util.io.Property;
import edu.uci.ics.sourcerer.util.io.PropertyManager;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class MavenDownloader {
  public static void downloadLinks() {
    try {
      Set<String> resume = Logging.initializeResumeLogger();
      PropertyManager properties = PropertyManager.getProperties();
      File input = new File(properties.getValue(Property.INPUT), properties.getValue(Property.LINKS_FILE));
     
      Repository repo = Repository.getUninitializedRepository();
      File outputDir = repo.getJarsDir();
      String baseUrl = properties.getValue(Property.MAVEN_URL);
      if (baseUrl.endsWith("/")) {
        baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
      }
      outputDir.mkdirs();
      BufferedReader br = new BufferedReader(new FileReader(input));
      for (String line = br.readLine(); line != null; line = br.readLine()) {
        if (line.endsWith(".jar") && !resume.contains(line)) {
          URL url = new URL(line);
          if (line.startsWith(baseUrl)) {
            String path = line.substring(baseUrl.length());
            File file = new File(outputDir + path);
            file.getParentFile().mkdirs();
            logger.info("Writing " + path + " to file");
            if (FileUtils.writeStreamToFile(url.openStream(), file)) {
              logger.log(RESUME, line);
            }
            Thread.sleep(10000);
          } else {
            logger.log(Level.SEVERE, "Unexpected line: " + line);
          }
        }
      }
    } catch (Exception e) {
      logger.log(Level.SEVERE, "Unable to download", e);
    }
  }
}
