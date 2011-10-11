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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Scanner;

import edu.uci.ics.sourcerer.tools.core.repo.model.RepoFile;
import edu.uci.ics.sourcerer.util.io.CustomSerializable;
import edu.uci.ics.sourcerer.util.io.FileUtils;
import edu.uci.ics.sourcerer.util.io.ObjectDeserializer;
import edu.uci.ics.sourcerer.util.io.arguments.Argument;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class RepoFileImpl implements RepoFile, CustomSerializable {
  private final RepoFileImpl root;
  private final RelativePathImpl relativePath;
  private File file;
  private final boolean isRoot;
  
  private RepoFileImpl(File file) {
    this.root = null;
    this.isRoot = true;
    this.relativePath = RelativePathImpl.makeEmpty();
    this.file = file;
  }

  private RepoFileImpl(RepoFileImpl root, boolean isRoot, RelativePathImpl relativePath) {
    this.root = root;
    this.isRoot = isRoot;
    this.relativePath = relativePath;
    this.file = null;
  }
  
  public static RepoFileImpl makeRoot(Argument<File> root) {
    return new RepoFileImpl(root.getValue());
  }
  
//  private RepoFileImpl internRoot(RelativePathImpl path) {
//    RepoFileImpl newRoot = internedRoots.get(path);
//    if (newRoot == null) {
//      newRoot = new RepoFileImpl(getOirignalRoot(), true, path);
//      internedRoots.put(path, newRoot);
//    }
//    return newRoot;
//  }
  
//  private RepoFileImpl internChild(RelativePathImpl path) {
//    if (path.isEmpty()) {
//      return root;
//    } else {
//      RepoFileImpl newChild = internedChildren.get(path);
//      if (newChild == null) {
//        newChild = new RepoFileImpl(isRoot ? this : root, false, path);
//        internedChildren.put(path, newChild);
//      }
//      return newChild;
//    }
//  }
  
  public RepoFileImpl reroot(RepoFileImpl newRoot) {
    if (isRoot) {
      return new RepoFileImpl(newRoot.getOirignalRoot(), true, relativePath);
    } else {
      return new RepoFileImpl(root, false, relativePath);
    }
  }
  
  private RepoFileImpl getOirignalRoot() {
    if (root == null) {
      return this;
    } else {
      return root.getOirignalRoot();
    }
  }

  public boolean isRoot() {
    return isRoot;
  }
  
  public RepoFileImpl getRoot() {
    return root;
  }
  
  @Override
  public RelativePathImpl getRelativePath() {
    return relativePath;
  }
  
  public RepoFileImpl asRoot() {
    if (isRoot) {
      return this;
    } else {
      return new RepoFileImpl(root.getOirignalRoot(), true, root.relativePath.append(relativePath));
    }
  }

  private File ensureFile() {
    if (file == null) {
      file = new File(root.ensureFile(), relativePath.toString());
    }
    return file;
  }
  
  @Override
  public boolean isDirectory() {
    return ensureFile().isDirectory();
  }
  
  @Override
  public boolean exists() {
    return ensureFile().exists();
//    if (ensureFile.exists()) {
//      if (file.isDirectory()) {
//        // make sure it's not empty
//        if (file.list().length == 0) {
//          file.delete();
//          return false;
//        } else {
//          return true;
//        }
//      } else {
//        return true;
//      }
//    } else {
//      return false;
//    }
  }
  
  public boolean delete() {
    if (ensureFile().exists()) {
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
    return ensureFile();
  }

  @Override
  public boolean makeDirs() {
    return ensureFile().mkdirs();
  }
  
  @Override
  public boolean makeParentDirs() {
    return ensureFile().getParentFile().mkdirs();
  }
    
  public File getChildFile(String child) {
    if (ensureFile().isFile()) {
      throw new IllegalStateException("Cannot get a child of a file: " + file.getPath() + " " + child);
    } else {
      return new File(file, child);
    }
  }
  
  public RepoFileImpl getChild(Argument<String> child) {
    return getChild(child.getValue());
  }
  
  public RepoFileImpl getChild(String child) {
    if (isRoot) {
      return new RepoFileImpl(this, false, RelativePathImpl.make(child));
    } else {
      return new RepoFileImpl(root, false, relativePath.append(child));
    }
  }
  
  private RepoFileImpl getChild(RelativePathImpl relativePath) {
    if (isRoot) {
      return new RepoFileImpl(this, false, relativePath);
    } else {
      return new RepoFileImpl(root, false, this.relativePath.append(relativePath));
    }
  }
  
  public RepoFileImpl getChildRoot(String child) {
    if (isRoot) {
      return new RepoFileImpl(getOirignalRoot(), true, relativePath.append(child));
    } else {
      return new RepoFileImpl(getOirignalRoot(), true, root.relativePath.append(relativePath).append(child));
    }
  }
  
  private RepoFileImpl getChildRoot(RelativePathImpl relativePath) {
    if (isRoot) {
      return new RepoFileImpl(getOirignalRoot(), true, this.relativePath.append(relativePath));
    } else {
      return new RepoFileImpl(getOirignalRoot(), true, root.relativePath.append(this.relativePath).append(relativePath));
    }
  }
  
  public RepoFileImpl getParent() {
    if (isRoot) {
      throw new IllegalStateException("May not get parent of root.");
    } else {
      RelativePathImpl parentPath = relativePath.getParent();
      if (parentPath.isEmpty()) {
        return root;
      } else {
        return new RepoFileImpl(root, false, parentPath);
      }
    }
  }
  
  public Collection<RepoFileImpl> getChildren() {
    if (ensureFile().isFile()) {
      throw new IllegalStateException("Cannot get children of a file: " + file.getPath());
    } else if (!file.exists()) {
      return Collections.emptyList();
    } else {
      String[] names = file.list();
      Collection<RepoFileImpl> children = new ArrayList<>(names.length);
      for (String name : names) {
        children.add(getChild(name));
      }
      return children;
    }
  }
  
  @Override
  public String getName() {
    return relativePath.getName();
  }
  
  @Override
  public String toString() {
    if (root == null) {
      return "/";
    } else {
      return root.toString() + "/" + relativePath.toString();
    }
  }
  
  /* Serialization Related Methods */
  public ObjectDeserializer<RepoFileImpl> makeDeserializer() {
    return new ObjectDeserializer<RepoFileImpl>() {
      @Override
      public RepoFileImpl deserialize(Scanner scanner) {
        if (scanner.hasNextInt()) {
          RepoFileImpl root = getOirignalRoot();
          for (int count = scanner.nextInt(); count > 1; count--) {
            root = root.getChildRoot(RelativePathImpl.makeFromWriteable(scanner.next()));
          }
          return root.getChild(RelativePathImpl.makeFromWriteable(scanner.next()));
        } else {
          throw new IllegalArgumentException("RepoFile expects an int.");
        }
      }
    };
  }
  
  @Override
  public String serialize() {
    RepoFileImpl file = this;
    Deque<String> paths = new LinkedList<>();
    while (file.root != null) {
      paths.push(file.relativePath.toWriteableString());
      file = file.root;
    }
    
    StringBuilder result = new StringBuilder();
    result.append(paths.size());
    for (String path : paths) {
      result.append(' ').append(path);
    }
    return result.toString();
  }
  
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    } else if (o instanceof RepoFileImpl) {
      RepoFileImpl other = (RepoFileImpl) o;
      if (root == null && other.root == null) {
        return file.equals(other.file);
      } else if (root != null && other.root != null) {
        return root.equals(other.root) && relativePath.equals(other.relativePath);
      } else {
        return false;
      }
    } else {
      return false;
    }
  }
  
  @Override
  public int hashCode() {
    return relativePath.hashCode();
  }
}
