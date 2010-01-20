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

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.uci.ics.sourcerer.db.adapter.client.Entity;
import edu.uci.ics.sourcerer.db.adapter.client.Relation;
import edu.uci.ics.sourcerer.scs.common.client.EntityCategory;

/**
 * @author <a href="bajracharya@gmail.com">Sushil Bajracharya</a>
 * @created Jul 22, 2009
 */
public class ERTables implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -1919212587918750257L;
	public LinkedList<Entity> entities;
	public LinkedList<Relation> relations;
	
	public HashSet<Long> getUsersInHits(Entity usedEntity){
		HashSet<Long> users = new HashSet<Long>();
		
		for(Relation r: relations){
			if (r.lhsEntityCategory.equals(EntityCategory.LOCAL)
					&& r.reid == usedEntity.entityId
					&& r.rhsEntityCategory.equals(usedEntity.category)){
				users.add(new Long(r.leid));
			}
		}
		
		return users;
	}
	
	public HashSet<Long> getUsersInHits(String usedEntityId, String category){
		
		Long eid = Long.parseLong(usedEntityId);
		EntityCategory cat = EntityCategory.valueOf(category);
		
		HashSet<Long> users = new HashSet<Long>();
		
		if (eid==null || cat==null){return users;}
		
		
		
		for(Relation r: relations){
			if (r.lhsEntityCategory.equals(EntityCategory.LOCAL)
					&& r.reid == eid.longValue()
					&& r.rhsEntityCategory.equals(cat)){
				users.add(new Long(r.leid));
			}
		}
		
		return users;
	}
	
//	public HashSet<String> getEntiyIdsUsingFqns(LinkedList<String> libFqns, LinkedList<String> jdkFqns){
//		HashSet<String> eids = new HashSet<String>();
//		
//		if (libFqns != null) {
//			for (String libFqn : libFqns) {
//				for (String eid : libFqnToEntityIdsMap.get(libFqn)) {
//					eids.add(eid);
//				}
//			}
//		}
//
//		if (jdkFqns != null) {
//
//			for (String jdkFqn : jdkFqns) {
//				for (String eid : jdkFqnToEntityIdsMap.get(jdkFqn)) {
//					eids.add(eid);
//				}
//			}
//		}
//		
//		return eids;
//	}
	
	
	public HashSet<String> getEntiyIdsUsingFqns(HashSet<String> libFqns, HashSet<String> jdkFqns){
		HashSet<String> eids = new HashSet<String>();
		
		if (libFqns != null) {
			for (String libFqn : libFqns) {
				for (String eid : libFqnToEntityIdsMap.get(libFqn)) {
					eids.add(eid);
				}
			}
		}

		if (jdkFqns != null) {

			for (String jdkFqn : jdkFqns) {
				for (String eid : jdkFqnToEntityIdsMap.get(jdkFqn)) {
					eids.add(eid);
				}
			}
		}
		
		return eids;
	}
	
	public LinkedList<String> getLibFqnsUsedByEntityId(String eid){
//		HashSet<String> libFqns = new HashSet<String>();
//		for(String fqn: entityIdToLibFqnsMap.get(eid)){
//			libFqns.add(fqn);
//		}
//		return libFqns;
		
		return entityIdToLibFqnsMap.get(eid);
	}
	
	
	public LinkedList<String> getJdkFqnsUsedByEntityId(String eid){
//		HashSet<String> jdkFqns = new HashSet<String>();
//		for(String fqn: entityIdToJdkFqnsMap.get(eid)){
//			jdkFqns.add(fqn);
//		}
//		return jdkFqns;
		
		return entityIdToJdkFqnsMap.get(eid);
	}
	
	// indices
	HashMap<String,String> jdkEidFqnMap = new HashMap<String, String>();
	HashMap<String,String> libEidFqnMap = new HashMap<String, String>();
	//HashMap<String, List<String>> libFqnEidsMap = new HashMap<String, List<String>>();
	//HashMap<String, List<String>> jdkFqnEidsMap = new HashMap<String, List<String>>();
	
	HashMap<String, LinkedList<String>> libFqnToEntityIdsMap = new HashMap<String, LinkedList<String>>();
	HashMap<String, LinkedList<String>> jdkFqnToEntityIdsMap = new HashMap<String, LinkedList<String>>();
	HashMap<String, LinkedList<String>> entityIdToLibFqnsMap = new HashMap<String, LinkedList<String>>();
	HashMap<String, LinkedList<String>> entityIdToJdkFqnsMap = new HashMap<String, LinkedList<String>>();
	
	public void buildIndices(){
		for(Entity e: entities){
			
			//System.err.println(e.fqn);
			
			if(e.category==EntityCategory.JDK){
				
				jdkEidFqnMap.put(e.entityId + "", e.fqn);
				
				
//				if(jdkFqnEidsMap.containsKey(e.fqn)){
//					jdkFqnEidsMap.get(e.fqn).add(e.entityId+"");
//				} else {
//					LinkedList<String> _list = new LinkedList<String>();
//					_list.add(e.entityId+"");
//					jdkFqnEidsMap.put(e.entityId+"", _list);
//				}
			}
			
			if(e.category==EntityCategory.LIB){
				
				libEidFqnMap.put(e.entityId + "", e.fqn);
				
//				if(libFqnEidsMap.containsKey(e.fqn)){
//					libFqnEidsMap.get(e.fqn).add(e.entityId+"");
//				} else {
//					LinkedList<String> _list = new LinkedList<String>();
//					_list.add(e.entityId+"");
//					libFqnEidsMap.put(e.entityId+"", _list);
//				}
			}
			
		}
		
		for(Relation r: relations){
			if(r.rhsEntityCategory == EntityCategory.JDK){
				String _jdkFqn = jdkEidFqnMap.get(r.reid+"");
				
				assert _jdkFqn != null;
				
				if (r.lhsEntityCategory == EntityCategory.LOCAL) {
					if (jdkFqnToEntityIdsMap.containsKey(_jdkFqn)) {
						jdkFqnToEntityIdsMap.get(_jdkFqn).add(r.leid + "");
					} else {
						LinkedList<String> _list = new LinkedList<String>();
						_list.add(r.leid + "");
						jdkFqnToEntityIdsMap.put(_jdkFqn, _list);
					}

					if (entityIdToJdkFqnsMap.containsKey(r.leid+"")) {
						entityIdToJdkFqnsMap.get(r.leid+"").add(_jdkFqn);
					} else {
						LinkedList<String> _list = new LinkedList<String>();
						_list.add(_jdkFqn);
						entityIdToJdkFqnsMap.put(r.leid+"", _list);
					}
				}
			} 
			
			if(r.rhsEntityCategory == EntityCategory.LIB){
				String _libFqn = libEidFqnMap.get(r.reid+"");
				
				assert _libFqn != null;
				
				if (r.lhsEntityCategory == EntityCategory.LOCAL) {
					if (libFqnToEntityIdsMap.containsKey(_libFqn)) {
						libFqnToEntityIdsMap.get(_libFqn).add(r.leid + "");
					} else {
						LinkedList<String> _list = new LinkedList<String>();
						_list.add(r.leid + "");
						libFqnToEntityIdsMap.put(_libFqn, _list);
					}

					if (entityIdToLibFqnsMap.containsKey(r.leid+"")) {
						entityIdToLibFqnsMap.get(r.leid+"").add(_libFqn);
					} else {
						LinkedList<String> _list = new LinkedList<String>();
						_list.add(_libFqn);
						entityIdToLibFqnsMap.put(r.leid+"", _list);
					}
				}
			} 
			
		}
		
	}
	
	
	
}
