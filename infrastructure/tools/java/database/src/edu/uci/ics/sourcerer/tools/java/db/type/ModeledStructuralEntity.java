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
package edu.uci.ics.sourcerer.tools.java.db.type;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import edu.uci.ics.sourcerer.tools.java.model.types.Entity;
import edu.uci.ics.sourcerer.tools.java.model.types.Modifiers;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class ModeledStructuralEntity extends ModeledEntity {
  private final Modifiers mods;
  private final Integer fileID;
  private ModeledStructuralEntity owner;
  private Collection<ModeledStructuralEntity> children;
  
  ModeledStructuralEntity(Integer entityID, Modifiers mods, String fqn, Entity type, Integer fileID, Integer projectID) {
    super(entityID, fqn, type, projectID);
    this.mods = mods;
    this.fileID = fileID;
    children = Collections.emptyList();
  }
  
  public final Modifiers getModifiers() {
    return mods;
  }
  
  public final Integer getFileID() {
    return fileID;
  }
  
  final void setOwner(ModeledStructuralEntity owner) {
    this.owner = owner;
  }
  
  public final ModeledStructuralEntity getOwner() {
    return owner;
  }
  
  final void addChild(ModeledStructuralEntity type) {
    if (children.isEmpty()) {
      children = new LinkedList<>();
    }
    children.add(type);
    type.setOwner(this);
  }
  
  public final Collection<? extends ModeledStructuralEntity> getChildren() {
    return children;
  }
}
