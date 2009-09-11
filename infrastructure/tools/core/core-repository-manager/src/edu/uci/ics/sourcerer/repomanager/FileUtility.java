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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.tools.ant.util.FileUtils;

/**
 * @author <a href="bajracharya@gmail.com">Sushil Bajracharya</a>
 * @created Jan 12, 2009
 *
 */
public class FileUtility {

	public static String stripStringSuffix(String toBeStripped, String suffix){
		if(toBeStripped.endsWith(suffix)){
			return toBeStripped.substring(0, toBeStripped.length() - suffix.length() );
		} else {
			return toBeStripped;
		}
		
	}
	
	public static String extractFileNameWithExtension(String filePath){
		String[] pathStack = FileUtils.getPathStack(filePath);
		return pathStack[pathStack.length-1];
	}
	
	/**
	 * 
	 * @param filePath
	 * @return returns the file name without the valid archive extension; 
	 * 			if a valid archive extension is not found, returns null
	 */
	public static String extractFileNameWithoutArchiveExtension(String filePath) {
		String[] pathStack = FileUtils.getPathStack(filePath);
		String _name = pathStack[pathStack.length-1]; 
		
		ArchivedFileExtensions _ext = ArchivedFileExtensions.extractSupportedArchiveExtension(_name);
		if (_ext != null)
			return stripStringSuffix(_name, _ext.getExtension());
		else
			return null;
	}
	
	
	/**
	 * 
	 * @param path at least two level of path chunks required with a path separator as delimiter
	 *			Throws an Exception otherwise
	 * @return strips off the last path chunk and returns the remaining string with the trailing
	 * 			path separator.
	 */
	public static String extractParentFolderName(String path) throws Exception{
		String[] pathStack = FileUtils.getPathStack(path);
		
		if(pathStack.length<2)
			throw new Exception(path + " does not contain a child/parent strucutre.");
		else{
			List pathList = new ArrayList(pathStack.length-1);
			for (int i = 0; i < pathStack.length - 2; i++) {
				pathList.add(i, pathStack[i]);
			}
			return FileUtils.getPath(pathList);
		}
	}
	
	
	public static void writeStringToFile(String string, File file) throws IOException{
        BufferedWriter out = new BufferedWriter(new FileWriter(file));
        out.write(string);
        out.close();
    }
	
}
