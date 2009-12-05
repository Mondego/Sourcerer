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

/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// Based on:
// package org.apache.mahout.cf.taste.example.bookcrossing;

import org.apache.mahout.cf.taste.common.Refreshable;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.common.Weighting;
import org.apache.mahout.cf.taste.impl.neighborhood.NearestNUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.CachingRecommender;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.EuclideanDistanceSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.LogLikelihoodSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.TanimotoCoefficientSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.cf.taste.recommender.Rescorer;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;

import edu.uci.ics.sourcerer.db.util.ManualPoolingMysqlDataSourceFactory;
import edu.uci.ics.sourcerer.ml.db.MySqlTanimotoCoefficientUserSimilarity;

import java.util.Collection;
import java.util.List;

/**
 * A simple {@link Recommender} implemented for the Book Crossing demo. See the
 * <a href="http://www.informatik.uni-freiburg.de/~cziegler/BX/">Book Crossing
 * site</a>.
 */
public final class ApiRecommender implements Recommender {

	private final Recommender recommender;

	public ApiRecommender(DataModel bcModel) throws TasteException {
		
//		UserSimilarity similarity = new EuclideanDistanceSimilarity(bcModel,Weighting.WEIGHTED);
		//UserSimilarity similarity = new LogLikelihoodSimilarity(bcModel);
		//UserSimilarity similarity = new PearsonCorrelationSimilarity(bcModel);

		UserSimilarity similarity = new TanimotoCoefficientSimilarity(bcModel);
		
//		UserSimilarity similarity = new HammingDistanceSimilarity(bcModel);

		UserNeighborhood neighborhood = new NearestNUserNeighborhood(
				15, 
				0.4,
				similarity, 
				bcModel, 
				1.0);
		recommender = new CachingRecommender(new GenericUserBasedRecommender(
				bcModel, neighborhood, similarity));
	}

	@Override
	public List<RecommendedItem> recommend(long userID, int howMany)
			throws TasteException {
		return recommender.recommend(userID, howMany);
	}

	@Override
	public List<RecommendedItem> recommend(long userID, int howMany,
			Rescorer<Long> rescorer) throws TasteException {
		return recommender.recommend(userID, howMany, rescorer);
	}

	@Override
	public float estimatePreference(long userID, long itemID)
			throws TasteException {
		return recommender.estimatePreference(userID, itemID);
	}

	@Override
	public void setPreference(long userID, long itemID, float value)
			throws TasteException {
		recommender.setPreference(userID, itemID, value);
	}

	@Override
	public void removePreference(long userID, long itemID)
			throws TasteException {
		recommender.removePreference(userID, itemID);
	}

	@Override
	public DataModel getDataModel() {
		return recommender.getDataModel();
	}

	@Override
	public void refresh(Collection<Refreshable> alreadyRefreshed) {
		recommender.refresh(alreadyRefreshed);
	}

	@Override
	public String toString() {
		return "ApiRecommender[recommender:" + recommender + ']';
	}

}