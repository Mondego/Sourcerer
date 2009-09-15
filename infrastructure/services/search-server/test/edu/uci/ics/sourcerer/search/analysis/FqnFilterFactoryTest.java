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
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.WhitespaceTokenizer;
import org.apache.solr.analysis.BaseTokenTestCase;
import org.apache.solr.analysis.WordDelimiterFilterFactory;

import edu.uci.ics.sourcerer.search.analysis.DelimiterFilterFactory;
import edu.uci.ics.sourcerer.search.analysis.FqnFilter;
import edu.uci.ics.sourcerer.search.analysis.FqnFilterFactory;

/**
 * 
 * @author <a href="bajracharya@gmail.com">Sushil Bajracharya</a>
 * @created Sep 14, 2009
 *
 */
public class FqnFilterFactoryTest extends BaseTokenTestCaseSourcerer {

	List<Token> input;
	TokenStream is;
	FqnFilterFactory fqnFactory = new FqnFilterFactory();
	WordDelimiterFilterFactory wdFactory = new WordDelimiterFilterFactory();
	DelimiterFilterFactory dFactory = new DelimiterFilterFactory();

	public void setUp() throws Exception {
		super.setUp();

		Token tok = new Token();
		tok.setTermBuffer("org.wonderly.ham.speech.DB23Announcements.connected(_UNRESOLVED_.Parameters<?java.util.List<java.lang.String>>,java.lang.String)");
		input = new ArrayList<Token>();
		input.clear();
		input.add(tok);
		
		is = new WhitespaceTokenizer(new StringReader("org.wonderly.ham.speech.DB23Announcements.connected(_UNRESOLVED_.Parameters<?java.util.List<java.lang.String>>,java.lang.String)"));

		Map<String, String> map = new HashMap<String, String>();
		map.put("generateWordParts", "1");
		map.put("generateNumberParts", "1");
		map.put("catenateWords", "0");
		map.put("catenateNumbers", "0");
		map.put("catenateAll", "0");
		map.put("splitOnCaseChange", "1");
		map.put("preserveOriginal", "0");
		wdFactory.init(map);

	}


	
	
	public void testSig2() throws Exception {
		// test extract signature
		Token tok3 = new Token();
		tok3.setTermBuffer("Parameters,java.lang.String");
		List<Token> expectJustSigs = new ArrayList<Token>();
		expectJustSigs.add(tok3);

		Map<String, String> mapFqn2 = new HashMap<String, String>();
		mapFqn2.put("extractSig", "1");
		fqnFactory.init(mapFqn2);

		List<Token> justSigs = getTokens(fqnFactory.create(new IterTokenStream(
				input)));
		assertTokEqual(expectJustSigs, justSigs);
	}

	public void testNoSig2() throws Exception {
		Map<String, String> mapFqn = new HashMap<String, String>();
		mapFqn.put("extractSig", "1");
		mapFqn.put("shortNamesOnly","0");
		
		fqnFactory.init(mapFqn);

		// note .. there is no whitespace here 
		is = new WhitespaceTokenizer(new StringReader("java.util.List<<?+dalma.Condition<<?+<T>>>>>"));

		FqnFilter ff = fqnFactory.create(is);
		
		final Token reusableToken = new Token();
		assertNull(ff.next(reusableToken));
	}
	
	public void testNoSig() throws Exception {
		
		Token tok = new Token();
		tok.setTermBuffer("org.wonderly.ham.speech.DB23Announcements<java.util.List<_UNRESOLVED_.Object>>");
		List<Token >_input = new ArrayList<Token>();
		_input.add(tok);


		Map<String, String> mapFqn2 = new HashMap<String, String>();
		mapFqn2.put("extractSig", "1");
		fqnFactory.init(mapFqn2);

		List<Token> justSigs = getTokens(fqnFactory.create(new IterTokenStream(_input)));
		assertEquals(0, justSigs.size());
		
	}
	


	public void testFqnSname() throws Exception {  
	    
	    Token tok2 = new Token();
	    tok2.setTermBuffer("connected");
	    List<Token> expect = new ArrayList<Token>();
	    expect.add(tok2);

	    Map<String,String> mapFqn = new HashMap<String,String>();
	    mapFqn.put("extractSig", "0");
	    mapFqn.put("shortNamesOnly", "1");
	    fqnFactory.init(mapFqn);
	    
	    List<Token> real = getTokens(fqnFactory.create( new IterTokenStream(input) ));
	    assertTokEqual( expect, real );
	    
	}
	
	
	public void testFqn() throws Exception {

		Map<String, String> mapFqn = new HashMap<String, String>();
		mapFqn.put("extractSig", "0");
		fqnFactory.init(mapFqn);
		
		FqnFilter tokenizer = fqnFactory.create(is);
	    final Token reusableToken = new Token();
	    Token nextToken = tokenizer.next(reusableToken);
	    assertEquals("org.wonderly.ham.speech.DB23Announcements.connected", nextToken.term());
	    assertNull(tokenizer.next(reusableToken));

	}
	
	public void testSig() throws Exception {
		// test extract signature
		Token tok3 = new Token();
		tok3.setTermBuffer("Parameters,java.lang.String");
		List<Token> expectJustSigs = new ArrayList<Token>();
		expectJustSigs.add(tok3);

		Map<String, String> mapFqn2 = new HashMap<String, String>();
		mapFqn2.put("extractSig", "1");
		fqnFactory.init(mapFqn2);

		List<Token> justSigs = getTokens(fqnFactory.create(new IterTokenStream(
				input)));
		assertTokEqual(expectJustSigs, justSigs);
	}
	

	public void testSigSname() throws Exception {
		// test extract signature
		Token tok3 = new Token();
		tok3.setTermBuffer("Parameters,String");
		List<Token> expectJustSigs = new ArrayList<Token>();
		expectJustSigs.add(tok3);

		Map<String, String> mapFqn2 = new HashMap<String, String>();
		mapFqn2.put("extractSig", "1");
		mapFqn2.put("shortNamesOnly", "1");
		fqnFactory.init(mapFqn2);

		List<Token> justSigs = getTokens(fqnFactory.create(new IterTokenStream(input)));
		assertTokEqual(expectJustSigs, justSigs);
	}

	public void testFqnVisual() throws Exception {
		List<Token> fqnTerms = getTokens(wdFactory.create(fqnFactory
				.create(new IterTokenStream(input))));

		for (Token t : fqnTerms) {
			System.out.println(t.term());
		}
	}



	static List<Token> getTokens(TokenStream tstream) throws IOException {
		List<Token> tokens = new ArrayList<Token>();
		while (true) {
			Token t = tstream.next();
			if (t == null)
				break;
			tokens.add(t);
		}
		return tokens;
	}

	
}
