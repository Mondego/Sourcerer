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

import edu.uci.ics.sourcerer.model.Project;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class SmallProjectDB {
  private Integer projectID;
  private Project type;
  private String path;
  private String hash;

  public SmallProjectDB(Integer projectID, Project type, String path, String hash) {
    this.projectID = projectID;
    this.type = type;
    this.path = path;
    this.hash = hash;
  }
  
  public boolean firstStageCompleted() {
    if (type == Project.CRAWLED) {
      return "END_FIRST".equals(hash) || hash == null;
    } else {
      return "END_FIRST".equals(path) || path == null;
    }
  }
  
  public boolean completed() {
    if (type == Project.CRAWLED) {
      return hash == null;
    } else {
      return path == null;
    }
  }
  
  public Integer getProjectID() {
    return projectID;
  }
  
  public Project getType() {
    return type;
  }
  
  public String getPath() {
    return path;
  }
  
  public String getHash() {
    return hash;
  }
  
  public String toString() {
    return projectID.toString();
  }
}
