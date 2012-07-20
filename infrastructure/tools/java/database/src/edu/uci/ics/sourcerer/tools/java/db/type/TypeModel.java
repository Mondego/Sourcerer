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
import java.util.HashMap;
import java.util.Map;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class TypeModel {
  private final Map<Integer, ModeledEntity> entities;
  private final TypeModel parentModel;
  
  TypeModel(TypeModel parentModel) {
    entities = new HashMap<>();
    this.parentModel = parentModel; 
  }
  
  void add(ModeledEntity entity) {
    entities.put(entity.getEntityID(), entity);
  }
  
  public ModeledEntity get(Integer entityID) {
    ModeledEntity entity = entities.get(entityID);
    if (entity == null && parentModel != null) {
      return parentModel.get(entityID);
    } else {
      return entity;
    }
  }
  
  public Collection<? extends ModeledEntity> getEntities() {
    return entities.values();
  }
}
