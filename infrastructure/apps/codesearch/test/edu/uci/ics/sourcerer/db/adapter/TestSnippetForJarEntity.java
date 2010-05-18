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

import java.util.List;

import com.mysql.jdbc.jdbc2.optional.SuspendableXAConnection;

import edu.uci.ics.sourcerer.scs.common.ResultProcessorNonGWT;
import edu.uci.ics.sourcerer.scs.common.SourcererSearchAdapter;
import edu.uci.ics.sourcerer.scs.common.client.EntityCategory;
import edu.uci.ics.sourcerer.scs.common.client.EntityType;
import edu.uci.ics.sourcerer.scs.common.client.HitFqnEntityId;
import edu.uci.ics.sourcerer.scs.common.client.HitsStat;
import edu.uci.ics.sourcerer.scs.common.client.SearchHeuristic;
import edu.uci.ics.sourcerer.scs.common.client.UsedFqn;

/**
 * @author <a href="bajracharya@gmail.com">Sushil Bajracharya</a>
 * @created Jan 5, 2010
 *
 */
public class TestSnippetForJarEntity extends TestSourcererDbAdapterBase {
	
	public void testGetSnippet(){
		String query_raw = "jdkLib_use_javadoc:(get method signature from ast node)" +
				" OR "
				+ "sim_sname_contents:(get method signature from ast node)";
		
		
		String query = 
			
			//"send file socket";
		
			//"eclipse open url in html browser";
			"eclipse get icon image";
		//	 "eclipse update status line text";
			//; "copy data from clipboard";
			// "eclipse mouse hover track";
			// "copy paste data from clipboard";
		SearchHeuristic heuristic = 
		//	SearchHeuristic.FQN_USEDFQN_JdkLibSimSNAME_SNAME;
		//	SearchHeuristic.TEXT_UJDOC_USEDFQN_FQN_JdkLibSimSNAME_SNAME;
		//	SearchHeuristic.TEXT_UJDOC_USEDFQN_FQN_JdkLibHdSimSNAME_SNAME;
		 SearchHeuristic.TEXT_UJDOC_USEDFQN_FQN_JdkLibTcSimSNAME_SNAME;
		// SearchHeuristic.TEXT_UJDOC_USEDFQN_FQN_SNAME;
			 //SearchHeuristic.TEXT_USEDFQN_FQN_SNAME;
		// SearchHeuristic.FQN_USEDFQN_JdkLibSimSNAME_SNAME;
		//	 SearchHeuristic.TEXT_FQN_SNAME;
		//	 SearchHeuristic.TEXT_USEDFQN_FQN_JdkLibSimSNAME_SNAME;
		//	SearchHeuristic.TEXT_USEDFQN_FQN_JdkLibHdSimSNAME_SNAME;
		 //  SearchHeuristic.TEXT_USEDFQN_FQN_JdkLibTcSimSNAME_SNAME;
		
		int numResults2fetch = 10;
	
		int topK = 5;
		
		String queryResult = SourcererSearchAdapter.searchSCSServer(
				query, 
				0, 
				1, 
				heuristic);
		
		HitsStat stat = ResultProcessorNonGWT.getStatsFromHits(queryResult);
		System.out.println("# of results: " + stat.numOfResults);
		
		int numResults2print = Math.min(10,(int)stat.numOfResults);
		
		queryResult = SourcererSearchAdapter.searchSCSServer(
				query, 
				0, 
				Math.max((int)stat.numOfResults/10,numResults2fetch),
				//10,
				heuristic);
		
		List<HitFqnEntityId> hits = ResultProcessorNonGWT.getFqnEntityIdFromHits(queryResult);
		
//{{ get usage from facets
//		String usedApisHits = SourcererSearchAdapter.getUsageAsFacets(
//				query, 
//				heuristic);
//		
//		List<HitFqnEntityId> apiHits = ResultProcessorNonGWT.getUsedApisFromFacetedHits(usedApisHits, EntityCategory.LIB);
//		List<UsedFqn> usedTopLibApis = dba.fillUsedFqnsDetails(apiHits, EntityCategory.LIB);
//}}
		
//{{ get usage from DB		
		List<UsedFqn> usedTopLibApis = dba.getTopUsedEntitiesForJarEntityHit(hits);
//}}		
		System.out.println("==== Top Apis ===========================================");
		
		System.out.println("== INTERFACE ==");
		printTopApi(usedTopLibApis, EntityType.INTERFACE, topK);
		System.out.println("== CLASS ==");
		printTopApi(usedTopLibApis, EntityType.CLASS, topK);
		System.out.println("== METHOD ==");
		printTopApi(usedTopLibApis, EntityType.METHOD, topK);
		System.out.println("== CONSTRUCTOR ==");
		printTopApi(usedTopLibApis, EntityType.CONSTRUCTOR, topK);
		System.out.println("== FIELD ==");
		printTopApi(usedTopLibApis, EntityType.FIELD, topK);
		System.out.println("== OTHER ==");
		printTopApi(usedTopLibApis, EntityType.OTHER, topK);
		
		
		
		System.out.println("==== Hits ===========================================");
		
		for(int i=0; i<numResults2print ; i++){
			String snippet = dba.getSnippetForJarEntityHit(hits.get(i).entityId, usedTopLibApis, topK);
			System.out.println(hits.get(i).entityId);
			System.out.println(hits.get(i).fqn);
			System.out.println(snippet);
			System.out.println("=======================================================================================");
		}
		
	}
	
	public void printTopApi(List<UsedFqn> usedTopLibApis, EntityType etype, int topK){
		int count = 0;
		for(UsedFqn ufqn: usedTopLibApis){
			if (ufqn.getType() == etype){
			System.out.println(
					ufqn.getUseCount() + "\t"
					+ ufqn.getType().toString() + "\t"
					+ ufqn.getFqn());
			
			count++;
			}
			if(count>topK*2) break;
		}	
	}
}
