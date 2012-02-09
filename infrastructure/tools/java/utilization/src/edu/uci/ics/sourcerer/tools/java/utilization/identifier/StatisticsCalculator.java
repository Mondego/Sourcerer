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
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.logging.Level;

import edu.uci.ics.sourcerer.tools.java.utilization.model.cluster.ClusterCollection;
import edu.uci.ics.sourcerer.tools.java.utilization.model.jar.JarCollection;
import edu.uci.ics.sourcerer.tools.java.utilization.model.jar.VersionedFqnNode;
import edu.uci.ics.sourcerer.util.Averager;
import edu.uci.ics.sourcerer.util.io.IOUtils;
import edu.uci.ics.sourcerer.util.io.LogFileWriter;
import edu.uci.ics.sourcerer.util.io.arguments.Arguments;
import edu.uci.ics.sourcerer.util.io.arguments.RelativeFileArgument;
import edu.uci.ics.sourcerer.util.io.logging.TaskProgressLogger;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class StatisticsCalculator {
  public static RelativeFileArgument JAR_LOG = new RelativeFileArgument("jar-log", null, Arguments.OUTPUT, "Log file for jars broken down by clusters");
  public static RelativeFileArgument CLUSTER_LOG = new RelativeFileArgument("cluster-log", null, Arguments.OUTPUT, "Log file for clusters broken down by jars");
  
  public static RelativeFileArgument GENERAL_STATS = new RelativeFileArgument("general-stats", "general-stats.txt", Arguments.OUTPUT, "File containing general stats.");
  public static RelativeFileArgument POPULAR_FQNS = new RelativeFileArgument("popular-fqns", null, Arguments.OUTPUT, "Log file containing the most popular FQNs.");
  
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
      writer.write("Smallest jar contains " + avgFqns.getMin() + " class files");
      writer.write("Largest jar contains " + avgFqns.getMax() + " class files");
      writer.write("Average jar contains " + doubleFormat.format(avgFqns.getMean()) + " (" + doubleFormat.format(avgFqns.getStandardDeviation()) + ") class files");
      
      task.finish();
      
      File popularFqnsLog = POPULAR_FQNS.getValue();
      if (popularFqnsLog != null) {
        task.start("Logging most popular FQNs to: " + popularFqnsLog.getPath());
        
        try (LogFileWriter log = IOUtils.createLogFileWriter(popularFqnsLog)) {
          // Put the FQNs into an array
          VersionedFqnNode fqns[] = new VersionedFqnNode[uniqueFqns];
          int i = 0;
          for (VersionedFqnNode fqn : jars.getRoot().getLeavesIterable()) {
            fqns[i++] = fqn;
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
      // How many clusters?
      // How many clusters contain only 1 jar?
      // What's the largest/smallest/average number of jars in a cluster?
      // What's the largest/smallest/average number of core/extra/exemplar FQNs per cluster?
     
      // How many jars match only a single cluster?
      // What's the largest/smallest/average number of clusters for a jar?
      
      // How many clusters have a 'good' exemplar
      
      // Print out list of clusters, ordered by number of jars
      // Print out list of jars, ordered by number of clusters
      task.finish();
      
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Error in writing logs", e);
    } finally {
      checkpoint.activate();
    }
  }
}
