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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multiset;

import edu.uci.ics.sourcerer.tools.java.utilization.model.cluster.Cluster;
import edu.uci.ics.sourcerer.tools.java.utilization.model.cluster.ClusterCollection;
import edu.uci.ics.sourcerer.tools.java.utilization.model.cluster.ClusterVersion;
import edu.uci.ics.sourcerer.tools.java.utilization.model.jar.FqnVersion;
import edu.uci.ics.sourcerer.tools.java.utilization.model.jar.Jar;
import edu.uci.ics.sourcerer.tools.java.utilization.model.jar.JarCollection;
import edu.uci.ics.sourcerer.tools.java.utilization.model.jar.JarSet;
import edu.uci.ics.sourcerer.tools.java.utilization.model.jar.VersionedFqnNode;
import edu.uci.ics.sourcerer.util.CollectionUtils;
import edu.uci.ics.sourcerer.util.io.logging.TaskProgressLogger;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class RepositoryBuilder {
  private final TaskProgressLogger task;
  
  private final JarCollection jars;
  private final ClusterCollection clusters;
//  private final ClusterMatcher matcher;
  
  private Multimap<Jar, Cluster> jarsToClusters;
  private Repository repo;
  
  private RepositoryBuilder(JarCollection jars, ClusterCollection clusters) {
    this.task = TaskProgressLogger.get();
    
    this.jars = jars;
    this.clusters = clusters;
    
//    this.matcher = clusters.getClusterMatcher();
    
    this.jarsToClusters = HashMultimap.create();
    
    this.repo = Repository.create(); 
  }
  
  public static Repository buildRepository(JarCollection jars, ClusterCollection clusters) {
    RepositoryBuilder builder = new RepositoryBuilder(jars, clusters);
    return builder.buildRepository();
  }
  
  private void buildJarToClusterMap() {
    task.start("Mapping jars to clusters", "clusters examined");
    for (Cluster cluster : clusters) {
      for (Jar jar : cluster.getJars()) {
        jarsToClusters.put(jar, cluster);
      }
      task.progress();
    }
    task.finish();
  }
  
  private JarSet createSimpleLibraries() {
    JarSet assignedJars = JarSet.create();
    
    PriorityQueue<Cluster> sortedClusters = new PriorityQueue<>(clusters.size(), Cluster.DESCENDING_SIZE_COMPARATOR);
    Map<VersionedFqnNode, Cluster> fqnToCluster = new HashMap<>();
    for (Cluster cluster : clusters) {
      sortedClusters.add(cluster);
      for (VersionedFqnNode fqn : cluster.getCoreFqns()) {
        fqnToCluster.put(fqn, cluster);
      }
      for (VersionedFqnNode fqn : cluster.getVersionFqns()) {
        fqnToCluster.put(fqn, cluster);
      }
    }

    task.start("Creating simple libraries from clusters", "clusters examined", 500);
    while (!sortedClusters.isEmpty()) {
      Cluster biggest = sortedClusters.poll();
      
      Library library = Library.create(biggest);
      repo.addLibrary(library);
      {
        Set<VersionedFqnNode> globalPotentials = new HashSet<>();
        Set<VersionedFqnNode> globalPartials = new HashSet<>();
        
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
              globalPotentials.add(fqn.getFqn());
            } else {
              globalPartials.add(fqn.getFqn());
            }
          }
        }
        globalPotentials.removeAll(globalPartials);
        globalPotentials.removeAll(biggest.getCoreFqns());
        globalPotentials.removeAll(biggest.getVersionFqns());
        // Are there any clusters that we match?
        Set<Cluster> potentialClusters = new HashSet<>();
        for (VersionedFqnNode fqn : globalPotentials) {
          Cluster cluster = fqnToCluster.get(fqn);
          if (cluster == null) {
            logger.severe("Missing cluster for FQN: " + fqn.getFqn());
          } else {
            potentialClusters.add(cluster);
          }
        }
        for (Cluster cluster : potentialClusters) {
          if (globalPotentials.containsAll(cluster.getCoreFqns()) && CollectionUtils.containsNone(globalPartials, cluster.getVersionFqns())) {
            library.addSecondaryCluster(cluster);
          }
        }
      }
      
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
      splitLibaryIntoVersions(library);
      
      task.progress();
    }
    task.finish();
    
    return assignedJars;
  }
  
  private void createCompoundLibraries(JarSet assignedJars) {
    task.start("Creating compound libraries from unassigned jars", "jars examined");
    Map<Set<Cluster>, Library> packages = new HashMap<>();
    for (Jar jar : jars) {
      if (!assignedJars.contains(jar)) {
        Set<Cluster> matched = new HashSet<>(jarsToClusters.get(jar));
        Library library = packages.get(matched);
        if (library == null) {
          library = Library.createPackaging();
          library.addSecondaryClusters(matched);
          packages.put(matched, library);
        }
        library.addJar(jar);
        task.progress();
      }
    }
    
    // Split them into versions and put them into the repo
    for (Library library : packages.values()) {
      splitLibaryIntoVersions(library);
      repo.addLibrary(library);
    }
    task.finish();
  }
  
  private void splitLibaryIntoVersions(Library library) {
    // Build up the set of fqns to compare
    Set<VersionedFqnNode> fqns = new HashSet<>();
    Cluster coreCluster = library.getCoreCluster();
    if (coreCluster != null) {
      fqns.addAll(coreCluster.getCoreFqns());
      fqns.addAll(coreCluster.getVersionFqns());
    }
    for (Cluster cluster : library.getSecondaryClusters()) {
      fqns.addAll(cluster.getCoreFqns());
      fqns.addAll(cluster.getVersionFqns());
    }
    
    // Go through each jar, and find its version
    for (Jar jar : library.getJars()) {
      Set<FqnVersion> version = new HashSet<>();
      for (FqnVersion fqn : jar.getFqns()) {
        if (fqns.contains(fqn.getFqn())) {
          version.add(fqn);
        }
      }
      
      LibraryVersion libVersion = library.getVersion(version);
      if (libVersion == null) {
        Set<ClusterVersion> clusterVersions = new HashSet<>();
        outer:
        for (Cluster cluster : jarsToClusters.get(jar)) {
          for (ClusterVersion clusterVersion : cluster.getVersions()) {
            if (version.containsAll(clusterVersion.getFqns())) {
              clusterVersions.add(clusterVersion);
              continue outer;
            }
          }
          logger.severe("Unable to find matching cluster version!");
        }
        libVersion = LibraryVersion.create(jar, version, clusterVersions);
        library.addVersion(libVersion);
      } else {
        libVersion.addJar(jar);
      }
    }
  }
  
  
  private void computeLibraryDependencies() {
    task.start("Computing library version to library dependencies");
    {
      // Build map from Clusters to Libraries
      Multimap<Cluster, Library> clustersToLibraries = HashMultimap.create();
      for (Library library : repo.getLibraries()) {
        if (library.getCoreCluster() != null) {
          clustersToLibraries.put(library.getCoreCluster(), library);
        }
        for (Cluster cluster : library.getSecondaryClusters()) {
          clustersToLibraries.put(cluster, library);
        }
      }
      
      for (Library library : repo.getLibraries()) {
        for (LibraryVersion version : library.getVersions()) {
          Multiset<Library> librarySet = HashMultiset.create();
          for (ClusterVersion clusterVersion : version.getClusters()) {
            librarySet.addAll(clustersToLibraries.get(clusterVersion.getCluster()));
          }
          
          for (Library dep : librarySet.elementSet()) {
            if (library != dep) {
              if (dep.getCoreCluster() == null) {
                // Must match every secondary cluster for package libraries
                if (librarySet.count(dep) == dep.getSecondaryClusters().size()) {
                  version.addLibraryDependency(dep);
                }
              } else {
                // See if there's a jar in this library that matches the right clusters
                for (Jar jar : dep.getJars()) {
                  if (version.getClusters().containsAll(jarsToClusters.get(jar))) {
                    version.addLibraryDependency(dep);
                    break;
                  }
                }
              }
            }
          }
        }
      }
    }
    task.finish();
  }
  
  private void computeVersionDependencies() {
    task.start("Computing library version to version dependencies");
    // Build map from FqnVersions to LibraryVersions
    Multimap<FqnVersion, LibraryVersion> fqnVersionToLibVersion = HashMultimap.create();
    for (Library library : repo.getLibraries()) {
      for (LibraryVersion version : library.getVersions()) {
        for (FqnVersion fqn : version.getFqnVersions()) {
          fqnVersionToLibVersion.put(fqn, version);
        }
      }
    }
    
    for (Library library : repo.getLibraries()) {
      for (LibraryVersion version : library.getVersions()) {
        // For each version of the library, look up all the libraries that contain that fqn
        Multiset<LibraryVersion> versionSet = HashMultiset.create();
        for (FqnVersion fqn : version.getFqnVersions()) {
          versionSet.addAll(fqnVersionToLibVersion.get(fqn));
        }
        
        // See if any other library contains a subset of the fqn versions for this library
        for (LibraryVersion libVersion : versionSet.elementSet()) {
          if (version != libVersion && versionSet.count(libVersion) == libVersion.getFqnVersions().size()) {
            version.addVersionDependency(libVersion);
          }
        }
      }
    }
    task.finish();
  }
  
  public Repository buildRepository() {
    task.start("Building repository structure for " + jars.size() + " jars and " + clusters.size() + " clusters");
    
    buildJarToClusterMap();
    createCompoundLibraries(createSimpleLibraries());
    computeLibraryDependencies();
    computeVersionDependencies();
    
    task.finish();
//    // Print things out
//    Map<Library, Integer> numMap = new HashMap<>();
//    int count = 0;
//    for (Library library : repo.getLibraries()) {
//      numMap.put(library, ++count);
//    }
//    Map<LibraryVersion, Integer> versionNumMap = new HashMap<>();
//    count = 0;
//    for (Library library : repo.getLibraries()) {
//      for (LibraryVersion version : library.getVersions()) {
//        versionNumMap.put(version, ++count);
//      }
//    }
//    for (Library library : repo.getLibraries()) {
//      logger.info(numMap.get(library) + ": Library " + (library.getCoreCluster() == null ? "package" : "core"));
//      for (LibraryVersion version : library.getVersions()) {
//        StringBuilder builder = new StringBuilder(" " + versionNumMap.get(version) + " Version: " + version.getJars().toString());
//        for (Library dep : version.getLibraryDependencies()) {
//          builder.append(" L").append(numMap.get(dep));
//        }
//        for (LibraryVersion dep : version.getVersionDependencies()) {
//          builder.append(" V").append(versionNumMap.get(dep));
//        }
//        logger.info(builder.toString());
//      }
//    }
    return repo;
  }
}
