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

import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.logging.Level;

import edu.uci.ics.sourcerer.model.File;
import edu.uci.ics.sourcerer.model.metrics.Metrics;
import edu.uci.ics.sourcerer.util.io.LineBuilder;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class FileEX implements ModelEX {
  private final File type;
  private final String name;
  private final Metrics metrics;
  private final String path;
  
  private FileEX(File type, String name, Metrics metrics, String path) {
    this.type = type;
    this.name = name;
    this.path = path;
    this.metrics = metrics;
  }

  public File getType() {
    return type;
  }
  
  public String getName() {
    return name;
  }
  
  public String getPath() {
    if (type == File.JAR) {
      throw new IllegalStateException("Cannot get the path for a jar file");
    } else {
      return path;
    }
  }
  
  public String getHash() {
    if (type == File.JAR) {
      return path;
    } else {
      throw new IllegalStateException("Cannot get the hash for a non-jar file");
    }
  }
  
  public Metrics getMetrics() {
    return metrics;
  }
  
  // ---- PARSER ----
  private static ModelExParser<FileEX> parser = new ModelExParser<FileEX>() {
    @Override
    public FileEX parseLine(String line) {
      Scanner scanner = LineBuilder.getScanner(line);

      try {
        File type = File.valueOf(scanner.next());
        String name = scanner.next();
        Metrics metrics = Metrics.parse(scanner);
        String path = scanner.next();
        if (scanner.hasNext()) {
          logger.log(Level.WARNING, "Line has extra entries: " + line);
        }
        return new FileEX(type, name, metrics, path);
      } catch (NoSuchElementException e) {
        logger.log(Level.SEVERE, "Unable to parse file line: " + line);
        return null;
      }
    }
  };
  
  public static ModelExParser<FileEX> getParser() {
    return parser;
  }
  
  public static String getSourceLine(String name, Metrics metrics, String path) {
    LineBuilder builder = new LineBuilder();
    
    builder.append(File.SOURCE.name());
    builder.append(name);
    builder.append(metrics);
    builder.append(path);
    
    return builder.toString();
  }
  
  public static String getClassLine(String name, Metrics metrics, String path) {
    LineBuilder builder = new LineBuilder();
    
    builder.append(File.CLASS.name());
    builder.append(name);
    builder.append(metrics);
    builder.append(path);
    
    return builder.toString();
  }
  
  public static String getJarLine(String name, Metrics metrics, String hash) {
    LineBuilder builder = new LineBuilder();
    
    builder.append(File.JAR.name());
    builder.append(name);
    builder.append(metrics);
    builder.append(hash);
    
    return builder.toString();
  }
}