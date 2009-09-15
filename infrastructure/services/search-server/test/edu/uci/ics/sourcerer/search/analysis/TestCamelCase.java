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

import edu.uci.ics.sourcerer.search.analysis.CamelCaseSplitFilter;
import edu.uci.ics.sourcerer.search.analysis.CamelCaseSplitFilterFactory;
import edu.uci.ics.sourcerer.search.analysis.FqnFilter;
import edu.uci.ics.sourcerer.search.analysis.FqnFilterFactory;
import edu.uci.ics.sourcerer.search.analysis.LetterDigitSplitFilter;
import edu.uci.ics.sourcerer.search.analysis.LetterDigitSplitFilterFactory;
import edu.uci.ics.sourcerer.search.analysis.NoTokenizerFactory;
import edu.uci.ics.sourcerer.search.analysis.NonAlphaNumTokenizerFilter;
import edu.uci.ics.sourcerer.search.analysis.NonAlphaNumTokenizerFilterFactory;

/**
 * 
 * @author <a href="bajracharya@gmail.com">Sushil Bajracharya</a>
 * @created Sep 14, 2009
 *
 */
public class TestCamelCase extends BaseTokenTestCaseSourcerer {

	TokenStream is;
	FqnFilterFactory fqnFactory = new FqnFilterFactory();
	CamelCaseSplitFilter ccf = null;
	
	public void setUp() throws Exception {
		
		Map<String, String> mapFqn = new HashMap<String, String>();
		mapFqn.put("extractSig", "0");
		mapFqn.put("shortNamesOnly","0");
		
		fqnFactory.init(mapFqn);
		
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	
	public void testCCFromJdkNames() throws Exception {
		// 
		ccf = getCCF("attWildcardAsURIs");
		
		final Token reusableToken = new Token();
		
		Token nextToken = ccf.next(reusableToken);
		assertEquals("att",nextToken.term());
		nextToken = ccf.next(reusableToken);
		assertEquals("Wildcard",nextToken.term());
		nextToken = ccf.next(reusableToken);
		assertEquals("As",nextToken.term());
		
		nextToken = ccf.next(reusableToken);
		assertEquals("UR",nextToken.term());
		
		nextToken = ccf.next(reusableToken);
		assertEquals("URI",nextToken.term());
		assertEquals(0, nextToken.getPositionIncrement());
		
		nextToken = ccf.next(reusableToken);
		assertEquals("Is",nextToken.term());
		assertEquals(1, nextToken.getPositionIncrement());
		
		assertNull(ccf.next(reusableToken));
		
		
	}
	
	public void test3() throws Exception {
		ccf = getCCF("c14nSupport");
		
		final Token reusableToken = new Token();
		
		Token nextToken = ccf.next(reusableToken);
		assertEquals("c14n",nextToken.term());
		nextToken = ccf.next(reusableToken);
		assertEquals("Support",nextToken.term());
		
		assertNull(ccf.next(reusableToken));
	}
	
	public void test4() throws Exception {
		ccf = getCCF("readUtf8Bands");
		
		final Token reusableToken = new Token();
		
		Token nextToken = ccf.next(reusableToken);
		assertEquals("read",nextToken.term());
		nextToken = ccf.next(reusableToken);
		assertEquals("Utf8",nextToken.term());
		nextToken = ccf.next(reusableToken);
		assertEquals("Bands",nextToken.term());
		
		assertNull(ccf.next(reusableToken));
	}
	
	public void test5() throws Exception {
		ccf = getCCF("knownUri2prefixIndexMap");
		
		final Token reusableToken = new Token();
		
		Token nextToken = ccf.next(reusableToken);
		assertEquals("known",nextToken.term());
		nextToken = ccf.next(reusableToken);
		assertEquals("Uri2prefix",nextToken.term());
		nextToken = ccf.next(reusableToken);
		assertEquals("Index",nextToken.term());
		nextToken = ccf.next(reusableToken);
		assertEquals("Map",nextToken.term());
		
		assertNull(ccf.next(reusableToken));
	}
	
	private CamelCaseSplitFilter getCCF(String name){
		
		NoTokenizerFactory ntf = new NoTokenizerFactory();
		is = ntf.create(new StringReader(name));
		
		//is = new WhitespaceTokenizer(new StringReader(name));

		FqnFilter fqnf = fqnFactory.create(is);
		
		NonAlphaNumTokenizerFilterFactory nff = new NonAlphaNumTokenizerFilterFactory();
		NonAlphaNumTokenizerFilter nf = nff.create(fqnf);
		CamelCaseSplitFilterFactory ccff = new CamelCaseSplitFilterFactory();
		CamelCaseSplitFilter ccf = ccff.create(nf);
		
		return ccf;

	}
	
	public void testCamelCase() throws Exception {
		
		ccf = getCCF("_Gdima.db_ConnMgr.DBConn.DBconn.DbConnMGRPool$1.fPool2Db<T>[]");
		
		final Token reusableToken = new Token();
		
		Token nextToken = ccf.next(reusableToken);
		assertEquals("Gdima",nextToken.term());
		
		nextToken = ccf.next(reusableToken);
		assertEquals("db",nextToken.term());
		
		nextToken = ccf.next(reusableToken);
		assertEquals("Conn",nextToken.term());
		
		nextToken = ccf.next(reusableToken);
		assertEquals("Mgr",nextToken.term());
		
		nextToken = ccf.next(reusableToken);
		assertEquals("DB",nextToken.term());
		
		nextToken = ccf.next(reusableToken);
		assertEquals("DBC",nextToken.term());
		
		nextToken = ccf.next(reusableToken);
		assertEquals("Conn",nextToken.term());
		
		nextToken = ccf.next(reusableToken);
		assertEquals("D",nextToken.term());
		
		nextToken = ccf.next(reusableToken);
		assertEquals("DB",nextToken.term());
		
		nextToken = ccf.next(reusableToken);
		assertEquals("Bconn",nextToken.term());
		
		nextToken = ccf.next(reusableToken);
		assertEquals("Db",nextToken.term());
		
		nextToken = ccf.next(reusableToken);
		assertEquals("Conn",nextToken.term());
		
		nextToken = ccf.next(reusableToken);
		assertEquals("MGR",nextToken.term());
		
		nextToken = ccf.next(reusableToken);
		assertEquals("MGRP",nextToken.term());
		
		nextToken = ccf.next(reusableToken);
		assertEquals("Pool",nextToken.term());
		
		nextToken = ccf.next(reusableToken);
		assertEquals("1",nextToken.term());
		
		nextToken = ccf.next(reusableToken);
		assertEquals("f",nextToken.term());
		
		nextToken = ccf.next(reusableToken);
		assertEquals("Pool2",nextToken.term());
		
		nextToken = ccf.next(reusableToken);
		assertEquals("Db",nextToken.term());
		
		assertNull(ccf.next(reusableToken));
	}
	
	public void testCamelCaseWithLetterDigitSplit() throws Exception {
		ccf = getCCF("MP3Player.wav2mp3");//.DbConnMGRPool$1.fPool2Db<T>[]");
		
//		LetterDigitSplitFactory ldsff = new LetterDigitSplitFactory();
//		LetterDigitSplitFilter ldsf = ldsff.create(ccf);
		
		final Token reusableToken = new Token();
		
		Token nextToken = ccf.next(reusableToken);
		assertEquals("MP3",nextToken.term());
		
		
		nextToken = ccf.next(reusableToken);
		assertEquals("Player",nextToken.term());
		
		nextToken = ccf.next(reusableToken);
		assertEquals("wav2mp3",nextToken.term());
		
//		nextToken = ccf.next(reusableToken);
//		assertEquals("Player",nextToken.term());
		
		assertNull(ccf.next(reusableToken));
		
	}
	
	public void testCamelCaseWithLetterDigitSplitPreserveOriginal() throws Exception {
		ccf = getCCF("MP3Player.wav2Mp3");//.DbConnMGRPool$1.fPool2Db<T>[]");
		
		LetterDigitSplitFilterFactory ldsff = new LetterDigitSplitFilterFactory();
		Map<String, String> map = new HashMap<String, String>();
		map.put("preserveOriginal", "1");
		ldsff.init(map);
		
		LetterDigitSplitFilter ldsf = ldsff.create(ccf);
		
		final Token reusableToken = new Token();
		
		Token nextToken = ldsf.next(reusableToken);
		assertEquals("MP",nextToken.term());
		
		nextToken = ldsf.next(reusableToken);
		assertEquals("3",nextToken.term());
		
		nextToken = ldsf.next(reusableToken);
		assertEquals("MP3",nextToken.term());
		
		nextToken = ldsf.next(reusableToken);
		assertEquals("Player",nextToken.term());
		
		nextToken = ldsf.next(reusableToken);
		assertEquals("wav",nextToken.term());
		
		nextToken = ldsf.next(reusableToken);
		assertEquals("2",nextToken.term());
		
		nextToken = ldsf.next(reusableToken);
		assertEquals("wav2",nextToken.term());
		
		nextToken = ldsf.next(reusableToken);
		assertEquals("Mp",nextToken.term());
		
		nextToken = ldsf.next(reusableToken);
		assertEquals("3",nextToken.term());
		
		nextToken = ldsf.next(reusableToken);
		assertEquals("Mp3",nextToken.term());
		
		assertNull(ldsf.next(reusableToken));
		
	}
	
	public void testCamelCaseWithLetterDigitSplit2() throws Exception {
		ccf = getCCF("MP3Player.wav2mp3");//.DbConnMGRPool$1.fPool2Db<T>[]");
		
		LetterDigitSplitFilterFactory ldsff = new LetterDigitSplitFilterFactory();
		LetterDigitSplitFilter ldsf = ldsff.create(ccf);
		
		final Token reusableToken = new Token();
		
		Token nextToken = ldsf.next(reusableToken);
		assertEquals("MP",nextToken.term());
		
		nextToken = ldsf.next(reusableToken);
		assertEquals("3",nextToken.term());
		
		nextToken = ldsf.next(reusableToken);
		assertEquals("Player",nextToken.term());
		
		nextToken = ldsf.next(reusableToken);
		assertEquals("wav",nextToken.term());
		
		nextToken = ldsf.next(reusableToken);
		assertEquals("2",nextToken.term());
		
		nextToken = ldsf.next(reusableToken);
		assertEquals("mp",nextToken.term());
		
		nextToken = ldsf.next(reusableToken);
		assertEquals("3",nextToken.term());
		
		assertNull(ldsf.next(reusableToken));
		
	}

}
