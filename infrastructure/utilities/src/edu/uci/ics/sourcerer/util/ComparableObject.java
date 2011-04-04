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
public class ComparableObject <T, C extends Comparable<C>> implements Comparable<ComparableObject<T, C>> {
  private T object;
  private C comp;
  
  public ComparableObject(T object, C comp) {
    this.object = object;
    this.comp = comp;
  }
  
  public T getObject() {
    return object;
  }
  
  public C getComp() {
    return comp;
  }
  
  @Override
  public int compareTo(ComparableObject<T, C> o) {
    return comp.compareTo(o.comp);
  }
  
  public static <T, C extends Comparable<C>> Comparator<ComparableObject<T, C>> getComparator() {
    return new Comparator<ComparableObject<T,C>>() {
      @Override
      public int compare(ComparableObject<T, C> o1, ComparableObject<T, C> o2) {
        return o1.comp.compareTo(o2.comp);
      }};
  }
  
  public static <T, C extends Comparable<C>> Comparator<ComparableObject<T, C>> getReverseComparator() {
    return new Comparator<ComparableObject<T,C>>() {
      @Override
      public int compare(ComparableObject<T, C> o1, ComparableObject<T, C> o2) {
        return o2.comp.compareTo(o1.comp);
      }};
  }
}
