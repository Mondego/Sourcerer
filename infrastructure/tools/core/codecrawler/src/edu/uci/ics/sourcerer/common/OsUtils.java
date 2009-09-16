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
/***********************************************************
 * Author: Trung Ngo
 * Created on May 9, 2004
 * File: OsUtils.java
 ***********************************************************/
package edu.uci.ics.sourcerer.common;


/**
 * 
 * @author <a href="mailto:hahrot@yahoo.com">Huy A. Huynh</a>
 * 
 * NOTE: copied from older Sourcerer implementation by
 * @author <a href="mailto:trungngo@gmail.com">Trung Ngo</a>
 */
public class OsUtils {

	/** the line separator character */  
	public static final String LINE_SEPARATOR = System.getProperty("line.separator");


	/** the path separator character */
	public static final String PATH_SEPARATOR = System.getProperty("path.separator");

	/** TODO check dir separator */
	public static final String DIR_SEPARATOR = System.getProperty("file.separator");
	
	
	/** the name of the operating system */
	public static final String osName = System.getProperty("os.name"); 	


	/** the version of the operating system */
	public static final String osVersion = System.getProperty("os.version"); 	

	
	/**
	 * determines if the current operating system is in the Windows family 
	 * @return
	 */
	public static boolean isWindowsFamily() {
		// TESTME
		return osName.indexOf("Windows") > -1;
	}

	/**
	 * determines if the current operating system is in the Netware family 
	 * @return
	 */
	public static boolean isNetwareFamily() {
		// on windows returns false
        return osName.indexOf("netware") > -1;
	}
	
	
}
