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
package edu.uci.ics.sourcerer.codecrawler.urlqueue.impl;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.PriorityQueue;

import edu.uci.ics.sourcerer.codecrawler.db.IGatewayFactory;
import edu.uci.ics.sourcerer.codecrawler.db.IUrlQueueGateway;
import edu.uci.ics.sourcerer.codecrawler.db.IVisitedGateway;
import edu.uci.ics.sourcerer.codecrawler.db.IVisitedUrlGateway;
import edu.uci.ics.sourcerer.codecrawler.db.UrlQueueEntry;
import edu.uci.ics.sourcerer.codecrawler.db.VisitedDomainEntry;
import edu.uci.ics.sourcerer.codecrawler.db.VisitedUrlEntry;
import edu.uci.ics.sourcerer.codecrawler.network.UrlString;
import edu.uci.ics.sourcerer.codecrawler.urlqueue.IUrlPriorityQueue;
import edu.uci.ics.sourcerer.codecrawler.urlqueue.PriorityType;
import edu.uci.ics.sourcerer.codecrawler.util.IPerformanceReportable;
import edu.uci.ics.sourcerer.common.LoggerUtils;

/**
 * @author <a href="mailto:hahrot@yahoo.com">Huy A. Huynh</a>
 * 
 */
public class UrlPriorityQueue implements IUrlPriorityQueue, IPerformanceReportable {

	private static final boolean debugEnabled = LoggerUtils.isDebugEnabled(UrlPriorityQueue.class);
	
	public static final long ACTUAL_QUEUE_MOD = 1000000;

	/**
	 * <code>CACHE_SIZE</code> only refers to the cache to store
	 * URLs not within the domain that is currently being crawled.
	 * All links within the domain are stored and being processed
	 * directly from memory.
	 */
	public static final int CACHE_SIZE = 100000;

	private static final int PQ_INIT_SIZE = 100;

	private static final Date ZERO_DATE = new Date(0);

	/**
	 * Comparator for priority queue(s).
	 */
	protected static final Prioritizer PRIORITIZER = new Prioritizer();

	private IUrlQueueGateway queueGateway;
	private IVisitedUrlGateway visitedUrlGateway;
	private IVisitedGateway visitedGateway;

	/**
	 * This queue stores URLs within the current domain
	 * and have not been visited (or have not expired).
	 */
	private PriorityQueue<UrlQueueEntry> topQueue;

	/**
	 * This hash set helps reduce duplicated links in the queue.
	 * This duplication might happen if a link is in the queue
	 * but has never been actually visited. This is a set of
	 * <code>String</code> objects.
	 */
	private HashSet<String> urlInQueue = new HashSet<String>();

	private UrlString lastUrl;
	private String currentDomain;
	private Date curDomainLastVisit = new Date();

	public synchronized UrlString getNextUrl() {
		while (true) {
			if (topQueue.isEmpty()) {
				if (!updateGateway())
					return null;
			}
			
			UrlString url = ((UrlQueueEntry)topQueue.remove()).getUrl();
			
			if ((lastUrl != null) && (url.equals(lastUrl)))
				continue;		//skip the same URLs
			
			lastUrl = url;
			
			if (!visitedGateway.updateUrlExpiration(url)) {
				//for some reason this has been recently visited, retrieve the next one
				continue;
			}

			if (debugEnabled)
				LoggerUtils.debug("  **Current domain: " + currentDomain
						+ "    PriorityQueue Count: " + topQueue.size());

			return url;
		}
	}

	/**
	 * Retrieves the next chunk of URLs of a domain from the URL
	 * queue gateway and puts them into the topQueue. Also updates
	 * the visited domain gateway.
	 * @return <code>true</code> if after this, topQueue is not empty.
	 */
	private synchronized boolean updateGateway() {
		if (debugEnabled)
			LoggerUtils.debug(this, "updateGateway()");
		
		topQueue.clear();			//should already be empty
		urlInQueue.clear();			//visited URLs are stilled stored in here
		
		Collection<UrlString> entries = queueGateway.getTopEntries(currentDomain);
		if (!entries.isEmpty()) {
			if (debugEnabled)
				LoggerUtils.debug(this, "updateGateway(): getTopEntries() return " + entries.size() + " URL(s)");
			
			//extracts current domain and sets its last visit to be today
			UrlString url = (UrlString)entries.iterator().next();
			currentDomain = url.getDomainName();
			curDomainLastVisit = new Date();

			//TODO
			//this domain might have been recently visited.
			//we'll just ignore that for now
			visitedGateway.updateDomainExpiration(currentDomain);

			//moves all URLS of the same domain to the topQueue
			addUrls(entries);
			if (topQueue.isEmpty()) {
				if (debugEnabled)
					LoggerUtils.debug(this, "updateGateway(): found URLs but not added any of them...");
				return false;
			}
			
			/*
			//generate the URL to all levels
			UrlString topUrl = ((UrlQueueEntry)topQueue.peek()).getUrl();
			try {
				String[] levels = UrlUtils.getLevels(topUrl.toString());
				if (levels != null) {
					String urlSoFar = "http://";
					for (int i = 0; i < levels.length; i++) {
						urlSoFar += levels[i] + "/";
						UrlString homepage = new UrlString(urlSoFar);
						addUrl(homepage);
					}
				}
			} catch (MalformedURLException e) {
				//ignored
			}
			*/

			return true;
		} else
			return false;
	}

	public synchronized boolean addUrl(UrlString url) {
		if (urlInQueue.contains(url.toString()))
			return false;
		
		boolean added = false;
		
		if (url.hasSameDomain(currentDomain)) {
			VisitedUrlEntry visitedUrlEntry = visitedUrlGateway.getUrlEntry(url);
			if (visitedUrlEntry == null) {
				//URL is not in the visited database
				//make its last visit date be ZERO, long long time ago!
				PriorityType priority =
					new PriorityType(curDomainLastVisit, currentDomain, ZERO_DATE, url.toString());
				if (topQueue.size() < CACHE_SIZE) {		//otherwise, sorryyyyyy!
					topQueue.add(new UrlQueueEntry(0, url, priority));	//id does not matter
					urlInQueue.add(url.toString());
					added = true;
				}
			} else if (visitedGateway.hasExpired(visitedUrlEntry)) {
				//URL is already in the visited database
				//retrieve its last visit date
				PriorityType priority =
					new PriorityType(curDomainLastVisit, currentDomain, visitedUrlEntry.getLastVisitDate(), url.toString());
				if (topQueue.size() < CACHE_SIZE) {		//otherwise, sorryyyyyy!
					topQueue.add(new UrlQueueEntry(0, url, priority));	//id does not matter
					urlInQueue.add(url.toString());
					added = true;
				}
			} else {
				//ignores the URL since it has been recently visited
			}
		} else {
			VisitedDomainEntry visitedDomainEntry = visitedUrlGateway.getDomainEntry(url.getDomainName());
			Date domainLastVisit = (visitedDomainEntry != null)?
					visitedDomainEntry.getLastVisitDate() : ZERO_DATE;

			VisitedUrlEntry visitedUrlEntry = visitedUrlGateway.getUrlEntry(url);
			if (visitedUrlEntry == null) {
				//URL is not in the visited database
				//make its last visit date be ZERO, long long time ago!
				PriorityType priority =	new PriorityType(domainLastVisit, url.getDomainName(),
						ZERO_DATE, url.toString());
				queueGateway.saveEntry(new UrlQueueEntry(queueGateway.getNewId(), url, priority));
				added = true;
			} else if (visitedGateway.hasExpired(visitedUrlEntry)) {
				//URL is already in the visited database
				//retrieve its last visit date
				PriorityType priority =	new PriorityType(domainLastVisit, url.getDomainName(),
						visitedUrlEntry.getLastVisitDate(), url.toString());
				queueGateway.saveEntry(new UrlQueueEntry(queueGateway.getNewId(), url, priority));
				added = true;
			} else {
				//ignores the URL since it has been recently visited
			}
		}
		
		return added;
	}

	public synchronized void addUrls(Collection<UrlString> urls) {
		for (UrlString url : urls) {
			addUrl(url);
		}
	}

	public UrlPriorityQueue(IVisitedGateway visitedGateway, IGatewayFactory gatewayFactory) {
		super();
		this.visitedGateway = visitedGateway;
		this.queueGateway = gatewayFactory.getUrlQueueGateway();
		this.visitedUrlGateway = gatewayFactory.getVisitedUrlGateway();
		topQueue = new PriorityQueue<UrlQueueEntry>(PQ_INIT_SIZE, PRIORITIZER);
	}

	public synchronized long getCurrentSize() {
		return topQueue.size() + ((long)urlInQueue.size())*ACTUAL_QUEUE_MOD;
	}

}
