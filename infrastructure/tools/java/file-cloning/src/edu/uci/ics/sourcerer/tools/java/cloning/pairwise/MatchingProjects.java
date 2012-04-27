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

import static edu.uci.ics.sourcerer.tools.java.cloning.method.DetectionMethod.*;

import java.util.Collection;
import java.util.Map;

import edu.uci.ics.sourcerer.tools.java.cloning.method.Confidence;
import edu.uci.ics.sourcerer.tools.java.cloning.method.DetectionMethod;
import edu.uci.ics.sourcerer.tools.java.cloning.method.File;
import edu.uci.ics.sourcerer.tools.java.cloning.method.Project;
import edu.uci.ics.sourcerer.util.Counter;
import edu.uci.ics.sourcerer.util.Helper;

public final class MatchingProjects {
  private Project project;
  private Map<File, MatchStatus> map = Helper.newHashMap();
  private Map<Confidence, Counter<?>[]> counterMap;
  private static final int NUM_COUNTERS = (int)Math.pow(2, DetectionMethod.values().length); 
  
  public MatchingProjects(Project project) {
    this.project = project;
  }
  
  public MatchStatus getMatchStatus(File file) {
    MatchStatus status = map.get(file);
    if (status == null) {
      status = new MatchStatus(file);
      map.put(file, status);
    }
    return status;
  }
  
  public Project getProject() {
    return project;
  }
  
  public Collection<MatchStatus> getMatchStatusSet() {
    return map.values();
  }
  
  private void initCounterMap() {
    counterMap = Helper.newEnumMap(Confidence.class);
    for (Confidence conf : Confidence.values()) {
      Counter<?>[] counters = new Counter<?>[NUM_COUNTERS];
      for (int i = 0; i < NUM_COUNTERS; i++) {
        counters[i] = new Counter<Object>();
      }
      counterMap.put(conf, counters);
    }
    
    for (MatchStatus status : map.values()) {
      int lowIndex = 0;
      int mediumIndex = 0;
      int highIndex = 0;
      if (status.getHash() != null) {
        lowIndex += HASH.getVal();
        mediumIndex += HASH.getVal();
        highIndex += HASH.getVal();
      }
      if (status.getFqn() == Confidence.HIGH) {
        lowIndex += FQN.getVal();
        mediumIndex += FQN.getVal();
        highIndex += FQN.getVal();
      } else if (status.getFqn() == Confidence.MEDIUM) {
        lowIndex += FQN.getVal();
        mediumIndex += FQN.getVal();
      } else if (status.getFqn() == Confidence.LOW) {
        lowIndex += FQN.getVal();
      }
      if (status.getFingerprint() == Confidence.HIGH) {
        lowIndex += FINGERPRINT.getVal();
        mediumIndex += FINGERPRINT.getVal();
        highIndex += FINGERPRINT.getVal();
      } else if (status.getFingerprint() == Confidence.MEDIUM) {
        lowIndex += FINGERPRINT.getVal();
        mediumIndex += FINGERPRINT.getVal();
      } else if (status.getFingerprint() == Confidence.LOW) {
        lowIndex += FINGERPRINT.getVal();
      }
      if (status.getCombined() == Confidence.HIGH) {
        lowIndex += COMBINED.getVal();
        mediumIndex += COMBINED.getVal();
        highIndex += COMBINED.getVal();
      } else if (status.getCombined() == Confidence.MEDIUM) {
        lowIndex += COMBINED.getVal();
        mediumIndex += COMBINED.getVal();
      } else if (status.getCombined() == Confidence.LOW) {
        lowIndex += COMBINED.getVal();
      } 
      counterMap.get(Confidence.LOW)[lowIndex].increment();
      counterMap.get(Confidence.MEDIUM)[mediumIndex].increment();
      counterMap.get(Confidence.HIGH)[highIndex].increment();
    }
  }
  
  public int getCount(Confidence confidence, DetectionMethod ... methods) {
    if (counterMap == null) {
      initCounterMap();
    }
    int idx = 0;
    for (DetectionMethod method : methods) {
      idx += method.getVal();
    }
    return counterMap.get(confidence)[idx].getCount();
  }
  
  public int getCount(DetectionMethod method, Confidence confidence) {
    if (counterMap == null) {
      initCounterMap();
    }
    int count = 0;
    Counter<?>[] counters = counterMap.get(confidence);
    for (int i = 0; i < counters.length; i++) {
      if ((i & method.getVal()) == 1) {
        count += counters[i].getCount();
      }
    }
    return count;
  }
}