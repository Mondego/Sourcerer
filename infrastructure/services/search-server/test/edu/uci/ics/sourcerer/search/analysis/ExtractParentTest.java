package edu.uci.ics.sourcerer.search.analysis;


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
import org.apache.solr.analysis.BaseTokenTestCase.IterTokenStream;

import edu.uci.ics.sourcerer.search.analysis.DelimiterFilterFactory;
import edu.uci.ics.sourcerer.search.analysis.FqnFilter;
import edu.uci.ics.sourcerer.search.analysis.FqnFilterFactory;

/**
 * 
 * @author <a href="bajracharya@gmail.com">Sushil Bajracharya</a>
 * @created Sep 14, 2009
 *
 */
public class ExtractParentTest extends BaseTokenTestCaseSourcerer {

	List<Token> input;
	TokenStream is;
	FqnFilterFactory fqnFactory = new FqnFilterFactory();
	WordDelimiterFilterFactory wdFactory = new WordDelimiterFilterFactory();
	DelimiterFilterFactory dFactory = new DelimiterFilterFactory();

//	public void setUp() throws Exception {
//		super.setUp();
//
//		Token tok = new Token();
//		tok.setTermBuffer("org.wonderly.ham.speech.DB23Announcements.connected(_UNRESOLVED_.Parameters<?java.util.List<java.lang.String>>,java.lang.String)");
//		input = new ArrayList<Token>();
//		input.clear();
//		input.add(tok);
//		
//		is = new WhitespaceTokenizer(new StringReader("org.wonderly.ham.speech.DB23Announcements.connected(_UNRESOLVED_.Parameters<?java.util.List<java.lang.String>>,java.lang.String)"));
//
//		Map<String, String> map = new HashMap<String, String>();
//		map.put("generateWordParts", "1");
//		map.put("generateNumberParts", "1");
//		map.put("catenateWords", "0");
//		map.put("catenateNumbers", "0");
//		map.put("catenateAll", "0");
//		map.put("splitOnCaseChange", "1");
//		map.put("preserveOriginal", "0");
//		wdFactory.init(map);
//
//	}

	public void testFqnSname2() throws Exception {  
		String fqn = "org.wonderly.ham";
		String parent = "wonderly";
		check(fqn, parent);
	}
	
	public void testFqnSname10() throws Exception {  
		String fqn = "org.wonderly.ham<T>.dd$ff(ff.dd)";
		String parent = "ham";
		check(fqn, parent);
	}
	
	public void testFqnSname3() throws Exception {  
		String fqn = "xx";
		checkEmpty(fqn);
	}
	
	public void testFqnSname4() throws Exception {  
		String fqn = "org.wonderly";
		String parent = "org";
		check(fqn, parent);
	}
	
	public void testFqnSname5() throws Exception {  
		String fqn = "org(_UNRESOLVED_.Parameters<?java.util.List<java.lang.String>>,java.lang.String)";
		
		checkEmpty(fqn);
		
		
	}

	/**
	 * @param fqn
	 * @throws IOException
	 */
	private void checkEmpty(String fqn) throws IOException {
		Token tok = new Token();
		tok.setTermBuffer(fqn);
		List<Token >_input = new ArrayList<Token>();
		_input.add(tok);
		
		 Map<String,String> mapFqn = new HashMap<String,String>();
		    mapFqn.put("extractSig", "0");
		    mapFqn.put("shortNamesOnly", "2");
		    fqnFactory.init(mapFqn);
		
		List<Token> justSigs = FqnFilterFactoryTest.getTokens(fqnFactory.create(new IterTokenStream(_input)));
		assertEquals(0, justSigs.size());
	}
	
	public void testFqnSname6() throws Exception {  
		String fqn = "org.wonderly(_UNRESOLVED_.Parameters<?java.util.List<java.lang.String>>,java.lang.String)";
		String parent = "org";
		check(fqn, parent);
	}
	
	public void testFqnSname7() throws Exception {  
		String fqn = "org.wonderly.ham.speech.DB23Announcements.Parameters<?java.util.List<java.lang.String>>";
		String parent = "DB23Announcements";
		check(fqn, parent);
	}
	
	public void testFqnSname8() throws Exception {  
		String fqn = "org.Parameters<?java.util.List<java.lang.String>>";
		String parent = "org";
		check(fqn, parent);
	}
	
	public void testFqnSname9() throws Exception {  
		String fqn = "Parameters<?java.util.List<java.lang.String>>";
		checkEmpty(fqn);
	}
	


	public void testFqnSname1() throws Exception {  
	    
		String fqn = "org.wonderly.ham.speech.DB23Announcements.connected(_UNRESOLVED_.Parameters<?java.util.List<java.lang.String>>,java.lang.String)";
		String parent = "DB23Announcements";
		
		check(fqn, parent);
	    
	}
	
	
	public void testFqnSname11() throws Exception {  
	    
		String fqn = "<T+java.lang.Object>";
		checkEmpty(fqn);
	    
	}

	/**
	 * @param fqn
	 * @param parent
	 * @throws IOException
	 */
	private void check(String fqn, String parent) throws IOException {
		Token tok = new Token();
		tok.setTermBuffer(fqn);
		input = new ArrayList<Token>();
		input.clear();
		input.add(tok);
		
	    Token tok2 = new Token();
	    tok2.setTermBuffer(parent);
	    List<Token> expect = new ArrayList<Token>();
	    expect.add(tok2);

	    Map<String,String> mapFqn = new HashMap<String,String>();
	    mapFqn.put("extractSig", "0");
	    mapFqn.put("shortNamesOnly", "2");
	    fqnFactory.init(mapFqn);
	    
	    List<Token> real = FqnFilterFactoryTest.getTokens(fqnFactory.create( new IterTokenStream(input) ));
	    assertTokEqual( expect, real );
	}
	
	
}


