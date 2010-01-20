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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import edu.uci.ics.sourcerer.db.adapter.client.Entity;
import edu.uci.ics.sourcerer.db.adapter.client.Relation;
import edu.uci.ics.sourcerer.scs.client.ERTables;
import edu.uci.ics.sourcerer.scs.common.SourcererSearchAdapter;
import edu.uci.ics.sourcerer.scs.common.client.EntityCategory;
import edu.uci.ics.sourcerer.scs.common.client.EntityType;
import edu.uci.ics.sourcerer.scs.common.client.HitFqnEntityId;
import edu.uci.ics.sourcerer.scs.common.client.RelationType;
import edu.uci.ics.sourcerer.scs.common.client.UsedFqn;

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

	public UsedFqn fillUsedFqnDetails(HitFqnEntityId hitFqn, EntityCategory cat) {
		
		String table;
		if(cat == EntityCategory.JDK){
			table = "library_entities";
		} else if (cat == EntityCategory.LIB) {
			table = "jar_entities";
		} else if (cat == EntityCategory.LOCAL){
			table = "entities";
		} else {
			return null; // never
		}
		
		String _sql = "select entity_id, entity_type from " 
			+ table
			+ " where fqn='"
			+ hitFqn.fqn
			+ "'";
		
		
		Iterator<Map<String, Object>> _results = dataSource.getData(_sql);
		
		UsedFqn _usedFqn = new UsedFqn(hitFqn.fqn, cat, hitFqn.getUseCount());

		while (_results.hasNext()) {
			Map<String, Object> resultMap = _results.next();
			
			_usedFqn.addEntityId(new Long(((BigInteger) resultMap.get("entity_id")).longValue()));
			String _entity_type = (String) resultMap.get("entity_type");
			
			setEntityType(_usedFqn, _entity_type);
			
			if(_usedFqn.getType().equals(EntityType.UNKNOWN) /*
			|| ufqn.getType().equals(EntityType.OTHER)*/){
				_usedFqn.setType(EntityType.valueOf(lookupJarEntityTypeForUnknown(_usedFqn.getFqn())));
			}	
			
		}
		
		return _usedFqn;
		
	}

	/**
	 * @param _usedFqn
	 * @param _entity_type
	 */
	private void setEntityType(UsedFqn _usedFqn, String _entity_type) {
		
		if(_entity_type.equals("CLASS")){
			_usedFqn.setType(EntityType.CLASS);
		}else if(_entity_type.equals("INTERFACE")){
			_usedFqn.setType(EntityType.INTERFACE);
		}else if(_entity_type.equals("METHOD")){
			_usedFqn.setType(EntityType.METHOD);
		}else if(_entity_type.equals("CONSTRUCTOR")){
			_usedFqn.setType(EntityType.CONSTRUCTOR);
		}else if(_entity_type.equals("FIELD")){
			_usedFqn.setType(EntityType.FIELD);
		}else if(_entity_type.equals("UNKNOWN")){
			_usedFqn.setType(EntityType.UNKNOWN);
		} else 
			_usedFqn.setType(EntityType.OTHER);
		
	}
	
	public String lookupJarEntityTypeForUnknown(String fqn){
		
		fqn = fqn.replaceFirst("\\.\\(",".<init>(");
		String sql = "select distinct entity_type from jar_entities where fqn='" 
			+ fqn + "'"
			+ " and entity_type <> 'UNKNOWN'";
		
		String type = "UNKNOWN";
		
		Iterator<Map<String, Object>> _results = dataSource.getData(sql);
		while (_results.hasNext()) {
			Map<String, Object> resultMap = _results.next();
			type = (String) resultMap.get("entity_type");
			break;
		}
		
		return type;
	}

	public List<UsedFqn> fillUsedFqnsDetails(List<HitFqnEntityId> apis,
			EntityCategory cat) {
		
		List<UsedFqn> usedFqns = new LinkedList<UsedFqn>();
		for(HitFqnEntityId api: apis){
			usedFqns.add(fillUsedFqnDetails(api, cat));
		}
		
		return usedFqns;
	}
	
	/**
	 * 
	 * @param entityId
	 * @param usedTopLibApis assuming sorted by count in desc order
	 * @param topKApis
	 * @return
	 */
	public String getSnippetForJarEntityHit(String entityId, List<UsedFqn> usedTopLibApis, int topKApis){
		
		if(usedTopLibApis == null)
			return "";
		else if(usedTopLibApis.size()==0)
			return "";
		
		List<String> topClasses = new LinkedList<String>();
		List<String> topInterfaces = new LinkedList<String>();
		List<String> topMethods = new LinkedList<String>();
		List<String> topConstructors = new LinkedList<String>();
		List<String> topFields = new LinkedList<String>();
		List<String> topOthers = new LinkedList<String>();
		
		int _CLcount = 0;
		int _INcount = 0;
		int _MEcount = 0;
		int _FIcount = 0;
		int _COcount = 0;
		int _OTcount = 0;
		
		
		for(UsedFqn usedFqn: usedTopLibApis){
			
			switch(usedFqn.getType()){
			
			case CLASS:
				if(_CLcount<topKApis) {
					for(Long cid: usedFqn.getEntityIdsMatchingFqns()) topClasses.add(cid + "");
				}
				_CLcount++;
				break;
			
			case INTERFACE:
				if(_INcount<topKApis/2) {
					for(Long iid: usedFqn.getEntityIdsMatchingFqns()) topInterfaces.add(iid + "");
				}
				_INcount++;
				break;
				
			case METHOD:
				if(_MEcount<topKApis) {
					for(Long mid: usedFqn.getEntityIdsMatchingFqns()) topMethods.add(mid + "");
				}
				_MEcount++;
				break;
				
			case CONSTRUCTOR:
				if(_COcount<topKApis) {
					for(Long coid: usedFqn.getEntityIdsMatchingFqns()) topConstructors.add(coid + "");
				}
				_COcount++;
				break;
			
			case FIELD:
				if(_FIcount<topKApis) {
					for(Long tid: usedFqn.getEntityIdsMatchingFqns()) topFields.add(tid + "");
				}
				_FIcount++;
				break;
				
			default: 
				if(_OTcount<topKApis) {
					for(Long oid: usedFqn.getEntityIdsMatchingFqns()) topOthers.add(oid + "");
				}
				_OTcount++;
				break;
			}
			
//			if( all_counts >=topKApis)
//				break;
		
		}
		
		String topUsedEntities = "";
		StringBuffer _sbufTopUsedEntities = new StringBuffer();
		_sbufTopUsedEntities.append("(");
		
//		for(String cl: topClasses){
//			_sbufTopUsedEntities.append(cl);
//			_sbufTopUsedEntities.append(",");
//		}
//		
		for(String in: topInterfaces){
			_sbufTopUsedEntities.append(in);
			_sbufTopUsedEntities.append(",");
		}
		
		for(String me: topMethods){
			_sbufTopUsedEntities.append(me);
			_sbufTopUsedEntities.append(",");
		}
		
//		for(String fi: topFields){
//			_sbufTopUsedEntities.append(fi);
//			_sbufTopUsedEntities.append(",");
//		}
		
		for(String co: topConstructors){
			_sbufTopUsedEntities.append(co);
			_sbufTopUsedEntities.append(",");
		}
		
//		for(String ot: topOthers){
//			_sbufTopUsedEntities.append(ot);
//			_sbufTopUsedEntities.append(",");
//		}
		
		_sbufTopUsedEntities.append(")");
		
		// will this be faster:  s.substring(0,s.length()-2) + ")"
		topUsedEntities = _sbufTopUsedEntities.toString().replaceFirst(",\\)", ")");
		
		// no used apis
		if (topUsedEntities.equals("()")) 
			return "";
		
		String _sql = "select jr.offset, jr.length, jr.jclass_fid, jr.relation_type, used_je.fqn from jar_relations as jr " +
				" inner join jar_entities as used_je on jr.rhs_jeid=used_je.entity_id " +
				" where jr.lhs_jeid=" + entityId
			+ " and jr.rhs_jeid is not null and jr.length is not null and jr.offset is not null and jr.rhs_jeid in "
		    + topUsedEntities;
		
		//System.err.println(_sql);
		
		Iterator<Map<String, Object>> _results = dataSource.getData(_sql);
		
		SortedMap<Long, Long> _offsetLengths = new TreeMap<Long, Long>();
		SortedMap<Long, String> _offsetComments = new TreeMap<Long, String>();
		String classFileId = "";
		
		while (_results.hasNext()) {
			Map<String, Object> resultMap = _results.next();
			
			Long _off = (Long) resultMap.get("offset");
			Long _length = (Long) resultMap.get("length");
			String _relation = (String) resultMap.get("relation_type");
			String _usedFqn = (String) resultMap.get("fqn");
			
			if(_offsetLengths.containsKey(_off)){
				if (_offsetLengths.get(_off).longValue() > _length){
					_offsetLengths.put(_off, _length);
				}
				_offsetComments.put(_off, _offsetComments.get(_off) + "\n" + "//-- " + _relation + " " + _usedFqn + " --//");
			} else{
				_offsetLengths.put(_off, _length);
				_offsetComments.put(_off, "\n//-- " + _relation + " " + _usedFqn + " --//");
			}
			
			String _cfid = ((BigInteger) resultMap.get("jclass_fid")).toString() + "";
			if(_cfid.length()>0) classFileId = _cfid;
			
		}
		
		// System.err.println(classFileId);
		// this entity is not using any top api
		if(classFileId.equals("")) return "";
		String jarClassFileContent = getClassFileIdForJarEntity(classFileId);
		
		return getSnippetFromSourceString(jarClassFileContent, _offsetLengths, _offsetComments);
	}
	
	/**
	 * return string that has lines of code extracted from jarClassFileContent (String
	 * representation of jar class file), based on list of offset,lengths
	 * 
	 * @param jarClassFileContent
	 * @param offsetLengths sorted on offset
	 * @param offsetComments 
	 * @return
	 */
	private String getSnippetFromSourceString(String jarClassFileContent,
			SortedMap<Long, Long> offsetLengths, SortedMap<Long, String> offsetComments) {
		
		StringBuffer snippetLines = new StringBuffer();
		
		String prevLine = "";
		for(Long off: offsetLengths.keySet()){
			String line = getLine(jarClassFileContent, off.intValue(), offsetLengths.get(off).intValue()).trim();
			line = "\n" + line + "\n";
			
			if(!prevLine.equals(line)){
				snippetLines.append(offsetComments.get(off));
				snippetLines.append(line);
			} else {
				snippetLines.replace(snippetLines.length() + 1 - prevLine.length(), snippetLines.length(), "");
//				snippetLines.append("[DUP]" + snippetLines.substring(snippetLines.length() + 1 - prevLine.length(), 
//						snippetLines.length()));
				String comments = offsetComments.get(off);
				snippetLines.append(comments.substring(1, comments.length()));
				snippetLines.append(line);
			}
			//snippetLines.append("\n");
			
			prevLine = line;
		}
		
		return snippetLines.toString();
	}

	private String getLine(String jarClassFileContent, int offset,
			int length) {
	
		
		int start = offset;
		int end = offset + length;
		
		int pointer = start;
		
		while(true){
		 if (pointer <= 0) break;
		 if(pointer>=jarClassFileContent.length()-1){
			 pointer = jarClassFileContent.length()-1;
			 break;
		 }
		 if (jarClassFileContent.charAt(pointer) == ';'
				 /*|| jarClassFileContent.charAt(pointer) == '\n'
				 || jarClassFileContent.charAt(pointer) == '\r'*/
					 ){
			 pointer = pointer +1;
			 break;
		 } else {
			 pointer = pointer - 1;
		 }
		}
		
		start = pointer;
		pointer = end;
		
		while(true){
			 if (pointer >= jarClassFileContent.length()) break;
			 
			 if (jarClassFileContent.charAt(pointer) == ';'
						 /*|| jarClassFileContent.charAt(pointer) == '\n'
						 || jarClassFileContent.charAt(pointer) == '\r'*/
							 ){
				 pointer = pointer + 1;
				 break;
			 } else {
				 pointer = pointer + 1;
			 }
			}
		end = pointer;
		
		// prevent AIOB
		if (start < 0 ) start = 0;
		if (end > jarClassFileContent.length()-1) end = jarClassFileContent.length()-1;
		
		return jarClassFileContent.substring(start, end);
		
	}

	public static String getClassFileIdForJarEntity(String jarClassFileID){
		return SourcererSearchAdapter.getJarClassFileCodeRaw(jarClassFileID);
	}
	
	/**
	 * For now this only gives used jar entities
	 * @param entityHits
	 * @return
	 */
	public List<UsedFqn> getTopUsedEntitiesForJarEntityHit(List<HitFqnEntityId> entityHits){
		
		StringBuffer sbufUserJeids = new StringBuffer();
		sbufUserJeids.append("(");
		for(HitFqnEntityId hit: entityHits){
			sbufUserJeids.append(hit.entityId);
			sbufUserJeids.append(",");
		}
		sbufUserJeids.append(")");
		String userJeids = sbufUserJeids.toString().replaceFirst(",\\)", ")");
		
		String sqlUsedJarEntities = "select provider_je.entity_id as pjeid, provider_je.fqn as pfqn, provider_je.entity_type as etype, jr.lhs_jeid as ujeid " 
			+ " from jar_relations as jr inner join jar_entities as provider_je on "
			+ " jr.rhs_jeid=provider_je.entity_id "
			+ " and jr.relation_type in ('CALLS','EXTENDS','IMPLEMENTS','INSTANTIATES','USES', 'OVERRIDES')" 
			+ " and jr.lhs_jeid in"
			+ userJeids;
		
		//System.err.println(sqlUsedJarEntities);
		if(userJeids.equals("()")){
			return null;
		}
		
		Iterator<Map<String, Object>> _results = dataSource.getData(sqlUsedJarEntities);
		
		HashMap<String, UsedFqn> usedFqns = new HashMap<String, UsedFqn>();
		HashMap<String, Set<Long>> fqnsUserIds = new HashMap<String, Set<Long>>();
		
		while (_results.hasNext()) {
			Map<String, Object> resultMap = _results.next();
			
			 Long providerJeid = new Long(((BigInteger) resultMap.get("pjeid")).longValue());
			 Long userJeid = new Long(((BigInteger) resultMap.get("ujeid")).longValue());
			 String providerFqn = (String) resultMap.get("pfqn");
			 String etype = (String) resultMap.get("etype");
			 
			 if(fqnsUserIds.containsKey(providerFqn)){
				 fqnsUserIds.get(providerFqn).add(userJeid);
			 } else {
				 HashSet<Long> userIdSet = new HashSet<Long>();
				 userIdSet.add(userJeid);
				 fqnsUserIds.put(providerFqn, userIdSet);
			 }
			 
			if(usedFqns.containsKey(providerFqn)){
				usedFqns.get(providerFqn).addEntityId(providerJeid);
			} else {
				UsedFqn usedFqn = new UsedFqn(providerFqn, EntityCategory.LIB, 1);
				usedFqn.addEntityId(providerJeid);
				usedFqns.put(providerFqn, usedFqn);
			}
			
			
			setEntityType(usedFqns.get(providerFqn), etype);
			usedFqns.get(providerFqn).setUseCount(fqnsUserIds.get(providerFqn).size());
			 
		}

		LinkedList<UsedFqn> retVal = new LinkedList<UsedFqn>();
		
		for(UsedFqn ufqn: usedFqns.values()){
			retVal.add(ufqn);
			if(ufqn.getType().equals(EntityType.UNKNOWN) /*
					|| ufqn.getType().equals(EntityType.OTHER)*/){
				ufqn.setType(EntityType.valueOf(lookupJarEntityTypeForUnknown(ufqn.getFqn())));
			}
		}
		
		Collections.sort(retVal, new UsedFqnValueComparator<UsedFqn>());
		
		return retVal;
	}
	
	class UsedFqnValueComparator<Usefqn> implements Comparator<UsedFqn> {

		  public int compare(UsedFqn a, UsedFqn b) {

		    if(a.getUseCount() < b.getUseCount()) {
		      return 1;
		    } else if(a.getUseCount() == b.getUseCount()) {
		      return 0;
		    } else {
		      return -1;
		    }
		  }
		}


}
