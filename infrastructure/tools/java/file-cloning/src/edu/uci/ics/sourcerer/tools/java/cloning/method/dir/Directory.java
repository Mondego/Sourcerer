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
package edu.uci.ics.sourcerer.tools.java.cloning.method.dir;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import edu.uci.ics.sourcerer.util.Helper;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class Directory {
  private static final int minMatchSize = DirectoryClusterer.MINIMUM_MATCH_SIZE.getValue();
  private String project;
  private String path;
  private JavaFile[] files;
  
  private Collection<String> matches80;
  private Collection<String> matches50;
  private Collection<String> matches30;
  
  protected Directory(String project, String path, JavaFile[] files) {
    this.project = project;
    this.path = path;
    this.files = files;
  }
  
  public JavaFile[] getFiles() {
    return files;
  }
  
  public String getProject() {
    return project;
  }
  
  public String getPath() {
    return path;
  }
  
  public Collection<String> getMatches80() {
    if (matches80 == null) {
      return Collections.emptyList();
    }  else {
      return matches80;
    }
  }
  
  public Collection<String> getMatches50() {
    if (matches50 == null) {
      return Collections.emptyList();
    } else {
      return matches50;
    }
  }
  
  public Collection<String> getMatches30() {
    if (matches30 == null) {
      return Collections.emptyList();
    } else {
      return matches30;
    }
  }
  
  public void compare(Directory other, Set<String> ignore) {
    if (!project.equals(other.project)) {
      int matchCount = 0;
      int i = 0, j = 0;
      while (i < files.length && j < other.files.length) {
        if (ignore.contains(files[i].getName())) {
          i++;
        } else if (ignore.contains(other.files[j].getName())) {
          j++;
        } else {
          int comp = files[i].getName().compareTo(other.files[j].getName());
          if (comp == 0) {
            matchCount++;
            i++;
            j++;
          } else if (comp < 0) {
            i++;
          } else {
            j++;
          }
        }
      }
      
      if (matchCount < minMatchSize) {
        return;
      }
      
      double percent = (double) matchCount / Math.min(files.length, other.files.length);
      if (percent < .3) {
        return;
      }
      i = 0;
      j = 0;
      String matchingPath = other.project + ":" + other.path;
      String currentPath = project + ":" + path;
      while (i < files.length && j < other.files.length) {
        int comp = files[i].getName().compareTo(other.files[j].getName());
        if (comp == 0) {
          files[i].addMatch(percent, matchingPath);
          other.files[j].addMatch(percent, currentPath);
          i++;
          j++;
        } else if (comp < 0) {
          i++;
        } else {
          j++;
        }
      }
      if (percent >= .8) {
        if (matches80 == null) {
          matches80 = Helper.newLinkedList();
        }
        matches80.add(matchingPath);
      } else if (percent >= .5) {
        if (matches50 == null) {
          matches50 = Helper.newLinkedList();
        }
        matches50.add(matchingPath);
      } else {
        if (matches30 == null) {
          matches30 = Helper.newLinkedList();
        }
        matches30.add(matchingPath);
      }
    }
  }
}
