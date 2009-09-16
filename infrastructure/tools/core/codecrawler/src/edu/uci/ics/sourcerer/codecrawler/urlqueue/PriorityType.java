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
package edu.uci.ics.sourcerer.codecrawler.urlqueue;

import java.util.Date;


/**
 * @author <a href="mailto:hahrot@yahoo.com">Huy A. Huynh</a>
 * 
 */
public class PriorityType implements Comparable<PriorityType> {

	private Date hostLastVisit;
	private String hostName;
	private Date urlLastVisit;
	private String url;

	/**
	 * @param obj
	 * @return Negative if this should be sorted <i>before</i> the other object.
	 * In another word, this compares the order. Smaller order numbers go first.
	 */
	public int compareTo(PriorityType that) {
		int result;
		if ((result = hostLastVisit.compareTo(that.hostLastVisit)) == 0)
			//if ((result = hostName.compareToIgnoreCase(that.hostName)) == 0)
				if ((result = urlLastVisit.compareTo(that.urlLastVisit)) == 0)
					if ((result = url.compareToIgnoreCase(that.url)) == 0);
		return result;
	}

	public Date getHostLastVisit() {
		return hostLastVisit;
	}

	public void setHostLastVisit(Date hostLastVisit) {
		this.hostLastVisit = hostLastVisit;
	}

	public String getHostName() {
		return hostName;
	}

	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public Date getUrlLastVisit() {
		return urlLastVisit;
	}

	public void setUrlLastVisit(Date urlLastVisit) {
		this.urlLastVisit = urlLastVisit;
	}

	public PriorityType(Date hostLastVisit, String hostName, Date urlLastVisit, String url) {
		super();
		this.hostLastVisit = hostLastVisit;
		this.hostName = hostName;
		this.urlLastVisit = urlLastVisit;
		this.url = url;
	}

	public PriorityType() {
		//FIXME remove this constructor. Test purpose only.
	}

}
