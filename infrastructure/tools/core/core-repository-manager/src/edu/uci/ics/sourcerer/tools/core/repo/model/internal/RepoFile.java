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
package edu.uci.ics.sourcerer.tools.core.repo.model.internal;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Scanner;

import edu.uci.ics.sourcerer.tools.core.repo.model.IRepoFile;
import edu.uci.ics.sourcerer.util.Helper;
import edu.uci.ics.sourcerer.util.io.arguments.Argument;
import edu.uci.ics.sourcerer.util.io.internal.FieldConverter;
import edu.uci.ics.sourcerer.util.io.internal.FileUtils;
import edu.uci.ics.sourcerer.util.io.internal.LWRec;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
class RepoFile implements IRepoFile, LWRec {
  private final RepoFile root;
  private final RelativePath relativePath;
  private final File file;
  private final boolean isRoot;
  
  private final Map<RelativePath, RepoFile> internedChildren;
  private final Map<RelativePath, RepoFile> internedRoots;
  
  private RepoFile(File file) {
    this.root = null;
    this.isRoot = true;
    this.relativePath = RelativePath.makeEmpty();
    this.file = file;
    this.internedChildren = Helper.newHashMap();
    this.internedRoots = Helper.newHashMap();
  }

  private RepoFile(RepoFile root, boolean isRoot, RelativePath relativePath) {
    this.root = root;
    this.isRoot = isRoot;
    if (isRoot) {
      this.relativePath = RelativePath.makeEmpty();
    } else {
      this.relativePath = relativePath;
    }
    this.file = new File(root.file, relativePath.toString());
    this.internedRoots = root.internedRoots;
    if (isRoot) {
      this.internedChildren = Helper.newHashMap();
    } else {
      this.internedChildren = root.internedChildren;
    }
  }
  
  protected static RepoFile makeRoot(Argument<File> root) {
    return new RepoFile(root.getValue());
  }
  
  private RepoFile internRoot(RelativePath path) {
    RepoFile newRoot = internedRoots.get(path);
    if (newRoot == null) {
      newRoot = new RepoFile(getOirignalRoot(), true, path);
      internedRoots.put(path, newRoot);
    }
    return newRoot;
  }
  
  private RepoFile internChild(RelativePath path) {
    if (path.isEmpty()) {
      return root;
    } else {
      RepoFile newChild = internedChildren.get(path);
      if (newChild == null) {
        newChild = new RepoFile(isRoot ? this : root, false, path);
        internedChildren.put(path, newChild);
      }
      return newChild;
    }
  }
  
  private RepoFile getOirignalRoot() {
    if (root == null) {
      return this;
    } else {
      return root.getOirignalRoot();
    }
  }

  protected boolean isRoot() {
    return isRoot;
  }
  
  protected RepoFile getRoot() {
    return root;
  }
  
  @Override
  public RelativePath getRelativePath() {
    return relativePath;
  }
  
  protected RepoFile asRoot() {
    if (isRoot) {
      return this;
    } else {
      return internRoot(root.relativePath.append(relativePath));
    }
  }
  
  @Override
  public boolean isDirectory() {
    return file.isDirectory();
  }
  
  @Override
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
  
  protected boolean delete() {
    if (file.exists()) {
      if (file.isDirectory()) {
        return FileUtils.delete(file);
      } else {
        return file.delete();
      }
    } else {
      return true;
    }
  }
  
  @Override
  public File toFile() {
    return file;
  }

  @Override
  public boolean makeDirs() {
    return file.mkdirs();
  }
  
  @Override
  public boolean makeParentDirs() {
    return file.getParentFile().mkdirs();
  }
    
  protected File getChildFile(String child) {
    if (file.isFile()) {
      throw new IllegalStateException("Cannot get a child of a file: " + file.getPath() + " " + child);
    } else {
      return new File(file, child);
    }
  }
  
  protected RepoFile getChild(String child) {
    if (file.isFile()) {
      throw new IllegalStateException("Cannot get a child of a file: " + file.getPath() + " " + child);
    } else {
      return internChild(relativePath.append(child));
    }
  }
  
  protected RepoFile getChild(RelativePath relativePath) {
    if (file.isFile()) {
      throw new IllegalStateException("Cannot get a child of a file: " + file.getPath() + " " + relativePath);
    } else {
      return internChild(this.relativePath.append(relativePath));
    }
  }
  
  protected RepoFile getChildRoot(String child) {
    if (file.isFile()) {
      throw new IllegalStateException("Cannot get a child of a file: " + file.getPath() + " " + child);
    } else {
      return internRoot(relativePath.append(child));
    }
  }
  
  protected RepoFile getChildRoot(RelativePath relativePath) {
    if (file.isFile()) {
      throw new IllegalStateException("Cannot get a child of a file: " + file.getPath() + " " + relativePath);
    } else {
      return internRoot(this.relativePath.append(relativePath));
    }
  }
  
  public RepoFile getParent() {
    return internChild(relativePath.getParent());
  }
  
  public Collection<RepoFile> getChildren() {
    if (file.isFile()) {
      throw new IllegalStateException("Cannot get children of a file: " + file.getPath());
    } else if (!file.exists()) {
      return Collections.emptyList();
    } else {
      String[] names = file.list();
      Collection<RepoFile> children = Helper.newArrayList(names.length);
      for (String name : names) {
        children.add(getChild(name));
      }
      return children;
    }
  }
  
  @Override
  public String getName() {
    return file.getName();
  }
  
  @Override
  public String toString() {
    if (isRoot) {
      if (root == null) {
        return "/";
      } else {
        return root.toString();
      }
    } else {
      return root.toString() + "/" + relativePath.toString();
    }
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
