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

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;

import edu.uci.ics.sourcerer.util.Helper;
import edu.uci.ics.sourcerer.util.io.CustomSimpleSerializable;
import edu.uci.ics.sourcerer.util.io.EntryWriter;
import edu.uci.ics.sourcerer.util.io.IOUtils;
import edu.uci.ics.sourcerer.util.io.Ignore;
import edu.uci.ics.sourcerer.util.io.LineBuilder;
import edu.uci.ics.sourcerer.util.io.SimpleSerializable;
import edu.uci.ics.sourcerer.util.io.SimpleSerializer;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
final class SimpleSerializerImpl implements SimpleSerializer {
  private final BufferedWriter bw;
  public static final String DIVIDER = "!_DONE_!";
  
  private String expectedKlass;
  private String expectedFields;
  private EntryWriter<?> writer;
  
  private SimpleSerializerImpl(BufferedWriter bw) {
    this.bw = bw;
  }
  
  private SimpleSerializerImpl(BufferedWriter bw, String expectedKlass, String expectedFields) {
    this.bw = bw;
    this.expectedKlass = expectedKlass;
    this.expectedFields = expectedFields;
  }
  
  protected static SimpleSerializerImpl make(File file) throws IOException {
    return new SimpleSerializerImpl(IOUtils.makeBufferedWriter(file));
  }
  
  protected static SimpleSerializerImpl resume(File file) throws IOException {
    if (file.exists()) {
      String klass = null;
      String fields = null;
      for (String line : IOUtils.getFileAsIterable(file)) {
        if (DIVIDER.equals(line)) {
          klass = null;
          fields = null;
        } else if (klass == null) {
          klass = line;
        } else if (fields == null) {
          fields = line;
        }
      }
      if (klass == null) {
        return new SimpleSerializerImpl(IOUtils.makeBufferedWriter(file, true));
      } else {
        return new SimpleSerializerImpl(IOUtils.makeBufferedWriter(file, true), klass, fields);
      }
    } else {
      return make(file);
    }
  }
  
  public static boolean isFinished(String line) {
    return DIVIDER.equals(line) || line == null;
  }
  
  private <T extends SimpleSerializable> Collection<ObjectSerializer> prepareStream(Class<T> klass) throws IOException {
    // Close the old writer
    IOUtils.close(writer);
    
    // Verify the klass matches what's being resumed
    if (expectedKlass != null) {
      if (!expectedKlass.equals(klass.getName())) {
        throw new IllegalStateException("Serializer was resumed at class " + expectedKlass + ", but writed requested for class " + klass.getName());
      }
    } else {
      // Write the class name
      bw.write(klass.getName());
      bw.newLine();
    }
    
    // Check if it overrides the default behavior
    if (CustomSimpleSerializable.class.isAssignableFrom(klass)) {
      if (expectedKlass != null) {
        if (!"writeToString".equals(expectedFields)) {
          throw new IllegalStateException("Serializer was resumed at class " + expectedKlass + " with fields " + expectedFields + ", but expecting writeToString.");
        }
      } else {
        bw.write("writeToString");
        bw.newLine();
      }
      return Collections.singleton(ObjectSerializer.makeCustomSerializer());
    }
    
    Collection<ObjectSerializer> converters = Helper.newLinkedList();
    if (expectedKlass == null) {
      LineBuilder builder = new LineBuilder();
      Field[] allFields = klass.getDeclaredFields();
      for (Field field : allFields) {
        if (field.getAnnotation(Ignore.class) == null) {
          builder.append(field.getName());
          converters.add(ObjectSerializer.makeBasicSerializer(field));
        }
      }
      bw.write(builder.toString());
      bw.newLine();
    } else {
      for (String field : LineBuilder.splitLine(expectedFields)) {
        try {
          Field f = klass.getDeclaredField(field);
          converters.add(ObjectSerializer.makeBasicSerializer(f));
        } catch (NoSuchFieldException e) {
          throw new IllegalStateException("Field " + field + " is missing for class " + expectedKlass);
        }
      }
    }

    return converters;
  }
  
  @Override
  public <T extends SimpleSerializable> void write(Iterable<T> iterable) throws IOException {
    Collection<ObjectSerializer> serializers = null;
    LineBuilder builder = new LineBuilder();
    for (T write : iterable) {
      if (serializers == null) {
        serializers = prepareStream(write.getClass());
      }
      for (ObjectSerializer serializer : serializers) {
        builder.append(serializer.serialize(write));
      }
      bw.write(builder.toString());
      bw.newLine();
    }
    bw.write(DIVIDER);
    bw.newLine();
  }

  @Override
  public <T extends SimpleSerializable> EntryWriter<T> getEntryWriter(final Class<T> klass) throws IOException {
    return new EntryWriter<T>() {
      private boolean closed = false;
      private Collection<ObjectSerializer> serializers = prepareStream(klass);
      private LineBuilder builder = new LineBuilder();
          
      @Override
      public void write(T write) throws IOException {
        if (closed) {
          throw new IllegalStateException("Cannot write to a closed EntryWriter.");
        }
        for (ObjectSerializer serializer : serializers) {
          builder.append(serializer.serialize(write));
        }
        bw.write(builder.toString());
        bw.newLine();
      }
      
      @Override
      public void close() throws IOException {
        if (!closed) {
          try {
            bw.write(DIVIDER);
            bw.newLine();
            bw.flush();
          } finally {
            closed = true;
          }
        }
      }
      
      @Override
      public void flush() throws IOException {
        bw.flush();
      }
    };
  }
    
//  public void write(int val) throws IOException {
//    bw.write(Integer.toString(val));
//    bw.newLine();
//  }
  
  @Override
  public void close() {
    IOUtils.close(bw);
  }
}
