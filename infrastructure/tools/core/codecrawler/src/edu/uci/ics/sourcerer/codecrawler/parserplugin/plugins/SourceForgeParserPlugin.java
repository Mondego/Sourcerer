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

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.Tag;
import org.htmlparser.filters.AndFilter;
import org.htmlparser.filters.HasAttributeFilter;
import org.htmlparser.filters.HasChildFilter;
import org.htmlparser.filters.HasParentFilter;
import org.htmlparser.filters.NodeClassFilter;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.nodes.TagNode;
import org.htmlparser.nodes.TextNode;
import org.htmlparser.tags.Bullet;
import org.htmlparser.tags.BulletList;
import org.htmlparser.tags.Div;
import org.htmlparser.tags.HeadingTag;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.tags.ParagraphTag;
import org.htmlparser.tags.TableColumn;
import org.htmlparser.tags.TableRow;
import org.htmlparser.tags.TableTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import org.htmlparser.util.SimpleNodeIterator;

import edu.uci.ics.sourcerer.codecrawler.db.Hit;
import edu.uci.ics.sourcerer.codecrawler.parserplugin.ParserPlugin;
import edu.uci.ics.sourcerer.codecrawler.util.CertaintyUtils;
import edu.uci.ics.sourcerer.codecrawler.util.DebugUtils;
import edu.uci.ics.sourcerer.codecrawler.util.UrlUtils;
import edu.uci.ics.sourcerer.codecrawler.util.html.HtmlUtils;
import edu.uci.ics.sourcerer.common.LoggerUtils;

/**
 * @author <a href="mailto:hahrot@yahoo.com">Huy A. Huynh</a>
 * @author <a href="bajracharya@gmail.com">Sushil Bajracharya</a>
 *
 */
public class SourceForgeParserPlugin extends ParserPlugin {
	
	private static final boolean debugEnabled = LoggerUtils.isDebugEnabled(SourceForgeParserPlugin.class);

	protected static class ProjectInfo {
		public String name;
		public String description;
		public String category;
		public String language;
		public String license;
		public String platform;
		public String releaseDate;
	}
	
	private Map<String, ProjectInfo> projectInfoMap = new HashMap<String, ProjectInfo>();
	
	public Set<Hit> parseHits(Parser htmlParser, String referringUrl) {	
		if (htmlParser.getURL().contains("showfiles.php") && 
				htmlParser.getURL().contains("package_id"))
			return parseDownloadPage(htmlParser);

		String[] levels = UrlUtils.getLevels(htmlParser.getURL());
		if (levels[1].equalsIgnoreCase("cvs") || levels[1].equalsIgnoreCase("svn"))
			return parseRepositoryPage(htmlParser);
		
		return null;
	}
	
	public ProjectInfo getProjectInfo(String projectName) {
		
		if (!projectInfoMap.containsKey(projectName))
		{
			//if the project is not found in the list, try parsing its homepage
			try {
				Parser htmlParser = new Parser("http://sourceforge.net/projects/" + projectName);
				ProjectInfo projectInfo = parseProjectHome(htmlParser);
				if (projectInfo != null) {
					parseProjectDevelop(htmlParser, projectInfo);
					projectInfoMap.put(projectName, projectInfo);
				}
			} catch (ParserException e) {}
		}
	
		return projectInfoMap.get(projectName);
	}
	
	private void parseProjectDevelop(Parser htmlParser, ProjectInfo projectInfo) throws ParserException {
		
		htmlParser.reset();
		htmlParser.setURL("http://sourceforge.net/projects/" + projectInfo.name + "/develop");
		
		htmlParser.reset();
		NodeList _plist = // _div.getChildren().extractAllNodesThatMatch(new NodeClassFilter(ParagraphTag.class));
			htmlParser.extractAllNodesThatMatch(
					new AndFilter(
							new TagNameFilter("P"),
							new HasParentFilter(new AndFilter(
									new NodeClassFilter(Div.class),
									new HasAttributeFilter("class","widget_body")))));
		SimpleNodeIterator _it = _plist.elements();
		
		while(_it.hasMoreNodes()){
			Node p = _it.nextNode();
			if(p.getChildren() == null) continue;
			
			 NodeList _nl = p.getChildren().extractAllNodesThatMatch(new NodeClassFilter(TagNode.class));
			 
			 if(_nl.elementAt(0).getClass()!=TagNode.class)
				 break;
			 TagNode b = (TagNode) _nl.elementAt(0);
			if (b.getRawTagName().equals("b")) {
				Node l = b.getNextSibling();
				if (l.getText().contains("Programming Languages:")) {

					StringBuffer buf = new StringBuffer();

					while (l != null) {
						if (!l.getClass().equals(LinkTag.class)){
							l = l.getNextSibling();
							continue;
						}
						buf.append(((LinkTag) l).getLinkText() + ";");
						l = l.getNextSibling();
					}
					projectInfo.language = buf.toString();
				}
			}
		}
	}
	
	private ProjectInfo parseProjectHome(Parser htmlParser) {
		//the div that contains the project details has one heading of text "Project Details"
		//and a bullet list (UL)
		NodeFilter projectDetailsDivFilter = new AndFilter(
				new HasChildFilter(new AndFilter(
						new NodeClassFilter(HeadingTag.class),
						new NodeFilter() {
							public boolean accept(Node node) {
								String text = HtmlUtils.extractText(node);
								return text.contains("Details");
							}
						})),
				new HasChildFilter(new NodeClassFilter(BulletList.class))
				);
		
		try {
			ProjectInfo projectInfo = new ProjectInfo();
			
			//get project name from the URL
			String[] levels = UrlUtils.getLevels(htmlParser.getURL());
			if (levels.length != 3) {
				if (debugEnabled)
					LoggerUtils.debug("SourceForgeParserPlugin: bad URL for project home: " + htmlParser.getURL());
				return null;
			}
			projectInfo.name = levels[2];
			
			// Find Project Description
			htmlParser.reset();
			Node descriptionDivNode = htmlParser.extractAllNodesThatMatch(
							new AndFilter(
									new NodeClassFilter(ParagraphTag.class),
									new HasAttributeFilter("class","pd-txt"))
							).elementAt(0);
			
			projectInfo.description = HtmlUtils.extractText(descriptionDivNode);
			
			// Find Project Detail
			htmlParser.reset();
			//get the div node
			Node projectDetailsDivNode = htmlParser.extractAllNodesThatMatch(
					new AndFilter(
							new NodeClassFilter(Div.class),
							new HasAttributeFilter("id","project-details"))).elementAt(0);
			
			//get the list of project property names
			NodeList propertyNamelist = projectDetailsDivNode.getChildren().extractAllNodesThatMatch(new NodeClassFilter(HeadingTag.class));
			//list of project property values
			NodeList valuesList = projectDetailsDivNode.getChildren().extractAllNodesThatMatch(new AndFilter(
					new NodeClassFilter(Div.class),
					new HasAttributeFilter("class","project-detail")));
			
			//loop through the list of property names
			SimpleNodeIterator iterator = propertyNamelist.elements();
			int i = 0;
			while (iterator.hasMoreNodes()) {
				Node node = iterator.nextNode();
	
				String property = HtmlUtils.extractText(node).trim();
				String value = "";
				Node valueNode = valuesList.elementAt(i);
				Node anchorNode = valueNode.getChildren().extractAllNodesThatMatch(new NodeClassFilter(LinkTag.class)).elementAt(0);
				if(anchorNode!=null){
					value = ((LinkTag) anchorNode).getLinkText().trim();
				} else
					value = valueNode.getFirstChild().getText().trim();
				
				if (property.contains("Topic"))
					projectInfo.category = value;
				else if (property.contains("Programming Language"))
					projectInfo.language = value;
				else if (property.contains("License"))
					projectInfo.license = value;
				else if (property.contains("Operating System"))
					projectInfo.platform = value;
				else if (property.contains("Release Date"))
					projectInfo.releaseDate = value;
				
				i++;
			}
			
			return projectInfo;
		
		} catch (ParserException e) {
			if (debugEnabled)
				LoggerUtils.debug("SourceForgeParserPlugin: ParserException while parsing. Stack trace: " +
						DebugUtils.getStackTrace(e));
			return null;
		} catch (NullPointerException e) {
			if (debugEnabled)
				LoggerUtils.debug("SourceForgeParserPlugin: some object not found while parsing. Stack trace: " +
						DebugUtils.getStackTrace(e));
			return null;
		}catch (ClassCastException e) {
			if (debugEnabled)
				LoggerUtils.debug("SourceForgeParserPlugin: some bad object found while parsing. Stack trace: " +
						DebugUtils.getStackTrace(e));
			return null;
		}
		
	}

	/**
	 * Parsing CVS/SVN page for checkout string
	 * @param htmlParser
	 * @return hit with checkout string
	 */
	public Set<Hit> parseRepositoryPage(Parser htmlParser) {
		String url = htmlParser.getURL();
		
		try {
			Set<Hit> result = new HashSet<Hit>();
			
			Node codeNode = htmlParser.extractAllNodesThatMatch(
						new TagNameFilter("code")
						).elementAt(0);
			
			StringBuffer _codeTextBuf = new StringBuffer();
			Node _nextSibling = codeNode.getNextSibling();
			while(true){
				
				if(_nextSibling == null || (_nextSibling instanceof  Tag && ((Tag) _nextSibling).isEndTag())){
					break;
				}
				
				if(_nextSibling.getClass().equals(TextNode.class)){
					_codeTextBuf.append(_nextSibling.getText().trim());
					_codeTextBuf.append(" ");
				}
				
				_nextSibling = _nextSibling.getNextSibling();
			}

			String rawCheckoutString = _codeTextBuf.toString();
			if(rawCheckoutString.startsWith("cvs")){
				rawCheckoutString = rawCheckoutString.replaceFirst("login[\\s]+cvs", "login; cvs");
			}
			
			String checkoutString = rawCheckoutString;
			
			// Matcher matcher = Pattern.compile("(\\w+).((svn)|(cvs)).sourceforge.net").matcher(checkoutString);
			Matcher matcher = Pattern.compile("([^\\s]+).((svn)|(cvs)).sourceforge.net").matcher(checkoutString);
			
			matcher.find();
			// remove cvs details
			String projectName = matcher.group(1).substring(matcher.group(1).indexOf("@")+1);
			// remove svn details
			projectName = projectName.replaceFirst("http[s]??://", "");
			
			if(projectName == null || projectName == "")
				return null;
			
			ProjectInfo projectInfo = getProjectInfo(projectName);
			Hit hit = new Hit(getIdGenerator().getNewHitId(), checkoutString);
			hit.setProjectName(projectInfo.name);
			hit.setProjectDescription(projectInfo.description);
			hit.setProjectCategory(projectInfo.category);
			hit.setProjectLicense(projectInfo.license);
			hit.setLanguage(projectInfo.language);
			hit.setPlatform(projectInfo.platform);
			hit.setContainerUrl(htmlParser.getURL());
			hit.setVersion("");
			hit.setReleaseDate(projectInfo.releaseDate);
			hit.setHitDate(new Date());
			hit.setSourceCode("");
			hit.setDescription(matcher.group(2));		//CVS or SVN
			
			result.add(hit);
			
			return result;
		} catch (ParserException e) {
			if (debugEnabled)
				LoggerUtils.error("SourceForgeParserPlugin: failed to get CVS/SVN checkout string for URL \"" + url + "\"");
			return null;
		}
	}
	
	
	public Set<Hit> parseDownloadPage(Parser htmlParser) {
		//get the project information
		ProjectInfo projectInfo = null;
		Node tableNode = null;
		try {
			htmlParser.reset();
			
			//find the Summary link to get the project name
			Node bulletListNode = htmlParser.extractAllNodesThatMatch(new AndFilter(
					new NodeClassFilter(BulletList.class),
					new HasAttributeFilter("class", "b-hornav")
					)).elementAt(0);
			
			Node summaryListNode = HtmlUtils.getTheIthNode(bulletListNode, 1,
					new AndFilter(
							new NodeClassFilter(Bullet.class),
							new HasChildFilter(new AndFilter(
									new NodeClassFilter(LinkTag.class),
									new NodeFilter() {
										public boolean accept(Node node) {
											return HtmlUtils.extractText(node).trim().equals("Summary");
										}}))));
			
			LinkTag projectLinkNode = (LinkTag)HtmlUtils.getTheIthNode(summaryListNode, 1, new NodeClassFilter(LinkTag.class));
			String link = projectLinkNode.extractLink();
			Matcher matcher = Pattern.compile("/projects/([\\S&&[^/]]+)").matcher(link);
			
			if (matcher.find()) {
				String projectName = matcher.group(1);
				projectInfo = getProjectInfo(projectName);
				if (projectInfo == null) {
					if (debugEnabled)
						LoggerUtils.error("SourceForgeParserPlugin: failed to get project info for project \"" + projectName + "\"");
					return null;
				}
			} else {
				if (debugEnabled)
					LoggerUtils.error("SourceForgeParserPlugin: failed to get project name from link \"" + link + "\"");
				return null;
			}
			
			//get the table containing downloads
			htmlParser.reset();
			tableNode = htmlParser.extractAllNodesThatMatch(new NodeClassFilter(TableTag.class)).elementAt(0); 
			if (tableNode == null) {
				if (debugEnabled)
					LoggerUtils.debug(this, "SourceForgeParserPlugin: no download links found for project \"" + projectInfo.name + "\"");
				return null;
			}
		} catch (ParserException e) {
			if (debugEnabled)
				LoggerUtils.error("SourceForgeParserPlugin: ParserException while parsing. Stack trace: " +
						DebugUtils.getStackTrace(e));
			return null;
		} catch (NullPointerException e) {
			if (debugEnabled)
				LoggerUtils.error("SourceForgeParserPlugin: Some elements cannot be found while parsing. " +
						"Stack trace: " + DebugUtils.getStackTrace(e));
			return null;
		}

		NodeList nodeList = tableNode.getChildren().extractAllNodesThatMatch(new NodeClassFilter(TableRow.class));
		SimpleNodeIterator iterator = nodeList.elements();
		
		Set<Hit> result = new HashSet<Hit>();
		
		String platform = null;
		String version = null;
		String releaseDate = null;
		String fileName = null;
		String sourceLink = null;
		String downloads = null;
		
		// process each package
		while (iterator.hasMoreNodes()) {
			Node node = iterator.nextNode();
			
			// process each row
			if(!node.getClass().equals(TableRow.class)){
				continue;
			}
			
			TableRow _row = (TableRow) node;
			TableColumn[] _cols = _row.getColumns();
			if (_cols == null || _cols.length<6)
				continue;
			
			Node _sourceLinkNode = _cols[0].getChildren().extractAllNodesThatMatch(
					new NodeClassFilter(LinkTag.class)).elementAt(0);
			
			if(_sourceLinkNode == null){
				version = _cols[0].getStringText().trim();
				continue;
			} 
			
			LinkTag _l = (LinkTag) _sourceLinkNode;
			sourceLink = _l.getLink();
			fileName = _l.getLinkText();
			platform = _cols[1].getStringText().trim();
			if (platform.equals("")) platform = "?";
			releaseDate = _cols[3].getStringText().trim();
			downloads = _cols[4].getStringText().trim();
			
			try {
				
				boolean source = (fileName.contains("Source")
						|| fileName.contains("source")
						|| fileName.contains("source") || fileName
						.contains("src"));

				Hit hit = new Hit(getIdGenerator().getNewHitId(), sourceLink);
				// project info
				hit.setProjectName(projectInfo.name);
				hit.setProjectDescription(projectInfo.description);
				hit.setProjectCategory(projectInfo.category);
				hit.setProjectLicense(projectInfo.license);
				hit.setLanguage(projectInfo.language);
				hit.setPlatform(projectInfo.platform);

				hit.setContainerUrl(htmlParser.getURL());

				// download file info
				hit.setVersion(version);
				hit.setReleaseDate(releaseDate);
				hit.setHitDate(new Date());
				hit.setSourceCode((source) ? CertaintyUtils
						.addCertaintyToValue("yes", 0.9) : "");
				hit.setDescription("platform:" + platform + " / " + "filename:" + fileName + " / " + "downloads:" + downloads );
				result.add(hit);

			} catch (NullPointerException e) {
				if (debugEnabled)
					LoggerUtils
							.error("SourceForgeParserPlugin: failed parsing download. Stack trace: "
									+ DebugUtils.getStackTrace(e));
			} catch (ClassCastException e) {
				if (debugEnabled)
					LoggerUtils
							.error("SourceForgeParserPlugin: failed parsing download. Stack trace: "
									+ DebugUtils.getStackTrace(e));
			}
			
		}// end process each package
		
		if (debugEnabled && result.isEmpty())
			LoggerUtils.debug(this, "SourceForgeParserPlugin: no download links found for project \"" + projectInfo.name + "\""); 
		
		return result;
	}
	
}