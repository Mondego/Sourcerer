/*
 * Sourcerer: An infrastructure for large-scale source code analysis.
 * Copyright (C) by contributors. See CONTRIBUTORS.txt for full list.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package edu.uci.ics.sourcerer.scs.common.client;

import java.io.Serializable;

/**
 * @author <a href="bajracharya@gmail.com">Sushil Bajracharya</a>
 * @created Jul 20, 2009
 */
public enum EntityType implements Serializable {
	PACKAGE, 
	CLASS, 
	INTERFACE, 
	CONSTRUCTOR, 
	METHOD, 
	INITIALIZER, 
	FIELD, 
	ENUM, 
	ENUM_CONSTANT, 
	ANNOTATION, 
	ANNOTATION_ELEMENT, 
	PRIMITIVE, 
	ARRAY, 
	TYPE_VARIABLE, 
	PARAMETERIZED_TYPE, 
	OTHER, // anything else than class, interface, method, constructor and field
	CLASS_INF, // class or interface
	METHOD_CONST, // method or constructor
	UNKNOWN
}
