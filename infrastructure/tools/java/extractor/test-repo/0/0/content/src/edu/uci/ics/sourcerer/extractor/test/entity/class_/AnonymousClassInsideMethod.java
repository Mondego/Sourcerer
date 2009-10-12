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

// CLASS public *pkg*.AnonymousClassInsideMethod public }
// INSIDE *pkg*.AnonymousClassInsideMethod *pkg*

// CONSTRUCTOR public *pkg*.AnonymousClassInsideMethod.<init>() -
// INSIDE *pkg*.AnonymousClassInsideMethod.<init>() *pkg*.AnonymousClassInsideMethod
// CALLS *pkg*.AnonymousClassInsideMethod.<init>() java.lang.Object.<init>() -

// METHOD public *pkg*.AnonymousClassInsideMethod.method() public }
// INSIDE *pkg*.AnonymousClassInsideMethod.method() *pkg*.AnonymousClassInsideMethod
// RETURNS *pkg*.AnonymousClassInsideMethod.method() void void
// USES *pkg*.AnonymousClassInsideMethod.method() void void

// CLASS - *pkg*.AnonymousClassInsideMethod$anonymous-1 { }
// INSIDE *pkg*.AnonymousClassInsideMethod$anonymous-1 *pkg*.AnonymousClassInsideMethod.method()
// EXTENDS *pkg*.AnonymousClassInsideMethod$anonymous-1 java.lang.Object Object
// INSTANTIATES *pkg*.AnonymousClassInsideMethod.method() *pkg*.AnonymousClassInsideMethod$anonymous-1.<init>() ?
// USES *pkg*.AnonymousClassInsideMethod.method() java.lang.Object Object

// CONSTRUCTOR - *pkg*.AnonymousClassInsideMethod$anonymous-1.<init>() -
// INSIDE *pkg*.AnonymousClassInsideMethod$anonymous-1.<init>() *pkg*.AnonymousClassInsideMethod$anonymous-1
// CALLS *pkg*.AnonymousClassInsideMethod$anonymous-1.<init>() java.lang.Object.<init>() -
package edu.uci.ics.sourcerer.extractor.test.entity.class_;

public class AnonymousClassInsideMethod {
  public void method() {
    /**
     * Will a javadoc comment be associated with an anonymous class? No
     */
    new Object() {};
  }
}
