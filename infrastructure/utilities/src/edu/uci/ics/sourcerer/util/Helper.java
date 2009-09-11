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
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class Helper {
  public static <T> ArrayList<T> newArrayList(int initialCapacity) {
    return new ArrayList<T>(initialCapacity);
  }
  
  public static <T> ArrayList<T> newArrayList(Collection<? extends T> c) {
    return new ArrayList<T>(c);
  }
  
  public static <T> ArrayList<T> newArrayList() {
    return new ArrayList<T>();
  }
  
	public static <T> LinkedList<T> newLinkedList() {
		return new LinkedList<T>();
	}
	
	public static <K extends Enum<K>,V> EnumMap<K,V> newEnumMap(Class<K> klass) {
	  return new EnumMap<K, V>(klass);
	}
	
	public static <K,V> HashMap<K,V> newHashMap() {
		return new HashMap<K, V>();
	}
	
	public static <K,V> TreeMap<K,V> newTreeMap() {
	  return new TreeMap<K, V>();
	}
	
	public static <T> HashSet<T> newHashSet() {
	  return new HashSet<T>();
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
