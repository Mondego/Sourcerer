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
package edu.uci.ics.sourcerer.scs.server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import java.net.URLEncoder;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;

import edu.uci.ics.sourcerer.scs.client.SourcererSearchService;
import edu.uci.ics.sourcerer.scs.common.JavaToHtml;

import edu.uci.ics.sourcerer.scs.common.SourcererSearchAdapter;
import edu.uci.ics.sourcerer.scs.common.client.SearchHeuristic;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/**
 * 
 * @author <a href="bajracharya@gmail.com">Sushil Bajracharya</a>
 * @created Sep 10, 2009
 *
 */
@SuppressWarnings("serial")
public class SourcererSearchServiceImpl extends RemoteServiceServlet implements
		SourcererSearchService {
	
	public String searchSCSServer(String query) {
		return SourcererSearchAdapter.searchSCSServer(query);
	}
	
	public String getEntityCode(String entityId, Set<String> qterms,
			Set<String> usedEntities){
		return SourcererSearchAdapter.getEntityCode(entityId, qterms, usedEntities);
	}
	
	public String getEntityCode(String entityId){
		return SourcererSearchAdapter.getEntityCodeRaw(entityId);
	}

	public String searchSCSServer(String query, long start, int rows) {
		return SourcererSearchAdapter.searchSCSServer(query, start, rows);
	}


	public String searchSCSServer(String query, long start, int rows,
			SearchHeuristic heuristic) {
		return SourcererSearchAdapter.searchSCSServer(query, start, rows, heuristic);
	}


	public String searchMltViaJdkUsage(String entityId) {
		return SourcererSearchAdapter.searchMltViaJdkUsage(entityId);
	}


	public String searchMltViaLibUsage(String entityId) {
		return SourcererSearchAdapter.searchMltViaLibUsage(entityId);
	}


	public String searchMltViaLocalUsage(String entityId) {
		return SourcererSearchAdapter.searchMltViaLocalUsage(entityId);
	}
	
	public String getUsageAsFacets(String query, SearchHeuristic heuristic){
		return SourcererSearchAdapter.getUsageAsFacets(query, heuristic);
	}

}

