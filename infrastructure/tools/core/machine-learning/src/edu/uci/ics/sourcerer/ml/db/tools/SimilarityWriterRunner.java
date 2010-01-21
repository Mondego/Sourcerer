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

import static edu.uci.ics.sourcerer.db.util.DatabaseConnection.DATABASE_PASSWORD;
import static edu.uci.ics.sourcerer.db.util.DatabaseConnection.DATABASE_URL;
import static edu.uci.ics.sourcerer.db.util.DatabaseConnection.DATABASE_USER;

import java.io.FileNotFoundException;

import org.apache.mahout.cf.taste.common.TasteException;

import edu.uci.ics.sourcerer.db.util.DatabaseConnection;
import edu.uci.ics.sourcerer.ml.Similarities;
import edu.uci.ics.sourcerer.util.io.DoubleProperty;
import edu.uci.ics.sourcerer.util.io.Logging;
import edu.uci.ics.sourcerer.util.io.LongProperty;
import edu.uci.ics.sourcerer.util.io.Property;
import edu.uci.ics.sourcerer.util.io.PropertyManager;
import edu.uci.ics.sourcerer.util.io.SimilaritiesOptionProperty;
import edu.uci.ics.sourcerer.util.io.properties.BooleanProperty;
import edu.uci.ics.sourcerer.util.io.properties.IntegerProperty;
import edu.uci.ics.sourcerer.util.io.properties.StringProperty;

/**
 * @author <a href="bajracharya@gmail.com">Sushil Bajracharya</a>
 * @created Nov 30, 2009
 *
 */
public class SimilarityWriterRunner {
	
	public static final Property<Double> SIM_THRESHOLD = 
		new DoubleProperty("sim-threshold", 0.1, "SimDatabase", 
				"Similarity threshold.").makeOptional();
	public static final Property<String> FQN_USE_FILE = 
		new StringProperty("fqn-use-file", null, "SimDatabase", 
				"Path to fqn use file.");
	public static final Property<Integer> NEIGHBORHOOD_SIZE =
		new IntegerProperty("n-size", 45, "SimDatabase",
				"Neighborhood size.").makeOptional();
	public static final Property<Boolean> FQN_USE_FROM_DB =
		new BooleanProperty("fqn-use-from-db", false, "SimDatabase",
				"Set true to load fqn use model from database.").makeOptional();
	public static final Property<Long> LOW_ID =
		new LongProperty("low-id", -1l, "SimDatabase",
				"Lower value for entity id in the range of entities, whose similarities will be written.").makeOptional();
	public static final Property<Long> HIGH_ID =
		new LongProperty("high-id", -1l, "SimDatabase",
				"Higher value for entity id in the range of entities, whose similarities will be written.").makeOptional();
	public static final Property<Similarities> SIMILARITY_TYPE =
		new SimilaritiesOptionProperty("similarity", Similarities.TANIMOTO_COEFFICIENT, "SimDatabase",
				"Types of similarity. One of: "+  Similarities.allElementNames() + ".").makeOptional();
	
	public static void main(String[] args) {
	    PropertyManager.initializeProperties(args);
	    Logging.initializeLogger();
	    
	    PropertyManager.registerAndVerify(SIM_THRESHOLD, NEIGHBORHOOD_SIZE, LOW_ID, HIGH_ID, SIMILARITY_TYPE);
	    PropertyManager.registerAndVerify(DATABASE_URL, DATABASE_USER, DATABASE_PASSWORD);
	    PropertyManager.registerAndVerify(FQN_USE_FILE);
	    
	    if(! (LOW_ID.getValue()==-1 && HIGH_ID.getValue()==-1) ){
	    	if (LOW_ID.getValue()<=-1 || HIGH_ID.getValue()<=-1 || HIGH_ID.getValue() <= LOW_ID.getValue() ) {
	    		System.err.println("Either both " 
	    				+ LOW_ID.getName() + " and " + HIGH_ID.getName() + " needs to be -1 (default) or both greater than -1."
	    				+ " If both are greater than -1, " + HIGH_ID.getName() + " needs to be greater than " + LOW_ID.getName());
	    		PropertyManager.printUsage();
	    		System.exit(-1);
	    	}
	    }
	    
	    DatabaseConnection connection = new DatabaseConnection();
	    connection.open();

	    if (!FQN_USE_FROM_DB.getValue()) {
	      
	    } else {
	    	throw new RuntimeException("Importing model from DB not supported yet");
	    }
	    
	    DBSimilarityWriter writer = null;
	    try {
			
	    	switch (SIMILARITY_TYPE.getValue()) {
	    	case HAMMING_DISTANCE:
	    		writer = 
					new HammingDistanceDBSimilarityWriter(
							FQN_USE_FILE.getValue(), 
							NEIGHBORHOOD_SIZE.getValue(), 
							SIM_THRESHOLD.getValue(),
							LOW_ID.getValue(),
							HIGH_ID.getValue(),
							connection);
	    		break;
	    		
	    	case TANIMOTO_COEFFICIENT:
	    		writer = 
					new TanimotoCoefficientDBSimilarityWriter(
							FQN_USE_FILE.getValue(), 
							NEIGHBORHOOD_SIZE.getValue(), 
							SIM_THRESHOLD.getValue(),
							LOW_ID.getValue(),
							HIGH_ID.getValue(),
							connection);
	    		break;
	    	default:
	    		System.err.println("Invalid selection for " + SIMILARITY_TYPE.getName());
	    		PropertyManager.printUsage();
	    		System.exit(-1);
	    	}
	    	
	    	
		} catch (FileNotFoundException e) {
			// TODO Log this
			e.printStackTrace();
		} catch (TasteException e) {
			// TODO Log this
			e.printStackTrace();
		}
	    
		if(writer!=null){
			writer.initializeSimilarityTable();
			try {
				writer.startWrite();
			} catch (TasteException e) {
				// TODO Log this
				e.printStackTrace();
			};
		}

	    connection.close();
	    
	    String range = " all entities";
	    if(LOW_ID.getValue()>-1 & HIGH_ID.getValue()>-1) 
	    	range = " entitiy ids in range (" + LOW_ID.getValue() + " - " + HIGH_ID.getValue() + ")";
	    
	    System.out.println("Done executing similarity writer for" + range);
	  }
}
