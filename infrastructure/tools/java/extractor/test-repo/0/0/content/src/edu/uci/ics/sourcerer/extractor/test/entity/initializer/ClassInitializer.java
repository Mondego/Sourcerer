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

// CLASS public *pkg*.ClassInitializer public }
// INSIDE *pkg*.ClassInitializer *pkg*

// CONSTRUCTOR public *pkg*.ClassInitializer.<init>() -
// INSIDE *pkg*.ClassInitializer.<init>() *pkg*.ClassInitializer
// CALLS *pkg*.ClassInitializer.<init>() java.lang.Object.<init>() -

// INITIALIZER - *pkg*.ClassInitializer.initializer-1 { }
// INSIDE *pkg*.ClassInitializer.initializer-1 *pkg*.ClassInitializer

// INITIALIZER - *pkg*.ClassInitializer.initializer-2 /** }
// INSIDE *pkg*.ClassInitializer.initializer-2 *pkg*.ClassInitializer

// INITIALIZER static *pkg*.ClassInitializer.initializer-3 static }
// INSIDE *pkg*.ClassInitializer.initializer-3 *pkg*.ClassInitializer

// INITIALIZER static *pkg*.ClassInitializer.initializer-4 /** }
// INSIDE *pkg*.ClassInitializer.initializer-4 *pkg*.ClassInitializer
package edu.uci.ics.sourcerer.extractor.test.entity.initializer;

public class ClassInitializer {
  {}
  
  /**
   * Javadoc comments associate with initializers, though they probably shouldn't.
   */
  {}
  
  static {}
  
  /**
   * Javadoc comments associate with initializers, though they probably shouldn't.
   */
  static {}
}
