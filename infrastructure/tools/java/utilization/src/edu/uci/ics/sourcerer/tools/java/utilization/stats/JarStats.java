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
package edu.uci.ics.sourcerer.tools.java.utilization.stats;

import static edu.uci.ics.sourcerer.util.io.logging.Logging.logger;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;

import com.google.common.base.Strings;

import edu.uci.ics.sourcerer.tools.java.utilization.model.cluster.Cluster;
import edu.uci.ics.sourcerer.tools.java.utilization.model.cluster.ClusterCollection;
import edu.uci.ics.sourcerer.tools.java.utilization.model.cluster.ClusterMatcher;
import edu.uci.ics.sourcerer.tools.java.utilization.model.jar.Jar;
import edu.uci.ics.sourcerer.tools.java.utilization.model.jar.JarCollection;
import edu.uci.ics.sourcerer.tools.java.utilization.model.jar.VersionedFqnNode;
import edu.uci.ics.sourcerer.util.Iterators;
import edu.uci.ics.sourcerer.util.io.IOUtils;
import edu.uci.ics.sourcerer.util.io.LogFileWriter;
import edu.uci.ics.sourcerer.util.io.arguments.Arguments;
import edu.uci.ics.sourcerer.util.io.arguments.RelativeFileArgument;
import edu.uci.ics.sourcerer.util.io.logging.TaskProgressLogger;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class JarStats {
  public static RelativeFileArgument JAR_LISTING = new RelativeFileArgument("jar-listing", null, Arguments.OUTPUT, "Log file for jars broken down by clusters");
  
  public static void calculate(JarCollection jars, ClusterCollection clusters) {
    if (JAR_LISTING.getValue() == null) {
      return;
    }
    TaskProgressLogger task = TaskProgressLogger.get();
    task.start("Logging jar listing to: " + JAR_LISTING.getValue().getPath());
    
    // Print out list of jars, ordered by number of clusters
    try (LogFileWriter log = IOUtils.createLogFileWriter(JAR_LISTING)) {
      Jar[] sorted = Iterators.toArray(jars, new Jar[jars.size()]);
      
      final ClusterMatcher matcher = clusters.getClusterMatcher();
      
      Arrays.sort(sorted, new Comparator<Jar>() {
        @Override
        public int compare(Jar one, Jar two) {
          return Integer.compare(matcher.getClusters(two).size(), matcher.getClusters(one).size());
        }});
      
      Integer maxJarsToShow = GeneralStats.MAX_TABLE_COLUMNS.getValue();
      
      for (Jar jar : sorted) {
        Collection<Cluster> jarClusters = matcher.getClusters(jar);
        
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
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Error in writing logs", e);
    }
    task.finish();
  }
}
