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

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class Counter <T> {
  private int counter = 0;
  private T object = null;
  
  public Counter() {}
  
  public Counter(T object) {
    this.object = object;
  }
  
  public Counter(T object, int count) {
    this.object = object;
    this.counter = count;
  }
  
  public void add(int value) {
    counter += value;
  }
  
  public void increment() {
    counter++;
  }
  
  public int getCount() {
    return counter;
  }
  
  public T getObject() {
    return object;
  }
  
  public static <T extends Comparable<T>> Comparator<Counter<T>> getReverseComparator() {
    return new Comparator<Counter<T>>() {
      @Override
      public int compare(Counter<T> o1, Counter<T> o2) {
        if (o1.counter == o2.counter) {
          return o1.object.compareTo(o2.object);
        } else {
          return o2.counter - o1.counter;
        }
      }
    };
  }
  
  public static <T extends Comparable<T>> Comparator<Counter<T>> getComparator() {
    return new Comparator<Counter<T>>() {
      @Override
      public int compare(Counter<T> o1, Counter<T> o2) {
        if (o1.counter == o2.counter) {
          return o1.object.compareTo(o2.object);
        } else {
          return o1.counter - o2.counter;
        }
      }
    };
  }
}
