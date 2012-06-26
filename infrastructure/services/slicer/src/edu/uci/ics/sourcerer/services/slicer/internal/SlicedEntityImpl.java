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
package edu.uci.ics.sourcerer.services.slicer.internal;

import edu.uci.ics.sourcerer.services.slicer.model.SlicedEntity;
import edu.uci.ics.sourcerer.tools.java.db.schema.EntitiesTable;
import edu.uci.ics.sourcerer.tools.java.model.types.Entity;
import edu.uci.ics.sourcerer.tools.java.model.types.Modifiers;
import edu.uci.ics.sourcerer.utils.db.sql.TypedQueryResult;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
class SlicedEntityImpl implements SlicedEntity {
  private final Integer entityID;
  private final String fqn;
  private final Modifiers modifiers;
  private final Integer projectID;
  private final Entity entityType;
  private final Integer fileID;
  private final Integer offset;
  private final Integer length;
  
  SlicedEntityImpl(TypedQueryResult result) {
    entityID = result.getResult(EntitiesTable.ENTITY_ID);
    fqn = result.getResult(EntitiesTable.FQN);
    modifiers = result.getResult(EntitiesTable.MODIFIERS);
    projectID = result.getResult(EntitiesTable.PROJECT_ID);
    entityType = result.getResult(EntitiesTable.ENTITY_TYPE);
    fileID = result.getResult(EntitiesTable.FILE_ID);
    offset = result.getResult(EntitiesTable.OFFSET);
    length = result.getResult(EntitiesTable.LENGTH);
  }

  @Override
  public Integer getEntityID() {
    return entityID;
  }
  
  @Override
  public String getFqn() {
    return fqn;
  }

  @Override
  public Modifiers getModifiers() {
    return modifiers;
  }
  
  @Override
  public Integer getProjectID() {
    return projectID;
  }

  @Override
  public Entity getEntityType() {
    return entityType;
  }

  @Override
  public Integer getFileID() {
    return fileID;
  }

  @Override
  public Integer getOffset() {
    return offset;
  }

  @Override
  public Integer getLength() {
    return length;
  }
  
  @Override
  public String toString() {
    return fqn + " (" + entityID + ")";
  }
}
