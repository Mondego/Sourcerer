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
package edu.uci.ics.sourcerer.tools.java.model.types;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public enum Relation {
  CONTAINS,
  EXTENDS,
  IMPLEMENTS,
  HOLDS,
  RETURNS,
  READS,
  WRITES,
  CALLS,
  INSTANTIATES,
  THROWS,
  CASTS,
  CHECKS,
  ANNOTATED_BY,
  USES,
  HAS_ELEMENTS_OF,
  PARAMETRIZED_BY,
  HAS_BASE_TYPE,
  HAS_TYPE_ARGUMENT,
  HAS_UPPER_BOUND,
  HAS_LOWER_BOUND,
  OVERRIDES,
  MATCHES,
  ;
  
  public static Relation parse(String name) {
    if (name == null) {
      return null;
    } else {
      for (Relation relation : values()) {
        if (relation.name().equals(name)) {
          return relation;
        }
      }
      return null;
    }
  }
}