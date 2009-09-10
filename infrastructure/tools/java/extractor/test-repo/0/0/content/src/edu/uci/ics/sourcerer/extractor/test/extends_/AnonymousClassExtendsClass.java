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

// CLASS *pkg*.AnonymousClassExtendsClass
// INSIDE *pkg*.AnonymousClassExtendsClass *pkg*

// CONSTRUCTOR *pkg*.AnonymousClassExtendsClass.<init>()
// INSIDE *pkg*.AnonymousClassExtendsClass.<init>() *pkg*.AnonymousClassExtendsClass
// CALLS *pkg*.AnonymousClassExtendsClass.<init>() java.lang.Object.<init>() -

// METHOD *pkg*.AnonymousClassExtendsClass.method()
// INSIDE *pkg*.AnonymousClassExtendsClass.method() *pkg*.AnonymousClassExtendsClass
// RETURNS *pkg*.AnonymousClassExtendsClass.method() void void
// USES *pkg*.AnonymousClassExtendsClass.method() void void
// INSTANTIATES *pkg*.AnonymousClassExtendsClass.method() *pkg*.AnonymousClassExtendsClass$anonymous-1.<init>() ?
// USES *pkg*.AnonymousClassExtendsClass.method() *pkg*.ClassExtended ClassExtended

// CLASS *pkg*.AnonymousClassExtendsClass$anonymous-1
// INSIDE *pkg*.AnonymousClassExtendsClass$anonymous-1 *pkg*.AnonymousClassExtendsClass.method()
// EXTENDS *pkg*.AnonymousClassExtendsClass$anonymous-1 *pkg*.ClassExtended ClassExtended

// CONSTRUCTOR *pkg*.AnonymousClassExtendsClass$anonymous-1.<init>()
// INSIDE *pkg*.AnonymousClassExtendsClass$anonymous-1.<init>() *pkg*.AnonymousClassExtendsClass$anonymous-1
// CALLS *pkg*.AnonymousClassExtendsClass$anonymous-1.<init>() *pkg*.ClassExtended.<init>() -
package edu.uci.ics.sourcerer.extractor.test.extends_;

public class AnonymousClassExtendsClass {
  public void method() {
    new ClassExtended() {};
  }
}
