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

import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;

/**
 * 
 * @author <a href="bajracharya@gmail.com">Sushil Bajracharya</a>
 * @created Sep 14, 2009
 *
 */
public class LetterDigitSplitFilter extends DelimiterFilter {

	final int preserveOriginal;
	
	public LetterDigitSplitFilter(TokenStream input, int preserveOriginal) {
		super(input);
		this.preserveOriginal = preserveOriginal;
	}

	protected void decompose(Token token) {

		int start = 0, end = 0;

		char[] buffer = token.termBuffer();

		int i = 0;
		for (; i < token.termLength(); i++) {

			if (i > 0) {

				if (// letter -> digit
				(Character.isLetter(buffer[i - 1]) && Character
						.isDigit(buffer[i]))
						||
						// digit -> letter
						(Character.isDigit(buffer[i - 1]) && Character
								.isLetter(buffer[i]))) {

					// create a new token upto buffer[i-1]
					tokens.add(newTok(token, start, end + 1));
					start = i;
					end = i;

				}
			} 

			end = i;
		} // end for

		if (start == 0 && end == i - 1) {
		// either all numbers or letters
		} else {
			// add the last token
			tokens.add(newTok(token, start, end + 1));
			
			if(preserveOriginal>0){
				Token _tok = newTok(token, 0, token.termLength());
				_tok.setPositionIncrement(0);
				tokens.add(_tok);
			}
		}

	}

}
