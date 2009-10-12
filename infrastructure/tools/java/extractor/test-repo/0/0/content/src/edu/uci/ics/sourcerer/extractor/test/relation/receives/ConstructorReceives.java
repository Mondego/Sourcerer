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

// CLASS public *pkg*.ConstructorReceives public }
// INSIDE *pkg*.ConstructorReceives *pkg*

// CONSTRUCTOR public *pkg*.ConstructorReceives.<init>(java.lang.Object) public }
// INSIDE *pkg*.ConstructorReceives.<init>(java.lang.Object) *pkg*.ConstructorReceives
// CALLS *pkg*.ConstructorReceives.<init>(java.lang.Object) java.lang.Object.<init>() -
// PARAM param java.lang.Object Object *pkg*.ConstructorReceives.<init>(java.lang.Object) param 0
// USES *pkg*.ConstructorReceives.<init>(java.lang.Object) java.lang.Object Object
package edu.uci.ics.sourcerer.extractor.test.relation.receives;

public class ConstructorReceives {
  public ConstructorReceives(Object param) {}
}
