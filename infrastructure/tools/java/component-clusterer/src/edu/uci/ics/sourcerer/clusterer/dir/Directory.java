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

import edu.uci.ics.sourcerer.util.io.TablePrettyPrinter;

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
  
  public void compare(Directory other) {
    if (!project.equals(other.project)) {
      int matchingCount = 0;
      int i = 0, j = 0;
      while (i < files.length && j < other.files.length) {
        int comp = files[i].compareTo(other.files[j]);
        if (comp == 0) {
          matchingCount++;
          i++;
          j++;
        } else if (comp < 0) {
          i++;
        } else {
          j++;
        }
      }
      double percent = ((double) matchingCount) / ((double) Math.min(files.length, other.files.length));
      if (percent >= .3) {
        matches30++;
        matches50++;
        matches80++;
        other.matches30++;
        other.matches50++;
        other.matches80++;
      } else if (percent >= .5) {
        matches50++;
        matches80++;
        other.matches50++;
        other.matches80++;
      } else if (percent >= .8) {
        matches80++;
        other.matches80++;
      }
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

  public void writeRow(TablePrettyPrinter printer) {
    printer.beginRow();
    printer.addCell(project);
    printer.addCell(path);
    printer.addCell(files.length);
    printer.addCell(matches30);
    printer.addCell(matches50);
    printer.addCell(matches80);
  }
  
  @Override
  public String toString() {
    return project;
  }
}
