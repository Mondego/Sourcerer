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

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public abstract class RelationsImporter extends NewDatabaseImporter {
  private LibraryEntityMap libraries;
  protected EntityMap entities;
  
  protected RelationsImporter(String taskName, LibraryEntityMap libraries) {
    super(taskName);
    this.libraries = libraries;
  }
  
  protected final void loadEntityMap(Integer projectID, Collection<Integer> externalProjects) {
    task.start("Populating entity map", "entities loaded");
    
    entities = new EntityMap(task, exec, projectID, externalProjects, libraries);

    task.finish();
  }
  
  protected Integer getLHS(String fqn) {
    DatabaseEntity entity = entities.getEntity(fqn);
    if (entity == null) {
      logger.severe("Missing lhs entity: " + fqn);
      return null;
    } else {
      return entity.getEntityID();
    }
  }
}
