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
package edu.uci.ics.sourcerer.model.db;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class TypedEntityID {
  public enum Type {
    LIBRARY,
    JAR,
    SOURCE;
    
    public boolean isSource() {
      return this == SOURCE;
    }
    
    public boolean isJar() {
      return this == JAR;
    }
  }
  
  private Type type;
  private String id;
  
  private TypedEntityID(Type type, String id) {
    this.type = type;
    this.id = id;
  }
  
  public Type getType() {
    return type;
  }
  
  public String getID() {
    return id;
  }
  
  public void setID(String id) {
    this.id = id;
  }
  
  public static TypedEntityID getLibraryEntityID(String id) {
    return new TypedEntityID(Type.LIBRARY, id); 
  }
  
  public static TypedEntityID getJarEntityID(String id) {
    return new TypedEntityID(Type.JAR, id); 
  }
  
  public static TypedEntityID getSourceEntityID(String id) {
    return new TypedEntityID(Type.SOURCE, id); 
  }
  
  public static TypedEntityID getEntityID(Type type, String id) {
    return new TypedEntityID(Type.SOURCE, id);
  }
  
  public int hashCode() {
    return id.hashCode();
  }
  
  public boolean equals(Object o) {
    if (o instanceof TypedEntityID) {
      TypedEntityID other = (TypedEntityID)o;
      return type.equals(other.type) && id.equals(other.id);
    } else {
      return false;
    }
  }
  
  public String toString() {
    return type.name() + "(" + id + ")"; 
  }
}
