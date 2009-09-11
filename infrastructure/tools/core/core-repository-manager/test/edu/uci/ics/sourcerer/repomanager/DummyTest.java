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

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;
import org.apache.tools.ant.util.FileUtils;

/**
 * @author <a href="bajracharya@gmail.com">Sushil Bajracharya</a>
 * @created Jan 12, 2009
 *
 */
public class DummyTest {
	
	@Test
	public void testSth(){
		String fileName = "./test/resources/fromurl/download/fileName_download.zip";
		String[] pathStack = FileUtils.getPathStack(fileName);
		System.out.println(pathStack.length);
		
		System.out.println("./src/ path " + new File("./src/").getPath());
		
		fileName = "https://swingx.dev.java.net/files/documents/2981/51057/swingx-2007_02_18-src.zip";
		pathStack = FileUtils.getPathStack(fileName);
		System.out.println(pathStack.length);
		
	}
		
	@Test
	public void testStringStripEnd(){
		String longS = "/src/test/ok/";
		String strip = "ok/";
		
		assertEquals("/src/test/", FileUtility.stripStringSuffix(longS, strip));
		
		strip = File.separator;
		assertEquals("/src/test/ok", FileUtility.stripStringSuffix(longS, strip));		
	}
	
	public void testExtractParentFolderName() throws Exception{
		String path = "./parent/child/";
		assertEquals("./parent/", FileUtility.extractParentFolderName(path));
		path = "./parent";
		try{
			FileUtility.extractParentFolderName(path);
		    Assert.fail();
		} catch(Exception e){
			
		}
		
		path = "./parent/child";
		assertEquals("./parent/", FileUtility.extractParentFolderName(path));
	}
}
