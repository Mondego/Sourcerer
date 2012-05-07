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

import java.util.Iterator;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class Pair<First, Second> {
  protected First a;
  protected Second b;
  
  public Pair(First a, Second b) {
    this.a = a;
    this.b = b;
  }
  
  public First getFirst() {
    return a;
  }
  
  public Second getSecond() {
    return b;
  }
  
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    } else if (o instanceof Pair<?, ?>) {
      Pair<?, ?> other = (Pair<?, ?>) o;
      return (a == null ? other.a == null : a.equals(other.a)) && (b == null ? other.b == null : b.equals(other.b));
    } else {
      return false;
    }
  }
  
  @Override
  public int hashCode() {
    int hashCode = 1;
    if (a != null) {
      hashCode += 37 * a.hashCode();
    }
    if (b != null) {
      hashCode += 37 * b.hashCode();
    }
    return hashCode;
  }
  
  public static <First, Second> Iterable<First> firstIterable(final Iterable<Pair<First,Second>> iterable) {
    return new Iterable<First>() {
      @Override
      public Iterator<First> iterator() {
        final Iterator<Pair<First,Second>> iter = iterable.iterator();
        return new Iterator<First>() {
          @Override
          public void remove() {
            throw new UnsupportedOperationException("Removal not supported");
          }
        
          @Override
          public First next() {
            return iter.next().getFirst();
          }
        
          @Override
          public boolean hasNext() {
            return iter.hasNext();
          }
        };
      }
    };
  }
  
  public static <First, Second> Iterable<Second> rhsIterable(final Iterable<Pair<First, Second>> iterable) {
    return new Iterable<Second>() {
      @Override
      public Iterator<Second> iterator() {
        final Iterator<Pair<First, Second>> iter = iterable.iterator();
        return new Iterator<Second>() {
          @Override
          public void remove() {
            throw new UnsupportedOperationException("Removal not supported");
          }
        
          @Override
          public Second next() {
            return iter.next().getSecond();
          }
        
          @Override
          public boolean hasNext() {
            return iter.hasNext();
          }
        };
      }
    };
  }
}
