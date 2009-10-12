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

// CLASS public *pkg*.LocalClassExtendsInnerClass public }
// INSIDE *pkg*.LocalClassExtendsInnerClass *pkg*

// CONSTRUCTOR public *pkg*.LocalClassExtendsInnerClass.<init>() -
// INSIDE *pkg*.LocalClassExtendsInnerClass.<init>() *pkg*.LocalClassExtendsInnerClass
// CALLS *pkg*.LocalClassExtendsInnerClass.<init>() java.lang.Object.<init>() -

// METHOD public *pkg*.LocalClassExtendsInnerClass.method() public }
// INSIDE *pkg*.LocalClassExtendsInnerClass.method() *pkg*.LocalClassExtendsInnerClass
// RETURNS *pkg*.LocalClassExtendsInnerClass.method() void void
// USES *pkg*.LocalClassExtendsInnerClass.method() void void

// CLASS - *pkg*.LocalClassExtendsInnerClass$local-1-Local class }
// INSIDE *pkg*.LocalClassExtendsInnerClass$local-1-Local *pkg*.LocalClassExtendsInnerClass.method()
// EXTENDS *pkg*.LocalClassExtendsInnerClass$local-1-Local edu.uci.ics.sourcerer.extractor.test.ClassType$Inner ClassType.Inner
// USES *pkg*.LocalClassExtendsInnerClass$local-1-Local edu.uci.ics.sourcerer.extractor.test.ClassType ClassType
// USES *pkg*.LocalClassExtendsInnerClass$local-1-Local edu.uci.ics.sourcerer.extractor.test.ClassType$Inner Inner

// CONSTRUCTOR - *pkg*.LocalClassExtendsInnerClass$local-1-Local.<init>()
// INSIDE *pkg*.LocalClassExtendsInnerClass$local-1-Local.<init>() *pkg*.LocalClassExtendsInnerClass$local-1-Local
// CALLS *pkg*.LocalClassExtendsInnerClass$local-1-Local.<init>() edu.uci.ics.sourcerer.extractor.test.ClassType$Inner.<init>() -
package edu.uci.ics.sourcerer.extractor.test.relation.extends_;

import edu.uci.ics.sourcerer.extractor.test.ClassType;

public class LocalClassExtendsInnerClass {
  public void method() {
    class Local extends ClassType.Inner {}
  }
}
