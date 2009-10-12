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

// CLASS public *pkg*.InitializerLocalVariable public }
// INSIDE *pkg*.InitializerLocalVariable *pkg*

// CONSTRUCTOR public *pkg*.InitializerLocalVariable.<init>() -
// INSIDE *pkg*.InitializerLocalVariable.<init>() *pkg*.InitializerLocalVariable
// CALLS *pkg*.InitializerLocalVariable.<init>() java.lang.Object.<init>() -

// INITIALIZER - *pkg*.InitializerLocalVariable.initializer-1 { }
// INSIDE *pkg*.InitializerLocalVariable.initializer-1 *pkg*.InitializerLocalVariable

// LOCAL local java.lang.Object Object *pkg*.InitializerLocalVariable.initializer-1 local
// USES *pkg*.InitializerLocalVariable.initializer-1 java.lang.Object Object
package edu.uci.ics.sourcerer.extractor.test.entity.local_variable;

public class InitializerLocalVariable {
  {
    /**
     * Javadoc comments cannot be associated with local variables.
     */
    Object local;
  }
}
