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
package edu.uci.ics.sourcerer.tools.java.model.extracted;

import edu.uci.ics.sourcerer.tools.java.model.types.Location;
import edu.uci.ics.sourcerer.tools.java.model.types.Relation;
import edu.uci.ics.sourcerer.util.io.SimpleSerializable;
import edu.uci.ics.sourcerer.util.io.arguments.Argument;
import edu.uci.ics.sourcerer.util.io.arguments.StringArgument;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public final class RelationEX implements SimpleSerializable {
  public static final Argument<String> RELATION_FILE = new StringArgument("relation-file", "relations.txt", "Filename for the extracted relations.").permit();
  
  private Relation type;
  private String lhs;
  private String rhs;
  private Location location;
  
  public RelationEX() {}
  
  public RelationEX(RelationEX relation) {
    type = relation.type;
    lhs = relation.lhs;
    rhs = relation.rhs;
    location = relation.location;
  }
  
  public RelationEX(Relation type, String lhs, String rhs, Location location) {
    this.type = type;
    this.lhs = lhs;
    this.rhs = rhs;
    this.location = location;
  }
  
  public RelationEX update(Relation type, String lhs, String rhs, Location location) {
    this.type = type;
    this.lhs = lhs;
    this.rhs = rhs;
    this.location = location;
    return this;
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
  
  public Location getLocation() {
    return location;
  }
  
  public String toString() {
    return type.name() + " " + lhs + " " + rhs;
  }
  
//  public int hashCode() {
//    return (lhs + rhs).hashCode();
//  }
//  
//  public boolean equals(Object o) {
//    if (o instanceof RelationEX) {
//      RelationEX other = (RelationEX)o;
//      if (type == Relation.PARAMETRIZED_BY) {
//        return type.equals(other.type) && lhs.equals(other.lhs) && startPos.equals(other.startPos) && rhs.equals(other.rhs) && length.equals(other.length) && paramPos.equals(other.paramPos) && path.equals(other.path);
//      } else if (type == Relation.INSIDE) {
//        return type.equals(other.type) && lhs.equals(other.lhs) && rhs.equals(other.rhs) && path.equals(other.path);
//      } else {
//        return type.equals(other.type) && lhs.equals(other.lhs) && startPos.equals(other.startPos) && rhs.equals(other.rhs) && length.equals(other.length) && path.equals(other.path);
//      }
//    } else {
//      return false;
//    }
//  }
}
