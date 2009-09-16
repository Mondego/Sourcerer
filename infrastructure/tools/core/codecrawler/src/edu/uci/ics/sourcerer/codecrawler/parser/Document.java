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

/**
 * @author <a href="mailto:hahrot@yahoo.com">Huy A. Huynh</a>
 */
import java.io.IOException;
import java.util.Collection;

import edu.uci.ics.sourcerer.codecrawler.db.Hit;
import edu.uci.ics.sourcerer.codecrawler.md5hash.MD5Hash;
import edu.uci.ics.sourcerer.codecrawler.network.NetworkInputStreamFactory;
import edu.uci.ics.sourcerer.codecrawler.network.UrlString;

public class Document{
	
	public static Document openDocument(UrlString url) throws IOException {
		Document doc = new Document(null, null, null, url);
		doc.setHashcode(MD5Hash.digest(NetworkInputStreamFactory.getBufferedInputStream(url)));
		return doc;
	}

	protected MD5Hash hashcode;
	protected Collection<Hit> hits;
	protected Collection<UrlString> links;
	protected UrlString url;

	public MD5Hash getHashcode() {
		return hashcode;
	}

	public void setHashcode(MD5Hash hashcode) {
		this.hashcode = hashcode;
	}

	public Collection<Hit> getHits() {
		return hits;
	}

	public void setHits(Collection<Hit> hits) {
		this.hits = hits;
	}

	public Collection<UrlString> getLinks() {
		return links;
	}

	public void setLinks(Collection<UrlString> links) {
		this.links = links;
	}

	public UrlString getUrl() {
		return url;
	}

	public void setUrl(UrlString url) {
		this.url = url;
	}

	public Document(MD5Hash hashcode, Collection<Hit> hits,
			Collection<UrlString> links, UrlString url) {
		this.hashcode = hashcode;
		this.hits = hits;
		this.links = links;
		this.url = url;
	}

}
