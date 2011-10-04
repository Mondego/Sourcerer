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

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import edu.uci.ics.sourcerer.tools.java.db.schema.EntitiesTable;
import edu.uci.ics.sourcerer.tools.java.db.schema.ProjectsTable;
import edu.uci.ics.sourcerer.tools.java.model.types.Entity;
import edu.uci.ics.sourcerer.tools.java.model.types.RelationClass;
import edu.uci.ics.sourcerer.util.io.TaskProgressLogger;
import edu.uci.ics.sourcerer.utils.db.DatabaseRunnable;
import edu.uci.ics.sourcerer.utils.db.QueryExecutor;
import edu.uci.ics.sourcerer.utils.db.sql.SelectQuery;
import edu.uci.ics.sourcerer.utils.db.sql.TypedQueryResult;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
class SynchronizedUnknownsMap  {
  private Integer unknownsProject;
  private volatile Map<String, DatabaseEntity> unknowns;
  
  SynchronizedUnknownsMap(TaskProgressLogger task) {
    populateMap(task);
  }
  
  private void populateMap(final TaskProgressLogger task) {
    new DatabaseRunnable() {
      @Override
      public void action() {
        task.start("Loading unknown entities", "unknown entities loaded");
        try (SelectQuery query = exec.makeSelectQuery(ProjectsTable.TABLE)) {
          query.addSelect(ProjectsTable.PROJECT_ID);
          query.andWhere(ProjectsTable.NAME.compareEquals(ProjectsTable.UNKNOWNS_PROJECT));
          unknownsProject = query.select().toSingleton(ProjectsTable.PROJECT_ID);
        }
        
        unknowns = new HashMap<>();
        try (SelectQuery query = exec.makeSelectQuery(EntitiesTable.TABLE)) {
          query.addSelects(EntitiesTable.ENTITY_ID, EntitiesTable.FQN);
          query.andWhere(EntitiesTable.PROJECT_ID.compareEquals(unknownsProject));
          TypedQueryResult result = query.selectStreamed();
          while (result.next()) {
            unknowns.put(result.getResult(EntitiesTable.FQN), DatabaseEntity.make(result.getResult(EntitiesTable.ENTITY_ID), RelationClass.UNKNOWN));
            task.progress();
          }
        }
        task.finish();
      }
    }.run(); 
  }
  
  synchronized void add(QueryExecutor exec, String fqn) {
    if (!contains(fqn)) {
      Integer eid = exec.insertWithKey(EntitiesTable.makeInsert(Entity.UNKNOWN, fqn, null, unknownsProject));
      if (eid == null) {
        logger.log(Level.SEVERE, "Missing eid for unknown: " + fqn);
      } else {
        unknowns.put(fqn, DatabaseEntity.make(eid, RelationClass.UNKNOWN));
      }
    }
  }
  
  synchronized boolean contains(String fqn) {
    return unknowns.containsKey(fqn);
  }
  
  synchronized DatabaseEntity getUnknown(String fqn) {
    DatabaseEntity entity = unknowns.get(fqn);
    if (entity == null) {
      return null;
    } else {
      return entity;
    }
  }
}
