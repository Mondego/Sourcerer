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
package edu.uci.ics.sourcerer.util.io;

import java.io.Closeable;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public interface SimpleDeserializer extends Closeable {
  public <T extends SimpleSerializable> Iterable<T> deserializeToIterable(Class<T> klass) throws IOException;
  public <T extends SimpleSerializable> Iterable<T> deserializeToIterable(Class<T> klass, boolean closeOnCompletion, boolean trans) throws IOException;
  public <T extends SimpleSerializable> Collection<T> deserializeToCollection(Class<T> klass) throws IOException;
  public <T extends CustomSerializable> Iterable<T> deserializeToIterable(ObjectDeserializer<T> deserializer, boolean closeOnCompletion) throws IOException;
  public <T extends CustomSerializable> Collection<T> deserializeToCollection(ObjectDeserializer<T> deserializer) throws IOException;
  public <K, V> Map<K, V> deserializeMap(Class<K> key, Class<V> value, boolean allowNullValues) throws IOException;
  public <K, V> Map<K, V> deserializeMap(ObjectDeserializer<K> keyDeserializer, Class<V> value, boolean allowNullValues) throws IOException;
  public <K, V> Map<K, V> deserializeMap(Class<K> key, ObjectDeserializer<V> valueDeserializer, boolean allowNullValues) throws IOException;
  public <K, V> Map<K, V> deserializeMap(ObjectDeserializer<K> keyDeserializer, ObjectDeserializer<V> valueDeserializer, boolean allowNullValues) throws IOException;
}
