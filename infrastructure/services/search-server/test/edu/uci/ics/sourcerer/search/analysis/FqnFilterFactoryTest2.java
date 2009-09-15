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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.WhitespaceTokenizer;
import org.apache.solr.analysis.BaseTokenTestCase;
import org.apache.solr.analysis.LowerCaseFilterFactory;

import edu.uci.ics.sourcerer.search.analysis.CamelCaseSplitFilter;
import edu.uci.ics.sourcerer.search.analysis.CamelCaseSplitFilterFactory;
import edu.uci.ics.sourcerer.search.analysis.DelimiterFilter;
import edu.uci.ics.sourcerer.search.analysis.DelimiterFilterFactory;
import edu.uci.ics.sourcerer.search.analysis.FqnFilter;
import edu.uci.ics.sourcerer.search.analysis.FqnFilterFactory;
import edu.uci.ics.sourcerer.search.analysis.FragmentFilter;
import edu.uci.ics.sourcerer.search.analysis.FragmentFilterFactory;
import edu.uci.ics.sourcerer.search.analysis.LetterDigitSplitFilter;
import edu.uci.ics.sourcerer.search.analysis.LetterDigitSplitFilterFactory;
import edu.uci.ics.sourcerer.search.analysis.NonAlphaNumTokenizerFilter;
import edu.uci.ics.sourcerer.search.analysis.NonAlphaNumTokenizerFilterFactory;
import edu.uci.ics.sourcerer.search.analysis.RemoveSigOrderFilterFactory;
import edu.uci.ics.sourcerer.search.analysis.SingleSpaceTokenizer;

/**
 * 
 * @author <a href="bajracharya@gmail.com">Sushil Bajracharya</a>
 * @created Sep 14, 2009
 *
 */
public class FqnFilterFactoryTest2 extends BaseTokenTestCaseSourcerer {
	

	TokenStream is;
	FqnFilterFactory fqnFactory = new FqnFilterFactory();
	DelimiterFilterFactory dFactory = new DelimiterFilterFactory();
	
	public void setUp() throws Exception {
		super.setUp();
	}
	
	public void testFqn() throws Exception {
		Map<String, String> mapFqn = new HashMap<String, String>();
		mapFqn.put("extractSig", "0");
		mapFqn.put("shortNamesOnly","0");
		
		fqnFactory.init(mapFqn);

		// note .. there is no whitespace here 
		is = new WhitespaceTokenizer(new StringReader("org.wonderly.ham.speech.DB23Announcements.connected(_UNRESOLVED_.Parameters<?java.util.List<java.lang.String>>,java.lang.String)"));

		
		
		FqnFilter tokenizer = fqnFactory.create(is);
		final Token reusableToken = new Token();
		Token nextToken = tokenizer.next(reusableToken);
		assertEquals("org.wonderly.ham.speech.DB23Announcements.connected",nextToken.term());
		assertNull(tokenizer.next(reusableToken));
	}
	
	public void testFqnShortName() throws Exception {
		Map<String, String> mapFqn = new HashMap<String, String>();
		mapFqn.put("extractSig", "0");
		mapFqn.put("shortNamesOnly","1");
		
		fqnFactory.init(mapFqn);

		// note .. there is no whitespace here 
		is = new WhitespaceTokenizer(new StringReader("org.wonderly.ham.speech.DB23Announcements.connected(_UNRESOLVED_.Parameters<?java.util.List<java.lang.String>>,java.lang.String)"));

		FqnFilter tokenizer = fqnFactory.create(is);
		final Token reusableToken = new Token();
		Token nextToken = tokenizer.next(reusableToken);
		assertEquals("connected",nextToken.term());
		assertNull(tokenizer.next(reusableToken));
	}
	
	public void testFqnShortName2() throws Exception {
		Map<String, String> mapFqn = new HashMap<String, String>();
		mapFqn.put("extractSig", "0");
		mapFqn.put("shortNamesOnly","1");
		
		fqnFactory.init(mapFqn);

		// note .. there is no whitespace here 
		is = new WhitespaceTokenizer(new StringReader("org.wonderly.ham.speech.DB23Announcements.connected"));

		FqnFilter tokenizer = fqnFactory.create(is);
		final Token reusableToken = new Token();
		Token nextToken = tokenizer.next(reusableToken);
		assertEquals("connected",nextToken.term());
		assertNull(tokenizer.next(reusableToken));
	}
	
	public void testFqnShortName3() throws Exception {
		Map<String, String> mapFqn = new HashMap<String, String>();
		mapFqn.put("extractSig", "0");
		mapFqn.put("shortNamesOnly","1");
		
		fqnFactory.init(mapFqn);

		// note .. there is no whitespace here 
		is = new WhitespaceTokenizer(new StringReader("connected"));

		FqnFilter tokenizer = fqnFactory.create(is);
		final Token reusableToken = new Token();
		Token nextToken = tokenizer.next(reusableToken);
		assertEquals("connected",nextToken.term());
		assertNull(tokenizer.next(reusableToken));
	}
	
	public void testSigFqn() throws Exception {
		Map<String, String> mapFqn = new HashMap<String, String>();
		mapFqn.put("extractSig", "1");
		mapFqn.put("shortNamesOnly","0");
		
		fqnFactory.init(mapFqn);

		// note .. there is no whitespace here 
		is = new WhitespaceTokenizer(new StringReader("org.wonderly.ham.speech.DB23Announcements.connected(_UNRESOLVED_.Parameters<?java.util.List<java.lang.String>>,java.lang.String)"));

		FqnFilter fqnf = fqnFactory.create(is);
		
		final Token reusableToken = new Token();
		Token nextToken = fqnf.next(reusableToken);
		assertEquals("Parameters,java.lang.String",nextToken.term());
		
		assertNull(fqnf.next(reusableToken));
	}
	
	public void testRemoveSigOrder() throws Exception {
		Map<String, String> mapFqn = new HashMap<String, String>();
		mapFqn.put("extractSig", "1");
		mapFqn.put("shortNamesOnly","0");
		
		fqnFactory.init(mapFqn);

		// note .. there is no whitespace here 
		is = new WhitespaceTokenizer(new StringReader("org.wonderly.ham.speech.DB23Announcements.connected(_UNRESOLVED_.Parameters<?java.util.List<java.lang.String>>,java.lang.String)"));

		FqnFilter fqnf = fqnFactory.create(is);
		RemoveSigOrderFilterFactory rsoff = new RemoveSigOrderFilterFactory(); 
		DelimiterFilter rsof = rsoff.create(fqnf);
		
		
		final Token reusableToken = new Token();
		Token nextToken = rsof.next(reusableToken);
		assertEquals("Parameters",nextToken.term());
		nextToken = rsof.next(reusableToken);
		assertEquals("java.lang.String",nextToken.term());
		assertNull(rsof.next(reusableToken));
	}
	
	public void testSigFragment() throws Exception {
		Map<String, String> mapFqn = new HashMap<String, String>();
		mapFqn.put("extractSig", "1");
		mapFqn.put("shortNamesOnly","0");
		
		fqnFactory.init(mapFqn);

		// note .. there is no whitespace here 
		is = new WhitespaceTokenizer(new StringReader("org.wonderly.ham.speech.DB23Announcements.connected(_UNRESOLVED_.Parameters<?java.util.List<java.lang.String>>,java.lang.String)"));

		FqnFilter fqnf = fqnFactory.create(is);
		RemoveSigOrderFilterFactory rsoff = new RemoveSigOrderFilterFactory(); 
		DelimiterFilter rsof = rsoff.create(fqnf);
		FragmentFilterFactory fff = new FragmentFilterFactory();
		FragmentFilter ff = fff.create(rsof);
		
		final Token reusableToken = new Token();
		Token nextToken = ff.next(reusableToken);
		assertEquals("Parameters",nextToken.term());
		nextToken = ff.next(reusableToken);
		assertEquals("java",nextToken.term());
		nextToken = ff.next(reusableToken);
		assertEquals("lang",nextToken.term());
		nextToken = ff.next(reusableToken);
		assertEquals("String",nextToken.term());
		assertNull(ff.next(reusableToken));
	}
	
	public void testFqnFragment() throws Exception {
		Map<String, String> mapFqn = new HashMap<String, String>();
		mapFqn.put("extractSig", "0");
		mapFqn.put("shortNamesOnly","0");
		
		fqnFactory.init(mapFqn);

		// note .. there is no whitespace here 
		is = new WhitespaceTokenizer(new StringReader("org.wonderly.ham.speech.DB23Announcements.connected(_UNRESOLVED_.Parameters<?java.util.List<java.lang.String>>,java.lang.String)"));

		FqnFilter fqnf = fqnFactory.create(is);
		RemoveSigOrderFilterFactory rsoff = new RemoveSigOrderFilterFactory(); 
		DelimiterFilter rsof = rsoff.create(fqnf);
		FragmentFilterFactory fff = new FragmentFilterFactory();
		FragmentFilter ff = fff.create(rsof);
		
		final Token reusableToken = new Token();
		Token nextToken = ff.next(reusableToken);
		assertEquals("org",nextToken.term());
		nextToken = ff.next(reusableToken);
		assertEquals("wonderly",nextToken.term());
		nextToken = ff.next(reusableToken);
		assertEquals("ham",nextToken.term());
		nextToken = ff.next(reusableToken);
		assertEquals("speech",nextToken.term());
		nextToken = ff.next(reusableToken);
		assertEquals("DB23Announcements",nextToken.term());
		nextToken = ff.next(reusableToken);
		assertEquals("connected",nextToken.term());
		assertNull(ff.next(reusableToken));
	}
	
	public void testSigSnameFragment() throws Exception {
		Map<String, String> mapFqn = new HashMap<String, String>();
		mapFqn.put("extractSig", "1");
		mapFqn.put("shortNamesOnly","1");
		
		fqnFactory.init(mapFqn);

		// note .. there is no whitespace here 
		is = new WhitespaceTokenizer(new StringReader("org.wonderly.ham.speech.DB23Announcements.connected(_UNRESOLVED_.Parameters<?java.util.List<java.lang.String>>,java.lang.String)"));

		FqnFilter fqnf = fqnFactory.create(is);
		RemoveSigOrderFilterFactory rsoff = new RemoveSigOrderFilterFactory(); 
		DelimiterFilter rsof = rsoff.create(fqnf);
		FragmentFilterFactory fff = new FragmentFilterFactory();
		FragmentFilter ff = fff.create(rsof);
		
		final Token reusableToken = new Token();
		Token nextToken = ff.next(reusableToken);
		assertEquals("Parameters",nextToken.term());
		nextToken = ff.next(reusableToken);
		assertEquals("String",nextToken.term());
		assertNull(ff.next(reusableToken));
	}
	
	public void testMethodSname() throws Exception {
		Map<String, String> mapFqn = new HashMap<String, String>();
		mapFqn.put("extractSig", "0");
		mapFqn.put("shortNamesOnly","1");
		
		fqnFactory.init(mapFqn);

		// note .. there is no whitespace here 
		is = new WhitespaceTokenizer(new StringReader("org.wonderly.ham.speech.DB23Announcements.connected()"));

		FqnFilter ff = fqnFactory.create(is);
//		RemoveSigOrderFilterFactory rsoff = new RemoveSigOrderFilterFactory(); 
//		DelimiterFilter rsof = rsoff.create(fqnf);
//		FragmentFilterFactory fff = new FragmentFilterFactory();
//		FragmentFilter ff = fff.create(rsof);
		
		final Token reusableToken = new Token();
		Token nextToken = ff.next(reusableToken);
		assertEquals("connected",nextToken.term());
		assertNull(ff.next(reusableToken));
	}
	
	public void testSigSname() throws Exception {
		Map<String, String> mapFqn = new HashMap<String, String>();
		mapFqn.put("extractSig", "1");
		mapFqn.put("shortNamesOnly","1");
		
		fqnFactory.init(mapFqn);

		// note .. there is no whitespace here 
		is = new WhitespaceTokenizer(new StringReader("org.wonderly.ham.speech.DB23Announcements.connected(_UNRESOLVED_.Parameters<?java.util.List<java.lang.String>>,java.lang.String)"));

		FqnFilter fqnf = fqnFactory.create(is);
		RemoveSigOrderFilterFactory rsoff = new RemoveSigOrderFilterFactory(); 
		DelimiterFilter rsof = rsoff.create(fqnf);
		
		
		final Token reusableToken = new Token();
		Token nextToken = rsof.next(reusableToken);
		assertEquals("Parameters",nextToken.term());
		nextToken = rsof.next(reusableToken);
		assertEquals("String",nextToken.term());
		assertNull(rsof.next(reusableToken));
	}
	
	public void testAngBrackets() throws Exception {
		/* 
		<tokenizer
			class="solr.WhitespaceTokenizerFactory"/>
	        <filter class="edu.uci.ics.sourcerer.search.analysis.FqnFilterFactory"
			extractSig="0" shortNamesOnly="1" />
		<filter class="edu.uci.ics.sourcerer.search.analysis.NonAlphaNumTokenizerFilterFactory"/>
		<filter class="edu.uci.ics.sourcerer.search.analysis.CamelCaseSplitFilterFactory"/>
		<filter class="edu.uci.ics.sourcerer.search.analysis.LetterDigitSplitFilterFactory" preserveOriginal="1"/>
		<filter class="solr.LowerCaseFilterFactory"/>
		*/
		String fqns =
			"org.ayutabeans.util.BindingTypes.getBindingTypes()" +
			" com.sun.xml.bind.v2.model.impl.ClassInfoImpl$ConflictException.<init>(java.util.List<java.lang.annotation.Annotation>)" +
			" com.redlenses.net.transport.pickling.reflection.PropertyAccessor.setAnnotations(java.util.List<java.lang.annotation.Annotation>)" +
			" org.cbf.impl.ModuleInfoImpl.getBeanConfigurations(java.lang.String)" +
			" org.cbf.impl.BeanDescriptionImpl.getBeanConfigurations()" +
			" org.cbf.BeanDescription.getBeanConfigurations()" +
			" org.ayutabeans.util.BindingTypes.match(java.util.List<java.lang.annotation.Annotation>)" +
			// this fqn was giving empty stack exception
			" org.ayutabeans.component.impl.JavaBeanComponentImpl.<init>" +
				"(org.ayutabeans.container.AyutaContainer,org.ayutabeans.util.BindingTypes<javax.webbeans.BindingType>," +
				"java.lang.Class<<?+java.lang.annotation.Annotation>>," +
				"java.lang.Class<<?+java.lang.annotation.Annotation>>," +
				"java.lang.String,java.lang.Class<<T>>," +
				"org.ayutabeans.component.ComponentConstructor<<T>>," +
				"java.util.List<org.ayutabeans.component.impl.InjectFieldImpl>," +
				"java.util.List<org.ayutabeans.component.impl.InjectMethodImpl>," +
				"org.ayutabeans.component.impl.LifecycleMethodImpl," +
				"org.ayutabeans.component.impl.LifecycleMethodImpl)" +
			
				" com.sun.jersey.core.spi.factory.InjectableProviderFactory.getInjectable(java.lang.Class<<?+java.lang.annotation.Annotation>>,com.sun.jersey.core.spi.component.ComponentContext,<A>,<C>,java.util.List<com.sun.jersey.core.spi.component.ComponentScope>)" +
			" com.sun.jersey.spi.inject.InjectableProviderContext.getInjectable(java.lang.Class<<?+java.lang.annotation.Annotation>>,com.sun.jersey.core.spi.component.ComponentContext,<A>,<C>,java.util.List<com.sun.jersey.core.spi.component.ComponentScope>) ";
		
		
		Map<String, String> mapFqn = new HashMap<String, String>();
		mapFqn.put("extractSig", "0");
		mapFqn.put("shortNamesOnly","1");
		
		fqnFactory.init(mapFqn);

		//is = new WhitespaceTokenizer(new StringReader(fqns));
		is = new SingleSpaceTokenizer(new StringReader(fqns));

		FqnFilter fqnf = fqnFactory.create(is);
		NonAlphaNumTokenizerFilterFactory ntf = new NonAlphaNumTokenizerFilterFactory();
		NonAlphaNumTokenizerFilter nf = ntf.create(fqnf);
		CamelCaseSplitFilterFactory ccf = new CamelCaseSplitFilterFactory();
		CamelCaseSplitFilter cf = ccf.create(nf);
		LetterDigitSplitFilterFactory lff = new LetterDigitSplitFilterFactory();
		Map<String, String> args = new HashMap<String, String>();
		args.put("preserveOriginal", "1");
		lff.init(args);
		LetterDigitSplitFilter lf = lff.create(cf);
		LowerCaseFilterFactory loff = new LowerCaseFilterFactory();
		LowerCaseFilter lof = loff.create(lf);
		
		final Token reusableToken = new Token();
		Token nextToken = lof.next(reusableToken);
		assertEquals("get",nextToken.term());
		nextToken = lof.next(reusableToken);
		assertEquals("binding",nextToken.term());
		nextToken = lof.next(reusableToken);
		assertEquals("types",nextToken.term());
		nextToken = lof.next(reusableToken);
		assertEquals("init",nextToken.term());
		nextToken = lof.next(reusableToken);
		assertEquals("set",nextToken.term());
		nextToken = lof.next(reusableToken);
		assertEquals("annotations",nextToken.term());
		nextToken = lof.next(reusableToken);
		assertEquals("get",nextToken.term());
		nextToken = lof.next(reusableToken);
		assertEquals("bean",nextToken.term());
		nextToken = lof.next(reusableToken);
		assertEquals("configurations",nextToken.term());
		nextToken = lof.next(reusableToken);
		assertEquals("get",nextToken.term());
		nextToken = lof.next(reusableToken);
		assertEquals("bean",nextToken.term());
		nextToken = lof.next(reusableToken);
		assertEquals("configurations",nextToken.term());
		nextToken = lof.next(reusableToken);
		assertEquals("get",nextToken.term());
		nextToken = lof.next(reusableToken);
		assertEquals("bean",nextToken.term());
		nextToken = lof.next(reusableToken);
		assertEquals("configurations",nextToken.term());
		nextToken = lof.next(reusableToken);
		assertEquals("match",nextToken.term());
		nextToken = lof.next(reusableToken);
		assertEquals("init",nextToken.term());
		nextToken = lof.next(reusableToken);
		assertEquals("get",nextToken.term());
		nextToken = lof.next(reusableToken);
		assertEquals("injectable",nextToken.term());
		nextToken = lof.next(reusableToken);
		assertEquals("get",nextToken.term());
		nextToken = lof.next(reusableToken);
		assertEquals("injectable",nextToken.term());
		nextToken = lof.next(reusableToken);
		
		// note the last space in the input string above 
		assertEquals(1,nextToken.term().length());
		nextToken = lof.next(reusableToken);
		
		assertNull(nextToken);
		
		
		
		
		

	}
	

	
}
