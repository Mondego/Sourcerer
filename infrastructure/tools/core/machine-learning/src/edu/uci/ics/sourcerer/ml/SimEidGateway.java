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
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.similarity.TanimotoCoefficientSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;

/**
 * @author <a href="bajracharya@gmail.com">Sushil Bajracharya</a>
 * @created Apr 14, 2010
 * 
 */
public class SimEidGateway {

	Logger logger = Logger.getLogger(this.getClass().getName());

	private String USAGE_FILE;
	private String NEIGH_SIZE;
	private String THRESHOLD;

	private SimilarUserCalculator scalcTC = new SimilarUserCalculator(null);
	private SimilarUserCalculator scalcHD = new SimilarUserCalculator(null);

	private SimEidGateway() {
		USAGE_FILE = System
				.getProperty("sourcerer.sim.usagefile",
						"/home/cerk/skb/sourcerertools/data/entity_eachfqn_usage_filtered.txt");
		NEIGH_SIZE = System.getProperty("sourcerer.sim.neighsize", "100");
		THRESHOLD = System.getProperty("sourcerer.sim.threshold", "0.4");

		try {
			logger.log(Level.INFO, "Setting up similarity calculator");
			setupCalculator();
			warmup();
		} catch (FileNotFoundException e) {
			logger.log(Level.SEVERE, "Unable to find usage file " + USAGE_FILE);
		} catch (TasteException e) {
			logger.log(Level.SEVERE, "TasteException while opening " + USAGE_FILE);
		}

	}

	private void warmup() {

		String seed = System.getProperty("sourcerer.sim.seedeid", "45138875");
		logger.log(Level.INFO,
				"Warming up similarity calculator using seed entityId " + seed);

		String result = _eidsViaSimEntitiesHD(seed);

		if (result.startsWith("Error")) {
			logger.log(Level.WARNING,
					"could not get simlar (HD) entity ids for seed entityId: "
							+ seed);
		} else {
			logger.log(Level.INFO,
					"got simlar (HD) entity ids for seed entityId: " + seed);
		}

		result = _eidsViaSimEntitiesTC(seed);

		if (result.startsWith("Error")) {
			logger.log(Level.WARNING,
					"could not get simlar (HD) entity ids for seed entityId: "
							+ seed);
		} else {
			logger.log(Level.INFO,
					"got simlar (HD) entity ids for seed entityId: " + seed);
		}

	}

	private void setupCalculator() throws FileNotFoundException, TasteException {

		File dataFile = new File(USAGE_FILE);
		DataModel dm = new TasteFileModelWithStringItemIds(dataFile);

		scalcTC.setDataModel(dm);
		scalcTC.setNeighborhoodSize(new Integer(NEIGH_SIZE).intValue());
		scalcTC.setSimilarityThreshold(new Double(THRESHOLD).doubleValue());
		scalcTC.setUserSimilarity(new TanimotoCoefficientSimilarity(dm));

		scalcHD.setDataModel(dm);
		scalcHD.setNeighborhoodSize(new Integer(NEIGH_SIZE).intValue());
		scalcHD.setSimilarityThreshold(new Double(THRESHOLD).doubleValue());
		scalcHD.setUserSimilarity(new HammingDistanceSimilarity(dm));

	}

	private String _eidsViaSimEntitiesTC(String entityID) {
		long userId = Long.parseLong(entityID);

		String eids = "";

		try {
			eids = makeEids(scalcTC.getSimilarUsers(userId));
			logger.log(Level.FINEST, "got similar eids (TC) for entityID:" + entityID);
		} catch (TasteException e) {
			logger.log(Level.FINE, "TasteException (TC) for entityID:" + entityID);
		}

		return eatError(eids);

	}

	private String _eidsViaSimEntitiesHD(String entityID) {
		long userId = Long.parseLong(entityID);

		String eids = "";

		try {
			eids = makeEids(scalcHD.getSimilarUsers(userId));
			logger.log(Level.FINEST, "got similar eids (HD) for entityID:" + entityID);
		} catch (TasteException e) {
			logger.log(Level.FINE, "TasteException (HD) for entityID:" + entityID);
		}

		return eatError(eids);
	}

	private static SimEidGateway instance = null;

	private static SimEidGateway getInstance() {
		if (instance == null) {
			instance = new SimEidGateway();
		}
		return instance;
	}

	private String makeEids(long[] eids) {
		if (eids == null || eids.length < 1) {
			return "Error : no entity ids to write";

		}

		StringBuilder sb = new StringBuilder();
		for (long eid : eids) {
			sb.append(eid);
			sb.append(",");
		}
		String eidsStr = sb.substring(0, sb.length() - 1);
		return eidsStr;
	}

	private String eatError(String result) {
		String eids = result;

		if (eids == null) {
			logger.log(Level.SEVERE,
					"Encountered null eids (which seems to be impossible). ");
			return "''";
		}

		if (eids.length() < 1 || eids.startsWith("Error"))
			return "''";

		return eids;
	}

	// --- APIS ----

	public static String eidsViaSimEntitiesTC(String entityId) {

		return getInstance()._eidsViaSimEntitiesTC(entityId);
	}

	public static String eidsViaSimEntitiesHD(String entityId) {
		return getInstance()._eidsViaSimEntitiesHD(entityId);
	}

}
