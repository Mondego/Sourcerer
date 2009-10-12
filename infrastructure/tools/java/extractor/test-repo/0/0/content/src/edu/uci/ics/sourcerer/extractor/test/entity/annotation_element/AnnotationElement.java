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

// ANNOTATION public *pkg*.AnnotationElement public }
// INSIDE *pkg*.AnnotationElement *pkg*

// ANNOTATION_ELEMENT - *pkg*.AnnotationElement.element() String element(); 
// INSIDE *pkg*.AnnotationElement.element() *pkg*.AnnotationElement
// RETURNS *pkg*.AnnotationElement.element() java.lang.String String
// USES *pkg*.AnnotationElement.element() java.lang.String String

// ANNOTATION_ELEMENT - *pkg*.AnnotationElement.elementWithJavadoc() /** elementWithJavadoc();
// INSIDE *pkg*.AnnotationElement.elementWithJavadoc() *pkg*.AnnotationElement
// RETURNS *pkg*.AnnotationElement.elementWithJavadoc() java.lang.String String
// USES *pkg*.AnnotationElement.elementWithJavadoc() java.lang.String String
package edu.uci.ics.sourcerer.extractor.test.entity.annotation_element;

public @interface AnnotationElement {
  String element();
  
  /**
   * Javadoc associated with annotation element
   */
  String elementWithJavadoc();
}