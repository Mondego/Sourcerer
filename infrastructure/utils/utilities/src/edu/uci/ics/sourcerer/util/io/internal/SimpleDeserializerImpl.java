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
package edu.uci.ics.sourcerer.util.io.internal;

import static edu.uci.ics.sourcerer.util.io.logging.Logging.logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.logging.Level;

import edu.uci.ics.sourcerer.util.io.CustomSerializable;
import edu.uci.ics.sourcerer.util.io.IOUtils;
import edu.uci.ics.sourcerer.util.io.LineBuilder;
import edu.uci.ics.sourcerer.util.io.ObjectDeserializer;
import edu.uci.ics.sourcerer.util.io.SimpleDeserializer;
import edu.uci.ics.sourcerer.util.io.SimpleSerializable;
import edu.uci.ics.sourcerer.util.io.logging.TaskProgressLogger;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
final class SimpleDeserializerImpl implements SimpleDeserializer {
  private final File file;
  private BufferedReader br;
  
  private SimpleDeserializerImpl(File file, BufferedReader br) {
    this.file = file;
    this.br = br;
  }
  
  static SimpleDeserializerImpl create(File file) throws IOException {
    return new SimpleDeserializerImpl(file, IOUtils.createBufferedReader(file));
  }
  
  static SimpleDeserializer create(InputStream is) throws IOException {
    return new SimpleDeserializerImpl(new File("/InputStream"), new BufferedReader(new InputStreamReader(is)));
  }
  
  private <T extends CustomSerializable> EntryReader<T> positionForNext(ObjectDeserializer<T> deserializer) throws IOException {
    if (br == null) {
      throw new NoSuchElementException("File already closed, unable to read custom deserializer.");
    } else {
      try {
        // Read the class name
        String line = br.readLine();
        if (line == null) {
          close();
          throw new NoSuchElementException("File is empty, unable to read custom deserializer.");
        } else if (SimpleSerializerImpl.DIVIDER.equals(line)) {
          return null;
        }
        
        Class<?> loadedClass = Class.forName(line);
        
        // Read in the fields
        line = br.readLine();
        if ("serialize".equals(line)) {
          if (CustomSerializable.class.isAssignableFrom(loadedClass)) {
            return new CustomEntryReader<T>(deserializer);
          } else {
            throw new IllegalStateException("File says custom serializable, but " + loadedClass.getName() + " is not");
          }
        } else {
          throw new IllegalStateException("Requested custom serializable, but file says it's not.");
        }
      } catch (ClassNotFoundException e) {
        throw new IllegalStateException("Unable to load class for deserialization.", e);
      }
    }
  }
  
  private <T extends SimpleSerializable> EntryReader<T> positionForNext(Class<T> klass, boolean trans) throws IOException {
    if (br == null) {
      throw new NoSuchElementException("File already closed, unable to read " + klass.getName() + ".");
    } else {
      try {
        // Read the class name
        String line = br.readLine();
        if (line == null) {
          close();
          throw new NoSuchElementException("File is empty, unable to read " + klass.getName() + ".");
        } else if (SimpleSerializerImpl.DIVIDER.equals(line)) {
          return null;
        }
        // Verify the class name matches
        Class<?> loadedClass = Class.forName(line);
        
        if (!klass.isAssignableFrom(loadedClass)) {
          close();
          throw new IllegalStateException("Specified type does not match file: " + klass.getName() + " vs " + loadedClass.getName());
        }
        
        // Read in the fields
        line = br.readLine();
        if ("serialize".equals(line)) {
          if (CustomSerializable.class.isAssignableFrom(loadedClass)) {
            return new CustomEntryReader<T>(loadedClass);
          } else {
            throw new IllegalStateException("File says custom serializable, but " + loadedClass.getName() + " is not");
          }
        } else {
          String[] fieldNames = LineBuilder.splitLine(line);
          Field[] fields = new Field[fieldNames.length];
          ObjectDeserializer<?>[] deserializers = new ObjectDeserializer[fieldNames.length];
          for (int i = 0; i < fieldNames.length; i++) {
            fields[i] = loadedClass.getDeclaredField(fieldNames[i]);
            fields[i].setAccessible(true);
            deserializers[i] = ObjectDeserializer.makeDeserializer(fields[i].getType());
          }
          if (trans) {
            return new TransientEntryReader<T>(loadedClass, fields, deserializers);
          } else {
            return new BasicEntryReader<T>(loadedClass, fields, deserializers);
          }
        }
      } catch (ClassNotFoundException e) {
        throw new IllegalStateException("Unable to load class for deserialization.", e);
      } catch (SecurityException e) {
        throw new IllegalStateException("JVM does not have sufficient security priviliges for deserialization.", e);
      } catch (NoSuchFieldException e) {
        throw new IllegalStateException("Unable to find field for deserialization.", e);
      } catch (NoSuchMethodException e) {
        throw new IllegalStateException("Unable to find method for deserialization.", e);
      } catch (InstantiationException e) {
        throw new IllegalStateException("Unable to instantiate object for deserialization.", e);
      } catch (IllegalAccessException e) {
        throw new IllegalStateException("JVM does not have sufficient security priviliges for deserialization.", e);
      } catch (InvocationTargetException e) {
        throw new IllegalStateException("Exception during object instantiation for deserialization.", e);
      }
    }
  }
  
  private static abstract class EntryReader<T> {
    public abstract T create(Scanner scanner) throws InstantiationException, IllegalAccessException, InvocationTargetException;
    
    public final T create(String line) throws InstantiationException, IllegalAccessException, InvocationTargetException {
      Scanner scanner = LineBuilder.getScanner(line);
      return create(scanner);
    }
  }
  
  private static class CustomEntryReader<T> extends EntryReader<T> {
    private ObjectDeserializer<?> deserializer;
    
    public CustomEntryReader(Class<?> klass) {
      this.deserializer = ObjectDeserializer.makeDeserializer(klass);
    }
    
    public CustomEntryReader(ObjectDeserializer<T> deserializer) {
      this.deserializer = deserializer;
    }
    
    @SuppressWarnings("unchecked")
    public T create(Scanner scanner) {
      return (T) deserializer.deserialize(scanner);
    }
  }
  
  private static class BasicEntryReader<T> extends EntryReader<T> {
    private Constructor<T> constructor;
    private Field[] fields;
    private ObjectDeserializer<?>[] deserializers;
    
    @SuppressWarnings("unchecked")
    private BasicEntryReader(Class<?> klass, Field[] fields, ObjectDeserializer<?>[] deserializers) throws SecurityException, NoSuchMethodException {
      this.fields = fields;
      this.deserializers = deserializers;
      for (Constructor<?> con : klass.getDeclaredConstructors()) {
        if (con.getParameterTypes().length == 0) {
          this.constructor = (Constructor<T>) con;
          con.setAccessible(true);
          break;
        }
      }
    }
    
    public T create(Scanner scanner) throws InstantiationException, IllegalAccessException, InvocationTargetException  {
      T obj = constructor.newInstance();
      for (int i = 0; i < fields.length; i++) {
        fields[i].set(obj, deserializers[i].deserialize(scanner));
      }
      return obj;
    }
  }
  
  private static class TransientEntryReader<T> extends EntryReader<T> {
    private Field[] fields;
    private ObjectDeserializer<?>[] deserializers;
    private T obj;
    
    @SuppressWarnings("unchecked")
    private TransientEntryReader(Class<?> klass, Field[] fields, ObjectDeserializer<?>[] deserializers) throws SecurityException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
      this.fields = fields;
      this.deserializers = deserializers;
      for (Constructor<?> con : klass.getDeclaredConstructors()) {
        if (con.getParameterTypes().length == 0) {
          con.setAccessible(true);
          obj = (T) con.newInstance();
          break;
        }
      }
    }
    
    public T create(Scanner scanner) throws IllegalAccessException {
      for (int i = 0; i < fields.length; i++) {
        fields[i].set(obj, deserializers[i].deserialize(scanner));
      }
      return obj;
    }
  }
  
  @Override
  public void close() {
    IOUtils.close(br);
    br = null;
  }

  @Override
  public <T extends SimpleSerializable> Iterable<T> deserializeToIterable(Class<T> klass) throws IOException {
    return deserializeToIterable(klass, false, false);
  }

  private <T extends SimpleSerializable> Iterable<T> makeIterable(final EntryReader<T> entryReader, final boolean closeOnCompletion) {
    return new Iterable<T>() {
      @Override
      public Iterator<T> iterator() {
        return new Iterator<T>() {
          private boolean open = true;
          private T next = null;

          @Override
          public boolean hasNext() {
            try {
              while (next == null && open) {
                String line = br.readLine();
                if (SimpleSerializerImpl.isFinished(line)) {
                  open = false;
                } else {
                  try {
                    next = entryReader.create(line);
                  } catch (NoSuchElementException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                    logger.log(Level.SEVERE, "Error deserializing line: " + line + " in " + file.getPath(), e);
                  }
                }
              }
            } catch (IOException e) {
              logger.log(Level.SEVERE, "Error reading line", e);
              open = false;
              close();
            }
            if (next == null) {
              if (closeOnCompletion) {
                close();
              }
              return false;
            } else {
              return true;
            }
          }

          @Override
          public T next() {
            if (hasNext()) {
              T ret = next;
              next = null;
              return ret;
            } else {
              throw new NoSuchElementException();
            }
          }
          
          @Override
          public void remove() {
            throw new UnsupportedOperationException();
          }};
      }
    };
  }
  
  @Override
  public <T extends SimpleSerializable> Iterable<T> deserializeToIterable(Class<T> klass, final boolean closeOnCompletion, boolean trans) throws IOException {
    final EntryReader<T> entryReader = positionForNext(klass, trans);
    if (entryReader == null) {
      return Collections.emptyList();
    } else {
      return makeIterable(entryReader, closeOnCompletion);
    }
  }

  @Override
  public <T extends SimpleSerializable> Collection<T> deserializeToCollection(Class<T> klass) throws IOException {
    EntryReader<T> entryReader = positionForNext(klass, false);
    if (entryReader == null) {
      return Collections.emptyList();
    } else {
      Collection<T> coll = new LinkedList<>();
      for (String line = br.readLine(); !SimpleSerializerImpl.isFinished(line); line = br.readLine()) {
        try {
          coll.add(entryReader.create(line));
        } catch (NoSuchElementException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
          logger.log(Level.SEVERE, "Error deserializing line: " + line + " in " + file.getPath(), e);
        }
      }
      return coll;
    }
  }

  @Override
  public <T extends CustomSerializable> Iterable<T> deserializeToIterable(ObjectDeserializer<T> deserializer, boolean closeOnCompletion) throws IOException {
    EntryReader<T> entryReader = positionForNext(deserializer);
    if (entryReader == null) {
      return Collections.emptyList();
    } else {
      return makeIterable(entryReader, closeOnCompletion);
    }
  }
  
  @Override
  public <T extends CustomSerializable> Collection<T> deserializeToCollection(ObjectDeserializer<T> deserializer) throws IOException {
    EntryReader<T> entryReader = positionForNext(deserializer);
    if (entryReader == null) {
      return Collections.emptyList();
    } else {
      Collection<T> coll = new LinkedList<>();
      for (String line = br.readLine(); !SimpleSerializerImpl.isFinished(line); line = br.readLine()) {
        try {
          coll.add(entryReader.create(line));
        } catch (NoSuchElementException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
          logger.log(Level.SEVERE, "Error deserializing line: " + line + " in " + file.getPath(), e);
        }
      }
      return coll;
    }
  }

  private static class MapBuilder<K, V> {
    private Map<K, V> map;
    private EntryReader<K> keyReader;
    private EntryReader<V> valueReader;
    private final boolean allowNullValues;
    
    public MapBuilder(Map<K, V> map, EntryReader<K> keyReader, EntryReader<V> valueReader, boolean allowNullValues) {
      this.map = map;
      this.keyReader = keyReader;
      this.valueReader = valueReader;
      this.allowNullValues = allowNullValues;
    }
    
    public void add(String line) {
      Scanner scanner = LineBuilder.getScanner(line);
      try {
        K key = keyReader.create(scanner);
        V value = valueReader.create(scanner);
        if (allowNullValues || value != null) {
          map.put(key, value);
        }
      } catch (InstantiationException e) {
        logger.log(Level.SEVERE, "Unable to deserialize: " + line, e);
      } catch (IllegalAccessException e) {
        logger.log(Level.SEVERE, "Unable to deserialize: " + line, e);
      } catch (InvocationTargetException e) {
        logger.log(Level.SEVERE, "Unable to deserialize: " + line, e);
      }
    }
    
    public Map<K, V> getMap() {
      return map;
    }
  }
  
  @SuppressWarnings("unchecked")
  private <K, V> Map<K, V> createMap() throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
    // Read the class name
    String line = br.readLine();
    if (line == null) {
      close();
      throw new NoSuchElementException("File is empty, unable to read map.");
    } else if (SimpleSerializerImpl.DIVIDER.equals(line)) {
      return null;
    }
    
    Class<?> loadedClass = Class.forName(line);
    if (!Map.class.isAssignableFrom(loadedClass)) {
      logger.log(Level.SEVERE, "Map deserialization requested, but type is " + loadedClass.getName());
    }
    return (Map<K, V>)loadedClass.newInstance();
  }
  
  private <K, V> MapBuilder<K, V> positionForNext(Class<K> key, Class<V> value, boolean allowNullValues) throws IOException {
    if (br == null) {
      logger.log(Level.SEVERE, "File already closed, unable to deserialize map.");
      return null;
    } else {
      try {
        Map<K, V> map = createMap();
        if (map == null) {
          return null;
        }
        // Read in the fields
        String[] fieldNames = LineBuilder.splitLine(br.readLine());
        int idx = 0;
        
        EntryReader<K> keyReader = null;
        if ("serialize".equals(fieldNames[idx])) {
          keyReader = new CustomEntryReader<K>(key);
          idx++;
        } else {
          int fieldCount = Integer.parseInt(fieldNames[idx++]);
          Field[] fields = new Field[fieldCount];
          ObjectDeserializer<?>[] deserializers = new ObjectDeserializer[fieldCount];
          for (int i = 0; i < fieldCount; i++) {
            fields[i] = key.getDeclaredField(fieldNames[idx++]);
            fields[i].setAccessible(true);
            deserializers[i] = ObjectDeserializer.makeDeserializer(fields[i].getType());
            if (deserializers[i] == null) {
              return null;
            }
          }
          keyReader = new BasicEntryReader<K>(key, fields, deserializers);
          idx += fieldCount;
        }
        
        EntryReader<V> valueReader = null;
        if ("serialize".equals(fieldNames[idx])) {
          valueReader = new CustomEntryReader<V>(value);
        } else {
          int fieldCount = Integer.parseInt(fieldNames[idx++]);
          Field[] fields = new Field[fieldCount];
          ObjectDeserializer<?>[] deserializers = new ObjectDeserializer[fieldCount];
          for (int i = 0; i < fieldCount; i++) {
            fields[i] = value.getDeclaredField(fieldNames[idx++]);
            fields[i].setAccessible(true);
            deserializers[i] = ObjectDeserializer.makeDeserializer(fields[i].getType());
            if (deserializers[i] == null) {
              return null;
            }
          }
          valueReader = new BasicEntryReader<V>(value, fields, deserializers);
        }
        
        return new MapBuilder<K, V>(map, keyReader, valueReader, allowNullValues);
      } catch (ClassNotFoundException e) {
        logger.log(Level.SEVERE, "Unable to load class for deserialization.", e);
      } catch (SecurityException e) {
        logger.log(Level.SEVERE, "JVM does not have sufficient security priviliges for deserialization.", e);
      } catch (NoSuchFieldException e) {
        logger.log(Level.SEVERE, "Unable to find field for deserialization.", e);
      } catch (NoSuchMethodException e) {
        logger.log(Level.SEVERE, "Unable to find method for deserialization.", e);
      } catch (InstantiationException e) {
        logger.log(Level.SEVERE, "Unable to instantiate object for deserialization.", e);
      } catch (IllegalAccessException e) {
        logger.log(Level.SEVERE, "JVM does not have sufficient security priviliges for deserialization.", e);
      } 
      return null;
    }
  }
  
  private <K, V> MapBuilder<K, V> positionForNext(ObjectDeserializer<K> keyDeserializer, Class<V> value, boolean allowNullValues) throws IOException {
    if (br == null) {
      logger.log(Level.SEVERE, "File already closed, unable to deserialize map.");
      return null;
    } else {
      try {
        Map<K, V> map = createMap();
        
        // Read in the fields
        String[] fieldNames = LineBuilder.splitLine(br.readLine());
        int idx = 0;
        
        EntryReader<K> keyReader = new CustomEntryReader<K>(keyDeserializer);
        if ("serialize".equals(fieldNames[idx])) {
          idx++;
        } else {
          int fieldCount = Integer.parseInt(fieldNames[idx++]);
          idx += fieldCount;
        }
        
        EntryReader<V> valueReader = null;
        if ("serialize".equals(fieldNames[idx])) {
          valueReader = new CustomEntryReader<V>(value);
        } else {
          int fieldCount = Integer.parseInt(fieldNames[idx++]);
          Field[] fields = new Field[fieldCount];
          ObjectDeserializer<?>[] deserializers = new ObjectDeserializer[fieldCount];
          for (int i = 0; i < fieldCount; i++) {
            fields[i] = value.getDeclaredField(fieldNames[idx++]);
            fields[i].setAccessible(true);
            deserializers[i] = ObjectDeserializer.makeDeserializer(fields[i].getType());
          }
          valueReader = new BasicEntryReader<V>(value, fields, deserializers);
        }
        
        return new MapBuilder<K, V>(map, keyReader, valueReader, allowNullValues);
      } catch (ClassNotFoundException e) {
        throw new IllegalStateException("Unable to load class for deserialization.", e);
      } catch (SecurityException e) {
        throw new IllegalStateException("JVM does not have sufficient security priviliges for deserialization.", e);
      } catch (NoSuchFieldException e) {
        throw new IllegalStateException("Unable to find field for deserialization.", e);
      } catch (NoSuchMethodException e) {
        throw new IllegalStateException("Unable to find method for deserialization.", e);
      } catch (InstantiationException e) {
        throw new IllegalStateException("Unable to instantiate object for deserialization.", e);
      } catch (IllegalAccessException e) {
        throw new IllegalStateException("JVM does not have sufficient security priviliges for deserialization.", e);
      } 
    }
  }
  
  private <K, V> MapBuilder<K, V> positionForNext(Class<K> key, ObjectDeserializer<V> valueDeserializer, boolean allowNullValues) throws IOException {
    if (br == null) {
      logger.log(Level.SEVERE, "File already closed, unable to deserialize map.");
      return null;
    } else {
      try {
        Map<K, V> map = createMap();
        if (map == null) {
          return null;
        }
        // Read in the fields
        String[] fieldNames = LineBuilder.splitLine(br.readLine());
        int idx = 0;
        
        EntryReader<K> keyReader = null;
        if ("serialize".equals(fieldNames[idx])) {
          keyReader = new CustomEntryReader<K>(key);
          idx++;
        } else {
          int fieldCount = Integer.parseInt(fieldNames[idx++]);
          Field[] fields = new Field[fieldCount];
          ObjectDeserializer<?>[] deserializers = new ObjectDeserializer[fieldCount];
          for (int i = 0; i < fieldCount; i++) {
            fields[i] = key.getDeclaredField(fieldNames[idx++]);
            fields[i].setAccessible(true);
            deserializers[i] = ObjectDeserializer.makeDeserializer(fields[i].getType());
          }
          keyReader = new BasicEntryReader<K>(key, fields, deserializers);
          idx += fieldCount;
        }
        
        EntryReader<V> valueReader = new CustomEntryReader<V>(valueDeserializer);
        
        return new MapBuilder<K, V>(map, keyReader, valueReader, allowNullValues);
      } catch (ClassNotFoundException e) {
        throw new IllegalStateException("Unable to load class for deserialization.", e);
      } catch (SecurityException e) {
        throw new IllegalStateException("JVM does not have sufficient security priviliges for deserialization.", e);
      } catch (NoSuchFieldException e) {
        throw new IllegalStateException("Unable to find field for deserialization.", e);
      } catch (NoSuchMethodException e) {
        throw new IllegalStateException("Unable to find method for deserialization.", e);
      } catch (InstantiationException e) {
        throw new IllegalStateException("Unable to instantiate object for deserialization.", e);
      } catch (IllegalAccessException e) {
        throw new IllegalStateException("JVM does not have sufficient security priviliges for deserialization.", e);
      } 
    }
  }
  
  private <K, V> MapBuilder<K, V> positionForNext(ObjectDeserializer<K> keyDeserializer, ObjectDeserializer<V> valueDeserializer, boolean allowNullValues) throws IOException {
    if (br == null) {
      logger.log(Level.SEVERE, "File already closed, unable to deserialize map.");
      return null;
    } else {
      try {
        Map<K, V> map = createMap();
        if (map == null) {
          return null;
        }
        // Read in the fields
        br.readLine();
        
        return new MapBuilder<K, V>(map, new CustomEntryReader<K>(keyDeserializer), new CustomEntryReader<V>(valueDeserializer), allowNullValues);
      } catch (ClassNotFoundException e) {
        throw new IllegalStateException("Unable to load class for deserialization.", e);
      } catch (InstantiationException e) {
        throw new IllegalStateException("Unable to instantiate map for deserialization.", e);
      } catch (IllegalAccessException e) {
        throw new IllegalStateException("Unable to instantiate map for deserialization.", e);
      }
    }
  }
  
  private <K, V> Map<K, V> buildMap(MapBuilder<K, V> builder) throws IOException {
    if (builder != null) {
      TaskProgressLogger task = TaskProgressLogger.get();
      task.start("Reading lines", "lines read", 500);
      for (String line = br.readLine(); !SimpleSerializerImpl.isFinished(line); line = br.readLine()) {
        builder.add(line);
        task.progress();
      }
      task.finish();
      return builder.getMap();
    } else {
      return Collections.emptyMap();
    }
  }
  
  @Override
  public <K, V> Map<K, V> deserializeMap(Class<K> key, Class<V> value, boolean allowNullValues) throws IOException {
    return buildMap(positionForNext(key, value, allowNullValues));
  }
  
  @Override
  public <K, V> Map<K, V> deserializeMap(ObjectDeserializer<K> keyDeserializer, Class<V> value, boolean allowNullValues) throws IOException {
    return buildMap(positionForNext(keyDeserializer, value, allowNullValues));
  }
  
  @Override
  public <K, V> Map<K, V> deserializeMap(Class<K> key, ObjectDeserializer<V> valueDeserializer, boolean allowNullValues) throws IOException {
    return buildMap(positionForNext(key, valueDeserializer, allowNullValues));
  }
  
  @Override
  public <K, V> Map<K, V> deserializeMap(ObjectDeserializer<K> keyDeserializer, ObjectDeserializer<V> valueDeserializer, boolean allowNullValues) throws IOException {
    return buildMap(positionForNext(keyDeserializer, valueDeserializer, allowNullValues));
  }
}
