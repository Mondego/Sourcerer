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
package edu.uci.ics.sourcerer.clusterer.stats;

import java.util.Set;

import edu.uci.ics.sourcerer.util.Helper;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class FileFilter implements Filter {
  private Set<String> files;
  
  public FileFilter() {
    files = Helper.newHashSet();
  }
  
  public void addFile(String project, String path) {
    if (path.startsWith("/")) {
      files.add(project + ":" + path);
    } else {
      files.add(project + ":/" + path);
    }
  }
  
  @Override
  public boolean pass(String project, String path) {
    if (path.startsWith("/")) {
      return files.contains(project + ":" + path);
    } else {
      return files.contains(project + ":/" + path);
    }
  }
}
