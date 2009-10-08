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

import edu.uci.ics.sourcerer.codecrawler.network.UrlString;
import edu.uci.ics.sourcerer.codecrawler.parser.Document;

/**
 * @author <a href="bajracharya@gmail.com">Sushil Bajracharya</a>
 * @created Sep 23, 2009
 *
 */
public class GoogleCodeParserPluginTest extends AbstractParserPluginTest{
	public String[] getPluginNames(){
		String[] p = { "edu.uci.ics.sourcerer.codecrawler.parserplugin.plugins.GoogleCodeParserPlugin" }; 
		return p;
	}

	public void testHg() throws Exception {
		
		String u1 = "http://code.google.com/p/jynx/";
		
		UrlString url  = new UrlString(u1);
		Document doc = Document.openDocument(url);
		docParser.parseDocument(doc, null);
		printHits(doc);
	}
	
	public void testSvn() throws Exception {
		
		String u2 = "http://code.google.com/p/symja/";
		
		UrlString url  = new UrlString(u2);
		Document doc = Document.openDocument(url);
		docParser.parseDocument(doc, null);
		printHits(doc);
	}
}
