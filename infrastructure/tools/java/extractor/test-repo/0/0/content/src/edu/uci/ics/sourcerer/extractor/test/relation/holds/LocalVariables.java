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

// CLASS public *pkg*.LocalVariables public }
// INSIDE *pkg*.LocalVariables *pkg*
// PARAMETRIZED_BY *pkg*.LocalVariables <T> T

// CONSTRUCTOR public *pkg*.LocalVariables.<init>() -
// INSIDE *pkg*.LocalVariables.<init>() *pkg*.LocalVariables
// CALLS *pkg*.LocalVariables.<init>() java.lang.Object.<init>() -

// METHOD public *pkg*.LocalVariables.method() public }
// INSIDE *pkg*.LocalVariables.method() *pkg*.LocalVariables
// RETURNS *pkg*.LocalVariables.method() void void
// USES *pkg*.LocalVariables.method() void void

// LOCAL classType edu.uci.ics.sourcerer.extractor.test.ClassType ClassType *pkg*.LocalVariables.method() classType
// USES *pkg*.LocalVariables.method() edu.uci.ics.sourcerer.extractor.test.ClassType ClassType

// LOCAL interfaceType edu.uci.ics.sourcerer.extractor.test.InterfaceType InterfaceType *pkg*.LocalVariables.method() interfaceType
// USES *pkg*.LocalVariables.method() edu.uci.ics.sourcerer.extractor.test.InterfaceType InterfaceType

// LOCAL enumType edu.uci.ics.sourcerer.extractor.test.EnumType EnumType *pkg*.LocalVariables.method() enumType
// USES *pkg*.LocalVariables.method() edu.uci.ics.sourcerer.extractor.test.EnumType EnumType

// LOCAL annotationType edu.uci.ics.sourcerer.extractor.test.AnnotationType AnnotationType *pkg*.LocalVariables.method() annotationType
// USES *pkg*.LocalVariables.method() edu.uci.ics.sourcerer.extractor.test.AnnotationType AnnotationType

// LOCAL primitiveType int int *pkg*.LocalVariables.method() primitiveType
// USES *pkg*.LocalVariables.method() int int

// LOCAL arrayType int[] int[] *pkg*.LocalVariables.method() arrayType
// USES *pkg*.LocalVariables.method() int[] int[]
// USES *pkg*.LocalVariables.method() int int

// LOCAL typeVariableType <T> T *pkg*.LocalVariables.method() typeVariableType
// USES *pkg*.LocalVariables.method() <T> T

// LOCAL parametrizedType edu.uci.ics.sourcerer.extractor.test.ParametrizedType<edu.uci.ics.sourcerer.extractor.test.ClassType> ParametrizedType<ClassType> *pkg*.LocalVariables.method() parametrizedType
// USES *pkg*.LocalVariables.method() edu.uci.ics.sourcerer.extractor.test.ParametrizedType<edu.uci.ics.sourcerer.extractor.test.ClassType> ParametrizedType<ClassType>
// USES *pkg*.LocalVariables.method() edu.uci.ics.sourcerer.extractor.test.ParametrizedType ParametrizedType
// USES *pkg*.LocalVariables.method() edu.uci.ics.sourcerer.extractor.test.ClassType ClassType
package edu.uci.ics.sourcerer.extractor.test.relation.holds;

import edu.uci.ics.sourcerer.extractor.test.AnnotationType;
import edu.uci.ics.sourcerer.extractor.test.ClassType;
import edu.uci.ics.sourcerer.extractor.test.EnumType;
import edu.uci.ics.sourcerer.extractor.test.InterfaceType;
import edu.uci.ics.sourcerer.extractor.test.ParametrizedType;

public class LocalVariables <T> {
  public void method() {
    ClassType classType;
    InterfaceType interfaceType;
    EnumType enumType;
    AnnotationType annotationType;
    int primitiveType;
    int[] arrayType;
    T typeVariableType;
    ParametrizedType<ClassType> parametrizedType;
  }
}
