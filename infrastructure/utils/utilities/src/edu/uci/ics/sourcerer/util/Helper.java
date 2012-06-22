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

import static edu.uci.ics.sourcerer.util.io.logging.Logging.logger;

import java.util.Collection;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class Helper {
 	@SuppressWarnings("unchecked")
  public static <A,B,K,V extends Map<A,B>> Map<A,B> getHashMapFromMap(Map<K,V> map, K key) {
	  Map<A,B> value = map.get(key);
	  if (value == null) {
	    value = new HashMap<>();
	    map.put(key, (V)value);
	  }
	  return value;
	}
	
	@SuppressWarnings("unchecked")
  public static <A,K,V extends Collection<A>> V getLinkedListFromMap(Map<K,V> map, K key) {
    V value = map.get(key);
    if (value == null) {
      value = (V)new LinkedList<>();
      map.put(key, value);
    }
    return value;
  }
	
	@SuppressWarnings("unchecked")
  public static <K,V> V getFromMap(Map<K, V> map, K key, Class<?> klass) {
	  V value = map.get(key);
	  if (value == null) {
	    try {
        value = (V) klass.newInstance();
      } catch (Exception e) {
        logger.log(Level.SEVERE, "Exception!", e);
        return null;
      }
	    map.put(key, value);
	  }
	  return value;
	}
	
	@SuppressWarnings("unchecked")
  public static <K, V, A extends V> A getSubFromMap(Map<K, V> map, K key, Class<A> klass) {
	  A value = (A) map.get(key);
    if (value == null) {
      try {
        value = klass.newInstance();
      } catch (Exception e) {
        return null;
      }
      map.put(key, value);
    }
    return value;
	}
	
	public static <K,V> TreeMap<K,V> newTreeMap() {
	  return new TreeMap<K, V>();
	}
	
	public static <T> HashSet<T> newHashSet() {
	  return new HashSet<T>();
	}
	
	public static <T extends Enum<T>> EnumSet<T> newEnumSet(Class<T> klass) {
	  return EnumSet.noneOf(klass);
	}
	
	public static <T> HashSet<T> newHashSet(Collection<? extends T> c) {
	  return new HashSet<T>(c);
	}
	
	public static <T> TreeSet<T> newTreeSet() {
    return new TreeSet<T>();
  }
	
	public static <T> TreeSet<T> newTreeSet(Comparator<? super T> comp) {
	  return new TreeSet<T>(comp);
	}
		
	public static <T> LinkedList<T> newStack() {
	  return new LinkedList<T>();
	}
	
	public static <T> LinkedList<T> newQueue() {
	  return new LinkedList<T>();
	}
}
