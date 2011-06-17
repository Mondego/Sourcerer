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
package edu.uci.ics.sourcerer.model;

import java.util.Scanner;

import edu.uci.ics.sourcerer.util.io.LineBuildable;
import edu.uci.ics.sourcerer.util.io.LineBuilder;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class Location implements LineBuildable {
  private String path;
  private int offset;
  private int length;
  
  public Location(String path) {
    this(path, -1, 0);
  }
  
  public Location(String path, int offset, int length) {
    this.path = path;
    this.offset = offset;
    this.length = length;
  }
  
  public void setPath(String path) {
    this.path = path;
  }
  
  public String getPath() {
    return path;
  }
  
  public int getOffset() {
    return offset;
  }
  
  public int getLength() {
    return length;
  }
  
  public static Location parse(Scanner scanner) {
    return new Location(scanner.next(), scanner.nextInt(), scanner.nextInt());
  }
  
  public void addToLineBuilder(LineBuilder builder) {
    builder.append(path).append(offset).append(length);
  }
}
