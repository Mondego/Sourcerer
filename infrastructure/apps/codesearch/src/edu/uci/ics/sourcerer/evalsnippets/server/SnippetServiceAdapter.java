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
package edu.uci.ics.sourcerer.evalsnippets.server;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.uci.ics.sourcerer.db.adapter.SourcererDbAdapter;
import edu.uci.ics.sourcerer.scs.common.ResultProcessorNonGWT;
import edu.uci.ics.sourcerer.scs.common.SourcererSearchAdapter;
import edu.uci.ics.sourcerer.scs.common.client.HitFqnEntityId;
import edu.uci.ics.sourcerer.scs.common.client.HitsStat;
import edu.uci.ics.sourcerer.scs.common.client.SearchHeuristic;
import edu.uci.ics.sourcerer.scs.common.client.UsedFqn;

/**
 * @author <a href="bajracharya@gmail.com">Sushil Bajracharya</a>
 * @created Jan 15, 2010
 *
 */
public class SnippetServiceAdapter {
	
	public static Set<SearchHeuristic> schemes = new HashSet<SearchHeuristic>();
	
	static{
		// use three schemes
		schemes.add(SearchHeuristic.TEXT_FQN_SNAME);
		schemes.add(SearchHeuristic.TEXT_UJDOC_USEDFQN_FQN_SNAME);
		schemes.add(SearchHeuristic.TEXT_UJDOC_USEDFQN_FQN_JdkLibSimSNAME_SNAME);
		schemes.add(SearchHeuristic.TEXT_UJDOC_USEDFQN_FQN_JdkLibHdSimSNAME_SNAME);
		schemes.add(SearchHeuristic.TEXT_UJDOC_USEDFQN_FQN_JdkLibTcSimSNAME_SNAME);
		
//		schemes.add(SearchHeuristic.FQN_USEDFQN_SNAME);
//		schemes.add(SearchHeuristic.UJDOC_USEDFQN_FQN_SNAME);
//		schemes.add(SearchHeuristic.UJDOC_USEDFQN_FQN_JdkLibSimSNAME_SNAME);
//		schemes.add(SearchHeuristic.UJDOC_USEDFQN_FQN_JdkLibHdSimSNAME_SNAME);
//		schemes.add(SearchHeuristic.UJDOC_USEDFQN_FQN_JdkLibTcSimSNAME_SNAME);
		
	}
	
	public static Set<SearchHeuristic> getSchemes() {
		return schemes;
	}
	
	// <eid : <scheme: snippet}>
	public static Map<String, Map<String, String>> getSnippetsInBulk(String query,
			SourcererDbAdapter dba) {
		
		Map<String, Map<String, String>> eidHSnippets = new HashMap<String, Map<String, String>>();
		
		// for each heuristic return..
		for(SearchHeuristic h: schemes){
			Map<String,String[]> eidSnippets = SnippetServiceAdapter.getSnippetsForScheme(query, dba, h);
			// update <eid: <heuristic:snippet>> map
			for(String eid: eidSnippets.keySet()){
				if(eidHSnippets.containsKey(eid)){
					eidHSnippets.get(eid).put(h.toString(), eidSnippets.get(eid)[0]);
				} else {
					HashMap<String,String> hSnippet = new HashMap<String,String>();
					hSnippet.put(h.toString(), eidSnippets.get(eid)[0]);
					eidHSnippets.put(eid, hSnippet);
				}
			}
		}
			
		return eidHSnippets;
	}
	
	// <eid : <scheme: {snippet, rank}>>
	public static Map<String, Map<String, String[]>> getSnippetsForAllSchemesWithRank(String query,
			SourcererDbAdapter dba) {
		
		Map<String, Map<String, String[]>> eidHSnippets = new HashMap<String, Map<String, String[]>>();
		
		// for each heuristic return..
		for(SearchHeuristic h: schemes){
			Map<String,String[]> eidSnippets = SnippetServiceAdapter.getSnippetsForScheme(query, dba, h);
			
			// update <eid: <heuristic: {snippet,rank}>> map
			for(String eid: eidSnippets.keySet()){
				String[] snipRank = {eidSnippets.get(eid)[0], eidSnippets.get(eid)[1]};
				
				if(eidHSnippets.containsKey(eid)){
					
					eidHSnippets.get(eid).put(h.toString(), snipRank);
				} else {
					HashMap<String,String[]> hSnippet = new HashMap<String,String[]>();
					hSnippet.put(h.toString(), snipRank);
					eidHSnippets.put(eid, hSnippet);
				}
			}
		}
			
		return eidHSnippets;
	}
	
	// <eid : {snippet, rank}
	public static Map<String,String[]> getSnippetsForScheme(String query, SourcererDbAdapter dba, SearchHeuristic heuristic){
		int numResults2fetch = 10;
		int topK = 5;
		String queryResult = SourcererSearchAdapter.searchSCSServer(
				query, 
				0, 
				1, 
				heuristic);
		
		HitsStat stat = ResultProcessorNonGWT.getStatsFromHits(queryResult);
		
//		System.out.println("Query: " + query);
//		System.out.println("(" 
//				+ heuristic.toString()
//				+ ") # of results: " + stat.numOfResults);
		
		int numResults2print = Math.min(10,(int)stat.numOfResults);
		
		queryResult = SourcererSearchAdapter.searchSCSServer(
				query, 
				0, 
				Math.max((int)stat.numOfResults/10,numResults2fetch),
				//10,
				heuristic);
		
		List<HitFqnEntityId> hits = ResultProcessorNonGWT.getFqnEntityIdFromHits(queryResult);
	
		List<UsedFqn> usedTopLibApis = dba.getTopUsedEntitiesForJarEntityHit(hits);
	
		Map<String,String[]> eidSnippets = new HashMap<String,String[]>();
		
		for(int i=0; i<numResults2print ; i++){
			String snippet = dba.getSnippetForJarEntityHit(hits.get(i).entityId, usedTopLibApis, topK);
			snippet = SourcererSearchAdapter.getFormattedSnippet(snippet);
			String snippetAndRank[] = {snippet, ((int)i+1) + ""};
			eidSnippets.put(hits.get(i).entityId, snippetAndRank);
		}
		return eidSnippets;
	}

}
