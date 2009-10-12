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

// CLASS public *pkg*.ArrayFields public }
// INSIDE *pkg*.ArrayFields *pkg*

// CONSTRUCTOR public *pkg*.ArrayFields.<init>() - -
// INSIDE *pkg*.ArrayFields.<init>() *pkg*.ArrayFields
// CALLS *pkg*.ArrayFields.<init>() java.lang.Object.<init>() -

// FIELD - *pkg*.ArrayFields.one one one
// INSIDE *pkg*.ArrayFields.one *pkg*.ArrayFields
// HOLDS *pkg*.ArrayFields.one int[] int[]
// USES *pkg*.ArrayFields.one int[] int[]
// USES *pkg*.ArrayFields.one int int

// FIELD - *pkg*.ArrayFields.two two two
// INSIDE *pkg*.ArrayFields.two *pkg*.ArrayFields
// HOLDS *pkg*.ArrayFields.two int[][] int[][]
// USES *pkg*.ArrayFields.two int[][] int[][]
// USES *pkg*.ArrayFields.two int int
package edu.uci.ics.sourcerer.extractor.test.entity.array;

public class ArrayFields {
  int[] one;
  int[][] two;
}
