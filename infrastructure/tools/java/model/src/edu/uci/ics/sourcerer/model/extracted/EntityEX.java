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

import java.util.Set;
import java.util.logging.Level;

import edu.uci.ics.sourcerer.model.Entity;
import edu.uci.ics.sourcerer.util.Helper;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public final class EntityEX implements ModelEX {
  private Entity type;
  private String fqn;
  private Integer mods;
  private String path;
  private Integer startPos;
  private Integer length;
  
  private EntityEX(Entity type, String fqn) {
    this(type, fqn, null, null, null, null);
  }
  
  private EntityEX(Entity type, String fqn, Integer mods, String path) {
    this(type, fqn, mods, path, null, null);
  }
  
  private EntityEX(Entity type, String fqn, Integer mods, String path, Integer offset, Integer length) {
    this.type = type;
    this.fqn = fqn;
    this.mods = mods;
    this.path = path;
    if (offset >= 0) {
      this.startPos = offset;
      this.length = length;
    }
  }
  
  public Entity getType() {
    return type;
  }

  public String getFqn() {
    return fqn;
  }

  public Integer getMods() {
    return mods;
  }

  public String getPath() {
    return path;
  }

  public Integer getStartPosition() {
    return startPos;
  }

  public Integer getLength() {
    return length;
  }
  
  public String toString() {
    return type.name() + " " + fqn;
  }
  
  // ---- PARSER ----
  public static ModelExParser<EntityEX> getParser() {
    return new ModelExParser<EntityEX>() {
      private Set<String> uniqueChecker = Helper.newHashSet();
      
      @Override
      public EntityEX parseLine(String line) {
        String[] parts = line.split(" ");
        
        try {
          if (parts.length == 2) {
            Entity type = Entity.valueOf(parts[0]);
            if (type == Entity.PACKAGE) {
              if (!uniqueChecker.contains(parts[1])) {
                uniqueChecker.add(parts[1]);
                return new EntityEX(type, parts[1]);
              } else {
                return null;
              }
            } else {
              logger.log(Level.SEVERE, "Unable to parse entity: " + line);
              return null;
            }
          } else if (parts.length == 4) {
            return new EntityEX(Entity.valueOf(parts[0]), parts[1], Integer.valueOf(parts[2]), parts[3]);
          } else if (parts.length == 6) {
            return new EntityEX(Entity.valueOf(parts[0]), parts[1], Integer.valueOf(parts[2]), parts[3], Integer.valueOf(parts[4]), Integer.valueOf(parts[5]));
          } else {
            logger.log(Level.SEVERE, "Unable to parse entity: " + line);
            return null;
          }
        } catch (IllegalArgumentException e) {
          logger.log(Level.SEVERE, "Unable to parse entity: " + line);
          return null;
        }
      }
    };
  }
  
  public static String getPackageLine(String fqn) {
    return Entity.PACKAGE.name() + " " + fqn;
  }
  
  public static String getSourceLine(Entity type, String fqn, int modifiers, String path, int startPos, int length) {
    return type.name() + " " + fqn + " " + modifiers + " " + path + " " + startPos + " " + length;
  }
  
  public static String getClassLine(Entity type, String fqn, int modifiers, String path) {
    return type.name() + " " + fqn + " " + modifiers + " " + path;
  }
}
