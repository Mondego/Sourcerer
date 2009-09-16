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
package edu.uci.ics.sourcerer.codecrawler.urlfilterplugin.plugins;

import java.util.HashSet;
import java.util.Set;

import edu.uci.ics.sourcerer.codecrawler.urlfilterplugin.UrlFilterPlugin;

/**
 * @author <a href="mailto:hahrot@yahoo.com">Huy A. Huynh</a>
 * 
 * Plug-ins that wish to filter on some domain (well, the actual domain,
 * not the "domain" in the concept of the crawler) can extend this class.
 * In their no-arg constructors, call the this class constructor with
 * the domain name they want to filter on as the only argument.
 */
public abstract class DomainFilterPlugin extends UrlFilterPlugin {

	private String domainName;
	
	public DomainFilterPlugin(String domainName) {
		this.domainName = domainName.toLowerCase(); 
	}
	
	public Set<String> filterUrl(String url, String containerUrl) {
		
		String original = url;
		
		url = getFirstChunkFromUrl(url);
		
		if (url.toLowerCase().contains(domainName)) {
			HashSet<String> result = new HashSet<String>();
			result.add(original);
			return result;
		}
		else
			return null;
	}
	
	/**
	 * @author skb
	 * @param url 
	 * @return only get the part between :// and the first /
	 * 			of url
	 */
	public static String getFirstChunkFromUrl(String url){
		int idx;
		//only get the part between :// and the first /
		if ((idx = url.indexOf("://")) > 0) {
			url = url.substring(idx+3);
		}
		if ((idx = url.indexOf("/")) > 0) {
			url = url.substring(0, idx);
		}
		return url;
		
	}

}
