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

import java.io.File;
import java.io.FileNotFoundException;
import java.math.BigInteger;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.similarity.CachingUserSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.LogLikelihoodSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.TanimotoCoefficientSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import edu.uci.ics.sourcerer.db.util.JdbcDataSource;
import edu.uci.ics.sourcerer.ml.HammingDistanceSimilarity;
import edu.uci.ics.sourcerer.ml.SimilarUserCalculator;
import edu.uci.ics.sourcerer.ml.TasteFileModelWithStringItemIds;
import edu.uci.ics.sourcerer.ml.db.tools.ISimilarityWriter;
import edu.uci.ics.sourcerer.util.TimeUtil;

import junit.framework.TestCase;

/**
 * @author <a href="mailto:bajracharya@gmail.com">Sushil Bajracharya</a>
 */
public class SimilarUserCalculatorTest extends TestCase {

	SimilarUserCalculator suCalc = new SimilarUserCalculator(
			new ConsoleSimilarityWriter());
	SimilarUserCalculator suCalc2 = new SimilarUserCalculator(
			new ConsoleSimilarityWriter());
	
	String dataFileLocation = "test/data.m.usage/entity_eachfqn_usage_filtered.txt";
	long startTime;

	@Before
	public void setUp() throws FileNotFoundException, TasteException {
		startTime = System.currentTimeMillis();
		File dataFile = new File(dataFileLocation);
		DataModel dm = new TasteFileModelWithStringItemIds(dataFile);
		suCalc.setDataModel(dm);
		suCalc.setNeighborhoodSize(100);
		suCalc.setSimilarityThreshold(0.3);
		suCalc.setUserSimilarity(new TanimotoCoefficientSimilarity(dm));
		
		suCalc2.setDataModel(dm);
		suCalc2.setNeighborhoodSize(100);
		suCalc2.setSimilarityThreshold(0.3);
		suCalc2.setUserSimilarity(new HammingDistanceSimilarity(dm));
		
		// suCalc.setUserSimilarity(new HammingDistanceSimilarity(dm));
		// suCalc.setUserSimilarity(new PearsonCorrelationSimilarity(dm));
		// suCalc.setUserSimilarity(new LogLikelihoodSimilarity(dm));
		System.out.println("Setup time (hh:mm:ss) = "
				+ TimeUtil.formatMs(System.currentTimeMillis() - startTime));
		startTime = System.currentTimeMillis();

	}

//	// @Test
//	public void testCalculateSimilarUsers() throws TasteException {
//		// suCalc.calculate();
//	}

	@Test
	public void testMany() throws TasteException {

		class T implements Runnable {
			int j;
			final SimilarUserCalculatorTest t = SimilarUserCalculatorTest.this;

			public T(int j2) {
				this.j = j2;
			}

			@Override
			public void run() {
				try {
					t.runSim(45138541 + j);
				} catch (TasteException e) {
					System.err.println((long)(45138541 + j) + "not found");
				}

			}
		};

		for (int i = 0; i < 10000; i++) {
			Runnable t = new T(i);
			t.run();
		}
	}

//	@Test
//	public void testSimilarUsers() throws TasteException {
//
//		long[] uids = new long[] { 45138541, 44069789, 43509450, 43509451,
//				43509452 };
//
//		for (long uid : uids) {
//
//			runSim(uid);
//
//		}
//	}

	/**
	 * @param uid
	 * @throws TasteException
	 */
	private void runSim(long uid) throws TasteException {
		startTime = System.currentTimeMillis();
		
		long[] users = suCalc.getSimilarUsers(uid);
		
		System.out.println("\n TC sim calculated in (hh:mm:ss) = "
				+ TimeUtil.formatMs(System.currentTimeMillis() - startTime));

		System.out.println("user# " + uid);
		System.out.print(uid);
		for (long u : users) {
			System.out.print("," + u);
		}
		
		
		users = suCalc2.getSimilarUsers(uid);
		
		System.out.println("\n HD sim calculated in (hh:mm:ss) = "
				+ TimeUtil.formatMs(System.currentTimeMillis() - startTime));

		System.out.println("user# " + uid);
		System.out.print(uid);
		for (long u : users) {
			System.out.print("," + u);
		}
		
	}

	@After
	public void tearDown() {
		System.out.println("Post Calculation Time (hh:mm:ss) = "
				+ TimeUtil.formatMs(System.currentTimeMillis() - startTime));
	}
}

class ConsoleSimilarityWriter implements ISimilarityWriter {

	String sql = "select fqn from entities where entity_id in (";
	StringBuffer buf = new StringBuffer(sql);

	String oldEid = "";

	JdbcDataSource ds = new JdbcDataSource();

	public ConsoleSimilarityWriter() {
		Properties p = new Properties();
		p.put("driver", "com.mysql.jdbc.Driver");
		p.put("url", "jdbc:mysql://tagus.ics.uci.edu:3306/sourcerer_eclipse");
		p.put("user", System.getProperty("sourcerer.db.user"));
		p.put("password", System.getProperty("sourcerer.db.password"));
		ds.init(p);

	}

	@Override
	public void writeSimilarty(String lhsEid, String rhsEid, String similarity) {
		// System.out.println(lhsEid + ", " + rhsEid + ", " + similarity);
		// System.out.println(oldEid);

		if (oldEid.equals(lhsEid)) {
			// keep adding
			buf.append(rhsEid);
			buf.append(",");

		} else {
			// run query
			if (!oldEid.equals("")) {

				String _sql = buf.toString().substring(0, buf.length() - 1);
				printEntities(_sql + ")");
				buf = new StringBuffer(sql);
				buf.append(lhsEid);
				buf.append(",");
				System.out.println("----");
			}

		}

		oldEid = lhsEid;
	}

	private void printEntities(String sql2) {
		Iterator<Map<String, Object>> eItr = ds.getData(sql2);
		while (eItr.hasNext()) {
			Map<String, Object> usageMap = eItr.next();
			// rid, eid, fqn
			// Long eid = ((BigInteger) usageMap.get("eid")).longValue();
			String fqn = (String) usageMap.get("fqn");
			System.out.println(fqn);

		}

	}

}
