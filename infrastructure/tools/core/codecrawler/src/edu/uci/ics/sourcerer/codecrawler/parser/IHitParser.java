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
package edu.uci.ics.sourcerer.codecrawler.parser;


import java.util.Set;

import org.htmlparser.Parser;

import edu.uci.ics.sourcerer.codecrawler.db.Hit;

/**
 * @author <a href="mailto:hahrot@yahoo.com">Huy A. Huynh</a>
 * 
 */
public interface IHitParser {

	/**
	 * @param parser
	 * @param referringUrl
	 * @return The set of the HITs or <code>null</code> if
	 * this parser is not responsible for parsing this type of URL.
	 * Note that the set of the HITs might be empty if
	 * this parser has tried to parse but found no HITs.
	 */
	public Set<Hit> parseHits(Parser htmlParser, String referringUrl);

}
