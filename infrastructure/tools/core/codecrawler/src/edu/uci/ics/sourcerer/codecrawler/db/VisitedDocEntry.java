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
package edu.uci.ics.sourcerer.codecrawler.db;

import java.util.Date;

import edu.uci.ics.sourcerer.codecrawler.md5hash.MD5Hash;

/**
 * @author <a href="mailto:hahrot@yahoo.com">Huy A. Huynh</a>
 */
public class VisitedDocEntry {

	private long id;

	private MD5Hash hashcode;

	private Date lastVisitDate;

	public MD5Hash getHashcode() {
		return hashcode;
	}

	public void setHashcode(MD5Hash hashcode) {
		this.hashcode = hashcode;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public VisitedDocEntry(long id, MD5Hash hashcode, Date lastVisitDate) {
		super();
		this.id = id;
		this.hashcode = hashcode;
		this.lastVisitDate = lastVisitDate;
	}

	public boolean equals(Object obj) {
		if (obj instanceof VisitedDocEntry)
			return (((VisitedDocEntry)obj).id == this.id);
		else
			return super.equals(obj);
	}

	public Date getLastVisitDate() {
		return lastVisitDate;
	}

	public void setLastVisitDate(Date lastVisitDate) {
		this.lastVisitDate = lastVisitDate;
	}

	public int hashCode() {
		return (int) (id % Integer.MAX_VALUE);
	}

}
