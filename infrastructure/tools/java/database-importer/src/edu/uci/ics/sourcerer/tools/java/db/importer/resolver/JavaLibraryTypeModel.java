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
import java.util.Deque;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import edu.uci.ics.sourcerer.tools.java.db.schema.EntitiesTable;
import edu.uci.ics.sourcerer.tools.java.db.schema.ProjectsTable;
import edu.uci.ics.sourcerer.tools.java.db.schema.RelationsTable;
import edu.uci.ics.sourcerer.tools.java.model.types.Entity;
import edu.uci.ics.sourcerer.tools.java.model.types.Modifier;
import edu.uci.ics.sourcerer.tools.java.model.types.Modifiers;
import edu.uci.ics.sourcerer.tools.java.model.types.Project;
import edu.uci.ics.sourcerer.tools.java.model.types.Relation;
import edu.uci.ics.sourcerer.tools.java.model.types.RelationClass;
import edu.uci.ics.sourcerer.util.Pair;
import edu.uci.ics.sourcerer.util.io.logging.TaskProgressLogger;
import edu.uci.ics.sourcerer.util.type.TypeUtils;
import edu.uci.ics.sourcerer.utils.db.DatabaseRunnable;
import edu.uci.ics.sourcerer.utils.db.sql.SelectQuery;
import edu.uci.ics.sourcerer.utils.db.sql.TypedQueryResult;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class JavaLibraryTypeModel {
  private Map<String, ModeledEntity> entities;
  private Map<Integer, ModeledEntity> reverseMap;
  
  private JavaLibraryTypeModel() {
    entities = new HashMap<>();
    reverseMap = new HashMap<>();
  }
  
  private void add(String fqn, ModeledEntity entity) {
    if (entities.containsKey(fqn)) {
      logger.severe("Duplicate FQN: " + fqn);
    } else {
      entities.put(fqn, entity);
    }
  }
  
  public static JavaLibraryTypeModel createJavaLibraryTypeModel() {
    final JavaLibraryTypeModel model = new JavaLibraryTypeModel();
    new DatabaseRunnable() {
      @Override
      public void action() {
        TaskProgressLogger task = TaskProgressLogger.get();
        
        task.start("Building Java library type model");
        
        task.start("Loading Java library entities", "entities loaded");

        try (SelectQuery query = exec.createSelectQuery(EntitiesTable.TABLE)) {
          query.addSelect(EntitiesTable.ENTITY_ID, EntitiesTable.FQN);
          query.andWhere(EntitiesTable.ENTITY_TYPE.compareEquals(Entity.PRIMITIVE));
          TypedQueryResult result = query.select();
          while (result.next()) {
            String fqn = result.getResult(EntitiesTable.FQN);
            Integer entityID = result.getResult(EntitiesTable.ENTITY_ID);
            model.add(fqn, new ModeledEntity(fqn, Entity.PRIMITIVE, entityID, RelationClass.JAVA_LIBRARY));
            task.progress();
          }
        }
        
        Collection<Integer> libraries = new ArrayList<>();
        try (SelectQuery query = exec.createSelectQuery(ProjectsTable.TABLE)) {
          // Get the Java Library projectIDs
          query.addSelect(ProjectsTable.PROJECT_ID);
          query.andWhere(ProjectsTable.PROJECT_TYPE.compareEquals(Project.JAVA_LIBRARY));
          
          libraries.addAll(query.select().toCollection(ProjectsTable.PROJECT_ID));
        }
        
        try (SelectQuery query = exec.createSelectQuery(EntitiesTable.TABLE)) {
          query.addSelect(EntitiesTable.ENTITY_ID, EntitiesTable.FQN, EntitiesTable.ENTITY_TYPE, EntitiesTable.PARAMS, EntitiesTable.RAW_PARAMS);
          query.andWhere(
              EntitiesTable.PROJECT_ID.compareIn(libraries),
              EntitiesTable.ENTITY_TYPE.compareIn(EnumSet.of(Entity.PACKAGE, Entity.CLASS, Entity.INTERFACE, Entity.ENUM, Entity.ANNOTATION, Entity.CONSTRUCTOR, Entity.METHOD, Entity.ANNOTATION_ELEMENT, Entity.ENUM_CONSTANT, Entity.FIELD)),
              EntitiesTable.MODIFIERS.compareNotEquals(Modifiers.make(Modifier.PRIVATE)));

          TypedQueryResult result = query.select();
          while (result.next()) {
            Integer entityID = result.getResult(EntitiesTable.ENTITY_ID);
            String fqn = result.getResult(EntitiesTable.FQN);
            Entity type = result.getResult(EntitiesTable.ENTITY_TYPE);
            String params = result.getResult(EntitiesTable.PARAMS);
            ModeledEntity entity = new ModeledEntity(fqn, type, entityID, RelationClass.JAVA_LIBRARY);
            if (params == null) {
              model.add(fqn, entity);
            } else {
              String rawParams = result.getResult(EntitiesTable.RAW_PARAMS);
              if (rawParams != null) {
                model.add(fqn + rawParams, entity);
              }
              model.add(fqn + params, entity);
            }
            if (type == Entity.CLASS || type == Entity.ENUM || type == Entity.ANNOTATION || type == Entity.INTERFACE) {
              model.reverseMap.put(entityID, entity);
            }
            task.progress();
          }
        }
        task.finish();
        
        try (SelectQuery query = exec.createSelectQuery(RelationsTable.TABLE)) {
          query.addSelect(RelationsTable.LHS_EID, RelationsTable.RHS_EID);
          query.andWhere(RelationsTable.PROJECT_ID.compareIn(libraries), RelationsTable.RELATION_TYPE.compareEquals(Relation.HAS_BASE_TYPE));
          Map<Integer, Integer> pMapping = new HashMap<>();
          
          task.start("Loading has_base_type relations", "relations loaded");
          TypedQueryResult result = query.select();
          while (result.next()) {
            pMapping.put(result.getResult(RelationsTable.LHS_EID), result.getResult(RelationsTable.RHS_EID));
            task.progress();
          }
          task.finish();
          
          task.start("Loading extends/implements relations", "relations loaded");
          query.clearWhere();
          query.andWhere(RelationsTable.PROJECT_ID.compareIn(libraries), RelationsTable.RELATION_TYPE.compareIn(EnumSet.of(Relation.EXTENDS, Relation.IMPLEMENTS)));
          
          result = query.select();
          while (result.next()) {
            Integer lhsEID = result.getResult(RelationsTable.LHS_EID);
            Integer rhsEID = result.getResult(RelationsTable.RHS_EID);
            Integer altRHS = pMapping.get(rhsEID);
            if (altRHS != null) {
              rhsEID = altRHS;
            }
            ModeledEntity child = model.reverseMap.get(lhsEID);
            if (child == null) {
//              logger.severe("Missing child from map: " + lhsEID);
              continue;
            }
            ModeledEntity parent = model.reverseMap.get(rhsEID);
            if (parent == null) {
//              logger.severe("Missing parent from map: " + rhsEID);
              continue;
            }
            child.addParent(parent);
            task.progress();
          }
        }
        task.finish();
        
        task.finish();
      }
    }.run(); 
    return model;
  }
  
  ModeledEntity getEntity(Integer entityID) {
    return reverseMap.get(entityID);
  }
  
  synchronized ModeledEntity getEntity(String fqn) {
    return entities.get(fqn);
  }
  
  synchronized ModeledEntity getVirtualEntity(String fqn) {
    // Try the map
    if (entities.containsKey(fqn)) {
      return entities.get(fqn);
    }
    
    // Is it a method or a field?
    if (TypeUtils.isMethod(fqn)) {
      Pair<String, String> parts = TypeUtils.breakMethod(fqn);
      
      // No resolution for constructors
      if (parts.getSecond().startsWith("<init>") || parts.getSecond().startsWith("<clinit>")) {
        return null;
      }
      
      // Can we find the receiver type?
      ModeledEntity receiver = entities.get(parts.getFirst());
      
      // No receiver, no virtual resolution
      if (receiver == null) {
        return null;
      } else {
        ModeledEntity classMethod = null;
        Collection<ModeledEntity> interfaceMethods = new HashSet<>();
        
        Set<ModeledEntity> seen = new HashSet<>();
        Deque<ModeledEntity> stack = new LinkedList<>();
        stack.push(receiver);
        while (!stack.isEmpty()) {
          // Get all the parents
          for (ModeledEntity parent : stack.pop().getParents()) {
            if (!seen.contains(parent)) {
              seen.add(parent);
              // See if the parent has the method
              ModeledEntity method = entities.get(parent.getFQN() + "." + parts.getSecond());
              if (method == null) {
                stack.add(parent);
              } else if (parent.getType() == Entity.INTERFACE) {
                interfaceMethods.add(method);
              } else if (classMethod == null){
                classMethod = method;
              } else {
                // If one of the class methods belongs to Object, drop it
                if (method.getFQN().startsWith("java.lang.Object")) {
                } else if (classMethod.getFQN().startsWith("java.lang.Object")) {
                  method = classMethod;
                } else {
                  logger.severe("Multiple class methods for: " + fqn + " (" + classMethod.toString() + " and " + method.toString() + ")");
                }
              }
            }
          }
        }
        
        if (classMethod == null && interfaceMethods.isEmpty()) {
          entities.put(fqn, null);
          return null;
        } else if (classMethod != null) {
          entities.put(fqn, classMethod);
          return classMethod;
        } else if (interfaceMethods.size() == 1) {
          ModeledEntity entity = interfaceMethods.iterator().next();
          entities.put(fqn, entity);
          return entity;
        } else {
          ModeledEntity entity = new ModeledEntity();
          for (ModeledEntity method : interfaceMethods) {
            entity.addVirtualDuplicate(method);
          }
          entities.put(fqn, entity);
          return entity;
        }
      }
    } else {
      int dot = fqn.lastIndexOf('.');
      if (dot == -1) {
        logger.warning("Field with no receiver: " + fqn);
        return null;
      }
      
      String receiverFQN = fqn.substring(0, dot);
      String fieldName = fqn.substring(dot + 1);
      
      // Can we find the receiver type?
      ModeledEntity receiver = entities.get(receiverFQN);
      
      // No receiver, no virtual resolution
      if (receiver == null) {
        return null;
      } else {
        Collection<ModeledEntity> fields = new HashSet<>();
        
        Set<ModeledEntity> seen = new HashSet<>();
        Deque<ModeledEntity> stack = new LinkedList<>();
        stack.push(receiver);
        while (!stack.isEmpty()) {
          // Get all the parents
          for (ModeledEntity parent : stack.pop().getParents()) {
            if (!seen.contains(parent)) {
              seen.add(parent);
              // See if the parent has the field
              ModeledEntity field = entities.get(parent.getFQN() + "." + fieldName);
              if (field == null) {
                stack.add(parent);
              } else {
                fields.add(field);
              }
            }
          }
        }
        
        if (fields.isEmpty()) {
          entities.put(fqn, null);
          return null;
        } else if (fields.size() == 1) {
          ModeledEntity entity = fields.iterator().next();
          entities.put(fqn, entity);
          return entity;
        } else {
          logger.severe("Virtual field resolution should never be ambiguous: " + fqn + " " + fields.toString());
          entities.put(fqn, null);
          return null;
        }
      }
    }
  }
}
