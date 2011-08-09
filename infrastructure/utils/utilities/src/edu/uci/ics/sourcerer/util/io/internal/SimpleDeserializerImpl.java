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

import static edu.uci.ics.sourcerer.util.io.Logging.logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.logging.Level;

import edu.uci.ics.sourcerer.util.Helper;
import edu.uci.ics.sourcerer.util.io.CustomSimpleSerializable;
import edu.uci.ics.sourcerer.util.io.IOUtils;
import edu.uci.ics.sourcerer.util.io.LineBuilder;
import edu.uci.ics.sourcerer.util.io.ObjectDeserializer;
import edu.uci.ics.sourcerer.util.io.SimpleDeserializer;
import edu.uci.ics.sourcerer.util.io.SimpleSerializable;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
final class SimpleDeserializerImpl implements SimpleDeserializer {
  private BufferedReader br;
  
  private SimpleDeserializerImpl(BufferedReader br) {
    this.br = br;
  }
  
  static SimpleDeserializerImpl make(File file) throws IOException {
    return new SimpleDeserializerImpl(IOUtils.makeBufferedReader(file));
  }
  
  private <T extends CustomSimpleSerializable> EntryReader<T> positionForNext(ObjectDeserializer<T> deserializer) throws IOException {
    if (br == null) {
      logger.log(Level.SEVERE, "File already closed, unable to read custom deserializer.");
      return null;
    } else {
      try {
        // Read the class name
        String line = br.readLine();
        if (line == null) {
          close();
          return null;
        }
        
        Class<?> loadedClass = Class.forName(line);
        
        // Read in the fields
        line = br.readLine();
        if ("writeToString".equals(line)) {
          if (CustomSimpleSerializable.class.isAssignableFrom(loadedClass)) {
            return new CustomEntryReader<T>(deserializer);
          } else {
            logger.log(Level.SEVERE, "File says custom serializable, but " + loadedClass.getName() + " is not");
            return null;
          }
        } else {
          return null;
        }
      } catch (ClassNotFoundException e) {
        logger.log(Level.SEVERE, "Unable to load class for deserialization.", e);
      }
      return null;
    }
  }
  
  private <T extends SimpleSerializable> EntryReader<T> positionForNext(Class<T> klass, boolean trans) throws IOException {
    if (br == null) {
      logger.log(Level.SEVERE, "File already closed, unable to read " + klass.getName() + ".");
      return null;
    } else {
      try {
        // Read the class name
        String line = br.readLine();
        if (line == null) {
          close();
          return null;
        }
        // Verify the class name matches
        Class<?> loadedClass = Class.forName(line);
        
        if (!klass.isAssignableFrom(loadedClass)) {
          logger.log(Level.SEVERE, "Specified type does not match file: " + klass.getName() + " vs " + loadedClass.getName());
          close();
          return null;
        }
        
        // Read in the fields
        line = br.readLine();
        if ("writeToString".equals(line)) {
          if (CustomSimpleSerializable.class.isAssignableFrom(loadedClass)) {
            return new CustomEntryReader<T>(loadedClass);
          } else {
            logger.log(Level.SEVERE, "File says custom serializable, but " + loadedClass.getName() + " is not");
            return null;
          }
        } else {
          String[] fieldNames = LineBuilder.splitLine(line);
          Field[] fields = new Field[fieldNames.length];
          ObjectDeserializer<?>[] deserializers = new ObjectDeserializer[fieldNames.length];
          for (int i = 0; i < fieldNames.length; i++) {
            fields[i] = loadedClass.getDeclaredField(fieldNames[i]);
            fields[i].setAccessible(true);
            deserializers[i] = ObjectDeserializer.makeDeserializer(fields[i].getType());
            if (deserializers[i] == null) {
              return null;
            }
          }
          if (trans) {
            return new TransientEntryReader<T>(loadedClass, fields, deserializers);
          } else {
            return new BasicEntryReader<T>(loadedClass, fields, deserializers);
          }
        }
        
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
      } catch (InvocationTargetException e) {
        logger.log(Level.SEVERE, "Exception during object instantiation for deserialization.", e);
      }
      return null;
    }
  }
  
  private interface EntryReader<T> {
    public T create(String line) throws InstantiationException, IllegalAccessException, InvocationTargetException;
  }
  
  private static class CustomEntryReader<T> implements EntryReader<T> {
    private ObjectDeserializer<?> deserializer;
    
    public CustomEntryReader(Class<?> klass) {
      this.deserializer = ObjectDeserializer.makeDeserializer(klass);
    }
    
    public CustomEntryReader(ObjectDeserializer<T> deserializer) {
      this.deserializer = deserializer;
    }
    
    @SuppressWarnings("unchecked")
    public T create(String line) {
      return (T) deserializer.deserialize(LineBuilder.getScanner(line));
    }
  }
  
  private static class BasicEntryReader<T> implements EntryReader<T> {
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
    
    public T create(String line) throws InstantiationException, IllegalAccessException, InvocationTargetException  {
      T obj = constructor.newInstance();
      Scanner scanner = LineBuilder.getScanner(line);
      for (int i = 0; i < fields.length; i++) {
        fields[i].set(obj, deserializers[i].deserialize(scanner));
      }
      return obj;
    }
  }
  
  private static class TransientEntryReader<T> implements EntryReader<T> {
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
    
    public T create(String line) throws IllegalAccessException {
      Scanner scanner = LineBuilder.getScanner(line);
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
  public <T extends SimpleSerializable> Iterable<T> readNextToIterable(Class<T> klass) throws IOException {
    return readNextToIterable(klass, false, false);
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
                  } catch (InstantiationException e) {
                    logger.log(Level.SEVERE, "Unable to create object instance", e);
                  } catch (IllegalAccessException e) {
                    logger.log(Level.SEVERE, "Unable to create object instance / set field values", e);
                  } catch (InvocationTargetException e) {
                    logger.log(Level.SEVERE, "Unable to create object instance", e);
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
  public <T extends SimpleSerializable> Iterable<T> readNextToIterable(Class<T> klass, final boolean closeOnCompletion, boolean trans) throws IOException {
    final EntryReader<T> entryReader = positionForNext(klass, trans);
    if (entryReader == null) {
      return Collections.emptyList();
    } else {
      return makeIterable(entryReader, closeOnCompletion);
    }
  }

  @Override
  public <T extends SimpleSerializable> Collection<T> readNextToCollection(Class<T> klass) throws IOException {
    EntryReader<T> entryReader = positionForNext(klass, false);
    if (entryReader != null) {
      Collection<T> coll = Helper.newLinkedList();
      for (String line = br.readLine(); !SimpleSerializerImpl.isFinished(line); line = br.readLine()) {
        try {
          coll.add(entryReader.create(line));
        } catch (InstantiationException e) {
          logger.log(Level.SEVERE, "Unable to create object instance", e);
        } catch (IllegalAccessException e) {
          logger.log(Level.SEVERE, "Unable to create object instance / set field values", e);
        } catch (InvocationTargetException e) {
          logger.log(Level.SEVERE, "Unable to create object instance", e);
        }
      }
      return coll;
    } else {
      return Collections.emptyList();
    }
  }

  @Override
  public <T extends CustomSimpleSerializable> Iterable<T> readNextToIterable(ObjectDeserializer<T> deserializer, boolean closeOnCompletion) throws IOException {
    EntryReader<T> entryReader = positionForNext(deserializer);
    if (entryReader == null) {
      return Collections.emptyList();
    } else {
      return makeIterable(entryReader, closeOnCompletion);
    }
  }

  @Override
  public <T extends CustomSimpleSerializable> Iterable<T> readNextToCollection(ObjectDeserializer<T> deserializer, boolean closeOnCompletion) throws IOException {
    EntryReader<T> entryReader = positionForNext(deserializer);
    if (entryReader != null) {
      Collection<T> coll = Helper.newLinkedList();
      for (String line = br.readLine(); !SimpleSerializerImpl.isFinished(line); line = br.readLine()) {
        try {
          coll.add(entryReader.create(line));
        } catch (InstantiationException e) {
          logger.log(Level.SEVERE, "Unable to create object instance", e);
        } catch (IllegalAccessException e) {
          logger.log(Level.SEVERE, "Unable to create object instance / set field values", e);
        } catch (InvocationTargetException e) {
          logger.log(Level.SEVERE, "Unable to create object instance", e);
        }
      }
      return coll;
    } else {
      return Collections.emptyList();
    }
  }

}
