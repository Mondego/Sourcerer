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

import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;

import edu.uci.ics.sourcerer.codecrawler.db.IUrlQueueGateway;
import edu.uci.ics.sourcerer.codecrawler.db.UrlQueueEntry;
import edu.uci.ics.sourcerer.codecrawler.network.UrlString;
import edu.uci.ics.sourcerer.codecrawler.util.IPerformanceReportable;
import edu.uci.ics.sourcerer.codecrawler.util.UrlUtils;

/**
 * @author <a href="mailto:hahrot@yahoo.com">Huy A. Huynh</a>
 * 
 */
public class UrlQueueGateway implements IUrlQueueGateway, IPerformanceReportable {

	private Hashtable<Long, UrlQueueEntry> table;
	private long newId;

	/**
	 * Optimize memory by eliminating URLs added before.
	 */
	private static final int URLINQUEUE_MAX_SIZE = 10000;
	private HashSet<String> urlInQueue;

	public UrlQueueGateway() {
		table = new Hashtable<Long, UrlQueueEntry>();
		urlInQueue = new HashSet<String>();
	}

	public Collection<UrlString> getTopEntries(String currentDomain) {
		HashSet<UrlString> urls = new HashSet<UrlString>();

		UrlQueueEntry similarDomainEntry = null;
		UrlQueueEntry lowestIdEntry = null;

		String currentHost = (currentDomain != null)? currentDomain : "";

		synchronized(this) {
		
			//get the entry with the lowest id for both a similar domain
			//and any domain
			Iterator itr = table.values().iterator();
			while (itr.hasNext()) {
				UrlQueueEntry tmp = (UrlQueueEntry)itr.next();
				String tmpDomain = UrlUtils.getHostName(tmp.getUrl().toString());
				if (tmpDomain.contains(currentHost) || (currentHost.contains(tmpDomain)))
					if ((similarDomainEntry == null) || (tmp.getId() < similarDomainEntry.getId()))
						similarDomainEntry = tmp;
				if ((lowestIdEntry == null)
						|| (tmp.getId() < lowestIdEntry.getId()))
					lowestIdEntry = tmp;
			}
	
			//if a similar domain entry found, use it
			UrlQueueEntry entry = (similarDomainEntry != null) ? similarDomainEntry : lowestIdEntry;
	
			//retrieve entries with the same domain
			if (entry != null) {
				String domain = UrlUtils.getHostName(entry.getUrl().toString());
				itr = table.values().iterator();
				while (itr.hasNext()) {
					UrlQueueEntry tmp = (UrlQueueEntry)itr.next();
					if (UrlUtils.getHostName(tmp.getUrl().toString()).equals(domain)) {
						urls.add(tmp.getUrl());
						itr.remove();
					}
				}
			}
			
		}

		return urls;
	}

	public UrlQueueEntry getEntry(long id) {
		synchronized(this) {
			return (UrlQueueEntry)table.get(new Long(id));
		}
	}

	public UrlQueueEntry getEntry(UrlString url) {
		synchronized(this) {
			Iterator itr = table.values().iterator();
			while (itr.hasNext()) {
				UrlQueueEntry entry = (UrlQueueEntry)itr.next();
				if (entry.getUrl().equals(url))
					return entry;
			}
			return null;
		}
	}

	public void deleteEntry(long id) {
		synchronized(this) {
			table.remove(new Long(id));
		}
	}

	public void saveEntry(UrlQueueEntry entry) {
		
		synchronized(this) {

			if (entry.getId() == getNewId()) {		//this is new
				if (urlInQueue.contains(entry.getUrl().toString())) {	//URL already added
					UrlQueueEntry oldEntry = getEntry(entry.getUrl());
					if (oldEntry != null)
						entry.setId(oldEntry.getId());	//just change the id back to the existing one
				}
			}
	
			table.put(new Long(entry.getId()), entry);
	
			if (!urlInQueue.contains(entry.getUrl().toString())) {
				urlInQueue.add(entry.getUrl().toString());
				while (urlInQueue.size() > URLINQUEUE_MAX_SIZE)
					urlInQueue.remove(urlInQueue.iterator().next());
			}
			
		}
	}

	public long getNewId() {
		synchronized(this) {
			newId++;
			while (table.get(new Long(newId)) != null)
				newId++;
			return newId;
		}
	}

	public long getCurrentSize() {
		synchronized(this) {
			return table.size();
		}
	}

}
