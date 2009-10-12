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

/**
 * @author Joel Ossher (jossher@uci.edu)
 */

// BEGIN TEST

// INTERFACE public *pkg*.InnerEnumInsideInterface public }
// INSIDE *pkg*.InnerEnumInsideInterface *pkg*

// ENUM public *pkg*.InnerEnumInsideInterface$Inner public }
// INSIDE *pkg*.InnerEnumInsideInterface$Inner *pkg*.InnerEnumInsideInterface

// CONSTRUCTOR private *pkg*.InnerEnumInsideInterface$Inner.<init>() -
// INSIDE *pkg*.InnerEnumInsideInterface$Inner.<init>() *pkg*.InnerEnumInsideInterface$Inner

// ENUM public *pkg*.InnerEnumInsideInterface$InnerWithJavadoc /** }
// INSIDE *pkg*.InnerEnumInsideInterface$InnerWithJavadoc *pkg*.InnerEnumInsideInterface

// CONSTRUCTOR private *pkg*.InnerEnumInsideInterface$InnerWithJavadoc.<init>() -
// INSIDE *pkg*.InnerEnumInsideInterface$InnerWithJavadoc.<init>() *pkg*.InnerEnumInsideInterface$InnerWithJavadoc
package edu.uci.ics.sourcerer.extractor.test.entity.enum_;

public interface InnerEnumInsideInterface {
  public enum Inner {}
  
  /**
   * Javadoc comment associated with inner enum.
   */
  public enum InnerWithJavadoc {}
}
