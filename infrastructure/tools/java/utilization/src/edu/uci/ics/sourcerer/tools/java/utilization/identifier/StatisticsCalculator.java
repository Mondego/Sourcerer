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

import java.io.IOException;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;

import com.google.common.base.Strings;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import edu.uci.ics.sourcerer.tools.java.utilization.model.cluster.Cluster;
import edu.uci.ics.sourcerer.tools.java.utilization.model.cluster.ClusterCollection;
import edu.uci.ics.sourcerer.tools.java.utilization.model.jar.Jar;
import edu.uci.ics.sourcerer.tools.java.utilization.model.jar.JarCollection;
import edu.uci.ics.sourcerer.tools.java.utilization.model.jar.VersionedFqnNode;
import edu.uci.ics.sourcerer.util.Averager;
import edu.uci.ics.sourcerer.util.Iterators;
import edu.uci.ics.sourcerer.util.io.IOUtils;
import edu.uci.ics.sourcerer.util.io.LogFileWriter;
import edu.uci.ics.sourcerer.util.io.arguments.Argument;
import edu.uci.ics.sourcerer.util.io.arguments.Arguments;
import edu.uci.ics.sourcerer.util.io.arguments.IntegerArgument;
import edu.uci.ics.sourcerer.util.io.arguments.RelativeFileArgument;
import edu.uci.ics.sourcerer.util.io.logging.TaskProgressLogger;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class StatisticsCalculator {
  public static RelativeFileArgument GENERAL_STATS = new RelativeFileArgument("general-stats", "general-stats.txt", Arguments.OUTPUT, "File containing general stats.");
  public static RelativeFileArgument POPULAR_FQNS = new RelativeFileArgument("popular-fqns", null, Arguments.OUTPUT, "Log file containing the most popular FQNs.");
  public static RelativeFileArgument JAR_LISTING = new RelativeFileArgument("jar-listing", null, Arguments.OUTPUT, "Log file for jars broken down by clusters");
  public static RelativeFileArgument CLUSTER_LISTING = new RelativeFileArgument("cluster-listing", null, Arguments.OUTPUT, "Log file for clusters broken down by jars");
  public static Argument<Integer> MAX_TABLE_COLUMNS = new IntegerArgument("max-listing-table-columns", null, "Maximum number of jars to compare in table listing");
  
  public static void calculateGeneralStatistics(JarCollection jars, ClusterCollection clusters) {
    TaskProgressLogger task = TaskProgressLogger.get();
    TaskProgressLogger.Checkpoint checkpoint = task.checkpoint();
    
    NumberFormat doubleFormat = NumberFormat.getNumberInstance();
    doubleFormat.setMinimumFractionDigits(2);
    doubleFormat.setMaximumFractionDigits(2);
    
    try (LogFileWriter writer = IOUtils.createLogFileWriter(GENERAL_STATS.getValue())) {
      task.start("Calculating general jar collection statistics");
      
      Averager<Integer> avgFqns = Averager.create();
      int totalFragments = 0;
      int uniqueFqns = 0;
      for (VersionedFqnNode fqn : jars.getRoot().getPostOrderIterable()) {
        totalFragments++;
        int inc = fqn.getVersions().getJars().size();
        if (inc > 0) {
          avgFqns.addValue(inc);
          uniqueFqns++;
        }
      }
      
      writer.writeAndIndent("Jar collection contains:");
      writer.write(jars.size() + " unique jars");
      writer.write((int) avgFqns.getSum() + " class files");
      writer.write(totalFragments + " name fragments");
      writer.write(uniqueFqns+ " unique names");
      
      writer.writeAndIndent("Class files per jar:");
      writer.write("Smallest jar contains " + avgFqns.getMin() + " class files");
      writer.write("Largest jar contains " + avgFqns.getMax() + " class files");
      writer.write("Average jar contains " + doubleFormat.format(avgFqns.getMean()) + " (" + doubleFormat.format(avgFqns.getStandardDeviation()) + ") class files");
      writer.unindent();
      
      writer.unindent();
      
      task.finish();
      
      if (POPULAR_FQNS.hasValue()) {
        task.start("Logging most popular FQNs to: " + POPULAR_FQNS.getValue().getPath());
        
        try (LogFileWriter log = IOUtils.createLogFileWriter(POPULAR_FQNS)) {
          writer.write(jars.size() + " unique jars");
          writer.write(uniqueFqns+ " unique names");
          writer.write((int) avgFqns.getSum() + " total names");
          writer.newLine();
          
          // Put the FQNs into an array
          VersionedFqnNode fqns[] = new VersionedFqnNode[uniqueFqns];
          int i = 0;
          for (VersionedFqnNode fqn : jars.getRoot().getPostOrderIterable()) {
            if (fqn.getVersions().getJars().size() > 0) {
              fqns[i++] = fqn;
            }
          }
          
          // Sort them by descending number of occurrences
          Arrays.sort(fqns, new Comparator<VersionedFqnNode>() {
            @Override
            public int compare(VersionedFqnNode one, VersionedFqnNode two) {
              return Integer.compare(two.getVersions().getJars().size(), one.getVersions().getJars().size());
            }
          });
          
          // Write them to the log
          for (VersionedFqnNode fqn : fqns) {
            log.write(fqn.getVersions().getJars().size() + "\t\t" + fqn.getFqn());
          }
        }
        task.finish();
      }
      
      task.start("Calculating general cluster statistics");
      
      // Compute cluster stats
      final Multimap<Jar, Cluster> jarsToClusters = HashMultimap.create();
      Averager<Integer> jarsPerCluster = Averager.create();
      int singleJarCount = 0;
      Averager<Integer> coreFqnsPerCluster = Averager.create();
      Averager<Integer> extraFqnsPerCluster = Averager.create();
      Averager<Integer> exemplarFqnsPerCluster = Averager.create();
      for (Cluster cluster : clusters) {
        if (cluster.getJars().size() == 1) {
          singleJarCount++;
        }
        jarsPerCluster.addValue(cluster.getJars().size());
        coreFqnsPerCluster.addValue(cluster.getCoreFqns().size());
        extraFqnsPerCluster.addValue(cluster.getExtraFqns().size());
        exemplarFqnsPerCluster.addValue(cluster.getExemplarFqns().size());
        for (Jar jar : cluster.getJars()) {
          jarsToClusters.put(jar, cluster);
        }
      }
      
      int singleClusterCount = 0;
      Averager<Integer> clustersPerJar = Averager.create();
      for (Jar jar : jarsToClusters.keySet()) {
        Collection<Cluster> clus = jarsToClusters.get(jar);
        if (clus.size() == 1) {
          singleClusterCount++;
        }
        clustersPerJar.addValue(clus.size());
      }
      
      // How many clusters have good exemplars?
      int goodExemplarCount = 0;
      Averager<Integer> exemplars = Averager.create();
      for (Cluster cluster : clusters) {
        boolean good = true;
        exemplars.addValue(cluster.getExemplars().size());
        for (Jar exemplar : cluster.getExemplars()) {
          good &= jarsToClusters.get(exemplar).size() == 1;
        }
        if (good) {
          goodExemplarCount++;
        }
      }
      
      writer.writeAndIndent("Cluster stats:");
      
      writer.writeAndIndent("Jars per cluster");
      writer.write(clusters.size() + " clusters");
      writer.write(singleJarCount + " clusters with one jar");
      writer.write((clusters.size() - singleJarCount) + " clusters with multiple jars");
      writer.write("Smallest cluster contains " + jarsPerCluster.getMin() + " jars");
      writer.write("Largest cluster contains " + jarsPerCluster.getMax() + " jars");
      writer.write("Average cluster contains " + doubleFormat.format(jarsPerCluster.getMean()) + " (" + doubleFormat.format(jarsPerCluster.getStandardDeviation()) + ") jars");
      writer.unindent();
      
      writer.writeAndIndent("Core FQNs per cluster");
      writer.write("Fewest core fqns: " + coreFqnsPerCluster.getMin());
      writer.write("Most core fqns: " + coreFqnsPerCluster.getMax());
      writer.write("Average core fqns " + doubleFormat.format(coreFqnsPerCluster.getMean()) + " (" + doubleFormat.format(coreFqnsPerCluster.getStandardDeviation()) + ")");
      writer.unindent();
      
      writer.writeAndIndent("Extra FQNs per cluster");
      writer.write("Fewest extra fqns: " + extraFqnsPerCluster.getMin());
      writer.write("Most extra fqns: " + extraFqnsPerCluster.getMax());
      writer.write("Average extra fqns " + doubleFormat.format(extraFqnsPerCluster.getMean()) + " (" + doubleFormat.format(extraFqnsPerCluster.getStandardDeviation()) + ")");
      writer.unindent();
      
      writer.writeAndIndent("Exemplar FQNs per cluster");
      writer.write("Fewest exemplar fqns: " + exemplarFqnsPerCluster.getMin());
      writer.write("Most exemplar fqns: " + exemplarFqnsPerCluster.getMax());
      writer.write("Average exemplar fqns " + doubleFormat.format(exemplarFqnsPerCluster.getMean()) + " (" + doubleFormat.format(exemplarFqnsPerCluster.getStandardDeviation()) + ")");
      writer.unindent();
      
      writer.writeAndIndent("Clusters per jar");
      writer.write(jars.size() + " jars");
      writer.write(singleClusterCount + " jars with one cluster");
      writer.write((jars.size() - singleClusterCount) + " jars with multiple clusters");
      writer.write("Smallest jar contains " + clustersPerJar.getMin() + " clusters");
      writer.write("Largest jar contains " + clustersPerJar.getMax() + " clusters");
      writer.write("Average jar contains " + doubleFormat.format(clustersPerJar.getMean()) + " (" + doubleFormat.format(clustersPerJar.getStandardDeviation()) + ") clusters");
      writer.unindent();
     
      writer.writeAndIndent("Exemplars");
      writer.write(clusters.size() + " clusters");
      writer.write(goodExemplarCount + " clusters with good exemplars");
      writer.write((clusters.size() - goodExemplarCount) + " clusters with bad exemplars");
      writer.write("Largest cluster contains " + exemplars.getMax() + " exemplars");
      writer.write("Smallest cluster contains " + exemplars.getMin() + " exemplars");
      writer.write("Average cluster contains " + doubleFormat.format(exemplars.getMean()) + " (" + doubleFormat.format(exemplars.getStandardDeviation()) + " ) exemplars");
      writer.unindent();
      
      if (JAR_LISTING.hasValue()) {
        task.start("Logging jar listing to: " + JAR_LISTING.getValue().getPath());
        // Print out list of jars, ordered by number of clusters
        try (LogFileWriter log = IOUtils.createLogFileWriter(JAR_LISTING)) {
          log.write(jars.size() + " jars");
          log.write(singleClusterCount + " jars with one cluster");
          log.write((jars.size() - singleClusterCount) + " jars with multiple clusters");
          Jar[] sorted = Iterators.toArray(jars, new Jar[jars.size()]);
          
          Arrays.sort(sorted, new Comparator<Jar>() {
            @Override
            public int compare(Jar one, Jar two) {
              return Integer.compare(jarsToClusters.get(two).size(), jarsToClusters.get(one).size());
            }});
          
          Integer maxJarsToShow = MAX_TABLE_COLUMNS.getValue();
          
          for (Jar jar : sorted) {
            Collection<Cluster> jarClusters = jarsToClusters.get(jar);
            
            // Collect all the jars that overlap with this jar
            Jar[] relatedJars = null;
            {
              Set<Jar> overlap = new TreeSet<>(new Comparator<Jar>() {
                @Override
                public int compare(Jar o1, Jar o2) {
                  String name1 = o1.getJar().toString();
                  String name2 = o2.getJar().toString();
                  return name1 == null ? (name2 == null ? 0 : -1) : (name2 == null ? 1 : name1.compareTo(name2));
                }});
              for (Cluster cluster : jarClusters) {
                for (Jar otherJar : cluster.getJars()) {
                  overlap.add(otherJar);
                }
              }
              relatedJars = new Jar[overlap.size()] ;
              // Remove this jar
              overlap.remove(jar);
              
              int i = 0;
              relatedJars[i++] = jar;
              for (Jar otherJar : overlap) {
                relatedJars[i++] = otherJar;
              }
            }
            
            // Log the header info
            log.newLine();
            log.writeAndIndent(jar + " fragmented into " + jarClusters.size() + " clusters");
            log.write("FQNs from this jar appear in " + (relatedJars.length - 1) + " other jars");
            log.writeAndIndent("Listing jars with overlap");
            
            // Log the list of jars that intersect with this jar
            int c = 1;
            for (Jar otherJar : relatedJars) {
              log.write(Strings.padEnd(c++ + ": ", 5, ' ') + otherJar + " " + otherJar.getJar().getProperties().HASH.getValue());
            }
            c = maxJarsToShow == null ? relatedJars.length : Math.min(maxJarsToShow, relatedJars.length);
            log.unindent();
            log.unindent();
            
            Collection<VersionedFqnNode> fqns = new HashSet<>(jar.getFqns());
            
            // Log the table for each cluster
            int clusterCount = 0;
            for (Cluster cluster : clusters) {
              log.write("Cluster " + ++clusterCount + " (" + cluster.getJars().size() + " jars )");
              // Start with the core FQNs
              // Log the number line
              for (int i = 1; i <= c; i++) {
                log.writeFragment(Integer.toString(i % 10));
              }
              log.writeFragment(" Core FQNs");
              log.newLine();
              
              // Every core FQN will match (by definition)
              // sort them alphabetically
              Collection<VersionedFqnNode> coreFqns = new TreeSet<>(cluster.getCoreFqns());
              for (VersionedFqnNode fqn : coreFqns) {
                for (Jar otherJar : relatedJars) {
                  // Does this other jar contain the FQN?
                  if (fqn.getVersions().getJars().contains(otherJar)) {
                    log.writeFragment("*");
                  } else {
                    log.writeFragment(" ");
                  }
                }
                log.writeFragment(" " + fqn.getFqn());
                log.newLine();
              }
              coreFqns = cluster.getCoreFqns();
              
              // See if there are any exemplar non-core FQNs
              Collection<VersionedFqnNode> nonCoreExemplar = new TreeSet<>();
              Collection<VersionedFqnNode> missingExemplar = new TreeSet<>();
              for (VersionedFqnNode fqn : cluster.getExemplarFqns()) {
                if (!coreFqns.contains(fqn)) {
                  if (fqns.contains(fqn)) {
                    nonCoreExemplar.add(fqn);
                  } else {
                    missingExemplar.add(fqn);
                  }
                }
              }
              
              // Log the non-core exemplar fqns
              if (!nonCoreExemplar.isEmpty()) {
                // Log the number line
                for (int i = 1; i <= c; i++) {
                  log.writeFragment(Integer.toString(i % 10));
                }
                log.writeFragment(" Non-core Exemplar FQNs");
                log.newLine();
                
                for (VersionedFqnNode fqn : nonCoreExemplar) {
                  for (Jar otherJar : relatedJars) {
                    // Does this other jar contain the FQN?
                    if (fqn.getVersions().getJars().contains(otherJar)) {
                      log.writeFragment("*");
                    } else {
                      log.writeFragment(" ");
                    }
                  }
                  log.writeFragment(" " + fqn.getFqn());
                  log.newLine();
                }
              }
              
              // Log the missing non-core exemplar fqns
              if (!missingExemplar.isEmpty()) {
                // Log the number line
                for (int i = 1; i <= c; i++) {
                  log.writeFragment(Integer.toString(i % 10));
                }
                log.writeFragment(" Missing Non-core Exemplar FQNs");
                log.newLine();
                
                for (VersionedFqnNode fqn : missingExemplar) {
                  for (Jar otherJar : relatedJars) {
                    // Does this other jar contain the FQN?
                    if (fqn.getVersions().getJars().contains(otherJar)) {
                      log.writeFragment("*");
                    } else {
                      log.writeFragment(" ");
                    }
                  }
                  log.writeFragment(" " + fqn.getFqn());
                  log.newLine();
                }
              }
              
              // See if there are any other extra fqns
              int missing = 0;
              Collection<VersionedFqnNode> extraFqns = new TreeSet<>();
              for (VersionedFqnNode fqn : cluster.getExtraFqns()) {
                if (!nonCoreExemplar.contains(fqn) && !missingExemplar.contains(fqn)) {
                  if (fqns.contains(fqn)) {
                    extraFqns.add(fqn);
                  } else {
                    missing++;
                  }
                }
              }
             
              // Log the extra fqns
              if (!extraFqns.isEmpty()) {
                // Log the number line
                for (int i = 1; i <= c; i++) {
                  log.writeFragment(Integer.toString(i % 10));
                }
                log.writeFragment(" Extra FQNs");
                log.newLine();
                
                for (VersionedFqnNode fqn : extraFqns) {
                  for (Jar otherJar : relatedJars) {
                    // Does this other jar contain the FQN?
                    if (fqn.getVersions().getJars().contains(otherJar)) {
                      log.writeFragment("*");
                    } else {
                      log.writeFragment(" ");
                    }
                  }
                  log.writeFragment(" " + fqn.getFqn());
                  log.newLine();
                }
              } 
              
              if (missing > 0) {
                log.writeFragment(Strings.repeat(" ", c + 1));
                log.writeFragment(missing + " extra FQNs in cluster missing from jar");
                log.newLine();
              }
            }
          }
        }
      }
      
      if (CLUSTER_LISTING.hasValue()) {
        // Print out list of clusters, ordered by number of jars
        try (LogFileWriter log = IOUtils.createLogFileWriter(CLUSTER_LISTING)) {
          log.write(jars.size() + " jars");
          Cluster[] sorted = Iterators.toArray(clusters, new Cluster[clusters.size()]);
          
          Arrays.sort(sorted, new Comparator<Cluster>() {
            @Override
            public int compare(Cluster one, Cluster two) {
              return Integer.compare(two.getJars().size(), one.getJars().size());
            }});
          
          Integer maxJarsToShow = MAX_TABLE_COLUMNS.getValue();
          
          for (Cluster cluster : sorted) {
            // Collect all the jars that overlap with this jar
            Jar[] containedJars = null;
            {
              Set<Jar> sortedJars = new TreeSet<>(new Comparator<Jar>() {
                @Override
                public int compare(Jar o1, Jar o2) {
                  String name1 = o1.getJar().toString();
                  String name2 = o2.getJar().toString();
                  return name1 == null ? (name2 == null ? 0 : -1) : (name2 == null ? 1 : name1.compareTo(name2));
                }});
              for (Jar jar : cluster.getJars()) {
                sortedJars.add(jar);
              }
              containedJars = new Jar[sortedJars.size()] ;
              
              int i = 0;
              for (Jar jar : sortedJars) {
                containedJars[i++] = jar;
              }
            }
            
            // Log the header info
            log.newLine();
            log.writeAndIndent("Cluster of " + containedJars.length + " jars");
            log.writeAndIndent("Listing jars in cluster");
            
            // Log the list of jars in this cluster
            int c = 1;
            for (Jar jar : containedJars) {
              log.write(Strings.padEnd(c++ + ": ", 5, ' ') + jar + " " + jar.getJar().getProperties().HASH.getValue());
            }
            c = maxJarsToShow == null ? containedJars.length : Math.min(maxJarsToShow, containedJars.length);
            log.unindent();
            log.unindent();
            
            // Start with the core FQNs
            // Log the number line
            for (int i = 1; i <= c; i++) {
              log.writeFragment(Integer.toString(i % 10));
            }
            log.writeFragment(" Core FQNs");
            log.newLine();
              
            // sort them alphabetically
            Collection<VersionedFqnNode> coreFqns = new TreeSet<>(cluster.getCoreFqns());
            for (VersionedFqnNode fqn : coreFqns) {
              for (Jar jar : containedJars) {
                // Does this jar contain the FQN?
                if (fqn.getVersions().getJars().contains(jar)) {
                  log.writeFragment("*");
                } else {
                  log.writeFragment(" ");
                }
              }
              log.writeFragment(" " + fqn.getFqn());
              log.newLine();
            }
            coreFqns = cluster.getCoreFqns();
            
            // See if there are any exemplar non-core FQNs
            Collection<VersionedFqnNode> nonCoreExemplar = new TreeSet<>();
            for (VersionedFqnNode fqn : cluster.getExemplarFqns()) {
              if (!coreFqns.contains(fqn)) {
                nonCoreExemplar.add(fqn);
              }
            }
              
            // Log the non-core exemplar fqns
            if (!nonCoreExemplar.isEmpty()) {
              // Log the number line
              for (int i = 1; i <= c; i++) {
                log.writeFragment(Integer.toString(i % 10));
              }
              log.writeFragment(" Non-core Exemplar FQNs");
              log.newLine();
              
              for (VersionedFqnNode fqn : nonCoreExemplar) {
                for (Jar jar : containedJars) {
                  // Does this jar contain the FQN?
                  if (fqn.getVersions().getJars().contains(jar)) {
                    log.writeFragment("*");
                  } else {
                    log.writeFragment(" ");
                  }
                }
                log.writeFragment(" " + fqn.getFqn());
                log.newLine();
              }
            }
            
            // See if there are any other extra fqns
            Collection<VersionedFqnNode> extraFqns = new TreeSet<>();
            for (VersionedFqnNode fqn : cluster.getExtraFqns()) {
              if (!nonCoreExemplar.contains(fqn)) {
                extraFqns.add(fqn);
              }
            }
             
            // Log the extra fqns
            if (!extraFqns.isEmpty()) {
              // Log the number line
              for (int i = 1; i <= c; i++) {
                log.writeFragment(Integer.toString(i % 10));
              }
              log.writeFragment(" Extra FQNs");
              log.newLine();
              
              for (VersionedFqnNode fqn : extraFqns) {
                for (Jar jar : containedJars) {
                  // Does this other jar contain the FQN?
                  if (fqn.getVersions().getJars().contains(jar)) {
                    log.writeFragment("*");
                  } else {
                    log.writeFragment(" ");
                  }
                }
                log.writeFragment(" " + fqn.getFqn());
                log.newLine();
              }
            } 
          }
        }
      }
      task.finish();
      
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Error in writing logs", e);
    } finally {
      checkpoint.activate();
    }
  }
}
