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
 * Created on Oct 18, 2003
 * File: LoggerUtils.java
 * 
 * 2007-02-27, cvl, updated to use Logger instead of Category, which has been deprecated
 ***********************************************************/
package edu.uci.ics.sourcerer.common;

import org.apache.log4j.Logger;

/**
 * 
 * @author <a href="mailto:hahrot@yahoo.com">Huy A. Huynh</a>
 * 
 * NOTE: copied from older Sourcerer implementation by
 * @author <a href="mailto:trungngo@gmail.com">Trung Ngo</a>
 */
public class LoggerUtils {

	/**
	 * construct a new instance of LoggerUtils 
	 */
	public LoggerUtils() {
		super();
	}
	
	/**
	 * get the root category for logging
	 * the log file is defined in log4j.properties file
	 * @return
	 */
	public static Logger getRootLogger() {
		
		Logger logger = Logger.getRootLogger();
		return logger;
	}
	
	
	
	/**
	 * checks if debugging is enabled for the given object 
	 * @param source an object
	 * @return true if debugging is enabled, otherwise returns false 
	 */
	public static boolean isDebugEnabled(Class c) {
		return Logger.getLogger(c).isDebugEnabled();
	}
	
	/**
	 * log an exception to the log database 
	 * @param source the source object which calls this method
	 * @param ex an exception to be logged
	 */
	public static void logException(Object source, Exception ex) {
		Logger logger = Logger.getLogger(source.getClass());
		logger.error("---- EXCEPTION ----: " + source.getClass().getName() + " report : " + ex.getMessage());
		logger.error("Stack Trace:");
		
		
		// TODO refactor this method to an utility class
		StackTraceElement[] elements = ex.getStackTrace();
		for (int i = 0; i < elements.length; i++) {
			logger.error("  class:" + elements[i].getClassName() + 
				"; method:"	+ elements[i].getMethodName() + 
				"; line: " + elements[i].getLineNumber());
		}
	}
	
	public static void info(String message) {
		Logger logger = getRootLogger();
		logger.info(message);
	}
	
	public static void warn(String message) {
		Logger logger = getRootLogger();
		logger.warn(message);
	}

	public static void error(String message) {
		Logger logger = getRootLogger();
		logger.error(message);
	}

	
	public static void debug(String message) {
		Logger logger = getRootLogger();
		logger.debug(message);
	}
	
	public static void info(Object source, String message) {
		info(source.getClass(), message);
	}

	public static void info(Class c, String message) {
		Logger logger = Logger.getLogger(c);
		logger.info(message);
	}


	public static void debug(Object source, String message) {
		debug(source.getClass(), message);
	}

	public static void debug(Class c, String message) {
		Logger logger = Logger.getLogger(c);
		logger.debug(message);
	}
	
	public static Logger getLogger(Class c) {
		return Logger.getLogger(c);
	}
}
	