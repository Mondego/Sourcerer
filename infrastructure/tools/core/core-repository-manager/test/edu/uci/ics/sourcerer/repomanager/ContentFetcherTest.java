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

import org.junit.Test;

/**
 * @author <a href="bajracharya@gmail.com">Sushil Bajracharya</a>
 * @created Jan 13, 2009
 *
 */
public class ContentFetcherTest {

	final static String repoRoot = "./test/resources/testrepo";
	ContentFetcher cf = new ContentFetcher(repoRoot);
	

	/**
	 * Test method for {@link edu.uci.ics.sourcerer.repomanager.ContentFetcher#fetch()}.
	 */
	@Test
	public void testFetch() {
		
		cf.setPauseDuration(1000);
		cf.fetch();
		
	}

}
