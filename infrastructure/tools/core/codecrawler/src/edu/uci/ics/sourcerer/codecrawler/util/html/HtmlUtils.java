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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.nodes.TextNode;
import org.htmlparser.util.NodeTreeWalker;

/**
 * @author <a href="mailto:hahrot@yahoo.com">Huy A. Huynh</a>
 * 
 */
public class HtmlUtils {

	/**
	 * Walks through all nodes inside the root node
	 * and concatenate all the text together. Useful
	 * for extracting the whole text that contains
	 * hyperlinks and such inside. CR/LF and spaces
	 * which will be ignored by the html render will
	 * not be included.
	 * TODO: text spacing is not very accurate in this
	 * implementation.
	 */
	public static String extractText(Node rootNode) {
		StringBuffer buf = new StringBuffer();
		
		NodeTreeWalker walker = new NodeTreeWalker(rootNode);
		while (walker.hasMoreNodes()) {
			Node next = walker.nextNode();
			if (next instanceof TextNode) {
				buf.append(removeIgnoredText(((TextNode)next).getText().trim()));
				buf.append(" ");
			}
		}
		
		return buf.toString();
	}
	
	public static String removeIgnoredText(String text) {
		Pattern p = Pattern.compile("\\s*[\\n\\r(\\r\\n)(\\n\\r)]+\\s*");
		Matcher m = p.matcher(text);
		
		StringBuffer buf = new StringBuffer();
		int idx = 0;
		
		while (m.find() && idx < text.length()) {
			buf.append(text.substring(idx, m.start()));
			buf.append(" ");
			idx = m.end();
		}
		
		if (idx < text.length())
			buf.append(text.substring(idx));
		
		return buf.toString().trim();
	}
	
	/**
	 * Starting from the root node, traverse through the node
	 * tree satisfying the path specified by the filters.
	 * @return <code>null</code> if cannot traverse via that path.
	 */
	public static Node traverse(Node rootNode, NodeFilter[] filterPath) {
		Node curNode = rootNode;
		
		for (NodeFilter filter : filterPath) {
			if (curNode == null)
				return null;
			
			//traverse to the first node that matches the filter
			curNode = curNode.getChildren().extractAllNodesThatMatch(filter).elementAt(0);
		}
		
		return curNode;
	}
	
	
	/**
	 * Get the i-th sub-node of a given node that matches the filter.
	 * @param index the i-th index, 1-based
	 */
	public static Node getTheIthNode(Node rootNode, int index, NodeFilter filter) {
		return rootNode.getChildren().extractAllNodesThatMatch(filter).elementAt(index-1);
	}
	
}
