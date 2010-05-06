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


import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;



import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import edu.uci.ics.sourcerer.db.adapter.JdbcDataSource;
import edu.uci.ics.sourcerer.db.adapter.SourcererDbAdapter;
import edu.uci.ics.sourcerer.scs.client.ERTables;
import edu.uci.ics.sourcerer.scs.client.SourcererDBService;
import edu.uci.ics.sourcerer.scs.common.ResultProcessorNonGWT;
import edu.uci.ics.sourcerer.scs.common.SourcererSearchAdapter;
import edu.uci.ics.sourcerer.scs.common.client.EntityCategory;
import edu.uci.ics.sourcerer.scs.common.client.EntityType;
import edu.uci.ics.sourcerer.scs.common.client.HitFqnEntityId;
import edu.uci.ics.sourcerer.scs.common.client.HitsStat;
import edu.uci.ics.sourcerer.scs.common.client.SearchHeuristic;
import edu.uci.ics.sourcerer.scs.common.client.SearchResultsWithSnippets;
import edu.uci.ics.sourcerer.scs.common.client.UsedFqn;

/**
 * @author <a href="bajracharya@gmail.com">Sushil Bajracharya</a>
 * @created Jul 22, 2009
 *
 */
public class SourcererDBServiceImpl extends RemoteServiceServlet implements
	SourcererDBService {
	
	JdbcDataSource ds = new JdbcDataSource();
	SourcererDbAdapter dba = new SourcererDbAdapter();
	
	public void init(){
		
		String dbUrl = "jdbc:mysql://tagus.ics.uci.edu:3306/sourcerer_eclipse";//getInitParameter("db-url");
		String dbUser = "sourcerer";//getInitParameter("db-user");
		String dbPassword = "sourcerer4us";//getInitParameter("db-password");
		
		Properties p = new Properties();
	    p.put("driver", "com.mysql.jdbc.Driver");
	    p.put("url", dbUrl);
	    p.put("user", dbUser);
	    p.put("password", dbPassword);
	    ds.init(p);
	    
	    dba.setDataSource(ds);
	}

	

	public UsedFqn fillUsedFqnDetails(HitFqnEntityId fqn, EntityCategory cat) {
		return dba.fillUsedFqnDetails(fqn, cat);
	}
	
	public List<UsedFqn> fillUsedFqnDetails(List<HitFqnEntityId> fqns, EntityCategory cat) {
		return dba.fillUsedFqnsDetails(fqns, cat);
	}
	
	public SearchResultsWithSnippets getSearchResultsWithSnippets(String query, long start, int rows, SearchHeuristic heuristic){
		return SourcererDBServiceImpl.getSearchResultsWithSnippets(query, start, rows, dba, heuristic, null);
	}
	
	public SearchResultsWithSnippets getSearchResultsWithSnippets(String query,
			long from, int rows, SearchHeuristic heuristic,
			HashSet<String> filterFqns) {
		return SourcererDBServiceImpl.getSearchResultsWithSnippets(query, from, rows, dba, heuristic, filterFqns);
	}
	
	
	// this should belong to an adapter
	
	
	public static SearchResultsWithSnippets getSearchResultsWithSnippets(String query, long start, int rows, 
			SourcererDbAdapter dba, SearchHeuristic heuristic, HashSet<String> fqnFilters){
		
		String queryResult = SourcererSearchAdapter.searchSCSServer(
				query, 
				start, 
				rows, 
				heuristic,
				fqnFilters);
		List<HitFqnEntityId> hits = ResultProcessorNonGWT.getFqnEntityIdFromHits(queryResult);
		
		HitsStat stat = ResultProcessorNonGWT.getStatsFromHits(queryResult);
		
		int numResults2fetch = 
			Math.min(Math.max((int)stat.numOfResults/10, 30), 100);
		int topK = 4;
		int minUsageNeededToShowAPI = 2;
		
		String queryResultTopHits = SourcererSearchAdapter.searchSCSServer(
				query, 
				0, 
			    numResults2fetch,
				heuristic,
				fqnFilters);
		
		List<HitFqnEntityId> hitsForFqns = ResultProcessorNonGWT.getFqnEntityIdFromHits(queryResultTopHits);
	
		List<UsedFqn> usedTopLibApis = dba.getTopUsedEntitiesForJarEntityHit(hitsForFqns);
	
		for(HitFqnEntityId hit: hits){
			List<String> snippetParts = dba.getSnippetsForJarEntityHit(hit.entityId, usedTopLibApis, topK);
			
			List<String> formattedSnippetParts = new LinkedList<String>();
			for(String s: snippetParts){
				formattedSnippetParts.add(SourcererSearchAdapter.getFormattedSnippet(s));
			}
			
			hit.snippetParts = formattedSnippetParts;
		}
		
		SearchResultsWithSnippets result = new SearchResultsWithSnippets();
		result.results = (LinkedList<HitFqnEntityId>) hits;
		
		LinkedList<UsedFqn> usedTopLibApisToShow = new LinkedList<UsedFqn>();
		for(UsedFqn fqn: usedTopLibApis){
			if(fqn.getUseCount()>=minUsageNeededToShowAPI){
				usedTopLibApisToShow.add(fqn);
			}
		}
		
		result.usedFqns = (LinkedList<UsedFqn>) usedTopLibApisToShow;
		
		result.stat = stat;
		
		result.wordCounts = buildWordCount(hitsForFqns, usedTopLibApisToShow, dba, numResults2fetch);
		
		return result;
		
		
	}

	
	
	private static HashMap<String, Integer> buildWordCount(
			List<HitFqnEntityId> hitsForFqns,
			LinkedList<UsedFqn> usedTopLibApisToShow,
			SourcererDbAdapter dba,
			int numResultToFetch) {
		
		HashMap<String, Integer> _wc = new HashMap<String, Integer>();
		
		for(HitFqnEntityId _hit: hitsForFqns){
			String[] terms = JavaTermExtractor.extractShortNameFragments(_hit.fqn);
			putTermsInMap(terms, _wc);
			
			// TODO get similar entity fqns from search server
//			Set<String> _simFqns = dba.getSimilartEntityFqns(_hit.entityId);
//			for(String s: _simFqns){
//				putTermsInMap(JavaTermExtractor.extractShortNameFragments(s), _wc);
//			}
			
		}
		
		for(UsedFqn _used : usedTopLibApisToShow){
			
			if(!(_used.getType()==EntityType.METHOD 
					||_used.getType()==EntityType.CLASS
					||_used.getType()==EntityType.INTERFACE))
				continue;
			
			if(_used.getUseCount()<3)
				continue;
			
			String[] terms = JavaTermExtractor.extractShortNameFragments(_used.getFqn());
			int multiplier = _used.getUseCount(); ///3;
			///if (multiplier==0) multiplier = 1;
			putTermsInMap(terms, _wc, multiplier);
		}
		
		
		
		return _wc;
		
		
	}

	private static void putTermsInMap(String[] terms,
			HashMap<String, Integer> wc,
			int multiplier) {
		
		
		for(String s: terms){
			
			if(s.length()==1) continue;
			
			if(wc == null){
				wc = new HashMap<String, Integer>();
				wc.put(s, multiplier);
			} else {
				int oldCount = 0;
				if(wc.containsKey(s)){
					oldCount = wc.get(s).intValue();
				}
				wc.put(s, oldCount + multiplier);
			}
		}
	}
	
	private static void putTermsInMap(String[] terms,
			HashMap<String, Integer> wc) {
		
		putTermsInMap(terms, wc, 1);
		
	}

	
	
}