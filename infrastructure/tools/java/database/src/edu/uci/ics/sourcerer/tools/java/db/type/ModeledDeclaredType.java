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
public class ModeledDeclaredType extends ModeledStructuralEntity {
  private ModeledEntity superclass;
  private Collection<ModeledEntity> interfaces;
  
  ModeledDeclaredType(Integer entityID, Modifiers mods, String fqn, Entity type, Integer fileID, Integer projectID) {
    super(entityID, mods, fqn, type, fileID, projectID);
    interfaces = Collections.emptyList();
  }
  
  void setSuperclass(ModeledEntity superclass) {
    this.superclass = superclass;
  }
  
  public ModeledEntity getSuperclass() {
    return superclass;
  }
  
  void addInterface(ModeledEntity iface) {
    if (interfaces.isEmpty()) {
      interfaces = new LinkedList<>();
    }
    interfaces.add(iface);
  }
  
  public Collection<? extends ModeledEntity> getInterfaces() {
    return interfaces;
  }
}
