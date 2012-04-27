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
package edu.uci.ics.sourcerer.tools.java.cloning.method.dir;

import java.util.Collection;
import java.util.Collections;

import edu.uci.ics.sourcerer.util.Helper;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class JavaFile {
  private String name;

  private Collection<String> matches80;
  private Collection<String> matches50;
  private Collection<String> matches30;
  
  protected JavaFile(String name) {
    this.name = name;
  }
  
  public String getName() {
    return name;
  }
  
  public Collection<String> getMatches80() {
    if (matches80 == null) {
      return Collections.emptyList();
    } else {
      return matches80;
    }
  }
  
  public Collection<String> getMatches50() {
    if (matches50 == null) {
      return Collections.emptyList();
    } else {
      return matches50;
    }
  }
  
  public Collection<String> getMatches30() {
    if (matches30 == null) {
      return Collections.emptyList();
    } else {
      return matches30;
    }
  }
  
  public void addMatch(double percent, String other) {
    if (percent >= .8) {
      if (matches80 == null) {
        matches80 = Helper.newLinkedList();
      }
      matches80.add(other);
    } else if (percent >= .5) {
      if (matches50 == null) {
        matches50 = Helper.newLinkedList();
      }
      matches50.add(other);
    } else { 
      if (matches30 == null) {
        matches30 = Helper.newLinkedList();
      }
      matches30.add(other);
    }
  }
}
