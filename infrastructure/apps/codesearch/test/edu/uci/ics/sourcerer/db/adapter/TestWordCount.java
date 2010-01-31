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
package edu.uci.ics.sourcerer.db.adapter;

import edu.uci.ics.sourcerer.scs.common.client.SearchHeuristic;
import edu.uci.ics.sourcerer.scs.common.client.SearchResultsWithSnippets;
import edu.uci.ics.sourcerer.scs.server.SourcererDBServiceImpl;

/**
 * @author <a href="bajracharya@gmail.com">Sushil Bajracharya</a>
 * @created Jan 27, 2010
 * 
 */
public class TestWordCount extends TestSourcererDbAdapterBase {

	public void testWC() {
		String query = "write to default error log";
		long start = 0;
		int rows = 10;
		SearchHeuristic heuristic = SearchHeuristic.TEXT_UJDOC_USEDFQN_FQN_JdkLibTcSimSNAME_SNAME;
		SearchResultsWithSnippets r = SourcererDBServiceImpl
				.getSearchResultsWithSnippets(query, start, rows, dba,
						heuristic, null);

		for (String word : r.wordCounts.keySet()) {
			System.out.println(word + ":" + r.wordCounts.get(word));
		}
	}
}
