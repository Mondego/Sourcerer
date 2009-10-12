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

// ENUM public *pkg*.InnerEnumInsideEnum public }
// INSIDE *pkg*.InnerEnumInsideEnum *pkg*

// CONSTRUCTOR private *pkg*.InnerEnumInsideEnum.<init>() -
// INSIDE *pkg*.InnerEnumInsideEnum.<init>() *pkg*.InnerEnumInsideEnum

// ENUM public *pkg*.InnerEnumInsideEnum$Inner public }
// INSIDE *pkg*.InnerEnumInsideEnum$Inner *pkg*.InnerEnumInsideEnum

// CONSTRUCTOR private *pkg*.InnerEnumInsideEnum$Inner.<init>() - 
// INSIDE *pkg*.InnerEnumInsideEnum$Inner.<init>() *pkg*.InnerEnumInsideEnum$Inner

// ENUM public *pkg*.InnerEnumInsideEnum$InnerWithJavadoc /** }
// INSIDE *pkg*.InnerEnumInsideEnum$InnerWithJavadoc *pkg*.InnerEnumInsideEnum

// CONSTRUCTOR private *pkg*.InnerEnumInsideEnum$InnerWithJavadoc.<init>() -
// INSIDE *pkg*.InnerEnumInsideEnum$InnerWithJavadoc.<init>() *pkg*.InnerEnumInsideEnum$InnerWithJavadoc
package edu.uci.ics.sourcerer.extractor.test.entity.enum_;

public enum InnerEnumInsideEnum {
  ;
  public enum Inner {}
  
  /**
   * Javadoc comment associated with inner enum.
   */
  public enum InnerWithJavadoc {}
}
