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
package edu.uci.ics.sourcerer.tools.java.cloning.method.fingerprint;

import java.util.Collection;
import java.util.Set;

import edu.uci.ics.sourcerer.tools.java.cloning.method.Confidence;
import edu.uci.ics.sourcerer.tools.java.cloning.method.KeyMatch;
import edu.uci.ics.sourcerer.util.Helper;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class NameFingerprintKey extends FingerprintKey {
  private String name;
  private String[] fields;
  private String[] methods;
  
  private NameFingerprintIndex index;
  
  private Collection<KeyMatch> matches;
  
  protected NameFingerprintKey(String name, String[] fields, String[] methods, NameFingerprintIndex index) {
    this.name = name;
    this.fields = fields;
    this.methods = methods;
    this.index = index;
    index.add(this);
  }

  private double computeJaccardIndex(NameFingerprintKey other) {
    if (FingerprintClusterer.REQUIRE_FINGERPRINT_NAME_MATCH.getValue()) {
      double intersectionCount = 0;
      double unionCount = 0;
      for (int i = 0, j = 0; i < fields.length && j < other.fields.length;) {
        int comp = fields[i].compareTo(other.fields[j]);
        if (comp == 0) {
          intersectionCount++;
          unionCount++;
          i++;
          j++;
        } else if (comp < 0) {
          unionCount++;
          i++;
        } else {
          unionCount++;
          j++;
        }
      }
      for (int i = 0, j = 0; i < methods.length && j < other.methods.length;) {
        int comp = methods[i].compareTo(other.methods[j]);
        if (comp == 0) {
          intersectionCount++;
          unionCount++;
          i++;
          j++;
        } else if (comp < 0) {
          unionCount++;
          i++;
        } else {
          unionCount++;
          j++;
        }
      }
      return intersectionCount / unionCount;
    } else {
      double intersectionCount = 0;
      double unionCount = 0;
      if (name.equals(other.name)) {
        intersectionCount = 1;
        unionCount = 1;
      } else {
        unionCount = 2;
      }
      for (int i = 0, j = 0; i < fields.length && j < other.fields.length;) {
        int comp = fields[i].compareTo(other.fields[j]);
        if (comp == 0) {
          intersectionCount++;
          unionCount++;
          i++;
          j++;
        } else if (comp < 0) {
          unionCount++;
          i++;
        } else {
          unionCount++;
          j++;
        }
      }
      for (int i = 0, j = 0; i < methods.length && j < other.methods.length;) {
        int comp = methods[i].compareTo(other.methods[j]);
        if (comp == 0) {
          intersectionCount++;
          unionCount++;
          i++;
          j++;
        } else if (comp < 0) {
          unionCount++;
          i++;
        } else {
          unionCount++;
          j++;
        }
      }
      return intersectionCount / unionCount;
    }
  }
  
  @Override
  public Collection<KeyMatch> getMatches() {
    if (matches == null) {
      matches = Helper.newArrayList();
      for (JaccardIndex jaccard : index.getJaccardIndices(this)) {
        if (jaccard.getIndex() >= FingerprintClusterer.MINIMUM_JACCARD_INDEX.getValue()) {
          if (computeJaccardIndex((NameFingerprintKey)jaccard.getFingerprintKey()) >= FingerprintClusterer.MINIMUM_JACCARD_INDEX.getValue()) {
            matches.add(new KeyMatch(jaccard.getFingerprintKey().getFile(), Confidence.HIGH));
          } else {
            matches.add(new KeyMatch(jaccard.getFingerprintKey().getFile(), Confidence.MEDIUM));
          }
        } else {
          matches.add(new KeyMatch(jaccard.getFingerprintKey().getFile(), Confidence.LOW));
        }
      }
    }
    return matches;
  }
    
  @Override
  public boolean isUnique(Confidence confidence) {
    getMatches();
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
  
  public String getName() {
    return name;
  }
  
  public String[] getFields() {
    return fields;
  }
  
  public String[] getMethods() {
    return methods;
  }
  
  public int getSize() {
    return 1 + fields.length + methods.length;
  }
  
  public int getSize(Set<String> excludedFields, Set<String> excludedMethods) {
    int count = 1;
    for (String field : fields) {
      if (!excludedFields.contains(field)) {
        count++;
      }
    }
    for (String method : methods) {
      if (!excludedMethods.contains(method)) {
        count++;
      }
    }
    return count;
  }
  
  public String toString() {
    StringBuilder builder = new StringBuilder(name);
    for (String field : fields) {
      builder.append(" ").append(field);
    }
    for (String method : methods) {
      builder.append(" ").append(method);
    }
    return builder.toString();
  }
}