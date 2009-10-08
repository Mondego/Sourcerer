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

import java.io.File;

import junit.framework.TestCase;

/**
 * @author <a href="bajracharya@gmail.com">Sushil Bajracharya</a>
 * @created Sep 25, 2009
 *
 */
public class FileUtilsTest extends TestCase {
	public void testCountChildren(){
		String dir = "./test/tempRepo";
		assertEquals(-1, FileUtils.countChildren(dir));
	}
	
	public void testLastNonEmptyLine(){
		String file = "./test/resources/cvsco/cvs.error.test";
		assertEquals("cvs [status aborted]: no repository",FileUtils.getLastNonEmptyLine(file));
	}
	
	public void testCleanDir(){
		File dir = new File("./test/resources/testrepo.withretry/44/20/content");
		FileUtils.cleanDirectory(dir);
		assertEquals(0, FileUtils.countChildren(dir.getAbsolutePath()));
		
		dir = new File("./test/resources/testrepo.withretry/44/22/content");
		FileUtils.cleanDirectory(dir);
		assertEquals(0, FileUtils.countChildren(dir.getAbsolutePath()));
	}
}
