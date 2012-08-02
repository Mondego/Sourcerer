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
package edu.uci.ics.sourcerer.tools.java.db.importer.resolver;

import static edu.uci.ics.sourcerer.util.io.logging.Logging.logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;

import edu.uci.ics.sourcerer.tools.java.db.schema.EntitiesTable;
import edu.uci.ics.sourcerer.tools.java.db.schema.RelationsTable;
import edu.uci.ics.sourcerer.tools.java.model.types.Entity;
import edu.uci.ics.sourcerer.tools.java.model.types.Relation;
import edu.uci.ics.sourcerer.tools.java.model.types.RelationClass;
import edu.uci.ics.sourcerer.utils.db.QueryExecutor;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class ModeledEntity {
  private String fqn;
  private Entity type;
  private Integer entityID;
  private RelationClass rClass;
  private Collection<Integer> duplicates;
  private Collection<ModeledEntity> virtualDuplicates;
  private Collection<ModeledEntity> parents;
  
  ModeledEntity() {
    virtualDuplicates = new ArrayList<>();
    type = Entity.VIRTUAL_DUPLICATE;
    rClass = RelationClass.NOT_APPLICABLE;
  }
  
  ModeledEntity(String fqn, Entity type, Integer entityID, RelationClass rClass) {
    this.fqn = fqn;
    this.type = type;
    this.entityID = entityID;
    this.rClass = rClass;
  }
  
  String getFQN() {
    return fqn;
  }
  
  Entity getType() {
    return type;
  }
  
//  public Integer getEntityID() {
//    return entityID;
//  }
  
  public RelationClass getRelationClass() {
    return rClass;
  }
  
  public Integer getEntityID(QueryExecutor exec, Integer projectID) {
    if (entityID == null) {
      if (duplicates != null) {
        entityID = exec.insertWithKey(EntitiesTable.createInsert(Entity.DUPLICATE, fqn, projectID));
        for (Integer dupID : duplicates) {
          exec.insert(RelationsTable.makeInsert(Relation.MATCHES, RelationClass.EXTERNAL, entityID, dupID, projectID));
        }
      } else if (virtualDuplicates != null) {
        entityID = exec.insertWithKey(EntitiesTable.createInsert(Entity.VIRTUAL_DUPLICATE, null, projectID));
        if (virtualDuplicates.size() <= 1) {
          logger.severe("Single virtual duplicate! " + virtualDuplicates.toString());
        }
        for (ModeledEntity dup : virtualDuplicates) {
          exec.insert(RelationsTable.makeInsert(Relation.MATCHES, dup.rClass, entityID, dup.entityID, projectID));
        }
      } else {
        logger.severe("Null entityID and no duplicates: " + fqn);
      }
    }
    return entityID;
  }
  
  public void addDuplicate(ModeledEntity entity) {
    if (duplicates == null) {
      duplicates = new HashSet<>();
      duplicates.add(this.entityID);
      type = Entity.DUPLICATE;
      rClass = RelationClass.NOT_APPLICABLE;
      entityID = null;
    }
    duplicates.add(entity.entityID);
  }
  
  public void addVirtualDuplicate(ModeledEntity entity) {
    virtualDuplicates.add(entity);
  }
  
  public void addParent(ModeledEntity entity) {
    if (parents == null) {
      parents = new LinkedList<>();
    }
    parents.add(entity);
  }
  
  public Collection<ModeledEntity> getParents() {
    if (parents == null) {
      return Collections.emptyList();
    } else {
      return parents;
    }
  }
  
  @Override
  public String toString() {
    return fqn + " (" + entityID + "-" + rClass + ")";
  }
}
