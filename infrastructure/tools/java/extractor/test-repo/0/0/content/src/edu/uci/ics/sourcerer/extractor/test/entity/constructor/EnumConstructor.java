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

// ENUM public *pkg*.EnumConstructor public }
// INSIDE *pkg*.EnumConstructor *pkg*

// CONSTRUCTOR private *pkg*.EnumConstructor.<init>() private }
// INSIDE *pkg*.EnumConstructor.<init>() *pkg*.EnumConstructor

// CONSTRUCTOR private *pkg*.EnumConstructor.<init>(java.lang.Object) /** }
// INSIDE *pkg*.EnumConstructor.<init>(java.lang.Object) *pkg*.EnumConstructor
// PARAM arg java.lang.Object Object *pkg*.EnumConstructor.<init>(java.lang.Object) arg 0
// USES *pkg*.EnumConstructor.<init>(java.lang.Object) java.lang.Object Object
package edu.uci.ics.sourcerer.extractor.test.entity.constructor;

public enum EnumConstructor {
  ;
  private EnumConstructor() {}
  
  /**
   * Javadoc comment associated with constructor.
   */
  private EnumConstructor(Object arg) {}
}
