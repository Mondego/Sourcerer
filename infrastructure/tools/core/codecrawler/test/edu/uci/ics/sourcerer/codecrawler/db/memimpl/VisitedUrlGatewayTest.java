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

import java.net.MalformedURLException;
import java.util.Date;

import junit.framework.TestCase;
import edu.uci.ics.sourcerer.codecrawler.db.IVisitedUrlGateway;
import edu.uci.ics.sourcerer.codecrawler.db.VisitedDomainEntry;
import edu.uci.ics.sourcerer.codecrawler.db.VisitedUrlEntry;
import edu.uci.ics.sourcerer.codecrawler.db.memimpl.VisitedUrlGateway;
import edu.uci.ics.sourcerer.codecrawler.network.UrlString;

/**
 * @author <a href="mailto:hahrot@yahoo.com">Huy A. Huynh</a>
 * 
 */
public class VisitedUrlGatewayTest extends TestCase {

	private IVisitedUrlGateway gateway;

	protected void setUp() throws Exception {
		super.setUp();
		gateway = new VisitedUrlGateway();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testUrl() {
		try {
			VisitedUrlEntry urlEntry1 = new VisitedUrlEntry(
					gateway.getNewUrlId(), new UrlString("http://www.apache.org"),
					new Date(), 0);
			gateway.saveEntry(urlEntry1);
			VisitedUrlEntry urlEntry2 = new VisitedUrlEntry(
					gateway.getNewUrlId(), new UrlString("http://www.sourceforge.net"),
					new Date(), 0);
			gateway.saveEntry(urlEntry2);
			assertTrue(urlEntry1.getId() != urlEntry2.getId());
			assertEquals(gateway.getUrlEntry(urlEntry1.getId()), urlEntry1);
			assertEquals(gateway.getUrlEntry(urlEntry2.getId()), urlEntry2);
			assertNotNull(gateway.getUrlEntry(new UrlString("http://www.apache.org")));
			assertEquals(gateway.getUrlEntry(new UrlString("http://www.apache.org")), urlEntry1);
			assertEquals(gateway.getUrlEntry(new UrlString("http://www.sourceforge.net")), urlEntry2);
			assertNull(gateway.getUrlEntry(new UrlString("http://www.apache.org/index.html")));

			urlEntry1.setVisitedCount(10);
			gateway.saveEntry(urlEntry1);
			assertEquals(10, gateway.getUrlEntry(urlEntry1.getId()).getVisitedCount());

			gateway.deleteUrlEntry(urlEntry1.getId());
			assertNull(gateway.getUrlEntry(urlEntry1.getId()));
			gateway.deleteUrlEntry(urlEntry2.getUrl());
			assertNull(gateway.getUrlEntry(urlEntry2.getId()));
		} catch (MalformedURLException e) {
			System.out.println("Exception");
		}
	}

	public void testDomain() {
		VisitedDomainEntry domainEntry1 = new VisitedDomainEntry(
				gateway.getNewDomainId(), "www.apache.org",
				new Date(), 0);
		gateway.saveEntry(domainEntry1);
		VisitedDomainEntry domainEntry2 = new VisitedDomainEntry(
				gateway.getNewDomainId(), "www.sourceforge.net",
				new Date(), 0);
		gateway.saveEntry(domainEntry2);
		assertTrue(domainEntry1.getId() != domainEntry2.getId());
		assertEquals(gateway.getDomainEntry(domainEntry1.getId()), domainEntry1);
		assertEquals(gateway.getDomainEntry(domainEntry2.getId()), domainEntry2);
		assertNotNull(gateway.getDomainEntry("www.apache.org"));
		assertNotNull(gateway.getDomainEntry("www.sourceforge.net"));
		assertEquals(gateway.getDomainEntry("www.apache.org"), domainEntry1);
		assertNull(gateway.getDomainEntry("http://www.apache.org"));
		gateway.deleteDomainEntry(domainEntry1.getId());
		assertNull(gateway.getDomainEntry(domainEntry1.getId()));
		gateway.deleteDomainEntry(domainEntry2.getDomain());
		assertNull(gateway.getDomainEntry(domainEntry2.getId()));
	}

}
