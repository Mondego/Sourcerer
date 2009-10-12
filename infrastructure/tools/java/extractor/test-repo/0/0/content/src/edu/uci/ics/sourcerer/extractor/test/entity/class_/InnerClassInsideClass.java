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

// CLASS public *pkg*.InnerClassInsideClass
// INSIDE *pkg*.InnerClassInsideClass *pkg*

// CONSTRUCTOR public *pkg*.InnerClassInsideClass.<init>()
// INSIDE *pkg*.InnerClassInsideClass.<init>() *pkg*.InnerClassInsideClass
// CALLS *pkg*.InnerClassInsideClass.<init>() java.lang.Object.<init>() -

// CLASS public *pkg*.InnerClassInsideClass$Inner public }
// INSIDE *pkg*.InnerClassInsideClass$Inner *pkg*.InnerClassInsideClass

// CONSTRUCTOR public *pkg*.InnerClassInsideClass$Inner.<init>()
// INSIDE *pkg*.InnerClassInsideClass$Inner.<init>() *pkg*.InnerClassInsideClass$Inner
// CALLS *pkg*.InnerClassInsideClass$Inner.<init>() java.lang.Object.<init>() -

// CLASS public *pkg*.InnerClassInsideClass$InnerWithJavadoc /** }
// INSIDE *pkg*.InnerClassInsideClass$InnerWithJavadoc *pkg*.InnerClassInsideClass

// CONSTRUCTOR public *pkg*.InnerClassInsideClass$InnerWithJavadoc.<init>()
// INSIDE *pkg*.InnerClassInsideClass$InnerWithJavadoc.<init>() *pkg*.InnerClassInsideClass$InnerWithJavadoc
// CALLS *pkg*.InnerClassInsideClass$InnerWithJavadoc.<init>() java.lang.Object.<init>() -
package edu.uci.ics.sourcerer.extractor.test.entity.class_;

public class InnerClassInsideClass {
  public class Inner {}
  
  /**
   * Javadoc comment associated with an inner class.
   */
  public class InnerWithJavadoc {}
}
