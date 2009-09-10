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

// CLASS *pkg*.ClassExtendsInnerClass
// INSIDE *pkg*.ClassExtendsInnerClass *pkg*
// EXTENDS *pkg*.ClassExtendsInnerClass *pkg*.InnerClassExtended$Inner InnerClassExtended.Inner
// USES *pkg*.ClassExtendsInnerClass *pkg*.InnerClassExtended InnerClassExtended
// USES *pkg*.ClassExtendsInnerClass *pkg*.InnerClassExtended$Inner Inner

// CONSTRUCTOR *pkg*.ClassExtendsInnerClass.<init>()
// INSIDE *pkg*.ClassExtendsInnerClass.<init>() *pkg*.ClassExtendsInnerClass
// CALLS *pkg*.ClassExtendsInnerClass.<init>() *pkg*.InnerClassExtended$Inner.<init>() -
package edu.uci.ics.sourcerer.extractor.test.extends_;

public class ClassExtendsInnerClass extends InnerClassExtended.Inner {

}
