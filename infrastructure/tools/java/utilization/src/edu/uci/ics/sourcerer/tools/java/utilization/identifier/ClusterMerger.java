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

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeSet;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

import edu.uci.ics.sourcerer.tools.java.utilization.model.cluster.Cluster;
import edu.uci.ics.sourcerer.tools.java.utilization.model.cluster.ClusterCollection;
import edu.uci.ics.sourcerer.tools.java.utilization.model.cluster.ClusterMatcher;
import edu.uci.ics.sourcerer.tools.java.utilization.model.cluster.ClusterVersion;
import edu.uci.ics.sourcerer.tools.java.utilization.model.jar.FqnVersion;
import edu.uci.ics.sourcerer.tools.java.utilization.model.jar.Jar;
import edu.uci.ics.sourcerer.tools.java.utilization.model.jar.VersionedFqnNode;
import edu.uci.ics.sourcerer.util.io.logging.TaskProgressLogger;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class ClusterMerger {
//  public static final RelativeFileArgument CLUSTER_MERGING_LOG = new RelativeFileArgument("cluster-merging-log", null, Arguments.OUTPUT, "Log file containing cluster merging info.");
//  public static final Argument<ClusterMergeMethod> CLUSTER_MERGE_METHOD = new EnumArgument<>("cluster-merge-method", ClusterMergeMethod.class, "Method for performing second stage merge.");
//  
//  public static void mergeClusters(ClusterCollection clusters) {
//    TaskProgressLogger task = TaskProgressLogger.get();
//    
//    task.start("Merging " + clusters.size() + " clusters using " + CLUSTER_MERGE_METHOD.getValue());
//
//    File mergeLog = CLUSTER_MERGING_LOG.getValue();
//    if (mergeLog != null) {
//      task.report("Logging merge information to: " + mergeLog.getPath());
//    }
//    
//    task.start("Sorting clusters by decreasing size");
//    Cluster[] clus = clusters.getClusters().toArray(new Cluster[clusters.size()]);
//    Arrays.sort(clus,
//        new Comparator<Cluster>() {
//          @Override
//          public int compare(Cluster o1, Cluster o2) {
//            int cmp = Integer.compare(o2.getJars().size(), o1.getJars().size());
//            if (cmp == 0) {
//              return Integer.compare(o1.hashCode(), o2.hashCode());
//            } else {
//              return cmp;
//            }
//          }
//        });
//    task.finish();
//    
//    task.start("Merging clusters", "clusters examined", 500);
//    Collection<Cluster> coreClusters = new LinkedList<>();
//    // Go from cluster containing the most jars to the least
//    try (LogFileWriter writer = IOUtils.createLogFileWriter(CLUSTER_MERGING_LOG.getValue())) {
//      for (Cluster biggest : clus) {
//        boolean merged = false;
//        // Find and merge any candidate clusters
//        for (Cluster coreCluster : coreClusters) {
//          // Check if the core cluster should include the next biggest
//          if (CLUSTER_MERGE_METHOD.getValue().shouldMerge(coreCluster, biggest, writer)) {
//            coreCluster.mergeExtra(biggest);
//            merged = true;
//            break;
//          }
//        }
//        if (!merged) {
//          coreClusters.add(biggest);
//        }
//        task.progress();
//      }
//    } catch (IOException e) {
//      logger.log(Level.SEVERE, "Error writing log", e);
//    }
//    task.finish();
//    
//    task.report("Cluster count reduced to " + coreClusters.size());
//    clusters.reset(coreClusters);
//    
//    task.finish();
//  }
  
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
    
    ClusterMatcher matcher = clusters.getClusterMatcher();
    
    TreeSet<Cluster> sortedClusters = new TreeSet<>(Cluster.DESCENDING_SIZE_COMPARATOR);
    sortedClusters.addAll(clusters.getClusters());
    
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
          newClusters.add(matcher.getCluster(fqn));
          usedFqns.add(fqn);
          biggest.addVersionedCore(fqn);
        }
        
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
