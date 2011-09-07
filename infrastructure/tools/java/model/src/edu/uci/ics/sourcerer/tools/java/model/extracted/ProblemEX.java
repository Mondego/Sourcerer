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

import edu.uci.ics.sourcerer.tools.java.model.types.Problem;
import edu.uci.ics.sourcerer.util.io.SimpleSerializable;
import edu.uci.ics.sourcerer.util.io.arguments.Argument;
import edu.uci.ics.sourcerer.util.io.arguments.StringArgument;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public final class ProblemEX implements SimpleSerializable {
  public static final Argument<String> PROBLEM_FILE = new StringArgument("problem-file", "problems.txt", "Filename for the extracted problems.").permit();
  
  private Problem type;
  private String path;
  private Integer errorCode;
  private String message;
  
  public ProblemEX() {}
  
  public ProblemEX(Problem type, String path, Integer errorCode, String message) {
    this.type = type;
    this.path = path;
    this.errorCode = errorCode;
    this.message = message;
  }
  
  public ProblemEX update(Problem type, String path, Integer errorCode, String message) {
    this.type = type;
    this.path = path;
    this.errorCode = errorCode;
    this.message = message;
    return this;
  }

  public Problem getType() {
    return type;
  }

  public String getPath() {
    return path;
  }

  public Integer getErrorCode() {
    return errorCode;
  }

  public String getMessage() {
    return message;
  }
  
  public String toString() {
    return type + " " + path + " " + errorCode + " " + message; 
  }
}
