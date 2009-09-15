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
package edu.uci.ics.sourcerer.search.analysis;

import java.util.LinkedList;
import java.util.List;

import org.apache.lucene.analysis.Token;

/**
 * @author <a href="bajracharya@gmail.com">Sushil Bajracharya</a>
 * @created Jun 30, 2009
 * 
 */
public class JavaFqnTokenizer {

	public static Token extractMethodSigArgsInShortName(Token token) {

		StringBuffer shortNameSig = new StringBuffer();
		List<Character> sname = new LinkedList<Character>();

		int startOffset = -2;

		char[] termBuffer = token.termBuffer();
		for (int i = 0; i < token.termLength(); i++) {

			if (termBuffer[i] == '(') {
				sname.clear();
				if (startOffset == -2)
					startOffset++;
				continue;
			} else if (termBuffer[i] == '.') {
				sname.clear();
				if (startOffset == -1)
					startOffset++;
				continue;
			} else if (termBuffer[i] == ',') {
				// add sname to shortNameSig
				for (Character c : sname) {
					shortNameSig.append(c);
				}
				if (startOffset <= 0) {
					startOffset = i - sname.size();
				}
				shortNameSig.append(",");
				sname.clear();
				continue;
			} else if (termBuffer[i] == ')') {
				// add sname to shortNameSig
				for (Character c : sname) {
					shortNameSig.append(c);
				}
				token.setEndOffset(i);
				token.setStartOffset(startOffset);
				token.setTermBuffer(shortNameSig.toString());
				token.setTermLength(shortNameSig.length());
				return token;
			}

			// add char to sname
			sname.add(termBuffer[i]);
			// i = char position
		}

		return null;
	}

	/***
	 * make sure token has a termbuffer with a raw fqn
	 * 
	 * @param token
	 * @return
	 */
	public static Token extractMethodSigArgsInFQN(Token token) {

		StringBuffer sigFqn = new StringBuffer();

		char[] termBuffer = token.termBuffer();
		for (int i = 0; i < token.termLength(); i++) {

			if (termBuffer[i] == '(') {
				sigFqn = new StringBuffer();
				token.setStartOffset(i + 1);
			} else if (termBuffer[i] == ')') {
				token.setEndOffset(i);
				token.setTermBuffer(sigFqn.toString());
				token.setTermLength(sigFqn.length());
				return token;
			} else {
				sigFqn.append(termBuffer[i]);
			}

		}

		return null;
	}

	public static Token extractFQN(Token token) {

		char[] termBuffer = token.termBuffer();

		for (int i = 0; i < token.termLength(); i++) {
			if (termBuffer[i] == '(') {

				token.setEndOffset(token.endOffset() - (token.termLength() - i));
				token.setTermBuffer(termBuffer, 0, i);
				token.setTermLength(i);

				return token;
			}
		}

		return token;
	}

	public static Token extractShortName(Token token) {

		char[] termBuffer = token.termBuffer();
		int start = 0;
		int i = 0;
		for (;i < token.termLength(); i++) {
			if (termBuffer[i] == '(') {

				token.setStartOffset(token.startOffset() + start);
				token.setEndOffset(token.endOffset() - (token.termLength() - i));
				token.setTermBuffer(termBuffer, start, i - start);
				token.setTermLength(i - start);

				return token;
			} else if (termBuffer[i] == '.'){
				
				if (i+1 <= token.termLength())
					start = i + 1;
				else
					// assert can never happen
					// this means fqn ended with '.'
					return token;
			}
		}

		token.setStartOffset(token.startOffset() + start);
		token.setEndOffset(token.endOffset() - (token.termLength() - i));
		token.setTermBuffer(termBuffer, start, i-start);
		token.setTermLength(i - start);
		return token;
	}

}
