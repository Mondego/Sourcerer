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
package edu.uci.ics.sourcerer.codecrawler.urlfilterplugin.plugins;

import java.io.IOException;
import java.util.Set;

import junit.framework.TestCase;

import org.apache.log4j.BasicConfigurator;

import edu.uci.ics.sourcerer.codecrawler.urlfilterplugin.plugins.SourceForgeListUrlFilterPlugin;

/**
 * 
 * @author <a href="bajracharya@gmail.com">Sushil Bajracharya</a>
 * @created Sep 15, 2009
 *
 */
public class SourceForgeListURLFilterTest extends TestCase {
		
		public void setUp()
		{
			BasicConfigurator.configure();
		}

		public void testURL1() throws IOException
		{
			SourceForgeListUrlFilterPlugin plugin = new SourceForgeListUrlFilterPlugin();
			
			String[] correctInputURL = {
					"http://sourceforge.net/projects/azureus",
					"http://sourceforge.net/cvs/?group_id=84122",
					"http://sourceforge.net/project/showfiles.php?group_id=84122",
			};
			
			String[] wrongInputURL = {
					"http://sourceforge.net/news/?group_id=84122",
					"http://sourceforge.net/forum/?group_id=84122",
					"http://azureus.cvs.sourceforge.net/viewvc/*checkout*/azureus/azureus3/.cvsignore",
					"http://azureus.cvs.sourceforge.net/viewvc/azureus/azureus3/.cvsignore?r1=1.2&r2=1.3"
			};
			
			Set<String> urls = null;
			
			
			for(String s : correctInputURL)
			{
				urls = plugin.filterUrl(s, null);
				assertTrue(urls!=null && urls.size()>0);
				System.out.println("PASSED:: "+s);
			}
			
			for(String s : wrongInputURL)
			{
				urls = plugin.filterUrl(s, null);
				assertFalse(urls!=null && urls.size()>0);
				System.out.println("PASSED:: "+s);
			}
			
		}
}