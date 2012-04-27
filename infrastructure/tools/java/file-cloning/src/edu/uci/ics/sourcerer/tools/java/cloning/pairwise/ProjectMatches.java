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
package edu.uci.ics.sourcerer.tools.java.cloning.pairwise;

import java.util.Collection;
import java.util.Map;

import edu.uci.ics.sourcerer.tools.java.cloning.method.DetectionMethod;
import edu.uci.ics.sourcerer.tools.java.cloning.method.File;
import edu.uci.ics.sourcerer.tools.java.cloning.method.KeyMatch;
import edu.uci.ics.sourcerer.tools.java.cloning.method.Project;
import edu.uci.ics.sourcerer.util.Helper;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public final class ProjectMatches {
  private Project project;
  private Map<Project, MatchingProjects> map;
  
  protected ProjectMatches(Project project) {
    this.project = project;
    map = Helper.newHashMap();
    for (File file : project.getFiles()) {
      if (file.hasAllKeys()) {
        for (KeyMatch match : file.getHashKey().getMatches()) {
          if (file != match.getFile() && match.getFile().hasAllKeys()) {
            getMatchStatus(match.getFile()).setHash(match.getConfidence());
          }
        }
        for (KeyMatch match : file.getFqnKey().getMatches()) {
          if (file != match.getFile() && match.getFile().hasAllKeys()) {
            getMatchStatus(match.getFile()).setFqn(match.getConfidence());
          }
        }
        for (KeyMatch match : file.getFingerprintKey().getMatches()) {
          if (file != match.getFile() && match.getFile().hasAllKeys()) {
            getMatchStatus(match.getFile()).setFingerprint(match.getConfidence());
          }
        }
        if (file.hasCombinedKey()) {
          for (KeyMatch match : file.getCombinedKey().getMatches()) {
            if (file != match.getFile() && match.getFile().hasAllKeys()) {
              getMatchStatus(match.getFile()).setCombined(match.getConfidence());
            }
          }
        }
        if (file.hasDirKey()) {
          for (KeyMatch match : file.getDirKey().getMatches()) {
            if (file != match.getFile() && match.getFile().hasAllKeys()) {
              getMatchStatus(match.getFile()).set(DetectionMethod.DIR, match.getConfidence());
            }
          }
        }
      }
    }
  }
  
  private MatchStatus getMatchStatus(File file) {
    MatchingProjects match = map.get(file.getProject());
    if (match == null) {
      match = new MatchingProjects(file.getProject());
      map.put(file.getProject(), match);
    }
    return match.getMatchStatus(file);
  }
  
  public MatchingProjects getFileMatch(Project project) {
    return map.get(project);
  }
  
  public Collection<MatchingProjects> getMatchingProjects() {
    return map.values();
  }
  
  public Project getProject() {
    return project;
  }
}
