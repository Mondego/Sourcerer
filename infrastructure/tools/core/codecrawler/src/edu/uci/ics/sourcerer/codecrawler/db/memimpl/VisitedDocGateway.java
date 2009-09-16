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

import edu.uci.ics.sourcerer.codecrawler.db.IVisitedDocGateway;
import edu.uci.ics.sourcerer.codecrawler.db.VisitedDocEntry;
import edu.uci.ics.sourcerer.codecrawler.md5hash.MD5Hash;
import edu.uci.ics.sourcerer.codecrawler.util.IPerformanceReportable;

/**
 * @author <a href="mailto:hahrot@yahoo.com">Huy A. Huynh</a>
 * 
 */
public class VisitedDocGateway implements IVisitedDocGateway, IPerformanceReportable {

	private Hashtable<Long, VisitedDocEntry> table;
	private HashMap<MD5Hash, VisitedDocEntry> hashcodeMap;		//facilitates fast retrieval using MD5 hashcode
	private long newId;

	public VisitedDocGateway() {
		table = new Hashtable<Long, VisitedDocEntry>();
		hashcodeMap = new HashMap<MD5Hash, VisitedDocEntry>();
	}

	public VisitedDocEntry getEntry(MD5Hash hashcode) {
		synchronized(this) {
			return (VisitedDocEntry)hashcodeMap.get(hashcode);
		}
	}

	public VisitedDocEntry getEntry(long id) {
		synchronized(this) {
			return (VisitedDocEntry)table.get(new Long(id));
		}
	}

	public void deleteEntry(long id) {
		synchronized(this) {
			VisitedDocEntry entry = (VisitedDocEntry)table.remove(new Long(id));
			hashcodeMap.remove(entry.getHashcode());
		}
	}

	public void saveEntry(VisitedDocEntry entry) {
		synchronized(this) {
			table.put(new Long(entry.getId()), entry);
			hashcodeMap.put(entry.getHashcode(), entry);
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
