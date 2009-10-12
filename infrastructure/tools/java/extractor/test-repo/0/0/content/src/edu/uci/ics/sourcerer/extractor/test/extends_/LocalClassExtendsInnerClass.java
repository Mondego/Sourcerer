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

// CLASS *pkg*.LocalClassExtendsInnerClass
// INSIDE *pkg*.LocalClassExtendsInnerClass *pkg*

// CONSTRUCTOR *pkg*.LocalClassExtendsInnerClass.<init>()
// INSIDE *pkg*.LocalClassExtendsInnerClass.<init>() *pkg*.LocalClassExtendsInnerClass
// CALLS *pkg*.LocalClassExtendsInnerClass.<init>() java.lang.Object.<init>() -

// METHOD *pkg*.LocalClassExtendsInnerClass.method()
// INSIDE *pkg*.LocalClassExtendsInnerClass.method() *pkg*.LocalClassExtendsInnerClass
// RETURNS *pkg*.LocalClassExtendsInnerClass.method() void void
// USES *pkg*.LocalClassExtendsInnerClass.method() void void

// CLASS *pkg*.LocalClassExtendsInnerClass$local-1-Local
// INSIDE *pkg*.LocalClassExtendsInnerClass$local-1-Local *pkg*.LocalClassExtendsInnerClass.method()
// EXTENDS *pkg*.LocalClassExtendsInnerClass$local-1-Local *pkg*.InnerClassExtended$Inner InnerClassExtended.Inner
// USES *pkg*.LocalClassExtendsInnerClass$local-1-Local *pkg*.InnerClassExtended InnerClassExtended
// USES *pkg*.LocalClassExtendsInnerClass$local-1-Local *pkg*.InnerClassExtended$Inner Inner

// CONSTRUCTOR *pkg*.LocalClassExtendsInnerClass$local-1-Local.<init>()
// INSIDE *pkg*.LocalClassExtendsInnerClass$local-1-Local.<init>() *pkg*.LocalClassExtendsInnerClass$local-1-Local
// CALLS *pkg*.LocalClassExtendsInnerClass$local-1-Local.<init>() *pkg*.InnerClassExtended$Inner.<init>() -
package edu.uci.ics.sourcerer.extractor.test.extends_;

public class LocalClassExtendsInnerClass {
  public void method() {
    class Local extends InnerClassExtended.Inner {}
  }
}
