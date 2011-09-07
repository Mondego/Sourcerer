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

import edu.uci.ics.sourcerer.tools.java.model.types.LocalVariable;
import edu.uci.ics.sourcerer.tools.java.model.types.Location;
import edu.uci.ics.sourcerer.tools.java.model.types.Modifiers;
import edu.uci.ics.sourcerer.util.io.SimpleSerializable;
import edu.uci.ics.sourcerer.util.io.arguments.Argument;
import edu.uci.ics.sourcerer.util.io.arguments.StringArgument;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public final class LocalVariableEX implements SimpleSerializable {
  public static final Argument<String> LOCAL_VARIABLE_FILE = new StringArgument("local-variables-file", "local-variables.txt", "Filename for the extracted local variables / parameters.").permit();
  
  private LocalVariable type;
  private String name;
  private Modifiers modifiers;
  private String typeFqn;
  private Location typeLocation;
  private String parent;
  private Integer position;
  private Location location;
  
  public LocalVariableEX() {}
  
  public LocalVariableEX(LocalVariableEX var) {
    this.type = var.type;
    this.name = var.name;
    this.modifiers = var.modifiers;
    this.typeFqn = var.typeFqn;
    this.typeLocation = var.typeLocation;
    this.parent = var.parent;
    this.position = var.position;
    this.location = var.location;
  }
  
  public LocalVariableEX(LocalVariable type, String name, int modifiers, String typeFqn, Location typeLocation, String parent, Integer position, Location location) {
    this.type = type;
    this.name = name;
    this.modifiers = Modifiers.make(modifiers);
    this.typeFqn = typeFqn;
    this.typeLocation = typeLocation;
    this.parent = parent;
    this.position = position;
    this.location = location;
  }
  
  public LocalVariableEX update(LocalVariable type, String name, int modifiers, String typeFqn, Location typeLocation, String parent, Integer position, Location location) {
    this.type = type;
    this.name = name;
    this.modifiers = Modifiers.make(modifiers);
    this.typeFqn = typeFqn;
    this.typeLocation = typeLocation;
    this.parent = parent;
    this.position = position;
    this.location = location;
    return this;
  }
  
  public LocalVariable getType() {
    return type;
  }

  public String getName() {
    return name;
  }

  public Modifiers getModifiers() {
    return modifiers;
  }

  public String getTypeFqn() {
    return typeFqn;
  }

  public Location getTypeLocation() {
    return typeLocation;
  }

  public String getParent() {
    return parent;
  }
  
  public Integer getPosition() {
    return position;
  }

  public Location getLocation() {
    return location;
  }
  
  public String toString() {
    return type.name() + " " + name + " " + parent;
  }
  
//  public boolean equals(Object o) {
//    if (this == o) {
//      return true;
//    } else if (o instanceof LocalVariableEX) {
//      LocalVariableEX other = (LocalVariableEX)o;
//      return type.equals(other.type) && name.equals(other.name) && parent.equals(other.parent) && typeFqn.equals(other.typeFqn) && typeStartPos.equals(other.typeStartPos) && typeLength.equals(other.typeLength) && path.equals(other.path) && startPos.equals(other.startPos) && length.equals(other.length);
//    } else {
//      return false;
//    }
//  }
//  
//  public int hashCode() {
//    return (name + parent).hashCode();
//  }
}
