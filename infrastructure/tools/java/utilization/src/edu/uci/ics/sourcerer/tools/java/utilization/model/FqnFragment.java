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
package edu.uci.ics.sourcerer.tools.java.utilization.model;

import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeMap;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class FqnFragment {
  private final String name;
  private final FqnFragment parent;
  private TreeMap<String, FqnFragment> children;
  private Set<Jar> jars;
  
  private FqnFragment(String name, FqnFragment parent) {
    this.name = name;
    this.parent = parent;    
  }
  
  static FqnFragment makeRoot() {
    return new FqnFragment(null, null);
  }
  
  FqnFragment getChild(String name) {
    FqnFragment child = null;
    if (children == null) {
      children = new TreeMap<>();
    } else {
      child = children.get(name);
    }
    if (child == null) {
      child = new FqnFragment(name, this);
      children.put(name, child);
    }
    return child;
  }
  
  void addJar(Jar jar) {
    if (jars == null) {
      jars = new HashSet<>();
    }
    jars.add(jar);
  }
  
  void deleteJar(Jar jar) {
    jars.remove(jar);
    // Delete this fragment
    if (jars.isEmpty() && (children == null || children.isEmpty())) {
      parent.deleteChild(name);
    }
  }
  
  private void deleteChild(String name) {
    children.remove(name);
    if (parent != null && jars.isEmpty() && children.isEmpty()) {
      parent.deleteChild(name);
    }
  }
  
  public FqnFragment getParent() {
    return parent;
  }
  
  public String getName() {
    return name;
  }
  
  public Set<Jar> getJars() {
    return jars;
  }
  
  public String getFqn() {
    if (parent == null) {
      return null;
    } else {
      Deque<FqnFragment> stack = new LinkedList<>();
      FqnFragment node = parent;
      while (node.parent != null) {
        stack.push(node);
        node = node.parent;
      }
      StringBuilder fqn = new StringBuilder();
      while (!stack.isEmpty()) {
        fqn.append(stack.pop().name).append(".");
      }
      fqn.append(name);
      return fqn.toString();
    }
  }
  
  @Override
  public String toString() {
    return getFqn();
  }
}
