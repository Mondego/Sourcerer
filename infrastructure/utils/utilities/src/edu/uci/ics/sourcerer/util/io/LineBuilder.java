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
package edu.uci.ics.sourcerer.util.io;

import java.util.Scanner;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public final class LineBuilder {
  private StringBuilder builder;
  private String lastItem;
  
  public void addItem(String item) {
    if (lastItem == null) {
      lastItem = item;
      builder = new StringBuilder();
    } else {
      builder.append(lastItem).append(" ");
      lastItem = item;
    }
  }
  
  public void addItem(int item) {
    addItem(Integer.toString(item));
  }
  
  public String toLine() {
    if (lastItem == null) {
      return "";
    } else {
      String ret = builder.append(lastItem).toString();
      builder = new StringBuilder();
      lastItem = null;
      return ret;
    }
  }
  
  public static String[] splitLine(String line) {
    return line.split(" ");
  }
  
  public static Scanner getScanner(String line) {
    return new Scanner(line).useDelimiter(" ");
  }
}
