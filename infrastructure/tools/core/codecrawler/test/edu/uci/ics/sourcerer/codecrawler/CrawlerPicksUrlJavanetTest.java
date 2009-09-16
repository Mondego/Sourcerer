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
package edu.uci.ics.sourcerer.codecrawler;

import edu.uci.ics.sourcerer.codecrawler.crawler.Crawler;
import edu.uci.ics.sourcerer.codecrawler.db.memimpl.GatewayFactory;
import edu.uci.ics.sourcerer.codecrawler.util.CrawlerProperties;
import junit.framework.TestCase;

/**
 * @author <a href="mailto:hahrot@yahoo.com">Huy A. Huynh</a>
 * 
 */
public class CrawlerPicksUrlJavanetTest extends TestCase {

	CrawlerProperties properties; 
	
	protected void setUp() throws Exception {
		super.setUp();
		properties = CrawlerProperties.loadPropertiesFile("test/resources/javanet.properties");
		
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		
		
	}
	
	public void testRunner(){
		Crawler c = new Crawler(new GatewayFactory(), new DummyCrawlerListener(), properties);
		
		c.run();
		
	}

}
