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
package edu.uci.ics.sourcerer.db.tools;

import java.util.Collection;

import edu.uci.ics.sourcerer.util.Helper;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public final class TypeUtils {
  private TypeUtils() {}
  
  public static boolean isLowerBound(String fqn) {
    // <?+...>
    return fqn.charAt(2) == '-';
  }
  
  public static String getWildcardBound(String fqn) {
    // <?+...>
    return fqn.substring(3, fqn.length() - 1);
  }
  
  public static Collection<String> breakTypeVariable(String typeVariable) {
    Collection<String> parts = Helper.newLinkedList();
    
    StringBuilder builder = new StringBuilder();
    int depth = 0;
    boolean afterPlus = false;
    for (char c : typeVariable.toCharArray()) {
      if (depth == 0) {
        if (c == '<') {
          depth++;
        } else {
          throw new IllegalArgumentException(typeVariable + " is not a valid type variable");
        }
      } else if (depth == 1) {
        if (afterPlus) {
          if (c == '&') {
            parts.add(builder.toString());
            builder.setLength(0);
          } else if (c == '>') {
            depth--;
            parts.add(builder.toString());
          } else if (c == '<') {
            depth++;
            builder.append(c);
          } else {
            builder.append(c);
          }
        } else if (c == '+') {
          afterPlus = true;
        }
      } else {
        if (c == '<') {
          depth++;
        } else if (c == '>') {
          depth--;
        }
        builder.append(c);
      }
      
    }
   
    return parts;
  }
  
  public static String getBaseType(String parametrizedType) {
    StringBuilder builder = new StringBuilder();
    int depth = 0;
    for (char c : parametrizedType.toCharArray()) {
      if (c == '<') {
        depth++;
      } else if (c == '>') {
        depth--;
      } else if (depth == 0) {
        builder.append(c);
      }
    }
    return builder.toString();
  }
  
  public static Collection<String> breakParametrizedType(String fqn) {
    Collection<String> parts = Helper.newLinkedList();
    
    StringBuilder builder = new StringBuilder();
    int depth = 0;
    for (char c : fqn.toCharArray()) {
      if (depth == 0) {
        if (c == '<') {
          depth++;
        }
      } else if (depth == 1) {
        if (c == ',') {
          parts.add(builder.toString());
          builder.setLength(0);
        } else if (c == '>') {
          depth--;
          parts.add(builder.toString());
        } else if (c == '<') {
          depth++;
          builder.append(c);
        } else {
          builder.append(c);
        }
      } else {
        if (c == '<') {
          depth++;
        } else if (c == '>') {
          depth--;
        }
        builder.append(c);
      }
    }
    
    return parts;
  }
}
