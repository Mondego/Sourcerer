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

import java.util.Set;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import edu.uci.ics.sourcerer.scs.common.client.SearchHeuristic;

/**
 * @author <a href="bajracharya@gmail.com">Sushil Bajracharya</a>
 * @created Sep 10, 2009
 */
@RemoteServiceRelativePath("scs")
public interface SourcererSearchService extends RemoteService {
	
	String searchSCSServer(String query);
	
	String searchSCSServer(String query, long start, int rows);
	
	String searchSCSServer(String query, long start, int rows, SearchHeuristic heuristic);
	
	String getEntityCode(String entityId);
	
	String getEntityCode(String entityId, Set<String> qterms,
			Set<String> usedEntities);
	
	String getUsageAsFacets(String query, SearchHeuristic heuristic);
	
	String searchMltViaJdkUsage(String entityId);
	String searchMltViaLibUsage(String entityId);
	String searchMltViaLocalUsage(String entityId);
	

}
