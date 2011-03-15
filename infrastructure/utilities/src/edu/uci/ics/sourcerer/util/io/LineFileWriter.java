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

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import java.util.logging.Level;

import edu.uci.ics.sourcerer.util.Helper;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class LineFileWriter implements Closeable {
  private BufferedWriter bw;
  private static final String DIVIDER = "!_DONE_!";
  
  protected LineFileWriter(BufferedWriter bw) {
    this.bw = bw;
  }
  
  public static boolean isFinished(String line) {
    return DIVIDER.equals(line) || line == null;
  }
  
  private <T extends LineWriteable> Collection<FieldConverter> prepareStream(Class<T> klass) throws IOException {
    // Write the class name
    bw.write(klass.getName());
    bw.newLine();
    
    // Check if it's a LWRec
    Collection<FieldConverter> fields = null;
    if (LWRec.class.isAssignableFrom(klass)) {
      fields = Collections.singleton(FieldConverter.getLWRecConverter());
    } else {
      // Write the fields
      LineBuilder builder = new LineBuilder();
      Field[] allFields = klass.getDeclaredFields();
      fields = Helper.newArrayList(allFields.length);
      for (Field field : allFields) {
        if (field.getAnnotation(LWField.class) != null) {
          builder.addItem(field.getName());
          fields.add(FieldConverter.getFieldConverter(field));
        }
      }
      bw.write(builder.toLine());
      bw.newLine();
    }
    return fields;
  }
  
  public <T extends LineWriteable> void write(Iterable<T> iterable) throws IOException {
    try {
      Collection<FieldConverter> fields = null;
      LineBuilder builder = new LineBuilder();
      for (T write : iterable) {
        if (fields == null) {
          fields = prepareStream(write.getClass());
        }
        for (FieldConverter field : fields) {
          builder.addItem(field.get(write));
        }
        bw.write(builder.toLine());
        bw.newLine();
      }
      bw.write(DIVIDER);
      bw.newLine();
    } catch (IllegalAccessException e) {
      logger.log(Level.SEVERE, "JVM does not have sufficient security access.", e);
    }
  }
  
  public <T extends LineWriteable> EntryWriter<T> getEntryWriter(Class<T> klass) throws IOException {
    return new EntryWriter<T>(prepareStream(klass));
  }
  
  public class EntryWriter <T extends LineWriteable> implements Closeable {
    private Collection<FieldConverter> fields = null;
    private LineBuilder builder = new LineBuilder();
    private EntryWriter(Collection<FieldConverter> fields) {
      this.fields = fields;
    }
    
    public void write(T write) throws IOException {
      try {
      for (FieldConverter field : fields) {
          builder.addItem(field.get(write));
      }
      bw.write(builder.toLine());
      bw.newLine();
      } catch (IllegalAccessException e) {
        logger.log(Level.SEVERE, "JVM does not have sufficient security access.", e);
      }
    }
    
    public void close() throws IOException {
      bw.write(DIVIDER);
      bw.newLine();
    }
  }
  
  public void write(int val) throws IOException {
    bw.write(Integer.toString(val));
    bw.newLine();
  }
  
  public void close() {
    FileUtils.close(bw);
  }
}
