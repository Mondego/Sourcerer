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
package edu.uci.ics.sourcerer.tools.java.cloning.stats;

import static edu.uci.ics.sourcerer.util.io.logging.Logging.logger;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;

import edu.uci.ics.sourcerer.util.Helper;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class FileCluster {
  private Collection<String> projects;
  private Collection<String> paths;
  
  public FileCluster() {
    projects = Helper.newHashSet();
    paths = Helper.newHashSet();
  }
  
  public void addFile(String project, String path) {
//    if (projects.contains(project)) {
//      logger.info("Within project duplicate: ");
//      StringBuilder builder = new StringBuilder();
//      builder.append(project).append(":").append(path);
//      for (String other : paths) {
//        builder.append(" ").append(other);
//      }
//      logger.info(builder.toString());
//    }
    projects.add(project);
    if (path.startsWith("/")) {
      paths.add(project + ":" + path);
    } else {
      paths.add(project + ":/" + path);
    }
  }
    
  public void addProjectUniqueFile(String project, String path) {
    if (projects.contains(project)) {
//      logger.log(Level.SEVERE, "Duplicate for " + project + ": " + path);
      projects.add(project);
      if (path.startsWith("/")) {
        paths.add(project + ":" + path);
      } else {
        paths.add(project + ":/" + path);
      }
    } else {
      projects.add(project);
      if (path.startsWith("/")) {
        paths.add(project + ":" + path);
      } else {
        paths.add(project + ":/" + path);
      }
    }
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
  
  public void validatePaths(Set<String> validPaths) {
    Iterator<String> iter = paths.iterator();
    for (String path = iter.next(); iter.hasNext(); path = iter.next()) {
      if (!validPaths.contains(path)) {
        logger.log(Level.SEVERE, "Invalid path: " + path);
        iter.remove();
      }
    }
  }
}
