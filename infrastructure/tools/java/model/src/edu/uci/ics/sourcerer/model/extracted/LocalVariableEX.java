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
  private Integer modifiers;
  private String typeFqn;
  private Integer typeStartPos;
  private Integer typeLength;
  private String parent;
  private Integer position;
  private String path;
  private Integer startPos;
  private Integer length;
  
  private LocalVariableEX(LocalVariable type, String name, Integer modifiers, String typeFqn, Integer typeStartPos, Integer typeLength, String parent, Integer position, String path, Integer startPos, Integer length) {
    this.type = type;
    this.name = name;
    this.modifiers = modifiers;
    this.typeFqn = typeFqn;
    this.typeStartPos = typeStartPos;
    this.typeLength = typeLength;
    this.parent = parent;
    this.position = position;
    this.path = path;
    if (startPos != null && startPos >= 0) {
      this.startPos = startPos;
      this.length = length; 
    }
  }
  
  public LocalVariable getType() {
    return type;
  }

  public String getName() {
    return name;
  }

  public Integer getModifiers() {
    return modifiers;
  }

  public String getTypeFqn() {
    return typeFqn;
  }

  public Integer getTypeStartPos() {
    return typeStartPos;
  }

  public Integer getTypeLength() {
    return typeLength;
  }

  public String getParent() {
    return parent;
  }
  
  public Integer getPosition() {
    return position;
  }

  public String getPath() {
    return path;
  }

  public Integer getStartPos() {
    return startPos;
  }

  public Integer getLength() {
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
          if (parts.length == 6) {
            return new LocalVariableEX(type, parts[1], null, parts[2], null, null, parts[3], Integer.valueOf(parts[4]), parts[5], null, null);
          } else if (parts.length == 11) {
            return new LocalVariableEX(type, parts[1], Integer.valueOf(parts[2]), parts[3], Integer.valueOf(parts[4]), Integer.valueOf(parts[5]), parts[6], Integer.valueOf(parts[7]), parts[8], Integer.valueOf(parts[9]), Integer.valueOf(parts[10]));
          } else {
            logger.log(Level.SEVERE, "Unable to parse local variable: " + line);
            return null;
          }
        } else if (type == LocalVariable.LOCAL) {
          return new LocalVariableEX(type, parts[1], Integer.valueOf(parts[2]), parts[3], Integer.valueOf(parts[4]), Integer.valueOf(parts[5]), parts[6], null, parts[7], Integer.valueOf(parts[8]), Integer.valueOf(parts[9]));
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
  
  public static String getSourceLineParam(String name, int modifiers, String type, int typeStartPos, int typeLength, String parent, int position, String path, int startPos, int length) {
    return LocalVariable.PARAM + " " + name + " " + modifiers + " " + type + " " + typeStartPos + " " + typeLength + " " + parent + " " + position + " " + path + " " + startPos + " " + length;
  }
  
  public static String getSourceLineLocal(String name, int modifiers, String type, int typeStartPos, int typeLength, String parent, String path, int startPos, int length) {
    return LocalVariable.LOCAL + " " + name + " " + modifiers + " " + type + " " + typeStartPos + " " + typeLength + " " + parent + " " + path + " " + startPos + " " + length;
  }
  
  public static String getClassLineParam(String name, String type, String parent, int position, String path) {
    return LocalVariable.PARAM + " " + name + " " + type + " " + parent + " " + position + " " + path;
  }
}
