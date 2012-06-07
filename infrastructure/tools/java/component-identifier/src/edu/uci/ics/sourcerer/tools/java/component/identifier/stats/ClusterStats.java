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
package edu.uci.ics.sourcerer.tools.java.component.identifier.stats;

import static edu.uci.ics.sourcerer.util.io.logging.Logging.logger;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;

import com.google.common.base.Strings;

import edu.uci.ics.sourcerer.tools.java.component.model.cluster.Cluster;
import edu.uci.ics.sourcerer.tools.java.component.model.cluster.ClusterCollection;
import edu.uci.ics.sourcerer.tools.java.component.model.jar.Jar;
import edu.uci.ics.sourcerer.tools.java.component.model.jar.JarCollection;
import edu.uci.ics.sourcerer.tools.java.component.model.jar.VersionedFqnNode;
import edu.uci.ics.sourcerer.util.Iterators;
import edu.uci.ics.sourcerer.util.io.IOUtils;
import edu.uci.ics.sourcerer.util.io.LogFileWriter;
import edu.uci.ics.sourcerer.util.io.arguments.Arguments;
import edu.uci.ics.sourcerer.util.io.arguments.RelativeFileArgument;
import edu.uci.ics.sourcerer.util.io.logging.TaskProgressLogger;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class ClusterStats {
  public static RelativeFileArgument CLUSTER_LISTING = new RelativeFileArgument("cluster-listing", null, Arguments.OUTPUT, "Log file for clusters broken down by jars");
  
  public static void calculate(JarCollection jars, ClusterCollection clusters) {
    if (CLUSTER_LISTING.getValue() == null) {
      return;
    }
    TaskProgressLogger task = TaskProgressLogger.get();
    task.start("Logging cluster listing to: " + CLUSTER_LISTING.getValue().getPath());
    
    // Print out list of clusters, ordered by number of jars
    try (LogFileWriter log = IOUtils.createLogFileWriter(CLUSTER_LISTING)) {
      Cluster[] sorted = Iterators.toArray(clusters, new Cluster[clusters.size()]);
      
      Arrays.sort(sorted, new Comparator<Cluster>() {
        @Override
        public int compare(Cluster one, Cluster two) {
          return Integer.compare(two.getJars().size(), one.getJars().size());
        }});
      
      Integer maxJarsToShow = null;//GeneralStats.MAX_TABLE_COLUMNS.getValue();
      
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
            if (fqn.getJars().contains(jar)) {
              log.writeFragment("*");
            } else {
              log.writeFragment(" ");
            }
          }
          log.writeFragment(" " + fqn.getFqn());
          log.newLine();
        }
        coreFqns = cluster.getCoreFqns();
        
        // Log the versioned core fqns
        if (!cluster.getVersionFqns().isEmpty()) {
          // Log the number line
          for (int i = 1; i <= c; i++) {
            log.writeFragment(Integer.toString(i % 10));
          }
          log.writeFragment(" Versioned Core FQNs");
          log.newLine();
          
          Collection<VersionedFqnNode> versionedCoreFqns = new TreeSet<>(cluster.getVersionFqns());
          for (VersionedFqnNode fqn : versionedCoreFqns) {
            for (Jar jar : containedJars) {
              // Does this jar contain the FQN?
              if (fqn.getJars().contains(jar)) {
                log.writeFragment("*");
              } else {
                log.writeFragment(" ");
              }
            }
            log.writeFragment(" " + fqn.getFqn());
            log.newLine();
          }
        }
        
//        // See if there are any other extra fqns
//        Collection<VersionedFqnNode> extraFqns = new TreeSet<>();
//        for (VersionedFqnNode fqn : cluster.getExtraFqns()) {
//          if (!nonCoreExemplar.contains(fqn)) {
//            extraFqns.add(fqn);
//          }
//        }
//         
//        // Log the extra fqns
//        if (!extraFqns.isEmpty()) {
//          // Log the number line
//          for (int i = 1; i <= c; i++) {
//            log.writeFragment(Integer.toString(i % 10));
//          }
//          log.writeFragment(" Extra FQNs");
//          log.newLine();
//          
//          for (VersionedFqnNode fqn : extraFqns) {
//            for (Jar jar : containedJars) {
//              // Does this other jar contain the FQN?
//              if (fqn.getVersions().getJars().contains(jar)) {
//                log.writeFragment("*");
//              } else {
//                log.writeFragment(" ");
//              }
//            }
//            log.writeFragment(" " + fqn.getFqn());
//            log.newLine();
//          }
//        } 
      }
    } catch (IOException e)  {
      logger.log(Level.SEVERE, "Error in writing logs", e);
    }
    task.finish();
  }
}
