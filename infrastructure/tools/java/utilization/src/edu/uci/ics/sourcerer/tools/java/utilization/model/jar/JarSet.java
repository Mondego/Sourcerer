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
package edu.uci.ics.sourcerer.tools.java.utilization.model.jar;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class JarSet implements Iterable<Jar> {
  private final static Multimap<Integer, JarSet> sets = ArrayListMultimap.create();
  private final Set<Jar> jars;
  
  private JarSet() {
    jars = Collections.emptySet();
  }
  
  private JarSet(Set<Jar> jars, Jar jar) {
    if (jars.isEmpty()) {
      this.jars = Collections.singleton(jar);
    } else {
      this.jars = new HashSet<>(jars);
      this.jars.add(jar);
    }
  }
  
  private JarSet(Set<Jar> jars) {
    this.jars = jars;
  }
  
  public static JarSet create() {
    for (JarSet set : sets.get(0)) {
      if (set.jars.isEmpty()) {
        return set;
      }
    }
    JarSet set = new JarSet();
    sets.put(0, set);
    return set;
  }
  
  public static JarSet create(Jar jar) {
    return create().add(jar);
  }
  
  public JarSet add(Jar jar) {
    int hashCode = hashCode() + jar.hashCode();
    for (JarSet set : sets.get(hashCode)) {
      if (set.jars.size() == jars.size() + 1 && set.jars.contains(jar) && set.jars.containsAll(jars)) {
        return set;
      }
    }
    JarSet set = new JarSet(jars, jar);
    sets.put(hashCode, set);
    return set;
  }
  
  public JarSet merge(JarSet other) {
    if (jars.containsAll(other.jars)) {
      return this;
    } else if (other.jars.containsAll(jars)) {
      return other;
    } else {
      Set<Jar> newSet = new HashSet<>();
      newSet.addAll(jars);
      newSet.addAll(other.jars);
      int hashCode = 0;
      for (Jar jar : newSet) {
        hashCode += jar.hashCode();
      }
      for (JarSet set : sets.get(hashCode)) {
        if (set.jars.size() == newSet.size() && set.jars.containsAll(newSet)) {
          return set;
        }
      }
      JarSet set = new JarSet(newSet);
      sets.put(hashCode, set);
      return set;
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
  
  public int size() {
    return jars.size();
  }
  
  @Override
  public int hashCode() {
    int hashCode = 0;
    if (jars != null) {
      for (Jar jar : jars) {
        hashCode += jar.hashCode();
      }
    }
    return hashCode;
  }

  @Override
  public Iterator<Jar> iterator() {
    return jars.iterator();
  }
}
