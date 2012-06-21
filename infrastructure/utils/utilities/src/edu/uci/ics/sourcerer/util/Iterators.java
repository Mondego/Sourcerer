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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class Iterators {
  @SuppressWarnings("unchecked")
  public static <T> Iterable<T> concat(final Iterable<T> ... iterables) {
    return new Iterable<T>() {
      @Override
      public Iterator<T> iterator() {
        return new Iterator<T>() {
          int index = 0;
          Iterator<T> iter = iterables[index].iterator();
          Iterator<T> removeFrom = null;
          
          @Override
          public boolean hasNext() {
            while (iter == null || !iter.hasNext()) {
              if (++index < iterables.length) {
                iter = iterables[index].iterator();
              } else {
                return false;
              }
            }
            return true;
          }

          @Override
          public T next() {
            if (hasNext()) {
              removeFrom = iter;
              return iter.next();
            } else {
              throw new NoSuchElementException();
            }
          }

          @Override
          public void remove() {
            if (removeFrom == null) {
              throw new IllegalStateException("Element already removed.");
            } else {
              removeFrom.remove();
              removeFrom = null;
            }
          }
        };
      }
    };
  }
  
  @SuppressWarnings("unchecked")
  public static <T> T[] toArray(Iterable<T> iterable) {
    ArrayList<T> list = new ArrayList<>();
    for (T item : iterable) {
      list.add(item);
    }
    return (T[]) list.toArray();
  }
  
  public static <T> T[] toArray(Iterable<T> iterable, T[] hint) {
    ArrayList<T> list = new ArrayList<>(hint.length);
    for (T item : iterable) {
      list.add(item);
    }
    return list.toArray(hint);
  }
}
