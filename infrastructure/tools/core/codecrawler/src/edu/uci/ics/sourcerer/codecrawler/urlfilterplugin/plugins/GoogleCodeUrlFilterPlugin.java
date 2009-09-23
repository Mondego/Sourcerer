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

import java.util.HashSet;
import java.util.Set;

import edu.uci.ics.sourcerer.codecrawler.urlfilterplugin.UrlFilterPlugin;
import edu.uci.ics.sourcerer.codecrawler.util.UrlUtils;

/**
 * @author <a href="bajracharya@gmail.com">Sushil Bajracharya</a>
 * @created Sep 23, 2009
 *
 */
public class GoogleCodeUrlFilterPlugin extends UrlFilterPlugin {
	
	private final String PAGINATE_URL_PREFIX = "http://code.google.com/hosting/search?q=label%3AJava&filter=0&start=";
	private final String PROJECT_URL_PREFIX = "http://code.google.com/p/";
	
	private static Set<String> pages = new HashSet<String>(5600);
	private static Set<String> projectNames = new HashSet<String>(56000);
	
	@Override
	public Set<String> filterUrl(String url, String containerUrl) {

		if(!containerUrl.startsWith(PAGINATE_URL_PREFIX))
			return null;
		
		if(!(url.startsWith(PAGINATE_URL_PREFIX)) &&
				!url.startsWith(PROJECT_URL_PREFIX))
			return null;
		
		Set<String> result = new HashSet<String>(1);
		
		if(url.startsWith(PAGINATE_URL_PREFIX)){
			String currentPage = url.substring(PAGINATE_URL_PREFIX.length());
			int ampIndex = currentPage.indexOf('&');
			if(ampIndex > -1) currentPage = currentPage.substring(0, ampIndex);
			
			synchronized (pages) {
				if (pages.contains(currentPage)){
					return null;
				} 
				pages.add(currentPage);
			}
			
			result.add(url);
			return result;
		}
		
		if(url.startsWith(PROJECT_URL_PREFIX)){
			String[] levels = UrlUtils.getLevels(url);
			if(levels.length!=3) return null;
			
			synchronized (projectNames) {
				if (projectNames.contains(levels[2])) return null;
				projectNames.add(levels[2]);
			}
			
			result.add(url);
			return result;
		}
		
		return null;
	}
// start   http://code.google.com/hosting/search?q=label%3AJava&filter=0&start=0
// next >> http://code.google.com/hosting/search?q=label%3AJava&filter=0&start=20
// prev << http://code.google.com/hosting/search?q=label%3AJava&filter=0&start=0
// project http://code.google.com/p/symja/	
	
	
	
}
