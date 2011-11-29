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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.logging.Level;

import edu.uci.ics.sourcerer.util.Helper;
import edu.uci.ics.sourcerer.util.Pair;
import edu.uci.ics.sourcerer.util.io.arguments.Argument;
import edu.uci.ics.sourcerer.util.io.arguments.Arguments;
import edu.uci.ics.sourcerer.util.io.arguments.RelativeFileArgument;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class FileUtils {
  public static final Argument<File> TEMP_DIR = new RelativeFileArgument("temp-dir", "temp", Arguments.OUTPUT, "Name of temp directory placed into OUTPUT directory");
  
  public static File ensureWriteable(File file) {
    File parentFile = file.getParentFile();
    if (!parentFile.exists()) {
      parentFile.mkdirs();
    }
    return file;
  }
  
  public static boolean delete(File dir) {
    boolean success = true;
    if (dir.exists()) {
      if (dir.isFile()) {
        success &= dir.delete();
      } else {
        for (File file : dir.listFiles()) {
          if (file.isDirectory()) {
            success &= delete(file);
          } else {
            success &= file.delete();
          }
        }
        success &= dir.delete();
      }
    }
    return success;
  }
  
  public static boolean moveFile(File source, File destination) {
    if (source.isDirectory()) {
      if (destination.exists()) {
        boolean result = true;
        Deque<Pair<File, File>> stack = Helper.newStack();
        stack.push(new Pair<File, File>(source, destination));
        while (!stack.isEmpty()) {
          Pair<File, File> pair = stack.pop();
          for (File file : pair.getFirst().listFiles()) {
            if (file.isDirectory()) {
              File target = new File(pair.getSecond(), file.getName());
              if (target.exists()) {
                stack.push(new Pair<File, File>(file, target));
              } else {
                pair.getSecond().mkdirs();
                result &= file.renameTo(target);
              }
            } else {
              result &= moveFileHelper(file, new File(pair.getSecond(), file.getName()));
            }
          }
        }
        delete(source);
        return result;
      } else {
        destination.getParentFile().mkdirs();
        return source.renameTo(destination);
      }
    } else {
      return moveFileHelper(source, destination);
    }
  }
  
  private static boolean moveFileHelper(File source, File destination) {
    if (destination.exists()) {
      if (destination.delete()) {
        return source.renameTo(destination);
      } else {
        return false;
      }
    } else {
      destination.getParentFile().mkdirs();
      return source.renameTo(destination);
    }
  }
  
  public static boolean copyFile(File source, File destination) {
    if (source.isDirectory()) {
      boolean result = true;
      Deque<Pair<File, File>> stack = Helper.newStack();
      stack.push(new Pair<File, File>(source, destination));
      while (!stack.isEmpty()) {
        Pair<File, File> pair = stack.pop();
        for (File file : pair.getFirst().listFiles()) {
          if (file.isDirectory()) {
            stack.push(new Pair<File, File>(file, new File(pair.getSecond(), file.getName())));
          } else {
            result &= copyFileHelper(file, new File(pair.getSecond(), file.getName()));
          }
        }
      }
      return result;
    } else {
      return copyFileHelper(source, destination);
    }
  }

  private static boolean copyFileHelper(File source, File destination) {
    FileInputStream in = null;
    FileOutputStream out = null;
    try {
      in = new FileInputStream(source);
      destination.getParentFile().mkdirs();
      out = new FileOutputStream(destination);
      IOUtils.writeStreamToStream(in, out);
      return true;
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Unable to copy file from " + source.getPath() + " to " + destination.getPath(), e);
      return false;
    } finally {
      IOUtils.close(in);
      IOUtils.close(out);
    }
  }
  
  public static File makeTempDir() {
    File tempDir = new File(Arguments.OUTPUT.getValue(), "tmp/thread-" + Thread.currentThread().getId());
    if (tempDir.exists() || tempDir.mkdirs())  {
      String name = Long.toString(System.currentTimeMillis());
      char c = 'a';
      do {
        tempDir = new File(tempDir, name + c);
        c++;
      } while (tempDir.exists());
      if (tempDir.mkdir()) {
        return tempDir;
      } else {
        logger.log(Level.SEVERE, "Unable to make temp dir: " + tempDir.getPath());
        return null;
      }
    } else {
      logger.log(Level.SEVERE, "Unable to make temp dir: " + tempDir.getPath());
      return null;
    }
  }
  
  public static String computeHash(File file) {
    try {
      MessageDigest md5 = MessageDigest.getInstance("MD5");

      byte[] buff = new byte[1024];
      InputStream is = null;
      try {
        is = new FileInputStream(file);
        int size;
        while ((size = is.read(buff)) != -1) {
          md5.update(buff, 0, size);
        }
      } finally {
        IOUtils.close(is);
      }
      return new BigInteger(1, md5.digest()).toString(16);
    } catch (Exception e) {
      logger.log(Level.SEVERE, "Error getting md5 for " + file.getPath(), e);
      return null;
    }
  }
  
  public static File getTempDir() {
    File tempDir = TEMP_DIR.getValue();
    tempDir = new File(tempDir, "thread-" + Thread.currentThread().getId());
    if (tempDir.exists() || tempDir.mkdirs()) {
      return tempDir;
    } else {
      return null;
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
            final BufferedReader br = IOUtils.makeBufferedReader(file);
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
                    IOUtils.close(reader);
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
  
  public static byte[] getFileAsByteArray(File file) {
    if (file == null) {
      return null;
    }
    InputStream is = null;
    try {
      long length = file.length();
      if (length > Integer.MAX_VALUE) {
        logger.log(Level.SEVERE, file.getPath() + " too big to read");
        return null;
      }
      byte[] retval = new byte[(int) length];
      is = new FileInputStream(file);
      int off = 0;
      for (int read = is.read(retval, off, retval.length - off); read > 0; read = is.read(retval, off, retval.length - off)) {
        off += read;
      }
      if (off < retval.length) {
        logger.log(Level.SEVERE, "Unable to completely read file " + file.getPath());
        return null;
      }
      return retval;
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Unable to read file.", e);
      return null;
    } finally {
      IOUtils.close(is);
    }
  }
}
