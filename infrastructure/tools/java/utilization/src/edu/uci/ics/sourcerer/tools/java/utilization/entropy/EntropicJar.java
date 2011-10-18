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
package edu.uci.ics.sourcerer.tools.java.utilization.entropy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.LinkedList;

import edu.uci.ics.sourcerer.tools.java.repo.model.JarFile;
import edu.uci.ics.sourcerer.tools.java.utilization.fqn.FqnUsageTree;
import edu.uci.ics.sourcerer.tools.java.utilization.fqn.FqnUsageTreeBuilder;
import edu.uci.ics.sourcerer.tools.java.utilization.fqn.FqnUsageTreeNode;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class EntropicJar {
  private JarFile jar;
  private Collection<FullyQualifiedName> fqns;
  private double entropy;
  
  private EntropicJar(JarFile jar) {
    this.jar = jar;
    fqns = new ArrayList<>();
    entropy = 0;
  }
  
  static EntropicJar calculateEntropy(FullyQualifiedNameMap fqnMap, JarFile jar) {
    EntropicJar entropic = new EntropicJar(jar);
    FqnUsageTree<?> tree = FqnUsageTreeBuilder.build(jar);
    
    Deque<FqnUsageTreeNode<?>> current = new LinkedList<>();
    Deque<FqnUsageTreeNode<?>> next = new LinkedList<>();
    current.add(tree.getRoot());
    int level = 1;
    
    while (!current.isEmpty()) {
      while (!current.isEmpty()) {
        FqnUsageTreeNode<?> node = current.pop();
        for (FqnUsageTreeNode<?> child : node.getChildren()) {
          if (!child.getSources().isEmpty()) {
            entropic.fqns.add(fqnMap.makeFQN(child.getFQN(), entropic));
          }
          next.push(child);
        }
      }
      if (!next.isEmpty()) {
        entropic.entropy += Math.log(next.size()) / level;
        Deque<FqnUsageTreeNode<?>> temp = current;
        current = next;
        next = temp;
      }
    }
    
    return entropic;
  }
  
  public JarFile getJar() {
    return jar;
  }
  
  public double getEntropy() {
    return entropy;
  }
  
  @Override
  public String toString() {
    return jar + " " + entropy;
  }
}
