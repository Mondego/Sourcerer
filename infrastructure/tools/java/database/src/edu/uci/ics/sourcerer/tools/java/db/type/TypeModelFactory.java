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

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;


import edu.uci.ics.sourcerer.tools.java.db.schema.EntitiesTable;
import edu.uci.ics.sourcerer.tools.java.db.schema.ProjectsTable;
import edu.uci.ics.sourcerer.tools.java.db.schema.RelationsTable;
import edu.uci.ics.sourcerer.tools.java.model.types.Entity;
import edu.uci.ics.sourcerer.tools.java.model.types.Modifier;
import edu.uci.ics.sourcerer.tools.java.model.types.Modifiers;
import edu.uci.ics.sourcerer.tools.java.model.types.Project;
import edu.uci.ics.sourcerer.tools.java.model.types.Relation;
import edu.uci.ics.sourcerer.tools.java.model.types.RelationClass;
import edu.uci.ics.sourcerer.util.io.logging.TaskProgressLogger;
import edu.uci.ics.sourcerer.utils.db.DatabaseConnection;
import edu.uci.ics.sourcerer.utils.db.DatabaseConnectionFactory;
import edu.uci.ics.sourcerer.utils.db.DatabaseRunnable;
import edu.uci.ics.sourcerer.utils.db.QueryExecutor;
import edu.uci.ics.sourcerer.utils.db.sql.QualifiedTable;
import edu.uci.ics.sourcerer.utils.db.sql.ResultConstructor;
import edu.uci.ics.sourcerer.utils.db.sql.ResultConstructor2;
import edu.uci.ics.sourcerer.utils.db.sql.SelectQuery;
import edu.uci.ics.sourcerer.utils.db.sql.TypedQueryResult;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class TypeModelFactory {
  private TypeModelFactory() {}
  
  public static TypeModel createJavaLibraryTypeModel() {
    return create(new JavaLibraryCreator());
  }
  
  private static TypeModel create(Creator creator) {
    try (DatabaseConnection conn = DatabaseConnectionFactory.INSTANCE.create()) {
      if (conn.open()) {
        return creator.create(conn.getExecutor());
      }
    }
    return null;
  }
  
  private static ResultConstructor2<ModeledEntity> CONSTRUCTOR = new ResultConstructor2<ModeledEntity>() {
    @Override
    public void addSelects(SelectQuery query) {
      query.addSelect(
          EntitiesTable.ENTITY_ID, 
          EntitiesTable.FQN, 
          EntitiesTable.ENTITY_TYPE, 
          EntitiesTable.PROJECT_ID,
          EntitiesTable.PARAMS,
          EntitiesTable.RAW_PARAMS);
    }
    
    @Override
    public ModeledEntity constructResult(TypedQueryResult result) {
      Entity type = result.getResult(EntitiesTable.ENTITY_TYPE);
      switch (type) {
        case CLASS:
        case INTERFACE:
        case ENUM:
        case ANNOTATION:
          return new ModeledType(
              result.getResult(EntitiesTable.ENTITY_ID), 
              result.getResult(EntitiesTable.FQN), 
              type, 
              result.getResult(EntitiesTable.PROJECT_ID));
        case METHOD:
        case ANNOTATION_ELEMENT:
          return new ModeledMethod(
              result.getResult(EntitiesTable.ENTITY_ID), 
              result.getResult(EntitiesTable.FQN), 
              type, 
              result.getResult(EntitiesTable.PROJECT_ID),
              result.getResult(EntitiesTable.PARAMS),
              result.getResult(EntitiesTable.RAW_PARAMS));
        case PACKAGE:
        case FIELD:
        case ENUM_CONSTANT:
          return new ModeledEntity(
              result.getResult(EntitiesTable.ENTITY_ID), 
              result.getResult(EntitiesTable.FQN), 
              type, 
              result.getResult(EntitiesTable.PROJECT_ID));
        default:
          return null;
      }
    }
  };
  
  private static abstract class Creator {
    protected TaskProgressLogger task = TaskProgressLogger.get();
    protected QueryExecutor exec;
    protected TypeModel model = new TypeModel();
    
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
  }

  private static class JavaLibraryCreator extends Creator {
    private Collection<Integer> libraries = null;
    @Override
    public void loadEntities() {
      // Load the primitive types
      try (SelectQuery select = exec.createSelectQuery(EntitiesTable.TABLE)) {
        CONSTRUCTOR.addSelects(select);
        select.andWhere(EntitiesTable.ENTITY_TYPE.compareEquals(Entity.PRIMITIVE));
        for (ModeledEntity entity : select.select().toIterable(CONSTRUCTOR)) {
          model.add(entity);
          task.progress();
        }
      }
      
      try (SelectQuery select = exec.createSelectQuery(ProjectsTable.TABLE)) {
        // Get the Java Library projectIDs
        select.addSelect(ProjectsTable.PROJECT_ID);
        select.andWhere(ProjectsTable.PROJECT_TYPE.compareEquals(Project.JAVA_LIBRARY));
        
        libraries = select.select().toCollection(ProjectsTable.PROJECT_ID);
      }
      
      try (SelectQuery select= exec.createSelectQuery(EntitiesTable.TABLE)) {
        CONSTRUCTOR.addSelects(select);
        select.andWhere(
            EntitiesTable.PROJECT_ID.compareIn(libraries),
            EntitiesTable.ENTITY_TYPE.compareIn(EnumSet.of(Entity.PACKAGE, Entity.CLASS, Entity.INTERFACE, Entity.ENUM, Entity.ANNOTATION, Entity.CONSTRUCTOR, Entity.METHOD, Entity.ANNOTATION_ELEMENT, Entity.ENUM_CONSTANT, Entity.FIELD)));

        for (ModeledEntity entity : select.select().toIterable(CONSTRUCTOR)) {
          model.add(entity);
          task.progress();
        }
      }
    }

    @Override
    protected void loadRelations() {
      Map<Integer, Integer> pMapping = new HashMap<>();
      try (SelectQuery query = exec.createSelectQuery(RelationsTable.TABLE)) {
        query.addSelect(RelationsTable.LHS_EID, RelationsTable.RHS_EID);
        query.andWhere(RelationsTable.PROJECT_ID.compareIn(libraries), RelationsTable.RELATION_TYPE.compareEquals(Relation.HAS_BASE_TYPE));
        
        TypedQueryResult result = query.select();
        while (result.next()) {
          pMapping.put(result.getResult(RelationsTable.LHS_EID), result.getResult(RelationsTable.RHS_EID));
          task.progress();
        }
      }
      
      try (SelectQuery query = exec.createSelectQuery(RelationsTable.TABLE)) {
        query.addSelect(RelationsTable.LHS_EID, RelationsTable.RHS_EID);
        query.andWhere(RelationsTable.PROJECT_ID.compareIn(libraries), RelationsTable.RELATION_TYPE.compareEquals(Relation.HAS_BASE_TYPE));
        query.andWhere(RelationsTable.PROJECT_ID.compareIn(libraries), RelationsTable.RELATION_TYPE.compareIn(EnumSet.of(Relation.EXTENDS, Relation.IMPLEMENTS)));
        
        TypedQueryResult result = query.select();
        while (result.next()) {
          Integer lhs = result.getResult(RelationsTable.LHS_EID);
          Integer rhs = result.getResult(RelationsTable.RHS_EID);
          task.progress();
        }
      }
    }
  }
}
