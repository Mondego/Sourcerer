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

import edu.uci.ics.sourcerer.model.Relation;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class RelationEX implements ModelEX {
  private Relation type;
  private String lhs;
  private String rhs;
  private String path;
  private String startPos;
  private String length;
  private String paramPos;
  private String paramName;
  
  private RelationEX(Relation type, String lhs, String rhs, String path) {
    this(type, lhs, rhs, path, null, null);
  }
  private RelationEX(Relation type, String lhs, String rhs, String path, String startPos, String length) {
    this.type = type;
    this.lhs = lhs;
    this.rhs = rhs;
    this.path = path;
    this.startPos = startPos;
    this.length = length;
  }
  
  private RelationEX(Relation type, String lhs, String rhs, String paramPos, String path, String startPos, String length) {
    this.type = type;
    this.lhs = lhs;
    this.rhs = rhs;
    this.path = path;
    this.startPos = startPos;
    this.length = length;
    this.paramPos = paramPos;
    this.paramName = rhs.substring(1, rhs.length() - 1);
  }
  
  public Relation getType() {
    return type;
  }

  public String getLhs() {
    return lhs;
  }

  public String getRhs() {
    return rhs;
  }
  
  public String getPath() {
    return path;
  }
  
  public String getStartPosition() {
    return startPos;
  }
  
  public String getLength() {
    return length;
  }
  
  public String getParamPos() {
    return paramPos;
  }
  
  public String getParamName() {
    return paramName;
  }
  
  public String toString() {
    return type.name() + " " + lhs + " " + rhs;
  }
  
  public int hashCode() {
    return (lhs + rhs).hashCode();
  }
  
  public boolean equals(Object o) {
    if (o instanceof RelationEX) {
      RelationEX other = (RelationEX)o;
      if (type == Relation.PARAMETRIZED_BY) {
        return type.equals(other.type) && lhs.equals(other.lhs) && startPos.equals(other.startPos) && rhs.equals(other.rhs) && length.equals(other.length) && paramPos.equals(other.paramPos) && path.equals(other.path);
      } else if (type == Relation.INSIDE) {
        return type.equals(other.type) && lhs.equals(other.lhs) && rhs.equals(other.rhs) && path.equals(other.path);
      } else {
        return type.equals(other.type) && lhs.equals(other.lhs) && startPos.equals(other.startPos) && rhs.equals(other.rhs) && length.equals(other.length) && path.equals(other.path);
      }
    } else {
      return false;
    }
  }
  
  // ---- PARSER ----
  private static ModelExParser<RelationEX> parser = new ModelExParser<RelationEX>() {
    @Override
    public RelationEX parseLine(String line) {
      String[] parts = line.split(" ");
      
      try {
        Relation type = Relation.valueOf(parts[0]);
        if (type == Relation.INSIDE) {
          return new RelationEX(type, parts[1], parts[2], parts[3]);
        } else if (type == Relation.PARAMETRIZED_BY) {
          if (parts.length == 5) {
            return new RelationEX(type, parts[1], parts[2], parts[3], parts[4], null, null);
          } else {
            return new RelationEX(type, parts[1], parts[2], parts[3], parts[4], parts[5], parts[6]);
          }
        } else {
          if (parts.length == 4) {
            return new RelationEX(type, parts[1], parts[2], parts[3]);
          } else {
            return new RelationEX(type, parts[1], parts[2], null, parts[3], parts[4], parts[5]);
          }
        }
      } catch (ArrayIndexOutOfBoundsException e) {
        logger.log(Level.SEVERE, "Unable to parse relation: " + line);
        return null;
      } catch (IllegalArgumentException e) {
        logger.log(Level.SEVERE, "Unable to parse relation: " + line);
        return null;
      }
    }
  };
  
  public static ModelExParser<RelationEX> getParser() {
    return parser;
  }
  
  public static String getSourceLine(Relation type, String lhs, String rhs, String path, int startPos, int length) {
    return type.name() + " " + lhs + " " + rhs + " " + path + " " + startPos + " " + length; 
  }
  
  public static String getSourceLineInside(String lhs, String rhs, String compilationUnitPath) {
    return Relation.INSIDE.name() + " " + lhs + " " + rhs + " " + compilationUnitPath;
  }
  
  public static String getSourceLineParametrizedBy(String lhs, String rhs, int position, String compilationUnitPath, int startPos, int length) {
    return Relation.PARAMETRIZED_BY.name() + " " + lhs + " " + rhs + " " + position + " " + compilationUnitPath + " " + startPos + " " + length;
  }
  
  public static String getClassLine(Relation type, String lhs, String rhs, String path) {
    return type.name() + " " + lhs + " " + rhs + " " + path;
  }
  
  public static String getClassLineParametrizedBy(String lhs, String rhs, int position, String path) {
    return Relation.PARAMETRIZED_BY + " " + lhs + " " + rhs + " " + position + " " + path;
  }
}
