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

import edu.uci.ics.sourcerer.model.LocalVariable;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class LocalVariableExJarParser implements ModelExParser<LocalVariableEX> {
  private LocalVariableExJarParser() {}
  
  public static LocalVariableExJarParser getParser() {
    return new LocalVariableExJarParser();
  }
  
  public static String getLineParam(String name, String type, String parent, int position) {
    return "PARAM " + name + " " + type + " " + parent + " " + position;
  }
  
  public LocalVariableEX parseLine(String line) {
    String[] parts = line.split(" ");
    
    try {
      LocalVariable type = LocalVariable.valueOf(parts[0]);
      if (type == LocalVariable.PARAM) {
        return LocalVariableEX.getJarParam(parts[1], parts[2], parts[3], parts[4]);
      } else {
        logger.log(Level.SEVERE, "Unable to parse line: " + line);
        return null;
      }
    } catch (Exception e) {
      logger.log(Level.SEVERE, "Unable to parse line: " + line, e);
      return null;
    }
  }
}
