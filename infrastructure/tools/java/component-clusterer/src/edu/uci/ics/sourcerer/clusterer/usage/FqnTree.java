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
package edu.uci.ics.sourcerer.clusterer.usage;

import static edu.uci.ics.sourcerer.util.io.logging.Logging.logger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Deque;
import java.util.PriorityQueue;
import java.util.logging.Level;

import edu.uci.ics.sourcerer.util.Helper;
import edu.uci.ics.sourcerer.util.io.FileUtils;
import edu.uci.ics.sourcerer.util.io.arguments.IOFileArgumentFactory;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class FqnTree {
  public static IOFileArgumentFactory FQN_TREE = new IOFileArgumentFactory("fqn-tree", "fqn-tree.txt", "File for storing fqn tree");
  
  private FqnFragment root;
  
  public FqnTree() {
    root = FqnFragment.getRootFragment();
  }
    
  public void addFqn(String fqn, int entityID) {
    // Skip ones where the FQN itself is unknown
    if (!fqn.startsWith("(1UNKNOWN)")) {
      // Split off the method parameters
      int idx = fqn.indexOf('(');
      String mainFqn = fqn.substring(0, idx);
      FqnFragment parentFragment = root;
      for (String name : mainFqn.split("\\.")) {
        if (name.indexOf('(') != -1) {
          logger.log(Level.SEVERE, "Misplaced paren: " + name + " from " + fqn);
        }
        parentFragment = parentFragment.addChild(name, entityID);
      }
      parentFragment.addChild(fqn.substring(idx), entityID);
    }
  }
  
  public void writeToDisk() {
    BufferedWriter bw = null;
    try {
      bw = FileUtils.getBufferedWriter(FQN_TREE);
      root.writeToDisk(bw);
    } catch(IOException e) {
      logger.log(Level.SEVERE, "Error writing to disk.", e);
    } finally {
      FileUtils.close(bw);
    }
  }
  
  public void readFromDisk() {
    BufferedReader br = null;
    try {
      br = FileUtils.getBufferedReader(FQN_TREE);
      FqnFragment parent = null;
      FqnFragment last = root;
      boolean paren = false;
      for (String line = br.readLine(); line != null; line = br.readLine()) {
        if (line.equals("+")) {
          if (paren) {
            logger.log(Level.SEVERE, "Paren has child! " + last.getFqn());
          }
          parent = last;
        } else if (line.equals("-")) {
          parent = parent.getParent();
        } else {
          String[] parts = line.split(" ");
          last = parent.getChild(parts[0]);
          paren = parts[0].indexOf('(') != -1;
          for (int i = 2; i < parts.length; i++) {
            last.addID(Integer.parseInt(parts[i]));
          }
          last.setReferenceCount(Integer.parseInt(parts[1]));
        }
      }
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Error reading from disk.", e);
    } finally {
      FileUtils.close(br);
    }
  }

  public FqnFragment[] getTopReferencedFragments(int count) {
    PriorityQueue<FqnFragment> queue = new PriorityQueue<FqnFragment>(count + 1, FqnFragment.getReferenceComparator());
    Deque<FqnFragment> stack = Helper.newStack();
    stack.push(root);
    while (!stack.isEmpty()) {
      FqnFragment top = stack.pop();
      queue.add(top);
      if (queue.size() > count) {
        queue.poll();
      }
      FqnFragment[] children = top.getChildren();
      if (children != null) {
        for (int i = 0; i < children.length && children[i] != null; i++) {
          stack.push(children[i]);
        }
      }
    }
    count = Math.min(count, queue.size());
    FqnFragment[] retval = new FqnFragment[count];
    for (int i = count - 1; i >= 0; i--) {
      retval[i] = queue.poll();
    }
    return retval;
  }
//  public FqnFragment[] getTopFqns(int count) {
//    PriorityQueue<FqnFragment> queue = new PriorityQueue<FqnFragment>(count + 1);
//    Deque<FqnFragment> stack = Helper.newStack();
//    stack.push(root);
//    while (!stack.isEmpty()) {
//      FqnFragment top = stack.pop();
//      if (top.isTopLevelClass()) {
//        queue.add(top);
//        if (queue.size() > count) {
//          queue.poll();
//        }
//      }
//      FqnFragment[] children = top.getChildren();
//      if (children != null) {
//        for (int i = 0; i < children.length && children[i] != null; i++) {
//          stack.push(children[i]);
//        }
//      }
//    }
//    count = Math.min(count, queue.size());
//    FqnFragment[] retval = new FqnFragment[count];
//    for (int i = count - 1; i >= 0; i--) {
//      retval[i] = queue.poll();
//    }
//    return retval;
//  }
}
