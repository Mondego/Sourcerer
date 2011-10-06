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
import java.util.logging.Level;

import edu.uci.ics.sourcerer.tools.java.db.schema.EntitiesTable;
import edu.uci.ics.sourcerer.tools.java.db.schema.RelationsTable;
import edu.uci.ics.sourcerer.tools.java.model.extracted.LocalVariableEX;
import edu.uci.ics.sourcerer.tools.java.model.extracted.RelationEX;
import edu.uci.ics.sourcerer.tools.java.model.extracted.io.ReaderBundle;
import edu.uci.ics.sourcerer.tools.java.model.types.Entity;
import edu.uci.ics.sourcerer.tools.java.model.types.LocalVariable;
import edu.uci.ics.sourcerer.tools.java.model.types.Relation;
import edu.uci.ics.sourcerer.tools.java.model.types.RelationClass;
import edu.uci.ics.sourcerer.util.io.TaskProgressLogger;
import edu.uci.ics.sourcerer.utils.db.BatchInserter;
import edu.uci.ics.sourcerer.utils.db.sql.ConstantCondition;
import edu.uci.ics.sourcerer.utils.db.sql.SelectQuery;
import edu.uci.ics.sourcerer.utils.db.sql.TypedQueryResult;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public abstract class StructuralRelationsImporter extends RelationsImporter {
  protected Collection<Integer> libraryProjects;
  
//  private SelectQuery externalEntitiesQuery;
//  private ConstantCondition<String> externalEntitiesQueryFqn;
  
//  private SelectQuery newEntitiesQuery;
//  private ConstantCondition<Integer> newEntitiesQueryProjectID;
  
  private SelectQuery localVariablesQuery;
  private ConstantCondition<Integer> localVariablesQueryProjectID;
  
  protected StructuralRelationsImporter(String taskName, LibraryEntityMap libraries) {
    super(taskName, libraries);
  }
  
//  @Override
//  protected void init() {
//    super.init();
//    try (SelectQuery query = exec.makeSelectQuery(ProjectsTable.TABLE)) {
//      // Get the Java Library projectIDs
//      query.addSelect(ProjectsTable.PROJECT_ID);
//      query.andWhere(ProjectsTable.PROJECT_TYPE.compareEquals(Project.JAVA_LIBRARY));
//      
//      libraryProjects = query.select().toCollection(ProjectsTable.PROJECT_ID);
//      
//      // Get the primitives projectID
//      query.clearWhere();
//
//      query.andWhere(ProjectsTable.NAME.compareEquals(ProjectsTable.PRIMITIVES_PROJECT).and(
//          ProjectsTable.PROJECT_TYPE.compareEquals(Project.SYSTEM)));
//      
//      libraryProjects.add(query.select().toSingleton(ProjectsTable.PROJECT_ID));
//    }
//  }
  
  protected final void initializeQueries() {
//    externalEntitiesQuery = exec.makeSelectQuery(EntitiesTable.TABLE);
//    externalEntitiesQuery.addSelects(EntitiesTable.ENTITY_ID, EntitiesTable.PROJECT_ID);
//    externalEntitiesQueryFqn = EntitiesTable.FQN.compareEquals();
//    externalEntitiesQuery.andWhere(externalEntitiesQueryFqn);
//    externalEntitiesQuery.andWhere(EntitiesTable.PROJECT_ID.compareIn(projectIDs));
    
//    newEntitiesQuery = exec.makeSelectQuery(EntitiesTable.TABLE);
//    newEntitiesQuery.addSelects(EntitiesTable.FQN, EntitiesTable.ENTITY_ID, EntitiesTable.ENTITY_TYPE);
//    newEntitiesQueryProjectID = EntitiesTable.PROJECT_ID.compareEquals();
//    newEntitiesQuery.andWhere(newEntitiesQueryProjectID);
//    newEntitiesQuery.andWhere(EntitiesTable.ENTITY_TYPE.compareIn(EnumSet.of(Entity.ARRAY, Entity.WILDCARD, Entity.TYPE_VARIABLE, Entity.PARAMETERIZED_TYPE, Entity.DUPLICATE)));
    
    localVariablesQuery = exec.makeSelectQuery(EntitiesTable.TABLE);
    localVariablesQuery.addSelects(EntitiesTable.ENTITY_ID);
    localVariablesQueryProjectID = EntitiesTable.PROJECT_ID.compareEquals();
    localVariablesQuery.andWhere(localVariablesQueryProjectID);
    localVariablesQuery.andWhere(EntitiesTable.ENTITY_TYPE.compareIn(EnumSet.of(Entity.PARAMETER, Entity.LOCAL_VARIABLE)));
    localVariablesQuery.orderBy(EntitiesTable.ENTITY_ID, true);
  }
  
  protected final void insert(ReaderBundle reader, Integer projectID, Collection<Integer> externalProjects) {
    loadFileMap(projectID);
    loadEntityMap(projectID, externalProjects);
    
    insertRemainingEntities(reader, projectID);
    insertStructuralRelations(reader, projectID);
    
    fileMap.clear();
    entities = null;
  }
  
  private void insertRemainingEntities(ReaderBundle reader, Integer projectID) {
    task.start("Inserting local variables, parameters and type entities", "entities inserted");
    
    BatchInserter inserter = exec.makeInFileInserter(tempDir, EntitiesTable.TABLE);
//    TypeEntityResolver resolver = new TypeEntityResolver(projectID);
    
    TaskProgressLogger processTask = task.spawnChild();
    processTask.start("Processing local variables and parameters file", "variables processed");
    for (LocalVariableEX var : reader.getTransientLocalVariables()) {
      // Get the file
      Integer fileID = getFileID(var.getLocation());
      
      if (fileID != null) {
        // Add the entity
        inserter.addInsert(EntitiesTable.makeInsert(var, projectID, fileID));
        task.progress();
        
        // Resolve the type fqn
//        resolver.resolve(var.getTypeFqn());
      }
      processTask.progress();
    }
    processTask.finish();
    
    
//    processTask.start("Processing relations file", "relations processed");
//    for (RelationEX relation : reader.getTransientRelations()) {
//      // Resolve the rhs fqn
//      if (relation.getType() != Relation.CALLS &&
//          relation.getType() != Relation.READS &&
//          relation.getType() != Relation.WRITES) {
//        if (relation.getType() == Relation.INSIDE) {
//          resolver.resolveInside(relation.getRhs());
//        } else {
//          resolver.resolve(relation.getRhs());
//        }
//        processTask.progress();
//      }
//    }
//    processTask.finish();
//    
//    processTask.start("Processing imports file", "imports processed");
//    for (ImportEX imp : reader.getTransientImports()) {
//      // Resolve the type fqn
//      resolver.resolve(imp.getImported());
//      processTask.progress();
//    }
//    processTask.finish();
    
    task.start("Performing db insert");
    inserter.insert();
    task.finish();
    
    task.finish();
    
//    return resolver.newTypeRelations;
  }
  
//  private void loadRemainingEntityMap(Integer projectID) {
//    task.start("Updating entity map");
//    
//    BatchInserter inserter = exec.makeInFileInserter(tempDir, RelationsTable.TABLE);
//    
//    task.start("Processing new entities", "entities processed");
//    newEntitiesQueryProjectID.setValue(projectID);
//    TypedQueryResult result = newEntitiesQuery.selectStreamed();
//    while (result.next()) {
//      String fqn = result.getResult(EntitiesTable.FQN);
//      DatabaseEntity entity = entityMap.get(fqn);
//      if (result.getResult(EntitiesTable.ENTITY_TYPE) == Entity.DUPLICATE) {
//        if (entity == null) {
//          logger.severe("Missing fqn for duplicate " + fqn);
//        } else {
//          DuplicatedDatabaseEntity dupEntity = (DuplicatedDatabaseEntity) entity;
//          dupEntity.updateDuplicate(inserter, result.getResult(EntitiesTable.ENTITY_ID), projectID);
//        }
//      } else {
//        if (entity == null) {
//          entity = DatabaseEntity.make(result.getResult(EntitiesTable.ENTITY_ID), RelationClass.NOT_APPLICABLE);
//          entityMap.put(fqn, entity);
//        } else {
//          logger.severe("FQN conflict! " + fqn);
//        }
//      }
//      task.progress();
//    }
//    task.finish();
//
//    task.start("Inserting duplicate relations");
//    inserter.insert();
//    task.finish();
//    
//    task.finish();
//  }
  
  private void insertStructuralRelations(ReaderBundle reader, Integer projectID) {
    task.start("Inserting relations");
    
    BatchInserter inserter = exec.makeInFileInserter(tempDir, RelationsTable.TABLE);
    
    task.start("Processing local variables & parameters", "variables processed");
    localVariablesQueryProjectID.setValue(projectID);
    try (TypedQueryResult result = localVariablesQuery.selectStreamed()) {
      for (LocalVariableEX var : reader.getTransientLocalVariables()) {
        if (result.next()) {
          Integer entityID = result.getResult(EntitiesTable.ENTITY_ID);
          Integer fileID = getFileID(var.getLocation());
          
          if (var.getType() == LocalVariable.PARAM) {
            entities.addUnique(var.getParent() + "#" + var.getPosition(), DatabaseEntity.make(entityID, RelationClass.INTERNAL));
          }
          
          // Add the holds relation
          DatabaseEntity type = entities.getEntity(var.getTypeFqn());
          if (type != null) {
            if (fileID == null) {
              inserter.addInsert(RelationsTable.makeInsert(Relation.HOLDS, type.getRelationClass(), entityID, type.getEntityID(), projectID));
            } else {
              inserter.addInsert(RelationsTable.makeInsert(Relation.HOLDS, type.getRelationClass(), entityID, type.getEntityID(), projectID, fileID, var.getLocation()));
            }
          }
          
          // Add the inside relation
          DatabaseEntity parent = entities.getEntity(var.getParent());
          if (parent != null) {
            inserter.addInsert(RelationsTable.makeInsert(Relation.INSIDE, parent.getRelationClass(), entityID, parent.getEntityID(), projectID, fileID));
          }
        } else {
          logger.log(Level.SEVERE, "Missing db local variable for " + var);
        }
        task.progress();
      }
    }
    task.finish();
    
//    task.start("Processing type relations", "relations processed");
//    for (RelationEX relation : newTypeRelations) {
//      Integer lhs = getLHS(relation.getLhs());
//      DatabaseEntity rhs = entities.getEntity(relation.getRhs()); 
//      
//      if (lhs != null && rhs != null) {
//        inserter.addInsert(RelationsTable.makeInsert(relation.getType(), rhs.getRelationClass(), lhs, rhs.getEntityID(), projectID));
//        task.progress();
//      }
//    }
//    newTypeRelations.clear();
//    task.finish();
      
    task.start("Processing structural relations", "relations processed");
    for (RelationEX relation : reader.getTransientRelations()) {
      if (relation.getType() != Relation.CALLS &&
          relation.getType() != Relation.READS &&
          relation.getType() != Relation.WRITES) {
        Integer fileID = getFileID(relation.getLocation());
        
        Integer lhs = getLHS(relation.getLhs());
        DatabaseEntity rhs = entities.getEntity(relation.getRhs());
        
        if (lhs != null && rhs != null) {
          if (fileID == null) {
            inserter.addInsert(RelationsTable.makeInsert(relation.getType(), rhs.getRelationClass(), lhs, rhs.getEntityID(), projectID));
          } else {
            inserter.addInsert(RelationsTable.makeInsert(relation.getType(), rhs.getRelationClass(), lhs, rhs.getEntityID(), projectID, fileID, relation.getLocation()));
          }
          task.progress();
        }
      }
    }
    task.finish();
    
    task.start("Performing db insert");
    inserter.insert();
    task.finish();
    
    task.finish();
  }
  
//  private class TypeEntityResolver {
//    private Integer projectID;
//    
//    private Collection<RelationEX> newTypeRelations;
//    
//    public TypeEntityResolver(Integer projectID) {
//      this.projectID = projectID;
//      
//      newTypeRelations = new ArrayList<>();
//    }
//    
//    public void resolveInside(String fqn) {
//      if (!entityMap.containsKey(fqn)) {
//        logger.log(Level.WARNING, "Missing " + fqn + " from entity map");
//        if (pendingEntities.contains(fqn)) {
//          return;
//        }
//        pendingEntities.add(fqn);
//      }
//    }
//    
//    public void resolve(String fqn) {
//      if (pendingEntities.contains(fqn)) {
//        return;
//      }
//      
//      // Maybe it's in the map
//      if (entityMap.containsKey(fqn)) {
////        if (entityMap.get(fqn).resolveDuplicates(projectID)) {
////          counter.increment();
////        }
////        pendingEntities.add(fqn);
//        return; 
//      }
//      
//      // Should never be a method
//      if (TypeUtils.isMethod(fqn)) {
//        throw new IllegalArgumentException("Cannot resolve a method fqn: " + fqn);
//      } else {
//        if (TypeUtils.isArray(fqn)) {
//          Pair<String, Integer> arrayInfo = TypeUtils.breakArray(fqn);
//          
//          // Insert the array entity
//          inserter.addInsert(EntitiesTable.makeInsert(Entity.ARRAY, fqn, null, arrayInfo.getSecond(), projectID));
//          pendingEntities.add(fqn);
//          task.progress();
//          
//          // Add has elements of relation
//          resolve(arrayInfo.getFirst());
//          newTypeRelations.add(new RelationEX(Relation.HAS_ELEMENTS_OF, fqn, arrayInfo.getFirst(), null));
//
//          return;
//        }
//        
//        if (TypeUtils.isWildcard(fqn)) {
//          // Insert the wildcard entity
//          inserter.addInsert(EntitiesTable.makeInsert(Entity.WILDCARD, fqn, null, projectID));
//          pendingEntities.add(fqn);
//          task.progress();
//        
//          // If it's bounded, add the bound relation
//          if (!TypeUtils.isUnboundedWildcard(fqn)) {
//            String bound = TypeUtils.getWildcardBound(fqn);
//            resolve(bound);
//            if (TypeUtils.isLowerBound(fqn)) {
//              newTypeRelations.add(new RelationEX(Relation.HAS_LOWER_BOUND, fqn, bound, null));
//            } else {
//              newTypeRelations.add(new RelationEX(Relation.HAS_UPPER_BOUND, fqn, bound, null));
//            }
//          }
//          
//          return;
//        }
//        
//        if (TypeUtils.isTypeVariable(fqn)) {
//          // Insert the type variable entity
//          inserter.addInsert(EntitiesTable.makeInsert(Entity.TYPE_VARIABLE, fqn, null, projectID));
//          pendingEntities.add(fqn);
//          task.progress();
//          
//          // Insert the bound relations
//          for (String bound : TypeUtils.breakTypeVariable(fqn)) {
//            resolve(bound);
//            newTypeRelations.add(new RelationEX(Relation.HAS_UPPER_BOUND, fqn, bound, null));
//          }
//          
//          return;
//        }
//        
//        if (TypeUtils.isParametrizedType(fqn)) {
//          // Insert the parametrized type entity
//          inserter.addInsert(EntitiesTable.makeInsert(Entity.PARAMETERIZED_TYPE, fqn, null, projectID));
//          pendingEntities.add(fqn);
//          task.progress();
//          
//          // Add the has base type relation
//          String baseType = TypeUtils.getBaseType(fqn);
//          resolve(baseType);
//          newTypeRelations.add(new RelationEX(Relation.HAS_BASE_TYPE, fqn, baseType, null));
//          
//          // Insert the type arguments
//          for (String arg : TypeUtils.breakParametrizedType(fqn)) {
//            resolve(arg);
//            newTypeRelations.add(new RelationEX(Relation.HAS_TYPE_ARGUMENT, fqn, arg, null));
//          }
//          
//          return; 
//        }
//      }
//      
//      // Some external reference?
//      externalEntitiesQueryFqn.setValue(fqn);
//      try (TypedQueryResult result = externalEntitiesQuery.select()) {
//        if (result.hasNext()) {
//          DuplicatedDatabaseEntity entity = new DuplicatedDatabaseEntity();
//          while (result.next()) {
//            entity.addInstance(result.getResult(EntitiesTable.ENTITY_ID), result.getResult(EntitiesTable.PROJECT_ID));
//          }
//          if (entity.hasMultipleInstances()) {
//            // Insert the duplicate entity
//            inserter.addInsert(EntitiesTable.makeInsert(Entity.DUPLICATE, fqn, null, projectID));
//          }
//          entityMap.put(fqn, entity);
//          pendingEntities.add(fqn);
//          task.progress();
//          return;
//        }
//      }
//      
//      // Give up
//      pendingEntities.add(fqn);
//    }
//  }
  
//  private class DuplicatedDatabaseEntity extends DatabaseEntity {
//    private Collection<ClassedEntity> instances;
//
//    private class ClassedEntity {
//      Integer entityID;
//      RelationClass rClass;
//      
//      ClassedEntity(Integer entityID, RelationClass rClass) {
//        this.entityID = entityID;
//        this.rClass = rClass;
//      }
//    }
//    
//    public DuplicatedDatabaseEntity() {
//      super();
//    }
//    
//    public void addInstance(Integer entityID, Integer projectID) {
//      if (instances == null) {
//        instances = new ArrayList<>();
//      }
//      RelationClass newClass = libraryProjects.contains(projectID) ? RelationClass.JAVA_LIBRARY : RelationClass.EXTERNAL;
//      instances.add(new ClassedEntity(entityID, newClass));
//      if (instances.size() == 1) {
//        this.entityID = entityID;
//        rClass = newClass;
//      } else {
//        this.entityID = null;
//        if (rClass != newClass) {
//          rClass = RelationClass.MIXED_EXTERNAL;
//        }
//      }
//    }
//    
//    public boolean hasMultipleInstances() {
//      return instances != null && instances.size() > 1; 
//    }
//    
//    public void updateDuplicate(BatchInserter inserter, Integer entityID, Integer projectID) {
//      this.entityID = entityID;
//      for (ClassedEntity entity : instances) {
//        inserter.addInsert(RelationsTable.makeInsert(Relation.MATCHES, entity.rClass, entityID, entity.entityID, projectID));
//      }
//    }
//  }
}
