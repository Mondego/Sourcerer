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

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.TTCCLayout;
import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.tags.Span;
import org.htmlparser.tags.TableColumn;
import org.htmlparser.tags.TableRow;
import org.htmlparser.tags.TableTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import org.htmlparser.util.SimpleNodeIterator;
import org.htmlparser.filters.AndFilter;
import org.htmlparser.filters.HasAttributeFilter;
import org.htmlparser.filters.HasParentFilter;
import org.htmlparser.filters.NodeClassFilter;
import org.htmlparser.filters.TagNameFilter;

import edu.uci.ics.sourcerer.codecrawler.db.Hit;
import edu.uci.ics.sourcerer.codecrawler.parserplugin.ParserPlugin;
import edu.uci.ics.sourcerer.codecrawler.util.UrlUtils;
import edu.uci.ics.sourcerer.codecrawler.util.html.HtmlUtils;

/**
 * @author <a href="bajracharya@gmail.com">Sushil Bajracharya</a>
 * @created Sep 23, 2009
 *
 */
public class GoogleCodeParserPlugin extends ParserPlugin {

	private final String PROJECT_URL_PREFIX = "http://code.google.com/p/";
	
	public Set<Hit> parseHits(Parser htmlParser, String referringUrl) {
		HashSet<Hit> hits = new HashSet<Hit>(1);
		
		String url = htmlParser.getURL();
		
		if(url.startsWith(PROJECT_URL_PREFIX)){
			hits.add(processProjectPage(htmlParser, referringUrl));
		}
		
		return hits;
	}

	/**
	 * @param htmlParser
	 * @param referringUrl
	 * @return
	 */
	private Hit processProjectPage(Parser htmlParser, String referringUrl) {
		
		String _scmCommand = "";
		Hit hit = new Hit(getIdGenerator().getNewHitId(), _scmCommand);
		
		hit.setContainerUrl(htmlParser.getURL());
		hit.setProjectName(UrlUtils.getLevels(htmlParser.getURL())[2]);
		hit.setLanguage("Java");
		
		// parse the home page for project/hit details
		try {
			
			NodeFilter pDescFilter = new AndFilter(
					new NodeClassFilter(LinkTag.class),
					new HasAttributeFilter("id","project_summary_link"));
			LinkTag pDescNode = (LinkTag) htmlParser.extractAllNodesThatMatch(pDescFilter).elementAt(0);
			hit.setProjectDescription(pDescNode.getLinkText());
			
			// parse the pMeta table that contains project metadata
			NodeFilter pMeta = new AndFilter(
					new NodeClassFilter(TableTag.class),
					new HasAttributeFilter("class","pmeta"));
			htmlParser.reset();
			Node pMetaTable = htmlParser.extractAllNodesThatMatch(pMeta).elementAt(0);
			NodeList rows = pMetaTable.getChildren().extractAllNodesThatMatch(new NodeClassFilter(TableRow.class));
			SimpleNodeIterator iterator = rows.elements();
			
			while (iterator.hasMoreNodes()) {
				Node node = iterator.nextNode();
				
				// process each row
				if(!node.getClass().equals(TableRow.class)){
					continue;
				}
				
				TableRow _row = (TableRow) node;
				TableColumn[] _cols = _row.getColumns();
				if (_cols == null || _cols.length!=1)
					continue;
				
				Node _licLabel = _cols[0].getChildren()
						.extractAllNodesThatMatch(
								new NodeClassFilter(LinkTag.class))
						.elementAt(0);
				if (_licLabel == null)
					continue;

				hit.setProjectLicense(((LinkTag) (_licLabel)).getLinkText());
				break;
				
			}
			
			NodeFilter pLabels = new AndFilter(
					new NodeClassFilter(LinkTag.class), 
					new HasParentFilter(new AndFilter(
							new NodeClassFilter(TableColumn.class),
							new HasAttributeFilter("id", "project_labels"))));
				
			htmlParser.reset();
			NodeList labels = htmlParser.extractAllNodesThatMatch(pLabels);
			SimpleNodeIterator iterator2 = labels.elements();
			StringBuffer _sbuf = new StringBuffer();
			while(iterator2.hasMoreNodes()){
				LinkTag l = (LinkTag) iterator2.nextNode();
				_sbuf.append(l.getLinkText() + ", ");
			}
			String _labelsStr = _sbuf.toString();
			if(_labelsStr.endsWith(", ")){
				_labelsStr = _labelsStr.substring(0, _labelsStr.length()-2);
			}
			hit.setProjectCategory(_labelsStr);
			
			
			
		} catch (ParserException e) {
			// TODO log
			e.printStackTrace();
		}
		
		// parse the Source page for the scm link
		// google code provides svn and mercurial hosting
		String sourcePage = hit.getContainerUrl() + "source/checkout";
		htmlParser.reset();
		try {
			htmlParser.setURL(sourcePage);
			htmlParser.reset();
			
			Node codeNodeTT = htmlParser.extractAllNodesThatMatch(
					new AndFilter(
							new TagNameFilter("tt"),
							new HasAttributeFilter("id","checkoutcmd"))).elementAt(0);
			
			Node coNode = codeNodeTT.getNextSibling();
			String co = coNode.getText();
			
			if (co.startsWith("svn")) {
				Node protocolNode = coNode.getNextSibling().getNextSibling().getNextSibling();
				String protocol = protocolNode.getText();

				Node restNode = protocolNode.getNextSibling().getNextSibling().getNextSibling();
				String rest = restNode.getText();

				String scmLink = co + " " + protocol + rest;

				hit.setCheckoutString(scmLink);
			} else if (co.startsWith("hg")) {
				hit.setCheckoutString(co);
			}
			
		} catch (ParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		return hit;
	}
	
	//Hits info
	//---------
	//ID	Date	Link	
	//Project name	Project description	Project category	
	//License	Language	Version	
	//Source	Release date	Description	
	//Container URL	Keywords	File Extensions
	
	//Sample from java.net
	//--------------------
	//12	2008-Dec-16	svn checkout https://ppm-7.dev.java.net/svn/ppm-7/trunk ppm-7 --username username	
	//ppm-7	CTS[Contribution Tracking System] a Group 7 PPM Implementation	None
	//http://www.opensource.org/licenses/bsd-license.php;Berkeley Software Distribution (BSD) License;	Java?certainty=0.853050	null
	//null	null	null	
	//https://ppm-7.dev.java.net/source/browse/ppm-7/	java=9	null

}
