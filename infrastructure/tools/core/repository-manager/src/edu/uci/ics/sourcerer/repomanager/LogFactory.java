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
package edu.uci.ics.sourcerer.repomanager;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * @author <a href="bajracharya@gmail.com">Sushil Bajracharya</a>
 * @created Jan 12, 2009
 *
 */
public class LogFactory {
	
	private static Logger rootLogger = null;
	
	public static Logger getFileLogger(String namespace, String logFileName) throws SecurityException, IOException{
		return getFileLogger(namespace, logFileName, false);
	}
	
	public static Logger getFileLogger(String namespace, String logFileName, boolean append) throws SecurityException, IOException{
		Logger logger = Logger.getLogger(namespace);
	    FileHandler fh = new FileHandler(logFileName, append);
	    	
	    fh.setFormatter(new SimpleFormatter());
	    logger.addHandler(fh);
	    logger.setUseParentHandlers(false); // suppress console
	    return logger;
	}
//	public static Logger getRootLogger(){
//		if(rootLogger == null){
//			rootLogger = getFileLogger(String namespace, String logFileName)
//		}
//	}
	
}
