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

// CLASS *pkg*.InnerClassInsideClass
// INSIDE *pkg*.InnerClassInsideClass *pkg*

// CONSTRUCTOR *pkg*.InnerClassInsideClass.<init>()
// INSIDE *pkg*.InnerClassInsideClass.<init>() *pkg*.InnerClassInsideClass
// CALLS *pkg*.InnerClassInsideClass.<init>() java.lang.Object.<init>() -

// CLASS *pkg*.InnerClassInsideClass$Inner
// INSIDE *pkg*.InnerClassInsideClass$Inner *pkg*.InnerClassInsideClass

// CONSTRUCTOR *pkg*.InnerClassInsideClass$Inner.<init>()
// INSIDE *pkg*.InnerClassInsideClass$Inner.<init>() *pkg*.InnerClassInsideClass$Inner
// CALLS *pkg*.InnerClassInsideClass$Inner.<init>() java.lang.Object.<init>() -
package edu.uci.ics.sourcerer.extractor.test.inside;

public class InnerClassInsideClass {
  public class Inner {}
}
