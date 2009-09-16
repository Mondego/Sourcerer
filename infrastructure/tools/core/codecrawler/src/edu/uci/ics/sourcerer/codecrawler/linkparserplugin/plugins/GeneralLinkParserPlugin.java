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
package edu.uci.ics.sourcerer.codecrawler.linkparserplugin.plugins;

import java.util.HashSet;
import java.util.Set;

import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.AndFilter;
import org.htmlparser.filters.NodeClassFilter;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

import edu.uci.ics.sourcerer.codecrawler.linkparserplugin.LinkParserPlugin;
import edu.uci.ics.sourcerer.codecrawler.util.UrlUtils;

/**
 * @author <a href="mailto:hahrot@yahoo.com">Huy A. Huynh</a>
 * 
 */
public class GeneralLinkParserPlugin extends LinkParserPlugin {

	public Set<String> parseLinks(Parser htmlParser, String referringUrl) {
		NodeFilter linkFilter = new NodeClassFilter(LinkTag.class);
		NodeFilter httpFilter = new AndFilter(linkFilter,
				new NodeFilter() {
					private static final long serialVersionUID = 2342364234L;
					public boolean accept(Node node) {
						return ((LinkTag)node).isHTTPLikeLink();
					}
				});
		try {
			NodeList nodeList = htmlParser.extractAllNodesThatMatch(httpFilter);
			HashSet<String> linkSet = new HashSet<String>();
			for (int i = 0; i < nodeList.size(); i++) {
				String link = ((LinkTag)nodeList.elementAt(i)).getLink();
				linkSet.add(UrlUtils.fixLink(link));
			}
			return linkSet;
		} catch (ParserException e) {}
		
		return null;
	}

}
