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
package edu.uci.ics.sourcerer.model.extracted;

import static edu.uci.ics.sourcerer.util.io.Logging.logger;

import java.util.logging.Level;

import edu.uci.ics.sourcerer.model.File;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class FileEX implements ModelEX {
  private File type;
  private String name;
  private String path;
  
  private FileEX(File type, String name, String path) {
    this.type = type;
    this.name = name;
    this.path = path;
  }

  public File getType() {
    return type;
  }
  
  public String getName() {
    return name;
  }
  
  public String getPath() {
    if (type == File.JAR) {
      throw new IllegalStateException("Cannot get the path for a jar");
    } else {
      return path;
    }
  }
  
  public String getHash() {
    if (type == File.JAR) {
      return path;
    } else {
      throw new IllegalStateException("Cannot get the hash for a non-jar");
    }
  }
  
  // ---- PARSER ----
  private static ModelExParser<FileEX> parser = new ModelExParser<FileEX>() {
    @Override
    public FileEX parseLine(String line) {
      String[] parts = line.split(" ");
      File type = File.valueOf(parts[0]);
      if (parts.length == 3 && type != null) {
        return new FileEX(type, parts[1], parts[2]);
      } else {
        logger.log(Level.SEVERE, "Unable to parse file: " + line);
        return null;
      }
    }
  };
  
  public static ModelExParser<FileEX> getParser() {
    return parser;
  }
  
  public static String getSourceLine(String name, String path) {
    return File.SOURCE + " " + name + " " + path;
  }
  
  public static String getClassLine(String name, String path) {
    return File.CLASS + " " + name + " " + path;
  }
  
  public static String getJarLine(String name, String hash) {
    return File.JAR + " " + name + " " + hash;
  }
}