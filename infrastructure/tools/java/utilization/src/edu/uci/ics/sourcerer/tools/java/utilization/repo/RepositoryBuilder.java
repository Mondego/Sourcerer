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
import java.util.Set;
import java.util.TreeSet;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multiset;

import edu.uci.ics.sourcerer.tools.java.utilization.model.cluster.Cluster;
import edu.uci.ics.sourcerer.tools.java.utilization.model.cluster.ClusterCollection;
import edu.uci.ics.sourcerer.tools.java.utilization.model.cluster.ClusterMatcher;
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
    
    
    TreeSet<Cluster> sortedClusters = new TreeSet<>(Cluster.ASCENDING_SIZE_COMPARATOR);
    sortedClusters.addAll(clusters.getClusters());
    
    Collection<Library> libraries = new LinkedList<>();
    task.start("Identifying libraries", "clusters examined");
    int count = 1;
    Multimap<Jar, Integer> map = HashMultimap.create();
    while (!sortedClusters.isEmpty()) {
      Cluster smallest = sortedClusters.pollFirst();
      // Make all the jars that match this cluster into a library
      for (Jar jar : smallest.getJars()) {
        map.put(jar, count);
      }
      System.out.println(count + ": " + smallest.getJars() + " " + smallest);
      count++;
//      // Find all the ways that this cluster is expressed
//      Map<Set<FqnVersion>, JarSet> versions = new HashMap<>();
//      for (Jar jar : smallest.getJars()) {
//        Set<FqnVersion> version = new HashSet<>();
//        for (VersionedFqnNode fqn : smallest.getCoreFqns()) {
//          version.add(fqn.getVersion(jar));
//        }
//        for (VersionedFqnNode fqn : smallest.getVersionFqns()) {
//          version.add(fqn.getVersion(jar));
//        }
//        JarSet matched = versions.get(version);
//        if (matched == null) {
//          versions.put(version, JarSet.create(jar));
//        } else {
//          versions.put(version, matched.add(jar));
//        }
//      }
      
//      Set<VersionedFqnNode> globalPotentials = new HashSet<>();
//      Set<VersionedFqnNode> globalPartials = new HashSet<>();
//      Set<Set<Cluster>> globalCombinations = new HashSet<>();
//      // For each version, find any fqns that always occur
//      for (JarSet matched : versions.values()) {
//        Multiset<FqnVersion> potentials = HashMultiset.create();
//        for (Jar jar : matched) {
//          for (FqnVersion version : jar.getFqns()) {
//            potentials.add(version);
//          }
//        }
//        
//        Set<Cluster> combinations = new HashSet<>(); 
//        int max = matched.size();
//        for (FqnVersion fqn : potentials.elementSet()) {
//          if (potentials.count(fqn) == max) {
//            globalPotentials.add(fqn.getFqn());
//            combinations.add(matcher.getCluster(fqn.getFqn()));
//          } else {
//            globalPartials.add(fqn.getFqn());
//          }
//        }
//      }
//      globalPotentials.removeAll(globalPartials);
      
    }
    
    for (Jar jar : map.keySet()) {
      System.out.println(jar + " " + map.get(jar));
    }
    
    
    
    
//    // Build the initial mapping to libraries
//
//    
//    // Compute dependencies between libraries
//    for (Library library: clusterSetMap.values()) {
//      // No dependencies if it's a single cluster
//      if (library.getClusters().size() > 1) {
//        // Find all libraries that are a subset of this cluster
//        for (Cluster cluster : library.getClusters()) {
//          for (Library subLib : clusterMap.get(cluster)) {
//            // Does our current library subsume this library
//            if (library != subLib && library.getClusters().containsAll(subLib.getClusters())) {
//              library.addDependency(subLib);
//            }
//          }
//        }
//      }
//    }
//    
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
