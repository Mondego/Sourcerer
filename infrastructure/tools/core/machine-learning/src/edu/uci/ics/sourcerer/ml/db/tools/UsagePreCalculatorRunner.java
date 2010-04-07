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
import static edu.uci.ics.sourcerer.util.io.Logging.logger;

import java.io.IOException;
import java.util.logging.Level;

import edu.uci.ics.sourcerer.ml.Similarities;
import edu.uci.ics.sourcerer.util.io.EntityTypeOptionProperty;
import edu.uci.ics.sourcerer.util.io.Logging;
import edu.uci.ics.sourcerer.util.io.Properties;
import edu.uci.ics.sourcerer.util.io.Property;
import edu.uci.ics.sourcerer.util.io.PropertyManager;
import edu.uci.ics.sourcerer.util.io.SimilaritiesOptionProperty;
import edu.uci.ics.sourcerer.util.io.properties.BooleanProperty;

/**
 * @author <a href="bajracharya@gmail.com">Sushil Bajracharya</a>
 * @created Mar 24, 2010
 *
 */
public class UsagePreCalculatorRunner {

	public static final Property<UsagePreCalculator.EntityType> ENTITY_TYPE  =
		new EntityTypeOptionProperty("entity-type", UsagePreCalculator.EntityType.METHOD, "UsageCalculator",
				"Types of entities. One of: "+  UsagePreCalculator.EntityType.allElementNames() + ".").makeOptional();
	public static final Property<Boolean> EXCLUDE_INTERNAL =
		new BooleanProperty("exclude-internal", true, "UsageCalculator",
				"Set false to include local relations in usage.").makeOptional();
	
	public static void main(String[] args) {
		
	
		PropertyManager.initializeProperties(args);
	    Logging.initializeLogger();
	    
	    logger.info("Starting usage preCalculator runner.");
	    
	    PropertyManager.registerAndVerify(ENTITY_TYPE, EXCLUDE_INTERNAL);
	    PropertyManager.registerAndVerify(DATABASE_URL, DATABASE_USER, DATABASE_PASSWORD);
	    FileUsageWriter writer = new FileUsageWriter(Properties.OUTPUT.getValue().getPath());
	    
	    UsagePreCalculator pcalc = new UsagePreCalculator();
	    pcalc.setWriter(writer);
	    
	    
	    pcalc.init(
	    		DATABASE_URL.getValue(), 
	    		DATABASE_USER.getValue(), 
	    		DATABASE_PASSWORD.getValue(),
	    		ENTITY_TYPE.getValue(),
	    		EXCLUDE_INTERNAL.getValue());
	    
		try {
			writer.openFiles();
			pcalc.writeUsage();
			writer.closeFiles();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.log(Level.SEVERE, "IOE during writing usage.");
			e.printStackTrace();
			System.exit(-1);
		}
	    
	    logger.info("Done executing usage preCalculator runner.");
	}
	    
}
