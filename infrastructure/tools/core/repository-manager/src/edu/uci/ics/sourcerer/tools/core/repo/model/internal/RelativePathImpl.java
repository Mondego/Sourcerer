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

import static edu.uci.ics.sourcerer.util.io.logging.Logging.logger;

import java.io.File;
import java.util.Map;
import java.util.logging.Level;

import edu.uci.ics.sourcerer.tools.core.repo.model.RelativePath;
import edu.uci.ics.sourcerer.util.InterningMap;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
final class RelativePathImpl implements RelativePath {
  private final String relativePath;
  private static final Map<String, RelativePathImpl> interned = new InterningMap<>();
  
  private RelativePathImpl(String relativePath) {
    this.relativePath = relativePath;
  }
  
  private static RelativePathImpl intern(String relativePath) {
    relativePath = relativePath.intern();
    RelativePathImpl path = interned.get(relativePath);
    if (path == null) {
      path = new RelativePathImpl(relativePath);
      interned.put(relativePath, path);
    }
    return path;
  }
  
  static RelativePathImpl makeEmpty() {
    return intern("");
  }
  
  static RelativePathImpl make(String relativePath) {
    relativePath = relativePath.replace('\\', '/');
    if (relativePath.charAt(0) == '/') {
      relativePath = relativePath.substring(1);
    }
    if (relativePath.charAt(relativePath.length() - 1) == '/') {
      relativePath = relativePath.substring(0, relativePath.length() - 1);
    }
    if (relativePath.indexOf('*') >= 0) {
      logger.log(Level.WARNING, "Problematic relative path for writing: " + relativePath);
    }
    return intern(relativePath);
  }
  
  static RelativePathImpl makeFromWriteable(String relativePath) {
    relativePath = relativePath.replace('*', ' ');
    return intern(relativePath);
  }
  
  boolean isEmpty() {
    return relativePath.equals("");
  }
  
  RelativePathImpl append(String path) {
    path = path.replace(File.separatorChar, '/');
    if (path.equals("")) {
      return this;
    } else {
      if (path.charAt(path.length() - 1) == '/') {
        path = path.substring(0, path.length() - 1);
      }
      if (path.charAt(0) == '/') {
        if (relativePath.equals("")) {
          return intern(path.substring(1));
        } else {
          return intern(relativePath + path);
        }
      } else {
        if (relativePath.equals("")) {
          return intern(path);
        } else {
          return intern(relativePath + "/" + path);
        }
      }
    }
  }
  
  RelativePathImpl append(RelativePathImpl path) {
    if (relativePath.equals("")) {
      return path;
    } else if (path.relativePath.equals("")) {
      return this;
    } else {
      return intern(relativePath + "/" + path.relativePath);
    }
  }
  
  String toWriteableString() {
    return relativePath.replace(' ', '*');
  }
  
  RelativePathImpl getParent() {
    if (isEmpty()) {
      throw new IllegalStateException("Cannot get a parent for an empty relative path.");
    } else {
      int idx = relativePath.lastIndexOf('/');
      if (idx == -1) {
        return makeEmpty();
      } else {
        return intern(relativePath.substring(0, idx));
      }
    }
  }
  
  String getName() {
    return relativePath.substring(relativePath.lastIndexOf('/') + 1);
  }
  
  @Override
  public String toString() {
    return relativePath;
  }
  
  @Override
  public int hashCode() {
    return relativePath.hashCode();
  }
}
