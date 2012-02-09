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
package edu.uci.ics.sourcerer.tools.java.utilization.model.cluster;

import static edu.uci.ics.sourcerer.util.io.logging.Logging.logger;

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
import edu.uci.ics.sourcerer.tools.java.utilization.model.jar.Jar;
import edu.uci.ics.sourcerer.tools.java.utilization.model.jar.JarCollection;
import edu.uci.ics.sourcerer.tools.java.utilization.model.jar.VersionedFqnNode;
import edu.uci.ics.sourcerer.util.io.IOUtils;
import edu.uci.ics.sourcerer.util.io.LogFileWriter;
import edu.uci.ics.sourcerer.util.io.SimpleDeserializer;
import edu.uci.ics.sourcerer.util.io.SimpleSerializer;
import edu.uci.ics.sourcerer.util.io.arguments.Argument;
import edu.uci.ics.sourcerer.util.io.arguments.Arguments;
import edu.uci.ics.sourcerer.util.io.arguments.DualFileArgument;
import edu.uci.ics.sourcerer.util.io.arguments.RelativeFileArgument;
import edu.uci.ics.sourcerer.util.io.logging.TaskProgressLogger;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class ClusterCollection implements Iterable<Cluster> {
  private ArrayList<Cluster> clusters;
  private ClusterMatcher matcher;
  
  private ClusterCollection() {}
  
  public static ClusterCollection create(Collection<Cluster> clusters) {
    ClusterCollection collection = new ClusterCollection();
    
    collection.clusters = new ArrayList<>(clusters);
    
    return collection;
  }
  
  public static ClusterCollection load(File file, JarCollection jars) {
    TaskProgressLogger task = TaskProgressLogger.get();
    
    ClusterCollection collection = new ClusterCollection();
    
    collection.clusters = new ArrayList<>(1000);
    task.start("Loading cluster collection", "clusters loaded", 500);
    try (SimpleDeserializer deserializer = IOUtils.makeSimpleDeserializer(file)) {
      for (Cluster cluster : deserializer.deserializeToIterable(Cluster.makeDeserializer(jars), true)) {
        task.progress();
        collection.clusters.add(cluster);
      }
      // Would use a finally block, but that crashes eclipse!
      task.finish();
      return collection;
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Error loading cluster collection", e);
      task.finish();
      return null;
    }
  }
  
  public void reset(Collection<Cluster> clusters) {
    this.clusters = new ArrayList<>(clusters);
  }
  
  public void save(File file) {
    try (SimpleSerializer serializer = IOUtils.makeSimpleSerializer(file)) {
      serializer.serialize(clusters);
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Error saving cluster collection", e);
    }
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
  
  public ClusterMatcher getClusterMatcher() {
    if (matcher == null) {
      matcher = ClusterMatcher.create(this);
    }
    return matcher;
  }
  
  public void printStatistics(TaskProgressLogger task) {
    NumberFormat format = NumberFormat.getPercentInstance();
    format.setMinimumFractionDigits(2);
    format.setMaximumFractionDigits(2);

    task.start("Printing jar statistics");
    try (LogFileWriter writer = IOUtils.createLogFileWriter(JAR_LOG.getValue())) {
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
      writer.write((clusters.size() - trivialCluster) + " clusters matching multiple jars");
      
      for (Jar jar : clusterMap.keySet()) {
        Collection<Cluster> clusters = clusterMap.get(jar);
        if (clusters.size() > 1) {
          writer.newLine();
          HashSet<VersionedFqnNode> fqns = new HashSet<>(jar.getFqns());
          
          Set<Jar> otherJars = new TreeSet<>(new Comparator<Jar>() {
            @Override
            public int compare(Jar o1, Jar o2) {
              String name1 = o1.getJar().getProperties().NAME.getValue();
              String name2 = o2.getJar().getProperties().NAME.getValue();
              int cmp = name1 == null ? (name2 == null ? 0 : -1) : (name2 == null ? 1 : name1.compareTo(name2));
              if (cmp == 0) {
                return Integer.compare(o1.hashCode(), o2.hashCode());
              } else {
                return cmp;
              }
            }});
          for (Cluster cluster : clusters) {
            for (Jar otherJar : cluster.getJars()) {
              otherJars.add(otherJar);
            }
          }
          
          writer.writeAndIndent(jar.getJar().getProperties().NAME.getValue() + (jar.getJar().getProperties().VERSION.getValue() == null ? "" : " (" + jar.getJar().getProperties().VERSION.getValue() +")") + " fragmented into " + clusters.size() + " clusters");
          writer.write("FQNs from this jar appear in " + (otherJars.size() - 1) + " other jars");
          writer.writeAndIndent("Listing jars with overlap");
          int c = 1;
          for (Jar otherJar : otherJars) {
            JarProperties props = otherJar.getJar().getProperties();
            writer.write(c++ + ": " + props.NAME.getValue() + (otherJar.getJar().getProperties().VERSION.getValue() == null ? "" : " (" + otherJar.getJar().getProperties().VERSION.getValue() +")") + ": " + props.HASH.getValue() + (otherJar == jar ? " <--" : ""));
          }
          writer.unindent();
          writer.unindent();
          int clusterCount = 0;
          for (Cluster cluster : clusters) {
            writer.write("Cluster " + ++clusterCount + ", from " + cluster.getJars().size() + " jars");
            for (int i = 1, max = otherJars.size(); i <= max; i++) {
              writer.writeFragment(Integer.toString(i % 10));
            }
            // Write out the core fqns - the table should be totally filled
            writer.writeFragment(" Core FQNs");
            writer.newLine();
            
            int skipped = 0;
            for (VersionedFqnNode fqn : cluster.getCoreFqns()) {
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
            if (skipped > 0) {
              for (int i = 0; i < c; i++)
                writer.writeFragment(" ");
              logger.severe("Impossible! Core FQNs skipped.");
              writer.writeFragment(" " + skipped + " core FQNs in cluster not in this jar");
              writer.newLine();
            }
            
            if (!cluster.getExtraFqns().isEmpty()) {
              for (int i = 1, max = otherJars.size(); i <= max; i++) {
                writer.writeFragment(Integer.toString(i % 10));
              }
              // write out the extra fqns
              writer.writeFragment(" Extra FQNs");
              writer.newLine();
              skipped = 0;
              for (VersionedFqnNode fqn : cluster.getExtraFqns()) {
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
              if (skipped > 0) {
                for (int i = 0; i < c; i++)
                  writer.writeFragment(" ");
                writer.writeFragment(" " + skipped + " extra FQNs in cluster not in this jar");
                writer.newLine();
              }
            }
          }
          
          writer.newLine();
        }
      }
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Error printing statistics", e);
    }
    task.finish();
    
    task.start("Printing cluster statistics");
    try (LogFileWriter writer = IOUtils.createLogFileWriter(CLUSTER_LOG.getValue())) {
      TreeSet<Cluster> sortedClusters = new TreeSet<>(new Comparator<Cluster>() {
        @Override
        public int compare(Cluster o1, Cluster o2) {
          int cmp = Integer.compare(o1.getCoreFqns().size(), o2.getCoreFqns().size());
          if (cmp == 0) {
            return Integer.compare(o1.hashCode(), o2.hashCode());
          } else {
            return cmp;
          }
        }});
      int trivial = 0;
      for (Cluster cluster : clusters) {
        if (cluster.getJars().size() == 1) {
          trivial++;
        }
        sortedClusters.add(cluster);
      }
      
      writer.write(clusters.size() + " clusters");
      writer.write(trivial + " clusters matching a single jar");
      writer.write((sortedClusters.size() - trivial) + " clusters matching multiple jars");

      for (Cluster cluster : sortedClusters.descendingSet()) {
        writer.newLine();
        writer.writeAndIndent("Cluster of " + cluster.getJars().size() + " jars");
        
        Set<Jar> jars = new TreeSet<>(new Comparator<Jar>() {
          @Override
          public int compare(Jar o1, Jar o2) {
            String name1 = o1.getJar().getProperties().NAME.getValue();
            String name2 = o2.getJar().getProperties().NAME.getValue();
            int cmp = name1 == null ? (name2 == null ? 0 : -1) : (name2 == null ? 1 : name1.compareTo(name2));
            if (cmp == 0) {
              return Integer.compare(o1.hashCode(), o2.hashCode());
            } else {
              return cmp;
            }
          }});
        for (Jar jar : cluster.getJars()) {
          jars.add(jar);
        }
        
        writer.writeAndIndent("Listing jars in cluster");
        int c = 1;
        for (Jar jar : jars) {
          JarProperties props = jar.getJar().getProperties();
          writer.write(c++ + ": " + props.NAME.getValue() + (jar.getJar().getProperties().VERSION.getValue() == null ? "" : " (" + jar.getJar().getProperties().VERSION.getValue() +")") + ": " + props.HASH.getValue());
        }
        writer.unindent();
        
        for (int i = 1, max = jars.size(); i <= max; i++) {
          writer.writeFragment(Integer.toString(i % 10));
        }
        // Write out the core fqns - the table should be totally filled
        writer.writeFragment(" Core FQNs");
        writer.newLine();
        
        for (VersionedFqnNode fqn : cluster.getCoreFqns()) {
          for (Jar jar : jars) {
            if (fqn.getVersions().getJars().contains(jar)) {
              writer.writeFragment("*");
            } else {
              writer.writeFragment(" ");
            }
          }
          writer.writeFragment(" " + fqn.getFqn());
          writer.newLine();
        }
        
        if (!cluster.getExtraFqns().isEmpty()) {
          for (int i = 1, max = jars.size(); i <= max; i++) {
            writer.writeFragment(Integer.toString(i % 10));
          }
          // write out the extra fqns
          writer.writeFragment(" Extra FQNs");
          writer.newLine();
          for (VersionedFqnNode fqn : cluster.getExtraFqns()) {
            int count = 0;
            for (Jar jar : jars) {
              if (fqn.getVersions().getJars().contains(jar)) {
                writer.writeFragment("*");
                count++;
              } else {
                writer.writeFragment(" ");
              }
            }
            double percent = (double) count / (double) jars.size();
            writer.writeFragment(" " + fqn.getFqn() + " " + format.format(percent));
            writer.newLine();
          }
        }

        writer.unindent();
      }
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Error printing statistics", e);
    }
    task.finish();
  }
}
