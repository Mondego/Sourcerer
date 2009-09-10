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

// CLASS *pkg*.InnerClassExtendsInnerClass
// INSIDE *pkg*.InnerClassExtendsInnerClass *pkg*

// CONSTRUCTOR *pkg*.InnerClassExtendsInnerClass.<init>()
// INSIDE *pkg*.InnerClassExtendsInnerClass.<init>() *pkg*.InnerClassExtendsInnerClass
// CALLS *pkg*.InnerClassExtendsInnerClass.<init>() java.lang.Object.<init>() -

// CLASS *pkg*.InnerClassExtendsInnerClass$Inner
// INSIDE *pkg*.InnerClassExtendsInnerClass$Inner *pkg*.InnerClassExtendsInnerClass
// EXTENDS *pkg*.InnerClassExtendsInnerClass$Inner *pkg*.InnerClassExtended$Inner InnerClassExtended.Inner
// USES *pkg*.InnerClassExtendsInnerClass$Inner *pkg*.InnerClassExtended InnerClassExtended
// USES *pkg*.InnerClassExtendsInnerClass$Inner *pkg*.InnerClassExtended$Inner Inner

// CONSTRUCTOR *pkg*.InnerClassExtendsInnerClass$Inner.<init>()
// INSIDE *pkg*.InnerClassExtendsInnerClass$Inner.<init>() *pkg*.InnerClassExtendsInnerClass$Inner
// CALLS *pkg*.InnerClassExtendsInnerClass$Inner.<init>() *pkg*.InnerClassExtended$Inner.<init>() -
package edu.uci.ics.sourcerer.extractor.test.extends_;

public class InnerClassExtendsInnerClass {
  public class Inner extends InnerClassExtended.Inner {}
}
