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
package edu.uci.ics.sourcerer.tools.java.component.identifier.stats;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import edu.uci.ics.sourcerer.tools.java.component.model.fqn.AbstractFqnNode;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class CountingFqnNode extends AbstractFqnNode<CountingFqnNode> {
  private Set<String> projects;
  private int count;

  protected CountingFqnNode(String name, CountingFqnNode parent) {
    super(name, parent);
    projects = Collections.emptySet();
    count = 0;
  }

  static CountingFqnNode createRoot() {
    return new CountingFqnNode(null, null);
  }
  
  @Override
  protected CountingFqnNode create(String name, AbstractFqnNode<?> parent) {
    return new CountingFqnNode(name, (CountingFqnNode) parent);
  }
  
  void add(String fqn, String project) {
    CountingFqnNode node = getChild(fqn, '.');
    if (node.projects.isEmpty()) {
      node.projects = new HashSet<>();
    }
    node.projects.add(project);
    node.count++;
  }
  
  public int getTotalCount() {
    return count;
  }
  
  public int getProjectCount() {
    return projects.size();
  }
  
  @Override
  public int compareTo(CountingFqnNode other) {
    int cmp = Integer.compare(projects.size(), other.projects.size());
    if (cmp == 0) {
      return super.compareTo(other);
    } else {
      return cmp;
    }
  }
}