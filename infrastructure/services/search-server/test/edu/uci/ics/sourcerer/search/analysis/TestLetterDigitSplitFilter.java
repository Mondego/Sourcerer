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

import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.WhitespaceTokenizer;
import org.apache.solr.analysis.BaseTokenTestCase;

import edu.uci.ics.sourcerer.search.analysis.LetterDigitSplitFilter;
import edu.uci.ics.sourcerer.search.analysis.LetterDigitSplitFilterFactory;

/**
 * 
 * @author <a href="bajracharya@gmail.com">Sushil Bajracharya</a>
 * @created Sep 14, 2009
 *
 */
public class TestLetterDigitSplitFilter extends BaseTokenTestCaseSourcerer {

	public void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	
	
	public void test2() throws Exception {
		
		TokenStream is = new WhitespaceTokenizer(new StringReader("aaa111bbb2ddd33"));
		
		Map<String, String> mapFqn = new HashMap<String, String>();
		mapFqn.put("preserveOriginal", "1");
		LetterDigitSplitFilterFactory ldsff = new LetterDigitSplitFilterFactory();
		ldsff.init(mapFqn);
		
		LetterDigitSplitFilter ldsf = ldsff.create(is);
		
		
		final Token reusableToken = new Token();
		
		Token nextToken = ldsf.next(reusableToken);
		assertEquals("aaa",nextToken.term());
		
		nextToken = ldsf.next(reusableToken);
		assertEquals("111",nextToken.term());
		
		nextToken = ldsf.next(reusableToken);
		assertEquals("bbb",nextToken.term());
		
		nextToken = ldsf.next(reusableToken);
		assertEquals("2",nextToken.term());
		
		nextToken = ldsf.next(reusableToken);
		assertEquals("ddd",nextToken.term());
		
		nextToken = ldsf.next(reusableToken);
		assertEquals("33",nextToken.term());
		
		nextToken = ldsf.next(reusableToken);
		assertEquals("aaa111bbb2ddd33",nextToken.term());
		
		assertNull(ldsf.next(reusableToken));
		
		
		
	}
	
	public void test1() throws Exception {
		TokenStream is = new WhitespaceTokenizer(new StringReader("aaa111bbb2ddd33"));
		LetterDigitSplitFilterFactory ldsff = new LetterDigitSplitFilterFactory();
		LetterDigitSplitFilter ldsf = ldsff.create(is);
		
		final Token reusableToken = new Token();
		
		Token nextToken = ldsf.next(reusableToken);
		assertEquals("aaa",nextToken.term());
		
		nextToken = ldsf.next(reusableToken);
		assertEquals("111",nextToken.term());
		
		nextToken = ldsf.next(reusableToken);
		assertEquals("bbb",nextToken.term());
		
		nextToken = ldsf.next(reusableToken);
		assertEquals("2",nextToken.term());
		
		nextToken = ldsf.next(reusableToken);
		assertEquals("ddd",nextToken.term());
		
		nextToken = ldsf.next(reusableToken);
		assertEquals("33",nextToken.term());
		
		
		
		assertNull(ldsf.next(reusableToken));
		
		
		
	}

}
