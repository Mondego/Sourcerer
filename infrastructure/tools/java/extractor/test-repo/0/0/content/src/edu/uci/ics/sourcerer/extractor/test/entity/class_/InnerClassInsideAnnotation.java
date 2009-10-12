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

// ANNOTATION public *pkg*.InnerClassInsideAnnotation public }
// INSIDE *pkg*.InnerClassInsideAnnotation *pkg*

// CLASS public *pkg*.InnerClassInsideAnnotation$Inner public }
// INSIDE *pkg*.InnerClassInsideAnnotation$Inner *pkg*.InnerClassInsideAnnotation

// CONSTRUCTOR public *pkg*.InnerClassInsideAnnotation$Inner.<init>() -
// INSIDE *pkg*.InnerClassInsideAnnotation$Inner.<init>() *pkg*.InnerClassInsideAnnotation$Inner
// CALLS *pkg*.InnerClassInsideAnnotation$Inner.<init>() java.lang.Object.<init>() -

// CLASS public *pkg*.InnerClassInsideAnnotation$InnerWithJavadoc /** }
// INSIDE *pkg*.InnerClassInsideAnnotation$InnerWithJavadoc *pkg*.InnerClassInsideAnnotation

// CONSTRUCTOR public *pkg*.InnerClassInsideAnnotation$InnerWithJavadoc.<init>() -
// INSIDE *pkg*.InnerClassInsideAnnotation$InnerWithJavadoc.<init>() *pkg*.InnerClassInsideAnnotation$InnerWithJavadoc
// CALLS *pkg*.InnerClassInsideAnnotation$InnerWithJavadoc.<init>() java.lang.Object.<init>() -
package edu.uci.ics.sourcerer.extractor.test.entity.class_;

public @interface InnerClassInsideAnnotation {
  public class Inner {}
  
  /**
   * Javadoc comment associated with an inner class.
   */
  public class InnerWithJavadoc {}
}
