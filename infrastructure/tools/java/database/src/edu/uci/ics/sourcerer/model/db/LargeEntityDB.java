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
public class LargeEntityDB extends MediumEntityDB {
  private Integer modifiers;
  private Integer multi;
  private Integer fileID;
  private Integer offset;
  private Integer length;
  
  public LargeEntityDB(Integer entityID, Entity type, String fqn, Integer modifiers, Integer multi, Integer projectID, Integer fileID, Integer offset, Integer length) {
    super(entityID, type, fqn, projectID);
    this.modifiers = modifiers;
    this.multi = multi;
    this.fileID = fileID;
    this.offset = offset;
    this.length = length;
  }

  public Integer getModifiers() {
    return modifiers;
  }

  public Integer getMulti() {
    return multi;
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
