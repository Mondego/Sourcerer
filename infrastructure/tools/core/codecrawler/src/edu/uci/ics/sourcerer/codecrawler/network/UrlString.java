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
package edu.uci.ics.sourcerer.codecrawler.network;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author <a href="mailto:hahrot@yahoo.com">Huy A. Huynh</a>
 * 
 */
public class UrlString {

	public static String PATH_LEVEL_SEPARATOR = "/";

	private URL url;
	private String urlString;

	public UrlString(String url) throws MalformedURLException {
		this.url = new URL(url);
		urlString = this.url.toString();
		urlString = quickFix(urlString);
	}

	public UrlString(URL url) {
		this.url = url;
		urlString = this.url.toString();
		urlString = quickFix(urlString);
	}
	
	private String quickFix(String url) {
		String result = url;
		result = result.replace("&amp;", "&");	//this bug was found when crawling tigris.org
		return result;
	}

	/**
	 * Returns the host name as if calling the <code>getHost()</code> method
	 * of the <code>java.net.URL</code> class.
	 * @return
	 */
	public String getHostName() {
		return url.getHost();
	}

	/**
	 * Returns the newly defined domain name.
	 * Our newly defined domain name consists of the host name, which is the
	 * actual domain, and the the path to the page. We extend the concept
	 * of domain name to minimize the size of priority queue and also to make
	 * the crawler crawl more efficiently.
	 * @return The "domain name".
	 * @see edu.uci.ics.sourcerer.codecrawler.urlqueueimpl.UrlPriorityQueue
	 */
	public String getDomainName() {
		String path = url.getHost() + url.getPath();
		int index = path.lastIndexOf(PATH_LEVEL_SEPARATOR);
		if (index > 0)
			return path.substring(0, index);
		else
			return path;
	}

	public boolean equals(Object obj) {
		if (!(obj instanceof UrlString))
			return false;
		else
			//TODO do something fancy and intelligent here to determine
			//the equality of 2 URLs. Some possibilities
			// - "http://www.google.com" is the same as "www.google.com"
			// - "www.google.coom" is the same as "www.google.com/index.html"
			// - case-sensitive might be important 
			return this.toString().equalsIgnoreCase(obj.toString());
	}

	public int hashCode() {
		return urlString.hashCode();
	}

	public String toString() {
		return urlString;
	}

	public boolean hasSameDomain(String domain) {
		return this.getDomainName().equalsIgnoreCase(domain);
	}

	public String getProtocol() {
		return url.getProtocol();
	}
	
	public String getContentType() throws IOException {
		return url.openConnection().getContentType();
	}
	
}