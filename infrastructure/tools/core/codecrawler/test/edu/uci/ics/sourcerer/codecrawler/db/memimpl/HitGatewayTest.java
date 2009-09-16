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

import java.util.HashSet;
import java.util.Iterator;

import junit.framework.TestCase;
import edu.uci.ics.sourcerer.codecrawler.db.Hit;
import edu.uci.ics.sourcerer.codecrawler.db.IHitGateway;
import edu.uci.ics.sourcerer.codecrawler.db.memimpl.HitGateway;

/**
 * @author <a href="mailto:hahrot@yahoo.com">Huy A. Huynh</a>
 * 
 */
public class HitGatewayTest extends TestCase {

	private IHitGateway gateway;

	protected void setUp() throws Exception {
		super.setUp();
		gateway = new HitGateway();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void test() {
		//testing for duplicated id and hash
		HashSet<Long> idHashSet = new HashSet<Long>();
		for (int i = 0; i < 100; i++) {
			long id = gateway.getNewId();
			Hit entry = new Hit(id, "svn:sample.com/sample" + id);
			assertFalse(idHashSet.contains(new Long(id)));
			idHashSet.add(new Long(id));
			gateway.saveHit(entry);
		}
		
		//testing retrieving methods
		Iterator itr = idHashSet.iterator();
		while (itr.hasNext()) {
			long id = ((Long)itr.next()).longValue();
			assertNotNull(gateway.getHit(id));
			assertNotNull(gateway.getHit("svn:sample.com/sample" + id));
		}

		//testing deleting method
		itr = idHashSet.iterator();
		while (itr.hasNext()) {
			long id = ((Long)itr.next()).longValue();
			gateway.deleteHit(id);
		}
		itr = idHashSet.iterator();
		while (itr.hasNext()) {
			assertNull(gateway.getHit(((Long)itr.next()).longValue()));
		}
	}

}
