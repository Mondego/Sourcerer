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

// CLASS public *pkg*.LocalClassInsideConstructor
// INSIDE *pkg*.LocalClassInsideConstructor *pkg*

// CONSTRUCTOR public *pkg*.LocalClassInsideConstructor.<init>()
// INSIDE *pkg*.LocalClassInsideConstructor.<init>() *pkg*.LocalClassInsideConstructor
// CALLS *pkg*.LocalClassInsideConstructor.<init>() java.lang.Object.<init>() -

// CLASS - *pkg*.LocalClassInsideConstructor$local-1-Local class }
// INSIDE *pkg*.LocalClassInsideConstructor$local-1-Local *pkg*.LocalClassInsideConstructor.<init>()

// CONSTRUCTOR - *pkg*.LocalClassInsideConstructor$local-1-Local.<init>()
// INSIDE *pkg*.LocalClassInsideConstructor$local-1-Local.<init>() *pkg*.LocalClassInsideConstructor$local-1-Local
// CALLS *pkg*.LocalClassInsideConstructor$local-1-Local.<init>() java.lang.Object.<init>() -

// CLASS - *pkg*.LocalClassInsideConstructor$local-2-LocalWithJavadoc /** }
// INSIDE *pkg*.LocalClassInsideConstructor$local-2-LocalWithJavadoc *pkg*.LocalClassInsideConstructor.<init>()

// CONSTRUCTOR - *pkg*.LocalClassInsideConstructor$local-2-LocalWithJavadoc.<init>()
// INSIDE *pkg*.LocalClassInsideConstructor$local-2-LocalWithJavadoc.<init>() *pkg*.LocalClassInsideConstructor$local-2-LocalWithJavadoc
// CALLS *pkg*.LocalClassInsideConstructor$local-2-LocalWithJavadoc.<init>() java.lang.Object.<init>() -
package edu.uci.ics.sourcerer.extractor.test.entity.class_;

public class LocalClassInsideConstructor {
  public LocalClassInsideConstructor() {
    class Local {}
    
    /**
     * Javadoc comment associated with a local class.
     */
    class LocalWithJavadoc {}
  }
}
