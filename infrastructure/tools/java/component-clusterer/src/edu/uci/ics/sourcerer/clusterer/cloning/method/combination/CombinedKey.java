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
package edu.uci.ics.sourcerer.clusterer.cloning.method.combination;

import java.util.Collection;

import edu.uci.ics.sourcerer.clusterer.cloning.basic.Confidence;
import edu.uci.ics.sourcerer.clusterer.cloning.basic.File;
import edu.uci.ics.sourcerer.clusterer.cloning.basic.Key;
import edu.uci.ics.sourcerer.clusterer.cloning.basic.KeyMatch;
import edu.uci.ics.sourcerer.util.Helper;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public final class CombinedKey implements Key {
  private Collection<KeyMatch> matches;
  
  protected CombinedKey() {
    matches = Helper.newArrayList();
  }

  @Override
  public void addFile(File file) {}

  protected void addMatch(KeyMatch match) {
    matches.add(match);
  }
  
  @Override
  public Collection<KeyMatch> getMatches() {
    return matches;
  }

  @Override
  public boolean isUnique(Confidence confidence) {
    if (matches.size() == 0) {
      return true;
    } else if (confidence == Confidence.LOW) {
      return false;
    } else if (confidence == Confidence.MEDIUM) {
      for (KeyMatch match : matches) {
        if (match.getConfidence() != Confidence.LOW) {
          return false;
        }
      }
      return true;
    } else {
      for (KeyMatch match : matches) {
        if (match.getConfidence() == Confidence.HIGH) {
          return false;
        }
      }
      return true;
    }
  }
}
