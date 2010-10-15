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
package edu.uci.ics.sourcerer.repo.index;

import static edu.uci.ics.sourcerer.util.io.Logging.logger;

import java.util.logging.Level;

import edu.uci.ics.sourcerer.repo.IndexedJar;

/**
 * @author Joel Ossher (jossher@uci.edu)
 *
 */
public class abstract IndexedJar {
  private String hash;
  private String relativePath;
  private String name;
  
  private String group;
  private String version;
  private String artifact;
  private String source;
  
  
  private IndexedJar(Type type, String hash, String group, String version, String artifact, String relativePath, String name, String source) {
    this.type = type;
    this.hash = hash;
    this.group = group;
    this.version = version;
    this.artifact = artifact;
    this.sou
  }
  
  public static IndexedJarNew parseLine(String line) {
    String[] parts = line.split(" ");
    IndexedJarNew jar = null;
    if (parts.length == 4) {
      if (Type.valueOf(parts[1]) == Type.PROJECT) {
        jar = new IndexedJarNew(Type.PROJECT, parts[0], null, null, null, parts[2], parts[3], null);
      } else {
        logger.log(Level.SEVERE, "Invalid index line: " + line);
      }
    } else if (parts.length == 7) {
      if ("MAVEN".equals(parts[1])) {
        jar = new IndexedJar(parts[0], parts[2], parts[3], parts[4], mavenPath.getChild(parts[5]), parts[6]);
      } else {
        logger.log(Level.SEVERE, "Invalid index line: " + line);
      }
    } else if (parts.length == 8) {
      if ("MAVEN".equals(parts[1])) {
        jar = new IndexedJar(parts[0], parts[2], parts[3], parts[4], mavenPath.getChild(parts[5]), parts[6], parts[7]);
      } else {
        logger.log(Level.SEVERE, "Invalid index line: " + line);
      }
    } else {
      logger.log(Level.SEVERE, "Invalid index line: " + line);
    }
  }
}
