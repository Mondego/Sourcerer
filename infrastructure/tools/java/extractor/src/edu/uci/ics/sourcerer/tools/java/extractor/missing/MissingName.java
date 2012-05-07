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
package edu.uci.ics.sourcerer.tools.java.extractor.missing;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class MissingName {
  private final String mainPackage;
  private final String name;
  private Collection<MissingPackage> potentialOwners;
  
  private MissingName(String mainPackage, String name) {
    this.mainPackage = mainPackage;
    this.name = name;
    potentialOwners = Collections.emptySet();
  }
  
  static MissingName create(String mainPackage, String name) {
    return new MissingName(mainPackage, name);
  }

  void addPotentialOwner(MissingPackage pkg) {
    if (potentialOwners.isEmpty()) {
      potentialOwners = new HashSet<>();
    }
    potentialOwners.add(pkg);
  }
  
  public String getName() {
    return name;
  }
}
