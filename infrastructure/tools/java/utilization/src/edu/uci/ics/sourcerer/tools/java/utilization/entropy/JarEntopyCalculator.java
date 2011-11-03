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

import static edu.uci.ics.sourcerer.util.io.Logging.logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import edu.uci.ics.sourcerer.tools.java.repo.model.JarFile;
import edu.uci.ics.sourcerer.tools.java.utilization.model.FqnFragment;
import edu.uci.ics.sourcerer.tools.java.utilization.model.Jar;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public interface JarEntopyCalculator {
  public double compute(Jar jar);  

//  static EntropicJar calculateEntropy(FullyQualifiedNameMap fqnMap, JarFile jar) {
//    EntropicJar entropic = new EntropicJar(jar);
//    FqnUsageTree<?> tree = FqnUsageTreeBuilder.build(jar);
//    
//    Deque<FqnUsageTreeNode<?>> current = new LinkedList<>();
//    Deque<FqnUsageTreeNode<?>> next = new LinkedList<>();
//    current.add(tree.getRoot());
//    int level = 1;
//    
//    while (!current.isEmpty()) {
//      while (!current.isEmpty()) {
//        FqnUsageTreeNode<?> node = current.pop();
//        for (FqnUsageTreeNode<?> child : node.getChildren()) {
//          if (!child.getSources().isEmpty()) {
//            entropic.fqns.add(fqnMap.makeFQN(child.getFQN(), entropic));
//          }
//          next.push(child);
//        }
//      }
//      if (!next.isEmpty()) {
//        entropic.entropy += Math.log(next.size()) / level;
//        Deque<FqnUsageTreeNode<?>> temp = current;
//        current = next;
//        next = temp;
//      }
//      level++;
//    }
//    
//    return entropic;
//  }
}
