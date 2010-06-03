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
package edu.uci.ics.sourcerer.db.tools;

import java.util.Map;

import edu.uci.ics.sourcerer.db.schema.DatabaseAccessor;
import edu.uci.ics.sourcerer.db.util.DatabaseConnection;
import edu.uci.ics.sourcerer.model.Entity;
import edu.uci.ics.sourcerer.model.db.LimitedEntityDB;
import edu.uci.ics.sourcerer.model.db.SlightlyLessLimitedEntityDB;
import edu.uci.ics.sourcerer.util.Helper;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class SynchronizedUnknownsMap extends DatabaseAccessor {
  private String unknownsProject;
  private volatile Map<String, String> unknowns;
  
  public SynchronizedUnknownsMap(DatabaseConnection connection) {
    super(connection);
    
    unknownsProject = projectsTable.getUnknownsProject();
    
    unknowns = Helper.newHashMap();
    for (SlightlyLessLimitedEntityDB entity : entitiesTable.getUnknownEntities(unknownsProject)) {
      unknowns.put(entity.getFqn(), entity.getEntityID());
    }
  }
  
  protected synchronized void add(String fqn) {
    if (!contains(fqn)) {
      unknowns.put(fqn, entitiesTable.forceInsertUnknown(fqn, unknownsProject));
    }
  }
  
  protected synchronized boolean contains(String fqn) {
    return unknowns.containsKey(fqn);
  }
  
  protected synchronized LimitedEntityDB getUnknown(String fqn) {
    return new LimitedEntityDB(unknownsProject, unknowns.get(fqn), Entity.UNKNOWN);
  }
}
