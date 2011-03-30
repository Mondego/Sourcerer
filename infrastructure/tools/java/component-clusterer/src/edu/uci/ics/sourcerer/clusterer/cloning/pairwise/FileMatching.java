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
package edu.uci.ics.sourcerer.clusterer.cloning.pairwise;

import java.util.Collection;
import java.util.Map;

import edu.uci.ics.sourcerer.clusterer.cloning.basic.File;
import edu.uci.ics.sourcerer.util.Helper;

public final class FileMatching {
  private Map<File, MatchStatus> map = Helper.newHashMap();
  
  protected FileMatching() {}
  
  protected MatchStatus getMatchStatus(File file) {
    MatchStatus status = map.get(file);
    if (status == null) {
      status = new MatchStatus();
      map.put(file, status);
    }
    return status;
  }
  
  public Collection<Map.Entry<File, MatchStatus>> getMatchStatusSet() {
    return map.entrySet();
  }
}