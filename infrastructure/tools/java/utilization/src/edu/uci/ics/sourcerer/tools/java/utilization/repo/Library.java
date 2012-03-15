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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import edu.uci.ics.sourcerer.tools.java.utilization.model.cluster.Cluster;
import edu.uci.ics.sourcerer.tools.java.utilization.model.jar.Jar;
import edu.uci.ics.sourcerer.tools.java.utilization.model.jar.JarSet;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class Library {
  private final Cluster coreCluster;
  private JarSet jars;
  private Set<Cluster> secondaryClusters;
//  private Collection<Library> dependencies;
//  private Collection<LibraryVersion> versions;
  
  private Library(Cluster coreCluster) {
    this.coreCluster = coreCluster;
    secondaryClusters = Collections.emptySet();
    jars = JarSet.create();
//    versions = Collections.emptyList();
//    dependencies = Collections.emptyList();
  }
  
  static Library createPackaging() {
    return new Library(null);
  }
  static Library create(Cluster cluster) {
    return new Library(cluster);
  }
  
  public Cluster getCoreCluster() {
    return coreCluster;
  }
  
  void addSecondaryClusters(Set<Cluster> clusters) {
    secondaryClusters = new HashSet<>(clusters);
  }
  
  public Set<Cluster> getSecondaryClusters() {
    return secondaryClusters;
  }
  
  void addJar(Jar jar) {
    jars = jars.add(jar);
  }
  
  public JarSet getJars() {
    return jars;
  }
  
//  void addDependency(Library library) {
//    if (dependencies.isEmpty()) {
//      dependencies = new HashSet<>();
//    }
//    dependencies.add(library);
//  }
//  
//  public Collection<Library> getDependencies() {
//    return dependencies;
//  }
//  
//  void addVersion(LibraryVersion version) {
//    if (versions.isEmpty()) {
//      versions = new LinkedList<>();
//    }
//    versions.add(version);
//  }
//  
//  public Collection<LibraryVersion> getVersions() {
//    return versions;
//  }
  
  @Override
  public String toString() {
    return "";
  }
}
