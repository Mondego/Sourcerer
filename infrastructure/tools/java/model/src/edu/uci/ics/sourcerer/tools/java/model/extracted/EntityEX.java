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

import edu.uci.ics.sourcerer.tools.java.model.types.Entity;
import edu.uci.ics.sourcerer.tools.java.model.types.Location;
import edu.uci.ics.sourcerer.tools.java.model.types.Metrics;
import edu.uci.ics.sourcerer.tools.java.model.types.Modifiers;
import edu.uci.ics.sourcerer.util.io.SimpleSerializable;
import edu.uci.ics.sourcerer.util.io.arguments.Argument;
import edu.uci.ics.sourcerer.util.io.arguments.StringArgument;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public final class EntityEX implements SimpleSerializable {
  public static final Argument<String> ENTITY_FILE = new StringArgument("entity-file", "entities.txt", "Filename for the extracted entities.").permit();
  
  private Entity type;
  private String fqn;
  private String signature;
  private String rawSignature;
  private Modifiers modifiers;
  private Metrics metrics;
  private Location location;
  
  public EntityEX() {}
  
  public EntityEX(EntityEX entity) {
    type = entity.type;
    fqn = entity.fqn;
    signature = entity.signature;
    rawSignature = entity.rawSignature;
    modifiers = entity.modifiers;
    metrics = entity.metrics;
    location = entity.location;
  }

  public EntityEX(Entity type, String fqn, String signature, String rawSignature, Modifiers modifiers, Metrics metrics, Location location) {
    this.type = type;
    this.fqn = fqn;
    this.signature = signature;
    this.rawSignature = rawSignature;
    this.modifiers = modifiers;
    this.metrics = metrics;
    this.location = location;
  }
  
  public EntityEX update(Entity type, String fqn, String signature, String rawSignature, int modifiers, Metrics metrics, Location location) {
    this.type = type;
    this.fqn = fqn;
    this.signature = signature;
    this.rawSignature = rawSignature;
    this.modifiers = Modifiers.make(modifiers);
    this.metrics = metrics;
    this.location = location;
    return this;
  }
  
  public Entity getType() {
    return type;
  }

  public String getFqn() {
    return fqn;
  }
  
  public String getSignature() {
    return signature;
  }
  
  public String getRawSignature() {
    return rawSignature;
  }

  public Modifiers getModifiers() {
    return modifiers;
  }
  
  public Metrics getMetrics() {
    return metrics;
  }

  public Location getLocation() {
    return location;
  }
  
  public String toString() {
    if (type == null) {
      return null + " " + fqn;
    } else {
      return type.name() + " " + fqn;
    }
  }
}
