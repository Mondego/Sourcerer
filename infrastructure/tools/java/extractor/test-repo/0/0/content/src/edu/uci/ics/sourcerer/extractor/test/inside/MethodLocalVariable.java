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

// CLASS *pkg*.MethodLocalVariable
// INSIDE *pkg*.MethodLocalVariable *pkg*

// CONSTRUCTOR *pkg*.MethodLocalVariable.<init>()
// INSIDE *pkg*.MethodLocalVariable.<init>() *pkg*.MethodLocalVariable
// CALLS *pkg*.MethodLocalVariable.<init>() java.lang.Object.<init>() -

// METHOD *pkg*.MethodLocalVariable.method()
// INSIDE *pkg*.MethodLocalVariable.method() *pkg*.MethodLocalVariable
// RETURNS *pkg*.MethodLocalVariable.method() void void
// USES *pkg*.MethodLocalVariable.method() void void
// LOCAL local java.lang.Object Object *pkg*.MethodLocalVariable.method() local
// USES *pkg*.MethodLocalVariable.method() java.lang.Object Object
package edu.uci.ics.sourcerer.extractor.test.inside;

public class MethodLocalVariable {
  public void method() {
    Object local;
  }
}
