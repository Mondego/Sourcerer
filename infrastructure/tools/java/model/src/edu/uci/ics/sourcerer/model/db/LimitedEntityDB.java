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
package edu.uci.ics.sourcerer.model.db;

import edu.uci.ics.sourcerer.model.Entity;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class LimitedEntityDB {
  private String projectID;
  private String entityID;
  private Entity type;
  
  
  public LimitedEntityDB(String projectID, String entityID, Entity type) {
    this.projectID = projectID;
    this.entityID = entityID;
    this.type = type;
  }
  
  public String getProjectID() {
    return projectID;
  }
  
  public String getEntityID() {
    return entityID;
  }
   
  public Boolean isInternal(String projectID) {
    if (type == Entity.DUPLICATE) {
      return true;
    } else if (type.isInternalMeaningful()) {
      return projectID.equals(this.projectID);
    } else {
      return null;
    }
  }
  
  public boolean notDuplicate() {
    return type != Entity.DUPLICATE;
  }
}
