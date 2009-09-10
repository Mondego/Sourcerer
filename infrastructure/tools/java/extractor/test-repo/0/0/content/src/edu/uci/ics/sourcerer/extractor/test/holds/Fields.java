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

// CLASS *pkg*.Fields
// INSIDE *pkg*.Fields *pkg*
// PARAMETRIZED_BY *pkg*.Fields <T> T

// CONSTRUCTOR *pkg*.Fields.<init>()
// INSIDE *pkg*.Fields.<init>() *pkg*.Fields
// CALLS *pkg*.Fields.<init>() java.lang.Object.<init>() -

// FIELD *pkg*.Fields.classType
// INSIDE *pkg*.Fields.classType *pkg*.Fields
// HOLDS *pkg*.Fields.classType *pkg*.ClassType ClassType
// USES *pkg*.Fields.classType *pkg*.ClassType ClassType

// FIELD *pkg*.Fields.interfaceType
// INSIDE *pkg*.Fields.interfaceType *pkg*.Fields
// HOLDS *pkg*.Fields.interfaceType *pkg*.InterfaceType InterfaceType
// USES *pkg*.Fields.interfaceType *pkg*.InterfaceType InterfaceType

// FIELD *pkg*.Fields.enumType
// INSIDE *pkg*.Fields.enumType *pkg*.Fields
// HOLDS *pkg*.Fields.enumType *pkg*.EnumType EnumType
// USES *pkg*.Fields.enumType *pkg*.EnumType EnumType

// FIELD *pkg*.Fields.annotationType
// INSIDE *pkg*.Fields.annotationType *pkg*.Fields
// HOLDS *pkg*.Fields.annotationType *pkg*.AnnotationType AnnotationType
// USES *pkg*.Fields.annotationType *pkg*.AnnotationType AnnotationType

// FIELD *pkg*.Fields.primitiveType
// INSIDE *pkg*.Fields.primitiveType *pkg*.Fields
// HOLDS *pkg*.Fields.primitiveType int int
// USES *pkg*.Fields.primitiveType int int

// FIELD *pkg*.Fields.arrayType
// INSIDE *pkg*.Fields.arrayType *pkg*.Fields
// HOLDS *pkg*.Fields.arrayType int[] int[]
// USES *pkg*.Fields.arrayType int[] int[]
// USES *pkg*.Fields.arrayType int int

// FIELD *pkg*.Fields.typeVariableType
// INSIDE *pkg*.Fields.typeVariableType *pkg*.Fields
// HOLDS *pkg*.Fields.typeVariableType <T> T
// USES *pkg*.Fields.typeVariableType <T> T

// FIELD *pkg*.Fields.parametrizedType
// INSIDE *pkg*.Fields.parametrizedType *pkg*.Fields
// HOLDS *pkg*.Fields.parametrizedType *pkg*.ParametrizedType<*pkg*.ClassType> ParametrizedType<ClassType>
// USES *pkg*.Fields.parametrizedType *pkg*.ParametrizedType<*pkg*.ClassType> ParametrizedType<ClassType>
// USES *pkg*.Fields.parametrizedType *pkg*.ParametrizedType ParametrizedType
// USES *pkg*.Fields.parametrizedType *pkg*.ClassType ClassType
package edu.uci.ics.sourcerer.extractor.test.holds;

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
