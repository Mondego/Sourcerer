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

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class MissingTypeCollection {
  private Set<String> imports;
  private Map<String, MissingType> missingTypes;
//  private Multimap<String, MissingName> missingNames;
  private Map<String, MissingPackage> missingPackages;
  
  private String currentPackage;
//  private Set<String> currentlyMissingFQNs;
//  private Set<String> currentlyMissingNames;
  private Set<String> currentlyMissingImports;
  private Set<String> currentlyMissingStaticImports;
  private Set<String> currentlyMissingOnDemandImports;
  private Set<String> currentlyMissingStaticOnDemandImports;
  
  private MissingTypeCollection() {
    imports = new HashSet<>();
    missingTypes = new HashMap<>();
//    missingNames = HashMultimap.create();
    missingPackages = new HashMap<>();
    
//    currentlyMissingFQNs = new HashSet<>();
//    currentlyMissingNames = new HashSet<>();
    currentlyMissingImports = new HashSet<>();
    currentlyMissingStaticImports = new HashSet<>();
    currentlyMissingOnDemandImports = new HashSet<>();
    currentlyMissingStaticOnDemandImports = new HashSet<>();
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
    // Missing FQNs are missing FQNs
//    for (String fqn : currentlyMissingFQNs) {
//      getType(fqn).reportMissing();
//    }
//    currentlyMissingFQNs.clear();
//    
    // For each basic import, add it to the missing types
    for (String fqn : currentlyMissingImports) {
      getType(fqn).reportMissing();
      // Remove the missing name if it is accounted for by this import
//      currentlyMissingNames.remove(fqn.substring(fqn.lastIndexOf('.') + 1));
      
    }
    currentlyMissingImports.clear();

    // For each static import
    for (String fqn : currentlyMissingStaticImports) {
      String klass = fqn.substring(0, fqn.lastIndexOf('.'));
      String name = fqn.substring(fqn.lastIndexOf('.') + 1);
      MissingType classType = getType(klass);
//      // Does the name match a missing name?
//      if (currentlyMissingFields.remove(name)) {
//        // It must be a field
//        classType.reportField(name);
//      }
//      // Does the name match a missing method?
//      else if (currentlyMissingMethods.remove(name)) {
//        // It must be a method
//        classType.reportMethod(name);
//      } else {
//        // Unclear what it is, likely an unused import
        classType.reportAssociatedName(name);
//      }
    }
    currentlyMissingStaticImports.clear();
    
    // Make MissingNames for every missing name
//    Set<MissingName> names = new HashSet<>();
//    for (String name : currentlyMissingNames) {
//      MissingName missingName = MissingName.create(currentPackage, name);
//      names.add(missingName);
//      missingNames.put(name, missingName);
//    }
  
    // For each on demand import
    for (String fqn : currentlyMissingOnDemandImports) {
      MissingPackage pkg = getPackage(fqn);
      pkg.reportMissing();
      // Add each missing name as a possible type
//      for (MissingName name : names) {
//        pkg.addPotentialName(name);
//      }
    }
    currentlyMissingOnDemandImports.clear();
    
    // For each on demand static import
    for (String fqn : currentlyMissingStaticOnDemandImports) {
      getType(fqn).reportMissingStaticOnDemand();
    }
    currentlyMissingStaticOnDemandImports.clear();
  }
  
  void addImport(String fqn, boolean onDemand, boolean isStatic) {
    if (isStatic) {
      if (onDemand) {
        imports.add(fqn);
      } else {
        String klass = fqn.substring(0, fqn.lastIndexOf('.'));
        imports.add(klass);
      }
    } else {
      if (onDemand) {
        
      } else {
        imports.add(fqn);
      }
    }
  }
  
  void setPackage(String pkg) {
    currentPackage = pkg;
  }
  
  void addMissingImport(String fqn, boolean onDemand, boolean isStatic) {
    if (isStatic) {
      if (onDemand) {
        currentlyMissingStaticOnDemandImports.add(fqn);
      } else {
        currentlyMissingStaticImports.add(fqn);
      }
    } else {
      if (onDemand) {
        currentlyMissingOnDemandImports.add(fqn);
      } else {
        currentlyMissingImports.add(fqn);
      }
    }
  }
  
//  void addMissingFQN(String fqn) {
//    currentlyMissingFQNs.add(fqn);
//  }
  
//  void addMissingName(String name) {
//    currentlyMissingNames.add(name);
//  }
  
  public boolean hasMissingTypes() {
    return !(missingTypes.isEmpty() && missingPackages.isEmpty());
  }
  
  public int getMissingTypeCount() {
    return missingTypes.size();
  }
  
  public Iterable<String> getImports() {
    return imports;
  }
  
  public int getImportCount() {
    return imports.size();
  }
  
  public Iterable<MissingType> getMissingTypes() {
    return missingTypes.values();
  }
}