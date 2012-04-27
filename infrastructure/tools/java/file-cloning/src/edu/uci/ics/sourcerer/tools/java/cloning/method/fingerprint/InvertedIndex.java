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
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import edu.uci.ics.sourcerer.tools.java.cloning.method.Key;
import edu.uci.ics.sourcerer.util.Counter;
import edu.uci.ics.sourcerer.util.Helper;
import edu.uci.ics.sourcerer.util.io.TablePrettyPrinter;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class InvertedIndex <T extends Key> {
  private Map<String, Collection<T>> map;
  
  protected InvertedIndex() {
    map = Helper.newHashMap();
  }
  
  private Collection<T> get(String key) {
    Collection<T> fingerprints = map.get(key);
    if (fingerprints == null) {
      return Collections.emptyList();
    } else {
      return fingerprints;
    }
  }
  
  public void addFingerprint(T fingerprint, String[] strings) {
    for (String string : strings) {
      Collection<T> fingerprints = map.get(string);
      if (fingerprints == null) {
        fingerprints = Helper.newArrayList();
        map.put(string, fingerprints);
      }
      if (!fingerprints.contains(fingerprint)) {
        fingerprints.add(fingerprint);
      }
    }
  }
  
  public void collectFingerprints(Map<T, Counter<T>> result, String[] strings) {
    for (String string : strings) {
      for (T fingerprint : get(string)) {
        Counter<T> counter = result.get(fingerprint);
        if (counter == null) {
          counter = new Counter<T>(fingerprint);
          result.put(fingerprint, counter);
        }
        counter.increment();
      }
    }
  }
  
  public Set<String> clearPopularNames(TablePrettyPrinter printer, int maxSize) {
    Set<String> removed = Helper.newHashSet();
    Set<Counter<String>> sorted = Helper.newTreeSet(Counter.<String>getReverseComparator());
    Iterator<Map.Entry<String, Collection<T>>> iter = map.entrySet().iterator();
    for (Map.Entry<String, Collection<T>> entry = iter.next(); iter.hasNext(); entry = iter.next()) {
      if (entry.getValue().size() > maxSize) {
        removed.add(entry.getKey());
        sorted.add(new Counter<String>(entry.getKey(), entry.getValue().size()));
        iter.remove();
      }
    }
    
    printer.beginTable(2);
    printer.addDividerRow();
    printer.addRow("Name", "Count");
    printer.addDividerRow();
    for (Counter<String> counter : sorted) {
      printer.beginRow();
      printer.addCell(counter.getObject());
      printer.addCell(counter.getCount());
    }
    printer.addDividerRow();
    printer.endTable();
    
    return removed;
  }
}
