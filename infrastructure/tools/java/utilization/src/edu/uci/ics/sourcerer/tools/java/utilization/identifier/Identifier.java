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
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.TreeSet;
import java.util.logging.Level;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import edu.uci.ics.sourcerer.tools.java.utilization.model.jar.Jar;
import edu.uci.ics.sourcerer.tools.java.utilization.model.jar.JarCollection;
import edu.uci.ics.sourcerer.tools.java.utilization.model.jar.JarSet;
import edu.uci.ics.sourcerer.tools.java.utilization.model.jar.VersionedFqnNode;
import edu.uci.ics.sourcerer.util.Averager;
import edu.uci.ics.sourcerer.util.io.IOUtils;
import edu.uci.ics.sourcerer.util.io.LogFileWriter;
import edu.uci.ics.sourcerer.util.io.arguments.Argument;
import edu.uci.ics.sourcerer.util.io.arguments.Arguments;
import edu.uci.ics.sourcerer.util.io.arguments.DoubleArgument;
import edu.uci.ics.sourcerer.util.io.arguments.EnumArgument;
import edu.uci.ics.sourcerer.util.io.arguments.RelativeFileArgument;
import edu.uci.ics.sourcerer.util.io.logging.TaskProgressLogger;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class Identifier {
  private Identifier() {
  }
  
  public static final RelativeFileArgument CLUSTER_MERGING_LOG = new RelativeFileArgument("cluster-merging-log", null, Arguments.OUTPUT, "Log file containing cluster merging info.");
  public static final Argument<Double> COMPATIBILITY_THRESHOLD = new DoubleArgument("compatibility-threshold", 1., "").permit();
  public static final Argument<ClusterMergeMethod> MERGE_METHOD = new EnumArgument<>("merge-method", ClusterMergeMethod.class, "Method for performing second stage merge.");
  
  private static boolean areCompatible(Cluster one, Cluster two) {
    // Do a pairwise comparison of every FQN. Calculate the conditional
    // probability of each FQN in B appearing given each FQN in A and average.
    // Then compute the reverse. Both values must be above the threshold.
    double threshold = COMPATIBILITY_THRESHOLD.getValue();
    // If the threshold is greater than 1, no match is possible
    if (threshold > 1) {
      return false;
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
        Averager<Double> otherGivenThis = new Averager<>();
        Averager<Double> thisGivenOther = new Averager<>();
      
        for (VersionedFqnNode fqn : one.getCoreFqns()) {
          for (VersionedFqnNode otherFqn : two.getCoreFqns()) {
            JarSet fqnJars = fqn.getVersions().getJars();
            JarSet otherFqnJars = otherFqn.getVersions().getJars();
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
  
  public static ClusterCollection identifyClusters(TaskProgressLogger task, JarCollection jars) {
    task.start("Identifying clusters in " + jars.size() + " jar files using tree clustering method");
    task.report("Compatibility threshold: " + COMPATIBILITY_THRESHOLD.getValue());
    task.report("Secondary merging method: " + MERGE_METHOD.getValue());
    
    Multimap<VersionedFqnNode, Cluster> tempClusterMap = ArrayListMultimap.create();
    
    task.start("Identification Stage One: performing post-order traversal of FQN suffix tree", "FQN fragments visited", 100000);
    int clusterCount = 0;
    // Explore the tree in post-order
    for (VersionedFqnNode fragment : jars.getRoot().getPostOrderIterable()) {
      task.progress("%d FQN fragments visited (" + clusterCount + " clusters) in %s");
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
        int cmp = Integer.compare(o1.getJars().size(), o2.getJars().size());
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
    try (LogFileWriter writer = IOUtils.createLogFileWriter(CLUSTER_MERGING_LOG.getValue())) {
      for (Cluster biggest : sortedClusters.descendingSet()) {
        boolean merged = false;
        // Find and merge any candidate clusters
        for (Cluster coreCluster : coreClusters) {
          // Check if the core cluster should include the next biggest
          if (MERGE_METHOD.getValue().shouldMerge(coreCluster, biggest, writer)) {
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
   
    ClusterCollection clusters = ClusterCollection.makeEmpty();
    for (Cluster cluster : coreClusters) {
      clusters.addCluster(cluster);
    }
    
    task.report("Stage Two reduced the cluster count to " + clusters.getClusters().size());
    
    task.finish();

    return clusters;
  }
  
  public static Argument<File> EXEMPLAR_LOG = new RelativeFileArgument("exemplar-log", null, Arguments.OUTPUT, "Log file listing the cluster exemplars.");
  public static Argument<Double> EXEMPLAR_THRESHOLD = new DoubleArgument("exemplar-threshold", 0.5, "Threshold for expanding core fqns.").permit();
  
  public static void identifyClusterExemplars(TaskProgressLogger task, ClusterCollection clusters) {
    task.start("Identifying cluster exemplars", "clusters examined", 500);
    boolean log = EXEMPLAR_LOG.getValue() != null;
    try (LogFileWriter logWriter = IOUtils.createLogFileWriter(EXEMPLAR_LOG.getValue())) {
      int goodExemplars = 0;
      for (final Cluster cluster : clusters) {
        if (log) logWriter.writeAndIndent("Identifying cluster exemplars");
        
        // All of the core FQNs are exemplar FQNs
        if (log) logWriter.writeAndIndent("Core FQNs (" + cluster.getCoreFqns().size() + ")");
        for (VersionedFqnNode fqn : cluster.getCoreFqns()) {
          cluster.addExemplarFqn(fqn);
          if (log) logWriter.write(fqn.getFqn());
        }
        if (log) logWriter.unindent();
        
        // An extra FQN becomes an exmplar FQN if it occurs in >= EXEMPLAR_THRESHOLD of jars
        if (log) logWriter.writeAndIndent("Extra FQNs (" + cluster.getExtraFqns().size() + ")");
        for (VersionedFqnNode fqn : cluster.getExtraFqns()) {
          double rate = (double) cluster.getJars().getIntersectionSize(fqn.getVersions().getJars()) / cluster.getJars().size();
          if (rate >= EXEMPLAR_THRESHOLD.getValue()) {
            if (log) logWriter.write("E  " + fqn.getFqn() + " " + rate);
            cluster.addExemplarFqn(fqn);
          } else {
            if (log) logWriter.write("   " + fqn.getFqn() + " " + rate);
          }
        }
        if (log) logWriter.unindent();
        
        // Now let's try to find exemplar jars
        class Stats {
          int exemplarCount = 0;
          int extraCount = 0;
          int outsideCount = 0;
          int score = 0;
          
          public String toString() {
            return score + " " + exemplarCount + " " + extraCount + " " + outsideCount;
          }
        }
        final Map<Jar, Stats> jarInfo = new HashMap<>();
        for (Jar jar : cluster.getJars()) {
          Stats stats = new Stats();
          for (VersionedFqnNode fqn : jar.getFqns()) {
            if (cluster.getExemplarFqns().contains(fqn)) {
              stats.exemplarCount++;
            } else if (cluster.getExtraFqns().contains(fqn)) {
              stats.extraCount++;
            } else {
              stats.outsideCount++;
            }
          }
          
          // Compute the score, lower is better
          // 100 points for every missing exemplar
          stats.score += 1000 * (cluster.getExemplarFqns().size() - stats.exemplarCount);
          // 1 point for every extra
          stats.score += 1 * stats.extraCount;
          // 10 points for every outside
          stats.score += 100 * stats.outsideCount;
          
          jarInfo.put(jar, stats);
        }
        
        PriorityQueue<Jar> queue = new PriorityQueue<>(cluster.getJars().size(), new Comparator<Jar>() {
          @Override
          public int compare(Jar one, Jar two) {
            return Integer.compare(jarInfo.get(one).score, jarInfo.get(two).score);
          }});
        for (Jar jar : cluster.getJars()) {
          queue.add(jar);
        }
        
        int bestScore = -1;
        if (log) logWriter.writeAndIndent("Jars (" + cluster.getJars().size() + ")");
        while (!queue.isEmpty()) {
          Jar top = queue.poll();
          Stats stats = jarInfo.get(top); 
          if (bestScore == -1) {
            cluster.addExemplar(top);
            bestScore = stats.score;
            if (stats.outsideCount == 0) {
              goodExemplars++;
            }
            if (log) logWriter.write("E  " + top.toString() + " " + stats);
          } else if (stats.score == bestScore) {
            cluster.addExemplar(top);
            if (log) logWriter.write("E  " + top.toString() + " " + stats);
          } else {
            if (log) logWriter.write("   " + top.toString() + " " + stats);
          }
        }
        if (log) logWriter.unindent();
       
        if (log) logWriter.unindent();
        task.progress();
      }
      task.report(goodExemplars + " of " + clusters.size() + " clusters had good exemplars");
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Error writing log", e);
    }
    
    task.finish();
  }
}
