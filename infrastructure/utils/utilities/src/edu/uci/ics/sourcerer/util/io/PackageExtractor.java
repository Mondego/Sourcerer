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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class PackageExtractor {
  private enum State {
    START,
    SLASH,
    IN_LINE_COMMENT,
    IN_BLOCK_COMMENT,
    IN_BLOCK_COMMENT_STAR,
    IN_PACKAGE;
  };
  
  public static String extractPackage(File file) {
    try (BufferedReader br = IOUtils.createBufferedReader(file)) {
      StringBuilder word = new StringBuilder();
      State state = State.START;
      for (int next = br.read(); next != -1; next = br.read()) {
        if (state == State.START) {
          if (next == '/') {
            state = State.SLASH;
          } else if (next == '{') {
            return "";
          } else if (Character.isLetterOrDigit(next)) {
            word.append((char) next);
          } else if (word.length() > 0) {
            if (word.toString().equals("package")) {
              state = State.IN_PACKAGE;
            }
            word.setLength(0);
          }
        } else if (state == State.SLASH) {
          if (next == '/') {
            state = State.IN_LINE_COMMENT;
          } else if (next == '*') {
            state = State.IN_BLOCK_COMMENT;
          } else {
            state = State.START;
          }
        } else if (state == State.IN_LINE_COMMENT) {
          if (next == '\r' || next == '\n') {
            state = State.START;
          }
        } else if (state == State.IN_BLOCK_COMMENT) {
          if (next == '*') {
            state = State.IN_BLOCK_COMMENT_STAR;
          }
        } else if (state == State.IN_BLOCK_COMMENT_STAR) {
          if (next == '/') {
            state = State.START;
          } else if (next != '*') {
            state = State.IN_BLOCK_COMMENT;
          }
        } else if (state == State.IN_PACKAGE) {
          if (next == ';') {
            return word.toString();
          } else if (Character.isJavaIdentifierPart(next) || next == '.') {
            word.append((char) next);
          } else if (!Character.isWhitespace(next)) {
            return null;
          }
        }
      }
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Unable to extract package for file: " + file.getPath(), e);
    }
    return null;
  }
}
