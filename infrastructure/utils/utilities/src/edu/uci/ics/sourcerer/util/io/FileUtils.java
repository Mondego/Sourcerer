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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Deque;
import java.util.logging.Level;

import edu.uci.ics.sourcerer.util.Helper;
import edu.uci.ics.sourcerer.util.Pair;
import edu.uci.ics.sourcerer.util.io.arguments.Arguments;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class FileUtils {
  public static boolean delete(File dir) {
    boolean success = true;
    for (File file : dir.listFiles()) {
      if (file.isDirectory()) {
        success &= delete(file);
      } else {
        success &= file.delete();
      }
    }
    success &= dir.delete();
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
}
