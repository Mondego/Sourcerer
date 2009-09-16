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

import edu.uci.ics.sourcerer.codecrawler.network.UrlString;

/**
 * @author <a href="mailto:hahrot@yahoo.com">Huy A. Huynh</a>
 *
 * URLs and Domains should be unique.
 */
public interface IVisitedUrlGateway {

	public VisitedUrlEntry getUrlEntry(UrlString url);

	public VisitedUrlEntry getUrlEntry(long id);

	public VisitedDomainEntry getDomainEntry(String domain);

	public VisitedDomainEntry getDomainEntry(long id);

	public void deleteUrlEntry(UrlString url);

	public void deleteUrlEntry(long id);

	public void deleteDomainEntry(String domain);

	public void deleteDomainEntry(long id);

	public void saveEntry(VisitedUrlEntry entry);

	public void saveEntry(VisitedDomainEntry entry);

	public long getNewUrlId();

	public long getNewDomainId();

}
