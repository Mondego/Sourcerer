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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class MissingType {
  private final String fqn;
  private final MissingPackage pkg;
  private int missingCount;
  private Set<String> missingFields;
  private int missingStaticOnDemandCount;
  
  private MissingType(String fqn, MissingPackage pkg) {
    this.fqn = fqn;
    this.pkg = pkg;
    missingCount = 0;
    missingStaticOnDemandCount = 0;
    missingFields = Collections.emptySet();
    pkg.addMemberType(this);
  }
  
  static MissingType create(String fqn, MissingPackage pkg) {
    return new MissingType(fqn, pkg);
  }
  
  void reportMissing() {
    missingCount++;
  }

  void reportMissingField(String name) {
    if (missingFields.isEmpty()) {
      missingFields = new HashSet<>();
    }
    missingFields.add(name);
  }
  
  void reportMissingStaticOnDemand() {
    missingStaticOnDemandCount++;
  }
}
