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

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class ImportExParser implements ModelExParser<ImportEX> {
  private ImportExParser() {}
  
  public static ImportExParser getParser() {
    return new ImportExParser();
  }
  
  public static String getLine(String imported, boolean isStatic, boolean onDemand, String compilationUnitPath, int startPos, int length) {
    return imported + " " + (isStatic ? "STATIC" : "NULL") + " " + (onDemand ? "ON_DEMAND" : "NULL") + " " + compilationUnitPath + " " + startPos + " " + length;
  }
  
  @Override
  public ImportEX parseLine(String line) {
    String[] parts = line.split(" ");
    if (parts.length == 5) {
      return new ImportEX(parts[0], parts[2], parts[3].equals("STATIC"), parts[4].equals("ON_DEMAND"));
    } else {
      logger.log(Level.SEVERE, "Unable to parse import: " + line);
      return null;
    }
  }
}
