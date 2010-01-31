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
package edu.uci.ics.sourcerer.scs.common.client;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * @author <a href="bajracharya@gmail.com">Sushil Bajracharya</a>
 * @created Jan 26, 2010
 *
 */
public class SearchResultsWithSnippets  implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 6090881961760084254L;
	
	public LinkedList<HitFqnEntityId> results;
	public LinkedList<UsedFqn> usedFqns;
	public HashMap<String, Integer> wordCounts;
	
	public HitsStat stat;
	
	public SearchResultsWithSnippets(){
		
	}
	
	public LinkedList<String> getHitEids(){
		LinkedList<String> eids = new LinkedList<String>();
		for(HitFqnEntityId hit: results){
			eids.add(hit.entityId);
		}
		
		return eids;
	}
	
	
	
}
