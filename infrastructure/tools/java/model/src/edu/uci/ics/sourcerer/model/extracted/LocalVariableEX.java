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
package edu.uci.ics.sourcerer.model.extracted;

import edu.uci.ics.sourcerer.model.LocalVariable;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class LocalVariableEX implements ModelEX {
  private LocalVariable type;
  private String name;
  private String modifiers;
  private String typeFqn;
  private String typeStartPos;
  private String typeLength;
  private String parent;
  private String position;
  private String path;
  private String startPos;
  private String length;
  
  private LocalVariableEX(LocalVariable type, String name, String modifiers, String typeFqn, String typeStartPos, String typeLength, String parent, String position, String path, String startPos, String length) {
    this.type = type;
    this.name = name;
    this.modifiers = modifiers;
    this.typeFqn = typeFqn;
    this.typeStartPos = typeStartPos;
    this.typeLength = typeLength;
    this.parent = parent;
    this.position = position;
    this.path = path;
    this.startPos = startPos;
    this.length = length; 
  }
  
  public static LocalVariableEX getJarParam(String name, String type, String parent, String position) {
    return new LocalVariableEX(LocalVariable.PARAM, name, null, type, null, null, parent, position, null, null, null);
  }
  
  public static LocalVariableEX getParam(String name, String modifiers, String type, String typeStartPos, String typeLength, String parent, String position, String path, String startPos, String length) {
    return new LocalVariableEX(LocalVariable.PARAM, name, modifiers, type, typeStartPos, typeLength, parent, position, path, startPos, length);
  }
  
  public static LocalVariableEX getLocal(String name, String modifiers, String type, String typeStartPos, String typeLength, String parent, String path, String startPos, String length) {
    return new LocalVariableEX(LocalVariable.LOCAL, name, modifiers, type, typeStartPos, typeLength, parent, null, path, startPos, length);
  }
  
  public LocalVariable getType() {
    return type;
  }

  public String getName() {
    return name;
  }

  public String getModifiers() {
    return modifiers;
  }

  public String getTypeFqn() {
    return typeFqn;
  }

  public String getTypeStartPos() {
    return typeStartPos;
  }

  public String getTypeLength() {
    return typeLength;
  }

  public String getParent() {
    return parent;
  }
  
  public String getPosition() {
    return position;
  }

  public String getPath() {
    return path;
  }

  public String getStartPos() {
    return startPos;
  }

  public String getLength() {
    return length;
  }
  
  public String toString() {
    return type.name() + " " + name + " " + parent;
  }
  
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    } else if (o instanceof LocalVariableEX) {
      LocalVariableEX other = (LocalVariableEX)o;
      return type.equals(other.type) && name.equals(other.name) && parent.equals(other.parent) && typeFqn.equals(other.typeFqn) && typeStartPos.equals(other.typeStartPos) && typeLength.equals(other.typeLength) && path.equals(other.path) && startPos.equals(other.startPos) && length.equals(other.length);
    } else {
      return false;
    }
  }
  
  public int hashCode() {
    return (name + parent).hashCode();
  }
 }
