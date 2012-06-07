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
package edu.uci.ics.sourcerer.tools.java.component.identifier.internal;

import java.util.LinkedList;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import edu.uci.ics.sourcerer.tools.java.component.model.cluster.Cluster;
import edu.uci.ics.sourcerer.tools.java.component.model.cluster.ClusterCollection;
import edu.uci.ics.sourcerer.tools.java.component.model.jar.JarCollection;
import edu.uci.ics.sourcerer.tools.java.component.model.jar.JarSet;
import edu.uci.ics.sourcerer.tools.java.component.model.jar.VersionedFqnNode;
import edu.uci.ics.sourcerer.util.Averager;
import edu.uci.ics.sourcerer.util.io.arguments.Argument;
import edu.uci.ics.sourcerer.util.io.arguments.DoubleArgument;
import edu.uci.ics.sourcerer.util.io.logging.TaskProgressLogger;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class ClusterIdentifier {
  private ClusterIdentifier() {}
  
  public static final Argument<Double> COMPATIBILITY_THRESHOLD = new DoubleArgument("compatibility-threshold", 1., "");
  
  private static boolean areCompatible(Cluster one, Cluster two) {
    // Do a pairwise comparison of every FQN. Calculate the conditional
    // probability of each FQN in B appearing given each FQN in A and average.
    // Then compute the reverse. Both values must be above the threshold.
    
    double threshold = COMPATIBILITY_THRESHOLD.getValue();
    // If the threshold is greater than 1, no match is possible
    if (threshold > 1.) {
      return false;
    } 
    // If it's 0 or lower, then everything matches
    else if (threshold <= 0.) {
      return true;
    }
    // If the threshold is 1, we can short-circuit this comparison
    // The primary jars must match exactly (can do == because JarSet is interned)
    else if (threshold >= 1.) {
      return one.getJars() == two.getJars();
    } 
    // Now we have to actually do the comparison
    else {
      // If there's no intersection between the JarSet, return false
      // There may be other optimizations that can be done to cut out cases where the full comparison has to be done
      if (one.getJars().getIntersectionSize(two.getJars()) == 0) {
        return false;
      } else {
        Averager<Double> otherGivenThis = Averager.create();
        Averager<Double> thisGivenOther = Averager.create();
      
        for (VersionedFqnNode fqn : one.getCoreFqns()) {
          for (VersionedFqnNode otherFqn : two.getCoreFqns()) {
            JarSet fqnJars = fqn.getJars();
            JarSet otherFqnJars = otherFqn.getJars();
            // Conditional probability of other given this
            // # shared jars / total jars in this
            otherGivenThis.addValue((double) fqnJars.getIntersectionSize(otherFqnJars) / fqnJars.size());
            // Conditional probabilty for this given other
            // # shared jars / total jars in other
            thisGivenOther.addValue((double) otherFqnJars.getIntersectionSize(fqnJars) / otherFqnJars.size());
          }
        }
        return otherGivenThis.getMean() >= threshold && thisGivenOther.getMean() >= threshold;
      }
    }
  }
  
  private static boolean areFullyCompatible(Cluster one, Cluster two) {
//    if (one.getJars() == two.getJars()) {
//      return true;
//    } else if (one.getJars().isSubset(two.getJars()) && two.getJars().isSubset(one.getJars())) {
//      System.out.println("foo");
//      return true;
//    } else {
//      return false;
//    }
    return one.getJars() == two.getJars();
  }
  
  public static ClusterCollection identifyFullyMatchingClusters(JarCollection jars) {
    TaskProgressLogger task = TaskProgressLogger.get();
    
    task.start("Identifying fully matching clusters in " + jars.size() + " jar files");
    
    Multimap<VersionedFqnNode, Cluster> clusterMap = ArrayListMultimap.create();
    
    // Explore the tree in post-order
    for (VersionedFqnNode parent : jars.getRoot().getPostOrderIterable()) {
      // If it's a leaf, then it starts out as a trivial cluster
      if (!parent.hasChildren()) {
        clusterMap.put(parent, Cluster.create(parent));
      } 
      // If it has children, see what children always co-occur
      else {
        // Check if the node itself should be a cluster
        if (parent.getJars().size() > 0) {
          clusterMap.put(parent, Cluster.create(parent));
        }
        for (VersionedFqnNode child : parent.getChildren()) {
          // Match the clusters from the children with the current clusters for this node
          for (Cluster childCluster : clusterMap.get(child)) {
            Cluster match = null;
            // Which clusters have already been found for this fragment?
            for (Cluster parentCluster : clusterMap.get(parent)) {
              if (areFullyCompatible(childCluster, parentCluster)) {
                // We found a match!
                match = parentCluster;
                // There can be only one, so break
                break;
              }
            }
            // If we found a match, merge
            if (match != null) {
              match.mergeCore(childCluster);
            }
            // Otherwise, promote the cluster
            else {
              clusterMap.put(parent, childCluster);
            }
          }
          clusterMap.removeAll(child);
        }
      }
    }
    
    ClusterCollection clusters = ClusterCollection.create(clusterMap.get(jars.getRoot()));
    
    task.report("Identified " + clusters.size() + " fully matching clusters");
    
    task.finish();
    
    return clusters;
  }
  
  public static ClusterCollection identifyClusters(JarCollection jars) {
    TaskProgressLogger task = TaskProgressLogger.get();
    
    task.start("Identifying core clusters in " + jars.size() + " jar files");
    task.report("Compatibility threshold: " + COMPATIBILITY_THRESHOLD.getValue());
    
    Multimap<VersionedFqnNode, Cluster> tempClusterMap = ArrayListMultimap.create();
    
    task.start("Performing post-order traversal of FQN suffix tree", "FQN fragments visited", 100000);
    // Explore the tree in post-order
    int clusterCount = 0;
    for (VersionedFqnNode fragment : jars.getRoot().getPostOrderIterable()) {
      task.progress("%d FQN fragments visited (" + clusterCount + " clusters) in %s");
      // If there are no children, then make it its own single-fqn library
      if (!fragment.hasChildren()) {
        Cluster cluster = Cluster.create(fragment);
        // Store it in the map for processing with the parent
        tempClusterMap.put(fragment, cluster);
        clusterCount++;
      } else {
        // Start merging children
        for (VersionedFqnNode child : fragment.getChildren()) {
          for (Cluster childCluster : tempClusterMap.get(child)) {
            LinkedList<Cluster> candidates = new LinkedList<>();
            
            // Check to see if it can be merged with any of the
            // libraries currently associated with the parent
            for (Cluster merge : tempClusterMap.get(fragment)) {
              if (areCompatible(merge, childCluster)) {
                candidates.add(merge);
              }
            }
            if (candidates.size() == 0) {
              // If nothing was found, promote the library
              tempClusterMap.put(fragment, childCluster);
            } else if (candidates.size() == 1) {
              // If one was found, merge in the child
              Cluster candidate = candidates.getFirst();
              candidate.mergeCore(childCluster);
              clusterCount--;
            } else {
              // This else will never be hit for threshold 1
              // TODO Change this for lower thresholds
              // If more than one was found, promote the library
              tempClusterMap.put(fragment, childCluster);
            }
          }
          // Clear the entry for this child fragment
          tempClusterMap.removeAll(child);
        }
      }
    }
    task.finish();
    
    task.report("Identified " + clusterCount + " core clusters");
    
    task.finish();
    
    return ClusterCollection.create(tempClusterMap.get(jars.getRoot()));
  }
}
