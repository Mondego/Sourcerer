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
package edu.uci.ics.sourcerer.tools.java.utilization.identifier;

import java.util.HashSet;
import java.util.Set;

import edu.uci.ics.sourcerer.tools.java.utilization.model.FqnFragment;
import edu.uci.ics.sourcerer.tools.java.utilization.model.Jar;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class Library {
  private final Set<Jar> jars;
  private final Set<FqnFragment> fqns;
  
  Library() {
    this.jars = new HashSet<>();
    this.fqns = new HashSet<>();
  }
  
  void addJar(Jar jar) {
    jars.add(jar);
  }
  
  void addFqn(FqnFragment fqn) {
    fqns.add(fqn);
  }
  
  public Set<Jar> getJars() {
    return jars;
  }
  
  public Set<FqnFragment> getFqns(){ 
    return fqns;
  }
}
