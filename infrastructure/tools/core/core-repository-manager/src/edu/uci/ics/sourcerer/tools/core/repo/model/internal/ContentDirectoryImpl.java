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

import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.NoSuchElementException;

import edu.uci.ics.sourcerer.tools.core.repo.model.ContentDirectory;
import edu.uci.ics.sourcerer.util.Helper;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public final class ContentDirectoryImpl implements ContentDirectory {
  private ContentDirectoryImpl parent;
  private Collection<ContentDirectoryImpl> dirs;
  private Map<RepoFileImpl, ContentFileImpl> files;
  private Integer value;
  private final RepoFileImpl file;
  
  private Map<RepoFileImpl, ContentDirectoryImpl> internedDirs;
  
  private ContentDirectoryImpl(RepoFileImpl file) {
    this.file = file;
    internedDirs = new HashMap<>();
    internedDirs.put(file, this);
  }
  
  private ContentDirectoryImpl(ContentDirectoryImpl parent, RepoFileImpl file) {
    this.file = file;
    this.parent = parent;
    this.internedDirs = parent.internedDirs;
    if (parent.dirs == null) {
      parent.dirs = new LinkedList<>();
    }
    parent.dirs.add(this);
  }
  
  protected static ContentDirectoryImpl makeRoot(RepoFileImpl file) {
    return new ContentDirectoryImpl(file.asRoot());
  }
  
  public ContentDirectoryImpl make(RepoFileImpl file) {
    if (this.file.equals(file)) {
      return this;
    } else if (this.file.equals(file.getRoot())) {
      ContentDirectoryImpl dir = internedDirs.get(file);
      if (dir == null) {
        dir = new ContentDirectoryImpl(make(file.getParent()), file);
        internedDirs.put(file, dir);
      }
      return dir;
    } else {
      throw new IllegalArgumentException(file + " must be a subdir of " + this + ", not " + file.getRoot());
    }
  }

  public ContentFileImpl makeFile(RepoFileImpl file) {
    ContentFileImpl contentFile = null;
    if (files == null) {
      files = new HashMap<>();
    } else {
      contentFile = files.get(file);
    }
    if (contentFile == null) {
      contentFile = new ContentFileImpl(this, file);
      files.put(file, contentFile);
    }
    return contentFile;
  }
  
  @Override
  public ContentDirectoryImpl getParentDirectory() {
    return parent;
  }
  
  @Override
  public Collection<ContentDirectoryImpl> getSubdirectories() {
    if (dirs == null) {
      return Collections.emptyList();
    } else {
      return dirs;
    }
  }
  
  @Override
  public RepoFileImpl getFile() {
    return file;
  }
  
  @Override
  public Collection<ContentFileImpl> getFiles() {
    if (files == null) {
      return Collections.emptyList();
    } else {
      return Collections.unmodifiableCollection(files.values());
    }
  }
  
  @Override
  public Iterable<ContentFileImpl> getAllFiles() {
    return new Iterable<ContentFileImpl>() {
      @Override
      public Iterator<ContentFileImpl> iterator() {
        return new Iterator<ContentFileImpl>() {
          Deque<ContentDirectoryImpl> dirs = Helper.newStack();
          Iterator<ContentFileImpl> iter = getFiles().iterator(); 
          {
            dirs.push(ContentDirectoryImpl.this);
          }
          @Override
          public boolean hasNext() {
            while (!iter.hasNext()) {
              if (dirs.isEmpty()) {
                return false;
              } else {
                ContentDirectoryImpl dir = dirs.pop();
                dirs.addAll(dir.getSubdirectories());
                iter = dir.getFiles().iterator();
              }
            }
            return true;
          }

          @Override
          public ContentFileImpl next() {
            if (hasNext()) {
              return iter.next();
            } else {
              throw new NoSuchElementException();
            }
          }

          @Override
          public void remove() {
            throw new UnsupportedOperationException();
          }
        };
      }
    };
  }
  
  public int getCount() {
    ContentDirectoryImpl dir = this;
    int count = 0;
    while (dir != null) {
      count += dir.getValue();
      dir = dir.parent;
    }
    return count;
  }
  
  private int getValue() {
    if (value == null) {
      int count = 0;
      Deque<ContentDirectoryImpl> stack = Helper.newStack();
      stack.add(this);
      while (!stack.isEmpty()) {
        ContentDirectoryImpl dir = stack.pop();
        count += dir.getFiles().size();
        stack.addAll(dir.getSubdirectories());
      }
      value = count;
      return count;
    } else {
      return value.intValue();
    }
  }
  
  @Override
  public String toString() {
    return file.toString();
  }
}
