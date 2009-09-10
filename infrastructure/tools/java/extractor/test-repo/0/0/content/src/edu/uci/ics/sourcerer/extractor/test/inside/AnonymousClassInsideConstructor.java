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

// CLASS *pkg*.AnonymousClassInsideConstructor
// CONSTRUCTOR *pkg*.AnonymousClassInsideConstructor.<init>()
// CLASS *pkg*.AnonymousClassInsideConstructor$anonymous-1
// CONSTRUCTOR *pkg*.AnonymousClassInsideConstructor$anonymous-1.<init>()
// INSIDE *pkg*.AnonymousClassInsideConstructor *pkg*
// INSIDE *pkg*.AnonymousClassInsideConstructor.<init>() *pkg*.AnonymousClassInsideConstructor
// INSIDE *pkg*.AnonymousClassInsideConstructor$anonymous-1 *pkg*.AnonymousClassInsideConstructor.<init>()
// INSIDE *pkg*.AnonymousClassInsideConstructor$anonymous-1.<init>() *pkg*.AnonymousClassInsideConstructor$anonymous-1
// USES *pkg*.AnonymousClassInsideConstructor.<init>() java.lang.Object Object
// EXTENDS *pkg*.AnonymousClassInsideConstructor$anonymous-1 java.lang.Object Object
// INSTANTIATES *pkg*.AnonymousClassInsideConstructor.<init>() *pkg*.AnonymousClassInsideConstructor$anonymous-1.<init>() ?
// CALLS *pkg*.AnonymousClassInsideConstructor.<init>() java.lang.Object.<init>() -
// CALLS *pkg*.AnonymousClassInsideConstructor$anonymous-1.<init>() java.lang.Object.<init>() -

package edu.uci.ics.sourcerer.extractor.test.inside;

public class AnonymousClassInsideConstructor {
  public AnonymousClassInsideConstructor() {
    new Object() {};
  }
}
