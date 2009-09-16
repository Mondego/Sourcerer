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

import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;

import junit.framework.TestCase;
import edu.uci.ics.sourcerer.codecrawler.db.IVisitedDocGateway;
import edu.uci.ics.sourcerer.codecrawler.db.VisitedDocEntry;
import edu.uci.ics.sourcerer.codecrawler.db.memimpl.VisitedDocGateway;
import edu.uci.ics.sourcerer.codecrawler.md5hash.MD5Hash;

/**
 * @author <a href="mailto:hahrot@yahoo.com">Huy A. Huynh</a>
 * 
 */
public class VisitedDocGatewayTest extends TestCase {

	private IVisitedDocGateway gateway;

	protected void setUp() throws Exception {
		super.setUp();
		gateway = new VisitedDocGateway();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void test() {
		//testing for duplicated id and hash
		Hashtable idHashtable = new Hashtable();
		Hashtable hashHashtable = new Hashtable();
		for (int i = 0; i < 100; i++) {
			long id = gateway.getNewId();
			MD5Hash hash = MD5Hash.digest("THIS IS A SAMPLE DOC" + id);
			VisitedDocEntry entry = new VisitedDocEntry(id,	hash, new Date());
			assertNull(idHashtable.get(new Long(id)));
			assertNull(hashHashtable.get(hash));
			idHashtable.put(new Long(id), new Object());
			hashHashtable.put(hash, new Object());
			gateway.saveEntry(entry);
		}

		//testing retrieving methods
		Iterator itr = idHashtable.keySet().iterator();
		while (itr.hasNext()) {
			long id = ((Long)itr.next()).longValue();
			assertNotNull(gateway.getEntry(id));
		}
		itr = hashHashtable.keySet().iterator();
		while (itr.hasNext()) {
			assertNotNull(gateway.getEntry((MD5Hash)itr.next()));
		}

		//testing deleting method
		itr = idHashtable.keySet().iterator();
		while (itr.hasNext()) {
			long id = ((Long)itr.next()).longValue();
			gateway.deleteEntry(id);
		}
		itr = hashHashtable.keySet().iterator();
		while (itr.hasNext()) {
			assertNull(gateway.getEntry((MD5Hash)itr.next()));
		}
	}
}
