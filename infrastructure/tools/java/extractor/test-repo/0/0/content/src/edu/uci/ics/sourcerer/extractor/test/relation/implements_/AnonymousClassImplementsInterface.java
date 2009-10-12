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

// CLASS public *pkg*.AnonymousClassImplementsInterface public }
// INSIDE *pkg*.AnonymousClassImplementsInterface *pkg*

// CONSTRUCTOR public *pkg*.AnonymousClassImplementsInterface.<init>() -
// INSIDE *pkg*.AnonymousClassImplementsInterface.<init>() *pkg*.AnonymousClassImplementsInterface
// CALLS *pkg*.AnonymousClassImplementsInterface.<init>() java.lang.Object.<init>() -

// METHOD public *pkg*.AnonymousClassImplementsInterface.method() public }
// INSIDE *pkg*.AnonymousClassImplementsInterface.method() *pkg*.AnonymousClassImplementsInterface
// RETURNS *pkg*.AnonymousClassImplementsInterface.method() void void
// USES *pkg*.AnonymousClassImplementsInterface.method() void void
// INSTANTIATES *pkg*.AnonymousClassImplementsInterface.method() *pkg*.AnonymousClassImplementsInterface$anonymous-1.<init>() ?
// USES *pkg*.AnonymousClassImplementsInterface.method() edu.uci.ics.sourcerer.extractor.test.InterfaceType InterfaceType

// CLASS - *pkg*.AnonymousClassImplementsInterface$anonymous-1 { }
// INSIDE *pkg*.AnonymousClassImplementsInterface$anonymous-1 *pkg*.AnonymousClassImplementsInterface.method()
// IMPLEMENTS *pkg*.AnonymousClassImplementsInterface$anonymous-1 edu.uci.ics.sourcerer.extractor.test.InterfaceType InterfaceType

// CONSTRUCTOR - *pkg*.AnonymousClassImplementsInterface$anonymous-1.<init>() -
// INSIDE *pkg*.AnonymousClassImplementsInterface$anonymous-1.<init>() *pkg*.AnonymousClassImplementsInterface$anonymous-1
// CALLS *pkg*.AnonymousClassImplementsInterface$anonymous-1.<init>() java.lang.Object.<init>() -
package edu.uci.ics.sourcerer.extractor.test.relation.implements_;

import edu.uci.ics.sourcerer.extractor.test.InterfaceType;

public class AnonymousClassImplementsInterface {
  public void method() {
    new InterfaceType() {};
  }
}
