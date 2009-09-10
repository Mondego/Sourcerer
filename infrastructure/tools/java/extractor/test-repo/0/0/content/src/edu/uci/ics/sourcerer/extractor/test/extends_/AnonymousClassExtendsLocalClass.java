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

// CLASS *pkg*.AnonymousClassExtendsLocalClass
// INSIDE *pkg*.AnonymousClassExtendsLocalClass *pkg*

// CONSTRUCTOR *pkg*.AnonymousClassExtendsLocalClass.<init>()
// INSIDE *pkg*.AnonymousClassExtendsLocalClass.<init>() *pkg*.AnonymousClassExtendsLocalClass
// CALLS *pkg*.AnonymousClassExtendsLocalClass.<init>() java.lang.Object.<init>() -

// METHOD *pkg*.AnonymousClassExtendsLocalClass.method()
// INSIDE *pkg*.AnonymousClassExtendsLocalClass.method() *pkg*.AnonymousClassExtendsLocalClass
// RETURNS *pkg*.AnonymousClassExtendsLocalClass.method() void void
// USES *pkg*.AnonymousClassExtendsLocalClass.method() void void
// INSTANTIATES *pkg*.AnonymousClassExtendsLocalClass.method() *pkg*.AnonymousClassExtendsLocalClass$anonymous-1.<init>() ?
// USES *pkg*.AnonymousClassExtendsLocalClass.method() *pkg*.AnonymousClassExtendsLocalClass$local-1-Local Local

// CLASS *pkg*.AnonymousClassExtendsLocalClass$local-1-Local
// INSIDE *pkg*.AnonymousClassExtendsLocalClass$local-1-Local *pkg*.AnonymousClassExtendsLocalClass.method()

// CONSTRUCTOR *pkg*.AnonymousClassExtendsLocalClass$local-1-Local.<init>()
// INSIDE *pkg*.AnonymousClassExtendsLocalClass$local-1-Local.<init>() *pkg*.AnonymousClassExtendsLocalClass$local-1-Local
// CALLS *pkg*.AnonymousClassExtendsLocalClass$local-1-Local.<init>() java.lang.Object.<init>() -

// CLASS *pkg*.AnonymousClassExtendsLocalClass$anonymous-1
// INSIDE *pkg*.AnonymousClassExtendsLocalClass$anonymous-1 *pkg*.AnonymousClassExtendsLocalClass.method()
// EXTENDS *pkg*.AnonymousClassExtendsLocalClass$anonymous-1 *pkg*.AnonymousClassExtendsLocalClass$local-1-Local Local

// CONSTRUCTOR *pkg*.AnonymousClassExtendsLocalClass$anonymous-1.<init>()
// INSIDE *pkg*.AnonymousClassExtendsLocalClass$anonymous-1.<init>() *pkg*.AnonymousClassExtendsLocalClass$anonymous-1
// CALLS *pkg*.AnonymousClassExtendsLocalClass$anonymous-1.<init>() *pkg*.AnonymousClassExtendsLocalClass$local-1-Local.<init>() -
package edu.uci.ics.sourcerer.extractor.test.extends_;

public class AnonymousClassExtendsLocalClass {
  public void method() {
    class Local {}
    new Local() {};
  }
}
