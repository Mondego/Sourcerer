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

import edu.uci.ics.sourcerer.codecrawler.util.CrawlerProperties;
import edu.uci.ics.sourcerer.codecrawler.util.CrawlerPropertiesException;
import junit.framework.TestCase;

/**
 * @author <a href="mailto:hahrot@yahoo.com">Huy A. Huynh</a>
 * 
 */
public class CrawlerPropertiesTest extends TestCase {

	public void test() {
		try {
			CrawlerProperties properties = new CrawlerProperties("test/resources/crawlertest.properties");
			String[] plugins = properties.getFilterPlugins();
			assertNotNull(plugins);
			assertEquals(3, plugins.length);
			for (int i = 0; i < 3; i++) {
				assertEquals("edu.uci.ics.sourcerer.codecrawler.urlfilterplugin.plugins.plugin" + (i+1), plugins[i]);
			}
		} catch (CrawlerPropertiesException e) {
			throw new RuntimeException("Properties file not found.");
		}
	}
}
