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
package edu.uci.ics.sourcerer.scs.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;

/**
 * @author <a href="bajracharya@gmail.com">Sushil Bajracharya</a>
 * @created Sep 10, 2009
 */
public class Util {
	
	public static String escapeHtml(String maybeHtml) {
		final Element div = DOM.createDiv();
		DOM.setInnerText(div, maybeHtml);
		return DOM.getInnerHTML(div);

		// return maybeHtml;
	}
	
	public static String zeroPadString(String s, int precision)
    {
        if (s == null)
            return s;
        int slen = s.length();
        if (precision == slen)
            return s;
        else if (precision > slen)
        {
            char[] ca  = new char[precision - slen];
            Arrays.fill(ca,0,precision - slen,'0');
            return new String(ca) + s;
        }
        else
        {
            // Shouldn't happen but just in case 
            // truncate
            return s.substring(0,precision);
        }
    }
	
	public static List<String> convertStringSetToStringList(Set<String> set) {
	    ArrayList<String> al = new ArrayList<String>();
	    
	    for(String s: set){
	    	al.add(s);
	    }
	    
	    return al;
	}

}
