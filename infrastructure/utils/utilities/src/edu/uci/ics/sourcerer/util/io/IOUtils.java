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
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.logging.Level;

import edu.uci.ics.sourcerer.util.io.arguments.Argument;
import edu.uci.ics.sourcerer.util.io.arguments.DualFileArgument;
import edu.uci.ics.sourcerer.util.io.internal.IOUtilFactory;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public final class IOUtils {
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
  
  public static void close (Closeable ... closeMe) {
    for (Closeable close : closeMe) {
      if (close != null) {
        try {
          close.close();
        } catch (IOException e) {}
      }
    }
  }
  
  /**
   * File remains open until iterator is exhausted.
   */
  public static Iterable<String> getFileAsIterable(final File file) {
    return new Iterable<String>() {
      @Override
      public Iterator<String> iterator() {
        if (file.exists()) {
          try {
            final BufferedReader br = makeBufferedReader(file);
            return new Iterator<String>() {
              BufferedReader reader = br;
              String nextLine = null;
              
              @Override
              public boolean hasNext() {
                if (nextLine == null && reader != null) {
                  try {
                    nextLine = reader.readLine();
                  } catch (IOException e) {
                    logger.log(Level.SEVERE, "Error trying to read: " + file.getPath(), e);
                    nextLine = null;
                  }
                  if (nextLine == null) {
                    close(reader);
                    reader = null;
                  }
                }
                return nextLine != null;
              }
              
              @Override
              public String next() {
                if (hasNext()) {
                  String next = nextLine;
                  nextLine = null;
                  return next;
                } else {
                  throw new NoSuchElementException();
                }
              }
              
              @Override
              public void remove() {
                throw new UnsupportedOperationException();
              }
            };
          } catch (IOException e) {
            logger.log(Level.SEVERE, "Unable to read file: " + file.getPath(), e);
            return Collections.<String>emptyList().iterator();
          }
        } else {
          return Collections.<String>emptyList().iterator();
        }
      }
    };
  }
  
  public static SimpleSerializer makeSimpleSerializer(DualFileArgument file) throws IOException {
    return makeSimpleSerializer(file.asOutput());
  }
  
  public static SimpleSerializer makeSimpleSerializer(Argument<File> file) throws IOException {
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
}
