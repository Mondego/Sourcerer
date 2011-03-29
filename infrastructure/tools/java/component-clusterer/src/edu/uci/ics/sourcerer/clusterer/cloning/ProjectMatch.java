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

import edu.uci.ics.sourcerer.clusterer.cloning.method.fingerprint.FingerprintClusterer;
import edu.uci.ics.sourcerer.util.Helper;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public final class ProjectMatch {
  private Map<Project, FileMatch> map;
  
  protected ProjectMatch(Project project) {
    map = Helper.newHashMap();
//    double minimumJaccard = FingerprintClusterer.MINIMUM_JACCARD_INDEX.getValue();
//    for (File file : project.getFiles()) {
//      if (file.hasAllKeys()) {
//        for (File otherFile : file.getHashKey().getFiles()) {
//          if (file != otherFile && otherFile.hasAllKeys()) {
//            getMatchStatus(otherFile).hash = true;
//          }
//        }
//        if (file.getFqnKey().getConfidence() == Confidence.HIGH) {
//          for (File otherFile : file.getFqnKey().getFiles()) {
//            if (file != otherFile && otherFile.hasAllKeys()) {
//              getMatchStatus(otherFile).fqn = true;
//            }
//          }
//        }
//        for (JaccardIndex index : file.getFingerprint().getJaccardIndices()) {
//          if (index.getIndex() > minimumJaccard) {
//            getMatchStatus(index.getFingerprint().getFile()).fingerprint = true;
//          }
//        }
//      }
//    }
  }
  
  private MatchStatus getMatchStatus(File file) {
    FileMatch match = map.get(file.getProject());
    if (match == null) {
      match = new FileMatch();
      map.put(file.getProject(), match);
    }
    return match.getMatchStatus(file);
  }
  
  public FileMatch getFileMatch(Project project) {
    return map.get(project);
  }
  
  public Collection<Map.Entry<Project, FileMatch>> getFileMatches() {
    return map.entrySet();
  }
}
