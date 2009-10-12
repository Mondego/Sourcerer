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

// CLASS *pkg*.Parameters
// INSIDE *pkg*.Parameters *pkg*
// PARAMETRIZED_BY *pkg*.Parameters <T> T

// CONSTRUCTOR *pkg*.Parameters.<init>()
// INSIDE *pkg*.Parameters.<init>() *pkg*.Parameters
// CALLS *pkg*.Parameters.<init>() java.lang.Object.<init>() -

// METHOD *pkg*.Parameters.method(*pkg*.ClassType,*pkg*.InterfaceType,*pkg*.EnumType,*pkg*.AnnotationType,int,int[],<T>,*pkg*.ParametrizedType<*pkg*.ClassType>)
// INSIDE *pkg*.Parameters.method(*pkg*.ClassType,*pkg*.InterfaceType,*pkg*.EnumType,*pkg*.AnnotationType,int,int[],<T>,*pkg*.ParametrizedType<*pkg*.ClassType>) *pkg*.Parameters
// RETURNS *pkg*.Parameters.method(*pkg*.ClassType,*pkg*.InterfaceType,*pkg*.EnumType,*pkg*.AnnotationType,int,int[],<T>,*pkg*.ParametrizedType<*pkg*.ClassType>) void void
// USES *pkg*.Parameters.method(*pkg*.ClassType,*pkg*.InterfaceType,*pkg*.EnumType,*pkg*.AnnotationType,int,int[],<T>,*pkg*.ParametrizedType<*pkg*.ClassType>) void void

// PARAM classType *pkg*.ClassType ClassType *pkg*.Parameters.method(*pkg*.ClassType,*pkg*.InterfaceType,*pkg*.EnumType,*pkg*.AnnotationType,int,int[],<T>,*pkg*.ParametrizedType<*pkg*.ClassType>) classType 0
// USES *pkg*.Parameters.method(*pkg*.ClassType,*pkg*.InterfaceType,*pkg*.EnumType,*pkg*.AnnotationType,int,int[],<T>,*pkg*.ParametrizedType<*pkg*.ClassType>) *pkg*.ClassType ClassType
// PARAM interfaceType *pkg*.InterfaceType InterfaceType *pkg*.Parameters.method(*pkg*.ClassType,*pkg*.InterfaceType,*pkg*.EnumType,*pkg*.AnnotationType,int,int[],<T>,*pkg*.ParametrizedType<*pkg*.ClassType>) interfaceType 1
// USES *pkg*.Parameters.method(*pkg*.ClassType,*pkg*.InterfaceType,*pkg*.EnumType,*pkg*.AnnotationType,int,int[],<T>,*pkg*.ParametrizedType<*pkg*.ClassType>) *pkg*.InterfaceType InterfaceType
// PARAM enumType *pkg*.EnumType EnumType *pkg*.Parameters.method(*pkg*.ClassType,*pkg*.InterfaceType,*pkg*.EnumType,*pkg*.AnnotationType,int,int[],<T>,*pkg*.ParametrizedType<*pkg*.ClassType>) enumType 2
// USES *pkg*.Parameters.method(*pkg*.ClassType,*pkg*.InterfaceType,*pkg*.EnumType,*pkg*.AnnotationType,int,int[],<T>,*pkg*.ParametrizedType<*pkg*.ClassType>) *pkg*.EnumType EnumType
// PARAM annotationType *pkg*.AnnotationType AnnotationType *pkg*.Parameters.method(*pkg*.ClassType,*pkg*.InterfaceType,*pkg*.EnumType,*pkg*.AnnotationType,int,int[],<T>,*pkg*.ParametrizedType<*pkg*.ClassType>) annotationType 3
// USES *pkg*.Parameters.method(*pkg*.ClassType,*pkg*.InterfaceType,*pkg*.EnumType,*pkg*.AnnotationType,int,int[],<T>,*pkg*.ParametrizedType<*pkg*.ClassType>) *pkg*.AnnotationType AnnotationType
// PARAM primitiveType int int *pkg*.Parameters.method(*pkg*.ClassType,*pkg*.InterfaceType,*pkg*.EnumType,*pkg*.AnnotationType,int,int[],<T>,*pkg*.ParametrizedType<*pkg*.ClassType>) primitiveType 4
// USES *pkg*.Parameters.method(*pkg*.ClassType,*pkg*.InterfaceType,*pkg*.EnumType,*pkg*.AnnotationType,int,int[],<T>,*pkg*.ParametrizedType<*pkg*.ClassType>) int int
// PARAM arrayType int[] int[] *pkg*.Parameters.method(*pkg*.ClassType,*pkg*.InterfaceType,*pkg*.EnumType,*pkg*.AnnotationType,int,int[],<T>,*pkg*.ParametrizedType<*pkg*.ClassType>) arrayType 5
// USES *pkg*.Parameters.method(*pkg*.ClassType,*pkg*.InterfaceType,*pkg*.EnumType,*pkg*.AnnotationType,int,int[],<T>,*pkg*.ParametrizedType<*pkg*.ClassType>) int int
// USES *pkg*.Parameters.method(*pkg*.ClassType,*pkg*.InterfaceType,*pkg*.EnumType,*pkg*.AnnotationType,int,int[],<T>,*pkg*.ParametrizedType<*pkg*.ClassType>) int[] int[]
// PARAM typeVariableType <T> T *pkg*.Parameters.method(*pkg*.ClassType,*pkg*.InterfaceType,*pkg*.EnumType,*pkg*.AnnotationType,int,int[],<T>,*pkg*.ParametrizedType<*pkg*.ClassType>) typeVariableType 6
// USES *pkg*.Parameters.method(*pkg*.ClassType,*pkg*.InterfaceType,*pkg*.EnumType,*pkg*.AnnotationType,int,int[],<T>,*pkg*.ParametrizedType<*pkg*.ClassType>) <T> T
// PARAM parametrizedType *pkg*.ParametrizedType<*pkg*.ClassType> ParametrizedType<ClassType> *pkg*.Parameters.method(*pkg*.ClassType,*pkg*.InterfaceType,*pkg*.EnumType,*pkg*.AnnotationType,int,int[],<T>,*pkg*.ParametrizedType<*pkg*.ClassType>) parametrizedType 7
// USES *pkg*.Parameters.method(*pkg*.ClassType,*pkg*.InterfaceType,*pkg*.EnumType,*pkg*.AnnotationType,int,int[],<T>,*pkg*.ParametrizedType<*pkg*.ClassType>) *pkg*.ParametrizedType<*pkg*.ClassType> ParametrizedType<ClassType>
// USES *pkg*.Parameters.method(*pkg*.ClassType,*pkg*.InterfaceType,*pkg*.EnumType,*pkg*.AnnotationType,int,int[],<T>,*pkg*.ParametrizedType<*pkg*.ClassType>) *pkg*.ParametrizedType ParametrizedType
// USES *pkg*.Parameters.method(*pkg*.ClassType,*pkg*.InterfaceType,*pkg*.EnumType,*pkg*.AnnotationType,int,int[],<T>,*pkg*.ParametrizedType<*pkg*.ClassType>) *pkg*.ClassType ClassType
package edu.uci.ics.sourcerer.extractor.test.holds;

public class Parameters <T> {
  public void method(ClassType classType, InterfaceType interfaceType, EnumType enumType, AnnotationType annotationType, int primitiveType, int[] arrayType, T typeVariableType, ParametrizedType<ClassType> parametrizedType) {}
}
