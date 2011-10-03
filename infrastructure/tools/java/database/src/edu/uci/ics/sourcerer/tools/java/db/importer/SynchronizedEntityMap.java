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

import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import edu.uci.ics.sourcerer.tools.java.db.schema.EntitiesTable;
import edu.uci.ics.sourcerer.tools.java.model.types.Entity;
import edu.uci.ics.sourcerer.tools.java.model.types.Modifier;
import edu.uci.ics.sourcerer.tools.java.model.types.Modifiers;
import edu.uci.ics.sourcerer.tools.java.model.types.RelationClass;
import edu.uci.ics.sourcerer.util.io.TaskProgressLogger;
import edu.uci.ics.sourcerer.utils.db.DatabaseRunnable;
import edu.uci.ics.sourcerer.utils.db.sql.SelectQuery;
import edu.uci.ics.sourcerer.utils.db.sql.TypedQueryResult;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class SynchronizedEntityMap {
  private SynchronizedUnknownsMap unknowns;
  private volatile Map<String, DatabaseEntity> entities;

  SynchronizedEntityMap(TaskProgressLogger task, Collection<Integer> projects, SynchronizedUnknownsMap unknowns) {
    this.unknowns = unknowns;
    entities = new HashMap<>();
    populateMap(entities, task, projects, RelationClass.JAVA_LIBRARY);
  }
  
  static void populateMap(final Map<String, DatabaseEntity> entities, final TaskProgressLogger task, final Collection<Integer> projects, final RelationClass rClass) {
    new DatabaseRunnable() {
      @Override
      public void action() {
        task.start("Loading entities", "entities loaded");

        try (SelectQuery query = exec.makeSelectQuery(EntitiesTable.TABLE)) {
          query.addSelects(EntitiesTable.ENTITY_ID, EntitiesTable.FQN, EntitiesTable.RAW_SIGNATURE);
          query.andWhere(EntitiesTable.PROJECT_ID.compareIn(projects).and(
              EntitiesTable.MODIFIERS.compareEquals(Modifiers.make(Modifier.PUBLIC)).or(
                  EntitiesTable.MODIFIERS.compareEquals(Modifiers.make(Modifier.PROTECTED)))).and(
                      EntitiesTable.ENTITY_TYPE.compareNotIn(EnumSet.of(Entity.PARAMETER, Entity.LOCAL_VARIABLE))));

          TypedQueryResult result = query.selectStreamed();
          while (result.next()) {
            String fqn = result.getResult(EntitiesTable.FQN);
            DatabaseEntity entity = DatabaseEntity.make(result.getResult(EntitiesTable.ENTITY_ID), rClass);
            String rawSignature = result.getResult(EntitiesTable.RAW_SIGNATURE);
            if (rawSignature != null) {
              entities.put(TypeUtils.getMethodName(fqn) + rawSignature, entity);
            }
            entities.put(fqn, entity);
            task.progress();
          }
        }
        task.finish();
      }
    }.run(); 
  }
  
  synchronized DatabaseEntity getEntity(String fqn) {
    DatabaseEntity entity = entities.get(fqn);
    if (entity == null) {
      return unknowns.getUnknown(fqn);
    } else {
      return entity;
    }
  }
}
