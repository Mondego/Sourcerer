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

import java.util.Set;
import java.util.regex.Pattern;

import junit.framework.TestCase;
import edu.uci.ics.sourcerer.codecrawler.linkparserplugin.plugins.GeneralLinkParserPlugin;
import edu.uci.ics.sourcerer.codecrawler.network.UrlString;
import edu.uci.ics.sourcerer.codecrawler.parser.Document;
import edu.uci.ics.sourcerer.codecrawler.parser.IDocumentParser;
import edu.uci.ics.sourcerer.codecrawler.parser.impl.DocumentParser;
import edu.uci.ics.sourcerer.codecrawler.parserplugin.plugins.JavaNetParserPlugin;
import edu.uci.ics.sourcerer.codecrawler.util.UrlUtils;

/**
 * @author <a href="mailto:hahrot@yahoo.com">Huy A. Huynh</a>
 * 
 */
public class JavaNetDomainFilterPluginTest extends TestCase {

	public void testRegex() {
		Pattern p = Pattern.compile("http[s]*://[^.]+[.]dev[.]java[.]net.*");
		
		final String[] urls = {
				"https://ergo.dev.java.net/",
				"https://dotnetfromjava.dev.java.net/",
				"https://add-apt-key.dev.java.net/",
				"https://alfabetizacao-interativa.dev.java.net"
		};
		
		for (String url : urls) {
			assertTrue(p.matcher(url).matches());
			assertTrue(UrlUtils.getLevels(url).length == 1);
		}
	}
	
	public void testAlphaPage() throws Exception {
		/*
		JavaNetDomainFilterPlugin filter = new JavaNetDomainFilterPlugin();
		UrlString url = new UrlString("http://community.java.net/projects/alpha.csp");
		IDocumentParser docParser = new DocumentParser(new JavaNetParserPlugin(), new GeneralLinkParserPlugin());
		
		Document doc = Document.openDocument(url);
		docParser.parseDocument(doc, url.toString());
		
		for (UrlString link : doc.getLinks()) {
			Set<String> urls = filter.filterUrl(link.toString(), url.toString());
			if ((urls != null) && (urls.size() > 0))
				System.out.println("ACCEPTED: " + link.toString());
			else
				System.out.println("REJECTED: " + link.toString());
				
		}
		*/
	}
}
