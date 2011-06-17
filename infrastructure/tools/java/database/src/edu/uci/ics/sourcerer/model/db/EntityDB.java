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
public class EntityDB {
  private Integer entityID;
  private Entity type;
  private String fqn;
  private Integer modifiers;
  private Integer multi;
  private Integer projectID;
  private Integer fileID;
  private Integer offset;
  private Integer length;
  
  public EntityDB(Integer entityID, Entity type, String fqn, Integer modifiers, Integer multi, Integer projectID, Integer fileID, Integer offset, Integer length) {
    this.entityID = entityID;
    this.type = type;
    this.fqn = fqn;
    this.modifiers = modifiers;
    this.multi = multi;
    this.projectID = projectID;
    this.fileID = fileID;
    this.offset = offset;
    this.length = length;
  }

  public Integer getEntityID() {
    return entityID;
  }

  public Entity getType() {
    return type;
  }

  public String getFqn() {
    return fqn;
  }

  public Integer getModifiers() {
    return modifiers;
  }

  public Integer getMulti() {
    return multi;
  }

  public Integer getProjectID() {
    return projectID;
  }

  public Integer getFileID() {
    return fileID;
  }

  public Integer getOffset() {
    return offset;
  }

  public Integer getLength() {
    return length;
  }
}
