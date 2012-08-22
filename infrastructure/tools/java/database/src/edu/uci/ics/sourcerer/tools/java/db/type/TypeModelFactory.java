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
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import edu.uci.ics.sourcerer.tools.java.db.schema.EntitiesTable;
import edu.uci.ics.sourcerer.tools.java.db.schema.ProjectsTable;
import edu.uci.ics.sourcerer.tools.java.db.schema.RelationsTable;
import edu.uci.ics.sourcerer.tools.java.model.types.Entity;
import edu.uci.ics.sourcerer.tools.java.model.types.Project;
import edu.uci.ics.sourcerer.tools.java.model.types.Relation;
import edu.uci.ics.sourcerer.util.io.logging.TaskProgressLogger;
import edu.uci.ics.sourcerer.utils.db.DatabaseConnection;
import edu.uci.ics.sourcerer.utils.db.DatabaseConnectionFactory;
import edu.uci.ics.sourcerer.utils.db.QueryExecutor;
import edu.uci.ics.sourcerer.utils.db.sql.ResultConstructor2;
import edu.uci.ics.sourcerer.utils.db.sql.SelectQuery;
import edu.uci.ics.sourcerer.utils.db.sql.TypedQueryResult;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class TypeModelFactory {
  private TypeModelFactory() {}
  
  public static TypeModel createJavaLibraryTypeModel() {
    return create(new JavaLibraryModelCreator());
  }
  
  public static TypeModel createJarTypeModel(Collection<Integer> jarIDs, TypeModel libraryModel) {
    return create(new JarModelCreator(jarIDs, libraryModel));
  }
  
  public static TypeModel createProjectTypeModel(Integer projectID, TypeModel jarModel) {
    return create(new ProjectModelCreator(projectID, jarModel));
  }
  
  private static TypeModel create(ModelCreator creator) {
    try (DatabaseConnection conn = DatabaseConnectionFactory.INSTANCE.create()) {
      if (conn.open()) {
        return creator.create(conn.getExecutor());
      }
    }
    return null;
  }
  
  private static abstract class ModelCreator {
    protected final TaskProgressLogger task;
    protected final TypeModel model;
    protected QueryExecutor exec;
    
    protected ModelCreator(TypeModel parentModel) {
      task = TaskProgressLogger.get();
      model = new TypeModel(parentModel);
    }
    
    // The ENTITIES
    protected final Set<Entity> ENTITIES = EnumSet.of(
        Entity.PACKAGE, 
        Entity.CLASS, 
        Entity.INTERFACE, 
        Entity.ENUM, 
        Entity.ANNOTATION, 
        Entity.CONSTRUCTOR, 
        Entity.METHOD, 
        Entity.ANNOTATION_ELEMENT, 
        Entity.ENUM_CONSTANT, 
        Entity.FIELD,
        Entity.INITIALIZER,
        Entity.PARAMETER,
        Entity.LOCAL_VARIABLE,
        Entity.PARAMETERIZED_TYPE,
        Entity.ARRAY,
        Entity.TYPE_VARIABLE,
        Entity.DUPLICATE,
        Entity.VIRTUAL_DUPLICATE,
        Entity.WILDCARD);
    protected static ResultConstructor2<ModeledEntity> ENTITY_CONSTRUCTOR = new ResultConstructor2<ModeledEntity>() {
      @Override
      public void addSelects(SelectQuery query) {
        query.addSelect(
            EntitiesTable.ENTITY_ID, 
            EntitiesTable.FQN, 
            EntitiesTable.ENTITY_TYPE,
            EntitiesTable.FILE_ID,
            EntitiesTable.PROJECT_ID,
            EntitiesTable.PARAMS,
            EntitiesTable.RAW_PARAMS,
            EntitiesTable.MODIFIERS);
      }
      
      @Override
      public ModeledEntity constructResult(TypedQueryResult result) {
        Entity type = result.getResult(EntitiesTable.ENTITY_TYPE);
        Integer entityID = result.getResult(EntitiesTable.ENTITY_ID);
        String fqn = result.getResult(EntitiesTable.FQN);
        Integer fileID = result.getResult(EntitiesTable.FILE_ID);
        Integer projectID = result.getResult(EntitiesTable.PROJECT_ID);
        
        switch (type) {
          case CLASS:
          case INTERFACE:
          case ENUM:
          case ANNOTATION:
            return new ModeledDeclaredType(entityID, result.getResult(EntitiesTable.MODIFIERS), fqn, type, fileID, projectID);
          case CONSTRUCTOR:
          case METHOD:
          case ANNOTATION_ELEMENT:
            return new ModeledMethod(entityID, result.getResult(EntitiesTable.MODIFIERS), fqn, type, fileID, projectID,               
                result.getResult(EntitiesTable.PARAMS),
                result.getResult(EntitiesTable.RAW_PARAMS));
          case FIELD:
          case ENUM_CONSTANT:
          case INITIALIZER:
          case PARAMETER:
          case LOCAL_VARIABLE:
            return new ModeledStructuralEntity(entityID, result.getResult(EntitiesTable.MODIFIERS), fqn, type, fileID, projectID);
          case PACKAGE:
            return new ModeledStructuralEntity(entityID, null, fqn, type, fileID, projectID);
          case PRIMITIVE:
          case UNKNOWN:
          case WILDCARD:
          case TYPE_VARIABLE:
            return new ModeledEntity(entityID, fqn, type, projectID);
          case PARAMETERIZED_TYPE:
            return new ModeledParametrizedType(entityID, fqn, projectID);
          case ARRAY:
            return new ModeledArrayType(entityID, fqn, projectID);
          case DUPLICATE:
          case VIRTUAL_DUPLICATE:
            return new ModeledDuplicate(entityID, fqn, type, projectID);
          default:
            return null;
        }
      }
    };
    
    // The RELATIONS
    private abstract class RelationProcessor <LHS extends ModeledEntity, RHS extends ModeledEntity> {
      private final Relation type;
      private final Class<LHS> lhsType;
      private final Class<RHS> rhsType;
      private final boolean noNull;
      
      protected RelationProcessor(Relation type, Class<LHS> lhsType, Class<RHS> rhsType, boolean noNull) {
        this.type = type;
        this.lhsType = lhsType;
        this.rhsType = rhsType;
        this.noNull = noNull;
      }
      
      protected abstract void process(LHS lhs, RHS rhs);
      
      public void process(Integer lhsEid, Integer rhsEid) {
        ModeledEntity lhs = model.get(lhsEid);
        ModeledEntity rhs = model.get(rhsEid);
        if (lhs != null && rhs != null) {
          boolean lhsValid = lhsType.isInstance(lhs);
          boolean rhsValid = rhsType.isInstance(rhs);
          if (lhsValid && rhsValid) {
            process(lhsType.cast(lhs), rhsType.cast(rhs));
          } else if (lhsValid) {
            task.report(Level.SEVERE, type.name() + " relation RHS invalid type: " + rhs);
          } else if (rhsValid) {
            task.report(Level.SEVERE, type.name() + " relation LHS invalid type: " + lhs);
          } else {
            task.report(Level.SEVERE, type.name() + " relation LHS & RHS invalid types: " + lhs);
          }
        } else if (noNull) {
          if (lhs == null && rhs == null) {
            task.report(Level.SEVERE, type.name() + " relation missing LHS & RHS: " + lhsEid + "->" + rhsEid);
          } else if (lhs == null) {
            task.report(Level.SEVERE, type.name() + " relation missing LHS: " + lhsEid + "->" + rhsEid);
          } else {
            task.report(Level.SEVERE, type.name() + " relation missing RHS: " + lhsEid + "->" + rhsEid);
          }
        }
      }
    }
    protected final Map<Relation, RelationProcessor<?, ?>> RELATIONS;
    {
      RELATIONS = new EnumMap<>(Relation.class);
      RELATIONS.put(Relation.CONTAINS, new RelationProcessor<ModeledStructuralEntity, ModeledStructuralEntity>(Relation.CONTAINS, ModeledStructuralEntity.class, ModeledStructuralEntity.class, false) {
        @Override
        protected void process(ModeledStructuralEntity lhs, ModeledStructuralEntity rhs) {
          lhs.addChild(rhs);
        }
      });
      RELATIONS.put(Relation.EXTENDS, new RelationProcessor<ModeledDeclaredType, ModeledEntity>(Relation.EXTENDS, ModeledDeclaredType.class, ModeledEntity.class, true) {
        @Override
        protected void process(ModeledDeclaredType lhs, ModeledEntity rhs) {
          lhs.setSuperclass(rhs);
        }
      });
      RELATIONS.put(Relation.IMPLEMENTS, new RelationProcessor<ModeledDeclaredType, ModeledEntity>(Relation.IMPLEMENTS, ModeledDeclaredType.class, ModeledEntity.class, true) {
        @Override
        protected void process(ModeledDeclaredType lhs, ModeledEntity rhs) {
          lhs.addInterface(rhs);
        }
      });
      RELATIONS.put(Relation.HAS_BASE_TYPE, new RelationProcessor<ModeledParametrizedType, ModeledDeclaredType>(Relation.HAS_BASE_TYPE, ModeledParametrizedType.class, ModeledDeclaredType.class, true) {
        @Override
        protected void process(ModeledParametrizedType lhs, ModeledDeclaredType rhs) {
          lhs.setBaseType(rhs);
        }
      });
      RELATIONS.put(Relation.HAS_TYPE_ARGUMENT, new RelationProcessor<ModeledParametrizedType, ModeledEntity>(Relation.HAS_TYPE_ARGUMENT, ModeledParametrizedType.class, ModeledEntity.class, true) {
        @Override
        protected void process(ModeledParametrizedType lhs, ModeledEntity rhs) {
          lhs.addTypeArgument(rhs);
        }
      });
      RELATIONS.put(Relation.HAS_ELEMENTS_OF, new RelationProcessor<ModeledArrayType, ModeledEntity>(Relation.HAS_ELEMENTS_OF, ModeledArrayType.class, ModeledEntity.class, true) {
        @Override
        protected void process(ModeledArrayType lhs, ModeledEntity rhs) {
          lhs.setElementType(rhs);
        }
      });
      RELATIONS.put(Relation.MATCHES, new RelationProcessor<ModeledDuplicate, ModeledEntity>(Relation.MATCHES, ModeledDuplicate.class, ModeledEntity.class, true) {
        @Override
        protected void process(ModeledDuplicate lhs, ModeledEntity rhs) {
          lhs.addMatch(rhs);
        }
      });
    }
    
    
    public TypeModel create(QueryExecutor exec) {
      this.exec = exec;
      
      task.start("Loading entities", "entities loaded");
      loadEntities();
      task.finish();
      
      task.start("Loading relations", "relations loaded");
      loadRelations();
      task.finish();
      
      return model;
    }
    
    protected abstract void loadEntities();
    
    protected abstract void loadRelations();
    
    protected void processRelations(TypedQueryResult result) {
      while (result.next()) {
        Integer lhsEid = result.getResult(RelationsTable.LHS_EID);
        Integer rhsEid = result.getResult(RelationsTable.RHS_EID);
        Relation type = result.getResult(RelationsTable.RELATION_TYPE);
        
        RelationProcessor<?, ?> processor = RELATIONS.get(type);
        if (processor != null) {
          processor.process(lhsEid, rhsEid);
        } else {
          throw new IllegalStateException("Unexpected relation type: " + type);
        }
        task.progress();
      }
    }
  }

  private static class JavaLibraryModelCreator extends ModelCreator {
    private Collection<Integer> libraries;
    
    private JavaLibraryModelCreator() {
      super(null);
    }
    
    @Override
    public void loadEntities() {
      // Load the primitives and unknowns
      try (SelectQuery select = exec.createSelectQuery(EntitiesTable.TABLE)) {
        ENTITY_CONSTRUCTOR.addSelects(select);
        select.andWhere(EntitiesTable.ENTITY_TYPE.compareIn(EnumSet.of(Entity.PRIMITIVE, Entity.UNKNOWN)));
        for (ModeledEntity entity : select.select().toIterable(ENTITY_CONSTRUCTOR)) {
          model.add(entity);
          task.progress();
        }
      }
      
      // Get the Java Library projectIDs
      try (SelectQuery select = exec.createSelectQuery(ProjectsTable.TABLE)) {
        select.addSelect(ProjectsTable.PROJECT_ID);
        select.andWhere(ProjectsTable.PROJECT_TYPE.compareEquals(Project.JAVA_LIBRARY));
        
        libraries = select.select().toCollection(ProjectsTable.PROJECT_ID);
      }
      
      // Load the entities
      try (SelectQuery select = exec.createSelectQuery(EntitiesTable.TABLE)) {
        ENTITY_CONSTRUCTOR.addSelects(select);
        select.andWhere(
            EntitiesTable.PROJECT_ID.compareIn(libraries),
            EntitiesTable.ENTITY_TYPE.compareIn(ENTITIES));

        for (ModeledEntity entity : select.select().toIterable(ENTITY_CONSTRUCTOR)) {
          model.add(entity);
          task.progress();
        }
      }
    }

    @Override
    protected void loadRelations() {
      // Load relations
      try (SelectQuery query = exec.createSelectQuery(RelationsTable.TABLE)) {
        query.addSelect(RelationsTable.LHS_EID, RelationsTable.RHS_EID, RelationsTable.RELATION_TYPE);
        query.andWhere(RelationsTable.PROJECT_ID.compareIn(libraries), RelationsTable.RELATION_TYPE.compareIn(RELATIONS.keySet()));
        
        processRelations(query.select());
      }
    }
  }
  
  private static class JarModelCreator extends ModelCreator {
    private final Collection<Integer> jarIDs;
    
    private JarModelCreator(Collection<Integer> jarIDs, TypeModel libraryModel) {
      super(libraryModel);
      this.jarIDs = jarIDs;
    }
    
    @Override
    public void loadEntities() {
      // Load the entities
      try (SelectQuery select= exec.createSelectQuery(EntitiesTable.TABLE)) {
        ENTITY_CONSTRUCTOR.addSelects(select);
        select.andWhere(
            EntitiesTable.PROJECT_ID.compareIn(jarIDs),
            EntitiesTable.ENTITY_TYPE.compareIn(ENTITIES));

        for (ModeledEntity entity : select.select().toIterable(ENTITY_CONSTRUCTOR)) {
          model.add(entity);
          task.progress();
        }
      }
    }

    @Override
    protected void loadRelations() {
      // Load relations
      try (SelectQuery query = exec.createSelectQuery(RelationsTable.TABLE)) {
        query.addSelect(RelationsTable.LHS_EID, RelationsTable.RHS_EID, RelationsTable.RELATION_TYPE);
        query.andWhere(RelationsTable.PROJECT_ID.compareIn(jarIDs), RelationsTable.RELATION_TYPE.compareIn(RELATIONS.keySet()));
        
        processRelations(query.select());
      }
    }
  }
  
  private static class ProjectModelCreator extends ModelCreator {
    private final Integer projectID;
    
    private ProjectModelCreator(Integer projectID, TypeModel libraryModel) {
      super(libraryModel);
      this.projectID = projectID;
    }
    
    @Override
    public void loadEntities() {
      // Load the entities
      try (SelectQuery select= exec.createSelectQuery(EntitiesTable.TABLE)) {
        ENTITY_CONSTRUCTOR.addSelects(select);
        select.andWhere(
            EntitiesTable.PROJECT_ID.compareEquals(projectID),
            EntitiesTable.ENTITY_TYPE.compareIn(ENTITIES));

        for (ModeledEntity entity : select.select().toIterable(ENTITY_CONSTRUCTOR)) {
          model.add(entity);
          task.progress();
        }
      }
    }

    @Override
    protected void loadRelations() {
      // Load relations
      try (SelectQuery query = exec.createSelectQuery(RelationsTable.TABLE)) {
        query.addSelect(RelationsTable.LHS_EID, RelationsTable.RHS_EID, RelationsTable.RELATION_TYPE);
        query.andWhere(RelationsTable.PROJECT_ID.compareEquals(projectID), RelationsTable.RELATION_TYPE.compareIn(RELATIONS.keySet()));
        
        processRelations(query.select());
      }
    }
  }
}
