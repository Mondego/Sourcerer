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

// CLASS public *pkg*.AnonymousClassInsideConstructor public }
// INSIDE *pkg*.AnonymousClassInsideConstructor *pkg*

// CONSTRUCTOR public *pkg*.AnonymousClassInsideConstructor.<init>() public }
// INSIDE *pkg*.AnonymousClassInsideConstructor.<init>() *pkg*.AnonymousClassInsideConstructor
// USES *pkg*.AnonymousClassInsideConstructor.<init>() java.lang.Object Object
// CALLS *pkg*.AnonymousClassInsideConstructor.<init>() java.lang.Object.<init>() -
// INSTANTIATES *pkg*.AnonymousClassInsideConstructor.<init>() *pkg*.AnonymousClassInsideConstructor$anonymous-1.<init>() ?

// CLASS - *pkg*.AnonymousClassInsideConstructor$anonymous-1 { }
// INSIDE *pkg*.AnonymousClassInsideConstructor$anonymous-1 *pkg*.AnonymousClassInsideConstructor.<init>()
// EXTENDS *pkg*.AnonymousClassInsideConstructor$anonymous-1 java.lang.Object Object

// CONSTRUCTOR - *pkg*.AnonymousClassInsideConstructor$anonymous-1.<init>()
// INSIDE *pkg*.AnonymousClassInsideConstructor$anonymous-1.<init>() *pkg*.AnonymousClassInsideConstructor$anonymous-1
// CALLS *pkg*.AnonymousClassInsideConstructor$anonymous-1.<init>() java.lang.Object.<init>() -
package edu.uci.ics.sourcerer.extractor.test.entity.class_;

public class AnonymousClassInsideConstructor {
  public AnonymousClassInsideConstructor() {
    /**
     * Will a javadoc comment be associated with an anonymous class? No
     */
    new Object() {};
  }
}
