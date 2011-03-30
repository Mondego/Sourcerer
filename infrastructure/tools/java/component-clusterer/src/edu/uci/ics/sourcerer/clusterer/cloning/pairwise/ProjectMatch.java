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
import edu.uci.ics.sourcerer.clusterer.cloning.basic.KeyMatch;
import edu.uci.ics.sourcerer.clusterer.cloning.basic.Project;
import edu.uci.ics.sourcerer.util.Helper;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public final class ProjectMatch {
  private Map<Project, FileMatching> map;
  
  protected ProjectMatch(Project project) {
    map = Helper.newHashMap();
    for (File file : project.getFiles()) {
      if (file.hasAllKeys()) {
        for (KeyMatch match : file.getHashKey().getMatches()) {
          if (file != match.getFile() && match.getFile().hasAllKeys()) {
            getMatchStatus(match.getFile()).hash = match.getConfidence();
          }
        }
        for (KeyMatch match : file.getFqnKey().getMatches()) {
          if (file != match.getFile() && match.getFile().hasAllKeys()) {
            getMatchStatus(match.getFile()).fqn = match.getConfidence();
          }
        }
        for (KeyMatch match : file.getFingerprintKey().getMatches()) {
          if (file != match.getFile() && match.getFile().hasAllKeys()) {
            getMatchStatus(match.getFile()).fingerprint = match.getConfidence();
          }
        }
      }
    }
  }
  
  private MatchStatus getMatchStatus(File file) {
    FileMatching match = map.get(file.getProject());
    if (match == null) {
      match = new FileMatching();
      map.put(file.getProject(), match);
    }
    return match.getMatchStatus(file);
  }
  
  public FileMatching getFileMatch(Project project) {
    return map.get(project);
  }
  
  public Collection<Map.Entry<Project, FileMatching>> getFileMatchings() {
    return map.entrySet();
  }
}
