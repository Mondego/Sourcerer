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

import java.net.MalformedURLException;

import junit.framework.TestCase;
import edu.uci.ics.sourcerer.codecrawler.db.Hit;
import edu.uci.ics.sourcerer.codecrawler.network.UrlString;
import edu.uci.ics.sourcerer.codecrawler.parser.Document;
import edu.uci.ics.sourcerer.codecrawler.parser.IDocumentParser;
import edu.uci.ics.sourcerer.codecrawler.parser.impl.DocumentParser;
import edu.uci.ics.sourcerer.codecrawler.parserplugin.IParserPluginIdGenerator;
import edu.uci.ics.sourcerer.codecrawler.parserplugin.ParserPluginLoadException;
import edu.uci.ics.sourcerer.codecrawler.parserplugin.ParserPluginManager;

/**
 * 
 * @author <a href="bajracharya@gmail.com">Sushil Bajracharya</a>
 * @created Sep 15, 2009
 *
 */
public class JavaNetParserPluginTest extends AbstractParserPluginTest {

	public String[] getPluginNames(){
		String[] p = { "edu.uci.ics.sourcerer.codecrawler.parserplugin.plugins.JavaNetParserPlugin" }; 
		return p;
	}

	public void test1() throws Exception {

		UrlString url = // new
		// UrlString("https://bloged.dev.java.net/servlets/ProjectDocumentList?folderID=3632&expandFolder=3632&folderID=3629");
		
		// new UrlString(
		//		"https://watchdog.dev.java.net/servlets/ProjectDocumentList?folderID=4048&expandFolder=4048&folderID=0");
		// new
		// UrlString("https://watchdog.dev.java.net/servlets/ProjectDocumentList");
		
		 new
		 UrlString("https://jsr-107-interest.dev.java.net/servlets/ProjectDocumentList?folderID=0&expandFolder=0&folderID=0");

		Document doc = Document.openDocument(url);
		docParser.parseDocument(doc, null);

		printHits(doc);

	}

	public void testSvnLinkExtraction() throws Exception {
		UrlString url = 
			new UrlString("https://appbuilder.dev.java.net/source/browse/appbuilder");
			// new UrlString("https://esb-console.dev.java.net/source/browse/esb-console"); // no project info
		// new UrlString("https://yygo-midp-local.dev.java.net/source/browse/yygo-midp-local"); // slowww
		Document doc = Document.openDocument(url);
		docParser.parseDocument(doc, null);

		printHits(doc);
		
	}

	public void testCvsLinkExtraction() throws Exception {
		UrlString url = 
			
			 new UrlString("https://watchdog.dev.java.net/source/browse/watchdog");
			// new UrlString("https://boothsignup.dev.java.net/source/browse/boothsignup");
		Document doc = Document.openDocument(url);
		docParser.parseDocument(doc, null);

		printHits(doc);
		
	}
	
	
}
