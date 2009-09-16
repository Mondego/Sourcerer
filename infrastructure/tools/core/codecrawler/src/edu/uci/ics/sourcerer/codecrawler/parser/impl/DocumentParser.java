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
package edu.uci.ics.sourcerer.codecrawler.parser.impl;

import java.net.MalformedURLException;
import java.util.HashSet;
import java.util.Set;

import org.htmlparser.Parser;
import org.htmlparser.util.ParserException;

import edu.uci.ics.sourcerer.codecrawler.db.Hit;
import edu.uci.ics.sourcerer.codecrawler.network.UrlString;
import edu.uci.ics.sourcerer.codecrawler.parser.Document;
import edu.uci.ics.sourcerer.codecrawler.parser.IDocumentParser;
import edu.uci.ics.sourcerer.codecrawler.parser.IHitParser;
import edu.uci.ics.sourcerer.codecrawler.parser.ILinkParser;
import edu.uci.ics.sourcerer.codecrawler.parser.ParseErrorException;

/**
 * @author <a href="mailto:hahrot@yahoo.com">Huy A. Huynh</a>
 * This DocumentParser use supplied HitParsers to parse
 * the HITs from the URL.
 */
public class DocumentParser implements IDocumentParser {

	private IHitParser hitParser;
	private ILinkParser linkParser;

	public DocumentParser(IHitParser hitParser, ILinkParser linkParser) {
		this.hitParser = hitParser;
		this.linkParser = linkParser;
	}

	public void parseDocument(Document document, String referringUrl) throws ParseErrorException {
		if (document == null)
			throw new ParseErrorException("Document is null.");
		
		try {
			Parser parser = new Parser(document.getUrl().toString());

			////////////////////////////////
			//get links
			Set<UrlString> linkSet = new HashSet<UrlString>();	//link set should not be null event if its empty
			if (linkParser != null) {
				Set<String> parsedSet;
				parsedSet = linkParser.parseLinks(parser, referringUrl);
				if (parsedSet != null) {
					for (String url : parsedSet) {
						try {
							UrlString urlString = new UrlString(url);
							linkSet.add(urlString);
						} catch (MalformedURLException e) {}
					}
				}
			}
			document.setLinks(linkSet);
			
			////////////////////////////////
			//get HITs
			Set<Hit> hitSet;
			if (hitParser != null) {
				hitSet = hitParser.parseHits(parser, referringUrl);
				if (hitSet == null)
					hitSet = new HashSet<Hit>();	//hitSet should not be null even if its empty
			} else
				hitSet = new HashSet<Hit>();		//hitSet should not be null even if its empty
			document.setHits(hitSet);

		} catch (ParserException e) {
			throw new ParseErrorException(e.getMessage());
		}
	}

	public void setHitParser(IHitParser hitParser) {
		this.hitParser = hitParser;
	}

}
