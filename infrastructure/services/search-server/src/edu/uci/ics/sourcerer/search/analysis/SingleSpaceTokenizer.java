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
import java.io.Reader;

import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.Tokenizer;

import java.util.LinkedList;

/**
 * @author <a href="bajracharya@gmail.com">Sushil Bajracharya</a>
 * @created Aug 20, 2009
 * 
 * Limitation: needs to have exactly one space as delimiter
 * no preceding or trailing spaces either, otherwise returns empty characters
 * and tokens in presence of multiple consecutive white spaces
 *
 */
public class SingleSpaceTokenizer extends Tokenizer {
	
	int start = 0;

  /** Collects only characters which do not satisfy
   * {@link Character#isWhitespace(char)}.*/
  protected boolean isTokenChar(char c) {
    return !(Character.isWhitespace(c));
  }
  
  LinkedList<Character> charArr = new LinkedList<Character>();
	
	int charSize = 0;
	int iChar;
	
	public SingleSpaceTokenizer(Reader in) {
	    super(in);
	    
	    try {
			readAll();
		} catch (IOException e) {
			charArr.clear();
		}
	    
	  }
	
	private void readAll() throws IOException{
		charSize = 0;
		iChar = input.read();
		if(iChar==-1) return;
		
		charArr.add(charSize++, new Character((char) iChar));
	    
		while(iChar > -1){
	    	iChar = input.read();
	    	if(iChar > -1){
	    		charArr.add(charSize++, new Character((char) iChar));
	    	}
		}
		
	}
	
	
  public Token next() throws IOException {
   
  	if (charArr.size()==0) 
  		return null;
  	
  	char[] _char = new char[charSize];
  	
  	int tokenSize = 0;
  	for(int i=0;i+start<charSize;i++){
  		
  		char _c = charArr.get(i+start);
  		
  		
  		if(isTokenChar(_c)){
  			tokenSize++;
  			_char[i] = _c;
  		}
  		else {
  			break;
  		}
  	}
  	
  	Token _tok = new Token(_char,0,tokenSize,start,start+tokenSize); //.., offset, length, start, end
  	if(start+tokenSize == charSize) charArr.clear();
  	start = start + tokenSize + 1;
  	return _tok;
  }

  
  
}

