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
package edu.uci.ics.sourcerer.tools.java.repo.importers;

import static edu.uci.ics.sourcerer.util.io.logging.Logging.logger;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
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
//  public static void importMavenToRepository() {
//    ModifiableJavaRepository repo = JavaRepositoryFactory.INSTANCE.loadModifiableJavaRepository(JavaRepositoryFactory.OUTPUT_REPO);
//
//    SAXParser parser = null;
//    try {
//      SAXParserFactory fact = SAXParserFactory.newInstance();
//      parser = fact.newSAXParser();
//    } catch (ParserConfigurationException | SAXException e) {
//      logger.log(Level.SEVERE, "Unable to create sax parser.", e);
//    }
//    
//    class Handler extends DefaultHandler {
//      private boolean inGroupID;
//      private boolean inArtifactID;
//      public String groupID;
//      public String artifactID;
//
//      @Override
//      public void startElement(String uri, String localName, String qName, Attributes attributes) {
//        switch (qName) {
//          case "groupId": inGroupID = true;
//          case "artifactId": inArtifactID = true;
//        }
//      }
//
//      @Override
//      public void characters(char[] ch, int start, int length) {
//        if (inGroupID) {
//          groupID = new String(ch, start, length);
//          inGroupID = false;
//        } else if (inArtifactID) {
//          artifactID = new String(ch, start, length);
//          inArtifactID = false;
//        }
//      }
//    };
//    Handler handler = new Handler();
//    
//    File root = Arguments.INPUT.getValue();
//    Deque<File> stack = new LinkedList<>();
//    stack.add(root);
//    
//    mainLoop:
//    while (!stack.isEmpty()) {
//      File next = stack.pop();
//      if (next.isDirectory()) {
//        // Look for maven-metadata.xml
//        for (File child : next.listFiles()) {
//          if (child.getName().equals("maven-metadata.xml")) {
//            try {
//              parser.parse(child, handler);
//            } catch (SAXException | IOException e) {
//              logger.log(Level.SEVERE, "Error reading maven metadata.", e);
//            }
//            for (File version : next.listFiles()) {
//              if (version.isDirectory()) {
//                String jarSuffix = version.getName() + ".jar";
//                String sourceSuffix = version.getName() + "-sources.jar";
//                File jar = null;
//                File source = null;
//                for (File file : version.listFiles()) {
//                  if (file.getName().endsWith(jarSuffix)) {
//                    if (jar == null) {
//                      jar = file;
//                    } else {
//                      logger.info("Multiple jar files for " + version.getAbsolutePath());
//                    }
//                  } else if (file.getName().endsWith(sourceSuffix)) {
//                    if (source == null) {
//                      source = file;
//                    } else {
//                      logger.info("Multiple source files for " + version.getAbsolutePath());
//                    }
//                  }
//                }
//                if (jar == null) {
//                  logger.info("Unable to find jar for " + version.getAbsolutePath());
//                } else {
//                  repo.addMavenJarFile(jar, source, handler.groupID, handler.artifactID, version.getName());
//                }
//              }
//            }
//            continue mainLoop;
//          }
//        }
//        for (File child : next.listFiles()) {
//          stack.add(child);
//        }
//      }
//    }
//  }
  
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
      private boolean inVersioning;
      private boolean inVersions;
      private boolean inVersion;
      public String groupID;
      public String artifactID;
      public Collection<String> versions = new LinkedList<>();

      @Override
      public void startElement(String uri, String localName, String qName, Attributes attributes) {
        switch (qName) {
          case "groupId": inGroupID = true; break;
          case "artifactId": inArtifactID = true; break;
          case "version": inVersion = inVersions; break;
          case "versioning": inVersioning = true; break;
          case "versions": inVersions = inVersioning; break;
        }
      }
      
      @Override
      public void endElement(String uri, String localName, String qName) throws SAXException {
        switch (qName) {
          case "versioning": inVersioning = false; break;
          case "versions": inVersions = false; break;
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
        } else if (inVersion) {
          versions.add(new String(ch, start, length));
          inVersion = false;
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
            // Verify the artifact info matches properly
            if (!handler.versions.isEmpty()) {
              String testGroup = next.getParent().substring(root.getPath().length()).replace(File.separatorChar, '.');
              if (!testGroup.isEmpty()) {
                testGroup = testGroup.substring(1);
              }
              if (!testGroup.equals(handler.groupID)) {
                logger.info("Group mismatch for: " + next.getPath() + " " + testGroup);
                handler.groupID = testGroup;
              }
              if (!next.getName().equals(handler.artifactID)) {
                logger.info("Artifact mismatch for: " + next.getPath());
                handler.artifactID = next.getName();
              }
            }
            // Look up the all the version
            for (String v : handler.versions) {
              File version = new File(next, v);
              if (version.isDirectory()) {
                File jar = new File(version, handler.artifactID + "-" + version.getName() + ".jar");
                File source = new File(version, handler.artifactID + "-" + version.getName() + "-sources.jar");
                if (!jar.exists()) {
                  jar = null;
                }
                if (!source.exists()) {
                  source = null;
                }
                if (jar == null || source == null) {
                  String jarSuffix = version.getName() + ".jar";
                  String sourceSuffix = version.getName() + "-sources.jar";
                  for (File file : version.listFiles()) {
                    if (file.getName().endsWith(jarSuffix)) {
                      if (jar == null) {
                        jar = file;
                      } else if (!jar.equals(file)) {
                        logger.info("Multiple jar files for " + version.getAbsolutePath());
                      }
                    } else if (file.getName().endsWith(sourceSuffix)) {
                      if (source == null) {
                        source = file;
                      } else if (!source.equals(file)) {
                        logger.info("Multiple source files for " + version.getAbsolutePath());
                      }
                    }
                  }
                }
                if (jar == null) {
//                  logger.info("Unable to find jar for " + version.getAbsolutePath());
                } else {
                  repo.addMavenJarFile(jar, source, handler.groupID, handler.artifactID, version.getName());
                }
              }
            }
            if (!handler.versions.isEmpty()) {
              handler.versions.clear();
              continue mainLoop;
            }
          }
        }
        for (File child : next.listFiles()) {
          if (!child.getName().startsWith(".")) {
            stack.add(child);
          }
        }
      }
    }
  }
  
  public static void importLatestMavenToRepository() {
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
      private boolean inVersion;
      public String groupID;
      public String artifactID;
      public String latestVersion;

      @Override
      public void startElement(String uri, String localName, String qName, Attributes attributes) {
        switch (qName) {
          case "groupId": inGroupID = true; break;
          case "artifactId": inArtifactID = true; break;
          case "version": inVersion = true; break;
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
        } else if (inVersion) {
          latestVersion = new String(ch, start, length);
          inVersion = false;
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
            // Look up the latest version
            File version = new File(next, handler.latestVersion);
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
