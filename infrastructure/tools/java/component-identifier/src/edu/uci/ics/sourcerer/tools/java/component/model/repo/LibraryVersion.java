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
package edu.uci.ics.sourcerer.tools.java.component.model.repo;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Set;

import edu.uci.ics.sourcerer.tools.java.component.model.cluster.ClusterVersion;
import edu.uci.ics.sourcerer.tools.java.component.model.jar.FqnVersion;
import edu.uci.ics.sourcerer.tools.java.component.model.jar.Jar;
import edu.uci.ics.sourcerer.tools.java.component.model.jar.JarSet;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class LibraryVersion {
  private JarSet jars;
  private final Set<FqnVersion> fqnVersions;
  private final Set<ClusterVersion> clusters;
  private Collection<Library> libraryDependencies;
  private Collection<LibraryVersion> versionDependencies;
  
  private LibraryVersion(Jar jar, Set<FqnVersion> fqnVersions, Set<ClusterVersion> clusters) {
    jars = JarSet.create(jar);
    this.fqnVersions = fqnVersions;
    this.clusters = clusters;
    this.libraryDependencies = Collections.emptyList();
    this.versionDependencies = Collections.emptyList();
  }
  
  public static LibraryVersion create(Jar jar, Set<FqnVersion> fqnVersions, Set<ClusterVersion> clusters) {
    return new LibraryVersion(jar, fqnVersions, clusters);
  }
  
  public void addJar(Jar jar) {
    jars = jars.add(jar);
  }
  
  public JarSet getJars() {
    return jars;
  }
  
  public Set<FqnVersion> getFqnVersions() {
    return fqnVersions;
  }
  
  public Set<ClusterVersion> getClusters() {
    return clusters;
  }
  
  public void addLibraryDependency(Library library) {
    if (libraryDependencies.isEmpty()) {
      libraryDependencies = new LinkedList<>();
    }
    libraryDependencies.add(library);
  }
  
  public Collection<Library> getLibraryDependencies() {
    return libraryDependencies;
  }
  
  public void addVersionDependency(LibraryVersion library) {
    if (versionDependencies.isEmpty()) {
      versionDependencies = new LinkedList<>();
    }
    versionDependencies.add(library);
  }
  
  public Collection<LibraryVersion> getVersionDependencies() {
    return versionDependencies;
  }
}
