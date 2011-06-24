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
package edu.uci.ics.sourcerer.repo.internal.core;

import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

import edu.uci.ics.sourcerer.repo.core.IContentDirectory;
import edu.uci.ics.sourcerer.util.Helper;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
final class ContentDirectory implements IContentDirectory {
  private ContentDirectory parent;
  private Collection<ContentDirectory> dirs;
  private Collection<ContentFile> files;
  private final RepoFile file;
  
  private Map<RepoFile, ContentDirectory> internedDirs;
  
  private ContentDirectory(RepoFile file) {
    this.file = file;
    internedDirs = Helper.newHashMap();
    internedDirs.put(file, this);
  }
  
  private ContentDirectory(ContentDirectory parent, RepoFile file) {
    this.file = file;
    this.parent = parent;
    this.internedDirs = parent.internedDirs;
    if (parent.dirs == null) {
      parent.dirs = Helper.newLinkedList();
    }
    parent.dirs.add(this);
  }
  
  protected static ContentDirectory makeRoot(RepoFile file) {
    return new ContentDirectory(file.asRoot());
  }
  
  protected ContentDirectory make(RepoFile file) {
    if (this.file == file) {
      return this;
    } else if (this.file == file.getRoot()) {
      ContentDirectory dir = internedDirs.get(file);
      if (dir == null) {
        dir = new ContentDirectory(make(file.getParent()), file);
        internedDirs.put(file, dir);
      }
      return dir;
    } else {
      throw new IllegalArgumentException(file + " must be a subdir of " + this);
    }
  }
  
  protected void addFile(ContentFile file) {
    if (files == null) {
      files = Helper.newLinkedList();
    }
    files.add(file);
  }
  
  @Override
  public ContentDirectory getParentDirectory() {
    return parent;
  }
  
  @Override
  public Collection<ContentDirectory> getSubdirectories() {
    if (dirs == null) {
      return Collections.emptyList();
    } else {
      return dirs;
    }
  }
  
  @Override
  public RepoFile getFile() {
    return file;
  }
  
  @Override
  public Collection<ContentFile> getFiles() {
    if (files == null) {
      return Collections.emptyList();
    } else {
      return files;
    }
  }
  
  @Override
  public Iterable<ContentFile> getAllFiles() {
    return new Iterable<ContentFile>() {
      @Override
      public Iterator<ContentFile> iterator() {
        return new Iterator<ContentFile>() {
          Deque<ContentDirectory> dirs = Helper.newStack();
          Iterator<ContentFile> iter = getFiles().iterator(); 
          {
            dirs.push(ContentDirectory.this);
          }
          @Override
          public boolean hasNext() {
            while (!iter.hasNext()) {
              if (dirs.isEmpty()) {
                return false;
              } else {
                ContentDirectory dir = dirs.pop();
                dirs.addAll(dir.getSubdirectories());
                iter = dir.getFiles().iterator();
              }
            }
            return true;
          }

          @Override
          public ContentFile next() {
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
}
