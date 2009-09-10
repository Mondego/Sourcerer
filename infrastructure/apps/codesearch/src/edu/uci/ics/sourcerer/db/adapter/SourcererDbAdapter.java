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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.uci.ics.sourcerer.db.adapter.client.Entity;
import edu.uci.ics.sourcerer.db.adapter.client.EntityCategory;
import edu.uci.ics.sourcerer.db.adapter.client.EntityType;
import edu.uci.ics.sourcerer.db.adapter.client.Relation;
import edu.uci.ics.sourcerer.db.adapter.client.RelationType;
import edu.uci.ics.sourcerer.scs.client.ERTables;

/**
 * @author <a href="bajracharya@gmail.com">Sushil Bajracharya</a>
 * @created Jul 20, 2009
 */

public class SourcererDbAdapter {

	private JdbcDataSource dataSource;

	private HashSet<Long> entitiesInHit;
	private HashSet<RelationType> useRelations;
	
	private Map<Long, Entity> usedLocalEntities;
	private Map<Long, Entity> usedJdkEntities;
	private Map<Long, Entity> usedLibEntities;
	
	private LinkedList<Relation> relations;

	private HashMap<Long, Integer> localEntityUseCount = new HashMap<Long, Integer>();
	private HashMap<Long, Integer> jdkEntityUseCount = new HashMap<Long, Integer>();
	private HashMap<Long, Integer> libEntityUseCount = new HashMap<Long, Integer>();
	

	
	private LinkedList<Entity> getUsedEntities(){
		LinkedList<Entity> entities = new LinkedList<Entity>();
		
		for(Entity e: usedLocalEntities.values()){
			e.parentId = getParent(e)==null?0:getParent(e).entityId;
			e.useCount = getUseCount(e);
			entities.add(e);
		}
		
		for(Entity e: usedLibEntities.values()){
			e.parentId = getParent(e)==null?0:getParent(e).entityId;
			e.useCount = getUseCount(e);
			entities.add(e);
		}
		
		for(Entity e: usedJdkEntities.values()){
			e.parentId = getParent(e)==null?0:getParent(e).entityId;
			e.useCount = getUseCount(e);
			entities.add(e);
		}
		
		return entities;
	}
	
//	public List<Relation> getRelations(){
//		return this.relations;
//	}
//	
//	public List<Entity> getUsedLocalEntities(){
//		return listFromMap(usedLocalEntities);
//	}
//	
//	public List<Entity> getUsedJdkEntities(){
//		return listFromMap(usedJdkEntities);
//	}
//	
//	public List<Entity> getUsedLibEntities(){
//		return listFromMap(usedLibEntities);
//	}
	
	private int getUseCount(Entity e){
		
		Long key = new Long(e.entityId);
		Integer value = null;
		
		if(e.category.equals(EntityCategory.JDK)){
			value = jdkEntityUseCount.get(key);
		} else if (e.category.equals(EntityCategory.LIB)){
			value = libEntityUseCount.get(key);
		} else if (e.category.equals(EntityCategory.LOCAL)){
			value = localEntityUseCount.get(key);
		} else {
			return 0;
		}
		
		if (value == null)
			return 0;
		else 
			return value.intValue();
	}
	
	/**
	 * 
	 * @param e
	 * @return only searches for the parent of this entity e in the
	 * 			list of entities that are used by the entities in the hits
	 */
	private Entity getParent(Entity e) {
		Entity parent = null;

		for (Relation r : relations) {
			if (r.leid == e.entityId && r.lhsEntityCategory.equals(e.category)
					&& r.type.equals(RelationType.INSIDE)) {

				if (r.rhsEntityCategory.equals(EntityCategory.LOCAL)) {
					parent = usedLocalEntities.get(new Long(r.reid));
				} else if (r.rhsEntityCategory.equals(EntityCategory.LIB)) {
					parent = usedLibEntities.get(new Long(r.reid));
				} else if (r.rhsEntityCategory.equals(EntityCategory.JDK)) {
					parent = usedJdkEntities.get(new Long(r.reid));
				}

			}
		}

		return parent;
	}
	
	
	
	
	// initialization
	
	public void setDataSource(JdbcDataSource ds){
		this.dataSource = ds;
	}
	
	public ERTables buildDbForHitEntities(List<String> entityIds) {

		entitiesInHit = new HashSet<Long>(entityIds.size());
		for(String e: entityIds){
			try{
				Long eid = Long.parseLong(e);
				if(eid.longValue()>0){
					entitiesInHit.add(eid);
				}
			} catch(NumberFormatException nfe){
			}
			
		}
		
		initTables();

		fillUseRelationsWithHitsAsSource(entityIds);

		loadUsedEntities();

		// get inside relations
		fillLocalEntityRelations();
		fillJdkEntityRelations();
		fillLibEntityRelations();
		
		ERTables erTables = new ERTables();
		erTables.entities = getUsedEntities();
		erTables.relations = relations;
		return erTables;

	}
	
	
//	private List<Entity> listFromMap(Map<Long,Entity> m){
//		List<Entity> _localEntities = new LinkedList<Entity>();
//		for(Entity e: m.values()){
//			_localEntities.add(e);
//		}
//		return _localEntities;
//	}

	private void initTables() {
		
		useRelations = new HashSet<RelationType>();
		useRelations.add(RelationType.EXTENDS);
		useRelations.add(RelationType.IMPLEMENTS);
		useRelations.add(RelationType.CALLS);
		useRelations.add(RelationType.RETURNS);
		useRelations.add(RelationType.HOLDS);
		useRelations.add(RelationType.USES);
		
		if (usedLocalEntities == null)
			usedLocalEntities = new HashMap<Long, Entity>();
		else
			usedLocalEntities.clear();
		
		if (usedJdkEntities == null)
			usedJdkEntities = new HashMap<Long, Entity>();
		else
			usedJdkEntities.clear();
		
		if (usedLibEntities == null)
			usedLibEntities = new HashMap<Long, Entity>();
		else
			usedLibEntities.clear();

		if (relations == null)
			relations = new LinkedList<Relation>();
		else
			relations.clear();
		
		localEntityUseCount.clear();
		jdkEntityUseCount.clear();
		libEntityUseCount.clear();
	}

	private void loadUsedEntities() {

		updateUseCounts();

		fillLocalEntities(new LinkedList<Long>(localEntityUseCount.keySet()));
		fillJdkEntities(new LinkedList<Long>(jdkEntityUseCount.keySet()));
		fillLibEntities(new LinkedList<Long>(libEntityUseCount.keySet()));

	}

	private void updateUseCounts() {
		for (Relation r : relations) {

			if((! entitiesInHit.contains(new Long(r.leid))) 
					|| (! useRelations.contains(r.type))) {
				continue;
			}
					
			
			Long targetEntityId = new Long(r.reid);

			if (r.rhsEntityCategory.equals(EntityCategory.LOCAL)) {
				updateUseCountForEntityCategory(localEntityUseCount,
						targetEntityId);
			} else if (r.rhsEntityCategory.equals(EntityCategory.JDK)) {
				updateUseCountForEntityCategory(jdkEntityUseCount,
						targetEntityId);
			} else if (r.rhsEntityCategory.equals(EntityCategory.LIB)) {
				updateUseCountForEntityCategory(libEntityUseCount,
						targetEntityId);
			}
		}
	}

	private void updateUseCountForEntityCategory(
			HashMap<Long, Integer> entityCountMap, Long targetEntityId) {
		Integer newValue = new Integer(1);
		if (entityCountMap.containsKey(targetEntityId)) {
			newValue = new Integer(entityCountMap.get(targetEntityId)
					.intValue() + 1);
			entityCountMap.remove(targetEntityId);
		}
		entityCountMap.put(targetEntityId, newValue);
	}

	private void fillUseRelationsWithHitsAsSource(List<String> entityIds) {
		String _sql = "select lhs_eid, rhs_eid, rhs_leid, rhs_jeid, relation_type from relations where lhs_eid in("
				+ delimitStringListWithComma(entityIds)
				+ ") "
				+ " AND ("
				+ " relation_type='EXTENDS' OR "
				+ " relation_type='IMPLEMENTS' OR"
				+ " relation_type='CALLS' OR"
				+ " relation_type='RETURNS' OR"
				+ " relation_type='HOLDS' OR" 
				+ " relation_type='USES')";

		Iterator<Map<String, Object>> _dbRelations = dataSource.getData(_sql);

		while (_dbRelations.hasNext()) {
			Map<String, Object> relationMap = _dbRelations.next();
			Relation rel = makeRelationWithLocalSource(relationMap);
			relations.add(rel);
		}
	}

	private Relation makeRelationWithLocalSource(Map<String, Object> relationMap) {
		Relation rel = new Relation();
		rel.leid = ((BigInteger) relationMap.get("lhs_eid")).longValue();
		rel.lhsEntityCategory = EntityCategory.LOCAL;
		rel.type = RelationType.valueOf((String) relationMap.get("relation_type"));

		Object reid = relationMap.get("rhs_eid");
		rel.rhsEntityCategory = EntityCategory.LOCAL;

		if (reid == null) {
			reid = relationMap.get("rhs_leid");
			rel.rhsEntityCategory = EntityCategory.JDK;
		}

		if (reid == null) {
			reid = relationMap.get("rhs_jeid");
			rel.rhsEntityCategory = EntityCategory.LIB;
		}

		// TODO handle properly, well this never happens
		assert reid != null;

		rel.reid = ((BigInteger) reid).longValue();

		return rel;

	}

	private Entity makeEntity(Map<String, Object> entityMap,
			EntityCategory entityCategory) {
		Entity entity = new Entity();

		entity.fqn = (String) entityMap.get("fqn");
		entity.type = EntityType.valueOf((String) entityMap.get("entity_type"));
		entity.entityId = ((BigInteger) entityMap.get("entity_id")).longValue();
		entity.category = entityCategory;

		return entity;

	}

	private void fillLocalEntities(List<Long> entityIds) {
		String _sql = "select entity_id, entity_type, fqn from entities where entity_id in("
				+ delimitLongListWithComma(entityIds) + ") ";

		Iterator<Map<String, Object>> _dbEntities = dataSource.getData(_sql);

		while (_dbEntities.hasNext()) {
			Map<String, Object> entityMap = _dbEntities.next();
			Entity entity = makeEntity(entityMap, EntityCategory.LOCAL);
			usedLocalEntities.put(new Long(entity.entityId), entity);
		}
	}

	private void fillJdkEntities(List<Long> entityIds) {
		String _sql = "select entity_id, entity_type, fqn from library_entities where entity_id in("
				+ delimitLongListWithComma(entityIds) + ") ";

		Iterator<Map<String, Object>> _dbEntities = dataSource.getData(_sql);

		while (_dbEntities.hasNext()) {
			Map<String, Object> entityMap = _dbEntities.next();
			Entity entity = makeEntity(entityMap, EntityCategory.JDK);
			usedJdkEntities.put(new Long(entity.entityId), entity);
		}
	}

	private void fillLibEntities(List<Long> entityIds) {
		String _sql = "select entity_id, entity_type, fqn from jar_entities where entity_id in("
				+ delimitLongListWithComma(entityIds) + ") ";

		Iterator<Map<String, Object>> _dbEntities = dataSource.getData(_sql);

		while (_dbEntities.hasNext()) {
			Map<String, Object> entityMap = _dbEntities.next();
			Entity entity = makeEntity(entityMap, EntityCategory.LIB);
			usedLibEntities.put(new Long(entity.entityId), entity);
		}
	}

	private void fillLocalEntityRelations() {
		String _sql = "select lhs_eid, rhs_eid, relation_type from relations where "
			+ " lhs_eid in("
			+ delimitLongListWithComma(new ArrayList<Long>(entitiesInHit))
			+ ") "
			+ " AND "
			+ " rhs_eid in("
			+ delimitLongListWithComma(new ArrayList<Long>(entitiesInHit))
			+ ") "
			+ " AND "
			+ " relation_type='INSIDE'"; 

	Iterator<Map<String, Object>> _dbLocalRelationsInside = dataSource
			.getData(_sql);

	while (_dbLocalRelationsInside.hasNext()) {
		Map<String, Object> relationMap = _dbLocalRelationsInside.next();
		Relation rel = makeInsideRelationAmongHitEntities(relationMap);
		relations.add(rel);
	}
	}
	
	private Relation makeInsideRelationAmongHitEntities(
			Map<String, Object> relationMap) {

		Relation rel = new Relation();
		rel.leid = ((BigInteger) relationMap.get("lhs_eid")).longValue();
		rel.lhsEntityCategory = EntityCategory.LOCAL;

		rel.reid = ((BigInteger) relationMap.get("rhs_eid")).longValue();
		rel.rhsEntityCategory = EntityCategory.LOCAL;
		
		rel.type = RelationType.valueOf((String) relationMap.get("relation_type"));
		
		return rel;
	}
	
	private void fillJdkEntityRelations() {
		String _sql = "select lhs_leid, rhs_leid, relation_type from library_relations where "
				+ " lhs_leid in("
				+ delimitLongListWithComma(new ArrayList<Long>(
						jdkEntityUseCount.keySet()))
				+ ") "
				+ " AND "
				+ " rhs_leid in("
				+ delimitLongListWithComma(new ArrayList<Long>(
						jdkEntityUseCount.keySet()))
				+ ") "
				+ " AND "
				+ " relation_type='INSIDE'";

		Iterator<Map<String, Object>> _dbJdkRelations = dataSource
				.getData(_sql);

		while (_dbJdkRelations.hasNext()) {
			Map<String, Object> relationMap = _dbJdkRelations.next();
			Relation rel = makeRelationAmongUsedJdkEntities(relationMap);
			relations.add(rel);
		}
	}

	private Relation makeRelationAmongUsedJdkEntities(
			Map<String, Object> relationMap) {

		Relation rel = new Relation();
		rel.leid = ((BigInteger) relationMap.get("lhs_leid")).longValue();
		rel.lhsEntityCategory = EntityCategory.JDK;

		rel.reid = ((BigInteger) relationMap.get("rhs_leid")).longValue();
		rel.rhsEntityCategory = EntityCategory.JDK;
		
		rel.type = RelationType.valueOf((String) relationMap.get("relation_type"));

		return rel;
	}

	private void fillLibEntityRelations() {
		fillLib2LibEntityRelations();
		fillLib2JdkEntityRelations();
	}
	
	private void fillLib2LibEntityRelations() {
		String _sql = "select lhs_jeid, rhs_jeid, rhs_leid, relation_type from jar_relations where "
				+ " lhs_jeid in("
				+ delimitLongListWithComma(new ArrayList<Long>(
						libEntityUseCount.keySet()))
				+ ") "
				+ " AND "
				+ " rhs_jeid in("
				+ delimitLongListWithComma(new ArrayList<Long>(
						libEntityUseCount.keySet()))
				+ ") AND "
				+ " relation_type='INSIDE'";

		Iterator<Map<String, Object>> _dbJdkRelations = dataSource
				.getData(_sql);

		while (_dbJdkRelations.hasNext()) {
			Map<String, Object> relationMap = _dbJdkRelations.next();
			Relation rel = makeLib2LibRelationAmongUsedLibEntities(relationMap);
			if(rel!=null) relations.add(rel);
			
			
		}
	}
	
	private void fillLib2JdkEntityRelations() {
		String _sql = "select lhs_jeid, rhs_jeid, rhs_leid, relation_type from jar_relations where "
				+ " lhs_jeid in("
				+ delimitLongListWithComma(new ArrayList<Long>(
						libEntityUseCount.keySet()))
				+ ") "
				+ " AND "
				+ " rhs_leid in("
				+ delimitLongListWithComma(new ArrayList<Long>(
						jdkEntityUseCount.keySet()))
				+ ") AND "
				+ " relation_type='INSIDE'";

		Iterator<Map<String, Object>> _dbJdkRelations = dataSource
				.getData(_sql);

		while (_dbJdkRelations.hasNext()) {
			Map<String, Object> relationMap = _dbJdkRelations.next();
			
			Relation rel2 = makeLib2JdkRelationAmongUsedLibEntities(relationMap);
			if(rel2!=null) relations.add(rel2);
		}
	}

	private Relation makeLib2LibRelationAmongUsedLibEntities(
			Map<String, Object> relationMap) {

		if(relationMap.get("rhs_jeid") == null) return null;
		
		Relation rel = new Relation();
		rel.leid = ((BigInteger) relationMap.get("lhs_jeid")).longValue();
		rel.lhsEntityCategory = EntityCategory.LIB;

		rel.reid = ((BigInteger) relationMap.get("rhs_jeid")).longValue();
		rel.rhsEntityCategory = EntityCategory.LIB;
		
		rel.type = RelationType.valueOf((String) relationMap.get("relation_type"));

		return rel;
	}
	
	private Relation makeLib2JdkRelationAmongUsedLibEntities(
			Map<String, Object> relationMap) {

		if(relationMap.get("rhs_leid") == null) return null;
		
		Relation rel = new Relation();
		rel.leid = ((BigInteger) relationMap.get("lhs_jeid")).longValue();
		rel.lhsEntityCategory = EntityCategory.LIB;

		rel.reid = ((BigInteger) relationMap.get("rhs_leid")).longValue();
		rel.rhsEntityCategory = EntityCategory.JDK;
		
		rel.type = RelationType.valueOf((String) relationMap.get("relation_type"));

		return rel;
	}

	private String delimitStringListWithComma(List<String> list) {
		if (list == null || list.size() == 0)
			return null;

		StringBuffer sb = new StringBuffer();

		if (list.size() > 0) {
			int i = 0;
			for (String s : list) {
				sb.append(s);
				if (i < list.size() - 1)
					sb.append(", ");

				i++;
			}
		}

		return sb.toString();
	}

	private String delimitLongListWithComma(List<Long> list) {
		if (list == null || list.size() == 0)
			return null;

		StringBuffer sb = new StringBuffer();

		if (list.size() > 0) {
			int i = 0;
			for (Long s : list) {
				sb.append(s.longValue() + "");
				if (i < list.size() - 1)
					sb.append(", ");

				i++;
			}
		}

		return sb.toString();
	}

}
