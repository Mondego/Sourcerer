///* 
// * Sourcerer: an infrastructure for large-scale source code analysis.
// * Copyright (C) by contributors. See CONTRIBUTORS.txt for full list.
// *
// * This program is free software: you can redistribute it and/or modify
// * it under the terms of the GNU General Public License as published by
// * the Free Software Foundation, either version 3 of the License, or
// * (at your option) any later version.
//
// * This program is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// * GNU General Public License for more details.
//
// * You should have received a copy of the GNU General Public License
// * along with this program. If not, see <http://www.gnu.org/licenses/>.
// */
//package edu.uci.ics.sourcerer.clusterer.fqn;
//
//import static edu.uci.ics.sourcerer.util.io.Logging.logger;
//
//import java.io.BufferedReader;
//import java.io.BufferedWriter;
//import java.io.File;
//import java.io.FileReader;
//import java.io.FileWriter;
//import java.io.IOException;
//import java.util.Deque;
//import java.util.PriorityQueue;
//import java.util.logging.Level;
//
//import edu.uci.ics.sourcerer.util.Helper;
//import edu.uci.ics.sourcerer.util.io.FileUtils;
//import edu.uci.ics.sourcerer.util.io.Properties;
//import edu.uci.ics.sourcerer.util.io.Property;
//import edu.uci.ics.sourcerer.util.io.properties.StringProperty;
//
///**
// * @author Joel Ossher (jossher@uci.edu)
// */
//public class FqnTree {
//  public static Property<String> FQN_TREE = new StringProperty("fqn-tree", "fqn-tree.txt", "Clusterer", "File for storing fqn tree");
//  
//  private FqnFragment root;
//  
//  public FqnTree() {
//    root = FqnFragment.getRootFragment();
//  }
//  
////  private Project getProject(String projectID) {
////    Integer id = Integer.parseInt(projectID);
////    Project proj = projects.get(id);
////    if (proj == null) {
////      proj = new Project(id);
////      projects.put(id, proj);
////    }
////    return proj;
////  }
//  
//  public void addFqn(String fqn, String projectID) {
////    Project project = getProject(projectID);
//    int id = Integer.parseInt(projectID);
//    FqnFragment parentFragment = root;
//    for (String name : fqn.split("\\.")) {
//      parentFragment = parentFragment.addChild(name, id);
//    }
//  }
//  
//  public void writeToDisk() {
//    BufferedWriter bw = null;
//    try {
//      bw = new BufferedWriter(new FileWriter(new File(Properties.OUTPUT.getValue(), FQN_TREE.getValue())));
//      root.writeToDisk(bw);
//    } catch(IOException e) {
//      logger.log(Level.SEVERE, "Error writing to disk.", e);
//    } finally {
//      FileUtils.close(bw);
//    }
//  }
//  
//  public void readFromDisk() {
//    BufferedReader br = null;
//    try {
//      br = new BufferedReader(new FileReader(new File(Properties.INPUT.getValue(), FQN_TREE.getValue())));
//      FqnFragment parent = null;
//      FqnFragment last = root;
//      for (String line = br.readLine(); line != null; line = br.readLine()) {
//        if (line.equals("+")) {
//          parent = last;
//        } else if (line.equals("-")) {
//          parent = parent.getParent();
//        } else {
//          String[] parts = line.split(" ");
//          last = parent.addChild(parts[0]);
//          for (int i = 1; i < parts.length; i++) {
//            last.addProject(Integer.parseInt(parts[i]));
//          }
//        }
//      }
//    } catch (IOException e) {
//      logger.log(Level.SEVERE, "Error reading from disk.", e);
//    } finally {
//      FileUtils.close(br);
//    }
//  }
//  
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
//}
