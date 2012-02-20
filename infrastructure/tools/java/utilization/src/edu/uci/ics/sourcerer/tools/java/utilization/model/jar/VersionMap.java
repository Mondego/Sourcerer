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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import edu.uci.ics.sourcerer.util.MutableSingletonMap;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class VersionMap {
  private JarSet jars;
  private Map<Fingerprint, JarSet> versions;
  
  private VersionMap() {
    jars = JarSet.create();
    versions = Collections.emptyMap();
  }
  
  public static VersionMap make() {
    return new VersionMap();
  }
  
  public void add(Fingerprint fingerprint, Jar jar) {
    if (versions.isEmpty()) {
      jars = jars.add(jar);
      versions = MutableSingletonMap.create(fingerprint, jars);
    } else if (versions.size() == 1) {
      JarSet set = versions.get(fingerprint);
      if (set == null) {
        versions = new HashMap<>(versions);
        versions.put(fingerprint, JarSet.create(jar));
        jars = jars.add(jar);
      } else {
        jars = jars.add(jar);
        versions.put(fingerprint, jars);
      }
    } else {
      JarSet set = versions.get(fingerprint);
      if (set == null) {
        set = JarSet.create();
      }
      versions.put(fingerprint, set.add(jar));
      jars = jars.add(jar);
    }
  }
  
  public JarSet getJars() {
    return jars;
  }
  
  public Set<Map.Entry<Fingerprint, JarSet>> getVersions() {
    return versions.entrySet();
  }
}
