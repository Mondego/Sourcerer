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

import static edu.uci.ics.sourcerer.util.io.arguments.Arguments.OUTPUT;
import static edu.uci.ics.sourcerer.util.io.logging.Logging.RESUME;
import static edu.uci.ics.sourcerer.util.io.logging.Logging.logger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Deque;
import java.util.Set;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.uci.ics.sourcerer.util.Helper;
import edu.uci.ics.sourcerer.util.io.arguments.Argument;
import edu.uci.ics.sourcerer.util.io.arguments.StringArgument;
import edu.uci.ics.sourcerer.util.io.logging.Logging;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class MavenCrawler {
  public static final Argument<String> MAVEN_URL = new StringArgument("maven-url", "URL of the maven repository");
  public static final Argument<String> LINKS_FILE = new StringArgument("links-file", "links.txt", "File containing links crawled from maven.");
  
  public static void getDownloadLinks() {
    Pattern linkPattern = Pattern.compile("<a\\shref=\"(.*?)\">");
    
    Set<String> completed = Logging.initializeResumeLogger();
    
    File outputDir = OUTPUT.getValue();
    try {
      Deque<String> links = Helper.newLinkedList();
      
      File linksFile = new File(outputDir, LINKS_FILE.getValue());
      if (linksFile.exists()) {
        BufferedReader br = new BufferedReader(new FileReader(linksFile));
        for (String line = br.readLine(); line != null; line = br.readLine()) {
          if (!completed.contains(line)) {
            links.add(line);
          }
        }
        br.close();
      }
      if (links.isEmpty()) {
        links.add("");
      }
      
      String baseUrl = MAVEN_URL.getValue();
      if (baseUrl.endsWith("/")) {
        baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
      }
      
      BufferedWriter progress = new BufferedWriter(new FileWriter(linksFile, true));
      
      while (!links.isEmpty()) {
        String link = links.pop();
        if (!completed.contains(link)) {
          if (link.endsWith("/")) {
            try {
              logger.info("Getting " + link);
              URL url = new URL(baseUrl + link);
              BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
              for (String line = br.readLine(); line != null; line = br.readLine()) {
                Matcher matcher = linkPattern.matcher(line);
                if (matcher.find()) {
                  String part = matcher.group(1);
                  if (!part.startsWith(".")) {
                    String newLink = link + part;
                    logger.info("Adding " + newLink);
                    links.push(newLink);
                    progress.write(newLink + "\n");
                    progress.flush();
                  }
                }
              }
              br.close();
              logger.log(RESUME, link);
              Thread.sleep(1000);
            } catch (Exception e) {
              logger.log(Level.SEVERE, "Error getting new links", e);
            }
          }
        }
      }
      progress.close();
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Error writing links", e);
    }
    
    logger.info("Done!");
  }
}
