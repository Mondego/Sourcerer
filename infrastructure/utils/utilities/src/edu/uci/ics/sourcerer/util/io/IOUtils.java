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

import static edu.uci.ics.sourcerer.util.io.logging.Logging.logger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.logging.Level;

import edu.uci.ics.sourcerer.util.io.arguments.Argument;
import edu.uci.ics.sourcerer.util.io.arguments.DualFileArgument;
import edu.uci.ics.sourcerer.util.io.internal.IOUtilFactory;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public final class IOUtils {
  public static OutputStream makeOutputStream(File file) throws IOException {
    return new FileOutputStream(file);
  }
  
  public static LogFileWriter createLogFileWriter(Argument<File> arg) throws IOException {
    return createLogFileWriter(arg.getValue());
  }
  
  public static LogFileWriter createLogFileWriter(File file) throws IOException {
    if (file == null) {
      return LogFileWriter.createNull();
    } else {
      return LogFileWriter.create(makeBufferedWriter(file));
    }
  }
  
  public static BufferedWriter makeBufferedWriter(DualFileArgument arg) throws IOException {
    return new BufferedWriter(new FileWriter(arg.asOutput().getValue()));
  }
  
  public static BufferedWriter makeBufferedWriter(File file) throws IOException {
    return new BufferedWriter(new FileWriter(file));
  }
  
  public static BufferedWriter makeBufferedWriter(File file, boolean append) throws IOException {
    return new BufferedWriter(new FileWriter(file, append));
  }
  
  public static BufferedReader makeBufferedReader(File file) throws IOException {
    return new BufferedReader(new FileReader(file));
  }
  
  public static BufferedReader makeBufferedReader(DualFileArgument arg) throws IOException {
    return new BufferedReader(new FileReader(arg.asInput().getValue()));
  }
  
  public static void close (Closeable ... closeMe) {
    for (Closeable close : closeMe) {
      if (close != null) {
        try {
          close.close();
        } catch (IOException e) {}
      }
    }
  }
  
  public static SimpleSerializer makeSimpleSerializer(DualFileArgument file) throws IOException {
    return makeSimpleSerializer(file.asOutput());
  }
  
  public static SimpleSerializer makeSimpleSerializer(Argument<File> file) throws IOException {
    return IOUtilFactory.makeSimpleSerializer(file);
  }
  
  public static SimpleSerializer makeSimpleSerializer(File file) throws IOException {
    return IOUtilFactory.makeSimpleSerializer(file);
  }
  
  public static SimpleSerializer resumeSimpleSerializer(DualFileArgument file) throws IOException {
    return IOUtilFactory.resumeSimpleSerializer(file.asOutput());
  }
  
  public static SimpleSerializer resumeSimpleSerializer(Argument<File> file) throws IOException {
    return IOUtilFactory.resumeSimpleSerializer(file);
  }
  
  public static SimpleDeserializer makeSimpleDeserializer(DualFileArgument file) throws IOException {
    return IOUtilFactory.makeSimpleDeserializer(file.asInput());
  }
  
  public static SimpleDeserializer makeSimpleDeserializer(Argument<File> file) throws IOException {
    return IOUtilFactory.makeSimpleDeserializer(file);
  }
  
  public static SimpleDeserializer makeSimpleDeserializer(File file) throws IOException {
    return IOUtilFactory.makeSimpleDeserializer(file);
  }
  
  /**
   * If the iterable is never read to completion, the file will remain open.
   */
  public static <T extends SimpleSerializable> Iterable<T> deserialize(Class<T> klass, File file, boolean trans) throws IOException {
    return makeSimpleDeserializer(file).deserializeToIterable(klass, true, trans);
  }
  
  public static <T extends SimpleSerializable> Collection<T> deserialize(Class<T> klass, File file) throws IOException {
    return makeSimpleDeserializer(file).deserializeToCollection(klass);
  }
  
  /**
   * If the iterable is never read to completion, the file will remain open.
   */
  public static <T extends SimpleSerializable> Iterable<T> deserialize(Class<T> klass, DualFileArgument file, boolean trans) throws IOException {
    return makeSimpleDeserializer(file).deserializeToIterable(klass, true, trans);
  }

  public static void writeStreamToStream(InputStream in, OutputStream out) throws IOException {
    byte[] buff = new byte[1024];
    for (int read = in.read(buff); read > 0; read = in.read(buff)) {
      out.write(buff, 0, read);
    }
  }
  
  public static byte[] getInputStreamAsByteArray(InputStream is, int estimated) {
    ByteArrayOutputStream bos = new ByteArrayOutputStream(estimated);
    try {
      byte[] buff = new byte[1024];
      int read = 0;
      while ((read = is.read(buff)) > 0) {
        bos.write(buff, 0, read);
      }
      return bos.toByteArray();
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Error reading from stream", e);
      return null;
    } finally {
      close(is);
    }
  }
}
