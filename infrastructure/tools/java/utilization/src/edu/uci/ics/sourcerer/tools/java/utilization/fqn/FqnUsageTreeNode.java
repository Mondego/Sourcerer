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
//package edu.uci.ics.sourcerer.tools.java.utilization.fqn;
//
//import java.util.Collection;
//import java.util.Collections;
//import java.util.Comparator;
//import java.util.Deque;
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.LinkedList;
//import java.util.Map;
//import java.util.Set;
//
///**
// * @author Joel Ossher (jossher@uci.edu)
// */
//public class FqnUsageTreeNode<Source> {
//  public static <Source> Comparator<FqnUsageTreeNode<Source>> makeTopFragmentComparator() {
//    return new Comparator<FqnUsageTreeNode<Source>>() {
//      @Override
//      public int compare(FqnUsageTreeNode<Source> o1, FqnUsageTreeNode<Source> o2) {
//        if (o1 == o2) {
//          return 0;
//        }
//        int comp = o1.getSecondarySources().size() - o2.getSecondarySources().size();
//        if (comp == 0) {
//          comp = o1.name.compareTo(o2.name);
//        }
//        if (comp == 0) {
//          comp = o1.getFQN().compareTo(o2.getFQN());
//        }
//        if (comp == 0) {
//          throw new IllegalStateException(o1 + " and " + o2 + " seem identical.");
//        }
//        return comp;
//      }
//    };
//  }
//  
//  public static <Source> Comparator<FqnUsageTreeNode<Source>> makeTopFqnComparator() {
//    return new Comparator<FqnUsageTreeNode<Source>>() {
//      @Override
//      public int compare(FqnUsageTreeNode<Source> o1, FqnUsageTreeNode<Source> o2) {
//        if (o1 == o2) {
//          return 0;
//        }
//        int comp = o1.getSources().size() - o2.getSources().size();
//        if (comp == 0) {
//          comp = o1.name.compareTo(o2.name);
//        }
//        if (comp == 0) {
//          comp = o1.getFQN().compareTo(o2.getFQN());
//        }
//        if (comp == 0) {
//          throw new IllegalStateException(o1 + " and " + o2 + " seem identical.");
//        }
//        return comp;
//      }
//    };
//  }
//}
