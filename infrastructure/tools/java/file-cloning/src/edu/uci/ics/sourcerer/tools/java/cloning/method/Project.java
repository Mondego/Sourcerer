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
package edu.uci.ics.sourcerer.tools.java.cloning.method;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import edu.uci.ics.sourcerer.util.Helper;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public final class Project {
  private String path;
  private Map<String, File> files;
  
  protected Project(String path) {
    this.path = path;
    files = Helper.newHashMap();
  }
  
  public String getName() {
    return path;
  }
  
  public Collection<File> getFiles() {
    return files.values();
  }
  
  public void filterFiles() {
    for (Iterator<File> iter = files.values().iterator(); iter.hasNext();) {
      File file = iter.next();
      if (!file.hasAllKeys()) {
        iter.remove();
      }
    }
  }
  
  protected File getFile(String path) {
    if (path.charAt(0) == '/') {
      path = path.substring(1);
    }
    File file = files.get(path);
    if (file == null) {
      file = new File(this, path);
      files.put(path, file);
    }
    return file;
  }

  public String toString() {
    return path;
  }
}
