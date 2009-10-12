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

// ENUM public *pkg*.InnerAnnotationInsideEnum public }
// INSIDE *pkg*.InnerAnnotationInsideEnum *pkg*

// CONSTRUCTOR private *pkg*.InnerAnnotationInsideEnum.<init>() -
// INSIDE *pkg*.InnerAnnotationInsideEnum.<init>() *pkg*.InnerAnnotationInsideEnum

// ANNOTATION public *pkg*.InnerAnnotationInsideEnum$Inner public }
// INSIDE *pkg*.InnerAnnotationInsideEnum$Inner *pkg*.InnerAnnotationInsideEnum

// ANNOTATION public *pkg*.InnerAnnotationInsideEnum$InnerWithJavadoc /** }
// INSIDE *pkg*.InnerAnnotationInsideEnum$InnerWithJavadoc *pkg*.InnerAnnotationInsideEnum
package edu.uci.ics.sourcerer.extractor.test.entity.annotation;

public enum InnerAnnotationInsideEnum {
  ;
  public @interface Inner {}
  
  /**
   * Javadoc comment associated with an inner annotation
   */
  public @interface InnerWithJavadoc {}
}
