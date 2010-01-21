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

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

import edu.uci.ics.sourcerer.util.io.Logging;
import edu.uci.ics.sourcerer.util.io.Properties;
import edu.uci.ics.sourcerer.util.io.Property;
import edu.uci.ics.sourcerer.util.io.PropertyManager;
import edu.uci.ics.sourcerer.util.io.properties.IntegerProperty;

import static edu.uci.ics.sourcerer.util.io.Logging.logger;

/**
 * @author <a href="bajracharya@gmail.com">Sushil Bajracharya</a>
 * @created Dec 8, 2009
 *
 */
public class UsageWriterRunner {
	
	public static final Property<Integer> MIN_FQNs_USED_BY_ENTITY =
		new IntegerProperty("min-apis", 3, "Usage",
				"Minimum count of distinct FQNs used by an entity to consider it for similarity.").makeOptional();
	public static final Property<Integer> MIN_USES_OF_FQN =
		new IntegerProperty("min-entities", 2, "Usage",
				"Minimum count of distinct users of an API FQN to consider it for including in feature vector.").makeOptional();
	
	public static void main(String[] args) {
		
		
		PropertyManager.initializeProperties(args);
	    Logging.initializeLogger();
	    
	    logger.info("Starting usage writer runner.");
	    
	    PropertyManager.registerAndVerify(MIN_FQNs_USED_BY_ENTITY, MIN_USES_OF_FQN);
	    
	    if(MIN_FQNs_USED_BY_ENTITY.getValue()<1 || MIN_FQNs_USED_BY_ENTITY.getValue()<1){
	    	System.err.println("minimum values should be greater than 1");
	    	PropertyManager.printUsage();
    		System.exit(-1);
	    }
	    
	    PropertyManager.registerAndVerify(DATABASE_URL, DATABASE_USER, DATABASE_PASSWORD);
	    FileUsageWriter writer = new FileUsageWriter(Properties.OUTPUT.getValue().getPath());
	    
	    String input = Properties.INPUT.getValue().getPath();
	    String usedFqnCountFileJdk = input + File.separator + "jdk-usage-by-entity.txt"; 
		String usedFqnCountFileJars = input + File.separator + "jar-usage-by-entity.txt"; 
		String popularFqnsFile = input + File.separator + "popular-fqn.txt";
	    
	    UsageCalculator ucalc = new UsageCalculator();
	    ucalc.setWriter(writer);
	    ucalc.init(
	    		DATABASE_URL.getValue(), 
	    		DATABASE_USER.getValue(), 
	    		DATABASE_PASSWORD.getValue(), 
	    		MIN_FQNs_USED_BY_ENTITY.getValue().intValue(), 
	    		MIN_USES_OF_FQN.getValue().intValue(), 
	    		usedFqnCountFileJdk, 
	    		usedFqnCountFileJars, 
	    		popularFqnsFile);
	    
		try {
			writer.openFiles();
			ucalc.writeUsage();
			writer.closeFiles();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.log(Level.SEVERE, "IOE during writing usage.");
			e.printStackTrace();
			System.exit(-1);
		}
	    
	    logger.info("Done executing usage writer runner.");
	}
	
	
}
