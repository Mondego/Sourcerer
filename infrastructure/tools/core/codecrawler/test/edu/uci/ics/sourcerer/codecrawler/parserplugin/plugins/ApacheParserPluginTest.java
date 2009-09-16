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
package edu.uci.ics.sourcerer.codecrawler.parserplugin.plugins;

import java.util.Set;

import org.htmlparser.Parser;

import edu.uci.ics.sourcerer.codecrawler.db.Hit;
import edu.uci.ics.sourcerer.codecrawler.parserplugin.IParserPluginIdGenerator;
import edu.uci.ics.sourcerer.codecrawler.parserplugin.plugins.ApacheParserPlugin;

import junit.framework.TestCase;

/**
 * 
 * @author <a href="bajracharya@gmail.com">Sushil Bajracharya</a>
 * @created Sep 15, 2009
 *
 */
public class ApacheParserPluginTest extends TestCase {

	public void test1() throws Exception {
		
		ApacheParserPlugin plugin = new ApacheParserPlugin();
		plugin.setIdGenerator(new IParserPluginIdGenerator() {
			public long getNewHitId() {
				return 0;
			}
		});
		
		String []urls = {
				"http://projects.apache.org/projects/jakarta_bcel.html",
				"http://projects.apache.org/projects/ant.html"
		};
		
		for (String url : urls) {
			Parser parser = new Parser(url);
			
			Set<Hit> hits = plugin.parseProjectPage(parser);
			
			for (Hit hit : hits) {
				System.out.println(hit.getCheckoutString() + "\t" + hit.getProjectName()
						+ "\t" + hit.getLanguage() + "\t" + hit.getVersion() + "\t" + hit.getSourceCode());
			}
		}
	}
	
}
