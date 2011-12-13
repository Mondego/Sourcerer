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
package edu.uci.ics.sourcerer.tools.java.utilization.model.jar;

import edu.uci.ics.sourcerer.tools.java.utilization.model.fqn.AbstractFqnNode;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class VersionedFqnNode extends AbstractFqnNode<VersionedFqnNode> {
  private VersionMap versions;
  
  private VersionedFqnNode(String name, VersionedFqnNode parent) {
    super(name, parent);
    this.versions = VersionMap.make();
  }
  
  @Override
  protected VersionedFqnNode create(String name, AbstractFqnNode<?> parent) {
    return new VersionedFqnNode(name, (VersionedFqnNode) parent);
  }
  
  public static VersionedFqnNode createRoot() {
    return new VersionedFqnNode(null, null);
  }
  
  void addJar(Jar jar, Fingerprint fingerprint) {
    versions.add(fingerprint, jar);
  }
  
  public VersionMap getVersions() {
    return versions;
  }
}
