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
import java.util.LinkedList;
import java.util.Set;

import edu.uci.ics.sourcerer.tools.java.utilization.model.jar.Jar;
import edu.uci.ics.sourcerer.tools.java.utilization.model.jar.JarSet;
import edu.uci.ics.sourcerer.tools.java.utilization.model.jar.FqnVersion;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class LibraryVersion {
  private JarSet jars;
  private final Set<FqnVersion> fqnVersions;
  private Collection<LibraryVersion> dependencies;
  
  private LibraryVersion(Jar jar, Set<FqnVersion> fqnVersions) {
    jars = JarSet.create(jar);
    this.fqnVersions = fqnVersions;
    this.dependencies = Collections.emptyList();
  }
  
  static LibraryVersion create(Jar jar, Set<FqnVersion> fqnVersions) {
    return new LibraryVersion(jar, fqnVersions);
  }
  
  void addJar(Jar jar) {
    jars = jars.add(jar);
  }
  
  public JarSet getJars() {
    return jars;
  }
  
  public Set<FqnVersion> getFqnVersions() {
    return fqnVersions;
  }
  
  void addDependency(LibraryVersion library) {
    if (dependencies.isEmpty()) {
      dependencies = new LinkedList<>();
    }
    dependencies.add(library);
  }
  
  public Collection<LibraryVersion> getDependencies() {
    return dependencies;
  }
}
