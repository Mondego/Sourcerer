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

// CLASS *pkg*.InnerClassExtended
// INSIDE *pkg*.InnerClassExtended *pkg*

// CONSTRUCTOR *pkg*.InnerClassExtended.<init>()
// INSIDE *pkg*.InnerClassExtended.<init>() *pkg*.InnerClassExtended
// CALLS *pkg*.InnerClassExtended.<init>() java.lang.Object.<init>() -

// CLASS *pkg*.InnerClassExtended$Inner
// INSIDE *pkg*.InnerClassExtended$Inner *pkg*.InnerClassExtended

// CONSTRUCTOR *pkg*.InnerClassExtended$Inner.<init>()
// INSIDE *pkg*.InnerClassExtended$Inner.<init>() *pkg*.InnerClassExtended$Inner
// CALLS *pkg*.InnerClassExtended$Inner.<init>() java.lang.Object.<init>() -
package edu.uci.ics.sourcerer.extractor.test.extends_;

public class InnerClassExtended {
  public static class Inner{}
}
