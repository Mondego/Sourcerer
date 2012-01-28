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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class CreationistMap<K, V> {
  private Map<K, V> map;
  private Constructor<V> constructor;
  
  public CreationistMap(Class<V> klass) {
    map = new HashMap<>();
    try {
      constructor = klass.getDeclaredConstructor();
      constructor.setAccessible(true);
    } catch (NoSuchMethodException | SecurityException e) {
      logger.log(Level.SEVERE, "Unable to find constructor for creationist map", e);
    }
  }
  
  public V get(K key) {
    V value = map.get(key);
    if (value == null) {
      try {
        value = constructor.newInstance();
      } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
        logger.log(Level.SEVERE, "Unable to create instance for creationist map", e);
        return null;
      }
      map.put(key, value);
    }
    return value;
  }
  
  public Set<? extends Map.Entry<K, V>> entrySet() {
    return map.entrySet();
  }
}