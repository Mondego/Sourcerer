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

/**
 * @author <a href="mailto:hahrot@yahoo.com">Huy A. Huynh</a>
 * 
 */
public class VisitedDomainEntry {

	private long id;

	private String domain;

	private Date lastVisitDate;

	private int visitedCount;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public Date getLastVisitDate() {
		return lastVisitDate;
	}

	public void setLastVisitDate(Date lastVisitDate) {
		this.lastVisitDate = lastVisitDate;
	}

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public int getVisitedCount() {
		return visitedCount;
	}

	public void setVisitedCount(int visitedCount) {
		this.visitedCount = visitedCount;
	}

	public VisitedDomainEntry(long id, String domain, Date lastVisitDate, int visitedCount) {
		super();
		this.id = id;
		this.domain = domain;
		this.lastVisitDate = lastVisitDate;
		this.visitedCount = visitedCount;
	}

	public boolean equals(Object obj) {
		if (obj instanceof VisitedDomainEntry)
			return (((VisitedDomainEntry)obj).id == this.id);
		else
			return super.equals(obj);
	}

	public int hashCode() {
		return (int) (id % Integer.MAX_VALUE);
	}
}
