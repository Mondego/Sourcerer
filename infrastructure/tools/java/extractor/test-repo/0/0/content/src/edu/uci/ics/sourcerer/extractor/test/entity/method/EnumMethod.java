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

// ENUM public *pkg*.EnumMethod public }
// INSIDE *pkg*.EnumMethod *pkg*

// CONSTRUCTOR private *pkg*.EnumMethod.<init>() -
// INSIDE *pkg*.EnumMethod.<init>() *pkg*.EnumMethod

// METHOD public *pkg*.EnumMethod.method() public }
// INSIDE *pkg*.EnumMethod.method() *pkg*.EnumMethod
// RETURNS *pkg*.EnumMethod.method() void void
// USES *pkg*.EnumMethod.method() void void

// METHOD public *pkg*.EnumMethod.methodWithJavadoc() /** }
// INSIDE *pkg*.EnumMethod.methodWithJavadoc() *pkg*.EnumMethod
// RETURNS *pkg*.EnumMethod.methodWithJavadoc() void void
// USES *pkg*.EnumMethod.methodWithJavadoc() void void
package edu.uci.ics.sourcerer.extractor.test.entity.method;

public enum EnumMethod {
  ;
  public void method() {}
  
  /**
   * Javadoc comment associated with method.
   */
  public void methodWithJavadoc() {}
}
