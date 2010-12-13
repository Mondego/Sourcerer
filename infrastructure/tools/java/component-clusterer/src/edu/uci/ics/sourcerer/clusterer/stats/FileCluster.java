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

import java.util.Collection;

import edu.uci.ics.sourcerer.util.Helper;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class FileCluster {
  private Collection<String> projects;
  private Collection<String> paths;
  
  public FileCluster() {
    projects = Helper.newHashSet();
    paths = Helper.newLinkedList();
  }
  
  public void addFile(String project, String path) {
    projects.add(project);
    paths.add(project + ":" + path);
  }
  
  public int getProjectCount() {
    return projects.size();
  }
  
  public Collection<String> getProjects() {
    return projects;
  }
  
  public Collection<String> getPaths() {
    return paths;
  }
  
  public int getFileCount() {
    return paths.size();
  }
}
