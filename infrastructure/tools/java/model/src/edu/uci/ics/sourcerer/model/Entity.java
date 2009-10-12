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

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public enum Entity {
    PACKAGE,
    CLASS,
    INTERFACE,
    ENUM,
    ANNOTATION,
    INITIALIZER,
    FIELD,
    ENUM_CONSTANT,
    CONSTRUCTOR,
    METHOD,
    ANNOTATION_ELEMENT,
    PARAMETER,
    LOCAL_VARIABLE,
    PRIMITIVE,
    ARRAY,
    TYPE_VARIABLE,
    WILDCARD,
    PARAMETERIZED_TYPE,
    DUPLICATE,
    UNKNOWN,
    ;

    public static Entity[] getLibraryValues() {
      Entity[] values = 
        { PACKAGE,
          CLASS,
          INTERFACE,
          ENUM,
          ANNOTATION,
          FIELD,
          ENUM_CONSTANT,
          CONSTRUCTOR,
          METHOD,
          PARAMETER,
          ANNOTATION_ELEMENT,
          PRIMITIVE,
          ARRAY,
          TYPE_VARIABLE,
          PARAMETERIZED_TYPE,
          WILDCARD,
          UNKNOWN };
      return values;
    }
    
    public static Entity[] getJarValues() {
      Entity[] values =
        { PACKAGE,
          CLASS,
          INTERFACE,
          ENUM,
          ANNOTATION,
          FIELD,
          ENUM_CONSTANT,
          CONSTRUCTOR,
          METHOD,
          PARAMETER,
          ANNOTATION_ELEMENT,
          ARRAY,
          TYPE_VARIABLE,
          PARAMETERIZED_TYPE,
          WILDCARD,
          UNKNOWN };
      return values;
    }
    
    public boolean isDeclaredType() {
      return this == CLASS || this == INTERFACE || this == ENUM || this == ANNOTATION;
    }

    public boolean isPackage() {
      return this == PACKAGE;
    }

    public boolean isAnnotation() {
      return this == ANNOTATION;
    }

    public boolean isInitializer() {
      return this == INITIALIZER;
    }
    
    public boolean isInterface() {
      return this == INTERFACE;
    }

    public boolean isEnum() {
      return this == ENUM;
    }

    public boolean isClass() {
      return this == CLASS;
    }

    public boolean isArray() {
      return this == ARRAY;
    }

    public boolean isParametrizedType() {
      return this == PARAMETERIZED_TYPE;
    }

    public boolean isCallableType() {
      return this == METHOD || this == CONSTRUCTOR;
    }

    public boolean isMethod() {
      return this == METHOD;
    }

    public boolean isConstructor() {
      return this == CONSTRUCTOR;
    }

    public boolean isUnknown() {
      return this == UNKNOWN;
    }

    public boolean isFieldImport() {
      return this == FIELD || this == ENUM_CONSTANT;
    }

    public boolean isPrimitive() {
      return this == PRIMITIVE;
    }

    public boolean isImportable() {
      return this != PRIMITIVE && this != UNKNOWN;
    }
    
    public static Entity parse(String name) {
      if (name == null) {
        return null;
      } else {
        for (Entity entity : values()) {
          if (entity.name().equals(name)) {
            return entity;
          }
        }
        return null;
      }
    }
    
    public static String getPossiblePackage(String fqn) {
      // unmethodify it
      int index = fqn.indexOf('(');
      if (index >= 0) {
        fqn = fqn.substring(0, index);
      }
      // unparametrized type it
      index = fqn.indexOf('<');
      if (index >= 0) {
        fqn = fqn.substring(0, index);
      }
      // get the potential package name
      index = fqn.lastIndexOf('.');
      if (index == -1) {
        return fqn;
      } else {
        return fqn.substring(0, index);
      }
    }
    

  }