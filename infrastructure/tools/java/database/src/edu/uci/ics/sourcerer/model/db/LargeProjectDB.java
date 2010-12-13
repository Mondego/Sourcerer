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
 * @author Joel Ossher (josshed@uci.edu)
 */
public final class LargeProjectDB extends SmallProjectDB {
  private String name;
  private String description;
  private String version;
  private String group;
  private boolean hasSource;
  
  public LargeProjectDB(Integer projectID, Project type, String name, String description, String version, String group, String path, String hash, boolean hasSource) {
    super(projectID, type, path, hash);
    this.name = name;
    this.description = description;
    this.version = version;
    this.group = group;
    this.hasSource = hasSource;
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public String getVersion() {
    return version;
  }

  public String getGroup() {
    return group;
  }

  public boolean hasSource() {
    return hasSource;
  }
  
  public String toString() {
    return "project " + name + "(" + getProjectID() + ")";
  }
}
