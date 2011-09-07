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
package edu.uci.ics.sourcerer.tools.java.repo.maven;

import static edu.uci.ics.sourcerer.util.io.Logging.logger;

import java.io.File;
import java.io.IOException;
import java.util.Deque;
import java.util.LinkedList;
import java.util.logging.Level;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import edu.uci.ics.sourcerer.tools.java.repo.model.JavaRepositoryFactory;
import edu.uci.ics.sourcerer.tools.java.repo.model.ModifiableJavaRepository;
import edu.uci.ics.sourcerer.util.io.arguments.Arguments;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class MavenImporter {
  public static void importMavenToRepository() {
    ModifiableJavaRepository repo = JavaRepositoryFactory.INSTANCE.loadModifiableJavaRepository(JavaRepositoryFactory.OUTPUT_REPO);

    SAXParser parser = null;
    try {
      SAXParserFactory fact = SAXParserFactory.newInstance();
      parser = fact.newSAXParser();
    } catch (ParserConfigurationException | SAXException e) {
      logger.log(Level.SEVERE, "Unable to create sax parser.", e);
    }
    
    class Handler extends DefaultHandler {
      private boolean inGroupID;
      private boolean inArtifactID;
      public String groupID;
      public String artifactID;

      @Override
      public void startElement(String uri, String localName, String qName, Attributes attributes) {
        switch (qName) {
          case "groupId": inGroupID = true;
          case "artifactId": inArtifactID = true;
        }
      }

      @Override
      public void characters(char[] ch, int start, int length) {
        if (inGroupID) {
          groupID = new String(ch, start, length);
          inGroupID = false;
        } else if (inArtifactID) {
          artifactID = new String(ch, start, length);
          inArtifactID = false;
        }
      }
    };
    Handler handler = new Handler();
    
    File root = Arguments.INPUT.getValue();
    Deque<File> stack = new LinkedList<>();
    stack.add(root);
    
    mainLoop:
    while (!stack.isEmpty()) {
      File next = stack.pop();
      if (next.isDirectory()) {
        // Look for maven-metadata.xml
        for (File child : next.listFiles()) {
          if (child.getName().equals("maven-metadata.xml")) {
            try {
              parser.parse(child, handler);
            } catch (SAXException | IOException e) {
              logger.log(Level.SEVERE, "Error reading maven metadata.", e);
            }
            for (File version : next.listFiles()) {
              if (version.isDirectory()) {
                String jarSuffix = version.getName() + ".jar";
                String sourceSuffix = version.getName() + "-sources.jar";
                File jar = null;
                File source = null;
                for (File file : version.listFiles()) {
                  if (file.getName().endsWith(jarSuffix)) {
                    if (jar == null) {
                      jar = file;
                    } else {
                      logger.info("Multiple jar files for " + version.getAbsolutePath());
                    }
                  } else if (file.getName().endsWith(sourceSuffix)) {
                    if (source == null) {
                      source = file;
                    } else {
                      logger.info("Multiple source files for " + version.getAbsolutePath());
                    }
                  }
                }
                if (jar == null) {
                  logger.info("Unable to find jar for " + version.getAbsolutePath());
                } else {
                  repo.addMavenJarFile(jar, source, handler.groupID, handler.artifactID, version.getName());
                }
              }
            }
            continue mainLoop;
          }
        }
        for (File child : next.listFiles()) {
          stack.add(child);
        }
      }
    }
  }
}
