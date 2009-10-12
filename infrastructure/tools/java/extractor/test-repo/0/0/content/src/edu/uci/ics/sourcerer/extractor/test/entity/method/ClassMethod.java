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

// CLASS public *pkg*.ClassMethod public }
// INSIDE *pkg*.ClassMethod *pkg*

// CONSTRUCTOR public *pkg*.ClassMethod.<init>() -
// INSIDE *pkg*.ClassMethod.<init>() *pkg*.ClassMethod
// CALLS *pkg*.ClassMethod.<init>() java.lang.Object.<init>() -

// METHOD public *pkg*.ClassMethod.method() public }
// INSIDE *pkg*.ClassMethod.method() *pkg*.ClassMethod
// RETURNS *pkg*.ClassMethod.method() void void
// USES *pkg*.ClassMethod.method() void void

// METHOD public *pkg*.ClassMethod.methodWithJavadoc() /** }
// INSIDE *pkg*.ClassMethod.methodWithJavadoc() *pkg*.ClassMethod
// RETURNS *pkg*.ClassMethod.methodWithJavadoc() void void
// USES *pkg*.ClassMethod.methodWithJavadoc() void void

// METHOD protected-synchronized-strictfp-static *pkg*.ClassMethod.modifierMethod()
// INSIDE *pkg*.ClassMethod.modifierMethod() *pkg*.ClassMethod
// RETURNS *pkg*.ClassMethod.modifierMethod() void void
// USES *pkg*.ClassMethod.modifierMethod() void void
package edu.uci.ics.sourcerer.extractor.test.entity.method;

public class ClassMethod {
  public void method() {}
  
  /**
   * Javadoc comment associated with method.
   */
  public void methodWithJavadoc() {}
  
  protected synchronized strictfp static void modifierMethod() {}
}
