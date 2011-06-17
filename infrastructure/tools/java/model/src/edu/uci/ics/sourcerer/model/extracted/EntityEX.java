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

import java.util.Scanner;
import java.util.Set;
import java.util.logging.Level;

import edu.uci.ics.sourcerer.model.Entity;
import edu.uci.ics.sourcerer.model.Location;
import edu.uci.ics.sourcerer.model.metrics.Metrics;
import edu.uci.ics.sourcerer.util.Helper;
import edu.uci.ics.sourcerer.util.io.LineBuilder;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public final class EntityEX implements ModelEX {
  private Entity type;
  private String fqn;
  private Integer mods;
  private Metrics metrics;
  private Location location;
  
  private EntityEX(Entity type, String fqn, Metrics metrics) {
    this(type, fqn, null, metrics, null);
  }
  
  private EntityEX(Entity type, String fqn, Integer mods, Metrics metrics, Location location) {
    this.type = type;
    this.fqn = fqn;
    this.mods = mods;
    this.metrics = metrics;
    this.location = location;
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
  
  public Metrics getMetrics() {
    return metrics;
  }

  public String getPath() {
    if (location == null) {
      return null;
    } else {
      return location.getPath();
    }
  }

  public Integer getOffset() {
    if (location == null || location.getOffset() == -1) {
      return null;
    } else {
      return location.getOffset();
    }
  }

  public Integer getLength() {
    if (location == null || location.getOffset() == -1) {
      return null;
    } else {
      return location.getLength();
    }
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
        Scanner scanner = LineBuilder.getScanner(line);

        try {
          Entity type = Entity.valueOf(scanner.next());
          String fqn = scanner.next();
          if (type == Entity.PACKAGE) {
            if (scanner.hasNext()) {
              logger.log(Level.WARNING, "Line has extra entries: " + line);
            }
            if (uniqueChecker.contains(fqn)) {
              return null;
            } else {
              uniqueChecker.add(fqn);
              return new EntityEX(type, fqn, null);
            }
          } else {
            int mods = scanner.nextInt();
            Metrics metrics = Metrics.parse(scanner);
            Location location = Location.parse(scanner);
            if (scanner.hasNext()) {
              logger.log(Level.WARNING, "Line has extra entries: " + line);
            }
            return new EntityEX(type, fqn, mods, metrics, location);
          }
        } catch (Exception e) {
          logger.log(Level.SEVERE, "Unable to parse entity: " + line, e);
          return null;
        }
      }
    };
  }
  
  public static String getPackageLine(String fqn) {
    return Entity.PACKAGE.name() + " " + fqn;
  }
  
  public static String getSourceLine(Entity type, String fqn, int modifiers, Metrics metrics, Location location) {
    LineBuilder builder = new LineBuilder();
    
    builder.append(type.name());
    builder.append(fqn);
    builder.append(modifiers);
    builder.append(metrics);
    builder.append(location);
    
    return builder.toString();
  }
  
  public static String getClassLine(Entity type, String fqn, int modifiers, Metrics metrics, Location location) {
    LineBuilder builder = new LineBuilder();
    
    builder.append(type.name());
    builder.append(fqn);
    builder.append(modifiers);
    builder.append(metrics);
    builder.append(location);
    
    return builder.toString();
  }
}
