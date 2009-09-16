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
package edu.uci.ics.sourcerer.codecrawler.db.impl;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import edu.uci.ics.sourcerer.codecrawler.db.IGatewayFactory;
import edu.uci.ics.sourcerer.codecrawler.db.IVisitedDocGateway;
import edu.uci.ics.sourcerer.codecrawler.db.IVisitedGateway;
import edu.uci.ics.sourcerer.codecrawler.db.IVisitedUrlGateway;
import edu.uci.ics.sourcerer.codecrawler.db.VisitedDocEntry;
import edu.uci.ics.sourcerer.codecrawler.db.VisitedDomainEntry;
import edu.uci.ics.sourcerer.codecrawler.db.VisitedUrlEntry;
import edu.uci.ics.sourcerer.codecrawler.md5hash.MD5Hash;
import edu.uci.ics.sourcerer.codecrawler.network.UrlString;

/**
 * @author <a href="mailto:hahrot@yahoo.com">Huy A. Huynh</a>
 * 
 */
public class VisitedGateway implements IVisitedGateway {

	public static final int DEFAULT_URL_EXPIRATION_DAYS = 30;
	public static final int DEFAULT_DOC_EXPIRATION_DAYS = 30;
	public static final int DEFAULT_DOMAIN_EXPIRATION_DAYS = 30;

	protected IVisitedUrlGateway urlGateway;
	protected IVisitedDocGateway docGateway;

	private int urlExpirationDays = DEFAULT_URL_EXPIRATION_DAYS;
	private int docExpirationDays = DEFAULT_DOC_EXPIRATION_DAYS;
	private int domainExpirationDays = DEFAULT_DOMAIN_EXPIRATION_DAYS;

	public VisitedGateway(IGatewayFactory gatewayFactory) {
		urlGateway = gatewayFactory.getVisitedUrlGateway();
		docGateway = gatewayFactory.getVisitedDocGateway();
	}

	public void setUrlExpirationDays(int days) {
		urlExpirationDays = days;
	}

	public int getUrlExpirationDays() {
		return urlExpirationDays;
	}

	public int getDocExpirationDays() {
		return docExpirationDays;
	}

	public void setDocExpirationDays(int docExpirationDays) {
		this.docExpirationDays = docExpirationDays;
	}

	public int getDomainExpirationDays() {
		return domainExpirationDays;
	}

	public void setDomainExpirationDays(int domainExpirationDays) {
		this.domainExpirationDays = domainExpirationDays;
	}

	public boolean hasExpired(VisitedDocEntry entry) {
		return ((entry == null) || hasExpired(entry.getLastVisitDate(), docExpirationDays));
	}

	public boolean hasExpired(VisitedDomainEntry entry) {
		return ((entry == null) || hasExpired(entry.getLastVisitDate(), domainExpirationDays));
	}

	public boolean hasExpired(VisitedUrlEntry entry) {
		return ((entry == null) || hasExpired(entry.getLastVisitDate(), urlExpirationDays));
	}

	public boolean isDocVisited(MD5Hash hashCode) {
		VisitedDocEntry entry = docGateway.getEntry(hashCode);
		return ((entry != null) && !hasExpired(entry.getLastVisitDate(), docExpirationDays));
	}

	public boolean isDomainVisited(String domain) {
		VisitedDomainEntry entry = urlGateway.getDomainEntry(domain);
		return ((entry != null) && !hasExpired(entry.getLastVisitDate(), domainExpirationDays));
	}

	public boolean isUrlVisited(UrlString url) {
		VisitedUrlEntry entry = urlGateway.getUrlEntry(url);
		return ((entry != null) && !hasExpired(entry.getLastVisitDate(), urlExpirationDays));
	}

	public boolean updateDocExpiration(MD5Hash hashCode) {
		VisitedDocEntry entry = docGateway.getEntry(hashCode);
		if (entry == null) {
			entry = new VisitedDocEntry(docGateway.getNewId(), hashCode, new Date());
			docGateway.saveEntry(entry);
			return true;
		} else if (hasExpired(entry.getLastVisitDate(), docExpirationDays)) {
			entry.setLastVisitDate(new Date());
			docGateway.saveEntry(entry);
			return true;
		} else {
			return false;
		}
	}

	public boolean updateDomainExpiration(String domain) {
		VisitedDomainEntry entry = urlGateway.getDomainEntry(domain);
		if (entry == null) {
			entry = new VisitedDomainEntry(urlGateway.getNewDomainId(), domain, new Date(), 0);
			urlGateway.saveEntry(entry);
			return true;
		} else if (hasExpired(entry.getLastVisitDate(), domainExpirationDays)) {
			entry.setLastVisitDate(new Date());
			entry.setVisitedCount(entry.getVisitedCount() + 1);
			urlGateway.saveEntry(entry);
			return true;
		} else {
			return false;
		}
	}

	public boolean updateUrlExpiration(UrlString url) {
		VisitedUrlEntry entry = urlGateway.getUrlEntry(url);
		if (entry == null) {
			entry = new VisitedUrlEntry(urlGateway.getNewUrlId(), url, new Date(), 0);
			urlGateway.saveEntry(entry);
			return true;
		} else if (hasExpired(entry.getLastVisitDate(), urlExpirationDays)) {
			entry.setLastVisitDate(new Date());
			entry.setVisitedCount(entry.getVisitedCount() + 1);
			urlGateway.saveEntry(entry);
			return true;
		} else {
			return false;
		}
	}

	protected boolean hasExpired(Date date, int expirationDays) {
		if (date == null)
			return true;
		Calendar calendar = new GregorianCalendar();
		calendar.add(Calendar.DAY_OF_MONTH, -expirationDays);
		return calendar.getTime().compareTo(date) >= 0;
	}

}
