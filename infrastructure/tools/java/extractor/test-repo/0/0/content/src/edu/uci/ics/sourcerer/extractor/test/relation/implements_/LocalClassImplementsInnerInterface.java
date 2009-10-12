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

// CLASS public *pkg*.LocalClassImplementsInnerInterface public }
// INSIDE *pkg*.LocalClassImplementsInnerInterface *pkg*

// CONSTRUCTOR public *pkg*.LocalClassImplementsInnerInterface.<init>() -
// INSIDE *pkg*.LocalClassImplementsInnerInterface.<init>() *pkg*.LocalClassImplementsInnerInterface
// CALLS *pkg*.LocalClassImplementsInnerInterface.<init>() java.lang.Object.<init>() -

// METHOD public *pkg*.LocalClassImplementsInnerInterface.method() public }
// INSIDE *pkg*.LocalClassImplementsInnerInterface.method() *pkg*.LocalClassImplementsInnerInterface
// RETURNS *pkg*.LocalClassImplementsInnerInterface.method() void void
// USES *pkg*.LocalClassImplementsInnerInterface.method() void void

// CLASS - *pkg*.LocalClassImplementsInnerInterface$local-1-Local class }
// INSIDE *pkg*.LocalClassImplementsInnerInterface$local-1-Local *pkg*.LocalClassImplementsInnerInterface.method()
// IMPLEMENTS *pkg*.LocalClassImplementsInnerInterface$local-1-Local edu.uci.ics.sourcerer.extractor.test.InterfaceType$Inner InterfaceType.Inner
// USES *pkg*.LocalClassImplementsInnerInterface$local-1-Local edu.uci.ics.sourcerer.extractor.test.InterfaceType$Inner Inner
// USES *pkg*.LocalClassImplementsInnerInterface$local-1-Local edu.uci.ics.sourcerer.extractor.test.InterfaceType InterfaceType

// CONSTRUCTOR - *pkg*.LocalClassImplementsInnerInterface$local-1-Local.<init>() -
// INSIDE *pkg*.LocalClassImplementsInnerInterface$local-1-Local.<init>() *pkg*.LocalClassImplementsInnerInterface$local-1-Local
// CALLS *pkg*.LocalClassImplementsInnerInterface$local-1-Local.<init>() java.lang.Object.<init>() -
package edu.uci.ics.sourcerer.extractor.test.relation.implements_;

import edu.uci.ics.sourcerer.extractor.test.InterfaceType;

public class LocalClassImplementsInnerInterface {
  public void method() {
    class Local implements InterfaceType.Inner {}
  }
}
