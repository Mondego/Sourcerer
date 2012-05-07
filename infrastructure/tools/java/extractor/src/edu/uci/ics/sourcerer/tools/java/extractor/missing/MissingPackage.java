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
import java.util.LinkedList;
import java.util.Set;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class MissingPackage {
  private final String fqn;
  private Collection<MissingType> memberTypes;
  private Multimap<String, MissingName> potentialNames;
  private int onDemandCount;
  
  private MissingPackage(String fqn) {
    this.fqn = fqn;
    memberTypes = Collections.emptySet();
    potentialNames = null;
    onDemandCount = 0;
  }
  
  static MissingPackage create(String fqn) {
    return new MissingPackage(fqn);
  }
  
  void addMemberType(MissingType type) {
    if (memberTypes.isEmpty()) {
      memberTypes = new LinkedList<>();
    }
    memberTypes.add(type);
  }
  
  void addPotentialName(MissingName name) {
    if (potentialNames == null) {
      potentialNames = HashMultimap.create();
    }
    potentialNames.put(name.getName(), name);
    name.addPotentialOwner(this);
  }
  
  void reportMissing() {
    onDemandCount++;
  }
}
