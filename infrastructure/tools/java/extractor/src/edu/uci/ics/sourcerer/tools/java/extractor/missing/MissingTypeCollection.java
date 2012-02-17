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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class MissingTypeCollection {
  private Map<String, MissingType> missingTypes;
  private Map<String, MissingPackage> missingPackages;
  
  private Set<String> currentlyMissing;
  private Set<String> currentlyMissingStatic;
  private Set<String> currentlyMissingOnDemand;
  private Set<String> currentlyMissingStaticOnDemand;
  
  private MissingTypeCollection() {
    missingTypes = new HashMap<>();
    missingPackages = new HashMap<>();
    currentlyMissing = new HashSet<>();
    currentlyMissingStatic = new HashSet<>();
    currentlyMissingOnDemand = new HashSet<>();
    currentlyMissingStaticOnDemand = new HashSet<>();
  }
  
  static MissingTypeCollection create() {
    return new MissingTypeCollection();
  }
  
  private MissingType getType(String fqn) {
    MissingType type = missingTypes.get(fqn);
    if (type == null) {
      // Get the package
      String pkg = fqn.substring(0, fqn.lastIndexOf('.'));
      type = MissingType.create(fqn, getPackage(pkg));
      missingTypes.put(fqn, type);
    }
    return type;
  }
  
  private MissingPackage getPackage(String fqn) {
    MissingPackage pkg = missingPackages.get(fqn);
    if (pkg == null) {
      pkg = MissingPackage.create(fqn);
      missingPackages.put(fqn, pkg);
    }
    return pkg;
  }
  
  void endFile() {
    // For each basic import, just add it to the missing types
    for (String fqn : currentlyMissing) {
      getType(fqn).reportMissing();
    }
    
    // For each on demand import
    for (String fqn : currentlyMissingOnDemand) {
      getPackage(fqn).reportMissing();
    }
    
    // For each static import
    for (String fqn : currentlyMissingStatic) {
      String klass = fqn.substring(0, fqn.lastIndexOf('.'));
      getType(klass).reportMissingField(fqn.substring(fqn.lastIndexOf('.') + 1));
    }
    
    // For each on demand static import
    for (String fqn : currentlyMissingStaticOnDemand) {
      getType(fqn).reportMissingStaticOnDemand();
    }
    
    currentlyMissing.clear();
    currentlyMissingStatic.clear();
    currentlyMissingOnDemand.clear();
    currentlyMissingStaticOnDemand.clear();
  }
  
  void addMissingImport(String fqn, boolean onDemand, boolean isStatic) {
    if (isStatic) {
      if (onDemand) {
        currentlyMissingStaticOnDemand.add(fqn);
      } else {
        currentlyMissingStatic.add(fqn);
      }
    } else {
      if (onDemand) {
        currentlyMissingOnDemand.add(fqn);
      } else {
        currentlyMissing.add(fqn);
      }
    }
  }
  
  public boolean hasMissingTypes() {
    return !(missingTypes.isEmpty() && missingPackages.isEmpty());
  }
  
  public int getMissingTypeCount() {
    return missingTypes.size();
  }
}
