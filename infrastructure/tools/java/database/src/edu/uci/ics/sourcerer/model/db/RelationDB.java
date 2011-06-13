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

import edu.uci.ics.sourcerer.model.Relation;
import edu.uci.ics.sourcerer.model.RelationClass;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class RelationDB {
  private Integer relationID;
  private Relation relationType;
  private RelationClass relationClass;
  private Integer lhsEid;
  private Integer rhsEid;
  private Integer projectID;
  private Integer fileID;
  private Integer offset;
  private Integer length;

  public RelationDB(Integer relationID, Relation relationType, RelationClass relationClass, Integer lhsEid, Integer rhsEid, Integer projectID, Integer fileID, Integer offset, Integer length) {
    this.relationID = relationID;
    this.relationType = relationType;
    this.relationClass = relationClass;
    this.lhsEid = lhsEid;
    this.rhsEid = rhsEid;
    this.projectID = projectID;
    this.fileID = fileID;
    this.offset = offset;
    this.length = length;
  }

  public Integer getRelationID() {
    return relationID;
  }

  public Relation getRelationType() {
    return relationType;
  }

  public RelationClass getRelationClass() {
    return relationClass;
  }

  public Integer getLhsEid() {
    return lhsEid;
  }

  public Integer getRhsEid() {
    return rhsEid;
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
