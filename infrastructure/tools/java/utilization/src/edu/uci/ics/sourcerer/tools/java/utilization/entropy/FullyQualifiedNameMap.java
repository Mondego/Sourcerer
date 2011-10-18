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
package edu.uci.ics.sourcerer.tools.java.utilization.entropy;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class FullyQualifiedNameMap {
  private Map<String, FullyQualifiedName> interned;
  
  FullyQualifiedNameMap() {
    interned = new HashMap<>();
  }
  
  FullyQualifiedName makeFQN(String fqn, EntropicJar jar) {
    FullyQualifiedName val = interned.get(fqn);
    if (val == null) {
      val = new FullyQualifiedName(fqn);
      interned.put(fqn, val);
    }
    val.addJar(jar);
    return val;
  }
  
  public Collection<FullyQualifiedName> getFullyQualifiedNames() {
    return interned.values();
  }
}
