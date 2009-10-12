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

// CLASS public *pkg*.LocalClassInsideMethod
// INSIDE *pkg*.LocalClassInsideMethod *pkg*

// CONSTRUCTOR public *pkg*.LocalClassInsideMethod.<init>()
// INSIDE *pkg*.LocalClassInsideMethod.<init>() *pkg*.LocalClassInsideMethod
// CALLS *pkg*.LocalClassInsideMethod.<init>() java.lang.Object.<init>() -

// METHOD public *pkg*.LocalClassInsideMethod.method()
// INSIDE *pkg*.LocalClassInsideMethod.method() *pkg*.LocalClassInsideMethod
// RETURNS *pkg*.LocalClassInsideMethod.method() void void
// USES *pkg*.LocalClassInsideMethod.method() void void

// CLASS - *pkg*.LocalClassInsideMethod$local-1-Local
// INSIDE *pkg*.LocalClassInsideMethod$local-1-Local *pkg*.LocalClassInsideMethod.method()

// CONSTRUCTOR - *pkg*.LocalClassInsideMethod$local-1-Local.<init>()
// INSIDE *pkg*.LocalClassInsideMethod$local-1-Local.<init>() *pkg*.LocalClassInsideMethod$local-1-Local
// CALLS *pkg*.LocalClassInsideMethod$local-1-Local.<init>() java.lang.Object.<init>() -

// CLASS - *pkg*.LocalClassInsideMethod$local-2-LocalWithJavadoc
// INSIDE *pkg*.LocalClassInsideMethod$local-2-LocalWithJavadoc *pkg*.LocalClassInsideMethod.method()

// CONSTRUCTOR - *pkg*.LocalClassInsideMethod$local-2-LocalWithJavadoc.<init>()
// INSIDE *pkg*.LocalClassInsideMethod$local-2-LocalWithJavadoc.<init>() *pkg*.LocalClassInsideMethod$local-2-LocalWithJavadoc
// CALLS *pkg*.LocalClassInsideMethod$local-2-LocalWithJavadoc.<init>() java.lang.Object.<init>() -
package edu.uci.ics.sourcerer.extractor.test.entity.class_;

public class LocalClassInsideMethod {
  public void method() {
    class Local {}
    
    /**
     * Javadoc comment associated with a local class.
     */
    class LocalWithJavadoc {}
  }
}
