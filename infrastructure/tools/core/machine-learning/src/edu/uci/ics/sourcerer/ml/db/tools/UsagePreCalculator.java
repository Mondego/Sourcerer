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
		CLASS, METHOD, CLASS_METHOD;
		
		public static String allElementNames(){
			StringBuffer buf = new StringBuffer();
			for(EntityType e: EntityType.values()){
				buf.append(e.toString());
				buf.append(" ");
			}
			
			return buf.toString();
		}
	}
	
	EntityType optSelectedType  = EntityType.CLASS_METHOD;
	boolean optExcludeInternal = true;
	
	JdbcDataSource ds = new JdbcDataSource();

	final String PREDICATE_ENTITY_TYPE_CM = " in ('CLASS','METHOD','CONSTRUCTOR') ";
	final String PREDICATE_ENTITY_TYPE_METHOD = " in ('METHOD','CONSTRUCTOR') ";
	final String PREDICATE_ENTITY_TYPE_CLASS  = " = 'CLASS' ";
	final String PLACEHOLDER_ENTITY_TYPE = "!!ETYPE!!";
	
	final String PREDICATE_EXCLUDE_INTERNAL = " and R.internal=0 ";
	final String PREDICATE_INCLUDE_INTERNAL = "";
	final String PLACEHOLDER_INTERNAL = "!!INTERNAL!!";
	
	// SQL used to get this list select project_id from projects where hash in (select hash from files where project_id='20671') union select project_id from projects where project_type='CRAWLED';
	// computed from cerkbd, therefore will only work on that or clones of that db
	final String PROJECT_IDS = " (10135,10112,10077,9956,9843,9831,9879,9824,9812,9835,9809,9798,9742,9708,9720,10210,10270,10295,10322,10347,10400,10440,10519,10536,10552,10570,10593,10599,10612,10646,10653,10700,10708,10724,10739,10782,10835,10872,10876,10892,10917,10944,11077,11110,11135,11148,11181,11193,11241,11245,11272,11330,11349,11388,11468,11501,11510,11514,11547,11623,11635,11760,11813,11872,11876,11882,11884,11890,11926,11949,11967,11981,11992,12029,12046,12052,12076,12130,12134,12160,12179,12194,12218,12232,12272,12299,12307,12384,12399,12438,12472,12477,12714,12719,12760,12784,12812,12853,12866,12921,12941,12946,12957,12979,13049,13091,13105,13114,13138,13152,13172,13202,13231,13238,13243,13368,13373,13513,13520,13527,13564,13568,13590,13649,13703,13804,13834,13856,13898,13916,13945,14054,14056,14125,14137,14141,14184,14237,14265,14276,14357,14411,14455,14513,14535,14538,14608,14639,14662,14708,14733,14763,14814,14820,14842,14880,14899,14902,14924,14927,14945,14950,15142,15148,15151,15154,15174,15177,15194,15250,15275,15304,15315,15323,15335,15347,15390,15437,15453,15465,15478,15481,15498,15500,15513,15518,15526,15603,15630,15689,15690,15755,15766,15771,15815,15827,15998,19931,19964,20156,20037,19984,20637,20162,20163,20138,20648,20079,20582,20574,19888,20089,20059,19893,19906,20341,20189,20190,19930,20639,20225,20063,20047,19907,19900,19882,20126,20062,19909,19939,20551,20543,20554,20539,20549,20548,20537,20552,20555,20544,20540,20533,20553,20538,20531,20530,20528,20526,20521,20519,20558,20524,20523,20516,20520,20513,20560,20511,20515,20514,20509,20508,20547,20546,20545,20505,20507,20542,20536,20534,20563,20499,20498,20497,20500,20529,20565,20557,20495,20493,20525,20491,20490,20564,20488,20486,20484,20487,20481,20535,20480,20479,20506,20504,20562,20503,20476,20502,20475,20527,20473,20496,20522,20571,20469,20518,20492,20573,20466,20517,20566,20489,20567,20485,20512,20460,20462,20575,20561,20482,20457,20459,20456,20458,20455,20453,20477,20451,20576,20577,20450,20474,20446,20550,20570,20578,20445,20472,20444,20442,20465,20440,20463,20579,20494,20438,20541,20461,20436,20580,20434,20581,20432,20483,20429,20428,20532,20427,20426,20449,20478,20422,20425,20420,20418,20584,20585,20441,20421,20556,20417,20471,20439,20413,20414,20412,20467,20435,20464,20411,20409,20587,20586,20405,20431,20403,20408,20401,20407,20404,20400,20402,20397,20510,20424,20399,20395,20396,20394,20452,20392,20591,20390,20388,20391,20448,20387,20501,20386,20447,20385,20384,20381,20383,20382,20379,20377,20594,20376,20380,20372,20437,20589,20370,20373,20433,20369,20590,20368,20393,20363,20364,20389,20367,20430,20361,20357,20362,20360,20354,20583,20359,20358,20423,20599,20356,20353,20351,20378,20374,20349,20419,20596,20348,20371,20415,20346,20344,20342,20416,20366,20597,20470,20340,20600,20410,20339,20338,20406,20468,20337,20336,20335,20333,20355,20331,20398,20329,20352,20604,20327,20603,20350,20454,20324,20345,20322,20321,20343,20605,20592,20614,20317,20320,20319,20315,20602,20443,20334,20313,20312,20332,20310,20375,20311,20309,20328,20365,20306,20305,20304,20302,20609,20301,20325,20300,20299,20323,20610,20298,20295,20598,20292,20606,20611,20316,20314,20291,20289,20290,20288,20308,20347,20286,20613,20287,20608,20303,20284,20559,20280,20279,20277,20615,20297,20296,20273,20612,20276,20272,20616,20601,20271,20293,20269,20588,20268,20266,20267,20617,20262,20261,20618,20282,20263,20620,20330,20257,20619,20260,20256,20259,20255,20258,20252,20254,20326,20274,20251,20270,20250,20246,20265,20245,20247,20243,20264,20241,20244,20242,20318,20238,20240,20236,20623,20235,20234,20232,20237,20624,20233,20607,20621,20230,20622,20228,20231,20227,20625,20307,20226,20229,20222,20224,20219,20221,20627,20215,20294,20218,20214,20213,20211,20210,20595,20209,20208,20207,20206,20593,20204,20629,20285,20203,20283,20205,20201,20149,20281,20148,20200,20278,20199,20146,20145,20275,20643,20144,20143,20196,20142,20141,20194,20139,20193,20137,20253,20192,20136,20191,20135,20249,20248,20568,20634,20131,20569,20130,20129,20188,20128,20239,20127,20572,20187,20124,20186,20647,20123,20122,20121,20185,20119,20184,20183,20117,20649,20115,20223,20182,20114,20181,20220,20113,20112,20180,20217,20109,20216,20652,20108,20212,20179,20106,20105,20104,20103,20178,20177,20101,20176,20098,20099,20202,20626,20097,20095,20175,20093,20174,20091,20628,20090,20198,20632,20088,20631,20197,20086,20173,20084,20635,20671,20172,20633,20082,20195,20081,20657,20080,20658,20078,20171,20076,20170,20636,20075,20169,20672,20072,20071,20168,20069,20167,19886,20638,20068,20166,20066,20165,20061,20164,20058,20057,20056,20055,20054,20053,20662,20052,20640,20051,20049,20161,20160,20048,20046,20045,20159,20644,20641,20043,20158,20042,20666,20645,20157,20038,20667,20155,20646,20036,20154,20153,20034,20033,20032,20031,20152,20654,20642,20029,20656,20151,20028,20027,20026,20655,20024,20023,20022,20150,20147,20019,20133,20659,20018,20017,20660,20016,20140,20015,20674,20014,20013,20012,20134,20675,20132,20011,20010,20009,20673,20008,20007,20661,20006,20125,19993,20005,20120,20004,20118,20116,20003,20002,20001,20677,20676,20111,20110,20000,19999,19998,20107,19991,19997,19996,19995,20651,20678,19994,20102,20668,19992,20100,20679,20669,19990,20096,20094,19989,19988,20092,19987,20630,19986,20087,20670,20085,19985,19981,20083,19983,19982,19977,19980,19979,20077,19978,20074,20073,19976,20070,20682,19975,20067,19974,19973,19972,20683,20065,19971,20064,19970,19969,20060,19968,20680,20663,19967,19966,20681,19965,20685,19963,20664,19962,20050,19961,19960,20665,20044,19959,19958,19957,20041,20040,19956,20039,19955,19954,19953,20684,19952,19951,20035,19950,19949,20650,19948,20653,20030,19947,19946,20025,19945,19944,20021,19943,20020,19942,19941,19940,19938,19937,19936,19935,19934,19933,19932,19929,19928,19927,19926,19925,19924,19923,19922,19921,19920,19919,19918,19917,19916,19915,19914,19913,19912,19911,19910,19908,19905,19904,19903,19902,19901,19877,19899,19896,19878,19898,19881,19897,19895,19894,19883,19892,19891,19884,19890,19889,19887,19885,19880,19879,19876)";
	
	String SQL_ALL_USAGE = "select userE.entity_id as eid, provE.fqn as fqn"
			+ " from relations as R"
			+ " inner join entities as userE on userE.entity_id=R.lhs_eid"
			+ " inner join entities as provE on provE.entity_id=R.rhs_eid"
			+ " where " 
			+ " R.relation_type in ('CALLS','EXTENDS','IMPLEMENTS','INSTANTIATES','USES', 'OVERRIDES')"
			+ " and userE.entity_type " + PLACEHOLDER_ENTITY_TYPE
			+ PLACEHOLDER_INTERNAL
			+ " and userE.project_id in "
			+ PROJECT_IDS; 
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
		p.put("batchSize", "-1");
		p.put("queryTimeout", "0");
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
		} else if(optSelectedType == EntityType.CLASS_METHOD) {
			SQL_ALL_USAGE = SQL_ALL_USAGE.replaceAll(PLACEHOLDER_ENTITY_TYPE, PREDICATE_ENTITY_TYPE_CM);
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
