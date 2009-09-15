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
 * works on the output of alphanumfilter
 * 
 * @author <a href="bajracharya@gmail.com">Sushil Bajracharya</a>
 * @created Jul 1, 2009
 * 
 */
public class CamelCaseSplitFilter extends DelimiterFilter {

	public CamelCaseSplitFilter(TokenStream input) {
		super(input);
	}

	protected void decompose(Token token) {

		int start = 0, end = 0;

		char[] buffer = token.termBuffer();

		int i = 0;
		for (; i < token.termLength(); i++) {
			
			// only compare two consecutive letters
			if (i > 0 && Character.isLetter(buffer[i]) && Character.isLetter(buffer[i - 1])) {
				
				// lower -> upper
				if (Character.isLowerCase(buffer[i - 1])
						&& Character.isUpperCase(buffer[i])) {

					// create a new token upto buffer[i-1]
					tokens.add(newTok(token, start, end+1));
					start = i;
					// end = i;

				} 
				// upper -> lower
				else if (Character.isUpperCase(buffer[i - 1]) && Character.isLowerCase(buffer[i])) {

					if (start < i - 1) {
						// create a new token upto buffer[i-2]
						tokens.add(newTok(token, start, i - 1));
						
						// also go back and check for consecutive Upper letters
						// and create a token if more than one found
						// URIs will produce UR, URI, Is
						// DBPool will produce DB, DBP, Pool 
						if (Character.isLetter(buffer[i-2]) && Character.isUpperCase(buffer[i-2])){
							Token _tok = newTok(token, start, i);
							_tok.setPositionIncrement(0);
							tokens.add(_tok);
						}
						
						start = i-1;
						
					}
				} // end upper -< lower
			} // end compare consecutive letters

			end = i;
		} // end for
		
		if(start == 0 && end == i -1){
			// no camel case found 
		} else {
			// add the last token
			tokens.add(newTok(token, start, end+1));
		}
		
	}
}
