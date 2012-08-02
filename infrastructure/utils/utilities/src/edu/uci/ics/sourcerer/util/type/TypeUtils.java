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
package edu.uci.ics.sourcerer.util.type;

import static edu.uci.ics.sourcerer.util.io.logging.Logging.logger;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import edu.uci.ics.sourcerer.util.Pair;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public final class TypeUtils {
  private TypeUtils() {}
  
  public static boolean isMethod(String fqn) {
    // ...(...)
    return fqn.contains("(") && fqn.endsWith(")");
  }
  
  public static Pair<String, String> breakMethod(String fqn) {
    int paren = fqn.indexOf('(');
    int dot = fqn.lastIndexOf('.', paren);
    return new Pair<>(fqn.substring(0, dot), fqn.substring(dot + 1));
  }
  
  public static Pair<String, String> breakParams(String fqn) {
    int paren = fqn.indexOf('(');
    return new Pair<>(fqn.substring(0, paren), fqn.substring(paren));
  }
  
  public static String getMethodName(String fqn) {
    return fqn.substring(0, fqn.indexOf('('));
  }
  
  public static String erase(String fqn) {
    StringBuilder result = new StringBuilder();
    
    int depth = 0;
    StringBuilder var = new StringBuilder();
    boolean beforeParen = true;
    for (char c : fqn.toCharArray()) {
      if (beforeParen) {
        if (c == '(') {
          beforeParen = false;
        }
        result.append(c);
      } else {
        if (depth == 0) {
          if (c == ',') {
            result.append(eraseParameter(var.toString())).append(',');
            var.setLength(0);
          } else if (c == ')') {
            if (var.length() > 0) {
              result.append(eraseParameter(var.toString()));
              var = null;
            }
            result.append(c);
          } else if (c == '<') {
            depth++;
            var.append(c);
          } else {
            var.append(c);
          }
        } else {
          if (c == '>') {
            depth--;
          } else if (c == '<') {
            depth++;
          }
          var.append(c);
        }
      }
    }
    return result.toString();
  }
  
  private static String eraseParameter(String var) {
    if (isArray(var)) {
      Pair<String, Integer> arr = breakArray(var);
      if (arr == null) {
        return null;
      }
      StringBuilder result = new StringBuilder();
      result.append(eraseParameter(arr.getFirst()));
      for (int i = 0, max = arr.getSecond(); i < max; i++) {
        result.append("[]");
      }
      return result.toString();
    } else if (isParametrizedType(var)) {
      return getBaseType(var);
    } else if (isTypeVariable(var)) {
      Collection<String> bounds = breakTypeVariable(var);
      if (bounds.isEmpty()) {
        return "java.lang.Object";
      } else {
        return eraseParameter(bounds.iterator().next());
      }
    } else {
      return var;
    }
  }
  
  public static boolean isArray(String fqn) {
    // ...[]
    return fqn.endsWith("[]");
  }
  
  public static Pair<String, Integer> breakArray(String fqn) {
    StringBuilder builder = new StringBuilder();
    int depth = 0;
    int dim = 0;
    boolean expectingClose = false;
    for (char c : fqn.toCharArray()) {
      if (depth == 0) {
        if (dim == 0) {
          if (c == '<') {
            depth++;
            builder.append(c);
          } else if (c == '[') {
            dim++;
            expectingClose = true;
          } else {
            builder.append(c);
          }
        } else {
          if (expectingClose) {
            if (c == ']') {
              expectingClose = false;
            } else {
              logger.severe(fqn + " is not a valid array type");
              return null;
            }
          } else {
            if (c == '[') {
              dim++;
              expectingClose = true;
            } else {
              logger.severe(fqn + " is not a valid array type");
              return null;
            }
          }
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
    return new Pair<String, Integer>(builder.toString(), dim);
  }
  
  public static boolean isWildcard(String fqn) {
    // <?...>
    return fqn.startsWith("<?") && fqn.endsWith(">");
  }
  
  public static boolean isUnboundedWildcard(String fqn) {
    // <?>
    return "<?>".equals(fqn);
  }
  
  public static boolean isLowerBound(String fqn) {
    // <?-...>
    return fqn.charAt(2) == '-';
  }
  
  public static String getWildcardBound(String fqn) {
    // <?+...>
    return fqn.substring(3, fqn.length() - 1);
  }
  
  public static boolean isTypeVariable(String fqn) {
    // <...>
    return fqn.startsWith("<") && fqn.endsWith(">");
  }
  
  public static Collection<String> breakTypeVariable(String typeVariable) {
    Collection<String> parts = new LinkedList<>();
    
    StringBuilder builder = new StringBuilder();
    int depth = 0;
    boolean afterPlus = false;
    for (char c : typeVariable.toCharArray()) {
      if (depth == 0) {
        if (c == '<') {
          depth++;
        } else {
          logger.severe(typeVariable + " is not a valid type variable");
          return Collections.emptyList();
        }
      } else if (depth == 1) {
        if (afterPlus) {
          if (c == '&') {
            parts.add(builder.toString());
            builder.setLength(0);
          } else if (c == '>') {
            depth--;
            parts.add(builder.toString());
            builder.setLength(0);
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
  
  public static boolean isParametrizedType(String fqn) {
    int baseIndex = fqn.indexOf('<');
    return baseIndex > 0 && fqn.charAt(fqn.length() - 1) == '>';
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
    Collection<String> parts = new LinkedList<>();
    
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
  
  public static int countParams(String params) {
    char[] arr = params.toCharArray();
    if (arr[0] != '(' || arr[arr.length - 1] != ')') {
      logger.severe("Invalid params: " + params);
      return 0;
    } else if (arr.length == 2) {
      return 0;
    } else {
      int count = 1;
      int depth = 0;
      for (int i = 1, max = arr.length - 1; i < max; i++) {
        switch (arr[i]) {
          case '<': depth++; break;
          case '>': depth--; break;
          case ',': if (depth == 0) count++; break;
        }
      }
      return count;
    }
  }
}
