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

 */
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author <a href="bajracharya@gmail.com">Sushil Bajracharya</a>
 * @created Jan 12, 2009
 *
 */
public class CrawlerEntryFilterSourceforgeTest {

	String sfOpFile = "./test/resources/crawlouts/sourceforge.net.hits.txt.1";
	CrawlerOutputFilter cof;
	
	@Before
	public void setUp() throws Exception {
		cof = new CrawlerOutputFilter();
		cof.setCrawledRepositoryName(Repositories.SOURCEFORGE.name());
		cof.setCrawlerOutputFileLocation(sfOpFile);
		cof.loadProjects();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testFilter() {
		Map<String, ProjectProperties> p = cof.getProjects();
		CrawlerEntryFilterSourceforge filter = new CrawlerEntryFilterSourceforge();
		filter.filter(p);
		System.out.println(p.size());
		// p is now filtered
		
	}

}
