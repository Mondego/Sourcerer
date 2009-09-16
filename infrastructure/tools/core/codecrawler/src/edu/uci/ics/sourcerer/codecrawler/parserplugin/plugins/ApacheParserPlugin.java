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

import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.AndFilter;
import org.htmlparser.filters.HasAttributeFilter;
import org.htmlparser.filters.HasChildFilter;
import org.htmlparser.filters.NodeClassFilter;
import org.htmlparser.tags.BodyTag;
import org.htmlparser.tags.Div;
import org.htmlparser.tags.HeadingTag;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.tags.TableColumn;
import org.htmlparser.tags.TableRow;
import org.htmlparser.tags.TableTag;
import org.htmlparser.util.ParserException;
import org.htmlparser.util.SimpleNodeIterator;

import edu.uci.ics.sourcerer.codecrawler.db.Hit;
import edu.uci.ics.sourcerer.codecrawler.parserplugin.ParserPlugin;
import edu.uci.ics.sourcerer.codecrawler.util.DebugUtils;
import edu.uci.ics.sourcerer.codecrawler.util.html.HtmlUtils;
import edu.uci.ics.sourcerer.common.LoggerUtils;

/**
 * 
 * @author <a href="bajracharya@gmail.com">Sushil Bajracharya</a>
 * @created Sep 15, 2009
 *
 */
public class ApacheParserPlugin extends ParserPlugin {
	
	private static final boolean debugEnabled = LoggerUtils.isDebugEnabled(ApacheParserPlugin.class);
	private static final String className = ApacheParserPlugin.class.getSimpleName();

	public Set<Hit> parseHits(Parser htmlParser, String referringUrl) {
		return parseProjectPage(htmlParser);
	}
	
	public Set<Hit> parseProjectPage(Parser htmlParser) {
		Set<Hit> result = new HashSet<Hit>();
		
		try	{
			htmlParser.reset();

			////////////
			//get the content cell
			Node bodyNode = htmlParser.extractAllNodesThatMatch(new NodeClassFilter(BodyTag.class)).elementAt(0);
			Node bodyTableNodeContainerDiv = HtmlUtils.getTheIthNode(bodyNode, 1, 
												new AndFilter(  new NodeClassFilter(Div.class),
																new HasAttributeFilter("id", "bodySection")));
										
			
			Node bodyTableNode = HtmlUtils.getTheIthNode(bodyTableNodeContainerDiv, 1, new NodeClassFilter(TableTag.class));
			//mainNode is the table cell node that contains the main content
			Node mainRowNode = HtmlUtils.getTheIthNode(bodyTableNode, 1, new AndFilter(
					new NodeClassFilter(TableRow.class),
					new HasChildFilter(new AndFilter(
							new NodeClassFilter(TableColumn.class),
							new HasAttributeFilter("class", "body")
							)
					))
			);
			Node mainNode = HtmlUtils.getTheIthNode(mainRowNode, 1, new AndFilter(
							new NodeClassFilter(TableColumn.class),
							new HasAttributeFilter("class", "body")
							));
			
			////////////
			//get project name
			String projectName = null;
			projectName = HtmlUtils.extractText(
					HtmlUtils.getTheIthNode(mainNode, 1, new NodeClassFilter(HeadingTag.class))
					).trim();		

			////////////
			//get project info

			//get project info table
			NodeFilter[] projectInfoTableFilterPath = {
				new NodeClassFilter(Div.class),
				new NodeClassFilter(TableTag.class)
			};
			Node projectInfoTableNode = HtmlUtils.traverse(mainNode, projectInfoTableFilterPath);
			
			//get project info
			String language = null;
			String category = null;
			String license = null;
			SimpleNodeIterator projectInfoIterator = 
				projectInfoTableNode.getChildren().extractAllNodesThatMatch(
						new NodeClassFilter(TableRow.class)).elements();
			while (projectInfoIterator.hasMoreNodes()) {
				TableRow row = (TableRow)projectInfoIterator.nextNode();
				String attribute = HtmlUtils.extractText(
						HtmlUtils.getTheIthNode(row, 1, new NodeClassFilter(TableColumn.class))).trim();
				String value = HtmlUtils.extractText(
						(TableColumn)HtmlUtils.getTheIthNode(row, 2, new NodeClassFilter(TableColumn.class))).trim();
				
				if (attribute.equals("Programming Languages")) {
					language = value;
				} else if (attribute.equals("Categories")) {
					category = value;
				} else if (attribute.equals("License")) {
					license = value;
				}
			}
			
			///////////
			//get svn link
			String checkoutString = null;
			
			//get to the table that contains the link
			NodeFilter[] pathToDiv = { new NodeClassFilter(Div.class), new NodeClassFilter(Div.class) };
			Node secondDivNode = HtmlUtils.traverse(mainNode, pathToDiv);
			Node theRightDiv = HtmlUtils.getTheIthNode(secondDivNode, 1, new AndFilter(
					new NodeClassFilter(Div.class),
					new HasChildFilter(new AndFilter(
							new NodeClassFilter(TableTag.class),
							new HasChildFilter(new AndFilter(
									new NodeClassFilter(TableRow.class),
									new NodeFilter() {
										public boolean accept(Node node) {
											return HtmlUtils.extractText(node).contains("SVN Direct");
										}								
									}
									))
							))
					));
			
			//get the link
			Node svnTable = HtmlUtils.getTheIthNode(theRightDiv, 1, new NodeClassFilter(TableTag.class));
			Node svnRow = HtmlUtils.getTheIthNode(svnTable, 1, new AndFilter(
					new NodeClassFilter(TableRow.class),
					new NodeFilter() {
						public boolean accept(Node node) {
							return HtmlUtils.extractText(node).contains("SVN Direct");
						}								
					}
					));
			Node svnColumn = HtmlUtils.getTheIthNode(svnRow, 2, new NodeClassFilter(TableColumn.class));
			LinkTag linkTag = (LinkTag)HtmlUtils.getTheIthNode(svnColumn, 1, new NodeClassFilter(LinkTag.class));
			checkoutString = "svn co " + linkTag.getLink();
			
			///////////
			//build the hit
			Hit hit = new Hit(getIdGenerator().getNewHitId(), checkoutString);
			hit.setProjectName(projectName);
			hit.setContainerUrl(htmlParser.getURL());
			hit.setLanguage(language);
			hit.setProjectCategory(category);
			hit.setProjectLicense(license);
			result.add(hit);
		
		} catch (ParserException e) {
			if (debugEnabled)
				LoggerUtils.error(className + ": ParserException while parsing. Message: "
						+ e.getMessage() + " Stack: " + DebugUtils.getStackTrace(e));
		} catch (NullPointerException e) {
			if (debugEnabled)
				LoggerUtils.error(className + ": Some element not found while parsing. Stack: "
						+ DebugUtils.getStackTrace(e));
		} catch (ClassCastException e) {
			if (debugEnabled)
				LoggerUtils.error(className + ": ClassCastException. Stack: "
						+ DebugUtils.getStackTrace(e));
		}
		
		return result;
	}

}
