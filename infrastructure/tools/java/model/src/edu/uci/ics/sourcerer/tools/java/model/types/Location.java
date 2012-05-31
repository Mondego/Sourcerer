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
package edu.uci.ics.sourcerer.tools.java.model.types;

import java.util.InputMismatchException;
import java.util.Scanner;

import edu.uci.ics.sourcerer.util.io.CustomSerializable;
import edu.uci.ics.sourcerer.util.io.SerializationUtils;


/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class Location implements CustomSerializable {
  private String classFile;
  private String path;
  private Integer offset;
  private Integer length;
  
  public Location(String classFile, String path, Integer offset, Integer length) {
    this.classFile = classFile;
    this.path = path;
    this.offset = offset;
    this.length = length;
  }
  
  public String getClassFile() {
    return classFile;
  }
  
  public String getPath() {
    return path;
  }
  
  public Integer getOffset() {
    return offset;
  }
  
  public Integer getLength() {
    return length;
  }

  @Override
  public String toString() {
    return classFile + "@" + offset + ":" + length + "@" + path;
  }
  
  protected static Location deserialize(Scanner scanner) {
    if (scanner.hasNextInt()) {
      if (scanner.nextInt() == 4) {
        String classFile = SerializationUtils.deserializeString(scanner);
        Integer offset = SerializationUtils.deserializeInteger(scanner);
        Integer length = SerializationUtils.deserializeInteger(scanner);
        String path = SerializationUtils.deserializeString(scanner);
        return new Location(classFile, path, offset, length);
      } else {
        throw new InputMismatchException();
      }
    } else {
      String next = scanner.next();
      if ("null".equals(next)) {
        return null;
      } else {
        throw new InputMismatchException();
      }
    }
  }
  
  @Override
  public String serialize() {
    return "4 " + SerializationUtils.serializeString(classFile) + " " + offset + " " + length + " " + SerializationUtils.serializeString(path);
  }
}