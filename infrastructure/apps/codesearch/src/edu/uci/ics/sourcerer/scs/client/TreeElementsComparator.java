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
package edu.uci.ics.sourcerer.scs.client;

import java.util.Comparator;
import java.util.Map;

/**
 * @author <a href="bajracharya@gmail.com">Sushil Bajracharya</a>
 * @created Jul 31, 2009
 */
public class TreeElementsComparator implements Comparator {

	Map baseMap;

	public TreeElementsComparator(Map baseMap) {
		this.baseMap = baseMap;
	}

	public int compare(Object o1, Object o2) {

		if (!baseMap.containsKey(o1) || !baseMap.containsKey(o2)) {
			return 0;
		}

		if (((TreeElements) baseMap.get(o1)).count > ((TreeElements) baseMap
				.get(o2)).count)
			return -1;
		else if (((TreeElements) baseMap.get(o1)).count < ((TreeElements) baseMap
				.get(o2)).count)
			return 1;
		else
			return 0;
	}

}
