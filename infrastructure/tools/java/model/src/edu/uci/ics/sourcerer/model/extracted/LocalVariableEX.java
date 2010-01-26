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

import static edu.uci.ics.sourcerer.util.io.Logging.logger;

import java.util.logging.Level;

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
  
  // ---- PARSER ----
  private static ModelExParser<LocalVariableEX> parser = new ModelExParser<LocalVariableEX>() {
    @Override
    public LocalVariableEX parseLine(String line) {
      String[] parts = line.split(" ");
      
      try {
        LocalVariable type = LocalVariable.valueOf(parts[0]);
        if (type == LocalVariable.PARAM) {
          if (parts.length == 5) {
            return LocalVariableEX.getJarParam(parts[1], parts[2], parts[3], parts[4]);
          } else {
            return LocalVariableEX.getParam(parts[1], parts[2], parts[3], parts[4], parts[5], parts[6], parts[7], parts[8], parts[9], parts[10]);
          }
        } else if (type == LocalVariable.LOCAL) {
          return LocalVariableEX.getLocal(parts[1], parts[2], parts[3], parts[4], parts[5], parts[6], parts[7], parts[8], parts[9]);
        } else {
          logger.log(Level.SEVERE, "Unable to parse local variable: " + line);
          return null;
        }
      } catch (Exception e) {
        logger.log(Level.SEVERE, "Unable to parse local variable: " + line, e);
        return null;
      }
    }
  };
  
  public static ModelExParser<LocalVariableEX> getParser() {
    return parser;
  }
  
  public static String getLineParam(String name, int modifiers, String type, int typeStartPos, int typeLength, int position, String parent, String path, int startPos, int length) {
    return LocalVariable.PARAM + " " + name + " " + modifiers + " " + type + " " + typeStartPos + " " + typeLength + " " + parent + " " + position + " " + path + " " + startPos + " " + length;
  }
  
  public static String getLineLocal(String name, int modifiers, String type, int typeStartPos, int typeLength, String parent, String path, int startPos, int length) {
    return LocalVariable.LOCAL + " " + name + " " + modifiers + " " + type + " " + typeStartPos + " " + typeLength + " "+ parent + " " + path + " " + startPos + " " + length;
  }
  
  public static String getJarLineParam(String name, String type, String parent, int position) {
    return LocalVariable.PARAM + " " + name + " " + type + " " + parent + " " + position;
  }
}
