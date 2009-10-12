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

// CLASS public *pkg*.ConstructorParameter public }
// INSIDE *pkg*.ConstructorParameter *pkg*

// CONSTRUCTOR public *pkg*.ConstructorParameter.<init>(java.lang.Object) public }
// INSIDE *pkg*.ConstructorParameter.<init>(java.lang.Object) *pkg*.ConstructorParameter
// CALLS *pkg*.ConstructorParameter.<init>(java.lang.Object) java.lang.Object.<init>() -

// PARAM param java.lang.Object Object *pkg*.ConstructorParameter.<init>(java.lang.Object) param 0
// USES *pkg*.ConstructorParameter.<init>(java.lang.Object) java.lang.Object Object
package edu.uci.ics.sourcerer.extractor.test.entity.parameter;

public class ConstructorParameter {
  public ConstructorParameter(/** Definitely can't associate a Javadoc with a parameter. */Object param) {}
}
