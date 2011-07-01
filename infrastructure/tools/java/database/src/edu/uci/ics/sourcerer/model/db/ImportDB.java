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

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class ImportDB {
  private boolean isStatic;
  private boolean isOnDemand;
  private Integer eid;
  private Integer projectID;
  private Integer fileID;
  private Integer offset;
  private Integer length;

  
  public ImportDB(boolean isStatic, boolean isOnDemand, Integer eid, Integer projectID, Integer fileID, Integer offset, Integer length) {
    super();
    this.isStatic = isStatic;
    this.isOnDemand = isOnDemand;
    this.eid = eid;
    this.projectID = projectID;
    this.fileID = fileID;
    this.offset = offset;
    this.length = length;
  }

  public boolean isStatic() {
    return isStatic;
  }

  public boolean isOnDemand() {
    return isOnDemand;
  }

  public Integer getEid() {
    return eid;
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
