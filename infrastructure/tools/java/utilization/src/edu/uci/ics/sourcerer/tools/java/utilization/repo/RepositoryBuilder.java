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
import java.util.Set;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import edu.uci.ics.sourcerer.tools.java.utilization.model.cluster.Cluster;
import edu.uci.ics.sourcerer.tools.java.utilization.model.cluster.ClusterCollection;
import edu.uci.ics.sourcerer.tools.java.utilization.model.cluster.ClusterMatcher;
import edu.uci.ics.sourcerer.tools.java.utilization.model.jar.Jar;
import edu.uci.ics.sourcerer.tools.java.utilization.model.jar.JarCollection;
import edu.uci.ics.sourcerer.tools.java.utilization.model.jar.FqnVersion;
import edu.uci.ics.sourcerer.tools.java.utilization.model.jar.VersionedFqnNode;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class RepositoryBuilder {
  public static Repository buildRepository(JarCollection jars, ClusterCollection clusters) {
    Map<Set<Cluster>, Library> clusterSetMap = new HashMap<>();
    Multimap<Cluster, Library> clusterMap = HashMultimap.create();
    
    ClusterMatcher matcher = clusters.getClusterMatcher();
    
    // Build the initial mapping to libraries
    for (Jar jar : jars) {
      // Find out how many clusters it matches
      Set<Cluster> matched = new HashSet<>(matcher.getClusters(jar));
      Library library = clusterSetMap.get(matched);
      if (library == null) {
        library = Library.create(matched);
        clusterSetMap.put(matched, library);
        for (Cluster cluster : matched) {
          clusterMap.put(cluster, library);
        }
      }
      library.addJar(jar);
    }
    
    // Compute dependencies between libraries
    for (Library library: clusterSetMap.values()) {
      // No dependencies if it's a single cluster
      if (library.getClusters().size() > 1) {
        // Find all libraries that are a subset of this cluster
        for (Cluster cluster : library.getClusters()) {
          for (Library subLib : clusterMap.get(cluster)) {
            // Does our current library subsume this library
            if (library != subLib && library.getClusters().containsAll(subLib.getClusters())) {
              library.addDependency(subLib);
            }
          }
        }
      }
    }
    
    // Break libraries into versions
    for (Library library : clusterSetMap.values()) {
      for (Jar jar : library.getJars()) {
        // Collect the versions of each core fqn for this jar
        Set<FqnVersion> fqnVersions = new HashSet<>();
        for (Cluster cluster : library.getClusters()) {
          for (VersionedFqnNode fqn : cluster.getCoreFqns()) {
            fqnVersions.add(fqn.getVersion(jar));
          }
          for (VersionedFqnNode fqn : cluster.getVersionFqns()) {
            FqnVersion version = fqn.getVersion(jar);
            if (version != null) {
              fqnVersions.add(version);
            }
          }
        }
        // See if an existing version is appropriate, or if it should be a new version
        LibraryVersion match = null;
        for (LibraryVersion version : library.getVersions()) {
          // Does the versions for this jar match?
          if (fqnVersions.equals(version.getFqnVersions())) {
            match = version;
            break;
          }
        }
        // Nothing matched, make a new version
        if (match == null) {
          match = LibraryVersion.create(jar, fqnVersions);
          library.addVersion(match);
        } else {
          match.addJar(jar);
        }
      }
    }
    
    // Link the versions to their dependencies
    for (Library library : clusterSetMap.values()) {
      // No dependencies if it's a single cluster
      if (library.getClusters().size() > 1) {
        // Look at each version
        for (LibraryVersion version : library.getVersions()) {
          // For every version of every dependency
          // see if anything is a subset of our version set
          for (Library dep : library.getDependencies()) {
            for (LibraryVersion depVersion : dep.getVersions()) {
              if (version.getFqnVersions().containsAll(depVersion.getFqnVersions())) {
                version.addDependency(depVersion);
              }
            }
          }
        }
      }
    }
    
    // Add the libraries to the repository
    Repository repo = Repository.create(clusterSetMap.values());
    
    // Print things out
    Map<Library, Integer> numMap = new HashMap<>();
    int count = 0;
    for (Library library : clusterSetMap.values()) {
      numMap.put(library, ++count);
    }
    Map<LibraryVersion, Integer> versionNumMap = new HashMap<>();
    count = 0;
    for (Library library : clusterSetMap.values()) {
      for (LibraryVersion version : library.getVersions()) {
        versionNumMap.put(version, ++count);
      }
    }
    for (Library library : clusterSetMap.values()) {
      logger.info(numMap.get(library) + ": Library");
      StringBuilder builder = new StringBuilder(" Depends on:");
      for (Library dep : library.getDependencies()) {
        builder.append(" ").append(numMap.get(dep));
      }
      logger.info(builder.toString());
      
      for (LibraryVersion version : library.getVersions()) {
        builder = new StringBuilder(" " + versionNumMap.get(version) + " Version: " + version.getJars().toString());
        for (LibraryVersion dep : version.getDependencies()) {
          builder.append(" ").append(versionNumMap.get(dep));
        }
        logger.info(builder.toString());
      }
    }

    return repo;
  }
}
