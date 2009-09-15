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
import java.util.LinkedList;

import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.Tokenizer;

/**
 * @author <a href="bajracharya@gmail.com">Sushil Bajracharya</a>
 * @created Jul 7, 2009
 *
 */
public class NoTokenizer extends Tokenizer {

	LinkedList<Character> charArr = new LinkedList<Character>();
	
	int charSize = 0;
	int iChar;
	
	public NoTokenizer(Reader in) {
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
    	for(int i=0;i<charSize;i++){
    		_char[i] = charArr.get(i);
    	}
    	
    	Token _tok = new Token(_char,0,charSize,0,charSize);
    	charArr.clear();
    	return _tok;
    }

	

}
