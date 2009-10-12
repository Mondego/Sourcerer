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

// CLASS public *pkg*.ClassConstructor public }
// INSIDE *pkg*.ClassConstructor *pkg*

// CONSTRUCTOR public *pkg*.ClassConstructor.<init>() public }
// INSIDE *pkg*.ClassConstructor.<init>() *pkg*.ClassConstructor
// CALLS *pkg*.ClassConstructor.<init>() java.lang.Object.<init>() -

// CONSTRUCTOR public *pkg*.ClassConstructor.<init>(java.lang.Object) /** }
// INSIDE *pkg*.ClassConstructor.<init>(java.lang.Object) *pkg*.ClassConstructor
// CALLS *pkg*.ClassConstructor.<init>(java.lang.Object) java.lang.Object.<init>() -
// PARAM arg java.lang.Object Object *pkg*.ClassConstructor.<init>(java.lang.Object) arg 0
// USES *pkg*.ClassConstructor.<init>(java.lang.Object) java.lang.Object Object
package edu.uci.ics.sourcerer.extractor.test.entity.constructor;

public class ClassConstructor {
  public ClassConstructor() {}
  
  /**
   * Javadoc comment associated with constructor.
   */
  public ClassConstructor(Object arg) {}
}
