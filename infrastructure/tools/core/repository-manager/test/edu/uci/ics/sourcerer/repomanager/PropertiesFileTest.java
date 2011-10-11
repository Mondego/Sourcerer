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

import junit.framework.TestCase;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

/**
 * @author <a href="bajracharya@gmail.com">Sushil Bajracharya</a>
 * @created Jan 12, 2009
 *
 */
public class PropertiesFileTest extends TestCase{
	public void testPropertiesContent() throws FileNotFoundException, IOException{
		
		String file = "./test/resources/project.properties";
		Properties p = new Properties();
		p.load(new FileInputStream(file));
		
		System.out.println(p.getProperty("scmUrl"));
		
//		assertEquals("svn co http://svn.apache.org/repos/asf/cocoon/trunk/",
//					  p.getProperty("scmUrl"));
		
		String sources = p.getProperty("sourceurl");
		System.out.println(sources);

		//TODO is this obselete ?
//		String[] sourceArr = sources.split(":");
//		System.out.println(sourceArr.length);
	}
}