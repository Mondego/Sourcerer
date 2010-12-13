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
public class ImportEX implements ModelEX {
  private String imported;
  private boolean isStatic;
  private boolean onDemand;
  private String path;
  private Integer offset;
  private Integer length;

  private ImportEX(String imported, boolean isStatic, boolean onDemand, String file, Integer offset, Integer length) {
    this.imported = imported;
    this.isStatic = isStatic;
    this.onDemand = onDemand;
    this.path = file;
    this.offset = offset;
    this.length = length;
  }
  
  public String getImported() {
    return imported;
  }

  public boolean isStatic() {
    return isStatic;
  }
  
  public boolean isOnDemand() {
    return onDemand;
  }
  
  public String getPath() {
    return path;
  }
  
  public Integer getOffset() {
    return offset;
  }
  
  public Integer getLength() {
    return length;
  }
  
  public String toString() {
    return imported + " " + path;
  }
  
  // ---- PARSER ----
  private static ModelExParser<ImportEX> parser = new ModelExParser<ImportEX>() {
    @Override
    public ImportEX parseLine(String line) {
      String[] parts = line.split(" ");
      if (parts.length == 6) {
        try {
          return new ImportEX(parts[0], parts[1].equals("STATIC"), parts[2].equals("ON_DEMAND"), parts[3], Integer.valueOf(parts[4]), Integer.valueOf(parts[5]));
        } catch (IllegalArgumentException e) {
          logger.log(Level.SEVERE, "Unable to parse import: " + line);
          return null;
        }
      } else {
        logger.log(Level.SEVERE, "Unable to parse import: " + line);
        return null;
      }
    }
  };
  
  public static ModelExParser<ImportEX> getParser() {
    return parser;
  }
  
  public static String getLine(String imported, boolean isStatic, boolean onDemand, String compilationUnitPath, int startPos, int length) {
    return imported + " " + (isStatic ? "STATIC" : "NULL") + " " + (onDemand ? "ON_DEMAND" : "NULL") + " " + compilationUnitPath + " " + startPos + " " + length;
  }
}
