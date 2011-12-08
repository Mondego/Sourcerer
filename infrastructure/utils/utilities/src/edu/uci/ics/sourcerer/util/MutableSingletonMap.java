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

import java.util.AbstractMap;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class MutableSingletonMap<K,V> extends AbstractMap<K, V> {
  private final K key;
  private V value;
  
  private MutableSingletonMap(K key, V value) {
    this.key = key;
    this.value = value;
  }
  
  public static <K, V> MutableSingletonMap<K, V> create(K key, V value) {
    return new MutableSingletonMap<>(key, value);
  }
  
  @Override
  public int size() {
    return 1;
  }
  
  @Override
  public boolean isEmpty() {
    return false;
  }
  
  @Override
  public boolean containsKey(Object key) {
    return eq(this.key, key);
  }
  
  @Override
  public boolean containsValue(Object value) {
    return eq(this.value, value);
  }
  
  @Override
  public V get(Object key) {
    return (eq(this.key, key) ? value : null);
  }
  
  @Override
  public V put(K key, V value) {
    if (eq(this.key, key)) {
      V old = this.value;
      this.value = value;
      return old;
    } else {
      throw new IllegalArgumentException("Cannot put two things into a singleton map");
    }
  }
  
  private static boolean eq(Object a, Object b) {
    return a == null ? b == null : a.equals(b);
  }

  private transient Set<Map.Entry<K, V>> entrySet = null;
  @Override
  public Set<Map.Entry<K, V>> entrySet() {
    if (entrySet == null) {
      entrySet = Collections.<Map.Entry<K, V>>singleton(new SimpleImmutableEntry<K, V>(key, value));
    }
    return entrySet;
  }
}
