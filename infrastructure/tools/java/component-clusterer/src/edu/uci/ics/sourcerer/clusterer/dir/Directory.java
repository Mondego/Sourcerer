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
/**
 *
 */
package edu.uci.ics.sourcerer.clusterer.dir;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import edu.uci.ics.sourcerer.util.Helper;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class Directory {
  private String project;
  private String path;
  private String[] files;
  
  private int matches30 = 0;
  private int matches50 = 0;
  private int matches80 = 0;
  
  protected Directory(String project, String path, String[] files) {
    this.project = project;
    this.path = path;
    this.files = files;
  }
  
  public String getProject() {
    return project;
  }
  
  public String getPath() {
    return path;
  }
  
  public String[] getFiles() {
    return files;
  }
  
  public int get30() {
    return matches30;
  }
  
  public int get50() {
    return matches50;
  }
  
  public int get80() {
    return matches80;
  }
  
  public void compare(Directory other, Set<String> ignore, Map<String, CopiedFile> copiedFiles) {
    if (!project.equals(other.project)) {
      Collection<String> matching = Helper.newLinkedList();
      int i = 0, j = 0;
      while (i < files.length && j < other.files.length) {
        if (ignore.contains(files[i])) {
          i++;
          continue;
        }
        if (ignore.contains(other.files[j])) {
          j++;
          continue;
        }
        int comp = files[i].compareTo(other.files[j]);
        if (comp == 0) {
          matching.add(files[i]);
          i++;
          j++;
        } else if (comp < 0) {
          i++;
        } else {
          j++;
        }
      }
      if (matching.size() < DirectoryClusterer.MINIMUM_DIR_SIZE.getValue()) {
        return;
      }
      
      double percent = ((double) matching.size()) / ((double) Math.min(files.length, other.files.length));
      if (percent >= .8) {
        matches30++;
        matches50++;
        matches80++;
        other.matches30++;
        other.matches50++;
        other.matches80++;
        for (String name : matching) {
          CopiedFile file = Helper.getFromMap(copiedFiles, name, CopiedFile.class);
          file.increment80();
        }
      } else if (percent >= .5) {
        matches30++;
        matches50++;
        other.matches30++;
        other.matches50++;
        for (String name : matching) {
          CopiedFile file = Helper.getFromMap(copiedFiles, name, CopiedFile.class);
          file.increment50();
        }
      } else if (percent >= .3) {
        matches30++;
        other.matches30++;
        for (String name : matching) {
          CopiedFile file = Helper.getFromMap(copiedFiles, name, CopiedFile.class);
          file.increment30();
        }
      }
    }
  }
  
  public Collection<String> matches(Directory other, Set<String> ignore, double threshold) {
    if (!project.equals(other.project)) {
      Collection<String> matching = Helper.newLinkedList();
      int i = 0, j = 0;
      while (i < files.length && j < other.files.length) {
        if (ignore.contains(files[i])) {
          i++;
          continue;
        }
        if (ignore.contains(other.files[j])) {
          j++;
          continue;
        }
        int comp = files[i].compareTo(other.files[j]);
        if (comp == 0) {
          matching.add(files[i]);
          i++;
          j++;
        } else if (comp < 0) {
          i++;
        } else {
          j++;
        }
      }
      if (matching.size() < DirectoryClusterer.MINIMUM_DIR_SIZE.getValue()) {
        return Collections.emptyList();
      }
      
      double percent = ((double) matching.size()) / ((double) Math.min(files.length, other.files.length));
      if (percent >= threshold) {
        return matching;
      } else {
        return Collections.emptyList();
      }
    } else {
      return Collections.emptyList();
    }
  }
  
  public boolean matches30() {
    return matches30 > 0;
  }
  
  public boolean matches50() {
    return matches50 > 0;
  }
  
  public boolean matches80() {
    return matches80 > 0;
  }
  
  @Override
  public String toString() {
    return project;
  }
}
