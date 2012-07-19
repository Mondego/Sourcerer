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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import edu.uci.ics.sourcerer.tools.java.model.types.Entity;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class ModeledParametrizedType extends ModeledEntity {
  private ModeledDeclaredType baseType;
  private List<ModeledEntity> typeArgs;
  
  ModeledParametrizedType(Integer entityID, String fqn, Entity type, Integer projectID) {
    super(entityID, fqn, type, projectID);
    typeArgs = Collections.emptyList();
  }
  
  void setBaseType(ModeledDeclaredType baseType) {
    this.baseType = baseType;
  }
  
  public ModeledDeclaredType getBaseType() {
    return baseType;
  }
  
  void addTypeArgument(ModeledEntity typeArg) {
    if (typeArgs.isEmpty()) {
      typeArgs = new LinkedList<>();
    }
    typeArgs.add(typeArg);
  }
  
  public List<? extends ModeledEntity> getTypeArgs() {
    return typeArgs;
  }
}
