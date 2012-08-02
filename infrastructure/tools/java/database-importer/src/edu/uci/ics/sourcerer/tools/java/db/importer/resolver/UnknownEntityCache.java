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

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import edu.uci.ics.sourcerer.tools.java.db.schema.EntitiesTable;
import edu.uci.ics.sourcerer.tools.java.db.schema.ProjectsTable;
import edu.uci.ics.sourcerer.tools.java.model.types.Entity;
import edu.uci.ics.sourcerer.tools.java.model.types.RelationClass;
import edu.uci.ics.sourcerer.util.io.logging.TaskProgressLogger;
import edu.uci.ics.sourcerer.util.type.TypeUtils;
import edu.uci.ics.sourcerer.utils.db.DatabaseRunnable;
import edu.uci.ics.sourcerer.utils.db.QueryExecutor;
import edu.uci.ics.sourcerer.utils.db.sql.SelectQuery;
import edu.uci.ics.sourcerer.utils.db.sql.TypedQueryResult;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class UnknownEntityCache  {
  private Integer unknownsProject;
  private Map<String, ModeledEntity> unknowns;
  
  private UnknownEntityCache() {
    unknowns = new HashMap<>();
  }
  
  public static UnknownEntityCache makeUnknownEntityCache(final TaskProgressLogger task) {
    final UnknownEntityCache cache = new UnknownEntityCache();
    new DatabaseRunnable() {
      @Override
      public void action() {
        task.start("Building unknown entities cache");
        
        task.start("Loading unknown entities", "unknown entities loaded");
        try (SelectQuery query = exec.createSelectQuery(ProjectsTable.TABLE)) {
          query.addSelect(ProjectsTable.PROJECT_ID);
          query.andWhere(ProjectsTable.NAME.compareEquals(ProjectsTable.UNKNOWNS_PROJECT));
          cache.unknownsProject = query.select().toSingleton(ProjectsTable.PROJECT_ID, false);
        }
        
        try (SelectQuery query = exec.createSelectQuery(EntitiesTable.TABLE)) {
          query.addSelect(EntitiesTable.ENTITY_ID, EntitiesTable.FQN, EntitiesTable.PARAMS);
          query.andWhere(EntitiesTable.PROJECT_ID.compareEquals(cache.unknownsProject));
          TypedQueryResult result = query.select();
          while (result.next()) {
            String fqn = result.getResult(EntitiesTable.FQN);
            String params = result.getResult(EntitiesTable.PARAMS);
            Integer entityID = result.getResult(EntitiesTable.ENTITY_ID);
            if (params != null) {
              fqn += params;
            } 
            cache.unknowns.put(fqn, new ModeledEntity(fqn, Entity.UNKNOWN, entityID, RelationClass.UNKNOWN));
            task.progress();
          }
        }
        task.finish();
        
        task.finish();
      }
    }.run(); 
    return cache;
  }
  
  synchronized ModeledEntity getUnknown(QueryExecutor exec, String fqn) {
    ModeledEntity entity = unknowns.get(fqn);
    if (entity == null) {
      Integer entityID = null;
      if (TypeUtils.isMethod(fqn)) {
        String name = TypeUtils.getMethodName(fqn);
        entityID = exec.insertWithKey(EntitiesTable.createInsert(Entity.UNKNOWN, name, fqn.substring(name.length()), unknownsProject));
      } else {
        entityID = exec.insertWithKey(EntitiesTable.createInsert(Entity.UNKNOWN, fqn, unknownsProject));
      }
      if (entityID == null) {
        logger.log(Level.SEVERE, "Error inserting unknown: " + fqn);
        return null;
      } else {
        entity = new ModeledEntity(fqn, Entity.UNKNOWN, entityID, RelationClass.UNKNOWN);
        unknowns.put(fqn, entity);
        return entity;
      }
    } else {
      return entity;
    }
  }
}
