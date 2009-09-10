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
package edu.uci.ics.sourcerer.scs.server;

import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

/**
 * @author <a href="bajracharya@gmail.com">Sushil Bajracharya</a>
 * @created Sep 10, 2009
 *
 */
public class TestHighlighter extends TestCase {

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	public void testApiHighlight(){
		
		String code = "<span class=\"string\">string</span> for(Entity e: mEntity2.values()){ _local.Entity.Add.o(e); }";
		Set<String> snames = new HashSet<String>();
		snames.add("Entity");
		snames.add("values");
		snames.add("add");
		snames.add("string");
		
		System.out.println(Highlighter.highlightApiSnamesUsed(code, snames));
		
		code = "Arrays.fill(string,ca,0,precision - slen,<span class=\"string\">string</span>);";
		snames.clear();
		//snames.add("zero");
		//snames.add("pad");
		snames.add("string");
		System.out.println(Highlighter.highlightSearchTerms(code, snames));
		
		code = "String s;";
		System.out.println(Highlighter.highlightSearchTerms(code, snames));
		
	}

}
