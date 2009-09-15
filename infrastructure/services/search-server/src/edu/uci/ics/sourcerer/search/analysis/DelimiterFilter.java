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

import java.io.IOException;
import java.util.LinkedList;

import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;

/**
 * @author <a href="bajracharya@gmail.com">Sushil Bajracharya</a>
 * @created Jul 2, 2009
 *
 */
public class DelimiterFilter extends TokenFilter {

	protected LinkedList<Token> tokens;

	protected DelimiterFilter(TokenStream input) {
		super(input);
		this.tokens = new LinkedList<Token>();
	}

	public final Token next(final Token reusableToken) throws IOException {
		assert reusableToken != null;

		if (tokens.size() > 0) {
			return (Token) tokens.removeFirst();
		}

		Token nextToken = input.next(reusableToken);
		if (nextToken == null) {
			return null;
		}

		decompose(nextToken);

		if (tokens.size() > 0) {
			return (Token) tokens.removeFirst();
		} else {
			// if this filter did not produced any new tokens from the 
			// current token, return the original token
			return nextToken;
		}

	}

	protected boolean isDelimiter(char c){
		return Character.isWhitespace(c);
	}
	
	protected void decompose(Token token) {
		int origStart = token.startOffset();
		
		int start = 0, end = 0;
		
		char[] buffer = token.termBuffer();

		int i = 0;
		for (; i < token.termLength(); i++) {

			// whitespace
			if (isDelimiter(buffer[i])) {
				// last character was not a whitespace too
				if (i > 0 && !isDelimiter(buffer[i-1])) {
					end = i;
					tokens.add(newTok(token, start, end));
				}
			}
			// non-white space
			else {
				if (i > 0 && isDelimiter(buffer[i-1])) {
					start = i;

				}

			}
		}

		// after last space
		if (start > end) {
			tokens.add(newTok(token, start, i));
		}
		
		
		if (start == origStart && end == origStart && start == token.startOffset()){
			// add the original in the list if no whitespace found
			// tokens.add(token);
			
		}

	}

	protected Token newTok(Token orig, int start, int end) {
		int startOff = orig.startOffset();
		int endOff = orig.endOffset();
		// if length by start + end offsets doesn't match the term text then
		// assume
		// this is a synonym and don't adjust the offsets.
		if (orig.termLength() == endOff - startOff) {
			endOff = startOff + end;
			startOff += start;
		}

		Token newTok = new Token(startOff, endOff, orig.type());
		newTok.setTermBuffer(orig.termBuffer(), start, (end - start));
		return newTok;
	}

}
