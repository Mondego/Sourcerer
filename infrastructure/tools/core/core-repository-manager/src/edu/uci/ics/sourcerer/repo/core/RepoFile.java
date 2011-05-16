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

import java.io.File;
import java.util.Map;
import java.util.Scanner;

import edu.uci.ics.sourcerer.util.Helper;
import edu.uci.ics.sourcerer.util.io.FieldConverter;
import edu.uci.ics.sourcerer.util.io.LWRec;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class RepoFile implements LWRec {
  private final RepoFile root;
  private final RelativePath relativePath;
  private final File file;
  private Map<RelativePath, RepoFile> subrootMap;
  
  private RepoFile(File file) {
    root = null;
    relativePath = RelativePath.makeEmpty();
    this.file = file;
    subrootMap = Helper.newHashMap();
  }
  
  private RepoFile(RepoFile root) {
    this.root = root;
    this.relativePath = RelativePath.makeEmpty();
    this.file = root.root.file;
    root.subrootMap.put(relativePath, this);
    subrootMap = Helper.newHashMap();
  }
  
  private RepoFile(RepoFile root, RelativePath relativePath) {
    this.root = root;
    this.relativePath = relativePath;
    this.file = new File(root.file, relativePath.toString());
  }
  
  public static RepoFile makeRoot(File root) {
    return new RepoFile(root);
  }

  public RepoFile getRoot() {
    return root;
  }
  
  public RelativePath getRelativePath() {
    return relativePath;
  }
  
  public RepoFile asRoot() {
    RepoFile subRoot = root.subrootMap.get(relativePath);
    if (subRoot == null) {
      subRoot = new RepoFile(this);
    }
    return subRoot;
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
  
  public RepoFile getChildRoot(String child) {
    if (file.isFile()) {
      throw new IllegalStateException("Cannot get a child of a file: " + file.getPath() + " " + relativePath);
    } else {
      RelativePath path = relativePath.append(child);
      RepoFile subRoot = root.subrootMap.get(path);
      if (subRoot == null) {
        subRoot = new RepoFile(new RepoFile(root, path));
      }
      return subRoot;
    }
  }
  
  public RepoFile getChildRoot(RelativePath relativePath) {
    if (file.isFile()) {
      throw new IllegalStateException("Cannot get a child of a file: " + file.getPath() + " " + relativePath);
    } else {
      RelativePath path = this.relativePath.append(relativePath);
      RepoFile subRoot = root.subrootMap.get(path);
      if (subRoot == null) {
        subRoot = new RepoFile(new RepoFile(root, path));
      }
      return subRoot;
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
  
  public static void registerConverterHelper(final RepoFile repoRoot) {
    FieldConverter.registerConverterHelper(RepoFile.class, new FieldConverter.FieldConverterHelper() {
      @Override
      protected Object makeFromScanner(Scanner scanner) throws IllegalAccessException {
        String value = scanner.next();
        RepoFile root = repoRoot;
        int sep = -1;
        int prevSep = sep + 1;
        while ((sep = value.indexOf(';', prevSep)) >= 0) {
          root = root.getChildRoot(RelativePath.makeFromWriteable(value.substring(prevSep, sep)));
          prevSep = sep + 1;
        }
        return root.getChild(RelativePath.makeFromWriteable(value.substring(prevSep)));
      }
    });
  }
  
  @Override
  public String writeToString() {
    if (root.root == null) {
      return relativePath.toWriteableString();
    } else {
      return root.writeToString() + ";" + relativePath.toWriteableString();
    }
  }
}
