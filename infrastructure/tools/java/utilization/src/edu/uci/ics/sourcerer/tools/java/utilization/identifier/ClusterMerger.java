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

import static edu.uci.ics.sourcerer.util.io.logging.Logging.logger;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;

import edu.uci.ics.sourcerer.tools.java.utilization.model.cluster.Cluster;
import edu.uci.ics.sourcerer.tools.java.utilization.model.cluster.ClusterCollection;
import edu.uci.ics.sourcerer.tools.java.utilization.model.cluster.ClusterMatcher;
import edu.uci.ics.sourcerer.tools.java.utilization.model.jar.Jar;
import edu.uci.ics.sourcerer.tools.java.utilization.model.jar.Version;
import edu.uci.ics.sourcerer.tools.java.utilization.model.jar.VersionedFqnNode;
import edu.uci.ics.sourcerer.util.io.IOUtils;
import edu.uci.ics.sourcerer.util.io.LogFileWriter;
import edu.uci.ics.sourcerer.util.io.arguments.Argument;
import edu.uci.ics.sourcerer.util.io.arguments.Arguments;
import edu.uci.ics.sourcerer.util.io.arguments.EnumArgument;
import edu.uci.ics.sourcerer.util.io.arguments.RelativeFileArgument;
import edu.uci.ics.sourcerer.util.io.logging.TaskProgressLogger;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class ClusterMerger {
  public static final RelativeFileArgument CLUSTER_MERGING_LOG = new RelativeFileArgument("cluster-merging-log", null, Arguments.OUTPUT, "Log file containing cluster merging info.");
  public static final Argument<ClusterMergeMethod> CLUSTER_MERGE_METHOD = new EnumArgument<>("cluster-merge-method", ClusterMergeMethod.class, "Method for performing second stage merge.");
  
  public static void mergeClusters(ClusterCollection clusters) {
    TaskProgressLogger task = TaskProgressLogger.get();
    
    task.start("Merging " + clusters.size() + " clusters using " + CLUSTER_MERGE_METHOD.getValue());

    File mergeLog = CLUSTER_MERGING_LOG.getValue();
    if (mergeLog != null) {
      task.report("Logging merge information to: " + mergeLog.getPath());
    }
    
    task.start("Sorting clusters by decreasing size");
    Cluster[] clus = clusters.getClusters().toArray(new Cluster[clusters.size()]);
    Arrays.sort(clus,
        new Comparator<Cluster>() {
          @Override
          public int compare(Cluster o1, Cluster o2) {
            int cmp = Integer.compare(o2.getJars().size(), o1.getJars().size());
            if (cmp == 0) {
              return Integer.compare(o1.hashCode(), o2.hashCode());
            } else {
              return cmp;
            }
          }
        });
    task.finish();
    
    task.start("Merging clusters", "clusters examined", 500);
    Collection<Cluster> coreClusters = new LinkedList<>();
    // Go from cluster containing the most jars to the least
    try (LogFileWriter writer = IOUtils.createLogFileWriter(CLUSTER_MERGING_LOG.getValue())) {
      for (Cluster biggest : clus) {
        boolean merged = false;
        // Find and merge any candidate clusters
        for (Cluster coreCluster : coreClusters) {
          // Check if the core cluster should include the next biggest
          if (CLUSTER_MERGE_METHOD.getValue().shouldMerge(coreCluster, biggest, writer)) {
            coreCluster.mergeExtra(biggest);
            merged = true;
            break;
          }
        }
        if (!merged) {
          coreClusters.add(biggest);
        }
        task.progress();
      }
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Error writing log", e);
    }
    task.finish();
    
    task.report("Cluster count reduced to " + coreClusters.size());
    clusters.reset(coreClusters);
    
    task.finish();
  }
  
  public static void mergeByVersions(ClusterCollection clusters) {
    TaskProgressLogger task = TaskProgressLogger.get();
    
    task.start("Merging " + clusters.size() + " clusters by matching versions");
    
    ClusterMatcher matcher = clusters.getClusterMatcher();
    
    TreeSet<Cluster> sortedClusters = new TreeSet<>(Cluster.DESCENDING_SIZE_COMPARATOR);
    sortedClusters.addAll(clusters.getClusters());
    
    Collection<Cluster> remainingClusters = new LinkedList<>();
    task.start("Merging clusters", "clusters examined", 500);
    // Starting from the most important jar
    // For each cluster
    Set<VersionedFqnNode> newFqns = new HashSet<>();
    while (!sortedClusters.isEmpty()) {
      Cluster biggest = sortedClusters.pollFirst();
      remainingClusters.add(biggest);
      // For each fqn in that cluster
      for (VersionedFqnNode fqn : biggest.getCoreFqns()) {
        // For each version of each fqn
        if (fqn.getVersions().getCount() > 1) {
          for (Version version : fqn.getVersions()) {
            Collection<VersionedFqnNode> currentCore = biggest.getCoreFqns(); 
            // What are the fqns that are always present with this version of this fqn?
            Set<VersionedFqnNode> potentials = null;
            // For every jar for this version
            for (Jar jar : version.getJars()) {
              // For the first jar, add everything to the potentials
              if (potentials == null) {
                potentials = new HashSet<>(jar.getFqns());
              }
              // Otherwise, retain everything in this jar
              else {
                potentials.retainAll(jar.getFqns());
              }
            }
            potentials.removeAll(currentCore);
            potentials.removeAll(newFqns);
            // For each potential fqn
            for (VersionedFqnNode potential : potentials) {
              // If it's a good match to the core of this cluster
              if (potential.getVersions().getJars().isSubset(biggest.getJars())) {
                newFqns.add(potential);
              }
            }
          }
        }
      }
      // Now we have a set of new fqns to add to the cluster
      for (VersionedFqnNode fqn : newFqns) {
        biggest.addVersionedCore(fqn);
        // Remove the associated cluster from the set
        sortedClusters.remove(matcher.getCluster(fqn));
      }
      newFqns.clear();
      
      task.progress();
    }
    
    
    task.finish();
    
    clusters.reset(remainingClusters);
    task.report(clusters.size() + " clusters remain");
    task.finish();
  }
}
