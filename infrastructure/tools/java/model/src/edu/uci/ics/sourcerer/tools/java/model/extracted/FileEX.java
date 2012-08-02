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
package edu.uci.ics.sourcerer.tools.java.model.extracted;

import edu.uci.ics.sourcerer.tools.java.model.types.File;
import edu.uci.ics.sourcerer.tools.java.model.types.Metrics;
import edu.uci.ics.sourcerer.util.io.SimpleSerializable;
import edu.uci.ics.sourcerer.util.io.arguments.Argument;
import edu.uci.ics.sourcerer.util.io.arguments.StringArgument;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public final class FileEX implements SimpleSerializable {
  public static final Argument<String> FILE_FILE = new StringArgument("file-file", "files.txt", "Filename for the extracted files.").permit();
  
  private File type;
  private String name;
  private Metrics metrics;
  private String hashPath;
  
  public FileEX() {}

  public FileEX(File type, String name, Metrics metrics, String hashPath) {
    this.type = type;
    this.name = name;
    this.metrics = metrics;
    this.hashPath = hashPath;
  }
  
  public FileEX update(File type, String name, Metrics metrics, String hashPath) {
    this.type = type;
    this.name = name;
    this.metrics = metrics;
    this.hashPath = hashPath;
    return this;
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
      return hashPath;
    }
  }
  
  public String getHash() {
    if (type == File.JAR) {
      return hashPath;
    } else {
      throw new IllegalStateException("Cannot get the hash for a non-jar file");
    }
  }
  
  public Metrics getMetrics() {
    return metrics;
  }
  
  @Override
  public String toString() {
    return name + " " + hashPath;
  }
}