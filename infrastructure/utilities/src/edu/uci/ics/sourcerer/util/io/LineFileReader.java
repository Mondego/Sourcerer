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

import static edu.uci.ics.sourcerer.util.io.Logging.logger;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.logging.Level;

import edu.uci.ics.sourcerer.util.Helper;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public final class LineFileReader implements Closeable {
  private BufferedReader br;
  private FieldConverter[] converters;
  
  protected LineFileReader(BufferedReader br) {
    this.br = br;
  }
  
  @SuppressWarnings("unchecked")
  private <T extends LineWriteable> Constructor<? extends T> positionForNext(Class<T> klass, String ... fields) throws IOException {
    if (br == null) {
      logger.log(Level.SEVERE, "File already empty, unable to read " + klass.getName() + ".");
      return null;
    } else {
      // Verify the class name matches
      try {
        String line = br.readLine();
        if (LineFileWriter.isFinished(line)) {
          return null;
        }
        Class<?> loadedClass = Class.forName(line);
      
        if (!klass.isAssignableFrom(loadedClass)) {
          logger.log(Level.SEVERE, "Specified type does not match file: " + klass.getName() + " vs " + loadedClass.getName());
          close();
          return null;
        }
        
        // LWRecs don't need fields
        if (LWRec.class.isAssignableFrom(loadedClass)) {
          // TODO: make this work
          return null;
        }

        // Read in the fields
        line = br.readLine();
        if (LineFileWriter.isFinished(line)) {
          logger.log(Level.SEVERE, "Unable to read in fields");
          return null;
        }
        String[] fieldNames = LineBuilder.splitLine(line);
        if (converters != null) {
          throw new IllegalStateException("Attempt to read next before previous finished.");
        }
        // Make sure all the fields appear
        if (fields != null && fields.length > 0) {
          List<String> nameList = Helper.newArrayList(Arrays.asList(fields));
          for (int i = 0; i < fieldNames.length; i++) {
            if (!nameList.remove(fieldNames[i])) {
              fieldNames[i] = null;
            }
          }
          if (!nameList.isEmpty()) {
            LineBuilder builder = new LineBuilder();
            for (String name : nameList) {
              builder.addItem(name);
            }
            logger.log(Level.SEVERE, "Unable to find fields " + builder.toLine() + "in " + klass.getName() + ".");
            close();
            return null;
          }
        }
        converters = new FieldConverter[fieldNames.length];
        for (int i = 0; i < fieldNames.length; i++) {
          if (fieldNames[i] == null) {
            converters[i] = FieldConverter.getFieldConverter(null);
          } else {
            converters[i] = FieldConverter.getFieldConverter(loadedClass.getDeclaredField(fieldNames[i]));
          }
        }
        Constructor<? extends T> constructor = (Constructor<? extends T>) loadedClass.getDeclaredConstructor();
        constructor.setAccessible(true);
        return constructor;
      } catch (ClassNotFoundException e) {
        logger.log(Level.SEVERE, "Unable to load class", e);
        close();
        return null;
      } catch (NoSuchFieldException e) {
        logger.log(Level.SEVERE, "The current fields of this class (" + klass.getName() + ") do not match the file.", e);
        close();
        return null;
      } catch (NoSuchMethodException e) {
        logger.log(Level.SEVERE, "Unable to load default constructor", e);
        close();
        return null;
      }
    }
  }
  
  public <T extends LineWriteable> Iterable<T> readNextToIterable(final Class<T> klass, String ... fields) throws IOException {
    return readNextToIterable(klass, false, fields);
  }
    
  public <T extends LineWriteable> Iterable<T> readNextToIterable(Class<T> klass, final boolean closeOnCompletion, String ... fields) throws IOException {
    final Constructor<? extends T> constructor = positionForNext(klass, fields);
    if (constructor == null) {
      return Collections.emptyList();
    }
    
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
                if (LineFileWriter.isFinished(line)) {
                  open = false;
                } else {
                  String[] values = LineBuilder.splitLine(line);
                  if (values.length == converters.length) {
                    try {
                      T obj = constructor.newInstance();
                      for (int i = 0; i < values.length; i++) {
                        converters[i].set(obj, values[i]);
                      }
                      next = obj;
                    } catch (InstantiationException e) {
                      logger.log(Level.SEVERE, "Unable to create object instance", e);
                    } catch (IllegalAccessException e) {
                      logger.log(Level.SEVERE, "Unable to create object instance / set field values", e);
                    } catch (InvocationTargetException e) {
                      logger.log(Level.SEVERE, "Unable to create object instance", e);
                    }
                  } else {
                    logger.log(Level.SEVERE, "Invalid line: " + line);
                  }
                }
              }
            } catch (IOException e) {
              logger.log(Level.SEVERE, "Error reading line", e);
              open = false;
              close();
            }
            if (next == null) {
              converters = null;
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
          }
        };
      }
    };
  }
  
  public <T extends LineWriteable> Collection<T> readNextToCollection(Class<T> klass, String ... fields) throws IOException {
    Constructor<? extends T> constructor = positionForNext(klass, fields);
    if (constructor != null) {
      Collection<T> coll = Helper.newLinkedList();
      for (String line = br.readLine(); !LineFileWriter.isFinished(line); line = br.readLine()) {
        String[] values = LineBuilder.splitLine(line);
        if (values.length == converters.length) {
          try {
            T obj = constructor.newInstance();
            for (int i = 0; i < values.length; i++) {
              converters[i].set(obj, values[i]);
            }
            coll.add(obj);
          } catch (InstantiationException e) {
            logger.log(Level.SEVERE, "Unable to create object instance", e);
          } catch (IllegalAccessException e) {
            logger.log(Level.SEVERE, "Unable to create object instance / set field values", e);
          } catch (InvocationTargetException e) {
            logger.log(Level.SEVERE, "Unable to create object instance", e);
          }
        } else {
          logger.log(Level.SEVERE, "Line has incorrect number of entries: " + line);
        }
      }
      converters = null;
      return coll;
    } else {
      return Collections.emptyList();
    }
  }
  
  public int readNextToInt() throws IOException {
    return Integer.parseInt(br.readLine());
  }
  
  public void close() {
    FileUtils.close(br);
    br = null;
  }
}
