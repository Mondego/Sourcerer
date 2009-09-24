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
import java.util.regex.Pattern;

import junit.framework.TestCase;

import org.apache.log4j.BasicConfigurator;
import org.htmlparser.Parser;

import edu.uci.ics.sourcerer.codecrawler.db.Hit;
import edu.uci.ics.sourcerer.codecrawler.parserplugin.IParserPluginIdGenerator;
import edu.uci.ics.sourcerer.codecrawler.parserplugin.plugins.SourceForgeParserPlugin;
import edu.uci.ics.sourcerer.codecrawler.util.UrlUtils;

/**
 * @author <a href="mailto:hahrot@yahoo.com">Huy A. Huynh</a>
 * @author <a href="bajracharya@gmail.com">Sushil Bajracharya</a>
 *
 */
public class SourceForgeParserPluginTest extends TestCase {

	SourceForgeParserPlugin plugin = new SourceForgeParserPlugin();
	
	public void setUp()
	{
		BasicConfigurator.configure();
		
		plugin.setIdGenerator(new IParserPluginIdGenerator() {
			public long getNewHitId() {
				return 0;
			}
		});
	
	}
	
	public void testProjectInfo() throws Exception {

		SourceForgeParserPlugin.ProjectInfo info = plugin.getProjectInfo("a-lms");
		System.out.println(String.format("Name:%s\nDescription:%s\nCategory:%s\nLanguage:%s\nPlatform:%s\nLicense:%s\n",
				info.name,
				info.description,
				info.category,
				info.language,
				info.platform,
				info.license //,
				//info.
				));
	}
	
	public void testDownloadPageParser1() throws Exception {
		
		Parser parser = new Parser("http://sourceforge.net/project/showfiles.php?group_id=84122&package_id=280700");
		
		Set<Hit> hits = plugin.parseDownloadPage(parser);
		
		for (Hit hit : hits) {
			
			System.out.println( hit.getCheckoutString() + "\t" 
					+ hit.getLanguage() + "\t" + hit.getVersion() 
					+ "\t" + hit.getReleaseDate() + "\t" + hit.getSourceCode()
					+ "\t" + hit.getDescription());
		}
	}
	
	public void testRepositoryPageParser() throws Exception {
		
		Parser parser = new Parser("http://sourceforge.net/cvs/?group_id=84122");
		Set<Hit> hits = plugin.parseRepositoryPage(parser);
		for (Hit hit : hits) {
			System.out.println(hit.getCheckoutString() + "\t" + hit.getLanguage() + "\t" + hit.getVersion() 
					+ "\t" + hit.getReleaseDate() + "\t" + hit.getDescription());
		}
		
		// http://sourceforge.net/svn/?group_id=152431
		parser = new Parser("http://sourceforge.net/svn/?group_id=152431");
		hits = plugin.parseRepositoryPage(parser);
		for (Hit hit : hits) {
			System.out.println(hit.getCheckoutString() + "\t" + hit.getLanguage() + "\t" + hit.getVersion() + "\t" 
					+ hit.getReleaseDate() + "\t" + hit.getDescription());
		}
		
		//http://sourceforge.net/svn/?group_id=135469 (has "-" in project name)
		parser = new Parser("http://sourceforge.net/svn/?group_id=135469");
		hits = plugin.parseRepositoryPage(parser);
		for (Hit hit : hits) {
			System.out.println(hit.getCheckoutString() + "\t" + hit.getLanguage() + "\t" + hit.getVersion() + "\t" 
					+ hit.getReleaseDate() + "\t" + hit.getDescription());
		}
	}
	
	public void testRegex() {
		assertTrue(Pattern.compile("\\bJava\\b").matcher("Java").find());
		assertTrue(Pattern.compile("\\bJava\\b").matcher("Java is here").find());
		assertTrue(Pattern.compile("\\bJava\\b").matcher("I found Java ns here").find());
		assertTrue(Pattern.compile("\\bJava\\b").matcher("C++;Java").find());
		assertTrue(Pattern.compile("\\bJava\\b").matcher("C++,Java").find());
		assertFalse(Pattern.compile("\\bJava\\b").matcher("C++,JavaScript").find());
		assertFalse(Pattern.compile("\\bJava\\b").matcher("JavaScript").find());
		assertFalse(Pattern.compile("\\bJava\\b").matcher(";JavaScript; Python").find());
	}
}