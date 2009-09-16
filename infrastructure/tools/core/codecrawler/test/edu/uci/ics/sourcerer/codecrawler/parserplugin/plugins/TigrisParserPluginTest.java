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

import java.util.regex.Pattern;

import junit.framework.TestCase;
import edu.uci.ics.sourcerer.codecrawler.db.Hit;
import edu.uci.ics.sourcerer.codecrawler.network.UrlString;
import edu.uci.ics.sourcerer.codecrawler.parser.Document;
import edu.uci.ics.sourcerer.codecrawler.parser.IDocumentParser;
import edu.uci.ics.sourcerer.codecrawler.parser.impl.DocumentParser;
import edu.uci.ics.sourcerer.codecrawler.parserplugin.IParserPluginIdGenerator;
import edu.uci.ics.sourcerer.codecrawler.parserplugin.ParserPluginManager;

/**
 * @author <a href="mailto:hahrot@yahoo.com">Huy A. Huynh</a>
 * 
 */
public class TigrisParserPluginTest extends TestCase {

	public void test1() throws Exception {
		UrlString url = new UrlString("http://antelope.tigris.org/servlets/ProjectDocumentList");
		
		ParserPluginManager pluginMgr = new ParserPluginManager();
		pluginMgr.setIdGenerator(new IParserPluginIdGenerator() {
			public long getNewHitId() {
				return 0;
			}
		});
		String[] pluginNames = {"edu.uci.ics.sourcerer.codecrawler.parserplugin.plugins.TigrisParserPlugin"};
		pluginMgr.loadPlugins(pluginNames);
		
		IDocumentParser docParser = new DocumentParser(pluginMgr, null);
		Document doc = Document.openDocument(url);
		
		docParser.parseDocument(doc, null);
		for (Hit hit : doc.getHits())
			System.out.println(hit.getCheckoutString() + "\t" + hit.getLanguage() + "\t" + hit.getVersion() + "\t" + hit.getSourceCode());
	}
	
	public void testRegex() {
		Pattern pattern = Pattern.compile("\\bwritten\\s+in\\s+java\\b", Pattern.CASE_INSENSITIVE);
		assertTrue(pattern.matcher("this program is written in  Java").find());
		pattern = Pattern.compile("\\bEclipse\\b", Pattern.CASE_INSENSITIVE);
		assertTrue(pattern.matcher("this is an eclipse-plugin").find());
		
		Pattern versionPattern = Pattern.compile("\\d+(.\\d+)*");
		assertTrue(versionPattern.matcher("3.5.1").find());
		assertTrue(versionPattern.matcher("3.5").find());
		assertTrue(versionPattern.matcher("3.15.5.1").find());
	}
	
	private double certaintyFunc(int n) {
		return 2-Math.pow(2, 1.0D/(Math.pow(n, 0.7)+0.4));
	}
	
	public void testCertaintyFunction() {
		for (int n = 1; n < 20; n++)
			System.out.println(String.format("%.9f", certaintyFunc(n)));
	}
}
