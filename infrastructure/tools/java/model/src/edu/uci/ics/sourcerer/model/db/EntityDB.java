// Sourcerer: an infrastructure for large-scale source code analysis.
// Copyright (C) by contributors. See CONTRIBUTORS.txt for full list.
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program. If not, see <http://www.gnu.org/licenses/>.
package edu.uci.ics.sourcerer.model.db;

import java.util.Set;

import edu.uci.ics.sourcerer.model.Entity;
import edu.uci.ics.sourcerer.model.Modifier;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class EntityDB {
  private TypedEntityID entityID;
  private Entity entityType;
  private String fqn;
  private String modifiers;

  public EntityDB(TypedEntityID entityID, Entity entityType, String fqn, String modifiers) {
    this.entityID = entityID;
    this.entityType = entityType;
    this.fqn = fqn;
    this.modifiers = modifiers;
  }

  public boolean isFromSource() {
    return entityID.getType().isSource();
  }

  public TypedEntityID getEntityID() {
    return entityID;
  }

  public Entity getEntityType() {
    return entityType;
  }

  public String getFqn() {
    return fqn;
  }

  public String getModifiers() {
    return modifiers;
  }

  public Set<Modifier> getModifiersSet() {
    return Modifier.convertFromString(modifiers);
  }
  
  public String toString() {
    return entityID.toString() + " " + fqn;
  }
}
