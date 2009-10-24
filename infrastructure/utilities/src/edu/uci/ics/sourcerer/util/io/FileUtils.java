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

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.zip.ZipFile;

import edu.uci.ics.sourcerer.util.io.properties.StringProperty;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public final class FileUtils {
  protected static final Property<String> TEMP_DIR = new StringProperty("temp-dir", "temp", "General", "Name of temp directory placed into OUTPUT directory");
  
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
    try {
      StringBuilder builder = new StringBuilder();
      FileReader reader = new FileReader(path);
      char[] buff = new char[1024];
      for (int read = reader.read(buff); read > 0; read = reader.read(buff)) {
        builder.append(buff, 0, read);
      }
      return builder.toString();
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Unable to read file.", e);
      return null;
    }
  }
  
  public static boolean writeStreamToFile(InputStream stream, File file) {
    FileOutputStream os = null;
    try {
      os = new FileOutputStream(file);
      byte[] buff = new byte[1024];
      for (int read = stream.read(buff); read > 0; read = stream.read(buff)) {
        os.write(buff, 0, read);
      }
      return true;
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Unable to write stream to file.", e);
      return false;
    } finally {
      if (os != null) {
        try {
          os.close();
        } catch (IOException e) {}
      }
      try {
        stream.close();
      } catch (IOException e) {}
      
    }
  }
  
  public static boolean copyFile(File source, File destination) {
    try {
      FileInputStream in = new FileInputStream(source);
      return writeStreamToFile(in, destination);
    } catch (IOException e) {
       logger.log(Level.SEVERE, "Unable to copy file from " + source.getPath() + " to " + destination.getPath());
       return false;
    }
  }
}
