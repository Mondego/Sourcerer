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
package edu.uci.ics.sourcerer.tools.java.utilization.fqn;

import java.util.Collection;
import java.util.Deque;
import java.util.LinkedList;
import java.util.PriorityQueue;

import edu.uci.ics.sourcerer.util.Pair;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class FqnUsageTree<Source> {
  private FqnUsageTreeNode<Source> root;
  
  FqnUsageTree() {
    root = new FqnUsageTreeNode<>();
  }
  
  void addSlashFqn(String fqn, Source source) {
    FqnUsageTreeNode<Source> node = root;
    int start = 0;
    int slash = fqn.indexOf('/');
    while (slash != -1) {
      node = node.getChild(fqn.substring(start, slash));
      start = slash + 1;
      slash = fqn.indexOf('/', start);
    }
    node.getChild(fqn.substring(start)).addSource(source);
  }
  
  public FqnUsageTreeNode<Source> getRoot() {
    return root;
  }
    
  public Pair<Integer, Integer> getFqnCounts() {
    int fqns = 0;
    int uniqueFqns = 0;
    Deque<FqnUsageTreeNode<Source>> stack = new LinkedList<>();
    stack.push(root);
    while (!stack.isEmpty()) {
      FqnUsageTreeNode<Source> node = stack.pop();
      int sources = node.getSources().size();
      fqns += sources;
      if (sources > 0) {
        uniqueFqns++;
      }
      stack.addAll(node.getChildren());
    }
    return new Pair<>(fqns, uniqueFqns);
  }
  
  public Collection<FqnUsageTreeNode<Source>> getTopFragments(int count) {
    PriorityQueue<FqnUsageTreeNode<Source>> queue = new PriorityQueue<>(count, FqnUsageTreeNode.<Source>makeTopFragmentComparator());
    
    Deque<FqnUsageTreeNode<Source>> stack = new LinkedList<>();
    stack.push(root);
    while (!stack.isEmpty()) {
      FqnUsageTreeNode<Source> node = stack.pop();
      for (FqnUsageTreeNode<Source> child : node.getChildren()) {
        if (queue.size() == count) {
          if (queue.comparator().compare(child, queue.element()) > 0) {
            queue.poll();
            queue.offer(child);
            stack.push(child);
          }
        } else {
          queue.offer(child);
          stack.push(child);
        }
      }
    }
    
    LinkedList<FqnUsageTreeNode<Source>> result = new LinkedList<>();
    while (!queue.isEmpty()) {
      result.addFirst(queue.poll());
    }
    return result;
  }
  
  public Collection<FqnUsageTreeNode<Source>> getTopFqns(int count) {
    PriorityQueue<FqnUsageTreeNode<Source>> queue = new PriorityQueue<>(count, FqnUsageTreeNode.<Source>makeTopFqnComparator());
    
    Deque<FqnUsageTreeNode<Source>> stack = new LinkedList<>();
    stack.push(root);
    while (!stack.isEmpty()) {
      FqnUsageTreeNode<Source> node = stack.pop();
      for (FqnUsageTreeNode<Source> child : node.getChildren()) {
        if (queue.size() == count) {
          if (queue.comparator().compare(child, queue.element()) > 0) {
            queue.poll();
            queue.offer(child);
          }
        } else {
          queue.offer(child);
        }
        stack.push(child);
      }
    }

    LinkedList<FqnUsageTreeNode<Source>> result = new LinkedList<>();
    while (!queue.isEmpty()) {
      result.addFirst(queue.poll());
    }
    return result;
  }
}
