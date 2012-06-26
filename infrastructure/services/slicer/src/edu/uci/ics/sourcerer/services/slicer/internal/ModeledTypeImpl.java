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
package edu.uci.ics.sourcerer.services.slicer.internal;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import edu.uci.ics.sourcerer.services.slicer.model.ModeledType;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
class ModeledTypeImpl implements ModeledType {
  private final Integer entityID;
  private ModeledTypeImpl superclass;
  private Collection<ModeledTypeImpl> superInterfaces;
  
  ModeledTypeImpl(Integer entityID) {
    this.entityID = entityID;
    superInterfaces = Collections.emptyList();
  }
  
  @Override
  public Integer getEntityID() {
    return entityID;
  }
  
  void setSuperClass(ModeledTypeImpl type) {
    superclass = type;
  }
  
  @Override
  public ModeledTypeImpl getSuperclass() {
    return superclass;
  }
  
  void addSuperInterace(ModeledTypeImpl type) {
    if (superInterfaces.isEmpty()) {
      superInterfaces = new LinkedList<>();
    }
    superInterfaces.add(type);
  }
  
  @Override
  public Collection<? extends ModeledTypeImpl> getSuperInterfaces() {
    return superInterfaces;
  }
  
  @Override
  public String toString() {
    return Integer.toString(entityID);
  }
}
