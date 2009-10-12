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

// CLASS public *pkg*.AnonymousClassExtendsInnerClass public }
// INSIDE *pkg*.AnonymousClassExtendsInnerClass *pkg*

// CONSTRUCTOR public *pkg*.AnonymousClassExtendsInnerClass.<init>() -
// INSIDE *pkg*.AnonymousClassExtendsInnerClass.<init>() *pkg*.AnonymousClassExtendsInnerClass
// CALLS *pkg*.AnonymousClassExtendsInnerClass.<init>() java.lang.Object.<init>() -

// METHOD public *pkg*.AnonymousClassExtendsInnerClass.method() public }
// INSIDE *pkg*.AnonymousClassExtendsInnerClass.method() *pkg*.AnonymousClassExtendsInnerClass
// RETURNS *pkg*.AnonymousClassExtendsInnerClass.method() void void
// USES *pkg*.AnonymousClassExtendsInnerClass.method() void void
// INSTANTIATES *pkg*.AnonymousClassExtendsInnerClass.method() *pkg*.AnonymousClassExtendsInnerClass$anonymous-1.<init>() ?
// USES *pkg*.AnonymousClassExtendsInnerClass.method() edu.uci.ics.sourcerer.extractor.test.ClassType ClassType
// USES *pkg*.AnonymousClassExtendsInnerClass.method() edu.uci.ics.sourcerer.extractor.test.ClassType$Inner Inner

// CLASS - *pkg*.AnonymousClassExtendsInnerClass$anonymous-1 { }
// INSIDE *pkg*.AnonymousClassExtendsInnerClass$anonymous-1 *pkg*.AnonymousClassExtendsInnerClass.method()
// EXTENDS *pkg*.AnonymousClassExtendsInnerClass$anonymous-1 edu.uci.ics.sourcerer.extractor.test.ClassType$Inner ClassType.Inner

// CONSTRUCTOR - *pkg*.AnonymousClassExtendsInnerClass$anonymous-1.<init>() -
// INSIDE *pkg*.AnonymousClassExtendsInnerClass$anonymous-1.<init>() *pkg*.AnonymousClassExtendsInnerClass$anonymous-1
// CALLS *pkg*.AnonymousClassExtendsInnerClass$anonymous-1.<init>() edu.uci.ics.sourcerer.extractor.test.ClassType$Inner.<init>() -
package edu.uci.ics.sourcerer.extractor.test.relation.extends_;

import edu.uci.ics.sourcerer.extractor.test.ClassType;

public class AnonymousClassExtendsInnerClass {
  public void method() {
    new ClassType.Inner() {};
  }
}
