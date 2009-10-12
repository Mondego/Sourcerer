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

// CLASS public *pkg*.AnonymousClassInsideInitializer public }
// INSIDE *pkg*.AnonymousClassInsideInitializer *pkg*

// CONSTRUCTOR public *pkg*.AnonymousClassInsideInitializer.<init>() -
// INSIDE *pkg*.AnonymousClassInsideInitializer.<init>() *pkg*.AnonymousClassInsideInitializer
// CALLS *pkg*.AnonymousClassInsideInitializer.<init>() java.lang.Object.<init>() -

// INITIALIZER - *pkg*.AnonymousClassInsideInitializer.initializer-1 { }
// INSIDE *pkg*.AnonymousClassInsideInitializer.initializer-1 *pkg*.AnonymousClassInsideInitializer
// INSTANTIATES *pkg*.AnonymousClassInsideInitializer.initializer-1 *pkg*.AnonymousClassInsideInitializer$anonymous-1.<init>() ?
// USES *pkg*.AnonymousClassInsideInitializer.initializer-1 java.lang.Object Object

// CLASS - *pkg*.AnonymousClassInsideInitializer$anonymous-1 { }
// INSIDE *pkg*.AnonymousClassInsideInitializer$anonymous-1 *pkg*.AnonymousClassInsideInitializer.initializer-1
// EXTENDS *pkg*.AnonymousClassInsideInitializer$anonymous-1 java.lang.Object Object

// CONSTRUCTOR - *pkg*.AnonymousClassInsideInitializer$anonymous-1.<init>() -
// INSIDE *pkg*.AnonymousClassInsideInitializer$anonymous-1.<init>() *pkg*.AnonymousClassInsideInitializer$anonymous-1
// CALLS *pkg*.AnonymousClassInsideInitializer$anonymous-1.<init>() java.lang.Object.<init>() -
package edu.uci.ics.sourcerer.extractor.test.entity.class_;

public class AnonymousClassInsideInitializer {
  {
    /**
     * Will a javadoc comment be associated with an anonymous class? No
     */
    new Object() {};
  }
}
