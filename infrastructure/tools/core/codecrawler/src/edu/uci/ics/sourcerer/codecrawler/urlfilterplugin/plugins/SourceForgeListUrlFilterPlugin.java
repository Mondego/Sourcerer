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

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

import edu.uci.ics.sourcerer.codecrawler.parserplugin.plugins.SourceForgeParserPlugin;
import edu.uci.ics.sourcerer.codecrawler.urlfilterplugin.UrlFilterPlugin;
import edu.uci.ics.sourcerer.codecrawler.util.CrawlerProperties;
import edu.uci.ics.sourcerer.codecrawler.util.UrlUtils;
import edu.uci.ics.sourcerer.common.LoggerUtils;

/**
 * @author <a href="mailto:hahrot@yahoo.com">Huy A. Huynh</a>
 * 
 * This plugin reads a list of project names from a text
 * file and fill the URL queue with them.
 * The file can be specified in the properties file in the
 * property named:
 * crawler.urlfilterplugin.SourceForgeListUrlFilterPlugin.listFile
 */
public class SourceForgeListUrlFilterPlugin extends UrlFilterPlugin {

	private static final boolean debugEnabled = LoggerUtils.isDebugEnabled(SourceForgeListUrlFilterPlugin.class);
	
	private static Set<String> projectNames = new HashSet<String>();
	static private Boolean loaded = false;
	static private Boolean called = false;
	
	public SourceForgeListUrlFilterPlugin() throws IOException {
		synchronized (loaded) {
			if (!loaded) {
				String filename = CrawlerProperties.getInstance().getProperty(CrawlerProperties.CRAWLER + 
						".urlfilterplugin.SourceForgeListUrlFilterPlugin.listFile", "properties/SourceForge.net.projects.list");
				
				Scanner scanner = new Scanner(new FileInputStream(filename));
				while (scanner.hasNextLine()) {
					String projectName = scanner.nextLine().trim();
					if (projectName.length() > 0) {
						projectNames.add(projectName);
					}
				}
				scanner.close();
				
				loaded = true;
				
				if (debugEnabled)
					LoggerUtils.debug("SourceForgeListUrlFilterPlugin: loaded project list from file \"" + filename + "\"");
			}
		}
	}
	
	@Override
	public Set<String> filterUrl(String url, String containerUrl) {
		Set<String> result = null;
		
		//only generate the list just once
		synchronized (called) {
			if (!called) {
				result = new HashSet<String>();
				for (String projectName : projectNames) {
					result.add("http://sourceforge.net/projects/"
							+ projectName);
				}
				called = true;
			}
		}
		
		//filter, accept only download pages
		String[] levels = UrlUtils.getLevels(url);
		
		//include 
		//	http://sourceforge.net/projects/azureus
		//	http://sourceforge.net/project/showfiles.php?group_id=84122		(page before the package page)
		//	http://sourceforge.net/project/showfiles.php?group_id=84122&package_id=280700
		boolean checkDownloadPage = (	levels.length==3 &&
										levels[0].endsWith("sourceforge.net") &&
										levels[1].equalsIgnoreCase("project") && 
										levels[2].equalsIgnoreCase("showfiles.php"));
		
		//include  
		//   http://sourceforge.net/cvs/?group_id=84122
		//	 http://sourceforge.net/svn/?group_id=197031
		boolean checkRepositoryPage = ( levels.length==2 && 
										levels[0].endsWith("sourceforge.net") &&
										(levels[1].equalsIgnoreCase("cvs") || levels[1].equalsIgnoreCase("svn")) );
		
		if (!checkDownloadPage && !checkRepositoryPage)
			return result;
		
		
		if (result == null)
			result = new HashSet<String>();
		result.add(url);
		
		return result;
	}

}