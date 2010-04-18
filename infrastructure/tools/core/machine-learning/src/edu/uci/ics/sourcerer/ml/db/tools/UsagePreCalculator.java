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
package edu.uci.ics.sourcerer.ml.db.tools;

import static edu.uci.ics.sourcerer.util.io.Logging.logger;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import edu.uci.ics.sourcerer.db.util.JdbcDataSource;

/**
 * @author <a href="bajracharya@gmail.com">Sushil Bajracharya</a>
 * @created Mar 24, 2010
 * 
 */
public class UsagePreCalculator {

	public enum EntityType{
		CLASS, METHOD;
		
		public static String allElementNames(){
			StringBuffer buf = new StringBuffer();
			for(EntityType e: EntityType.values()){
				buf.append(e.toString());
				buf.append(" ");
			}
			
			return buf.toString();
		}
	}
	
	EntityType optSelectedType  = EntityType.METHOD;
	boolean optExcludeInternal = true;
	
	JdbcDataSource ds = new JdbcDataSource();

	final String PREDICATE_ENTITY_TYPE_METHOD = " in ('METHOD','CONSTRUCTOR') ";
	final String PREDICATE_ENTITY_TYPE_CLASS  = " = 'CLASS' ";
	final String PLACEHOLDER_ENTITY_TYPE = "!!ETYPE!!";
	
	final String PREDICATE_EXCLUDE_INTERNAL = " and R.internal = 0 ";
	final String PREDICATE_INCLUDE_INTERNAL = "";
	final String PLACEHOLDER_INTERNAL = "!!INTERNAL!!";
	
	String SQL_ALL_USAGE = "select userE.entity_id as eid, provE.fqn as fqn"
			+ " from relations as R"
			+ " inner join entities as userE on userE.entity_id=R.lhs_eid"
			+ " inner join entities as provE on provE.entity_id=R.rhs_eid"
			+ " inner join projects as proj on proj.project_id=userE.project_id"
			+ " where userE.length > 0"
			+ " and R.relation_type in ('CALLS','EXTENDS','IMPLEMENTS','INSTANTIATES','USES')"
			+ " and userE.entity_type " 
			+ PLACEHOLDER_ENTITY_TYPE
			+ PLACEHOLDER_INTERNAL
			+ " and proj.has_source=1"
			+ " and proj.project_type='CRAWLED'"
			// + " limit 100"
			;
	
	//project_type='CRAWLED' and has_source=1 and length>0 and entity_type in ('METHOD','CONSTRUCTOR')
	
	// select count(*) from entities inner join projects on projects.project_id=entities.project_id where ;

	private long fqnCount = 0;
	
	IUsageWriter writer;

	public void setWriter(IUsageWriter writer) {
		this.writer = writer;
	}
	
	// Hashtable<String, Long> fqnTable = new Hashtable<String, Long>();
	HashMap<String,FqnData> fqnTable = new HashMap<String, FqnData>();
	HashMap<Long, String> id2Fqn = new HashMap<Long, String>();
	HashMap<Long, Usage> usageTable = new HashMap<Long, Usage>();
	
	protected void updateFqnUse(Long eid, Long fqnId) {
		if (!usageTable.containsKey(eid)) {
			usageTable.put(eid, new Usage(fqnId));
		} else {
			usageTable.get(eid).incrementCount(fqnId);
		}
	}

	protected long updateFqn(String fqnUsed) {
		long fqnId = -1;
		if (fqnTable.containsKey(fqnUsed))
			fqnId = fqnTable.get(fqnUsed).id;
		else {
			fqnCount = fqnCount + 1;
			fqnTable.put(fqnUsed, new FqnData(fqnCount, 0));
			fqnId = fqnCount;
		}
		
		if (!id2Fqn.containsKey(new Long(fqnId))){
			id2Fqn.put(new Long(fqnId), fqnUsed);
		}
		
		return fqnId;
	}
	
	public void init(String dbUri, String dbUser, String dbPassword, EntityType entityType, boolean optExcludeInternal2) {
		
		this.optSelectedType = entityType;
		this.optExcludeInternal = optExcludeInternal2;
		
		initDb(dbUri, dbUser, dbPassword);

		calculateUsage();
		logger.info("Done retrieving usage information from database.");
		
		updateFqnUsedCount();

	}

	private void initDb(String dbUri, String dbUser, String dbPassword) {
		Properties p = new Properties();
		p.put("driver", "com.mysql.jdbc.Driver");
		p.put("url", dbUri);
		p.put("user", dbUser);
		p.put("password", dbPassword);
		ds.init(p);
	}

	private void calculateUsage() {
		Iterator<Map<String, Object>> allUsage = ds.getData(getSqlString());
		processUsageResults(allUsage);
	}

	private String getSqlString() {
		if(optExcludeInternal){
			SQL_ALL_USAGE = SQL_ALL_USAGE.replaceAll(PLACEHOLDER_INTERNAL, PREDICATE_EXCLUDE_INTERNAL);
		} else {
			SQL_ALL_USAGE = SQL_ALL_USAGE.replaceAll(PLACEHOLDER_INTERNAL, PREDICATE_INCLUDE_INTERNAL);;
		}
		
		if(optSelectedType == EntityType.CLASS){
			SQL_ALL_USAGE = SQL_ALL_USAGE.replaceAll(PLACEHOLDER_ENTITY_TYPE, PREDICATE_ENTITY_TYPE_CLASS);
		} else if(optSelectedType == EntityType.METHOD) {
			SQL_ALL_USAGE = SQL_ALL_USAGE.replaceAll(PLACEHOLDER_ENTITY_TYPE, PREDICATE_ENTITY_TYPE_METHOD);
		}
			
		return SQL_ALL_USAGE;
	}

	private void processUsageResults(Iterator<Map<String, Object>> usage) {
		while (usage.hasNext()) {
			Map<String, Object> usageMap = usage.next();
			// rid, eid, fqn
			Long eid = ((BigInteger) usageMap.get("eid")).longValue();
			String fqn = (String) usageMap.get("fqn");
			updateUsage(eid, fqn);
		}
	}

	protected void updateUsage(Long eid, String fqnUsed) {
		
		// skip if fqn does not have a '.' in it
		// only include proper fqns with '.'
		if(fqnUsed.indexOf('.')<1) return;
		
		// if(simplifyFqn){
		// split complex fqn into simpler ones
		// based on ? + - < >
		// update fqns with simple fqns extracted
		// }
		
		long fqnId = updateFqn(fqnUsed);
		
		updateFqnUse(eid, fqnId);
	}

	private void updateFqnUsedCount(){
		for (Long entityId : usageTable.keySet()) {

			Usage u = usageTable.get(entityId);

			for (Long apiFqnId : u.getApiFqnIds()) {
				String fqn = id2Fqn.get(apiFqnId);
				fqnTable.get(fqn).count = fqnTable.get(fqn).count + 1; 
			}
		}
	}
	
	public void writeUsage() {

		assert writer != null;

		for (Long entityId : usageTable.keySet()) {

			Usage u = usageTable.get(entityId);

			for (Long apiFqnId : u.getApiFqnIds()) {
				int useCountForEntity = u.getApiUsedCount(apiFqnId);
				writer.writeUsage(entityId, apiFqnId, useCountForEntity);
			}
			
			writer.writeNumFqnsUsed(entityId, u.getApiFqnIds().size());
		}
		logger.info("Done writing entity/each-fqn/count.");
		
		for (String fqn : fqnTable.keySet()) {
			writer.writeFqnId(fqnTable.get(fqn).id, fqn, fqnTable.get(fqn).count);
		}
		logger.info("Done writing Fqns.");
	}

}

class Usage {

	private Hashtable<Long, Integer> usedFqns;

	public Set<Long> getApiFqnIds() {
		return usedFqns.keySet();
	}

	public int getApiUsedCount(long apiEntityId) {
		return this.usedFqns.get(apiEntityId).intValue();
	}

	public Usage(long usedFqnId) {
		usedFqns = new Hashtable<Long, Integer>();
		this.usedFqns.put(usedFqnId, 1);
	}

	public void incrementCount(Long usedFqnId) {
		Integer count = this.usedFqns.get(usedFqnId);
		if (count == null)
			this.usedFqns.put(usedFqnId, 1);
		else
			this.usedFqns.put(usedFqnId, count + 1);
	}
}

class FqnData {
	public long id = 0;
	public int count = 0;
	public FqnData(long id, int count){
		this.id = id;
		this.count = count;
	}
}
