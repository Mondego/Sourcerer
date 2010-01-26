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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.uci.ics.sourcerer.model.Problem;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class ProblemEX implements ModelEX {
  private Problem type;
  private String relativePath;
  private String errorCode;
  private String message;
  
  protected ProblemEX(Problem type, String relativePath, String errorCode, String message) {
    this.type = type;
    this.relativePath = relativePath;
    this.errorCode = errorCode;
    this.message = message;
  }

  public Problem getType() {
    return type;
  }

  public String getRelativePath() {
    return relativePath;
  }

  public String getErrorCode() {
    return errorCode;
  }

  public String getMessage() {
    return message;
  }
  
  public String toString() {
    return type + " " + relativePath + " " + errorCode + " " + message; 
  }
  
  // ---- PARSER ----
  private static ModelExParser<ProblemEX> parser = new ModelExParser<ProblemEX>() {
    private Pattern pattern = Pattern.compile("([^\\s]*)\\s([^\\s]*)\\s([^\\s]*)\\s(.*)");
    @Override
    public ProblemEX parseLine(String line) {
      Matcher matcher = pattern.matcher(line);
      if (matcher.matches()) {
        return new ProblemEX(Problem.valueOf(matcher.group(1)), matcher.group(3), matcher.group(2), matcher.group(4));
      } else {
        logger.log(Level.SEVERE, "Unable to parse problem: " + line);
        return null;
      }
    }
  };
  
  public static ModelExParser<ProblemEX> getParser() {
    return parser;
  }
  
  public static String getLine(Problem type, int errorCode, String message, String filename) {
    return type.name() + " " + errorCode + " " + filename + " " + message;
  }
}
