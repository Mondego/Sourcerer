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

// CLASS public *pkg*.InnerEnumInsideClass public }
// INSIDE *pkg*.InnerEnumInsideClass *pkg*

// CONSTRUCTOR public *pkg*.InnerEnumInsideClass.<init>() -
// INSIDE *pkg*.InnerEnumInsideClass.<init>() *pkg*.InnerEnumInsideClass
// CALLS *pkg*.InnerEnumInsideClass.<init>() java.lang.Object.<init>() -

// ENUM public *pkg*.InnerEnumInsideClass$Inner public }
// INSIDE *pkg*.InnerEnumInsideClass$Inner *pkg*.InnerEnumInsideClass

// CONSTRUCTOR private *pkg*.InnerEnumInsideClass$Inner.<init>() -
// INSIDE *pkg*.InnerEnumInsideClass$Inner.<init>() *pkg*.InnerEnumInsideClass$Inner

// ENUM public *pkg*.InnerEnumInsideClass$InnerWithJavadoc /** }
// INSIDE *pkg*.InnerEnumInsideClass$InnerWithJavadoc *pkg*.InnerEnumInsideClass

// CONSTRUCTOR private *pkg*.InnerEnumInsideClass$InnerWithJavadoc.<init>() -
// INSIDE *pkg*.InnerEnumInsideClass$InnerWithJavadoc.<init>() *pkg*.InnerEnumInsideClass$InnerWithJavadoc
package edu.uci.ics.sourcerer.extractor.test.entity.enum_;

public class InnerEnumInsideClass {
  public enum Inner {}
  
  /**
   * Javadoc comment associated with inner enum.
   */
  public enum InnerWithJavadoc {}
}
