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
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import edu.uci.ics.sourcerer.tools.java.repo.model.JarProperties;
import edu.uci.ics.sourcerer.tools.java.utilization.model.jar.VersionedFqnNode;
import edu.uci.ics.sourcerer.tools.java.utilization.model.jar.Jar;
import edu.uci.ics.sourcerer.tools.java.utilization.model.jar.JarSet;
import edu.uci.ics.sourcerer.util.io.IOUtils;
import edu.uci.ics.sourcerer.util.io.LogFileWriter;
import edu.uci.ics.sourcerer.util.io.TaskProgressLogger;
import edu.uci.ics.sourcerer.util.io.arguments.Arguments;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class ClusterCollection implements Iterable<Cluster> {
  private final Collection<Cluster> clusters;
  
  ClusterCollection() {
    clusters = new ArrayList<>();
  }
  
  void addCluster(Cluster library) {
    clusters.add(library);
  }
  
  public Collection<Cluster> getClusters() {
    return clusters;
  }

  @Override
  public Iterator<Cluster> iterator() {
    return clusters.iterator();
  }

  public int size() {
    return clusters.size();
  }
  
//  public void printStatistics(TaskProgressLogger task) {
//    task.start("Printing library statistics");
//    task.report(libraries.size() + " libraries identified");
//    int trivial = 0;
//    TreeSet<Library> nonTrivial = new TreeSet<>(new Comparator<Library>() {
//      @Override
//      public int compare(Library o1, Library o2) {
//        int cmp = Integer.compare(o1.getJars().size(), o2.getJars().size());
//        if (cmp == 0) {
//          return Integer.compare(o1.hashCode(), o2.hashCode());
//        } else {
//          return cmp;
//        }
//      }});
//    for (Library library : libraries) {
//      if (library.getJars().size() > 1) {
//        nonTrivial.add(library);
//      } else {
//        trivial++;
//      }
//    }
//    task.report(trivial + " unique libraries");
//    task.report(nonTrivial.size() + " compound libraries");
//    task.start("Examining compound libraries");
//    while (!nonTrivial.isEmpty()) {
//      Library biggest = nonTrivial.pollLast();
//      task.start("Listing FQNs for library found in " + biggest.getJars().size() + " jars");
//      for (FqnFragment fqn : biggest.getFqns()) {
//        task.report(fqn.getFqn());
//      }
//      task.finish();
//    }
//    task.finish();
//    task.finish();
//  }
  
  public void printStatistics(TaskProgressLogger task, String jarFileName, String clusterFileName) {
    NumberFormat format = NumberFormat.getPercentInstance();
    format.setMinimumFractionDigits(2);
    format.setMaximumFractionDigits(2);

    task.start("Printing jar statistics");
    try (LogFileWriter writer = IOUtils.createLogFileWriter(new File(Arguments.OUTPUT.getValue(), jarFileName))) {
      Multimap<Jar, Cluster> clusterMap = HashMultimap.create();
      int trivialCluster = 0;
      for (Cluster cluster : clusters) {
        for (Jar jar : cluster.getJars()) {
          clusterMap.put(jar, cluster);
        }
        if (cluster.getJars().size() == 1) {
          trivialCluster++;
        }
      }
      int trivialJar = 0;
      for (Jar jar : clusterMap.keySet()) {
        Collection<Cluster> clusters = clusterMap.get(jar);
        if (clusters.size() == 1) {
          trivialJar++;
        }
      }
      writer.write(clusterMap.keySet().size() + " jars");
      writer.write(clusters.size() + " clusters");
      writer.write(trivialJar + " jars covered by single cluster");
      writer.write(trivialCluster + " clusters matching a single jar");
      writer.write((clusterMap.keySet().size() - trivialJar) + " jars covered by multiple clusters");
      writer.write((clusters.size() - trivialCluster) + " clustered matching multiple jars");
      
      for (Jar jar : clusterMap.keySet()) {
        Collection<Cluster> clusters = clusterMap.get(jar);
        if (clusters.size() > 1) {
          HashSet<VersionedFqnNode> fqns = new HashSet<>(jar.getFqns());
          writer.writeAndIndent(jar.getJar().getProperties().NAME.getValue() + " fragmented into " + clusters.size() + " clusters");
          Set<Jar> otherJars = new HashSet<>();
          for (VersionedFqnNode fqn : jar.getFqns()) {
            for (Jar otherJar : fqn.getVersions().getJars()) {
              otherJars.add(otherJar);
            }
          }
          writer.write("FQNs from this jar appear in " + (otherJars.size() - 1) + " other jars");
          writer.writeAndIndent("Listing jars with overlap");
          int c = 1;
          for (Jar otherJar : otherJars) {
            JarProperties props = otherJar.getJar().getProperties();
            writer.write(c++ + ": " + props.NAME.getValue() + ": " + props.HASH.getValue() + (otherJar == jar ? " <--" : ""));
          }
          writer.unindent();
          
          for (int i = 1, max = otherJars.size(); i <= max; i++) {
            writer.writeFragment(Integer.toString(i % 10));
          }
          writer.newLine();
          
          int clusterCount = 0;
          for (Cluster lib : clusters) {
            for (int i = 0; i < c; i++)
              writer.writeFragment(" ");
            writer.writeFragment(" Cluster " + ++clusterCount + ", from " + lib.getJars().size() + " (" + lib.getPrimaryJars().size() + ") jars");
            writer.newLine();
            int skipped = 0;
            for (VersionedFqnNode fqn : lib.getFqns()) {
              if (fqns.contains(fqn)) {
                for (Jar otherJar : otherJars) {
                  if (fqn.getVersions().getJars().contains(otherJar)) {
                    writer.writeFragment("*");
                  } else {
                    writer.writeFragment(" ");
                  }
                }
                writer.writeFragment(" " + fqn.getFqn());
                writer.newLine();
              } else {
                skipped++;
              }
            }
            for (int i = 0; i < c; i++)
              writer.writeFragment(" ");
            if (skipped > 0)
              writer.writeFragment(" " + skipped + " FQNS in cluster not in this jar");
            writer.newLine();
          }
          
          writer.unindent();
        }
      }
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Error printing statistics", e);
    }
    task.finish();
    
    task.start("Printing cluster statistics");
    try (LogFileWriter writer = IOUtils.createLogFileWriter(new File(Arguments.OUTPUT.getValue(), clusterFileName))) {
      TreeSet<Cluster> sortedClusters = new TreeSet<>(new Comparator<Cluster>() {
        @Override
        public int compare(Cluster o1, Cluster o2) {
          int cmp = Integer.compare(o1.getFqns().size(), o2.getFqns().size());
          if (cmp == 0) {
            return Integer.compare(o1.hashCode(), o2.hashCode());
          } else {
            return cmp;
          }
        }});
      for (Cluster cluster : clusters) {
        if (cluster.getJars().size() > 1) {
          sortedClusters.add(cluster);
        }
      }
      
      writer.write(clusters.size() + " clusters");
      writer.write((clusters.size() - sortedClusters.size()) + " clusters matching a single jar");
      writer.write(sortedClusters.size() + " clusters matching multiple jars");

      while (!sortedClusters.isEmpty()) {
        Cluster cluster = sortedClusters.pollFirst();
        writer.writeAndIndent("Cluster of " + cluster.getJars().size() + " (" + cluster.getPrimaryJars().size() + ") jars");
        JarSet mainSet = cluster.getPrimaryJars();
        for (VersionedFqnNode fqn : cluster.getFqns()) {
          double percent = (double) fqn.getVersions().getJars().getIntersectionSize(mainSet) / (double) fqn.getVersions().getJars().size();
          writer.write("  " + fqn.getFqn() + " " + fqn.getVersions().getJars().size() + " " + format.format(percent));
        }
        writer.unindent();
      }
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Error printing statistics", e);
    }
    task.finish();
  }
}
