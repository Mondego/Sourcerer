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

// ENUM public *pkg*.EnumInitializer public }
// INSIDE *pkg*.EnumInitializer *pkg*

// CONSTRUCTOR private *pkg*.EnumInitializer.<init>() -
// INSIDE *pkg*.EnumInitializer.<init>() *pkg*.EnumInitializer

// INITIALIZER - *pkg*.EnumInitializer.initializer-1 { }
// INSIDE *pkg*.EnumInitializer.initializer-1 *pkg*.EnumInitializer

// INITIALIZER - *pkg*.EnumInitializer.initializer-2 /** }
// INSIDE *pkg*.EnumInitializer.initializer-2 *pkg*.EnumInitializer

// INITIALIZER static *pkg*.EnumInitializer.initializer-3 static }
// INSIDE *pkg*.EnumInitializer.initializer-3 *pkg*.EnumInitializer

// INITIALIZER static *pkg*.EnumInitializer.initializer-4 /** }
// INSIDE *pkg*.EnumInitializer.initializer-4 *pkg*.EnumInitializer
package edu.uci.ics.sourcerer.extractor.test.entity.initializer;

public enum EnumInitializer {
  ;
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
