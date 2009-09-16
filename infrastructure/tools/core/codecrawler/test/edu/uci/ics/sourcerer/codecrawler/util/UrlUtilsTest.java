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
package edu.uci.ics.sourcerer.codecrawler.util;

import edu.uci.ics.sourcerer.codecrawler.util.UrlUtils;
import junit.framework.TestCase;

/**
 * @author <a href="mailto:hahrot@yahoo.com">Huy A. Huynh</a>
 * 
 */
public class UrlUtilsTest extends TestCase {

	public void testStripHttp() {
		assertEquals("www.goo.com/search?query=foo", UrlUtils.stripHttp("http://www.goo.com/search?query=foo"));
	}
	
	public void testStripQuery() {
		assertEquals("http://www.goo.com/search", UrlUtils.stripQuery("http://www.goo.com/search?query=foo"));
		assertEquals("http://www.goo.com/search/", UrlUtils.stripQuery("http://www.goo.com/search/?query=foo"));
	}
	
	public void testGetHostName() {
		assertEquals("www.goo.com", UrlUtils.getHostName("http://www.goo.com/search?query=foo"));
	}
	
	public void testGetQuery() {
		assertEquals("query=foo", UrlUtils.getQuery("http://www.goo.com/search?query=foo"));
	}
	
	public void testGetLevels() {
		String[] levels = UrlUtils.getLevels("http://www.goo.com/servlets/search?query=foo");
		assertNotNull(levels);
		assertEquals(3, levels.length);
		assertEquals("www.goo.com", levels[0]);
		assertEquals("servlets", levels[1]);
		assertEquals("search", levels[2]);
	}
}
