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
package edu.uci.ics.sourcerer.util.io;

import static edu.uci.ics.sourcerer.util.io.logging.Logging.logger;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.logging.Level;

import edu.uci.ics.sourcerer.util.Strings;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class LogFileWriter implements AutoCloseable {
  private final BufferedWriter writer;
  private String SPACES;
  private int indent;
  
  private LogFileWriter(BufferedWriter writer) {
    this.writer = writer;
    indent = 0;
    SPACES = Strings.create(' ', 10);
  }
  
  static LogFileWriter create(BufferedWriter writer) {
    return new LogFileWriter(writer);
  }
  
  static LogFileWriter createNull() {
    return new LogFileWriter(null) {
      @Override
      public void writeAndIndent(String string) {}

      @Override
      public void write(String string) {}

      @Override
      public void writeFragment(String string) {}

      @Override
      public void newLine() {}
      
      @Override
      public void indent() {}
      
      @Override
      public void unindent() {}
    };
  }
  
  public void indent() {
    indent += 2;
  }
  
  public void unindent() {
    if (indent >= 2) {
      indent -= 2;
    } else {
      throw new IllegalStateException("Cannot unindent indentation of: " + indent);
    }
  }
  
  private void writeIndent() throws IOException {
    if (indent > SPACES.length()) {
      SPACES = Strings.create(' ', 10 + Math.max(indent, SPACES.length()));
    }
    writer.write(SPACES.substring(0, indent));
  }
 
  public void writeAndIndent(String string) {
    write(string);
    indent();
  }
  
  public void write(String string) {
    try {
      writeIndent();
      writer.write(string);
      writer.newLine();
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Error writing log file", e);
    }
  }
  
  public void writeFragment(String string) {
    try {
      writer.write(string);
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Error writing log file", e);
    }
  }
  
  public void newLine() {
    try {
      writer.newLine();
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Error writing log file", e);
    }
  }
  
  @Override
  public void close() {
    IOUtils.close(writer);
  }
}
