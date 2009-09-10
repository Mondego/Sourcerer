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
package edu.uci.ics.sourcerer.scs.common;

import java.util.Collection;
import java.util.Arrays;
import java.util.HashSet;

// Note: This class is copied from another open source project
//       that was found using Sourcerer code search.

/**
 * @author <a href="bajracharya@gmail.com">Sushil Bajracharya</a>
 * @created Sep 10, 2009
 *
 */
public class JavaToHtml {

	// Could use a J2SE 5.0 enum instead of this.
	public abstract static class State {
		public final static int COMMENT_JAVADOC = 0;
		public final static int COMMENT_MULTI = 1;
		public final static int COMMENT_SINGLE = 2;
		public final static int DEFAULT = 3;
		public final static int KEYWORD = 4;
		public final static int IMPORT_NAME = 5;
		public final static int PACKAGE_NAME = 6;
		public final static int QUOTE_DOUBLE = 8;
		public final static int QUOTE_SINGLE = 9;
	}

	// TODO: Set a style for JavaDoc tags
	// private static final Collection javaJavaDocTags;
	private static final Collection javaKeywords;
	private static final Collection javaPrimitiveLiterals;
	private static final Collection javaPrimitiveTypes;

	static {
		// TODO: Probably need to add anything new in J2SE 5.0
		// final String javaJavaDocTagsArray[] = { "see", "author", "version",
		// "param", "return", "exception",
		// "deprecated", "throws", "link", "since", "serial", "serialField",
		// "serialData", "beaninfo" };
		final String[] javaKeywordsArray = { "abstract", "assert", "break",
				"case", "catch", "class", "const", "continue", "default", "do",
				"else", "extends", "final", "finally", "for", "goto", "if",
				"interface", "implements", "import", "instanceof", "native",
				"new", "package", "private", "protected", "public", "return",
				"static", "strictfp", "super", "switch", "synchronized",
				"this", "throw", "throws", "transient", "try", "volatile",
				"while" };
		final String javaPrimitiveTypesArray[] = { "boolean", "byte", "char",
				"double", "float", "int", "long", "short", "void" };
		final String javaPrimitiveLiteralsArray[] = { "false", "null", "true" };

		// javaJavaDocTags = new HashSet(Arrays.asList(javaJavaDocTagsArray));
		javaKeywords = new HashSet(Arrays.asList(javaKeywordsArray));
		javaPrimitiveTypes = new HashSet(Arrays.asList(javaPrimitiveTypesArray));
		javaPrimitiveLiterals = new HashSet(Arrays
				.asList(javaPrimitiveLiteralsArray));
	}

	private int state = State.DEFAULT;

	private static String escapeEntity(final char character) {
		if (character == '&')
			return "&amp;";
		else if (character == '<')
			return "&lt;";
		else if (character == '>')
			return "&gt;";
		else if (character == '\t')
			return "&nbsp;&nbsp;&nbsp;&nbsp;";
		else if (character == ' ')
			return "&nbsp;";
		else
			return new Character(character).toString();
	}

	/**
	 * Add HTML colorization to a block of Java code.
	 * 
	 * @param text
	 *            The block of Java code.
	 * @return The same block of Java code with added span tags. Newlines are
	 *         preserved.
	 */
	public String process(final String text) {
		if (text == null)
			throw new IllegalArgumentException("\"text\" can not be null.");

		StringBuffer ret = new StringBuffer();

		// This look is really complicated because it preserves all
		// combinations of \r, \n, \r\n, and \n\r
		int begin, end, nextCR;
		begin = 0;
		end = text.indexOf('\n', begin);
		nextCR = text.indexOf('\r', begin);
		if ((nextCR != -1) && ((end == -1) || (nextCR < end)))
			end = nextCR;
		while (end != -1) {
			ret.append(processLine(text.substring(begin, end)) + "<br/>");

			if ((end + 1 < text.length())
					&& ((text.charAt(end + 1) == '\n') || (text.charAt(end + 1) == '\r'))) {
				ret.append(text.substring(end, end + 1));
				begin = end + 2;
			} else {
				ret.append(text.charAt(end));
				begin = end + 1;
			}

			end = text.indexOf('\n', begin);
			nextCR = text.indexOf('\r', begin);
			if ((nextCR != -1) && ((end == -1) || (nextCR < end)))
				end = nextCR;
		}
		ret.append(processLine(text.substring(begin)));

		return ret.toString();
	}

	/**
	 * Add HTML colorization to a single line of Java code.
	 * 
	 * @param line
	 *            One line of Java code.
	 * @return The same line of Java code with added span tags.
	 */
	private String processLine(final String line) {
		if (line == null)
			throw new IllegalArgumentException("\"line\" can not be null.");
		if ((line.indexOf('\n') != -1) || (line.indexOf('\r') != -1))
			throw new IllegalArgumentException(
					"\"line\" can not contain newline or carriage return characters.");

		StringBuffer ret = new StringBuffer();
		int currentIndex = 0;

		while (currentIndex != line.length()) {
			if (state == State.DEFAULT) {
				if ((currentIndex + 2 < line.length())
						&& line.substring(currentIndex, currentIndex + 3)
								.equals("/**")) {
					state = State.COMMENT_JAVADOC;

				} else if ((currentIndex + 1 < line.length())
						&& line.substring(currentIndex, currentIndex + 2)
								.equals("/*")) {
					state = State.COMMENT_MULTI;

				} else if ((currentIndex + 1 < line.length())
						&& (line.substring(currentIndex, currentIndex + 2)
								.equals("//"))) {
					state = State.COMMENT_SINGLE;

				} else if (Character.isJavaIdentifierStart(line
						.charAt(currentIndex))) {
					state = State.KEYWORD;

				} else if (line.charAt(currentIndex) == '\'') {
					state = State.QUOTE_SINGLE;

				} else if (line.charAt(currentIndex) == '"') {
					state = State.QUOTE_DOUBLE;

				} else {
					// Default: No highlighting.
					ret.append(escapeEntity(line.charAt(currentIndex++)));
				}
			} // End of State.DEFAULT

			else if ((state == State.COMMENT_MULTI)
					|| (state == State.COMMENT_JAVADOC)) {
				// Print everything from the current character until the
				// closing */ No exceptions.
				ret.append("<span class=\"comment\">");
				while ((currentIndex != line.length())
						&& !((currentIndex + 1 < line.length()) && (line
								.substring(currentIndex, currentIndex + 2)
								.equals("*/")))) {
					ret.append(escapeEntity(line.charAt(currentIndex++)));
				}
				if (currentIndex == line.length()) {
					ret.append("</span>");
				} else {
					ret.append("*/</span>");
					state = State.DEFAULT;
					currentIndex += 2;
				}
			} // End of State.COMMENT_MULTI

			else if (state == State.COMMENT_SINGLE) {
				// Print everything from the current character until the
				// end of the line
				ret.append("<span class=\"comment\">");
				while (currentIndex != line.length()) {
					ret.append(escapeEntity(line.charAt(currentIndex++)));
				}
				ret.append("</span>");
				state = State.DEFAULT;

			} // End of State.COMMENT_SINGLE

			else if (state == State.KEYWORD) {
				StringBuffer tmp = new StringBuffer();
				do {
					tmp.append(line.charAt(currentIndex++));
				} while ((currentIndex != line.length())
						&& (Character.isJavaIdentifierPart(line
								.charAt(currentIndex))));
				if (javaKeywords.contains(tmp.toString()))
					ret.append("<span class=\"keyword\">" + tmp + "</span>");
				else if (javaPrimitiveLiterals.contains(tmp.toString()))
					ret.append("<span class=\"keyword\">" + tmp + "</span>");
				else if (javaPrimitiveTypes.contains(tmp.toString()))
					ret.append("<span class=\"keyword\">" + tmp + "</span>");
				else
					ret.append(tmp);
				if (tmp.toString().equals("import"))
					state = State.IMPORT_NAME;
				else if (tmp.toString().equals("package"))
					state = State.PACKAGE_NAME;
				else
					state = State.DEFAULT;
			} // End of State.KEYWORD

			else if (state == State.IMPORT_NAME) {
				ret.append(escapeEntity(line.charAt(currentIndex++)));
				state = State.DEFAULT;
			} // End of State.IMPORT_NAME

			else if (state == State.PACKAGE_NAME) {
				ret.append(escapeEntity(line.charAt(currentIndex++)));
				state = State.DEFAULT;
			} // End of State.PACKAGE_NAME

			else if (state == State.QUOTE_DOUBLE) {
				// Print everything from the current character until the
				// closing ", checking for \"
				ret.append("<span class=\"string\">");
				do {
					ret.append(escapeEntity(line.charAt(currentIndex++)));
				} while ((currentIndex != line.length())
						&& (!(line.charAt(currentIndex) == '"') || ((line
								.charAt(currentIndex - 1) == '\\') && (line
								.charAt(currentIndex - 2) != '\\'))));
				if (currentIndex == line.length()) {
					ret.append("</span>");
				} else {
					ret.append("\"</span>");
					state = State.DEFAULT;
					currentIndex++;
				}
			} // End of State.QUOTE_DOUBLE

			else if (state == State.QUOTE_SINGLE) {
				// Print everything from the current character until the
				// closing ', checking for \'
				ret.append("<span class=\"string\">");
				do {
					ret.append(escapeEntity(line.charAt(currentIndex++)));
				} while ((currentIndex != line.length())
						&& (!(line.charAt(currentIndex) == '\'') || ((line
								.charAt(currentIndex - 1) == '\\') && (line
								.charAt(currentIndex - 2) != '\\'))));
				if (currentIndex == line.length()) {
					ret.append("</span>");
				} else {
					ret.append("\'</span>");
					state = State.DEFAULT;
					currentIndex++;
				}
			} // End of State.QUOTE_SINGLE

			else {
				// Default: No highlighting.
				ret.append(escapeEntity(line.charAt(currentIndex++)));
			} // End of unknown state
		}

		return ret.toString();
	}

	/**
	 * Reset the state of this Java parser. Call this if you have been parsing
	 * one Java file and you want to begin parsing another Java file.
	 * 
	 */
	public void reset() {
		state = State.DEFAULT;
	}

}