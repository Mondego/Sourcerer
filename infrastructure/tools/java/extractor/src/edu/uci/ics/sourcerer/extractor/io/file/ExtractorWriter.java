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
package edu.uci.ics.sourcerer.extractor.io.file;

import static edu.uci.ics.sourcerer.util.io.Logging.logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;

import edu.uci.ics.sourcerer.extractor.io.IExtractorWriter;
import edu.uci.ics.sourcerer.repo.base.Repository;
import edu.uci.ics.sourcerer.util.Helper;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public abstract class ExtractorWriter implements IExtractorWriter {
  private static Map<File, BufferedWriter> writerMap = Helper.newHashMap();
  private BufferedWriter writer;
  private Repository input;
  
  protected ExtractorWriter(File output, Repository input) {
    this(output, input, false);
  }
  
  protected ExtractorWriter(File output, Repository input, boolean append) {
    this.input = input;
    try {
      if (writerMap.containsKey(output)) {
        writer = writerMap.get(output);
      } else {
        writer = new BufferedWriter(new FileWriter(output, append));
        writerMap.put(output, writer);
      }
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Error opening file", e);
    }
  }
  
  protected String convertToRelativePath(String path) {
    if (input == null) {
      return path;
    } else {
      return input.convertToRelativePath(path);
    }
  }
  
  public void close() {
    try {
      writer.close();
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Error closing writer", e);
    }
  }
  
  protected void write(String line) {
    try {
      writer.write(line);
      writer.write("\n");
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Error writing line to file", e);
    }
  }
}
