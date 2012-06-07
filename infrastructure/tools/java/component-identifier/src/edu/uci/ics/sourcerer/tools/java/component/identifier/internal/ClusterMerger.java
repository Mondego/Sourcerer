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

import static edu.uci.ics.sourcerer.util.io.logging.Logging.logger;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

import edu.uci.ics.sourcerer.tools.java.component.model.cluster.Cluster;
import edu.uci.ics.sourcerer.tools.java.component.model.cluster.ClusterCollection;
import edu.uci.ics.sourcerer.tools.java.component.model.cluster.ClusterVersion;
import edu.uci.ics.sourcerer.tools.java.component.model.jar.FqnVersion;
import edu.uci.ics.sourcerer.tools.java.component.model.jar.Jar;
import edu.uci.ics.sourcerer.tools.java.component.model.jar.VersionedFqnNode;
import edu.uci.ics.sourcerer.util.io.logging.TaskProgressLogger;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class ClusterMerger {
  /*
   * There are two ways to do this. 
   * 
   * Version one:
   * Group the jars according to their versions of the core fqns.
   * For every version of the core, look through each jar for that version.
   * Collect every VERSION of every FQN in those jars.
   * If one version occurs in every jar, add it to the potential fqns list.
   * If one version does not occur in every jar, add it to the invalid fqns list.
   * Once every version has been examined, remove all the invalid fqns from the potentials list.
   * Go through each potential fqn to see if there is complete coverage of a cluster.
   * If there is, merge that cluster.
   * 
   * The intuition is that the extra fqns should only change version if the core is changing version.
   * If the core is fixed and they're changing version, the worry is that the other cluster is and application
   * and this cluster is the library, which means they should not be merged.
   * This turns out not to be true in cases when the only difference between two versions is in the extra fqns.
   * Can't see a good way to fix that.
   * 
   * Version two:
   * Group the jars according to their versions of the core fqns.
   * For every version of the core, look through each jar for that version.
   * Collect every fqn in those jars.
   * If one fqn occurs in every jar, add it to the potentials fqns list.
   * If one fqn does not occur in every jar, add it to the invalid fqns list.
   * Once every version has been examined, remove all the invalid fqns from the potentials list.
   * Collect all the clusters for these fqns, and merge them.
   * A cluster will never be paritally matched, as they are all 100% co-occurence.
   * 
   * This fixes the problem from above, but runs the risk of the issue mentioned above as well.
   */
  
  public static void mergeByVersions(ClusterCollection clusters) {
    TaskProgressLogger task = TaskProgressLogger.get();
    
    task.start("Merging " + clusters.size() + " clusters by matching versions");
    
    TreeSet<Cluster> sortedClusters = new TreeSet<>(Cluster.DESCENDING_SIZE_COMPARATOR);
    Map<VersionedFqnNode, Cluster> fqnToCluster = new HashMap<>();
    for (Cluster cluster : clusters) {
      sortedClusters.add(cluster);
      for (VersionedFqnNode fqn : cluster.getCoreFqns()) {
        fqnToCluster.put(fqn, cluster);
      }
    }
    
    Collection<Cluster> remainingClusters = new LinkedList<>();
    Set<VersionedFqnNode> usedFqns = new HashSet<>();
    task.start("Merging clusters", "clusters examined", 500);
    // Starting from the most important jar
    // For each cluster
    while (!sortedClusters.isEmpty()) {
      Cluster biggest = sortedClusters.pollFirst();
      remainingClusters.add(biggest);

      usedFqns.addAll(biggest.getCoreFqns());
      // Repeatedly add new fqns to the cluster, until no new ones can be added
      boolean addedSomething = true;
      while (addedSomething) {
        Set<VersionedFqnNode> globalPotentials = new HashSet<>();
        Set<VersionedFqnNode> globalPartials = new HashSet<>();

        // For each version, find any fqns that always occur
        for (ClusterVersion version : biggest.getVersions()) {
          Multiset<VersionedFqnNode> potentials = HashMultiset.create();
          for (Jar jar : version.getJars()) {
            for (FqnVersion fqn : jar.getFqns()) {
              if (!usedFqns.contains(fqn.getFqn())) {
                potentials.add(fqn.getFqn());
              }
            }
          }
          
          int max = version.getJars().size();
          for (VersionedFqnNode fqn : potentials.elementSet()) {
            if (potentials.count(fqn) > max) {
              logger.severe("wtf! " + fqn.getFqn());
              // Check the jars for duplicates
              for (Jar jar : version.getJars()) {
                for (FqnVersion node : jar.getFqns()) {
                  if (node.getFqn() == fqn) {
                    logger.severe(jar.getJar().getProperties().HASH.getValue() + " " + node.getFingerprint().serialize());
                  }
                }
              }
            }
            if (potentials.count(fqn) == max && fqn.getJars().isSubset(biggest.getJars())) {
              globalPotentials.add(fqn);
            } else {
              globalPartials.add(fqn);
            }
          }
        }
        
        globalPotentials.removeAll(globalPartials);
        
        // Collect the clusters we plan on merging
        Set<Cluster> newClusters = new HashSet<>();
        for (VersionedFqnNode fqn : globalPotentials) {
          Cluster newCluster = fqnToCluster.get(fqn);
          if (newCluster == null) {
            logger.log(Level.SEVERE, "Unable to find cluster for: " + fqn.getFqn());
          } else {
            newClusters.add(newCluster);
            usedFqns.add(fqn);
            biggest.addVersionedCore(fqn);
          }
        }
        
//        // Verify the clusters
//        for (Cluster cluster : newClusters) {
//          for (VersionedFqnNode fqn : cluster.getCoreFqns()) {
//            if (!globalPotentials.contains(fqn)) {
//              logger.severe("Cluster included without fqn: " + fqn.getFqn());
//              // Every node should have the same JarSet
//              for (VersionedFqnNode node : cluster.getCoreFqns()) {
//                logger.severe(" " + node.getFqn() + " " + globalPotentials.contains(node) + " " + globalPartials.contains(node) + " " + usedFqns.contains(node) + " "+ node.getJars().hashCode());
//              }
//            }
//          }
//        }
        
//        usedFqns.addAll(globalPotentials);
        
        // Remove the clusters from the queue
        sortedClusters.removeAll(newClusters);
        
        addedSomething = !globalPotentials.isEmpty();
      }

      task.progress();
    }
    
    
    task.finish();
    
    clusters.reset(remainingClusters);
    task.report(clusters.size() + " clusters remain");
    task.finish();
  }
}
