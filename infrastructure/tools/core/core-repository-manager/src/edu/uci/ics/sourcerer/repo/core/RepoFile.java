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
package edu.uci.ics.sourcerer.repo.core;

import static edu.uci.ics.sourcerer.util.io.Logging.logger;

import java.io.File;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Level;

import edu.uci.ics.sourcerer.util.Helper;
import edu.uci.ics.sourcerer.util.io.FieldConverter;
import edu.uci.ics.sourcerer.util.io.FileUtils;
import edu.uci.ics.sourcerer.util.io.LWRec;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class RepoFile implements LWRec {
  private final RepoFile root;
  private final RelativePath relativePath;
  private final File file;
  
  private static Map<File, RepoFile> rootMap = Helper.newHashMap();

  private RepoFile(File file) {
    root = this;
    relativePath = RelativePath.make("");
    this.file = file;
  }
  
  private RepoFile(RepoFile root, RelativePath relativePath) {
    this.root = root;
    this.relativePath = relativePath;
    this.file = new File(root.file, relativePath.toString());
  }
  
  public static RepoFile makeRoot(File root) {
    RepoFile file = rootMap.get(root);
    if (file == null) {
      file = new RepoFile(root);
      rootMap.put(root, file);
    }
    return file;
  }

  public RepoFile getRoot() {
    return root;
  }
  
  public RelativePath getRelativePath() {
    return relativePath;
  }
  
  public RepoFile asRoot() {
    return makeRoot(file);
  }
  
  public boolean isDirectory() {
    return file.isDirectory();
  }
  
  public boolean exists() {
    if (file.exists()) {
      if (file.isDirectory()) {
        // make sure it's not empty
        if (file.list().length == 0) {
          file.delete();
          return false;
        } else {
          return true;
        }
      } else {
        return true;
      }
    } else {
      return false;
    }
  }
  
  /**
   * Creates the parent directories, if needed.
   */
  public File toFile() {
    if (!file.exists()) {
      File parent = file.getParentFile();
      if (!parent.exists()) {
        parent.mkdirs();
      }
    }
  
    return file;
  }
  
  /**
   * Creates the directory, if needed.
   */
  public File toDir() {
    if (!file.exists()) {
      file.mkdirs();
    }
    
    return file;
  }
    
  public File getChildFile(String child) {
    if (file.isFile()) {
      throw new IllegalStateException("Cannot get a child of a file: " + file.getPath() + " " + child);
    } else {
      return new File(toDir(), child);
    }
  }
  
  public RepoFile getChild(String child) {
    if (file.isFile()) {
      throw new IllegalStateException("Cannot get a child of a file: " + file.getPath() + " " + child);
    } else {
      return new RepoFile(root, relativePath.append(child));
    }
  }
  
  public RepoFile getChild(RelativePath relativePath) {
    if (file.isFile()) {
      throw new IllegalStateException("Cannot get a child of a file: " + file.getPath() + " " + relativePath);
    } else {
      return new RepoFile(root, this.relativePath.append(relativePath));
    }
  }
  
  public String getName() {
    return file.getName();
  }
  
  @Override
  public String toString() {
    return relativePath.toString();
  }
  
  /* LWRec Related Methods */
  
  public static void registerConverterHelper(RepoFile repoRoot) {
    FieldConverter.registerConverterHelper(RepoFile.class, new FieldConverter.FieldConverterHelper() {
      @Override
      protected Object makeFromScanner(Scanner scanner) throws IllegalAccessException {
        String value = scanner.next();
        int colon = value.indexOf(';');
        if (colon == -1) {
          logger.log(Level.SEVERE, "Invalid value: " + value);
          return null;
        } else {
          File rootFile = FileUtils.fromWriteableString(value.substring(0, colon));
          RepoFile root = makeRoot(rootFile);
          RelativePath relativePath = RelativePath.makeFromWriteable(value.substring(colon + 1));
          return new RepoFile(root, relativePath);
        }
      }
    });
  }
  
  @Override
  public String writeToString() {
    return FileUtils.toWriteableString(root.file) + ";" + relativePath.toWriteableString();
  }
}
