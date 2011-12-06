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
package edu.uci.ics.sourcerer.tools.java.utilization.model;

import static edu.uci.ics.sourcerer.util.io.Logging.logger;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class JarSetMap<E> implements Iterable<Jar> {
  private final JarMapFactoryImpl<E> factory;
  private final Map<Jar, E> jars;
  
  private JarSetMap(JarMapFactoryImpl<E> factory) {
    this.factory = factory;
    jars = Collections.emptyMap();
  }
  
  private JarSetMap(JarMapFactoryImpl<E> factory, Map<Jar, E> jars, Jar jar, E value) {
    this.factory = factory;
    if (jars.isEmpty()) {
      this.jars = Collections.singletonMap(jar, value);
    } else {
      this.jars = new HashMap<>(jars);
      this.jars.put(jar, value);
    }
  }
  
  private JarSetMap(JarMapFactoryImpl<E> factory, Map<Jar, E> jars) {
    this.factory = factory;
    this.jars = jars;
  }
  
  public JarSetMap<E> add(Jar jar, E newValue) {
    E value = jars.get(jar);
    if (value == null) {
      int hashCode = hashCode() + jar.hashCode();
      for (JarSetMap<E> set : factory.lookup(hashCode)) {
        if (set.jars.size() == jars.size() + 1 && set.jars.contains(jar) && set.jars.containsAll(jars)) {
          return set;
        }
      }
      JarSetMap set = new JarSetMap(jars, jar);
      sets.put(hashCode, set);
      return set;
    } else {
      if (value.equals(newValue)) {
        return this;
      } else {
        logger.severe("Value conflict for " + jar + ": " + value + " vs " + newValue);
      }
    }
  }
  
  public JarSetMap merge(JarSetMap other) {
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
      for (JarSetMap set : sets.get(hashCode)) {
        if (set.jars.size() == newSet.size() && set.jars.containsAll(newSet)) {
          return set;
        }
      }
      JarSetMap set = new JarSetMap(newSet);
      sets.put(hashCode, set);
      return set;
    }
  }
  
  public boolean contains(Jar jar) {
    return jars.contains(jar);
  }
  
  public int getIntersectionSize(JarSetMap other) {
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
  
  private static class JarMapFactoryImpl<E> implements JarMapFactory<E> {
    Multimap<Integer, JarSetMap<E>> sets = ArrayListMultimap.create();
    
    @Override
    public JarSetMap<E> makeEmpty() {
      for (JarSetMap<E> set : sets.get(0)) {
        if (set.jars.isEmpty()) {
          return set;
        }
      }
      JarSetMap<E> set = new JarSetMap<E>(this);
      sets.put(0, set);
      return set;
    }
    
    public Iterable<JarSetMap<E>> lookup(int hashCode) {
      return sets.get(hashCode);
    }
  };
  
  public static <E> JarMapFactory<E> createFactory() {
    return new JarMapFactoryImpl<>();
  }
}
