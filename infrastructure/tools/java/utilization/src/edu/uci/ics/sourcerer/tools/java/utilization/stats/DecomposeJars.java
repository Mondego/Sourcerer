///* 
// * Sourcerer: an infrastructure for large-scale source code analysis.
// * Copyright (C) by contributors. See CONTRIBUTORS.txt for full list.
// *
// * This program is free software: you can redistribute it and/or modify
// * it under the terms of the GNU General Public License as published by
// * the Free Software Foundation, either version 3 of the License, or
// * (at your option) any later version.
//
// * This program is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// * GNU General Public License for more details.
//
// * You should have received a copy of the GNU General Public License
// * along with this program. If not, see <http://www.gnu.org/licenses/>.
// */
//package edu.uci.ics.sourcerer.tools.java.utilization.stats;
//
//import static edu.uci.ics.sourcerer.util.io.logging.Logging.logger;
//
//import java.io.File;
//import java.io.IOException;
//import java.util.Collection;
//import java.util.HashSet;
//import java.util.Set;
//import java.util.TreeSet;
//import java.util.logging.Level;
//
//import edu.uci.ics.sourcerer.tools.java.utilization.model.cluster.Cluster;
//import edu.uci.ics.sourcerer.tools.java.utilization.model.cluster.ClusterCollection;
//import edu.uci.ics.sourcerer.tools.java.utilization.model.cluster.ClusterMatcher;
//import edu.uci.ics.sourcerer.tools.java.utilization.model.jar.Jar;
//import edu.uci.ics.sourcerer.tools.java.utilization.model.jar.JarCollection;
//import edu.uci.ics.sourcerer.tools.java.utilization.model.jar.VersionedFqnNode;
//import edu.uci.ics.sourcerer.util.io.IOUtils;
//import edu.uci.ics.sourcerer.util.io.LogFileWriter;
//import edu.uci.ics.sourcerer.util.io.arguments.Argument;
//import edu.uci.ics.sourcerer.util.io.arguments.Arguments;
//import edu.uci.ics.sourcerer.util.io.arguments.FileArgument;
//import edu.uci.ics.sourcerer.util.io.arguments.RelativeFileArgument;
//import edu.uci.ics.sourcerer.util.io.logging.TaskProgressLogger;
//
///**
// * @author Joel Ossher (jossher@uci.edu)
// */
//public class DecomposeJars {
//  public static Argument<File> TEST_REPO = new FileArgument("test-repo", "Repository containing test jar files.");
//  public static Argument<File> TEST_REPO_CACHE = new RelativeFileArgument("test-repo-cache", "test-repo-cache", Arguments.CACHE, "Test repo cache directory.").permit();
//  public static RelativeFileArgument DECOMPOSED_JAR_LISTING = new RelativeFileArgument("decomposed-jar-listing", null, Arguments.OUTPUT, "List of jars decomposed into clusters");
//  
//  public static void decomposeJars(ClusterCollection clusters) {
//    if (DECOMPOSED_JAR_LISTING.getValue() == null) {
//      return;
//    }
//    TaskProgressLogger task = TaskProgressLogger.get();
//    
//    JarCollection jars = JarCollection.create(TEST_REPO, TEST_REPO_CACHE);
//    
//    task.start("Decomposing " + jars.size() + " test jars into clusters", "jars processed", 100);
//    
//    ClusterMatcher matcher = clusters.getClusterMatcher();
//    try (LogFileWriter log = IOUtils.createLogFileWriter(DECOMPOSED_JAR_LISTING)) {
//      for (Jar jar : jars) {
//        Set<String> fqns = new HashSet<>();
//        for (VersionedFqnNode fqn : jar.getFqns()) {
//          fqns.add(fqn.getFqn());
//        }
//        Collection<Cluster> matched = matcher.getClusters(fqns);
//        log.writeAndIndent(jar + " decomposed into " + matched.size() + " clusters");
//        
//        int clusterCount = 0;
//        for (Cluster cluster : matched) {
//          log.writeAndIndent("Cluster " + ++clusterCount);
//          
//          // Start with the core FQNs
//          log.writeAndIndent("Core FQNs");
//          Set<VersionedFqnNode> coreFqns = new TreeSet<>(cluster.getCoreFqns());
//          for (VersionedFqnNode fqnNode : coreFqns) {
//            log.write(fqnNode.getFqn());
//          }
//          log.unindent();
//          
//          // Now the exemplar non-core FQNs
//          Set<String> nonCoreExemplar = new TreeSet<>();
//          Set<String> missingExemplar = new TreeSet<>();
//          for (VersionedFqnNode fqn : cluster.getExemplarFqns()) {
//            if (!coreFqns.contains(fqn)) {
//              String fqnString = fqn.getFqn();
//              if (fqns.contains(fqnString)) {
//                nonCoreExemplar.add(fqnString);
//              } else {
//                missingExemplar.add(fqnString);
//              }
//            }
//          }
//          
//          if (!nonCoreExemplar.isEmpty()) {
//            log.writeAndIndent("Non-core Exemplar FQNs");
//            for (String fqn : nonCoreExemplar) {
//              log.write(fqn);
//            }
//            log.unindent();
//          }
//          
//          if (!missingExemplar.isEmpty()) {
//            log.writeAndIndent("Missing Exemplar FQNs");
//            for (String fqn : missingExemplar) {
//              log.write(fqn);
//            }
//            log.unindent();
//          }
//          
//          // See if there are any other extra fqns
//          int missing = 0;
//          Collection<String> extraFqns = new TreeSet<>();
//          for (VersionedFqnNode fqn : cluster.getExtraFqns()) {
//            String fqnString = fqn.getFqn();
//            if (!nonCoreExemplar.contains(fqnString) && !missingExemplar.contains(fqnString)) {
//              if (fqns.contains(fqnString)) {
//                extraFqns.add(fqnString);
//              } else {
//                missing++;
//              }
//            }
//          }
//          
//          if (!extraFqns.isEmpty()) {
//            log.writeAndIndent("Extra FQNs");
//            for (String fqn : extraFqns) {
//              log.write(fqn);
//            }
//            if (missing > 0) {
//              log.write(missing + " extra FQNs in cluster missing from jar");
//            }
//            log.unindent();
//          } else if (missing > 0){
//            log.writeAndIndent("Extra FQNs");
//            log.write(missing + " extra FQNs in cluster missing from jar");
//            log.unindent();
//          }
//          log.unindent();
//        }
//        log.unindent();
//      }
//      task.progress();
//    } catch (IOException e) {
//      logger.log(Level.SEVERE, "Error writing log", e);
//    }
//    task.finish();
//  }
//}
