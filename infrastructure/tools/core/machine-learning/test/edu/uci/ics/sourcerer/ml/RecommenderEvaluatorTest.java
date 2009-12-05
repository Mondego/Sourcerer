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

import junit.framework.TestCase;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.eval.IRStatistics;
import org.apache.mahout.cf.taste.eval.RecommenderBuilder;
import org.apache.mahout.cf.taste.eval.RecommenderIRStatsEvaluator;
import org.apache.mahout.cf.taste.impl.eval.GenericRecommenderIRStatsEvaluator;
import org.apache.mahout.cf.taste.impl.recommender.NearestNeighborClusterSimilarity;
import org.apache.mahout.cf.taste.impl.recommender.RandomRecommender;
import org.apache.mahout.cf.taste.impl.recommender.TreeClusteringRecommender;
import org.apache.mahout.cf.taste.impl.recommender.TreeClusteringRecommender2;
import org.apache.mahout.cf.taste.impl.recommender.slopeone.SlopeOneRecommender;
import org.apache.mahout.cf.taste.impl.recommender.svd.SVDRecommender;
import org.apache.mahout.cf.taste.impl.similarity.TanimotoCoefficientSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;

/**
 * @author <a href="mailto:bajracharya@gmail.com">Sushil Bajracharya</a>
 */
public class RecommenderEvaluatorTest extends TestCase {

	private static final double EPSILON = 0.00001;
	String dataFileLocation = "test/resources/2jdk.txt";

	public void testEvaluate() throws Exception {
		DataModel model = getDataModel();
		
		RecommenderBuilder builder = new RecommenderBuilder() {
			@Override
			public Recommender buildRecommender(DataModel dataModel)
					throws TasteException {
				
				// return new SlopeOneRecommender(dataModel);
				
				// return new SVDRecommender(dataModel, 25, 100);
				
				//return new RandomRecommender(dataModel);
				
				return new ApiRecommender(dataModel);
				
//				return new TreeClusteringRecommender(
//						dataModel,
//						new NearestNeighborClusterSimilarity(
//								//UserSimilarity similarity = new EuclideanDistanceSimilarity(bcModel,Weighting.WEIGHTED);
//								//UserSimilarity similarity = new LogLikelihoodSimilarity(bcModel);
//								//UserSimilarity similarity = new PearsonCorrelationSimilarity(bcModel);
//								new TanimotoCoefficientSimilarity(dataModel)		
//						),
//						0.5);
			}
		};

		RecommenderIRStatsEvaluator evaluator = new GenericRecommenderIRStatsEvaluator();
		IRStatistics stats = evaluator.evaluate(
				builder, 
				null, 
				model, 
				null, 
				2,
				1.0, 
				1.0);
		
		System.out.println(stats.getPrecision() + ", "
				+ stats.getRecall() + ", "
				+ stats.getF1Measure());
		
//		assertEquals(0.75, stats.getPrecision(), EPSILON);
//		assertEquals(0.75, stats.getRecall(), EPSILON);
//		assertEquals(0.75, stats.getF1Measure(), EPSILON);
	}

	private DataModel getDataModel() throws FileNotFoundException {

		File dataFile = new File(dataFileLocation);
		DataModel dm = new TasteFileModelWithStringItemIds(dataFile);
		return dm;
	}

}