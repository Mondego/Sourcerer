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
package edu.uci.ics.sourcerer.tools.java.highlighter;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public enum TagType {
  METHOD_LINK,
  TYPE_LINK,
  IMPORT_LINK,
  FIELD_LINK,
  COLOR,
  ;
  
  public boolean isLinkType() {
    return this == METHOD_LINK || this == TYPE_LINK || this == IMPORT_LINK || this == FIELD_LINK;
  }
}
