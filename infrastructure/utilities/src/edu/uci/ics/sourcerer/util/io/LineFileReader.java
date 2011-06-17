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
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.logging.Level;

import edu.uci.ics.sourcerer.util.Helper;
import edu.uci.ics.sourcerer.util.io.FieldConverter.FieldConverterHelper;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public final class LineFileReader implements Closeable {
  private BufferedReader br;
  
  protected LineFileReader(BufferedReader br) {
    this.br = br;
  }
  
  @SuppressWarnings("unchecked")
  private <T extends LineWriteable> EntryReader<T> positionForNext(Class<T> klass, String ... fields) throws IOException {
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
          return new LWRecEntryReader<T>(FieldConverter.getHelper(loadedClass));
        }

        // Read in the fields
        line = br.readLine();
        if (LineFileWriter.isFinished(line)) {
          logger.log(Level.SEVERE, "Unable to read in fields");
          return null;
        }
        String[] fieldNames = LineBuilder.splitLine(line);
        boolean[] trimmedFieldNames = new boolean[fieldNames.length];
        for (int i = 0; i < trimmedFieldNames.length; i++) {
          trimmedFieldNames[i] = true;
        }
        
        // Make sure all the fields appear
        if (fields != null && fields.length > 0) {
          List<String> nameList = Helper.newArrayList(Arrays.asList(fields));
          for (int i = 0; i < fieldNames.length; i++) {
            trimmedFieldNames[i] = nameList.remove(fieldNames[i]);
          }
          if (!nameList.isEmpty()) {
            LineBuilder builder = new LineBuilder();
            for (String name : nameList) {
              builder.append(name);
            }
            logger.log(Level.SEVERE, "Unable to find fields " + builder.toString() + "in " + klass.getName() + ".");
            close();
            return null;
          }
        }
        FieldConverter[] converters = new FieldConverter[fieldNames.length];
        for (int i = 0; i < fieldNames.length; i++) {
          Field field = loadedClass.getDeclaredField(fieldNames[i]);
          if (trimmedFieldNames[i]) {
            converters[i] = FieldConverter.getFieldReadConverter(field);
          } else {
            converters[i] = FieldConverter.getNullReadConverter(field);
          }
        }
        Constructor<? extends T> constructor = (Constructor<? extends T>) loadedClass.getDeclaredConstructor();
        return new BasicEntryReader<T>(constructor, converters);
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
    final EntryReader<T> entryReader = positionForNext(klass, fields);
    if (entryReader == null) {
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
          }
        };
      }
    };
  }
  
  public <T extends LineWriteable> Collection<T> readNextToCollection(Class<T> klass, String ... fields) throws IOException {
    EntryReader<T> entryReader = positionForNext(klass, fields);
    if (entryReader != null) {
      Collection<T> coll = Helper.newLinkedList();
      for (String line = br.readLine(); !LineFileWriter.isFinished(line); line = br.readLine()) {
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
  
  public int readNextToInt() throws IOException {
    return Integer.parseInt(br.readLine());
  }
  
  public void close() {
    FileUtils.close(br);
    br = null;
  }
  
  private static abstract class EntryReader <T extends LineWriteable> {
    public abstract T create(String line) throws InvocationTargetException, InstantiationException, IllegalAccessException ;
  }
  
  private static class BasicEntryReader <T extends LineWriteable> extends EntryReader<T> {
    private Constructor<? extends T> constructor;
    private FieldConverter[] converters;
    
    public BasicEntryReader(Constructor<? extends T> constructor, FieldConverter[] converters) {
      this.constructor = constructor; 
      constructor.setAccessible(true);
      this.converters = converters;
    }
    
    public T create(String line) throws InvocationTargetException, InstantiationException, IllegalAccessException {
      T obj = constructor.newInstance();
      Scanner scanner = LineBuilder.getScanner(line);
      for (FieldConverter converter : converters) {
        converter.set(obj, scanner);
      }
      return obj;
    }
  }
  
  private static class LWRecEntryReader <T extends LineWriteable> extends EntryReader<T> {
    private FieldConverterHelper converter;
    
    public LWRecEntryReader(FieldConverterHelper converter) {
      this.converter = converter;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public T create(String line) throws InvocationTargetException, InstantiationException, IllegalAccessException {
      Scanner scanner = LineBuilder.getScanner(line);
      return (T)converter.makeFromScanner(scanner);
    }
    
  }
}
