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
package edu.uci.ics.sourcerer.tools.java.utilization.model.cluster;

import java.util.HashMap;
import java.util.Map;

import edu.uci.ics.sourcerer.tools.java.utilization.model.jar.VersionedFqnNode;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class ClusterMatcher {
  private Map<String, Cluster> map;
  
  private ClusterMatcher() {
    map = new HashMap<>();
  }
  
  static ClusterMatcher create(ClusterCollection clusters) {
    ClusterMatcher matcher = new ClusterMatcher();
    for (Cluster cluster : clusters) {
      for (VersionedFqnNode fqn : cluster.getCoreFqns()) {
        matcher.map.put(fqn.getFqn(), cluster);
      }
      for (VersionedFqnNode fqn : cluster.getExtraFqns()) {
        matcher.map.put(fqn.getFqn(), cluster);
      }
    }
    return matcher;
  }
  
  /**
   * Assumes the VersionedFqnNode comes from a different
   */
  public Cluster getMatch(String fqn) {
    return map.get(fqn);
  }
}
