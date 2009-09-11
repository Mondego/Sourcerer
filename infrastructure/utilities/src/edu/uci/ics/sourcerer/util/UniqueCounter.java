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

import java.util.Comparator;
import java.util.Set;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class UniqueCounter <E, T extends Comparable<T>> {
  private Set<E> values = null; 
  private T object = null;
  
  public UniqueCounter(T object) {
    this.object = object;
    values = Helper.newHashSet();
  }
  
  public void add(E value) {
    values.add(value);
  }
  
  public int getCount() {
    return values.size();
  }
  
  public T getObject() {
    return object;
  }
  
  public static <E, T extends Comparable<T>> Comparator<UniqueCounter<E, T>> getReverseComparator() {
    return new Comparator<UniqueCounter<E, T>>() {
      @Override
      public int compare(UniqueCounter<E, T> o1, UniqueCounter<E, T> o2) {
        if (o1.values.size() == o2.values.size()) {
          return o1.object.compareTo(o2.object);
        } else {
          return o2.values.size() - o1.values.size();
        }
      }
    };
  }
}
