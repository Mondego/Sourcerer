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

// CLASS *pkg*.LocalClassImplementsInterface
// INSIDE *pkg*.LocalClassImplementsInterface *pkg*

// CONSTRUCTOR *pkg*.LocalClassImplementsInterface.<init>()
// INSIDE *pkg*.LocalClassImplementsInterface.<init>() *pkg*.LocalClassImplementsInterface
// CALLS *pkg*.LocalClassImplementsInterface.<init>() java.lang.Object.<init>() -

// METHOD *pkg*.LocalClassImplementsInterface.method()
// INSIDE *pkg*.LocalClassImplementsInterface.method() *pkg*.LocalClassImplementsInterface
// RETURNS *pkg*.LocalClassImplementsInterface.method() void void
// USES *pkg*.LocalClassImplementsInterface.method() void void

// CLASS *pkg*.LocalClassImplementsInterface$local-1-Local
// INSIDE *pkg*.LocalClassImplementsInterface$local-1-Local *pkg*.LocalClassImplementsInterface.method()
// IMPLEMENTS *pkg*.LocalClassImplementsInterface$local-1-Local *pkg*.InterfaceToImplement InterfaceToImplement
// USES *pkg*.LocalClassImplementsInterface$local-1-Local *pkg*.InterfaceToImplement InterfaceToImplement

// CONSTRUCTOR *pkg*.LocalClassImplementsInterface$local-1-Local.<init>()
// INSIDE *pkg*.LocalClassImplementsInterface$local-1-Local.<init>() *pkg*.LocalClassImplementsInterface$local-1-Local
// CALLS *pkg*.LocalClassImplementsInterface$local-1-Local.<init>() java.lang.Object.<init>() -
package edu.uci.ics.sourcerer.extractor.test.implements_;

public class LocalClassImplementsInterface {
  public void method() {
    class Local implements InterfaceToImplement {}
  }
}
