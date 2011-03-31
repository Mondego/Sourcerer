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
package edu.uci.ics.sourcerer.clusterer.cloning.pairwise;

import java.util.Collection;
import java.util.Map;

import edu.uci.ics.sourcerer.clusterer.cloning.basic.Confidence;
import edu.uci.ics.sourcerer.clusterer.cloning.basic.File;
import edu.uci.ics.sourcerer.util.Counter;
import edu.uci.ics.sourcerer.util.Helper;

public final class FileMatching {
  public static enum Type {
    HASH(1),
    FQN(2),
    FINGERPRINT(4);
    
    private int val;
    
    Type(int val) {
      this.val = val;
    }
  }
  
  private Map<File, MatchStatus> map = Helper.newHashMap();
  private Map<Confidence, Counter<?>[]> counterMap;
  
  public FileMatching() {}
  
  public MatchStatus getMatchStatus(File file) {
    MatchStatus status = map.get(file);
    if (status == null) {
      status = new MatchStatus();
      map.put(file, status);
    }
    return status;
  }
  
  public Collection<Map.Entry<File, MatchStatus>> getMatchStatusSet() {
    return map.entrySet();
  }
  
  private void initCounterMap() {
    counterMap = Helper.newEnumMap(Confidence.class);
    for (Confidence conf : Confidence.values()) {
      Counter<?>[] counters = new Counter<?>[8];
      for (int i = 0; i < 8; i++) {
        counters[i] = new Counter<Object>();
      }
      counterMap.put(conf, counters);
    }
    
    for (MatchStatus status : map.values()) {
      int lowIndex = 0;
      int mediumIndex = 0;
      int highIndex = 0;
      if (status.hash != null) {
        lowIndex += Type.HASH.val;
        mediumIndex += Type.HASH.val;
        highIndex += Type.HASH.val;
      }
      if (status.fqn == Confidence.HIGH) {
        lowIndex += Type.FQN.val;
        mediumIndex += Type.FQN.val;
        highIndex += Type.FQN.val;
      } else if (status.fqn == Confidence.MEDIUM) {
        lowIndex += Type.FQN.val;
        mediumIndex += Type.FQN.val;
      } else if (status.fqn == Confidence.LOW) {
        lowIndex += Type.FQN.val;
      }
      if (status.fingerprint == Confidence.HIGH) {
        lowIndex += Type.FINGERPRINT.val;
        mediumIndex += Type.FINGERPRINT.val;
        highIndex += Type.FINGERPRINT.val;
      } else if (status.fingerprint == Confidence.MEDIUM) {
        lowIndex += Type.FINGERPRINT.val;
        mediumIndex += Type.FINGERPRINT.val;
      } else if (status.fingerprint == Confidence.LOW) {
        lowIndex += Type.FINGERPRINT.val;
      }
      counterMap.get(Confidence.LOW)[lowIndex].increment();
      counterMap.get(Confidence.MEDIUM)[mediumIndex].increment();
      counterMap.get(Confidence.HIGH)[highIndex].increment();
    }
  }
  
  public int getCount(Confidence confidence, Type ... types) {
    if (counterMap == null) {
      initCounterMap();
    }
    int idx = 0;
    for (Type type : types) {
      idx += type.val;
    }
    return counterMap.get(confidence)[idx].getCount();
  }
  
  public int getCount(Type type, Confidence confidence) {
    if (counterMap == null) {
      initCounterMap();
    }
    int count = 0;
    Counter<?>[] counters = counterMap.get(confidence);
    for (int i = 0; i < counters.length; i++) {
      if ((i & type.val) == 1) {
        count += counters[i].getCount();
      }
    }
    return count;
  }
}