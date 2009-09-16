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

import java.util.Date;

import junit.framework.TestCase;
import edu.uci.ics.sourcerer.codecrawler.db.IGatewayFactory;
import edu.uci.ics.sourcerer.codecrawler.db.IVisitedGateway;
import edu.uci.ics.sourcerer.codecrawler.db.IVisitedUrlGateway;
import edu.uci.ics.sourcerer.codecrawler.db.VisitedDomainEntry;
import edu.uci.ics.sourcerer.codecrawler.db.VisitedUrlEntry;
import edu.uci.ics.sourcerer.codecrawler.db.impl.VisitedGateway;
import edu.uci.ics.sourcerer.codecrawler.db.memimpl.GatewayFactory;
import edu.uci.ics.sourcerer.codecrawler.network.UrlString;
import edu.uci.ics.sourcerer.codecrawler.urlqueue.impl.UrlPriorityQueue;

/**
 * @author <a href="mailto:hahrot@yahoo.com">Huy A. Huynh</a>
 * 
 */
public class UrlPriorityQueueTest extends TestCase {
	
	private UrlPriorityQueue queue;
	private IGatewayFactory gatewayFactory = new GatewayFactory();
	private IVisitedGateway visitedGateway = new VisitedGateway(gatewayFactory);
	private IVisitedUrlGateway visitedUrlGateway = gatewayFactory.getVisitedUrlGateway();

	protected void setUp() throws Exception {
		super.setUp();
		queue = new UrlPriorityQueue(visitedGateway, gatewayFactory);
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	public void test() throws Exception {
		UrlString VISITED_URLS[] = { 
				new UrlString("http://www.tigris.org/"),
				new UrlString("http://www.google.com/"),
				new UrlString("http://www.yahoo.com/")
		};

		UrlString NEW_URLS[] = {
				new UrlString("http://www.tigris.org/index.html"),
				new UrlString("http://www.tigris.org/index1.html"),
				new UrlString("http://www.tigris.org/index2.html"),
				new UrlString("http://www.tigris.org/index3.html"),
		};

		for (int i = 0; i < VISITED_URLS.length; i++){
			VisitedUrlEntry urlEntry = new VisitedUrlEntry(visitedUrlGateway.getNewUrlId(),
					VISITED_URLS[i], new Date(0), 1);
			visitedUrlGateway.saveEntry(urlEntry);
			
			VisitedDomainEntry domainEntry = new VisitedDomainEntry(visitedUrlGateway.getNewDomainId(),
					VISITED_URLS[i].getDomainName(), new Date(0), 1);
			visitedUrlGateway.saveEntry(domainEntry);
		}
		
		//test unmatched domain
		for (int i = 0; i < VISITED_URLS.length; i++) {
			queue.addUrl(VISITED_URLS[i]);
			assertEquals(0, queue.getCurrentSize());
		}
		
		//test retrieval
		UrlString nextUrl;
		while ((nextUrl = queue.getNextUrl()) != null) {
			if (nextUrl.getDomainName().equals(NEW_URLS[0].getDomainName()))
				break;
		}
		assertNotNull(nextUrl);
		
		//test adding new urls
		for (int i = 0; i < NEW_URLS.length-1; i++) {
			queue.addUrl(NEW_URLS[i]);
		}
		nextUrl = queue.getNextUrl();
		assertNotNull(nextUrl);
		assertTrue(nextUrl.getDomainName().equals(NEW_URLS[0].getDomainName()));
		
		//test adding with the same domain
		long oldSize = queue.getCurrentSize() % 1000000;
		queue.addUrl(NEW_URLS[NEW_URLS.length-1]);
		long newSize = queue.getCurrentSize() % 1000000;
		assertTrue(newSize == (oldSize+1));
		
		//test duplicated item
		queue.addUrl(NEW_URLS[NEW_URLS.length-1]);
		assertTrue(newSize == (queue.getCurrentSize() % 1000000));
	}

}
