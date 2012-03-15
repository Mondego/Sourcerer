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
package edu.uci.ics.sourcerer.tools.java.utilization.repo;

import static edu.uci.ics.sourcerer.util.io.logging.Logging.logger;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.TreeSet;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multiset;

import edu.uci.ics.sourcerer.tools.java.utilization.model.cluster.Cluster;
import edu.uci.ics.sourcerer.tools.java.utilization.model.cluster.ClusterCollection;
import edu.uci.ics.sourcerer.tools.java.utilization.model.cluster.ClusterMatcher;
import edu.uci.ics.sourcerer.tools.java.utilization.model.cluster.ClusterVersion;
import edu.uci.ics.sourcerer.tools.java.utilization.model.jar.Jar;
import edu.uci.ics.sourcerer.tools.java.utilization.model.jar.JarCollection;
import edu.uci.ics.sourcerer.tools.java.utilization.model.jar.FqnVersion;
import edu.uci.ics.sourcerer.tools.java.utilization.model.jar.JarSet;
import edu.uci.ics.sourcerer.tools.java.utilization.model.jar.VersionedFqnNode;
import edu.uci.ics.sourcerer.util.io.logging.TaskProgressLogger;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class RepositoryBuilder {
  public static Repository buildRepository(JarCollection jars, ClusterCollection clusters) {
    TaskProgressLogger task = TaskProgressLogger.get();
    
    task.start("Building repository structure for " + jars.size() + " jars and " + clusters.size() + " clusters");
    
    ClusterMatcher matcher = clusters.getClusterMatcher();
    
//    task.start("Identifying matching packaging", "jars examined");
//    Map<Set<Cluster>, Library> clusterSetMap = new HashMap<>();
//    Multimap<Cluster, Library> clusterMap = HashMultimap.create();
//    for (Jar jar : jars) {
//      // Find out how many clusters it matches
//      Set<Cluster> matched = new HashSet<>(matcher.getClusters(jar));
//      Library library = clusterSetMap.get(matched);
//      if (library == null) {
//        library = Library.create(matched);
//        clusterSetMap.put(matched, library);
//        for (Cluster cluster : matched) {
//          clusterMap.put(cluster, library);
//        }
//      }
//      library.addJar(jar);
//    }
//    task.finish();
    
    task.start("Mapping jars to clusters", "clusters examined");
    Multimap<Jar, Cluster> jarsToClusters = HashMultimap.create();
    for (Cluster cluster : clusters) {
      for (Jar jar : cluster.getJars()) {
        jarsToClusters.put(jar, cluster);
      }
      task.progress();
    }
    task.finish();
    
    PriorityQueue<Cluster> sortedClusters = new PriorityQueue<>(clusters.size(), Cluster.DESCENDING_SIZE_COMPARATOR);
    sortedClusters.addAll(clusters.getClusters());
    
    Map<Cluster, Library> libraries = new HashMap<>();
    JarSet assignedJars = JarSet.create();
    
    task.start("Creating libraries from clusters", "clusters examined");
    while (!sortedClusters.isEmpty()) {
      Cluster biggest = sortedClusters.poll();
      
      Library library = Library.create(biggest);
      libraries.put(biggest, library);
      
      Set<FqnVersion> globalPotentials = new HashSet<>();
      Set<FqnVersion> globalPartials = new HashSet<>();
      
      // For each version, find any fqns that always occur
      for (ClusterVersion version : biggest.getVersions()) {
        Multiset<FqnVersion> potentials = HashMultiset.create();
        for (Jar jar : version.getJars()) {
          for (FqnVersion fqn : jar.getFqns()) {
            potentials.add(fqn);
          }
        }

        int max = version.getJars().size();
        for (FqnVersion fqn : potentials) {
          if (potentials.count(fqn) == max) {
            globalPotentials.add(fqn);
          } else {
            globalPartials.add(fqn);
          }
        }
      }
      globalPotentials.removeAll(globalPartials);
      // Are there any clusters that we match?
      Set<Cluster> newClusters = new HashSet<>();
      Set<ClusterVersion> possibleClusterVersions = new HashSet<>();
      for (FqnVersion fqn : globalPotentials) {
        possibleClusterVersions.addAll(matcher.getClusterVersions(fqn));
      }
      for (ClusterVersion version : possibleClusterVersions) {
        if (globalPotentials.containsAll(version.getFqns())) {
          newClusters.add(version.getCluster());
        }
      }
      // Add all the secondary clusters
      library.addSecondaryClusters(newClusters);
      
      
      // Add the jars to the library
      for (Jar jar : biggest.getJars()) {
        boolean addMe = true;
        for (Cluster cluster : jarsToClusters.get(jar)) {
          if (cluster != biggest && !library.getSecondaryClusters().contains(cluster)) {
            addMe = false;
            break;
          }
        }
        if (addMe) {
          library.addJar(jar);
          assignedJars = assignedJars.add(jar);
        }
      }
      
      // Split the jars into versions
      
      task.progress();
    }
    task.finish();
    
    task.start("Creating libraries from unassigned jars", "jars examined");
    Map<Set<Cluster>, Library> packages = new HashMap<>();
    Multimap<Cluster, Library> clusterToPackage = HashMultimap.create();
    for (Jar jar : jars) {
      if (!assignedJars.contains(jar)) {
        Set<Cluster> matched = new HashSet<>(jarsToClusters.get(jar));
        Library library = packages.get(matched);
        if (library == null) {
          library = Library.createPackaging();
          library.addSecondaryClusters(matched);
          packages.put(matched, library);
          for (Cluster cluster : matched) {
            clusterToPackage.put(cluster, library);
          }
        }
        library.addJar(jar);
        task.progress();
      }
    }
    task.finish();
    
//    // Hook up the dependencies
//    for (Library library : libraries.values()) {
//      // No dependencies if it's a single cluster
//      if (!library.getSecondaryClusters().isEmpty()) {
//        // Add the libraries for each secondary cluster
//        for (Cluster cluster : library.getSecondaryClusters()) {
//          library.addDependency(libraries.get(cluster));
//        }
//    }

//    // Break libraries into versions
//    for (Library library : clusterSetMap.values()) {
//      for (Jar jar : library.getJars()) {
//        // Collect the versions of each core fqn for this jar
//        Set<FqnVersion> fqnVersions = new HashSet<>();
//        for (Cluster cluster : library.getClusters()) {
//          for (VersionedFqnNode fqn : cluster.getCoreFqns()) {
//            fqnVersions.add(fqn.getVersion(jar));
//          }
//          for (VersionedFqnNode fqn : cluster.getVersionFqns()) {
//            FqnVersion version = fqn.getVersion(jar);
//            if (version != null) {
//              fqnVersions.add(version);
//            }
//          }
//        }
//        // See if an existing version is appropriate, or if it should be a new version
//        LibraryVersion match = null;
//        for (LibraryVersion version : library.getVersions()) {
//          // Does the versions for this jar match?
//          if (fqnVersions.equals(version.getFqnVersions())) {
//            match = version;
//            break;
//          }
//        }
//        // Nothing matched, make a new version
//        if (match == null) {
//          match = LibraryVersion.create(jar, fqnVersions);
//          library.addVersion(match);
//        } else {
//          match.addJar(jar);
//        }
//      }
//    }
//    
//    // Link the versions to their dependencies
//    for (Library library : clusterSetMap.values()) {
//      // No dependencies if it's a single cluster
//      if (library.getClusters().size() > 1) {
//        // Look at each version
//        for (LibraryVersion version : library.getVersions()) {
//          // For every version of every dependency
//          // see if anything is a subset of our version set
//          for (Library dep : library.getDependencies()) {
//            for (LibraryVersion depVersion : dep.getVersions()) {
//              if (version.getFqnVersions().containsAll(depVersion.getFqnVersions())) {
//                version.addDependency(depVersion);
//              }
//            }
//          }
//        }
//      }
//    }
    
    // Add the libraries to the repository
//    Repository repo = Repository.create(clusterSetMap.values());
    
    // Print things out
//    Map<Library, Integer> numMap = new HashMap<>();
//    int count = 0;
//    for (Library library : clusterSetMap.values()) {
//      numMap.put(library, ++count);
//    }
//    Map<LibraryVersion, Integer> versionNumMap = new HashMap<>();
//    count = 0;
//    for (Library library : clusterSetMap.values()) {
//      for (LibraryVersion version : library.getVersions()) {
//        versionNumMap.put(version, ++count);
//      }
//    }
//    for (Library library : clusterSetMap.values()) {
//      logger.info(numMap.get(library) + ": Library");
//      StringBuilder builder = new StringBuilder(" Depends on:");
//      for (Library dep : library.getDependencies()) {
//        builder.append(" ").append(numMap.get(dep));
//      }
//      logger.info(builder.toString());
//      
//      for (LibraryVersion version : library.getVersions()) {
//        builder = new StringBuilder(" " + versionNumMap.get(version) + " Version: " + version.getJars().toString());
//        for (LibraryVersion dep : version.getDependencies()) {
//          builder.append(" ").append(versionNumMap.get(dep));
//        }
//        logger.info(builder.toString());
//      }
//    }
    return null;
//    return repo;
  }
}
