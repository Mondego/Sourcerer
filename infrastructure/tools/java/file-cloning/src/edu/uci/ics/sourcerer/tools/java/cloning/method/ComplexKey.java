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
package edu.uci.ics.sourcerer.tools.java.cloning.method;

import java.util.Collection;

import edu.uci.ics.sourcerer.util.Helper;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public final class ComplexKey implements Key {
  private Collection<KeyMatch> matches;
  
  public ComplexKey() {
    matches = Helper.newArrayList();
  }

  @Override
  public void addFile(File file) {}

  public void addMatch(KeyMatch match) {
    matches.add(match);
  }
  
  @Override
  public Collection<KeyMatch> getMatches() {
    return matches;
  }

  @Override
  public boolean isUnique(Confidence confidence) {
    for (KeyMatch match : matches) {
      if (confidence.compareTo(match.getConfidence()) <= 0) {
        return false;
      }
    }
    return true;
  }
}
