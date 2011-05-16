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

import java.util.Map;
import java.util.logging.Level;

import edu.uci.ics.sourcerer.util.Helper;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class RelativePath {
  private final String relativePath;
  private static final Map<String, RelativePath> singletonMap = Helper.newHashMap();
  
  private RelativePath(String relativePath) {
    this.relativePath = relativePath;
  }
  
  private static RelativePath getSingleton(String relativePath) {
    RelativePath path = singletonMap.get(relativePath);
    if (path == null) {
      path = new RelativePath(relativePath);
      singletonMap.put(relativePath, path);
    }
    return path;
  }
  
  protected static RelativePath make(String relativePath) {
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
    return getSingleton(relativePath);
  }
  
  protected static RelativePath makeFromWriteable(String relativePath) {
    relativePath = relativePath.replace('*', ' ');
    return getSingleton(relativePath);
  }
  
  protected RelativePath append(String path) {
    path = path.replace('\\', '/');
    if (path.charAt(path.length() - 1) == '/') {
      path = path.substring(0, path.length() - 1);
    }
    if (path.charAt(0) == '/') {
      if (relativePath.equals("")) {
        return getSingleton(path.substring(1));
      } else {
        return getSingleton(relativePath + path);
      }
    } else {
      if (relativePath.equals("")) {
        return getSingleton(path);
      } else {
        return getSingleton(relativePath + "/" + path);
      }
    }
  }
  
  protected RelativePath append(RelativePath path) {
    if (relativePath.equals("")) {
      return path;
    } else {
      return getSingleton(relativePath + "/" + path.relativePath);
    }
  }
  
  protected String toWriteableString() {
    return relativePath.replace(' ', '*');
  }
  
  @Override
  public String toString() {
    return relativePath;
  }
}
