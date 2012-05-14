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

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class CollectionUtils {
  public static <T> boolean containsNone(Collection<T> outer, Iterable<T> inner) {
    for (T item : inner) {
      if (outer.contains(item)) {
        return false;
      }
    }
    return true;
  }
  
  public static <T> boolean containsAny(Collection<T> outer, Iterable<T> inner) {
    for (T item : inner) {
      if (outer.contains(item)) {
        return true;
      }
    }
    return false;
  }
  
  public static <T> double compuateJaccard(Collection<T> one, Collection<T> two) {
    int intersection = 0;
    int union = 0;
    for (T item : one) {
      if (two.contains(item)) {
        intersection++;
      }
      union++;
    }
    for (T item : two) {
      if (!one.contains(item)) {
        union++;
      }
    }
    return (double) intersection / (double) union;
  }
  
  public static <T> int intersectionSize(Collection<T> one, Collection<T> two) {
    int intersection = 0;
    for (T item : one) {
      if (two.contains(item)) {
        intersection++;
      }
    }
    return intersection;
  }
}
