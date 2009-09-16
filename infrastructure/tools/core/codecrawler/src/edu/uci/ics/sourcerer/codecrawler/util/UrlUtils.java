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
package edu.uci.ics.sourcerer.codecrawler.util;

import java.util.StringTokenizer;

/**
 * @author <a href="mailto:hahrot@yahoo.com">Huy A. Huynh</a>
 * 
 */
public class UrlUtils {

	public static int URL_MAX_LENGTH = 0x100;

	/**
	 * This method does the followings:<br/>
	 *  - remove anchor links (#top, etc.)
	 *  - remove jsession parameters
	 *  - truncate the length to 256 of it is longer
	 * @param url The URL to fix, as a String.
	 * @return The fixed URL as a String.
	 */
	public static String fixLink(String url) {
		//remove session parameters
		int k1 = url.indexOf(";jsessionid=");
		if (k1 > 0) {
			int k2 = url.indexOf("?", k1 + 1);

			if (k2 > k1) {
				url = url.substring(0, k1) + url.substring(k2);
			} else {
				url = url.substring(0, k1);
			}
		}

		//remove anchor
		int k = url.indexOf("#");
		if (k > 0)
			url = url.substring(0, k);

		//truncate length
		if (url.length() > URL_MAX_LENGTH)
			url = url.substring(0, URL_MAX_LENGTH);

		return url;
	}
	
	public static String stripHttp(String url) {
		url = url.replace("http://", "");
		return url.replace("https://", "");
	}
	
	public static String stripQuery(String url) {
		if (url.indexOf("?") != -1)
			return url.substring(0, url.indexOf("?"));
		else
			return url;
	}
	
	public static String getQuery(String url) {
		if (url.indexOf("?") != -1)
			return url.substring(url.indexOf("?")+1);
		else
			return null;
	}
	
	public static String getHostName(String url) {
		url = stripHttp(url);
		if (url.indexOf("/") != -1)
			return url.substring(0, url.indexOf("/"));
		else
			return url;
	}
	
	/**
	 * Example: http://www.mycom.com/folder1/folder11/file1.php?a=b
	 * will return list of Strings: www.mycom.com, folder1, folder11, file1.php
	 * in that order
	 */
	public static String[] getLevels(String url) {
		url = stripHttp(url);
		url = stripQuery(url);
		StringTokenizer tokenizer = new StringTokenizer(url, "/");
		String[] list = new String[tokenizer.countTokens()];
		int idx = 0;
		while (tokenizer.hasMoreTokens())
			list[idx++] = tokenizer.nextToken();
		return list;
	}
}
