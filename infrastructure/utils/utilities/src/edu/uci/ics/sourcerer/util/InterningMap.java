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

import java.lang.ref.WeakReference;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * The value must hold a strong reference to the key.
 * 
 * @author Joel Ossher (jossher@uci.edu)
 */
public class InterningMap<K, V> implements Map<K, V> {
//  private WeakReference<Map<K, WeakReference<V>>> mapReference;
  private WeakHashMap<K, WeakReference<V>> map;
  
  public InterningMap() {
//    mapReference = new WeakReference<>(null);
    map = new WeakHashMap<>();
  }
  
  private V convert(WeakReference<V> ref) {
    if (ref == null) {
      return null;
    } else {
      return ref.get();
    }
  }
  
  private Map<K, WeakReference<V>> getMap() {
//    return mapReference.get();
    return map;
  }
  
  private Map<K, WeakReference<V>> ensureMap() {
//    Map<K, WeakReference<V>> map = mapReference.get();
//    if (map == null) {
//      map = new HashMap<>();
//      mapReference = new WeakReference<>(map);
//    }
    return map;
  }
  
  @Override
  public int size() {
    Map<?, ?> map = getMap();
    if (map == null) {
      return 0;
    } else {
      return map.size();
    }
  }

  @Override
  public boolean isEmpty() {
    return size() == 0;
  }

  @Override
  public boolean containsKey(Object key) {
    Map<?, ?> map = getMap();
    if (map == null) {
      return false;
    } else {
      return map.containsKey(key);
    }
  }

  @Override
  public boolean containsValue(Object value) {
    throw new UnsupportedOperationException("Interning map doesn't do this.");
  }

  @Override
  public V get(Object key) {
    Map<K, WeakReference<V>> map = getMap();
    if (map == null) {
      return null;
    } else {
      return convert(map.get(key));
    }
  }

  
  @Override
  public V put(K key, V value) {
    Map<K, WeakReference<V>> map = ensureMap();
    return convert(map.put(key, new WeakReference<>(value)));
  }

  @Override
  public V remove(Object key) {
    Map<K, WeakReference<V>> map = getMap();
    if (map == null) {
      return null;
    } else {
      return convert(map.remove(key));
    }
  }

  @Override
  public void putAll(Map<? extends K, ? extends V> m) {
    Map<K, WeakReference<V>> map = ensureMap();
    for (Map.Entry<? extends K, ? extends V> entry : m.entrySet()) {
      map.put(entry.getKey(), new WeakReference<V>(entry.getValue()));
    }
  }

  @Override
  public void clear() {
    Map<K, WeakReference<V>> map = getMap();
    if (map != null) {
      map.clear();
    }
  }

  @Override
  public Set<K> keySet() {
    Map<K, WeakReference<V>> map = getMap();
    if (map == null) {
      return Collections.emptySet();
    } else {
      return map.keySet();
    }
  }

  @Override
  public Collection<V> values() {
    Map<K, WeakReference<V>> map = getMap();
    if (map == null) {
      return Collections.emptySet();
    } else {
      Collection<WeakReference<V>> values = map.values();
      Collection<V> result = new ArrayList<>(values.size());
      for (WeakReference<V> ref : values) {
        if (ref != null) {
          V val = ref.get();
          if (val != null) {
            result.add(val);
          }
        }
      }
      return result;
    }
  }

  @Override
  public Set<Map.Entry<K, V>> entrySet() {
    Map<K, WeakReference<V>> map = getMap();
    if (map == null) {
      return Collections.emptySet();
    } else {
      Set<Map.Entry<K, V>> result = new HashSet<>();
      for (Map.Entry<K, WeakReference<V>> entry : map.entrySet()) {
        WeakReference<V> ref = entry.getValue();
        if (ref != null) {
          V value = ref.get();
          if (value != null) {
            result.add(new SimpleImmutableEntry<K, V>(entry.getKey(), value));
          }
        }
      }
      return result; 
    }
  }
}
