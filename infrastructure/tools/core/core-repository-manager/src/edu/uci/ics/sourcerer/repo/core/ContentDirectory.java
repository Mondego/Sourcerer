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

import java.util.Collection;
import java.util.Map;

import edu.uci.ics.sourcerer.util.Helper;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class ContentDirectory {
  private ContentDirectory parent;
  private Collection<ContentDirectory> dirs;
  private Collection<ContentFile> files;
  private final RepoFile file;
  
  private Map<RepoFile, ContentDirectory> internedDirs;
  
  private ContentDirectory(RepoFile file) {
    this.file = file;
    internedDirs = Helper.newHashMap();
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
  
  public ContentDirectory getParentDirectory() {
    return parent;
  }
  
  public Collection<ContentDirectory> getSubdirectories() {
    return dirs;
  }
  
  public Collection<ContentFile> getFiles() {
    return files;
  }
  
  public Iterable<ContentFile> getAllFiles() {
    return null;
  }
}
