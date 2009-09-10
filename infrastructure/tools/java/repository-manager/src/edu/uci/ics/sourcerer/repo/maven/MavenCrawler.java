// Sourcerer: an infrastructure for large-scale source code analysis.
// Copyright (C) by contributors. See CONTRIBUTORS.txt for full list.
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program. If not, see <http://www.gnu.org/licenses/>.
package edu.uci.ics.sourcerer.repo.maven;

import static edu.uci.ics.sourcerer.util.io.Logging.logger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Deque;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.uci.ics.sourcerer.util.Helper;
import edu.uci.ics.sourcerer.util.io.Property;
import edu.uci.ics.sourcerer.util.io.PropertyManager;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class MavenCrawler {
  public static void getDownloadLinks() {
    Pattern linkPattern = Pattern.compile("<a\\shref=\"(.*?)\">");
    PropertyManager properties = PropertyManager.getProperties();
    
    Deque<String> links = Helper.newLinkedList();
    links.add(properties.getValue(Property.INPUT));
    
    try {
      File outputFile = new File(properties.getValue(Property.OUTPUT), properties.getValue(Property.LINK_FILE));
      BufferedWriter out = new BufferedWriter(new FileWriter(outputFile));
      
      while (!links.isEmpty()) {
        String link = links.pop();
        if (link.endsWith("/")) {
          try {
            logger.info("Getting " + link);
            URL url = new URL(link);
            BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
            for (String line = br.readLine(); line != null; line = br.readLine()) {
              Matcher matcher = linkPattern.matcher(line);
              if (matcher.find()) {
                String part = matcher.group(1);
                if (!part.equals("../")) {
                  logger.info("Adding " + link + part);
                  links.push(link + part);
                }
              }
            }
            br.close();
            Thread.sleep(1000);
          } catch (Exception e) {
            logger.log(Level.SEVERE, "Error getting new links", e);
          }
        } else if (link.endsWith(".jar")) {
          out.write(link + "\n");
        }
      }
      out.close();
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Error writing links", e);
    }
  }
}
