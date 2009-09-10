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

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author <a href="bajracharya@gmail.com">Sushil Bajracharya</a>
 * @created Jul 21, 2009
 *
 */
public class Highlighter {
	
	public static String highlightSearchTerms(String javaCode, Set<String> searchTerms){
		
		String styles = "\\B(comment|keyword|srcUncovered|string|text_italic)\\B";
		
		for(String s: searchTerms){
			
			Pattern p = Pattern.compile("(<span[\\s]+class=\"" + styles + "\">[\\s]*)?(?i)"+s+"([\\s]*</span>)?");
			
	//		Pattern p = Pattern.compile("((?i)"+s+")(^\")");
			Matcher m = p.matcher(javaCode);
			if(m.find()){
				
//				System.out.println(m.group(0));
//				System.out.println(m.group(1));
//				System.out.println(m.group(2));
//				System.out.println(m.group(3));
//				System.out.println(m.group(4));
//				System.out.println(m.group(5));
//		
				System.out.println(m.groupCount());
				
				javaCode = m.replaceAll("<span class=\"search-term\">$1$2$3</span>");
			}
		}
		
		return javaCode;
	}
	
	public static String highlightApiSnamesUsed(String javaCode, Set<String> snames){
		
		
		for(String s: snames){
			
			Pattern p = Pattern.compile("([\\W)]+)(" + s +")");	
			Matcher m = p.matcher(javaCode);
			if(m.find()){
				
				javaCode = m.replaceAll("$1<span class=\"used-api\">$2</span>");
				
			}
			
			//javaCode.replaceAll(, )
		}
		
		return javaCode;
	}
	
	
}
