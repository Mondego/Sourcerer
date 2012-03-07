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

  public LineBuilder() {
    builder = new StringBuilder();
  }
  
  public <T> LineBuilder append(T[] items) {
    builder.append(items.length).append(" ");
    for (T item : items) {
      builder.append(item).append(" ");
    }
    return this;
  }
  
  public LineBuilder append(String item) {
    builder.append(item).append(" ");
    return this;
  }
  
  public LineBuilder append(int item) {
    return append(Integer.toString(item));
  }
  
//  public LineBuilder append(LineBuildable obj) {
//    if (obj == null) {
//      append("null");
//    } else {
//      obj.addToLineBuilder(this);
//    }
//    return this;
//  }
  
  public String toString() {
    if (builder.length() == 0) {
      return "";
    } else {
      return builder.toString().substring(0, builder.length() - 1);
    }
  }
  
  public void reset() {
    builder.setLength(0);
  }
  
  public static String[] splitLine(String line) {
    return line.split(" ");
  }
  
  public static Scanner getScanner(String line) {
    return new Scanner(line).useDelimiter(" ");
  }
}
