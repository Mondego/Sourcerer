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
package edu.uci.ics.sourcerer.clusterer.cloning;

import java.util.Collection;
import java.util.Map;

import edu.uci.ics.sourcerer.util.Helper;

public final class FileMatch {
  private Map<File, MatchStatus> map = Helper.newHashMap();
  private int sharedCount = -1;
  private int uniqueHashCount = -1;
  private int uniqueFqnCount = -1;
  
  protected FileMatch() {}
  
  protected MatchStatus getMatchStatus(File file) {
    MatchStatus status = map.get(file);
    if (status == null) {
      status = new MatchStatus();
      map.put(file, status);
    }
    return status;
  }
    
  private void computeCounts() {
    sharedCount = 0;
    uniqueHashCount = 0;
    uniqueFqnCount = 0;
    for (MatchStatus status : map.values()) {
      if (status.hash && status.fqn) {
        sharedCount++;
      } else if (status.hash) {
        uniqueHashCount++;
      } else if (status.fqn) {
        uniqueFqnCount++;
      }
    }
  }
  
  public int getSharedCount() {
    if (sharedCount == -1) {
      computeCounts();
    }
    return sharedCount;
  }
  public int getUniqueHashCount() {
    if (uniqueHashCount == -1) {
      computeCounts();
    }
    return uniqueHashCount;
  }
  
  public int getUniqueFqnCount() {
    if (uniqueFqnCount == -1) {
      computeCounts();
    }
    return uniqueFqnCount;
  }
  
  public Collection<Map.Entry<File, MatchStatus>> getMatchStatusSet() {
    return map.entrySet();
  }
}