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

import edu.uci.ics.sourcerer.model.Entity;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class EntityExParser implements ModelExParser<EntityEX> {
  private EntityExParser() {}
  
  public static EntityExParser getParser() {
    return new EntityExParser();
  }
  
  public static String getLine(Entity type, String fqn, int modifiers, String compilationUnitPath, int startPos, int length) {
    return type.name() + " " + fqn + " " + modifiers + " " + compilationUnitPath + " " + startPos + " " + length;
  }
  
  @Override
  public EntityEX parseLine(String line) {
    String[] parts = line.split(" ");
    
    try {
      return new EntityEX(Entity.valueOf(parts[0]), parts[1], parts[2], parts[3], parts[4], parts[5]);
    } catch (ArrayIndexOutOfBoundsException e) {
      logger.log(Level.SEVERE, "Unable to parse line: " + line);
      return null;
    } catch (IllegalArgumentException e) {
      logger.log(Level.SEVERE, "Unable to parse line: " + line);
      return null;
    }
  }
}
