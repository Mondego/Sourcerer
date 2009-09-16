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
import java.util.Hashtable;
import java.util.Iterator;

import junit.framework.TestCase;
import edu.uci.ics.sourcerer.codecrawler.db.IUrlQueueGateway;
import edu.uci.ics.sourcerer.codecrawler.db.UrlQueueEntry;
import edu.uci.ics.sourcerer.codecrawler.db.memimpl.UrlQueueGateway;
import edu.uci.ics.sourcerer.codecrawler.network.UrlString;
import edu.uci.ics.sourcerer.codecrawler.urlqueue.PriorityType;

/**
 * @author <a href="mailto:hahrot@yahoo.com">Huy A. Huynh</a>
 * 
 */
public class UrlQueueGatewayTest extends TestCase {

	private IUrlQueueGateway gateway;

	protected void setUp() throws Exception {
		super.setUp();
		gateway = new UrlQueueGateway();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void test() {
		try {
			//testing for duplicated id and hash
			Hashtable idHashtable = new Hashtable();
			for (int i = 0; i < 100; i++) {
				long id = gateway.getNewId();
				UrlQueueEntry entry = new UrlQueueEntry(id,	new UrlString("http://test" + id + ".net"), new PriorityType());
				assertNull(idHashtable.get(new Long(id)));
				idHashtable.put(new Long(id), new Object());
				gateway.saveEntry(entry);
			}

			//testing retrieving methods
			Iterator itr = idHashtable.keySet().iterator();
			while (itr.hasNext()) {
				long id = ((Long)itr.next()).longValue();
				assertNotNull(gateway.getEntry(id));
				assertNotNull(gateway.getEntry(new UrlString("http://test" + id + ".net")));
			}

			//testing deleting method
			itr = idHashtable.keySet().iterator();
			while (itr.hasNext()) {
				long id = ((Long)itr.next()).longValue();
				gateway.deleteEntry(id);
			}
			itr = idHashtable.keySet().iterator();
			while (itr.hasNext()) {
				assertNull(gateway.getEntry(((Long)itr.next()).longValue()));
			}
		} catch (MalformedURLException e) {
			System.out.println("Exception");
		}
	}

}
