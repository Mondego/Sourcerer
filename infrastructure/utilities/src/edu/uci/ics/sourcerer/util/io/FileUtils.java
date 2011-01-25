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
import static edu.uci.ics.sourcerer.util.io.Properties.OUTPUT;
import static edu.uci.ics.sourcerer.util.io.Properties.INPUT;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.Collections;
import java.util.Set;
import java.util.logging.Level;
import java.util.zip.ZipFile;

import edu.uci.ics.sourcerer.util.Helper;
import edu.uci.ics.sourcerer.util.Pair;
import edu.uci.ics.sourcerer.util.io.properties.StringProperty;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public final class FileUtils {
  protected static final Property<String> TEMP_DIR = new StringProperty("temp-dir", "temp", "Name of temp directory placed into OUTPUT directory").register("General");
  
  private FileUtils() {}
  
  public static void close (Closeable closeMe) {
    if (closeMe != null) {
      try {
        closeMe.close();
      } catch (IOException e) {}
    }
  }  
  
  public static void close(ZipFile zipFile) {
    if (zipFile != null) {
      try {
        zipFile.close();
      } catch (IOException e) {}
    }
  }
  
  public static File getTempDir() {
    File tempDir = new File(OUTPUT.getValue(), TEMP_DIR.getValue());
    tempDir = new File(tempDir, "thread-" + Thread.currentThread().getId());
    if (tempDir.exists() || tempDir.mkdirs()) {
      return tempDir;
    } else {
      return null;
    }
  }
  
  public static void resetTempDir() {
    File tempDir = getTempDir();
    for (File file : tempDir.listFiles()) {
      if (file.isDirectory()) {
        deleteDirectory(file);
      } else {
        file.delete();
      }
    }
  }
  
  public static void cleanTempDir() {
    File tempDir = getTempDir();
    deleteDirectory(tempDir);
  }
  
  private static void deleteDirectory(File dir) {
    for (File file : dir.listFiles()) {
      if (file.isDirectory()) {
        deleteDirectory(file);
      } else {
        file.delete();
      }
    }
    dir.delete();
  }
  
  public static String getFileAsString(String path) {
    return getFileAsString(new File(path));
  }
  
  public static String getFileAsString(File file) {
    FileReader reader = null;
    try {
      StringBuilder builder = new StringBuilder();
      reader = new FileReader(file);
      char[] buff = new char[1024];
      for (int read = reader.read(buff); read > 0; read = reader.read(buff)) {
        builder.append(buff, 0, read);
      }
      return builder.toString();
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Unable to read file.", e);
      return null;
    } finally {
      close(reader);
    }
  }
  
  public static byte[] getFileAsByteArray(File file) {
    InputStream is = null;
    try {
      long length = file.length();
      if (length > Integer.MAX_VALUE) {
        logger.log(Level.SEVERE, file.getPath() + " too big to read");
        return null;
      }
      byte[] retval = new byte[(int)length];
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
      close(is);
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
  
  public static byte[] getFileFragmentAsByteArray(File file, int offset, int length) {
    InputStream is = null;
    try {
      byte[] retval = new byte[length];
      is = new FileInputStream(file);
      while (offset > 0) {
        offset -= is.skip(offset);
      }
      for (int read = is.read(retval, offset, retval.length - offset); read > 0; read = is.read(retval, offset, retval.length - offset)) {
        offset += read;
      }
      if (offset < retval.length) {
        logger.log(Level.SEVERE, "Unable to completely read file " + file.getPath());
        return null;
      }
      return retval;
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Unable to read file.", e);
      return null;
    } finally {
      close(is);
    }
  }
  
  public static byte[] getInputStreamFragmentAsByteArray(InputStream is, int offset, int length) {
    try {
      byte[] buff = new byte[length];
      while (offset > 0) {
        offset -= is.skip(offset);
      }
      
      int read = 0;
      while (read < length) {
        read += is.read(buff, read, length - read);
      }
      return buff;
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Error reading from stream", e);
      return null;
    } finally {
      close(is);
    }
  }
  
  public static Set<String> getFileAsSet(File file) {
    BufferedReader br = null;
    try {
      Set<String> set = Helper.newHashSet();
      br = new BufferedReader(new FileReader(file));
      for (String line = br.readLine(); line != null; line = br.readLine()) {
        set.add(line);
      }
      return set;
    } catch (IOException e) {
      return Collections.emptySet();
    } finally {
      close(br);
    }
  }
  
  public static boolean writeStreamToFile(InputStream stream, File file) {
    FileOutputStream os = null;
    try {
      File parent = file.getParentFile();
      if (!parent.exists()) {
        parent.mkdirs();
      }
      os = new FileOutputStream(file);
      byte[] buff = new byte[1024];
      for (int read = stream.read(buff); read > 0; read = stream.read(buff)) {
        os.write(buff, 0, read);
      }
      return true;
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Unable to write stream to " + file.getPath(), e);
      return false;
    } finally {
      close(os);
      close(stream);      
    }
  }
  
  public static boolean copyFile(File source, File destination) {
    try {
      FileInputStream in = new FileInputStream(source);
      return writeStreamToFile(in, destination);
    } catch (IOException e) {
       logger.log(Level.SEVERE, "Unable to copy file from " + source.getPath() + " to " + destination.getPath(), e);
       return false;
    }
  }
  
  public static String convertToRelativePath(String base, String path) {
    path = path.replace('\\', '/');
    base = base.replace('\\', '/');
    if (base == null) {
      return path.replace(' ', '*');
    } else {
      if (path.startsWith(base)) {
        return path.substring(base.length()).replace(' ', '*');
      } else {
        logger.severe("Unable to convert " + path + " to relative path (" + base + ")");
        return path.replace(' ', '*');
      }
    }
  }
  
  public static String stripFileName(String path) {
    int index = path.lastIndexOf('/');
    if (index == -1) {
      return "";
    } else {
      return path.substring(0, index);
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
        FileUtils.close(is);
      }
      return new BigInteger(1, md5.digest()).toString(16);
    } catch (Exception e) {
      logger.log(Level.SEVERE, "Error getting md5 for " + file.getPath(), e);
      return null;
    }
  }
  
  public static Pair<String, String> computeHashes(File file) {
    try {
      MessageDigest md5 = MessageDigest.getInstance("MD5");
      MessageDigest sha = MessageDigest.getInstance("SHA");
      
      byte[] buff = new byte[1024];
      InputStream is = null; 
      try {
        is = new FileInputStream(file);
        int size;
        while ((size = is.read(buff)) != -1) {
          md5.update(buff, 0, size);
          sha.update(buff, 0, size);
        }
      } finally {
        FileUtils.close(is);
      }
      return new Pair<String, String>(new BigInteger(1, md5.digest()).toString(16), new BigInteger(1, sha.digest()).toString(16));
    } catch (Exception e) {
      logger.log(Level.SEVERE, "Error getting md5 for " + file.getPath(), e);
      return null;
    }
  }
  
  public static BufferedWriter getBufferedWriter(String name) throws IOException {
    return new BufferedWriter(new FileWriter(new File(OUTPUT.getValue(), name)));
  }
  
  public static BufferedWriter getBufferedWriter(Property<String> property) throws IOException {
    return new BufferedWriter(new FileWriter(new File(OUTPUT.getValue(), property.getValue())));
  }
  
  public static BufferedReader getBufferedReader(Property<String> property) throws IOException {
    return new BufferedReader(new FileReader(new File(INPUT.getValue(), property.getValue())));
  }
}
