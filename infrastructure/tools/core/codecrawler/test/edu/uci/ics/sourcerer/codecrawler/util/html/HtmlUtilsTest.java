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
package edu.uci.ics.sourcerer.codecrawler.util.html;

import junit.framework.TestCase;

import org.htmlparser.Node;
import org.htmlparser.Parser;
import org.htmlparser.filters.NodeClassFilter;
import org.htmlparser.tags.BodyTag;
import org.htmlparser.util.ParserException;

import edu.uci.ics.sourcerer.codecrawler.util.html.HtmlUtils;

/**
 * @author <a href="mailto:hahrot@yahoo.com">Huy A. Huynh</a>
 * 
 */
public class HtmlUtilsTest extends TestCase {

	public void testExtractText() throws ParserException {
		Parser parser = new Parser("http://htmlparser.sourceforge.net/");
		
		Node bodyNode = parser.extractAllNodesThatMatch(new NodeClassFilter(BodyTag.class)).elementAt(0);
		
		System.out.println(HtmlUtils.extractText(bodyNode));
	}
	
}
