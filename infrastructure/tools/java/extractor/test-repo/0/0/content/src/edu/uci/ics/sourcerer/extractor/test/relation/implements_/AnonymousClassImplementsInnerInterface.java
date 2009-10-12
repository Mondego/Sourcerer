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

// CLASS public *pkg*.AnonymousClassImplementsInnerInterface public }
// INSIDE *pkg*.AnonymousClassImplementsInnerInterface *pkg*

// CONSTRUCTOR public *pkg*.AnonymousClassImplementsInnerInterface.<init>() -
// INSIDE *pkg*.AnonymousClassImplementsInnerInterface.<init>() *pkg*.AnonymousClassImplementsInnerInterface
// CALLS *pkg*.AnonymousClassImplementsInnerInterface.<init>() java.lang.Object.<init>() -

// METHOD public *pkg*.AnonymousClassImplementsInnerInterface.method() public }
// INSIDE *pkg*.AnonymousClassImplementsInnerInterface.method() *pkg*.AnonymousClassImplementsInnerInterface
// RETURNS *pkg*.AnonymousClassImplementsInnerInterface.method() void void
// USES *pkg*.AnonymousClassImplementsInnerInterface.method() void void
// INSTANTIATES *pkg*.AnonymousClassImplementsInnerInterface.method() *pkg*.AnonymousClassImplementsInnerInterface$anonymous-1.<init>() ?
// USES *pkg*.AnonymousClassImplementsInnerInterface.method() edu.uci.ics.sourcerer.extractor.test.InterfaceType$Inner Inner
// USES *pkg*.AnonymousClassImplementsInnerInterface.method() edu.uci.ics.sourcerer.extractor.test.InterfaceType InterfaceType

// CLASS - *pkg*.AnonymousClassImplementsInnerInterface$anonymous-1 { }
// INSIDE *pkg*.AnonymousClassImplementsInnerInterface$anonymous-1 *pkg*.AnonymousClassImplementsInnerInterface.method()
// IMPLEMENTS *pkg*.AnonymousClassImplementsInnerInterface$anonymous-1 edu.uci.ics.sourcerer.extractor.test.InterfaceType$Inner InterfaceType.Inner

// CONSTRUCTOR - *pkg*.AnonymousClassImplementsInnerInterface$anonymous-1.<init>() -
// INSIDE *pkg*.AnonymousClassImplementsInnerInterface$anonymous-1.<init>() *pkg*.AnonymousClassImplementsInnerInterface$anonymous-1
// CALLS *pkg*.AnonymousClassImplementsInnerInterface$anonymous-1.<init>() java.lang.Object.<init>() -
package edu.uci.ics.sourcerer.extractor.test.relation.implements_;

import edu.uci.ics.sourcerer.extractor.test.InterfaceType;

public class AnonymousClassImplementsInnerInterface {
  public void method() {
    new InterfaceType.Inner() {};
  }
}
