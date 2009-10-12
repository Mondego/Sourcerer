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

// CLASS public *pkg*.MethodParameter public }
// INSIDE *pkg*.MethodParameter *pkg*

// CONSTRUCTOR public *pkg*.MethodParameter.<init>() -
// INSIDE *pkg*.MethodParameter.<init>() *pkg*.MethodParameter
// CALLS *pkg*.MethodParameter.<init>() java.lang.Object.<init>() -

// METHOD public *pkg*.MethodParameter.method(java.lang.Object) public }
// INSIDE *pkg*.MethodParameter.method(java.lang.Object) *pkg*.MethodParameter
// RETURNS *pkg*.MethodParameter.method(java.lang.Object) void void
// USES *pkg*.MethodParameter.method(java.lang.Object) void void

// PARAM param java.lang.Object Object *pkg*.MethodParameter.method(java.lang.Object) param 0
// USES *pkg*.MethodParameter.method(java.lang.Object) java.lang.Object Object
package edu.uci.ics.sourcerer.extractor.test.entity.parameter;

public class MethodParameter {
  public void method(/** Definitely can't associate a Javadoc with a parameter. */Object param) {}
}
