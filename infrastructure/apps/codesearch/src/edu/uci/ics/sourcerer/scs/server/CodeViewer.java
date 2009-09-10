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
package edu.uci.ics.sourcerer.scs.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author <a href="bajracharya@gmail.com">Sushil Bajracharya</a>
 * @created Jul 22, 2009
 *
 */

/**
 * @author jossher
 */


public class CodeViewer {
//	private static boolean debugEnabled = LoggerUtils.isDebugEnabled(JspSearchResultPresenter.class);
	
	private String codeContent;
	private int position;
	private int length;
	
	private static final String regex =
		"\\G(" + //don't skip anything
		"(\\s+)|" + //match whitespace
		"(/\\*\\*.*?\\*/)|" + //match javadoc comments
		"(/\\*.*?\\*/)|" + //match normal comments
		"(//.*?$)|" + //match line comments
		"(\\S+))" //match words
		;
	
	private static final String java_keywords =
		"\\b(" +
		"abstract|" +
		"assert|" +
		"boolean|" +
		"break|" +
		"byte|" +
		"case|" +
		"catch|" +
		"char|" +
		"class|" +
		"const|" +
		"continue|" +
		"default|" +
		"do|" +
		"double|" +
		"else|" +
		"enum|" +
		"extends|" +
		"false|" +
		"final|" +
		"finally|" +
		"float|" +
		"for|" +
		"goto|" +
		"if|" +
		"implements|" +
		"import|" +
		"instanceof|" +
		"int|" +
		"interface|" +
		"long|" +
		"native|" +
		"new|" +
		"null|" +
		"package|" +
		"private|" +
		"protected|" +
		"public|" +
		"return|" + 
		"short|" +
		"static|" +
		"strictfp|" +
		"super|" +
		"switch|" +
		"synchronized|" +
		"this|" +
		"throw|" +
		"throws|" +
		"transient|" +
		"true|" +
		"try|" +
		"void|" +
		"volatile|" +
		"while" +
		")\\b"; 
	
	private static final Pattern javaPattern = Pattern.compile(regex, Pattern.DOTALL | Pattern.MULTILINE);
	private static final Pattern javaKeywordPattern = Pattern.compile(java_keywords);
	private Pattern keywordPattern = null;
	private String searchKeywords = null;
	
	public CodeViewer(String codeContent, String keywords, String position, String length) {
		this.codeContent = codeContent;
		this.position = Integer.parseInt(position);
		this.length = Integer.parseInt(length);
		
		StringBuffer keywordPatternBuffer = new StringBuffer();
		for (String keyword : keywords.split(",")) {
			if (!keyword.matches("AND|OR")) {
				keywordPatternBuffer.append(keyword + "|");
			}
		}
		if (keywordPatternBuffer.length() > 0) {
			keywordPatternBuffer.deleteCharAt(keywordPatternBuffer.length() - 1);
		}
		searchKeywords = "(?i)" + keywordPatternBuffer.toString();
		keywordPattern = Pattern.compile(java_keywords + "|" + searchKeywords, Pattern.CASE_INSENSITIVE); 
	}
	
	public CodeViewer(String codeContent, List<String> keywords) {
		this.codeContent = codeContent;
		this.position = 0;
		this.length = codeContent.length();
		
		StringBuffer keywordPatternBuffer = new StringBuffer();
		for (String keyword : keywords) {
			if (!keyword.matches("AND|OR")) {
				keywordPatternBuffer.append(keyword + "|");
			}
		}
		if (keywordPatternBuffer.length() > 0) {
			keywordPatternBuffer.deleteCharAt(keywordPatternBuffer.length() - 1);
		}
		searchKeywords = "(?i)" + keywordPatternBuffer.toString();
		keywordPattern = Pattern.compile(java_keywords + "|" + searchKeywords, Pattern.CASE_INSENSITIVE); 
	}
	
	public String getHighightedCode(){
		StringWriter sw = new StringWriter();
		render(sw);
		sw.flush();
		return sw.toString();
	}
	
	public static String highlightInlined(String code, List<String> queryTerms){
		CodeViewer cv = new CodeViewer(code, queryTerms);
		return cv.getHighightedCode();
		
	}
	
	public void render(Writer writer) {
		
		String code = this.codeContent;//"";//CerkWebUtil.getCodeFromFile(codeContent);
		
		String before = code.substring(0, position);
		String during = code.substring(position, position + length);
		String after = code.length() > position + length ? code.substring(position + length) : ""; 
		
		before = before.replaceAll("<", "&lt;");
		before = before.replaceAll(">", "&gt;");
		before = before.replaceAll(" ", "&nbsp;");
		before = before.replaceAll("\t", "&nbsp;&nbsp;&nbsp;&nbsp;");
		before = before.replaceAll("\n", "<br/>");
		
		during = during.replaceAll("<", "&lt;");
		during = during.replaceAll(">", "&gt;");
		during = during.replaceAll(" ", "&nbsp;");
		during = during.replaceAll("\t", "&nbsp;&nbsp;&nbsp;&nbsp;");
		during = during.replaceAll("\n", "<br/>");
		
		after = after.replaceAll("<", "&lt;");
		after = after.replaceAll(">", "&gt;");
		after = after.replaceAll(" ", "&nbsp;");
		after = after.replaceAll("\t", "&nbsp;&nbsp;&nbsp;&nbsp;");
		after = after.replaceAll("\n", "<br/>");
	
		try {
			writer.write(
					highlight(before, false) +
					"<div class=\"search-keyword-entity\">" +
					"<a id=\"search-entity\" name=\"search-entity\"></a>" +
					highlight(during, true) +
					"</div>" +
					highlight(after, false)
					);
		} catch (IOException e) {
//			if (debugEnabled) {
//				LoggerUtils.error(e.getMessage());
//				StringWriter s = new StringWriter();
//				e.printStackTrace(new PrintWriter(s));
//				LoggerUtils.error(s.toString());
//			}
//			
			throw new RuntimeException(e);
		} catch (Exception e) {
//			if (debugEnabled) {
//				LoggerUtils.error(e.getMessage());
//				StringWriter s = new StringWriter();
//				e.printStackTrace(new PrintWriter(s));
//				LoggerUtils.error(s.toString());
//			}
		}
	}
	
	
	private String highlight(String input, boolean keyword) {
		StringBuffer output = new StringBuffer();
		
		Matcher matcher = javaPattern.matcher(input);
		
		while (matcher.find()) {
			String match = matcher.group();
			if (match.matches("(?s)/\\*\\*.*?\\*/")) {
				output.append("<span class=\"javadoc-comment\">");
				output.append(keyword ? match.replaceAll(searchKeywords, "<span class=\"search-keyword\">$0</span>") : match);
				output.append("</span>");
			} else if (match.matches("(?s)(?m)(/\\*.*?\\*/)|(//.*?$)")) {
				output.append("<span class=\"java-comment\">");
				output.append(keyword ? match.replaceAll(searchKeywords, "<span class=\"search-keyword\">$0</span>") : match);
				output.append("</span>");
			} else {
				if (keyword) {
					Matcher keywordMatcher = keywordPattern.matcher(match);
					int start = 0;
					while (keywordMatcher.find()) {
						output.append(match.substring(start, keywordMatcher.start()));
						start = keywordMatcher.end();
						if (keywordMatcher.group().matches(java_keywords)) {
							output.append("<span class=\"java-keyword\">" + keywordMatcher.group() + "</span>");												
						} else {
							output.append("<span class=\"search-keyword\">" + keywordMatcher.group() + "</span>");
						}
					}
					output.append(match.substring(start));
				} else {
					Matcher keywordMatcher = javaKeywordPattern.matcher(match);
					output.append(keywordMatcher.replaceAll("<span class=\"java-keyword\">$0</span>"));
				}
			}
		}
		
		return output.toString();
	}
	
	public static String highlight(String text) {
		StringBuffer output = new StringBuffer();
		
		text = text.replaceAll("<", "&lt;");
		text = text.replaceAll(">", "&gt;");
		text = text.replaceAll(" ", "&nbsp;");
		text = text.replaceAll("\t", "&nbsp;&nbsp;&nbsp;&nbsp;");
		text = text.replaceAll("\n", "<br/>");
		
		Matcher matcher = javaPattern.matcher(text);
		
		while (matcher.find()) {
			String group;
			if ((group = matcher.group(3))!= null) { //javadoc comment
				output.append("<span class=\"javadoc-comment\">");
				output.append(group);
				output.append("</span>");
			} else if ((group = matcher.group(4)) != null) { //normal comment
				output.append("<span class=\"java-comment\">");
				output.append(group);
				output.append("</span>");
			} else if ((group = matcher.group(5)) != null) { //line comment
				output.append("<span class=\"java-comment\">");
				output.append(group);
				output.append("</span>");
			} else if ((group = matcher.group(6)) != null) { //word
				output.append(javaKeywordPattern.matcher(group).replaceAll("<span class=\"java-keyword\">$0</span>"));
			} else {
				output.append(matcher.group());
			}
		}
		
		return output.toString();
	}
}

