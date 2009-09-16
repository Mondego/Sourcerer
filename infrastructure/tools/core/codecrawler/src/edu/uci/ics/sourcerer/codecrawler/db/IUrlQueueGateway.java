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
package edu.uci.ics.sourcerer.codecrawler.db;

import java.util.Collection;

import edu.uci.ics.sourcerer.codecrawler.network.UrlString;

/**
 * @author <a href="mailto:hahrot@yahoo.com">Huy A. Huynh</a>
 */
public interface IUrlQueueGateway {

	public UrlQueueEntry getEntry(long id);

	public UrlQueueEntry getEntry(UrlString url);

	/**
	 * Returns a <code>Collection</code> of URLs that are in one
	 * domain. With the hint from <code>currentDomain</code>, the gateway
	 * will try to returns the URLs that are mostly related, which
	 * might mean that they have the same host. Other than that,
	 * the URLs that are added earlier should be returned first (regardless
	 * of their priority, since priority arranges the URL in alphabetical order).
	 * @param currentDomain
	 * @return
	 */
	public Collection<UrlString> getTopEntries(String currentDomain);

	public void deleteEntry(long id);

	public void saveEntry(UrlQueueEntry entry);

	public long getNewId();

}
