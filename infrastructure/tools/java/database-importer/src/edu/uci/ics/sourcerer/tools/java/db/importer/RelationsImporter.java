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

import static edu.uci.ics.sourcerer.util.io.logging.Logging.logger;

import java.util.logging.Level;

import edu.uci.ics.sourcerer.tools.java.db.importer.resolver.JavaLibraryTypeModel;
import edu.uci.ics.sourcerer.tools.java.db.importer.resolver.ModeledEntity;
import edu.uci.ics.sourcerer.tools.java.db.importer.resolver.ProjectTypeModel;
import edu.uci.ics.sourcerer.tools.java.db.importer.resolver.UnknownEntityCache;
import edu.uci.ics.sourcerer.tools.java.model.types.RelationClass;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public abstract class RelationsImporter extends DatabaseImporter {
  protected JavaLibraryTypeModel javaModel;
  protected ProjectTypeModel projectModel;
  protected UnknownEntityCache unknowns;
  
  protected RelationsImporter(String taskName, JavaLibraryTypeModel javaModel, UnknownEntityCache unknowns) {
    super(taskName);
    this.javaModel = javaModel;
    this.unknowns = unknowns;
  }
    
  protected Integer getLHS(String fqn, Integer projectID) {
    if (fqn.indexOf('#') >= 0) {
      task.report(Level.WARNING, "Skipping param: " + fqn);
      return null;
    } else {
      ModeledEntity entity = projectModel.getEntity(fqn);
      if (entity.getRelationClass() != RelationClass.INTERNAL) {
        logger.severe("Invalid lhs entity: " + entity);
        return null;
      } else {
        return entity.getEntityID(exec, projectID);
      }
    }
  }
  
  protected Integer getDeclaredEntity(String fqn, Integer projectID) {
    ModeledEntity entity = projectModel.getDeclaredEntity(fqn);
    if (entity == null) {
      logger.severe("Invalid declared entity: " + fqn);
      return null;
    } else {
      return entity.getEntityID(exec, projectID);
    }
  }
}
