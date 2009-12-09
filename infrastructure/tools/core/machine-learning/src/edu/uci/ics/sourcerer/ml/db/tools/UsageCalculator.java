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

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.util.BitSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;

import edu.uci.ics.sourcerer.db.util.JdbcDataSource;
import static edu.uci.ics.sourcerer.util.io.Logging.logger;

/**
 * @author <a href="bajracharya@gmail.com">Sushil Bajracharya</a>
 * @created Dec 7, 2009
 *
 */
public class UsageCalculator {
	
	final String SQL_JAR_USAGE = 
		"select " +
		"distinct r1.relation_id as rid, " +
		" r1.lhs_jeid as eid, " +
		" je.fqn as fqn, " +
		" users.entity_type as etype " +
		"from jar_relations as r1 " +
		"inner join jar_relations as r2 " +
		" on r1.lhs_jeid=r2.lhs_jeid AND " +
		" r2.relation_type='INSIDE' " +
		"inner join jar_relations as r3 " +
		" on r1.rhs_jeid=r3.lhs_jeid AND " +
		" r3.relation_type='INSIDE' " +
		"inner join jar_entities as je " + // -- je is an API entity 
		" on r1.rhs_jeid=je.entity_id  AND" +
		" IF(je.modifiers & 0x0001,1,0)=1  " + // -- only public entities (API
		"inner join jar_entities as users " +
		" on users.entity_id=r1.lhs_jeid " +
		"inner join jars as j " +
		" on j.jar_id=users.jar_id " +
		" where " +
		"r1.relation_type IN ('CALLS','EXTENDS','IMPLEMENTS','INSTANTIATES','USES') and " +
		"r2.rhs_jeid <> r3.rhs_jeid and " + // -- has subset of relations among jar entities, skips relations among siblings
		"j.name like '%eclipse.%' AND " + // -- choose relations from eclipse projects
		"users.entity_type in ('CLASS','METHOD','CONSTRUCTOR') " // -- limit to these types for users
		// + "and r1.lhs_jeid=2981061"
		//+ "limit 1000" 
		;
	
	final String SQL_JDK_USAGE = 
		// -- jar entity_ids and library fqns used
		"select " +
		"distinct jr_2.relation_id as rid, jr_2.lhs_jeid as eid, le.fqn as fqn, " +
		" users.entity_type as etype " +
		"from jar_relations as jr_2 " +
		"inner join library_entities as le " + // -- le is an API entity 
		" on jr_2.rhs_leid=le.entity_id " +
		"inner join jar_entities as users " +
		" on users.entity_id=jr_2.lhs_jeid " +
		"inner join jars as j " +
		" on j.jar_id=users.jar_id " +
		" where " +
		"jr_2.relation_type in ('CALLS','EXTENDS','IMPLEMENTS','INSTANTIATES','USES') AND " +
		"IF(le.modifiers & 0x0001,1,0)=1 AND " + //-- only public entities (API) 
		"j.name like '%eclipse.%' AND " + //-- choose relations from eclipse projects
		"users.entity_type in ('CLASS','METHOD','CONSTRUCTOR') "  //-- limit to these types for users
		// + "limit 0"
		;  
	
	private long fqnCount = 0;
	UsageFilter filter;
	
	Hashtable<String, Long> fqnTable = new Hashtable<String, Long>();
	Hashtable<Long, Usage> usageTable = new Hashtable<Long, Usage>();
	BitSet fqnsToSkip;
	BitSet isEntityClass = new BitSet(500000); // 1/2 million entities ?
	
	IUsageWriter writer;
	
	public void setWriter(IUsageWriter writer) {
		this.writer = writer;
	}


	JdbcDataSource ds = new JdbcDataSource();
	
	/**
	 * 
	 * @param dbUri URI for the sourcerer DB
	 * @param dbUser database user
	 * @param dbPassword database password
	 * @param distinctFqnsUsedByEntities threshold to filter users who do not use enough APIs
	 * @param fqnUseCount threshold to filter APIs that are not used by enough entities
	 * @param usedFqnCountFileJdk Path to text file storing FQNs from Jdk and their use count.
	 * 			Tab separated, first column FQN second column count
	 * @param usedFqnCountFileJars Path to text file storing FQNs from Jars and their use count.
	 * 			Tab separated, first column FQN second column count
	 */
	public void init(
			String dbUri, String dbUser, String dbPassword,
			int distinctFqnsUsedByEntities, // = 2;
			// only allow APIs that are used more than this number
			int fqnUseCount, // = 3;
			// jdk_fqn, count
			String usedFqnCountFileJdk,
			// jar_fqn, count
			String usedFqnCountFileJars,
			// fqn
			String popularFqnsFile
	){
		initDb(dbUri, dbUser, dbPassword);
		calculateJdkUsage();
		calculateJarUsage();
		filter = new UsageFilter(distinctFqnsUsedByEntities, 
				fqnUseCount, usedFqnCountFileJdk, usedFqnCountFileJars,
				popularFqnsFile);
		initFqnsToSkip();
	}
	
	private void initDb(String dbUri, String dbUser, String dbPassword){
		Properties p = new Properties();
		p.put("driver", "com.mysql.jdbc.Driver");
	    p.put("url", dbUri);
	    p.put("user", dbUser);
	    p.put("password", dbPassword);
	    ds.init(p);
		ds.init(p);
	}
	
	private void initFqnsToSkip(){
		
		if (filter == null)
			return;
		
		assert (fqnTable!=null && fqnTable.size()>0);
		
		fqnsToSkip = new BitSet(fqnTable.size());
		
		if(filter.popularFqnsFile!=null){
			initPopularFqnsToSkip(filter.popularFqnsFile);
		}
		
		// load fqns from all jar usage count
		if(filter.usedFqnCountFileJars!=null){
			initFqnsToSkip(filter.usedFqnCountFileJars);
		}
		
		// load fqns from all jdk usage count
		if(filter.usedFqnCountFileJdk!=null){
			initFqnsToSkip(filter.usedFqnCountFileJdk);
		}
		
	}
	
	private void initPopularFqnsToSkip(String fileName) {
		
		FileInputStream in;
		BufferedReader br;
		try {
			in = new FileInputStream(fileName);
			br = new BufferedReader(
					new InputStreamReader(in));
			String strLine = null;
			while ((strLine = br.readLine()) != null) {
				if(strLine==null || strLine.length()<=0) continue;
				
				if (!fqnTable.containsKey(strLine)) {
					// possibly an anamoly but ok for now as
					// only fqns in fqnTable get used as features
					// System.err.println(_fqn + " ");
					continue;
				}
				
				long _fqnId = fqnTable.get(strLine);
				// making sure ids are withing limit of narrow casting
				// limits the # of entities to 2,147,483,647
				assert (_fqnId < Integer.MAX_VALUE);
				fqnsToSkip.set((int) (_fqnId - 1));
			}
			
			br.close();
			in.close();
		} catch (FileNotFoundException e) {
			logger.log(Level.SEVERE, "File not found: " + fileName + "." +
					" Highly used FQNs won't be skipped");
			
		} catch (IOException e) {
			logger.log(Level.SEVERE, "IOE with file: " + fileName + "." +
				" Highly used FQNs won't be skipped");
		}
		
		
		
	}

	private void initFqnsToSkip(String fileName){
		int userEntitiesCountThreshold = filter.fqnUseCount;
		FileInputStream in;
		BufferedReader br;
		try {
			in = new FileInputStream(fileName);
			br = new BufferedReader(
					new InputStreamReader(in));
			String strLine = null;
			while ((strLine = br.readLine()) != null) {
				if(strLine==null || strLine.length()<=0) continue;
				
				// process line
				String[] _cols = strLine.split("\t");
				if (_cols == null || _cols.length != 2)
					continue;

				String _fqn = _cols[0];
				if (!fqnTable.containsKey(_fqn)) {
					// possibly an anamoly but ok for now as
					// only fqns in fqnTable get used as features
					// System.err.println(_fqn + " ");
					continue;
				}

				int _fCount = Integer.parseInt(_cols[1]);
				if (_fCount >= userEntitiesCountThreshold) {
					// don't skip this fqn
					continue;
				} else {
					long _fqnId = fqnTable.get(_fqn);
					// making sure ids are withing limit of narrow casting
					// limits the # of entities to 2,147,483,647
					assert (_fqnId < Integer.MAX_VALUE);
					fqnsToSkip.set((int) (_fqnId - 1));
				}

			}
			
			br.close();
			in.close();
		} catch (FileNotFoundException e) {
			logger.log(Level.SEVERE, "File not found: " + fileName + "." +
			" Least used FQNs won't be skipped");
		} catch (IOException e) {
			logger.log(Level.SEVERE, "IOE with file: " + fileName + "." +
			" Least used FQNs won't be skipped");
		} 

	}
	
	private void calculateJarUsage(){
		Iterator<Map<String, Object>> jarUsage = ds.getData(SQL_JAR_USAGE);
		processUsageResults(jarUsage);
	}
	
	private void calculateJdkUsage() {
		Iterator<Map<String, Object>> jarUsage = ds.getData(SQL_JDK_USAGE);
		processUsageResults(jarUsage);
	}
	
	public void writeUsage(){
		
		assert writer != null;
		
		for(String fqn: fqnTable.keySet()){
			writer.writeFqnId(fqnTable.get(fqn), fqn);
		}
		
		for(Long entityId: usageTable.keySet()){
			
			Usage u = usageTable.get(entityId);
			// IF skip entity
			if(u.getApiEntityIds().size() < getMinApiUsedByEntityThreshold())
				continue;
			
			for(Long apiFqnId: u.getApiEntityIds()){
				int useCountForEntity = u.getApiUsedCount(apiFqnId);
				if(skipApi(apiFqnId)){
					continue;
				}
				writer.writeUsage(entityId, apiFqnId, useCountForEntity, isClass(entityId));
			}
		}
	}

	private boolean skipApi(Long apiFqnId) {
		if(fqnsToSkip == null)
			return false;
		else{
			assert apiFqnId <= Integer.MAX_VALUE;
				
			if(fqnsToSkip.get((int)(apiFqnId-1)))
					return true;
			else
				return false;
		}
			
	}

	private int getMinApiUsedByEntityThreshold() {
		if (filter==null)
		return 1;
		else
			return filter.distinctFqnsUsedByEntities;
	}

	private void processUsageResults(Iterator<Map<String, Object>> jarUsage) {
		while (jarUsage.hasNext()) {
			Map<String, Object> usageMap = jarUsage.next();
			// rid, eid, fqn
			Long eid = ((BigInteger) usageMap.get("eid")).longValue();
			String fqn = (String) usageMap.get("fqn");
			String entityType =((String) usageMap.get("etype"));
			updateUsage(eid, fqn, entityType);
		}
	}
	
	private void updateUsage(Long eid, String fqnUsed, String entityType){
		long fqnId = updateFqn(fqnUsed);
		if(entityType.equals("CLASS")) setEidAsClass(eid);
		updateFqnUse(eid, fqnId);
	}


	private void setEidAsClass(Long eid) {
		assert (int)(eid - 1) <= Integer.MAX_VALUE;
		isEntityClass.set((int) (eid -1));
	}
	
	private boolean isClass(Long eid){
		assert (int)(eid - 1) <= Integer.MAX_VALUE;
		return isEntityClass.get((int) (eid -1));
	}

	private void updateFqnUse(Long eid, Long fqnId) {
		if(!usageTable.containsKey(eid)){
			usageTable.put(eid, new Usage(fqnId));
		} else {
			usageTable.get(eid).incrementCount(fqnId);
		}
	}


	private long updateFqn(String fqnUsed) {
		if(fqnTable.containsKey(fqnUsed))
			return fqnTable.get(fqnUsed).longValue();
		else {
			fqnCount = fqnCount + 1;
			fqnTable.put(fqnUsed, fqnCount);
			return fqnCount;
		}
	}
	
	
		
}

class UsageFilter {
	
	public UsageFilter(int distinctFqnsUsedByEntities2, int fqnUseCount2,
			String usedFqnCountFileJdk2, String usedFqnCountFileJars2, String popularFqnsFile2) {
		this.distinctFqnsUsedByEntities = distinctFqnsUsedByEntities2;
		this.fqnUseCount = fqnUseCount2;
		this.usedFqnCountFileJars = usedFqnCountFileJars2;
		this.usedFqnCountFileJdk = usedFqnCountFileJdk2;
		this.popularFqnsFile = popularFqnsFile2;
	}
	
	// only allow entities using more that this # of entities (APIs)
	int distinctFqnsUsedByEntities = 2;
	// only allow APIs that are used more than this number
	int fqnUseCount = 3;
	
	// jdk_fqn, count
	String usedFqnCountFileJdk;
	// jar_fqn, count
	String usedFqnCountFileJars;
	// fqn
	String popularFqnsFile;
	
}

class Usage {
	
	private Hashtable<Long, Integer> usageTable;
	
	public Set<Long> getApiEntityIds(){
		return usageTable.keySet();
	}
	
	public int getApiUsedCount(long apiEntityId){
		return this.usageTable.get(apiEntityId).intValue();
	}
	
	public Usage(long usedFqnId){
		usageTable = new Hashtable<Long, Integer>();
		this.usageTable.put(usedFqnId, 1);
	}
	
	public void incrementCount(Long usedFqnId){
		Integer count = this.usageTable.get(usedFqnId);
		if(count == null) 
			this.usageTable.put(usedFqnId, 1);
		else
			this.usageTable.put(usedFqnId, count + 1);
	}
}