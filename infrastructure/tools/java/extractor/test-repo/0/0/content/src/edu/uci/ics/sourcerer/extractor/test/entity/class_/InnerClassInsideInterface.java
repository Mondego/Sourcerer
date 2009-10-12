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

// INTERFACE public *pkg*.InnerClassInsideInterface
// INSIDE *pkg*.InnerClassInsideInterface *pkg*

// CLASS public *pkg*.InnerClassInsideInterface$Inner
// INSIDE *pkg*.InnerClassInsideInterface$Inner *pkg*.InnerClassInsideInterface

// CONSTRUCTOR public *pkg*.InnerClassInsideInterface$Inner.<init>()
// INSIDE *pkg*.InnerClassInsideInterface$Inner.<init>() *pkg*.InnerClassInsideInterface$Inner
// CALLS *pkg*.InnerClassInsideInterface$Inner.<init>() java.lang.Object.<init>() -

// CLASS public *pkg*.InnerClassInsideInterface$InnerWithJavadoc /** }
// INSIDE *pkg*.InnerClassInsideInterface$InnerWithJavadoc *pkg*.InnerClassInsideInterface

// CONSTRUCTOR public *pkg*.InnerClassInsideInterface$InnerWithJavadoc.<init>()
// INSIDE *pkg*.InnerClassInsideInterface$InnerWithJavadoc.<init>() *pkg*.InnerClassInsideInterface$InnerWithJavadoc
// CALLS *pkg*.InnerClassInsideInterface$InnerWithJavadoc.<init>() java.lang.Object.<init>() -
package edu.uci.ics.sourcerer.extractor.test.entity.class_;

public interface InnerClassInsideInterface {
  public class Inner {}
  
  /**
   * Javadoc comment associated with an inner class.
   */
  public class InnerWithJavadoc {}
}
