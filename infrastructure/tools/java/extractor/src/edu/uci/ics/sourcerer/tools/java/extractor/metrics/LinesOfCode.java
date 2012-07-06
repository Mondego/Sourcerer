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
package edu.uci.ics.sourcerer.tools.java.extractor.metrics;

import static edu.uci.ics.sourcerer.util.io.logging.Logging.logger;
import edu.uci.ics.sourcerer.tools.java.model.types.Metric;
import edu.uci.ics.sourcerer.tools.java.model.types.Metrics;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class LinesOfCode {
  private LinesOfCode() {}
  
  private static enum State {
    START,
    IN_LINE_COMMENT,
    IN_BLOCK_COMMENT,
    IN_STRING_LITERAL,
    IN_CHARACTER_LITERAL,
  }

  public static void computeLinesOfCode(String source, Metrics metrics) {
    if (source != null) {
      int whitespaceLineCount = 0;
      int commentLineCount = 0;
      int codeLineCount = 0;

      boolean hasContent = false;
      boolean wasComment = false;
      State state = State.START;
      int i = 0;
      for (boolean cond = true; cond;) {
        int c;
        if (i < source.length()) {
          c = source.codePointAt(i);
        } else {
          c = '\n';
          cond = false;
        }
        
        if (state == State.START) {
          if (c == '/') {
            int j = i + Character.charCount(c);
            if (j < source.length()) {
              int n = source.codePointAt(j);
              if (n == '/') {
                state = State.IN_LINE_COMMENT;
              } else if (n == '*') { 
                state = State.IN_BLOCK_COMMENT;
              }
            }
          } else if (c == '"') {
            state = State.IN_STRING_LITERAL;
          } else if (c == '\'') {
            state = State.IN_CHARACTER_LITERAL;
          } else if (c == '\n') {
            if (wasComment) {
              if (hasContent) {
                codeLineCount++;
              } else {
                commentLineCount++;
              }
            } else {
              if (hasContent) {
                codeLineCount++;
              } else {
                whitespaceLineCount++;
              }
            }
            hasContent = false;
            wasComment = false;
          } else if (!Character.isWhitespace(c)) {
            hasContent = true;
          }
        } else if (state == State.IN_LINE_COMMENT) {
          if (c == '\n') {
            state = State.START;
            if (hasContent) {
              codeLineCount++;
              hasContent = false;
            } else {
              commentLineCount++;
            }
            wasComment = false;
          }
        } else if (state == State.IN_BLOCK_COMMENT) {
          if (c == '\n') {
            if (hasContent) {
              codeLineCount++;
              hasContent = false;
            } else {
              commentLineCount++;
            }
          } else if (c == '/') {
            if (source.codePointBefore(i) == '*') {
              state = State.START;
              wasComment = true;
            }
          }
        } else if (state == State.IN_STRING_LITERAL) {
          if (c == '"' && source.codePointBefore(i) != '\\') {
            state = State.START;
          }
        } else if (state == State.IN_CHARACTER_LITERAL) {
          if (c == '\'' && source.codePointBefore(i) != '\\') {
            state = State.START;
          }
        } else {
          logger.severe("Invalid state! " + state);
        }
        i += Character.charCount(c);
      }
      
      metrics.addMetric(Metric.LINES_OF_CODE, whitespaceLineCount + commentLineCount + codeLineCount);
      metrics.addMetric(Metric.NON_WHITESPACE_LOC, commentLineCount + codeLineCount);
      metrics.addMetric(Metric.NON_COMMENTS_LOC, codeLineCount);
    }
  }
}