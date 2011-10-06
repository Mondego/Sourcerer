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
package edu.uci.ics.sourcerer.tools.java.db.importer;

import static edu.uci.ics.sourcerer.util.io.Logging.logger;

import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import edu.uci.ics.sourcerer.tools.java.db.schema.EntitiesTable;
import edu.uci.ics.sourcerer.tools.java.db.schema.RelationsTable;
import edu.uci.ics.sourcerer.tools.java.model.types.Entity;
import edu.uci.ics.sourcerer.tools.java.model.types.Relation;
import edu.uci.ics.sourcerer.tools.java.model.types.RelationClass;
import edu.uci.ics.sourcerer.util.Pair;
import edu.uci.ics.sourcerer.util.io.TaskProgressLogger;
import edu.uci.ics.sourcerer.utils.db.DatabaseRunnable;
import edu.uci.ics.sourcerer.utils.db.QueryExecutor;
import edu.uci.ics.sourcerer.utils.db.sql.ConstantCondition;
import edu.uci.ics.sourcerer.utils.db.sql.SelectQuery;
import edu.uci.ics.sourcerer.utils.db.sql.TypedQueryResult;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
final class EntityMap {
  private LibraryEntityMap libraries;
  private Map<String, DatabaseEntity> entities;
  private Integer projectID;
  
  private QueryExecutor exec;
  private SelectQuery select;
  private ConstantCondition<String> selectFQN;
  
  public EntityMap(TaskProgressLogger task, QueryExecutor exec, Integer projectID, Collection<Integer> externalProjects, LibraryEntityMap libraries) {
    this.libraries = libraries;
    this.projectID = projectID;
    this.exec = exec;
    entities = new HashMap<>();
    addInternalEntities(task);
    if (externalProjects != null && !externalProjects.isEmpty()) {
      select = exec.makeSelectQuery(EntitiesTable.TABLE);
      select.addSelects(EntitiesTable.ENTITY_ID, EntitiesTable.FQN, EntitiesTable.PARAMS, EntitiesTable.RAW_PARAMS);
      selectFQN = EntitiesTable.FQN.compareEquals();
      select.andWhere(EntitiesTable.PROJECT_ID.compareIn(externalProjects).and(selectFQN));
    }
  }
  
  public void addUnique(String fqn, DatabaseEntity entity) {
    if (entities.containsKey(fqn)) {
      logger.severe("Duplicate FQN: " + fqn);
    } else {
      entities.put(fqn, entity);
    }
  }
   
  private void addInternalEntities(final TaskProgressLogger task) {
    new DatabaseRunnable() {
      @Override
      public void action() {
        task.start("Loading entities", "entities loaded");
        try (SelectQuery query = exec.makeSelectQuery(EntitiesTable.TABLE)) {
          query.addSelects(EntitiesTable.ENTITY_ID, EntitiesTable.FQN, EntitiesTable.PARAMS, EntitiesTable.RAW_PARAMS);
          query.andWhere(
              EntitiesTable.PROJECT_ID.compareEquals(projectID).and(
              EntitiesTable.ENTITY_TYPE.compareNotIn(EnumSet.of(Entity.PARAMETER, Entity.LOCAL_VARIABLE))));

          TypedQueryResult result = query.selectStreamed();
          while (result.next()) {
            DatabaseEntity entity = DatabaseEntity.make(result.getResult(EntitiesTable.ENTITY_ID), RelationClass.INTERNAL);
            String fqn = result.getResult(EntitiesTable.FQN);
            String params = result.getResult(EntitiesTable.PARAMS);
            if (params == null) {
              addUnique(fqn, entity);
            } else {
              String rawParams = result.getResult(EntitiesTable.RAW_PARAMS);
              if (rawParams != null) {
                addUnique(fqn + rawParams, entity);
              }
              addUnique(fqn + params, entity);
            }
            task.progress();
          }
        }
        task.finish();
      }
    }.run(); 
  }
  
  private final void addDuplicate(String fqn, Integer entityID) {
    DatabaseEntity entity = entities.get(fqn);
    if (entity == null) {
      entity = DatabaseEntity.make(entityID, RelationClass.EXTERNAL);
      entities.put(fqn, entity);
    } else {
      if (entity.getRelationClass() == RelationClass.INTERNAL) {
        logger.severe("Should not duplicate inernal FQN: " + fqn);
      } else if (entity.getRelationClass() == RelationClass.EXTERNAL) {
        Integer dupID = exec.insertWithKey(EntitiesTable.makeInsert(Entity.DUPLICATE, fqn, null, projectID));
        exec.insert(RelationsTable.makeInsert(Relation.MATCHES, RelationClass.EXTERNAL, dupID, entity.getEntityID(), projectID));
        exec.insert(RelationsTable.makeInsert(Relation.MATCHES, RelationClass.EXTERNAL, dupID, entityID, projectID));
        DatabaseEntity dupEntity = DatabaseEntity.make(dupID, RelationClass.MIXED_EXTERNAL);
        entities.put(fqn, dupEntity);
      } else if (entity.getRelationClass() == RelationClass.MIXED_EXTERNAL) {
        exec.insert(RelationsTable.makeInsert(Relation.MATCHES, RelationClass.EXTERNAL, entity.getEntityID(), entityID, projectID));
      }
    }
  }
  
  private DatabaseEntity getTypeEntity(String fqn) {
    if (TypeUtils.isArray(fqn)) {
      Pair<String, Integer> arrayInfo = TypeUtils.breakArray(fqn);
      
      // Insert the array entity
      Integer entityID = exec.insertWithKey(EntitiesTable.makeInsert(Entity.ARRAY, fqn, null, arrayInfo.getSecond(), projectID));
      DatabaseEntity entity = DatabaseEntity.make(entityID, RelationClass.NOT_APPLICABLE);
      addUnique(fqn, entity);
      
      // Get the component type
      DatabaseEntity component = getEntity(arrayInfo.getFirst());

      // Add has elements of relation
      exec.insert(RelationsTable.makeInsert(Relation.HAS_ELEMENTS_OF, component.getRelationClass(), entityID, component.getEntityID(), projectID));
  
      return entity;
    }
    
    if (TypeUtils.isWildcard(fqn)) {
      // Insert the wildcard entity
      Integer entityID = exec.insertWithKey(EntitiesTable.makeInsert(Entity.WILDCARD, fqn, null, projectID));
      DatabaseEntity entity = DatabaseEntity.make(entityID, RelationClass.NOT_APPLICABLE);
      
      // If it's bounded, add the bound relation
      if (!TypeUtils.isUnboundedWildcard(fqn)) {
        DatabaseEntity bound = getEntity(TypeUtils.getWildcardBound(fqn));
        if (TypeUtils.isLowerBound(fqn)) {
          exec.insert(RelationsTable.makeInsert(Relation.HAS_LOWER_BOUND, bound.getRelationClass(), entityID, bound.getEntityID(), projectID));
        } else {
          exec.insert(RelationsTable.makeInsert(Relation.HAS_UPPER_BOUND, bound.getRelationClass(), entityID, bound.getEntityID(), projectID));
        }
      }
      
      return entity;
    }
    
    if (TypeUtils.isTypeVariable(fqn)) {
      // Insert the type variable entity
      Integer entityID = exec.insertWithKey(EntitiesTable.makeInsert(Entity.TYPE_VARIABLE, fqn, null, projectID));
      DatabaseEntity entity = DatabaseEntity.make(entityID, RelationClass.NOT_APPLICABLE);
      
      // Insert the bound relations
      for (String bound : TypeUtils.breakTypeVariable(fqn)) {
        DatabaseEntity boundEntity = getEntity(bound);
        exec.insert(RelationsTable.makeInsert(Relation.HAS_UPPER_BOUND, boundEntity.getRelationClass(), entityID, boundEntity.getEntityID(), projectID));
      }
      
      return entity;
    }
    
    if (TypeUtils.isParametrizedType(fqn)) {
      // Insert the parametrized type entity
      Integer entityID = exec.insertWithKey(EntitiesTable.makeInsert(Entity.PARAMETERIZED_TYPE, fqn, null, projectID));
      DatabaseEntity entity = DatabaseEntity.make(entityID, RelationClass.NOT_APPLICABLE);
      
      // Add the has base type relation
      DatabaseEntity baseType = getEntity(TypeUtils.getBaseType(fqn));
      exec.insert(RelationsTable.makeInsert(Relation.HAS_BASE_TYPE, baseType.getRelationClass(), entityID, baseType.getEntityID(), projectID));
      
      // Insert the type arguments
      for (String arg : TypeUtils.breakParametrizedType(fqn)) {
        DatabaseEntity argEntity = getEntity(arg);
        exec.insert(RelationsTable.makeInsert(Relation.HAS_TYPE_ARGUMENT, argEntity.getRelationClass(), entityID, argEntity.getEntityID(), projectID));
      }
      
      return entity; 
    }
    
    return null;
  }
  
  public DatabaseEntity getEntity(String fqn) {
    // Try the map
    DatabaseEntity entity = entities.get(fqn);
    if (entity != null) {
      return entity;
    }
    
    // Maybe it's a type entity
    entity = getTypeEntity(fqn);
    if (entity != null) {
      return entity;
    }
    
    // Maybe it's external
    if (selectFQN != null) {
      selectFQN.setValue(fqn);
      TypedQueryResult result = select.select();
      while (result.next()) {
        addDuplicate(fqn, result.getResult(EntitiesTable.ENTITY_ID));
      }
      entity = entities.get(fqn);
      if (entity != null) {
        return entity;
      }
    }
    
    // Must be a java library
    return libraries.getEntity(exec, fqn);
  }
  
  //TODO Make this actually do virtual resolution
  public DatabaseEntity getVirtualEntity(String fqn) {
    DatabaseEntity entity = entities.get(fqn);
    if (entity == null) {
      return libraries.getEntity(exec, fqn);
    } else {
      return entity;
    }
  }
}
