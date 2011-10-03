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

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import edu.uci.ics.sourcerer.tools.java.db.schema.EntitiesTable;
import edu.uci.ics.sourcerer.tools.java.model.types.Entity;
import edu.uci.ics.sourcerer.tools.java.model.types.RelationClass;
import edu.uci.ics.sourcerer.utils.db.sql.SelectQuery;
import edu.uci.ics.sourcerer.utils.db.sql.TypedQueryResult;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public abstract class RelationsImporter extends NewDatabaseImporter {
  protected Map<String, DatabaseEntity> entityMap;
  
  protected RelationsImporter(String taskName) {
    super(taskName);
    entityMap = new HashMap<>();
  }
  
  protected final void loadEntityMap(Integer projectID) {
    task.start("Populating entity map", "entities loaded");
    
    try (SelectQuery query = exec.makeSelectQuery(EntitiesTable.TABLE)) {
      query.addSelects(EntitiesTable.ENTITY_ID, EntitiesTable.FQN, EntitiesTable.RAW_SIGNATURE);
      query.andWhere(EntitiesTable.PROJECT_ID.compareEquals(projectID).and(
          EntitiesTable.ENTITY_TYPE.compareNotIn(EnumSet.of(Entity.PARAMETER, Entity.LOCAL_VARIABLE))));
  
      TypedQueryResult result = query.select();
      
      while (result.next()) {
        String fqn = result.getResult(EntitiesTable.FQN);
        DatabaseEntity entity = entityMap.get(fqn);
        if (entity == null) {
          entity = DatabaseEntity.make(result.getResult(EntitiesTable.ENTITY_ID), RelationClass.INTERNAL);
          String rawSignature = result.getResult(EntitiesTable.RAW_SIGNATURE);
          if (rawSignature != null) {
            entityMap.put(TypeUtils.getMethodName(fqn) + rawSignature, entity);
          }
          entityMap.put(fqn, entity);
        } else {
          logger.severe("FQN conflict: " + fqn);
        }
        task.progress();
      }
    }
    task.finish();
  }
  
  protected Integer getLHS(String fqn) {
    DatabaseEntity entity = entityMap.get(fqn);
    if (entity == null) {
      logger.severe("Missing lhs entity: " + fqn);
      return null;
    } else {
      return entity.getEntityID();
    }
  }
}
