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

// CLASS public *pkg*.LocalClassExtendsClass public }
// INSIDE *pkg*.LocalClassExtendsClass *pkg*

// CONSTRUCTOR public *pkg*.LocalClassExtendsClass.<init>() -
// INSIDE *pkg*.LocalClassExtendsClass.<init>() *pkg*.LocalClassExtendsClass
// CALLS *pkg*.LocalClassExtendsClass.<init>() java.lang.Object.<init>() -

// METHOD public *pkg*.LocalClassExtendsClass.method() public }
// INSIDE *pkg*.LocalClassExtendsClass.method() *pkg*.LocalClassExtendsClass
// RETURNS *pkg*.LocalClassExtendsClass.method() void void
// USES *pkg*.LocalClassExtendsClass.method() void void

// CLASS - *pkg*.LocalClassExtendsClass$local-1-Local class }
// INSIDE *pkg*.LocalClassExtendsClass$local-1-Local *pkg*.LocalClassExtendsClass.method()
// EXTENDS *pkg*.LocalClassExtendsClass$local-1-Local edu.uci.ics.sourcerer.extractor.test.ClassType ClassType
// USES *pkg*.LocalClassExtendsClass$local-1-Local edu.uci.ics.sourcerer.extractor.test.ClassType ClassType

// CONSTRUCTOR - *pkg*.LocalClassExtendsClass$local-1-Local.<init>()
// INSIDE *pkg*.LocalClassExtendsClass$local-1-Local.<init>() *pkg*.LocalClassExtendsClass$local-1-Local
// CALLS *pkg*.LocalClassExtendsClass$local-1-Local.<init>() edu.uci.ics.sourcerer.extractor.test.ClassType.<init>() -
package edu.uci.ics.sourcerer.extractor.test.relation.extends_;

import edu.uci.ics.sourcerer.extractor.test.ClassType;

public class LocalClassExtendsClass {
  public void method() {
    class Local extends ClassType {}
  }
}
