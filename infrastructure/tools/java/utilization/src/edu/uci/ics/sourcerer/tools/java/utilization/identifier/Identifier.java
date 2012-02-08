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

import edu.uci.ics.sourcerer.tools.java.utilization.model.cluster.Cluster;
import edu.uci.ics.sourcerer.tools.java.utilization.model.cluster.ClusterCollection;
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
