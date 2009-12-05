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
package edu.uci.ics.sourcerer.ml;

import java.io.File;
import java.io.FileNotFoundException;

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

import edu.uci.ics.sourcerer.ml.db.tools.ISimilarityWriter;

import junit.framework.TestCase;

/**
 * @author <a href="mailto:bajracharya@gmail.com">Sushil Bajracharya</a>
 */
public class SimilarUserCalculatorTest extends TestCase{
	
	SimilarUserCalculator suCalc = new SimilarUserCalculator(new ConsoleWriter());
	String dataFileLocation = "test/resources/2jdk.txt";
	long startTime;
	
	@Before
	public void setUp() throws FileNotFoundException, TasteException{
		startTime = System.currentTimeMillis();
		File dataFile = new File(dataFileLocation);
		DataModel dm = new TasteFileModelWithStringItemIds(dataFile);
		suCalc.setDataModel(dm);
		suCalc.setNeighborhoodSize(10);
		suCalc.setSimilarityThreshold(0.01);
		
//		 suCalc.setUserSimilarity(
//				 //new CachingUserSimilarity(
//						 new TanimotoCoefficientSimilarity(dm)
//				//		 , dm)
//				 ) ;
		 
		suCalc.setUserSimilarity(new HammingDistanceSimilarity(dm));
		// suCalc.setUserSimilarity(new PearsonCorrelationSimilarity(dm));
		// suCalc.setUserSimilarity(new LogLikelihoodSimilarity(dm));
	}
	
	@Test
	public void testCalculateSimilarUsers() throws TasteException{
		suCalc.calculate();
	}
	
//	@Test
//	public void testSimilarUsers() throws TasteException{
//		long userid = 
//			583498
//					// 98468
//					// 4278081
//					// 3316574
//					//	4137205
//					// 3321880;
//		;
//		suCalc.setNeighborhoodSize(10);
//		long[] users = suCalc.getSimilarUsers(userid);
//		
//		System.out.print(userid);
//		for(long u : users){
//			System.out.print("," + u);
//		}
//		System.out.println();
//	}
	
	@After
	public void tearDown(){
		System.out.println("Time Elapsed (hh:mm:ss) = " + Util.formatMs(System.currentTimeMillis()-startTime));
	}
}

class ConsoleWriter implements ISimilarityWriter{

	@Override
	public void writeSimilarty(String lhsEid, String rhsEid, String similarity) {
		System.out.println(lhsEid + ", " + rhsEid + ", " + similarity);
	}
	
}
