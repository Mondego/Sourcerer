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
package edu.uci.ics.sourcerer.server.similarity;

import static edu.uci.ics.sourcerer.util.io.Logging.logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.similarity.TanimotoCoefficientSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;

import edu.uci.ics.sourcerer.ml.HammingDistanceSimilarity;
import edu.uci.ics.sourcerer.ml.SimilarUserCalculator;
import edu.uci.ics.sourcerer.ml.TasteFileModelWithStringItemIds;
import edu.uci.ics.sourcerer.util.io.DoubleProperty;
import edu.uci.ics.sourcerer.util.io.Property;
import edu.uci.ics.sourcerer.util.io.PropertyManager;
import edu.uci.ics.sourcerer.util.io.properties.IntegerProperty;
import edu.uci.ics.sourcerer.util.io.properties.StringProperty;
import edu.uci.ics.sourcerer.util.server.ServletUtils;

/**
 * @author <a href="bajracharya@gmail.com">Sushil Bajracharya</a>
 * @created Mar 29, 2010
 * 
 */
public class SimilarityServer extends HttpServlet {

	String USAGE_FILE = "";
	String NEIGH_SIZE = "100";
	String THRESHOLD = "0.4";

	SimilarUserCalculator scalcTC = new SimilarUserCalculator(null);
	SimilarUserCalculator scalcHD = new SimilarUserCalculator(null);

	private static final long serialVersionUID = -1360580712603026545L;

	@Override
	public void init() throws ServletException {
		
		USAGE_FILE = getInitParameter("usage-file");
		NEIGH_SIZE = getInitParameter("neigh-size");
		THRESHOLD = getInitParameter("threshold");
		
		try {
			setupCalculator();
		} catch (FileNotFoundException e) {
			logger.log(Level.SEVERE, "Unable to find usage file " + USAGE_FILE);
			// e.printStackTrace();
		} catch (TasteException e) {
			// TODO Auto-generated catch block
			logger.log(Level.SEVERE, "TasteException " + USAGE_FILE);
			logger.log(Level.SEVERE, e.getMessage());
		}

	}

	@Override
	public void destroy() {
		logger.log(Level.INFO, "Destroying");
	}

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		String entityID = request.getParameter("entityID");
		if(entityID==null){
			ServletUtils.writeErrorMsg(response, "Error entityID is null");
			return;
		}
			
		long userId = Long.parseLong(entityID);
		
		if (entityID != null) {
			String simType = request.getParameter("simType");
			long[] simUsers = null;
			if(simType == null){
				ServletUtils.writeErrorMsg(response, "Error : simType is null");
			} else if("HD".equalsIgnoreCase(simType)){
				try {
					simUsers = scalcHD.getSimilarUsers(userId);
				} catch (TasteException e) {
					ServletUtils.writeErrorMsg(response, "Error : " + e.getMessage());
					// e.printStackTrace();
				}
			} else if ("TC".equalsIgnoreCase(simType)){
				try {
					simUsers = scalcTC.getSimilarUsers(userId);
				} catch (TasteException e) {
					ServletUtils.writeErrorMsg(response, "Error : " + e.getMessage());
					// e.printStackTrace();
				}
			}
//				else if ("TFIDF".equalsIgnoreCase(simType)){
//				ServletUtils.writeErrorMsg(response, "Error, not yet supported");
//			}
			
			if(simUsers==null || simUsers.length<1){
				ServletUtils.writeErrorMsg(response, "Error : No similar users found");
			} else{
				ServletUtils.writeEntityIds(response, simUsers);
			}
			
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

}
