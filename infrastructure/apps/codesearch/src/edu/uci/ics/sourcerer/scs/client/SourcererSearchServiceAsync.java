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

import com.google.gwt.user.client.rpc.AsyncCallback;

import edu.uci.ics.sourcerer.scs.common.client.SearchHeuristic;

import java.util.Set;

/**
 * @author <a href="bajracharya@gmail.com">Sushil Bajracharya</a>
 * @created Sep 10, 2009
 */
public interface SourcererSearchServiceAsync {
	
	void searchSCSServer(String query, AsyncCallback<String> callback);
	
	void searchSCSServer(String query, long start, int rows,
			AsyncCallback<String> callback);
	
	void searchSCSServer(String query, long start, int rows, 
			SearchHeuristic heuristic, AsyncCallback<String> callback);
	
	void getEntityCode(String entityId, AsyncCallback<String> callback);
	
	void getEntityCode(String entityId, Set<String> qterms,
			Set<String> usedEntities, AsyncCallback<String> callback);
	
	void getUsageAsFacets(String query, SearchHeuristic heuristic, AsyncCallback<String> callback);
	
	void searchMltViaJdkUsage(String entityId, AsyncCallback<String> callback);
	void searchMltViaLibUsage(String entityId, AsyncCallback<String> callback);
	void searchMltViaLocalUsage(String entityId, AsyncCallback<String> callback);

}
