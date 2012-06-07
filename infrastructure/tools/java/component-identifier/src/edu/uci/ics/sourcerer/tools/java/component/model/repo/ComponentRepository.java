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

import java.util.ArrayList;
import java.util.Collection;

import edu.uci.ics.sourcerer.tools.java.component.model.cluster.ClusterCollection;
import edu.uci.ics.sourcerer.tools.java.component.model.jar.JarCollection;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class ComponentRepository {
  private final JarCollection jars;
  private final ClusterCollection clusters;
  private final Collection<Library> libraries;
  
  private ComponentRepository(JarCollection jars, ClusterCollection clusters) {
    this.jars = jars;
    this.clusters = clusters;
    this.libraries = new ArrayList<>();
  }
  
  public static ComponentRepository create(JarCollection jars, ClusterCollection clusters) {
    return new ComponentRepository(jars, clusters);
  }
  
  public void addLibrary(Library library) {
    libraries.add(library);
  }
  
  public JarCollection getJars() {
    return jars;
  }
  
  public ClusterCollection getClusters() {
    return clusters;
  }
  
  public Collection<Library> getLibraries() {
    return libraries;
  }
}
