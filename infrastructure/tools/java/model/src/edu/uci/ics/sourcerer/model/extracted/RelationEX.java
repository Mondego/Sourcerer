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

import edu.uci.ics.sourcerer.model.Relation;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class RelationEX implements ModelEX {
  private Relation type;
  private String lhs;
  private String rhs;
  private String compilationUnitPath;
  private String startPos;
  private String length;
  private String paramPos;
  private String paramName;
  
  private RelationEX(Relation type, String lhs, String rhs, String compilationUnitPath, String startPos, String length) {
    this.type = type;
    this.lhs = lhs;
    this.rhs = rhs;
    this.compilationUnitPath = compilationUnitPath;
    this.startPos = startPos;
    this.length = length;
  }
  
  private RelationEX(Relation type, String lhs, String rhs, String paramPos, String compilationUnitPath, String startPos, String length) {
    this.type = type;
    this.lhs = lhs;
    this.rhs = rhs;
    this.compilationUnitPath = compilationUnitPath;
    this.startPos = startPos;
    this.length = length;
    this.paramPos = paramPos;
    this.paramName = rhs.substring(1, rhs.length() - 1);
  }
  
  protected static RelationEX getJarRelation(Relation type, String lhs, String rhs) {
    return new RelationEX(type, lhs, rhs, null, null, null);
  }
  
  protected static RelationEX getJarParametrizedByRelation(String lhs, String rhs, String pos) {
    return new RelationEX(Relation.PARAMETRIZED_BY, lhs, rhs, pos, null, null, null);
  }
  
  protected static RelationEX getRelation(Relation type, String lhs, String rhs, String compilationUnitPath, String startPos, String length) {
    return new RelationEX(type, lhs, rhs, compilationUnitPath, startPos, length);
  }
  
  protected static RelationEX getInsideRelation(String lhs, String rhs, String compilationUnitPath) {
    return new RelationEX(Relation.INSIDE, lhs, rhs, compilationUnitPath, null, null);
  }
  
  protected static RelationEX getParametrizedByRelation(String lhs, String rhs, String position, String compilationUnitPath, String startPos, String length) {
    return new RelationEX(Relation.PARAMETRIZED_BY, lhs, rhs, position, compilationUnitPath, startPos, length);
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
    return compilationUnitPath;
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
  
  public int hashCode() {
    return (lhs + rhs).hashCode();
  }
  
  public boolean equals(Object o) {
    if (o instanceof RelationEX) {
      RelationEX other = (RelationEX)o;
      if (type == Relation.PARAMETRIZED_BY) {
        return type.equals(other.type) && lhs.equals(other.lhs) && startPos.equals(other.startPos) && rhs.equals(other.rhs) && length.equals(other.length) && paramPos.equals(other.paramPos) && compilationUnitPath.equals(other.compilationUnitPath);
      } else if (type == Relation.INSIDE) {
        return type.equals(other.type) && lhs.equals(other.lhs) && rhs.equals(other.rhs) && compilationUnitPath.equals(other.compilationUnitPath);
      } else {
        return type.equals(other.type) && lhs.equals(other.lhs) && startPos.equals(other.startPos) && rhs.equals(other.rhs) && length.equals(other.length) && compilationUnitPath.equals(other.compilationUnitPath);
      }
    } else {
      return false;
    }
  }
}
