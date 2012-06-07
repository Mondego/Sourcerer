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
package edu.uci.ics.sourcerer.tools.java.component.model.jar;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class FqnVersion {
  private final VersionedFqnNode fqn;
  private final Fingerprint fingerprint;
  private JarSet jars;
  
  private FqnVersion(VersionedFqnNode fqn, Fingerprint fingerprint) {
    this.fqn = fqn;
    this.fingerprint = fingerprint;
    jars = JarSet.create();
  }
  
  static FqnVersion create(VersionedFqnNode fqn, Fingerprint fingerprint) {
    return new FqnVersion(fqn, fingerprint);
  }
  
  void addJar(Jar jar) {
    jars = jars.add(jar);
    fqn.addJar(jar);
  }
  
  public VersionedFqnNode getFqn() {
    return fqn;
  }
  
  public Fingerprint getFingerprint() {
    return fingerprint;
  }
  
  public JarSet getJars() {
    return jars;
  }
}
