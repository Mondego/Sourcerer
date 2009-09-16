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

import java.util.Comparator;

import edu.uci.ics.sourcerer.codecrawler.db.UrlQueueEntry;

/**
 * @author <a href="mailto:hahrot@yahoo.com">Huy A. Huynh</a>
 * 
 */
class Prioritizer implements Comparator<UrlQueueEntry> {

	/**
	 * Parameters are of <code>UrlQueueEntry</code> type.
	 */
	public int compare(UrlQueueEntry entry0, UrlQueueEntry entry1) {
		try {
			if ((entry0 != null) && (entry1 != null))
				return entry0.getPriority().compareTo(entry1.getPriority());
			else
				return 0;
		} catch (Exception o) {
			return 0;
		}
	}

}