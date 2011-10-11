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
import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * @author <a href="bajracharya@gmail.com">Sushil Bajracharya</a>
 * @created Jan 12, 2009
 *
 */
public class RepoWalkerWithListerTest {
	
	
	final static String repoRoot = "./test/resources/testrepo";
	RepoWalker walker = new RepoWalker(new File(repoRoot));
	RepoFoldersListerCommand lister = new RepoFoldersListerCommand();
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		walker.clearCommands();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void testLister() throws IOException {
		
		Logger logger;
		
		try {
			logger = LogFactory.getFileLogger("edu.uci.ics.sourcerer.repomanager.repowalker", repoRoot + File.separator + "lister.log", true);
			lister.setLogger(logger);
		} catch (SecurityException e) {
			// TODO write to console
			System.err.println("Cannot obtain logger");
			e.printStackTrace();
		} catch (IOException e) {
			// TODO write to console
			System.err.println("Cannot obtain logger");
			e.printStackTrace();
		}
		
		walker.addCommand(lister,0);
		walker.startWalk();
		lister.printNames();
	}

}
