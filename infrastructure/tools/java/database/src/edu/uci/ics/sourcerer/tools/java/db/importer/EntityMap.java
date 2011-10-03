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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import edu.uci.ics.sourcerer.tools.java.model.types.RelationClass;
import edu.uci.ics.sourcerer.util.io.TaskProgressLogger;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class EntityMap {
  private SynchronizedEntityMap libraries;
  private Map<String, DatabaseEntity> entities;
  
  EntityMap(SynchronizedEntityMap libraries) {
    this.libraries = libraries;
    entities = Collections.emptyMap();
  }
  
  void populate(TaskProgressLogger task, Collection<Integer> projects) {
    entities = new HashMap<>();
    SynchronizedEntityMap.populateMap(entities, task, projects, RelationClass.EXTERNAL);
  }
  
  DatabaseEntity getEntity(String fqn) {
    DatabaseEntity entity = entities.get(fqn);
    if (entity == null) {
      return libraries.getEntity(fqn);
    } else {
      return entity;
    }
  }
}
