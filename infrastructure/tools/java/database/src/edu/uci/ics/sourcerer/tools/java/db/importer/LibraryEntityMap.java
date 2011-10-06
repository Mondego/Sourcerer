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

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import edu.uci.ics.sourcerer.tools.java.db.schema.EntitiesTable;
import edu.uci.ics.sourcerer.tools.java.db.schema.ProjectsTable;
import edu.uci.ics.sourcerer.tools.java.model.types.Entity;
import edu.uci.ics.sourcerer.tools.java.model.types.Modifier;
import edu.uci.ics.sourcerer.tools.java.model.types.Modifiers;
import edu.uci.ics.sourcerer.tools.java.model.types.Project;
import edu.uci.ics.sourcerer.tools.java.model.types.RelationClass;
import edu.uci.ics.sourcerer.util.io.TaskProgressLogger;
import edu.uci.ics.sourcerer.utils.db.DatabaseRunnable;
import edu.uci.ics.sourcerer.utils.db.QueryExecutor;
import edu.uci.ics.sourcerer.utils.db.sql.SelectQuery;
import edu.uci.ics.sourcerer.utils.db.sql.TypedQueryResult;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
final class LibraryEntityMap {
  private SynchronizedUnknownsMap unknowns;
  private Map<String, DatabaseEntity> entities;

  public LibraryEntityMap(TaskProgressLogger task, SynchronizedUnknownsMap unknowns) {
    this.unknowns = unknowns;
    entities = new HashMap<>();
    addLibraryEntities(task);
  }
  
  private final void addUnique(String fqn, DatabaseEntity entity) {
    if (entities.containsKey(fqn)) {
      logger.severe("Duplicate FQN: " + fqn);
    } else {
      entities.put(fqn, entity);
    }
  }
  
  private void addLibraryEntities(final TaskProgressLogger task) {
    new DatabaseRunnable() {
      @Override
      public void action() {
        task.start("Loading entities", "entities loaded");
        
        try (SelectQuery query = exec.makeSelectQuery(EntitiesTable.TABLE)) {
          query.addSelects(EntitiesTable.ENTITY_ID, EntitiesTable.FQN);
          query.andWhere(EntitiesTable.ENTITY_TYPE.compareEquals(Entity.PRIMITIVE));
          TypedQueryResult result = query.selectStreamed();
          while (result.next()) {
            DatabaseEntity entity = DatabaseEntity.make(result.getResult(EntitiesTable.ENTITY_ID), RelationClass.JAVA_LIBRARY);
            addUnique(result.getResult(EntitiesTable.FQN), entity);
          }
        }
        
        Collection<Integer> libraries = new ArrayList<>();
        try (SelectQuery query = exec.makeSelectQuery(ProjectsTable.TABLE)) {
          // Get the Java Library projectIDs
          query.addSelect(ProjectsTable.PROJECT_ID);
          query.andWhere(ProjectsTable.PROJECT_TYPE.compareEquals(Project.JAVA_LIBRARY));
          
          libraries.addAll(query.select().toCollection(ProjectsTable.PROJECT_ID));
        }
        
        try (SelectQuery query = exec.makeSelectQuery(EntitiesTable.TABLE)) {
          query.addSelects(EntitiesTable.ENTITY_ID, EntitiesTable.FQN, EntitiesTable.PARAMS, EntitiesTable.RAW_PARAMS);
          query.andWhere(
              EntitiesTable.PROJECT_ID.compareIn(libraries).and(
              EntitiesTable.ENTITY_TYPE.compareNotIn(EnumSet.of(Entity.PACKAGE, Entity.PARAMETER, Entity.LOCAL_VARIABLE))).and(
              EntitiesTable.MODIFIERS.compareNotEquals(Modifiers.make(Modifier.PRIVATE))));

          TypedQueryResult result = query.selectStreamed();
          while (result.next()) {
            DatabaseEntity entity = DatabaseEntity.make(result.getResult(EntitiesTable.ENTITY_ID), RelationClass.JAVA_LIBRARY);
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
  
  public synchronized DatabaseEntity getEntity(QueryExecutor exec, String fqn) {
    DatabaseEntity entity = entities.get(fqn);
    if (entity == null) {
      return unknowns.getUnknown(exec, fqn);
    } else {
      return entity;
    }
  }
  
  //TODO Make this actually do the virtual resolution
  public synchronized DatabaseEntity getVirtualEntity(QueryExecutor exec, String fqn) {
    DatabaseEntity entity = entities.get(fqn);
    if (entity == null) {
      return unknowns.getUnknown(exec, fqn);
    } else {
      return entity;
    }
  }
}
