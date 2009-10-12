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

// ENUM public *pkg*.InnerClassInsideEnum
// INSIDE *pkg*.InnerClassInsideEnum *pkg*

// CONSTRUCTOR private *pkg*.InnerClassInsideEnum.<init>()
// INSIDE *pkg*.InnerClassInsideEnum.<init>() *pkg*.InnerClassInsideEnum

// CLASS public *pkg*.InnerClassInsideEnum$Inner public }
// INSIDE *pkg*.InnerClassInsideEnum$Inner *pkg*.InnerClassInsideEnum

// CONSTRUCTOR public *pkg*.InnerClassInsideEnum$Inner.<init>()
// INSIDE *pkg*.InnerClassInsideEnum$Inner.<init>() *pkg*.InnerClassInsideEnum$Inner
// CALLS *pkg*.InnerClassInsideEnum$Inner.<init>() java.lang.Object.<init>() -

// CLASS public *pkg*.InnerClassInsideEnum$InnerWithJavadoc /** }
// INSIDE *pkg*.InnerClassInsideEnum$InnerWithJavadoc *pkg*.InnerClassInsideEnum

// CONSTRUCTOR public *pkg*.InnerClassInsideEnum$InnerWithJavadoc.<init>()
// INSIDE *pkg*.InnerClassInsideEnum$InnerWithJavadoc.<init>() *pkg*.InnerClassInsideEnum$InnerWithJavadoc
// CALLS *pkg*.InnerClassInsideEnum$InnerWithJavadoc.<init>() java.lang.Object.<init>() -
package edu.uci.ics.sourcerer.extractor.test.entity.class_;

public enum InnerClassInsideEnum {
  ;
  public class Inner{}
  
  /**
   * Javadoc comment associated with an inner class.
   */
  public class InnerWithJavadoc {}
}
