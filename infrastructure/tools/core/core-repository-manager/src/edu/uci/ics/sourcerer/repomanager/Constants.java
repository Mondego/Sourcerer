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

/**
 * @author <a href="bajracharya@gmail.com">Sushil Bajracharya</a>
 * @created Jan 12, 2009
 *
 */
public class Constants {
	
	// this is not actually a constant
	public static final String getCertScriptFileName(){
		return "getCerts4svn_" + System.currentTimeMillis() + ".sh";
	}
	
	public static final String getSourceFolderName(){
		return "content";
	}
	
	public static final String getDownloadFolderName(){
		return "download";
	}
	
	public static final String getCvsTaskLogFileName(){
		return "cvsrun.log";
	}
	
	public static final String getCvsErrorFileName(){
		return "cvs.error";
	}
	
	public static final String getCvsCoOutputFileName(){
		return "cvsco.out";
	}
	
	public static final String getCvsStatOutputFileName(){
		return "cvsstat.out";
	}
	
	public static final String getSvnStatOutputFileName(){
		return "svnstat.properties";
	}
	
	public static final String getDownloadOutputFileName(){
		return "download.out";
	}
	
	public static final String getSourceProjectPropertiesFileName(){
		return "project.properties";
	}
	
	public static final String getPackageFolderName(){
		return "package";
	}
	
	public static final String getPackageFolderNameSeparator(){
		return ".";
	}
	
	public static final String getUnarchiverLogFileName(){
		return "unarchive.log";
	}
	
	public static final String getDownloaderLogFileName(){
		return "downloader.log";
	}
}
