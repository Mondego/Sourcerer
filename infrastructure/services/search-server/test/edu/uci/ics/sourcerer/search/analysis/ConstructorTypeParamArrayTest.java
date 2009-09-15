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

import edu.uci.ics.sourcerer.search.analysis.DelimiterFilterFactory;
import edu.uci.ics.sourcerer.search.analysis.FqnFilter;
import edu.uci.ics.sourcerer.search.analysis.FqnFilterFactory;
import edu.uci.ics.sourcerer.search.analysis.FragmentFilter;
import edu.uci.ics.sourcerer.search.analysis.NoTokenizerFactory;

/**
 * 
 * @author <a href="bajracharya@gmail.com">Sushil Bajracharya</a>
 * @created Sep 14, 2009
 *
 */
public class ConstructorTypeParamArrayTest extends BaseTokenTestCaseSourcerer {

	TokenStream is;
	FqnFilterFactory fqnFactory = new FqnFilterFactory();
	DelimiterFilterFactory dFactory = new DelimiterFilterFactory();
	
	public void setUp() throws Exception {
		super.setUp();
	}
	
	

	// AbstractTypeTester$AResource.post(<T>)
	// ComponentConstructor$ConstructorInjectablePair.<init>(java.lang.reflect.Constructor<<T>>,java.util.List<com.sun.jersey.spi.inject.Injectable>)
	
	// AnnotatedMethod.asList(<T>[])
	public void testSigArray() throws Exception {
		Map<String, String> mapFqn = new HashMap<String, String>();
		mapFqn.put("extractSig", "1");
		mapFqn.put("shortNamesOnly","0");
		
		fqnFactory.init(mapFqn);

//		// note .. there is no whitespace here 
//		is = new WhitespaceTokenizer(new StringReader("AnnotatedMethod.asList(<T>[])"));
		
		NoTokenizerFactory ntf = new NoTokenizerFactory();
		is = ntf.create(new StringReader("AnnotatedMethod.asList(<T>[])"));

		FqnFilter ff = fqnFactory.create(is);
		FragmentFilter f = new FragmentFilter(ff);
		
		final Token reusableToken = new Token();
		Token nextToken = f.next(reusableToken);
		assertEquals("<T>[]",nextToken.term());
		
		assertNull(f.next(reusableToken));
	}
	
	
	// java.util.List<<?+dalma.Condition<<?+<T>>>>>
	public void testFqn() throws Exception {
		Map<String, String> mapFqn = new HashMap<String, String>();
		mapFqn.put("extractSig", "0");
		mapFqn.put("shortNamesOnly","0");
		
		fqnFactory.init(mapFqn);

//		// note .. there is no whitespace here 
//		is = new WhitespaceTokenizer(new StringReader("java.util.List<<?+dalma.Condition<<?+<T>>>>>"));
		
		NoTokenizerFactory ntf = new NoTokenizerFactory();
		is = ntf.create(new StringReader("java.util.List<<?+dalma.Condition<<?+<T>>>>>"));

		FqnFilter ff = fqnFactory.create(is);
		
		
		final Token reusableToken = new Token();
		Token nextToken = ff.next(reusableToken);
		assertEquals("java.util.List",nextToken.term());
		
		assertNull(ff.next(reusableToken));
	}
	
	public void testFqnArray() throws Exception {
		Map<String, String> mapFqn = new HashMap<String, String>();
		mapFqn.put("extractSig", "0");
		mapFqn.put("shortNamesOnly","0");
		
		fqnFactory.init(mapFqn);

		// note .. there is no whitespace here 
		is = new WhitespaceTokenizer(new StringReader("java.util.List<<?+dalma.Condition<<?+<T>>>>>[][]"));

		FqnFilter ff = fqnFactory.create(is);
		
		
		final Token reusableToken = new Token();
		Token nextToken = ff.next(reusableToken);
		assertEquals("java.util.List[][]",nextToken.term());
		
		assertNull(ff.next(reusableToken));
	}
	
	// java.util.List.add(<<?+dalma.Condition<<?+<T>>>>>)  
	public void testSig2() throws Exception {
		Map<String, String> mapFqn = new HashMap<String, String>();
		mapFqn.put("extractSig", "1");
		mapFqn.put("shortNamesOnly","0");
		
		fqnFactory.init(mapFqn);

		// note .. there is no whitespace here 
		is = new WhitespaceTokenizer(new StringReader("java.util.List.add(<<?+dalma.Condition<<?+<T>>>>>)"));

		FqnFilter ff = fqnFactory.create(is);
		
		
		final Token reusableToken = new Token();
		Token nextToken = ff.next(reusableToken);
		assertEquals("<T>",nextToken.term());
		
		assertNull(ff.next(reusableToken));
	}
	
	
	
	// <X+net.java.ao.RawEntity<<T>>>
	
	public void testSig1() throws Exception {
		Map<String, String> mapFqn = new HashMap<String, String>();
		mapFqn.put("extractSig", "1");
		mapFqn.put("shortNamesOnly","1");
		
		fqnFactory.init(mapFqn);

		// note .. there is no whitespace here 
		is = new WhitespaceTokenizer(new StringReader("_test(<N>,java.lang.Class,<V>)"));

		FqnFilter ff = fqnFactory.create(is);
		FragmentFilter f = new FragmentFilter(ff);
		
		final Token reusableToken = new Token();
		Token nextToken = f.next(reusableToken);
		assertEquals("<T>",nextToken.term());
		nextToken = f.next(reusableToken);
		assertEquals("Class",nextToken.term());
		nextToken = f.next(reusableToken);
		assertEquals("<T>",nextToken.term());
		assertNull(f.next(reusableToken));
	}
	
	// for now fqns for constructors will end with a '.'
	// EntityManager$CacheKey.<init>(<T>,java.lang.Class<<?+net.java.ao.RawEntity<<T>>>>)
	public void testInit() throws Exception {
		Map<String, String> mapFqn = new HashMap<String, String>();
		mapFqn.put("extractSig", "0");
		mapFqn.put("shortNamesOnly","0");
		
		fqnFactory.init(mapFqn);

		// note .. there is no whitespace here 
		is = new WhitespaceTokenizer(new StringReader("EntityManager$CacheKey.<init>(<T>,java.lang.Class<<?+net.java.ao.RawEntity<<T>>>>)"));

		FqnFilter ff = fqnFactory.create(is);
		
		final Token reusableToken = new Token();
		Token nextToken = ff.next(reusableToken);
		assertEquals("EntityManager$CacheKey.<init>",nextToken.term());

		
		assertNull(ff.next(reusableToken));
	}
	
	// EntityManager$CacheKey.<init>(<T>,java.lang.Class<<?+net.java.ao.RawEntity<<T>>>>)
	public void testInitFragment() throws Exception {
		Map<String, String> mapFqn = new HashMap<String, String>();
		mapFqn.put("extractSig", "0");
		mapFqn.put("shortNamesOnly","0");
		
		fqnFactory.init(mapFqn);

		// note .. there is no whitespace here 
		is = new WhitespaceTokenizer(new StringReader("EntityManager$CacheKey.<init>(<T>,java.lang.Class<<?+net.java.ao.RawEntity<<T>>>>)"));

		FqnFilter ff = fqnFactory.create(is);
		FragmentFilter f = new FragmentFilter(ff);
		
		final Token reusableToken = new Token();
		Token nextToken = f.next(reusableToken);
		assertEquals("EntityManager$CacheKey",nextToken.term());
		
		nextToken = f.next(reusableToken);
		assertEquals("<init>",nextToken.term());
		
		assertNull(f.next(reusableToken));
	}
	
	// sun.net.spi.nameservice.dns.DNSNameService.<clinit>() 
	public void testClinitFragment() throws Exception {
		Map<String, String> mapFqn = new HashMap<String, String>();
		mapFqn.put("extractSig", "0");
		mapFqn.put("shortNamesOnly","0");
		
		fqnFactory.init(mapFqn);

		// note .. there is no whitespace here 
		is = new WhitespaceTokenizer(new StringReader("DNSNameService.<clinit>()"));

		FqnFilter ff = fqnFactory.create(is);
		FragmentFilter f = new FragmentFilter(ff);
		
		final Token reusableToken = new Token();
		Token nextToken = f.next(reusableToken);
		assertEquals("DNSNameService",nextToken.term());
		
		nextToken = f.next(reusableToken);
		assertEquals("<clinit>",nextToken.term());
		
		assertNull(f.next(reusableToken));
	}
	
	// java.util.Comparator<<T>>[]
	// com.sun.jmx.snmp.IPAcl.ParseException.<init>(com.sun.jmx.snmp.IPAcl.Token,int[][],java.lang.String[]) 
	
	
	// Table.addAll(java.lang.reflect.TypeVariable<<?>>[],int[][])
	public void testArrayInSigSName1() throws Exception{
		Map<String, String> mapFqn = new HashMap<String, String>();
		mapFqn.put("extractSig", "1");
		mapFqn.put("shortNamesOnly","1");
		
		fqnFactory.init(mapFqn);

		// note .. there is no whitespace here 
		is = new WhitespaceTokenizer(
				new StringReader("Table.addAll(java.lang.reflect.TypeVariable<<?>>[],int[][])"));

		FqnFilter f = fqnFactory.create(is);
		
		final Token reusableToken = new Token();
		Token nextToken = f.next(reusableToken);
		assertEquals("TypeVariable[],int[][]",nextToken.term());
		
		assertNull(f.next(reusableToken));
	}
	
	public void testArrayInSigFragment() throws Exception{
		Map<String, String> mapFqn = new HashMap<String, String>();
		mapFqn.put("extractSig", "1");
		mapFqn.put("shortNamesOnly","0");
		
		fqnFactory.init(mapFqn);

		// note .. there is no whitespace here 
		is = new WhitespaceTokenizer(
				new StringReader("Table.addAll(java.lang.reflect.TypeVariable<<?>>[],int[][],java.lang.String[])"));

		FqnFilter ff = fqnFactory.create(is);
		FragmentFilter f = new FragmentFilter(ff);
		
		final Token reusableToken = new Token();
		Token nextToken = f.next(reusableToken);		
		assertEquals("java",nextToken.term());
		
		nextToken = f.next(reusableToken);
		assertEquals("lang",nextToken.term());
		
		nextToken = f.next(reusableToken);
		assertEquals("reflect",nextToken.term());
		
		nextToken = f.next(reusableToken);
		assertEquals("TypeVariable[]",nextToken.term());
		
		nextToken = f.next(reusableToken);
		assertEquals("int[][]",nextToken.term());
		
		nextToken = f.next(reusableToken);
		assertEquals("java",nextToken.term());
		
		nextToken = f.next(reusableToken);
		assertEquals("lang",nextToken.term());
		
		nextToken = f.next(reusableToken);
		assertEquals("String[]",nextToken.term());
		
		
		
		assertNull(f.next(reusableToken));
	}
	
	// net.java.privateer.writer.TypeVariableTable.addAll(java.lang.reflect.TypeVariable<<?>>[])
	public void testSigWithArray() throws Exception{
		Map<String, String> mapFqn = new HashMap<String, String>();
		mapFqn.put("extractSig", "1");
		mapFqn.put("shortNamesOnly","0");
		
		fqnFactory.init(mapFqn);

		// note .. there is no whitespace here 
		is = new WhitespaceTokenizer(
				new StringReader("net.java.privateer.writer.TypeVariableTable.addAll(java.lang.reflect.TypeVariable<<?>>[])"));

		FqnFilter f = fqnFactory.create(is);
		
		final Token reusableToken = new Token();
		Token nextToken = f.next(reusableToken);
		assertEquals("java.lang.reflect.TypeVariable[]",nextToken.term());
		
		assertNull(f.next(reusableToken));
	}

}
