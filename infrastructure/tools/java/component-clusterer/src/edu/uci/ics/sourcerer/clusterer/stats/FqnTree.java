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
package edu.uci.ics.sourcerer.clusterer.stats;

import java.util.Map;
import java.util.Set;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class FqnTree {
  private Fragment root;
  
  public FqnTree() {
    root = new Fragment();
  }
  
  public void addFqn(String fqn, Project project) {
    Fragment parentFragment = root;
    for (String name : fqn.split("\\.")) {
      parentFragment = parentFragment.addChild(name, project);
    }
  }
  
  private class Fragment {
    private String name;
    private Map<String, Fragment> children;
    private Set<Project> projects;
    
    public Fragment() {}
    
    public Fragment addChild(String name, Project project) {
      Fragment child = children.get(name);
      if (child == null) {
        child = new Fragment();
        child.name = name;
        children.put(name, child);
      }
      child.addProject(project);
      return child;
    }
    
    public void addProject(Project project) {
      projects.add(project);
    }
  }
}
