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
public class LocalVariableExParser implements ModelExParser<LocalVariableEX> {
  private LocalVariableExParser() {}
  
  public static LocalVariableExParser getParser() {
    return new LocalVariableExParser();
  }
 
  public static String getLineParam(String name, int modifiers, String type, int typeStartPos, int typeLength, int position, String parent, String path, int startPos, int length) {
    return "PARAM " + name + " " + modifiers + " " + type + " " + typeStartPos + " " + typeLength + " " + parent + " " + position + " " + path + " " + startPos + " " + length;
  }
  
  public static String getLineLocal(String name, int modifiers, String type, int typeStartPos, int typeLength, String parent, String path, int startPos, int length) {
    return "LOCAL " + name + " " + modifiers + " " + type + " " + typeStartPos + " " + typeLength + " "+ parent + " " + path + " " + startPos + " " + length;
  }
  
  @Override
  public LocalVariableEX parseLine(String line) {
    String[] parts = line.split(" ");
    
    try {
      LocalVariable type = LocalVariable.valueOf(parts[0]);
      if (type == LocalVariable.PARAM) {
        return LocalVariableEX.getParam(parts[1], parts[2], parts[3], parts[4], parts[5], parts[6], parts[7], parts[8], parts[9], parts[10]);
      } else if (type == LocalVariable.LOCAL) {
        return LocalVariableEX.getLocal(parts[1], parts[2], parts[3], parts[4], parts[5], parts[6], parts[7], parts[8], parts[9]);
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
