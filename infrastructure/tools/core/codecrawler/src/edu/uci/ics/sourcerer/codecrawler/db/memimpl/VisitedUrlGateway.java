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
package edu.uci.ics.sourcerer.codecrawler.db.memimpl;

import java.util.HashMap;
import java.util.Hashtable;

import edu.uci.ics.sourcerer.codecrawler.db.IVisitedUrlGateway;
import edu.uci.ics.sourcerer.codecrawler.db.VisitedDomainEntry;
import edu.uci.ics.sourcerer.codecrawler.db.VisitedUrlEntry;
import edu.uci.ics.sourcerer.codecrawler.network.UrlString;
import edu.uci.ics.sourcerer.codecrawler.util.IPerformanceReportable;

/**
 * @author <a href="mailto:hahrot@yahoo.com">Huy A. Huynh</a>
 * 
 */
public class VisitedUrlGateway implements IVisitedUrlGateway, IPerformanceReportable {

	private Hashtable<Long, VisitedUrlEntry> urlHashtable;
	private HashMap<UrlString, VisitedUrlEntry> urlMap;				//facilitates quick retreival based on URL
	private Hashtable<Long, VisitedDomainEntry> domainHashtable;
	private HashMap<String, VisitedDomainEntry> domainMap;			//facilitates quick retreival based on domain
	private long newUrlId;
	private long newDomainId;

	public VisitedUrlGateway() {
		urlHashtable = new Hashtable<Long, VisitedUrlEntry>();
		urlMap = new HashMap<UrlString, VisitedUrlEntry>();
		domainHashtable = new Hashtable<Long, VisitedDomainEntry>();
		domainMap = new HashMap<String, VisitedDomainEntry>();
	}

	public VisitedUrlEntry getUrlEntry(UrlString url) {
		synchronized(this) {
			return (VisitedUrlEntry)urlMap.get(url);
		}
	}

	public VisitedUrlEntry getUrlEntry(long id) {
		synchronized(this) {
			return (VisitedUrlEntry)urlHashtable.get(new Long(id));
		}
	}

	public VisitedDomainEntry getDomainEntry(String domain) {
		synchronized(this) {
			return (VisitedDomainEntry)domainMap.get(domain);
		}
	}

	public VisitedDomainEntry getDomainEntry(long id) {
		synchronized(this) {
			return (VisitedDomainEntry)domainHashtable.get(new Long(id));
		}
	}

	public void deleteUrlEntry(UrlString url) {
		synchronized(this) {
			VisitedUrlEntry entry = (VisitedUrlEntry)urlMap.remove(url);
			if (entry != null)
				urlHashtable.remove(new Long(entry.getId()));
		}
	}

	public void deleteUrlEntry(long id) {
		synchronized(this) {
			VisitedUrlEntry entry = (VisitedUrlEntry)urlHashtable.remove(new Long(id));
			if (entry != null)
				urlMap.remove(entry.getUrl());
		}
	}

	public void deleteDomainEntry(String domain) {
		synchronized(this) {
			VisitedDomainEntry entry = (VisitedDomainEntry)domainMap.get(domain);
			if (entry != null)
				domainHashtable.remove(new Long(entry.getId()));
		}
	}

	public void deleteDomainEntry(long id) {
		synchronized(this) {
			domainHashtable.remove(new Long(id));
		}
	}

	public void saveEntry(VisitedUrlEntry entry) {
		synchronized(this) {
			urlHashtable.put(new Long(entry.getId()), entry);
			urlMap.put(entry.getUrl(), entry);
		}
	}

	public void saveEntry(VisitedDomainEntry entry) {
		synchronized(this) {
			domainHashtable.put(new Long(entry.getId()), entry);
			domainMap.put(entry.getDomain(), entry);
		}
	}

	public long getNewUrlId() {
		synchronized(this) {
			while (urlHashtable.get(new Long(newUrlId)) != null)
				newUrlId++;
			return newUrlId;
		}
	}

	public long getNewDomainId() {
		synchronized(this) {
			while (domainHashtable.get(new Long(newDomainId)) != null)
				newDomainId++;
			return newDomainId;
		}
	}

	public long getCurrentSize() {
		synchronized(this) {
			return urlHashtable.size() + domainHashtable.size();
		}
	}

}
