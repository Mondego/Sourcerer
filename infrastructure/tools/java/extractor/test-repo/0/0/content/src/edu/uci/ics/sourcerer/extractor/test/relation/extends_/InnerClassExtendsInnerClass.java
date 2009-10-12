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

// CLASS public *pkg*.InnerClassExtendsInnerClass public }
// INSIDE *pkg*.InnerClassExtendsInnerClass *pkg*

// CONSTRUCTOR public *pkg*.InnerClassExtendsInnerClass.<init>() -
// INSIDE *pkg*.InnerClassExtendsInnerClass.<init>() *pkg*.InnerClassExtendsInnerClass
// CALLS *pkg*.InnerClassExtendsInnerClass.<init>() java.lang.Object.<init>() -

// CLASS public *pkg*.InnerClassExtendsInnerClass$Inner public }
// INSIDE *pkg*.InnerClassExtendsInnerClass$Inner *pkg*.InnerClassExtendsInnerClass
// EXTENDS *pkg*.InnerClassExtendsInnerClass$Inner edu.uci.ics.sourcerer.extractor.test.ClassType$Inner ClassType.Inner
// USES *pkg*.InnerClassExtendsInnerClass$Inner edu.uci.ics.sourcerer.extractor.test.ClassType ClassType
// USES *pkg*.InnerClassExtendsInnerClass$Inner edu.uci.ics.sourcerer.extractor.test.ClassType$Inner Inner

// CONSTRUCTOR public *pkg*.InnerClassExtendsInnerClass$Inner.<init>()
// INSIDE *pkg*.InnerClassExtendsInnerClass$Inner.<init>() *pkg*.InnerClassExtendsInnerClass$Inner
// CALLS *pkg*.InnerClassExtendsInnerClass$Inner.<init>() edu.uci.ics.sourcerer.extractor.test.ClassType$Inner.<init>() -
package edu.uci.ics.sourcerer.extractor.test.relation.extends_;

import edu.uci.ics.sourcerer.extractor.test.ClassType;

public class InnerClassExtendsInnerClass {
  public class Inner extends ClassType.Inner {}
}
