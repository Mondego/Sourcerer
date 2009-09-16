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
 * Processing "certainty" information.
 * Certainty information is present like in URI query format.
 * Sererval examples:
 * Java?certainty=1.0;C++?certainty=0.0
 * Java?version=1.4&certainty=1.0
 */
public class CertaintyUtils {

	public static String addCertaintyToValue(String valueToken, double certainty) {
		if (valueToken == null)
			throw new NullPointerException("Value token passed as null");
		if (certainty < 0.0 || certainty > 1.0)
			throw new CertaintyException("Invalid certainty number");
		
		if (valueToken.contains("?"))
			if (!valueToken.endsWith("&") && !valueToken.endsWith("?"))
				return String.format("%s&certainty=%f", valueToken, certainty);
			else
				return String.format("%scertainty=%f", valueToken, certainty);
		else
			return String.format("%s?certainty=%f", valueToken, certainty);
	}
	
	public static String addValueToList(String list, String valueToken) {
		if (list == null)
			list = "";
		else if ((list.length() > 0) && (!list.endsWith(";")))
			list += ";";
		
		return list + valueToken;
	}
	
	public static String[] separateValueTokens(String list) {
		StringTokenizer tokenizer = new StringTokenizer(list, ";");
		String[] tokens = new String[tokenizer.countTokens()];
		int idx = 0;
		while (tokenizer.hasMoreTokens())
			tokens[idx++] = tokenizer.nextToken();
		return tokens;
	}
	
	public static double getCertainty(String valueToken) {
		int queryIdx = valueToken.indexOf("?");
		if (queryIdx < 0)
			throw new CertaintyException("No certainty found");
		
		String query = valueToken.substring(queryIdx);	//? is still there
		StringTokenizer tokenizer = new StringTokenizer(query, "?&");
		while (tokenizer.hasMoreTokens()) {
			String token = tokenizer.nextToken();
			if (token.startsWith("certainty=")) {
				if (token.equals("certainty="))
					throw new CertaintyException("No certainty found");
				try {
					double certaintyValue = Double.parseDouble(token.substring(token.indexOf("certainty=")+"certainty=".length()));
					if (certaintyValue < 0.0 || certaintyValue > 1.0)
						throw new CertaintyException("Invalid certainty number");
					return certaintyValue;
				} catch (NumberFormatException e) {
					throw new CertaintyException("Invalid certainty number");
				}
			}
		}
		throw new CertaintyException("No certainty found");
	}
	
}
