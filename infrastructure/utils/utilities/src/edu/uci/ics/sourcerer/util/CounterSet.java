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
package edu.uci.ics.sourcerer.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class CounterSet <T> {
  private Map<T, Counter<T>> counters;
  
  public CounterSet() {
    counters = new HashMap<>();
  }
  
  public void increment(T key) {
    Counter<T> counter = counters.get(key);
    if (counter == null) {
      counter = new Counter<T>(key);
      counters.put(key, counter);
    }
    counter.increment();
  }
  
  public void clear() {
    counters.clear();
  }
  
  public Counter<T> getCounter(T key) {
    return counters.get(key);
  }
  
  public Collection<Counter<T>> getCounters() {
    return counters.values();
  }
  
  public Counter<T> getMax() {
    Counter<T> max = null;
    for (Counter<T> counter : counters.values()) {
      if (max == null || counter.getCount() > max.getCount()) {
        max = counter;
      }
    }
    return max;
  }
}
