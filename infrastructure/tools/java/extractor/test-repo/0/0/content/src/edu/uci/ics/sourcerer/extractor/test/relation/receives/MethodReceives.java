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

// CLASS public *pkg*.MethodReceives public }
// INSIDE *pkg*.MethodReceives *pkg*

// CONSTRUCTOR public *pkg*.MethodReceives.<init>() -
// INSIDE *pkg*.MethodReceives.<init>() *pkg*.MethodReceives
// CALLS *pkg*.MethodReceives.<init>() java.lang.Object.<init>() -

// METHOD public *pkg*.MethodReceives.method(java.lang.Object) public }
// INSIDE *pkg*.MethodReceives.method(java.lang.Object) *pkg*.MethodReceives
// RETURNS *pkg*.MethodReceives.method(java.lang.Object) void void
// USES *pkg*.MethodReceives.method(java.lang.Object) void void
// PARAM param java.lang.Object Object *pkg*.MethodReceives.method(java.lang.Object) param 0
// USES *pkg*.MethodReceives.method(java.lang.Object) java.lang.Object Object
package edu.uci.ics.sourcerer.extractor.test.relation.receives;

public class MethodReceives {
  public void method(Object param) {}
}
