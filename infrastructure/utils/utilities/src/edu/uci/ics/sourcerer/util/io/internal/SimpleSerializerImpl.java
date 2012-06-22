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
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Map;

import edu.uci.ics.sourcerer.util.Pair;
import edu.uci.ics.sourcerer.util.io.CustomSerializable;
import edu.uci.ics.sourcerer.util.io.EntryWriter;
import edu.uci.ics.sourcerer.util.io.FileUtils;
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
      for (String line : FileUtils.getFileAsIterable(file)) {
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
  
  private Pair<ObjectSerializer, ObjectSerializer> prepareStream(Class<?> map, Class<?> key, Class<?> value) throws IOException {
    // Close the old writer
    IOUtils.close(writer);
    
    // Verify the klass matches what's being resumed
    if (expectedKlass != null) {
      throw new IllegalStateException("May not resume writing a map.");
    } else {
      // Write the class name
      bw.write(map.getName());
      bw.newLine();
    }
    
    
    // Make the serializer for the key
    ObjectSerializer keySerializer = null;
    // Check if it overrides the default behavior
    if (CustomSerializable.class.isAssignableFrom(key) || !SimpleSerializable.class.isAssignableFrom(key)) {
      bw.write("serialize");
      keySerializer = ObjectSerializer.makeSerializer();
    } else {
      LineBuilder builder = new LineBuilder();
      Field[] allFields = key.getDeclaredFields();
      ArrayList<Field> fields = new ArrayList<>();
      for (Field field : allFields) {
        if (field.getAnnotation(Ignore.class) == null && !Modifier.isStatic(field.getModifiers())) {
          builder.append(field.getName());
          fields.add(field);
        }
      }
      bw.write(fields.size() + " ");
      bw.write(builder.toString());
      keySerializer = ObjectSerializer.makeSerializer(fields.toArray(new Field[fields.size()]));
    }

    // Make the serializer for the value
    ObjectSerializer valueSerializer = null;
    // Check if it overrides the default behavior
    if (CustomSerializable.class.isAssignableFrom(value)  || !SimpleSerializable.class.isAssignableFrom(value)) {
      bw.write(" serialize");
      bw.newLine();
      valueSerializer = ObjectSerializer.makeSerializer();
    } else {
      LineBuilder builder = new LineBuilder();
      Field[] allFields = value.getDeclaredFields();
      ArrayList<Field> fields = new ArrayList<>();
      for (Field field : allFields) {
        if (field.getAnnotation(Ignore.class) == null && !Modifier.isStatic(field.getModifiers())) {
          builder.append(field.getName());
          fields.add(field);
        }
      }
      bw.write(" " + fields.size() + " ");
      bw.write(builder.toString());
      bw.newLine();
      valueSerializer = ObjectSerializer.makeSerializer(fields.toArray(new Field[fields.size()]));
    }
    
    return new Pair<ObjectSerializer, ObjectSerializer>(keySerializer, valueSerializer);
  }
  
  private <T extends SimpleSerializable> ObjectSerializer prepareStream(Class<T> klass) throws IOException {
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
    if (CustomSerializable.class.isAssignableFrom(klass)) {
      if (expectedKlass != null) {
        if (!"serialize".equals(expectedFields)) {
          throw new IllegalStateException("Serializer was resumed at class " + expectedKlass + " with fields " + expectedFields + ", but expecting serialize.");
        }
      } else {
        bw.write("serialize");
        bw.newLine();
      }
      return ObjectSerializer.makeSerializer();
    } else {
      if (expectedKlass == null) {
        LineBuilder builder = new LineBuilder();
        Field[] allFields = klass.getDeclaredFields();
        ArrayList<Field> fields = new ArrayList<>();
        for (Field field : allFields) {
          if (field.getAnnotation(Ignore.class) == null && !Modifier.isStatic(field.getModifiers())) {
            builder.append(field.getName());
            fields.add(field);
          }
        }
        bw.write(builder.toString());
        bw.newLine();
        return ObjectSerializer.makeSerializer(fields.toArray(new Field[fields.size()]));
      } else {
        String[] parts = LineBuilder.splitLine(expectedFields);
        Field[] fields = new Field[parts.length];
        for (int i = 0; i < parts.length; i++) {
          try {
            fields[i] = klass.getDeclaredField(parts[i]);
          } catch (NoSuchFieldException e) {
            throw new IllegalStateException("Field " + parts[i] + " is missing for class " + expectedKlass);
          }
        }
        return ObjectSerializer.makeSerializer(fields);
      }
    }
  }
  
  @Override
  public <K, V> void serialize(Map<K, V> map) throws IOException {
    Pair<ObjectSerializer, ObjectSerializer> serializers = null;
    LineBuilder builder = new LineBuilder();
    for (Map.Entry<K, V> entry : map.entrySet()) {
      if (serializers == null) {
        serializers = prepareStream(map.getClass(), entry.getKey().getClass(), entry.getValue().getClass());
      }
      builder.append(serializers.getFirst().serialize(entry.getKey()));
      builder.append(serializers.getSecond().serialize(entry.getValue()));
      bw.write(builder.toString());
      builder.reset();
      bw.newLine();
    }
    bw.write(DIVIDER);
    bw.newLine();
    bw.flush();
  }
  
  @Override
  public <T extends SimpleSerializable> void serialize(Iterable<T> iterable) throws IOException {
    ObjectSerializer serializer = null;
    for (T write : iterable) {
      if (serializer == null) {
        serializer = prepareStream(write.getClass());
      }
      bw.write(serializer.serialize(write));
      bw.newLine();
    }
    bw.write(DIVIDER);
    bw.newLine();
    bw.flush();
  }

  @Override
  public <T extends SimpleSerializable> EntryWriter<T> getEntryWriter(final Class<T> klass) throws IOException {
    return new EntryWriter<T>() {
      private boolean closed = false;
      private ObjectSerializer serializer = prepareStream(klass);
          
      @Override
      public void write(T write) throws IOException {
        if (closed) {
          throw new IllegalStateException("Cannot write to a closed EntryWriter.");
        }
        bw.write(serializer.serialize(write));
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
