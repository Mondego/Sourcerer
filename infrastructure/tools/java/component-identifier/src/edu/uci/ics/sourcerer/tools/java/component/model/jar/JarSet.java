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
package edu.uci.ics.sourcerer.tools.java.component.model.jar;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import com.google.common.collect.ImmutableSet;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class JarSet implements Iterable<Jar> {
  private final static Map<Set<Jar>, Reference<JarSet>> sets = new WeakHashMap<>();
  private final Set<Jar> jars;

  private JarSet(Set<Jar> jars) {
    this.jars = jars;
  }
  
  private static JarSet create(Set<Jar> set) {
    Reference<JarSet> ref = sets.get(set);
    JarSet result = ref == null ? null : ref.get();
    if (result == null) {
      result = new JarSet(ImmutableSet.copyOf(set));
      sets.put(result.jars, new WeakReference<>(result));
    }
    return result;
  }
  
  public static JarSet create() {
    return create(ImmutableSet.<Jar>of());
  }
  
  public static JarSet create(Jar jar) {
    return create(ImmutableSet.<Jar>of(jar));
  }
  
  public JarSet add(Jar jar) {
    if (jars.contains(jar)) {
      return this;
    } else {
      Set<Jar> set = new HashSet<>(jars);
      set.add(jar);
      return create(set);
    }
  }
  
//  public JarSet remove(Jar jar) {
//    if (jars.contains(jar)) {
//      int hashCode = 0;
//      for (Jar j : jars) {
//        if (j != jar) {
//          hashCode += j.hashCode();
//        }
//      }
//      for (JarSet set : sets.get(hashCode)) {
//        if (set.jars.size() == jars.size() - 1 && !set.jars.contains(jar) && jars.containsAll(set.jars)) {
//          return set;
//        }
//      }
//      Set<Jar> newSet = new HashSet<>(jars);
//      newSet.remove(jar);
//      JarSet set = new JarSet(newSet);
//      sets.put(hashCode, set);
//      return set;
//    } else {
//      return this;
//    }
//  }
  
  public JarSet merge(JarSet other) {
    if (jars.containsAll(other.jars)) {
      return this;
    } else if (other.jars.containsAll(jars)) {
      return other;
    } else {
      Set<Jar> newSet = new HashSet<>();
      newSet.addAll(jars);
      newSet.addAll(other.jars);
      return create(newSet);
    }
  }
  
  public boolean contains(Jar jar) {
    return jars.contains(jar);
  }
  
  public int getIntersectionSize(JarSet other) {
    int count = 0;
    for (Jar jar : jars) {
      if (other.jars.contains(jar)) {
        count++;
      }
    }
    return count;
  }
  
  /**
   * Is this JarSet a subset of the argument?
   */
  public boolean isSubset(JarSet other) {
    for (Jar jar : jars) {
      if (!other.jars.contains(jar)) {
        return false;
      }
    }
    return true;
  }
  
  public int size() {
    return jars.size();
  }
  
  public boolean isEmpty() {
    return jars.isEmpty();
  }
  
//  @Override
//  public int hashCode() {
//    return jars.hashCode();
//  }

  @Override
  public Iterator<Jar> iterator() {
    return jars.iterator();
  }
  
  @Override
  public String toString() {
    return jars.toString();
  }
}
