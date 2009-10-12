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

// CLASS public *pkg*.InnerAnnotationInsideClass public }
// INSIDE *pkg*.InnerAnnotationInsideClass *pkg*

// CONSTRUCTOR public *pkg*.InnerAnnotationInsideClass.<init>() -
// INSIDE *pkg*.InnerAnnotationInsideClass.<init>() *pkg*.InnerAnnotationInsideClass
// CALLS *pkg*.InnerAnnotationInsideClass.<init>() java.lang.Object.<init>() -

// ANNOTATION public *pkg*.InnerAnnotationInsideClass$Inner public }
// INSIDE *pkg*.InnerAnnotationInsideClass$Inner *pkg*.InnerAnnotationInsideClass

// ANNOTATION public *pkg*.InnerAnnotationInsideClass$InnerWithJavadoc /** }
// INSIDE *pkg*.InnerAnnotationInsideClass$InnerWithJavadoc *pkg*.InnerAnnotationInsideClass
package edu.uci.ics.sourcerer.extractor.test.entity.annotation;

public class InnerAnnotationInsideClass {
  public @interface Inner {}
  
  /**
   * Javadoc comment associated with an inner annotation
   */
  public @interface InnerWithJavadoc {}
}
