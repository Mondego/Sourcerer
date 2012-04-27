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
package edu.uci.ics.sourcerer.tools.java.cloning.pairwise;

import java.util.Collection;
import java.util.Map;

import edu.uci.ics.sourcerer.tools.java.cloning.method.Project;
import edu.uci.ics.sourcerer.util.Helper;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public final class ProjectMatchSet {
  private Map<Project, ProjectMatches> map;
  
  public ProjectMatchSet(Collection<Project> projects) {
    map = Helper.newHashMap();
    for (Project project : projects) {
      map.put(project, new ProjectMatches(project));
    }
  }
  
  public MatchingProjects getFileMatch(Project a, Project b) {
    return map.get(a).getFileMatch(b);
  }
  
  public Collection<ProjectMatches> getProjectMatches() {
    return map.values();
  }
}
