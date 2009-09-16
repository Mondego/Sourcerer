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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.AndFilter;
import org.htmlparser.filters.HasChildFilter;
import org.htmlparser.filters.HasParentFilter;
import org.htmlparser.filters.NodeClassFilter;
import org.htmlparser.filters.OrFilter;
import org.htmlparser.nodes.TextNode;
import org.htmlparser.tags.BodyTag;
import org.htmlparser.tags.Bullet;
import org.htmlparser.tags.BulletList;
import org.htmlparser.tags.Div;
import org.htmlparser.tags.HeadingTag;
import org.htmlparser.tags.Html;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.tags.ParagraphTag;
import org.htmlparser.tags.TableColumn;
import org.htmlparser.tags.TableHeader;
import org.htmlparser.tags.TableRow;
import org.htmlparser.tags.TableTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.NodeTreeWalker;
import org.htmlparser.util.ParserException;

import edu.uci.ics.sourcerer.codecrawler.db.Hit;
import edu.uci.ics.sourcerer.codecrawler.parserplugin.ParserPlugin;
import edu.uci.ics.sourcerer.codecrawler.util.CertaintyUtils;
import edu.uci.ics.sourcerer.codecrawler.util.CrawlerProperties;
import edu.uci.ics.sourcerer.codecrawler.util.StringFormatUtils;
import edu.uci.ics.sourcerer.codecrawler.util.UrlUtils;
import edu.uci.ics.sourcerer.codecrawler.util.html.HtmlUtils;

/**
 * @author <a href="mailto:hahrot@yahoo.com">Huy A. Huynh</a>
 * @author <a href="mailto:bajracharya@gmail.com">Sushil Bajracharya</a>
 * 
 * Dec 16, 2008 @sushil Rewrote the plugin to extract the right SCM links
 */
public class JavaNetParserPlugin extends ParserPlugin {
	
	private class DocumentListTableRow {
		//anti encapsulation here, but OK...
		public String fileUrl;
		public String fileName;
		public String modifiedBy;
		public String description;
		public String infoUrl;
	}
	
	//**********************************************************
	
	private String[] keywords = new String[0];
	
	public JavaNetParserPlugin() {
		String keywordsValue = CrawlerProperties.getInstance().getProperty(
				CrawlerProperties.CRAWLER + ".parserplugin.JavaNetParserPlugin.keywords",
				"java;eclipse;ant;written in java;eclipse plug-in;j2ee;jdk;jedit");
		StringTokenizer tokenizer = new StringTokenizer(keywordsValue, ";");
		ArrayList<String> words = new ArrayList<String>();
		while (tokenizer.hasMoreTokens())
			words.add(tokenizer.nextToken().replace(" ", "\\s+"));	//regex: we can tolerate multiple spaces between words
		keywords = words.toArray(keywords);
	}

	public Set<Hit> parseHits(Parser htmlParser, String referringUrl) {
		HashSet<Hit> hits = new HashSet<Hit>();
		
		String url = htmlParser.getURL();
		if (url.contains("servlets/ProjectDocumentList"))
			hits.addAll(processDocumentList(htmlParser, referringUrl));
		else if (url.contains("source/browse/"))
			hits.addAll(processSourceBrowse(htmlParser, referringUrl));
		return hits;
	}
	
	private Set<Hit> processDocumentList(Parser htmlParser, String referringUrl) {
		htmlParser.reset();
		HashSet<Hit> hits = new HashSet<Hit>();
		
		Pattern versionPattern = Pattern.compile("\\d+([_.][a-zA-Z\\d]+)*");
		
		//get table header nodes, with text: Name, Status, Modified by (3 should be enough...)
		TableTag tableNode = null;
		try {
			NodeList tableHeaderNodeList = htmlParser.extractAllNodesThatMatch(new NodeClassFilter(TableHeader.class));
			for (Node tableHeaderNode : tableHeaderNodeList.toNodeArray()) {
				tableNode = validateDocumentListTable(tableHeaderNode);
				if (tableNode != null)
					break;
			}
			if (tableNode == null)
				return hits;
		} catch (ParserException e) {
			return hits;
		}
		
		// the title of the package might contain version number
		String headingVersion = null;
		String headingText = "";
		NodeList headingNodeList = tableNode.getParent().getChildren();
		for (Node headingNode : headingNodeList.toNodeArray()) {
			if (headingNode instanceof HeadingTag) {
				headingText = headingNode.getFirstChild().getText().trim();
				Matcher matcher = versionPattern.matcher(headingText);
				if (matcher.find())
					headingVersion = matcher.group(0);
				break;
			}
		}
		
		//get all the rows
		ArrayList<DocumentListTableRow> tableRows = new ArrayList<DocumentListTableRow>();
		TableHeader[] tableHeaders = null;
		for (TableRow tableRow : tableNode.getRows()) {
			TableHeader[] headers = tableRow.getHeaders();
			if (headers.length > 0) {
				tableHeaders = headers;
			} else {
				DocumentListTableRow row = extractDocumentListRow(tableRow, tableHeaders);
				if (row != null)
					tableRows.add(row);
			}
		}

		//loop through all files in here
		//for each, go to the Info link and the Project home page
		for (DocumentListTableRow row : tableRows) {
			if (row.fileUrl == null)
				continue;
			
			//we're taking only these extensions
			if (!(row.fileUrl.toLowerCase().endsWith(".zip") 
					|| row.fileUrl.toLowerCase().endsWith(".jar")
					|| row.fileUrl.toLowerCase().endsWith(".war")
					|| row.fileUrl.toLowerCase().endsWith(".tar") 
					|| row.fileUrl.toLowerCase().endsWith(".tar.gz")
					|| row.fileUrl.toLowerCase().endsWith(".tar.bz2")))
				continue;
			
			Hit hit = new Hit(getIdGenerator().getNewHitId(), row.fileUrl);
			hit.setHitDate(new Date());
			
			hit.setContainerUrl(htmlParser.getURL());
			
			Date date = StringFormatUtils.parseDateIn("MMM dd, yyyy", row.modifiedBy);
			if (date != null) {
				hit.setReleaseDate(StringFormatUtils.formatDate("yyyy-MMM-dd", date));
			}
			
			hit.setDescription(row.description);
			if (row.description.toLowerCase().contains("source")
					|| row.fileName.toLowerCase().contains("src")
					|| row.fileName.toLowerCase().contains("source")
					|| headingText.toLowerCase().contains("source"))
				hit.setSourceCode(CertaintyUtils.addCertaintyToValue("yes", 0.9D));
			
			Matcher versionMatcher = versionPattern.matcher(row.fileName);
			if (versionMatcher.find()) {
				hit.setVersion(CertaintyUtils.addCertaintyToValue(
						versionMatcher.group(0),
						0.9D));
			} else if (headingVersion != null)
				hit.setVersion(CertaintyUtils.addCertaintyToValue(
						headingVersion,
						0.9D));
			
			String projectHome = UrlUtils.getHostName(htmlParser.getURL());
			updateProjectInfo(hit, "http://" + projectHome + "/");
			hit.setProjectName(projectHome.substring(0, projectHome.indexOf(".")));
			
			hits.add(hit);
		}
		
		return hits;
	}
	
	/**
	 * Visit the project home page, parse the info there, and update
	 * this information into the Hit
	 */
	private void updateProjectInfo(Hit hit, String projectHome) {
		try {
			Parser parser = new Parser(projectHome);
			NodeList tableHeaderNodeList = parser.extractAllNodesThatMatch(new NodeClassFilter(TableHeader.class));
			for (Node tableHeaderNode : tableHeaderNodeList.toNodeArray()) {
				TableTag tableNode = validateProjectInfoTable(tableHeaderNode);
				if (tableNode != null) {
					TableRow[] rows = tableNode.getRows();
					for (TableRow row : rows) {
						if (row.getHeaderCount() ==0 || row.getColumnCount() == 0)
							continue;
						TableHeader header = row.getHeaders()[0];
						TableColumn column = row.getColumns()[0];
						String headerString = header.getFirstChild().getText().toLowerCase();
						
						if (headerString.contains("summary")) {
							hit.setProjectDescription(column.getFirstChild().getText().trim());
						} else if (headerString.contains("categor")) {	//categories or category
							Node[] children = column.getChildren().extractAllNodesThatMatch(new NodeClassFilter(TextNode.class), true).toNodeArray();
							String tmp = "";
							for (Node child : children)
								tmp += child.getText().trim().replace(",", ";");
							hit.setProjectCategory(tmp);
						} else if (headerString.contains("license")) {
							String license = "";
							Node[] children = column.getChildren().extractAllNodesThatMatch(
									new OrFilter(new NodeClassFilter(LinkTag.class), new NodeClassFilter(TextNode.class)), true).toNodeArray();
							for (Node child : children)  {
								if (child instanceof LinkTag) {
									license += ((LinkTag)child).getLink() + ";";
								} else if (child instanceof TextNode) {
									String tmp = child.getText().trim();
									if (tmp.length() > 0)
										license +=  tmp + ";";
								}
							}
							hit.setProjectLicense(license);
						}
					}	//end of for row
					break;
				}
			}	//end of for tableHeaderNode
			
			parser.reset();
			Node[] textNodes = parser.extractAllNodesThatMatch(new NodeClassFilter(TextNode.class)).toNodeArray();
			String hitKeywords = "";
			int totalCount = 0;
			for (String keyword : keywords) {
				Pattern pattern = Pattern.compile("\\b" + keyword + "\\b", Pattern.CASE_INSENSITIVE);
				int count = 0;
				for (Node textNode : textNodes) {
					Matcher matcher = pattern.matcher(textNode.getText());
					while (matcher.find())
						count++;
				}
				if (count > 0) {
					if (hitKeywords.length() == 0)
						hitKeywords += keyword + "=" + count;
					else
						hitKeywords += ";" + keyword + "=" + count;
				}
				totalCount += count;
			}
			hit.setKeywords(hitKeywords);
			if (totalCount != 0)
				hit.setLanguage(CertaintyUtils.addCertaintyToValue("Java", 2-Math.pow(2, 1.0D/(Math.pow(totalCount, 0.7)+0.4))));
		} catch (ParserException e) {
		}
	}
	
	private TableTag validateDocumentListTable(Node tableHeaderNode) {
		if (!(tableHeaderNode instanceof TableHeader))
			return null;	
		if (!tableHeaderNode.getFirstChild().getText().toLowerCase().contains("modified by"))
			return null;
		if (tableHeaderNode.getParent() == null || tableHeaderNode.getParent().getParent() == null)
			return null;
		if (!(tableHeaderNode.getParent() instanceof TableRow)
				|| !(tableHeaderNode.getParent().getParent() instanceof TableTag))
			return null;
				
		
		Node parent = tableHeaderNode.getParent();
		if (parent == null)
			return null;
		Node[] siblings = parent.getChildren().toNodeArray();
		
		
		boolean nameFound = false;
		boolean statusFound = false;
		boolean sizeFound = false;
		for (Node sibling : siblings) {
			if ((sibling instanceof TableHeader)) {
				String headerString = sibling.getFirstChild().getText().toLowerCase();
				if (headerString.contains("name"))
					nameFound = true;
				if (headerString.contains("size"))
					sizeFound = true;
				if (headerString.contains("status"))
					statusFound = true;
			}
		}

		if (nameFound && sizeFound && statusFound)
			return (TableTag)tableHeaderNode.getParent().getParent();
		else
			return null;
	}
	
	private TableTag validateProjectInfoTable(Node tableHeaderNode) {
		if (!(tableHeaderNode instanceof TableHeader))
			return null;
		if (!tableHeaderNode.getFirstChild().getText().toLowerCase().contains("license"))
			return null;
		if (tableHeaderNode.getParent() == null || tableHeaderNode.getParent().getParent() == null)
			return null;
		if (!(tableHeaderNode.getParent() instanceof TableRow)
				|| !(tableHeaderNode.getParent().getParent() instanceof TableTag))
			return null;
		
		TableTag tableNode = (TableTag)tableHeaderNode.getParent().getParent();
		TableRow[] rows = tableNode.getRows();
		boolean summaryFound = false;
		boolean categoriesFound = false;
		boolean ownersFound = false;
		for (TableRow row : rows) {
			TableHeader header = row.getHeaders()[0];
			if (header!=null) {
				String headerString = header.getFirstChild().getText().toLowerCase();
				if (headerString.contains("summary")) {
					summaryFound = true;
				} else if (headerString.contains("categor")) {
					categoriesFound = true;
				} else if (headerString.contains("owner")) {
					ownersFound = true;
				}
			}
		}
		
		if (summaryFound && categoriesFound && ownersFound)
			return tableNode;
		else
			return null;
	}
	
	private DocumentListTableRow extractDocumentListRow(TableRow tableRow, TableHeader[] headers) {
		TableColumn[] columns = tableRow.getColumns();
		if (columns.length == 0)
			return null;
		
		DocumentListTableRow row = new DocumentListTableRow();
		for (int i = 0; i < Math.min(headers.length, columns.length); i++) {
			String headerString = headers[i].getFirstChild().getText().toLowerCase();
			if (headerString.contains("name")) {
				Node[] children = columns[i].getChildren().extractAllNodesThatMatch(
						new NodeClassFilter(LinkTag.class), true).toNodeArray();
				for (Node child : children) {
					if (child instanceof LinkTag) {
						LinkTag link = (LinkTag)child;
						row.fileName = link.getLinkText().trim();
						row.fileUrl = link.getLink();
					}
				}
			} else if (headerString.contains("modified by")) {
				Node[] children = columns[i].getChildren().extractAllNodesThatMatch(
						new NodeClassFilter(TextNode.class), true).toNodeArray();
				row.modifiedBy = "";
				for (Node child : children)
					row.modifiedBy += child.getText();
				row.modifiedBy = row.modifiedBy.trim();
			} else if (headerString.contains("description")) {
				Node[] children = columns[i].getChildren().extractAllNodesThatMatch(
						new NodeClassFilter(TextNode.class), true).toNodeArray();
				row.description = "";
				for (Node child : children)
					row.description += child.getText();
				row.description = row.description.trim();
			} else if (headerString.contains("status")
					|| headerString.contains("size")
					|| headerString.contains("reservations")) {
				//ignored
			} else {
				Node[] children = columns[i].getChildren().extractAllNodesThatMatch(
						new NodeClassFilter(LinkTag.class), true).toNodeArray();
				for (Node child : children) {
					if (child instanceof LinkTag) {
						LinkTag link = (LinkTag)child;
						if (link.getLinkText().toLowerCase().contains("info"))
							row.infoUrl = link.getLink();
					}
				}
			}	//end of if
		}	//end of for
		return row;
	}
	
	private Set<Hit> processSourceBrowse(Parser htmlParser, String referringUrl) {

		String _scmCommand1 = "";
		String _scmCommand2 = "";
		
		HashSet<Hit> hits = new HashSet<Hit>();
		htmlParser.reset();
		
		try {
			NodeList _nodes = htmlParser.extractAllNodesThatMatch(
					new AndFilter(new NodeFilter() {
										private static final long serialVersionUID = 4746917834152728696L;

										public boolean accept(Node node) {
											return node.getText().trim().startsWith("cvs") || node.getText().trim().startsWith("svn") ;
										}
								  },
								 new HasParentFilter(new AndFilter(
									new NodeClassFilter(ParagraphTag.class),
									new HasParentFilter(new AndFilter(
										new NodeClassFilter(Bullet.class),
										new HasParentFilter(new AndFilter(
												new NodeClassFilter(BulletList.class),
												new HasParentFilter(new AndFilter(
														new NodeClassFilter(Div.class),
														new HasParentFilter(new AndFilter(
																new NodeClassFilter(Div.class),
																new HasParentFilter(new AndFilter(
																		new NodeClassFilter(Div.class),
																		new HasParentFilter(new AndFilter(
																				new NodeClassFilter(TableColumn.class),
																				new HasParentFilter(new AndFilter(
																						new NodeClassFilter(TableRow.class),
																						new HasParentFilter(new AndFilter(
																								new NodeClassFilter(TableTag.class),
																								new HasParentFilter(new AndFilter(
																										new NodeClassFilter(BodyTag.class),
																										new HasParentFilter(
																												new NodeClassFilter(Html.class))))))))))))))))))))))));
			
		if (_nodes!=null && _nodes.size()==1){
			_scmCommand1 = _nodes.elementAt(0).getText().trim();
		} else if(_nodes!=null && _nodes.size()>=2){
			_scmCommand1 = _nodes.elementAt(0).getText().trim();
			_scmCommand2 = _nodes.elementAt(1).getText().trim();
		} else {
			// log this ..
		}
		
		} catch (ParserException e) {
			// TODO log this
			// e.printStackTrace();
		} 
				
		/// xpath into cvs info in html tree
		// /html/body/table/tbody/tr/td[2]/div/div[2]/div/ul/li[2]/p[2]/code --> first scm command xpath
		// /html/body/table/tbody/tr/td[2]/div/div[2]/div/ul/li[2]/p[4]/code --> second scm command xpath
		
		/// xpath into svn info in html tree
		// /html/body/table/tbody/tr/td[2]/div/div[2]/div/ul/li[2]/p[2]/code
		
		/// cvs example:
		// <code>cvs -d :pserver:username@cvs.dev.java.net:/cvs login</code> 
		// <code> cvs -d :pserver:username@cvs.dev.java.net:/cvs checkout open-esb</code>
		
		/// svn example:
		// <code>svn checkout https://esb-console.dev.java.net/svn/esb-console/trunk esb-console --username username</code> 
		
		String projectHome = UrlUtils.getHostName(htmlParser.getURL());
		
		String _scmCommand = _scmCommand1.length()>0?(_scmCommand1 + (_scmCommand2.length()>0?("; "+_scmCommand2):"") ):"";
		
		if (_scmCommand.length()<1)
			_scmCommand = "cvs -d :pserver:guest@cvs.dev.java.net:/cvs login" +
								"; cvs -d :pserver:guest@cvs.dev.java.net:/cvs checkout " + 
									projectHome.substring(0, projectHome.indexOf(".")); 
		
		
		Hit hit = new Hit(getIdGenerator().getNewHitId(), _scmCommand);
		
		hit.setHitDate(new Date());
		
		hit.setContainerUrl(htmlParser.getURL());
		
		updateProjectInfo(hit, "http://" + projectHome + "/");
		hit.setProjectName(projectHome.substring(0, projectHome.indexOf(".")));
		
		hits.add(hit);
		
		return hits;
	}

}
