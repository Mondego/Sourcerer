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

// CLASS public *pkg*.Fields public }
// INSIDE *pkg*.Fields *pkg*
// PARAMETRIZED_BY *pkg*.Fields <T> T

// CONSTRUCTOR public *pkg*.Fields.<init>() -
// INSIDE *pkg*.Fields.<init>() *pkg*.Fields
// CALLS *pkg*.Fields.<init>() java.lang.Object.<init>() -

// FIELD public *pkg*.Fields.classType class Type
// INSIDE *pkg*.Fields.classType *pkg*.Fields
// HOLDS *pkg*.Fields.classType edu.uci.ics.sourcerer.extractor.test.ClassType ClassType
// USES *pkg*.Fields.classType edu.uci.ics.sourcerer.extractor.test.ClassType ClassType

// FIELD public *pkg*.Fields.interfaceType interface Type
// INSIDE *pkg*.Fields.interfaceType *pkg*.Fields
// HOLDS *pkg*.Fields.interfaceType edu.uci.ics.sourcerer.extractor.test.InterfaceType InterfaceType
// USES *pkg*.Fields.interfaceType edu.uci.ics.sourcerer.extractor.test.InterfaceType InterfaceType

// FIELD public *pkg*.Fields.enumType enum Type
// INSIDE *pkg*.Fields.enumType *pkg*.Fields
// HOLDS *pkg*.Fields.enumType edu.uci.ics.sourcerer.extractor.test.EnumType EnumType
// USES *pkg*.Fields.enumType edu.uci.ics.sourcerer.extractor.test.EnumType EnumType

// FIELD public *pkg*.Fields.annotationType annotation Type
// INSIDE *pkg*.Fields.annotationType *pkg*.Fields
// HOLDS *pkg*.Fields.annotationType edu.uci.ics.sourcerer.extractor.test.AnnotationType AnnotationType
// USES *pkg*.Fields.annotationType edu.uci.ics.sourcerer.extractor.test.AnnotationType AnnotationType

// FIELD public *pkg*.Fields.primitiveType primitive Type
// INSIDE *pkg*.Fields.primitiveType *pkg*.Fields
// HOLDS *pkg*.Fields.primitiveType int int
// USES *pkg*.Fields.primitiveType int int

// FIELD public *pkg*.Fields.arrayType array Type
// INSIDE *pkg*.Fields.arrayType *pkg*.Fields
// HOLDS *pkg*.Fields.arrayType int[] int[]
// USES *pkg*.Fields.arrayType int[] int[]
// USES *pkg*.Fields.arrayType int int

// FIELD public *pkg*.Fields.typeVariableType type Type
// INSIDE *pkg*.Fields.typeVariableType *pkg*.Fields
// HOLDS *pkg*.Fields.typeVariableType <T> T
// USES *pkg*.Fields.typeVariableType <T> T

// FIELD public *pkg*.Fields.parametrizedType parametrized Type
// INSIDE *pkg*.Fields.parametrizedType *pkg*.Fields
// HOLDS *pkg*.Fields.parametrizedType edu.uci.ics.sourcerer.extractor.test.ParametrizedType<edu.uci.ics.sourcerer.extractor.test.ClassType> ParametrizedType<ClassType>
// USES *pkg*.Fields.parametrizedType edu.uci.ics.sourcerer.extractor.test.ParametrizedType<edu.uci.ics.sourcerer.extractor.test.ClassType> ParametrizedType<ClassType>
// USES *pkg*.Fields.parametrizedType edu.uci.ics.sourcerer.extractor.test.ParametrizedType ParametrizedType
// USES *pkg*.Fields.parametrizedType edu.uci.ics.sourcerer.extractor.test.ClassType ClassType
package edu.uci.ics.sourcerer.extractor.test.relation.holds;

import edu.uci.ics.sourcerer.extractor.test.AnnotationType;
import edu.uci.ics.sourcerer.extractor.test.ClassType;
import edu.uci.ics.sourcerer.extractor.test.EnumType;
import edu.uci.ics.sourcerer.extractor.test.InterfaceType;
import edu.uci.ics.sourcerer.extractor.test.ParametrizedType;

public class Fields <T> {
  public ClassType classType;
  public InterfaceType interfaceType;
  public EnumType enumType;
  public AnnotationType annotationType;
  public int primitiveType;
  public int[] arrayType;
  public T typeVariableType;
  public ParametrizedType<ClassType> parametrizedType;
}
