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

import edu.uci.ics.sourcerer.codecrawler.md5hash.MD5Hash;
import edu.uci.ics.sourcerer.codecrawler.network.UrlString;

/**
 * @author <a href="mailto:hahrot@yahoo.com">Huy A. Huynh</a>
 */
public interface IVisitedGateway {

	public void setUrlExpirationDays(int days);

	public int getUrlExpirationDays();

	public void setDocExpirationDays(int days);

	public int getDocExpirationDays();

	public void setDomainExpirationDays(int days);

	public int getDomainExpirationDays();

	public boolean hasExpired(VisitedUrlEntry entry);

	public boolean hasExpired(VisitedDocEntry entry);

	public boolean hasExpired(VisitedDomainEntry entry);

	/**
	 * @param url
	 * @return <code>true</code> if the URL has not been visited
	 * or has expired.
	 */
	public boolean updateUrlExpiration(UrlString url);

	public boolean isUrlVisited(UrlString url);

	/**
	 * @param hashCode
	 * @return <code>true</code> if the MD5Hash has not been visited
	 * or has expired.
	 */
	public boolean updateDocExpiration(MD5Hash hashCode);

	public boolean isDocVisited(MD5Hash hashCode);

	/**
	 * @param domain
	 * @return <code>true</code> if the domain has not been visited
	 * or has expired
	 */
	public boolean updateDomainExpiration(String domain);

	public boolean isDomainVisited(String domain);
}
