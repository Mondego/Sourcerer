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

// CLASS public *pkg*.LocalClassExtendsLocalClass public }
// INSIDE *pkg*.LocalClassExtendsLocalClass *pkg*

// CONSTRUCTOR public *pkg*.LocalClassExtendsLocalClass.<init>() -
// INSIDE *pkg*.LocalClassExtendsLocalClass.<init>() *pkg*.LocalClassExtendsLocalClass
// CALLS *pkg*.LocalClassExtendsLocalClass.<init>() java.lang.Object.<init>() -

// METHOD public *pkg*.LocalClassExtendsLocalClass.method() public }
// INSIDE *pkg*.LocalClassExtendsLocalClass.method() *pkg*.LocalClassExtendsLocalClass
// RETURNS *pkg*.LocalClassExtendsLocalClass.method() void void
// USES *pkg*.LocalClassExtendsLocalClass.method() void void

// CLASS - *pkg*.LocalClassExtendsLocalClass$local-1-Extended class }
// INSIDE *pkg*.LocalClassExtendsLocalClass$local-1-Extended *pkg*.LocalClassExtendsLocalClass.method()

// CONSTRUCTOR - *pkg*.LocalClassExtendsLocalClass$local-1-Extended.<init>() -
// INSIDE *pkg*.LocalClassExtendsLocalClass$local-1-Extended.<init>() *pkg*.LocalClassExtendsLocalClass$local-1-Extended
// CALLS *pkg*.LocalClassExtendsLocalClass$local-1-Extended.<init>() java.lang.Object.<init>() -

// CLASS - *pkg*.LocalClassExtendsLocalClass$local-2-Local class }
// INSIDE *pkg*.LocalClassExtendsLocalClass$local-2-Local *pkg*.LocalClassExtendsLocalClass.method()
// EXTENDS *pkg*.LocalClassExtendsLocalClass$local-2-Local *pkg*.LocalClassExtendsLocalClass$local-1-Extended Extended
// USES *pkg*.LocalClassExtendsLocalClass$local-2-Local *pkg*.LocalClassExtendsLocalClass$local-1-Extended Extended

// CONSTRUCTOR - *pkg*.LocalClassExtendsLocalClass$local-2-Local.<init>() -
// INSIDE *pkg*.LocalClassExtendsLocalClass$local-2-Local.<init>() *pkg*.LocalClassExtendsLocalClass$local-2-Local
// CALLS *pkg*.LocalClassExtendsLocalClass$local-2-Local.<init>() *pkg*.LocalClassExtendsLocalClass$local-1-Extended.<init>() -
package edu.uci.ics.sourcerer.extractor.test.relation.extends_;

public class LocalClassExtendsLocalClass {
  public void method() {
    class Extended {}
    class Local extends Extended {}
  }
}
