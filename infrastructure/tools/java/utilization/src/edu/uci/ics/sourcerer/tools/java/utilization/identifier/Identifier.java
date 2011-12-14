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
package edu.uci.ics.sourcerer.tools.java.utilization.identifier;

import static edu.uci.ics.sourcerer.util.io.Logging.logger;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.TreeSet;
import java.util.logging.Level;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import edu.uci.ics.sourcerer.tools.java.utilization.model.jar.JarCollection;
import edu.uci.ics.sourcerer.tools.java.utilization.model.jar.VersionedFqnNode;
import edu.uci.ics.sourcerer.util.io.IOUtils;
import edu.uci.ics.sourcerer.util.io.LogFileWriter;
import edu.uci.ics.sourcerer.util.io.TaskProgressLogger;
import edu.uci.ics.sourcerer.util.io.arguments.Arguments;



/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class Identifier {
  private Identifier() {
  }
  
  public static ClusterCollection identifyLibraries(TaskProgressLogger task, JarCollection jars, String logFileName) {
    task.start("Identifying clusters in " + jars.size() + " jar files using tree clustering method");
    task.report("Compatibility threshold: " + Cluster.COMPATIBILITY_THRESHOLD.getValue());
    task.report("Secondary merging method: " + Cluster.MERGE_METHOD.getValue());
    
    Multimap<VersionedFqnNode, Cluster> tempClusterMap = ArrayListMultimap.create();
    
    task.start("Identification Stage One: performing post-order traversal of FQN suffix tree", "FQN fragments visited", 100000);
    int clusterCount = 0;
    // Explore the tree in post-order
    for (VersionedFqnNode fragment : jars.getRoot().getPostOrderIterable()) {
      task.progress("%d FQN fragments visited (" + clusterCount + " libraries) in %s");
      // If there are no children, then make it its own single-fqn library
      if (!fragment.hasChildren()) {
        Cluster cluster = new Cluster();
        // Add the fqn
        cluster.addCoreFqn(fragment);
        // Store it in the map for processing with the parent
        tempClusterMap.put(fragment, cluster);
        clusterCount++;
      } else {
        // Start merging children
        for (VersionedFqnNode child : fragment.getChildren()) {
          for (Cluster childCluster : tempClusterMap.get(child)) {
            LinkedList<Cluster> candidates = new LinkedList<>();
            
            // Check to see if it can be merged with any of the libraries
            for (Cluster merge : tempClusterMap.get(fragment)) {
              if (merge.isCompatible(childCluster)) {
                candidates.add(merge);
              }
            }
            if (candidates.size() == 0) {
              // If nothing was found, promote the library
              tempClusterMap.put(fragment, childCluster);
            } else if (candidates.size() == 1) {
              // If one was found, merge in the child
              Cluster candidate = candidates.getFirst();
              for (VersionedFqnNode fqn : childCluster.getCoreFqns()) {
                candidate.addCoreFqn(fqn);
              }
              clusterCount--;
            } else {
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
    
    task.report("Stage One identified " + clusterCount + " clusters");
    
    task.start("Identification Stage Two: merging similar clusters");
    // Second stage
    TreeSet<Cluster> sortedClusters = new TreeSet<>(new Comparator<Cluster>() {
      @Override
      public int compare(Cluster o1, Cluster o2) {
        int cmp = Integer.compare(o1.getCoreFqns().size(), o2.getCoreFqns().size());
        if (cmp == 0) {
          return Integer.compare(o1.hashCode(), o2.hashCode());
        } else {
          return cmp;
        }
      }
    });
    
    // Sort all the clusters by the number of core fqns they contain
    for (Cluster cluster : tempClusterMap.get(jars.getRoot())) {
      sortedClusters.add(cluster);
    }
    tempClusterMap.clear();
    
    Collection<Cluster> coreClusters = new LinkedList<>();
    // Go from cluster containing the most jars to the least
    try (LogFileWriter writer = IOUtils.createLogFileWriter(new File(Arguments.OUTPUT.getValue(), logFileName))) {
      for (Cluster biggest : sortedClusters.descendingSet()) {
        boolean merged = false;
        // Find and merge any candidate clusters
        for (Cluster coreCluster : coreClusters) {
          // Check if the core cluster should include the next biggest
          if (coreCluster.isSecondStageCompatible(biggest, writer)) {
            coreCluster.mergeCluster(biggest);
            merged = true;
            break;
          }
        }
        if (!merged) {
          coreClusters.add(biggest);
        }
      }
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Error writing log", e);
    }
    
    task.finish();
   
    ClusterCollection clusters = new ClusterCollection();
    for (Cluster cluster : coreClusters) {
      clusters.addCluster(cluster);
    }
    
    task.report("Stage Two reduced the cluster count to " + clusters.getClusters().size());
    
    task.finish();
    
    return clusters;
  }
}
