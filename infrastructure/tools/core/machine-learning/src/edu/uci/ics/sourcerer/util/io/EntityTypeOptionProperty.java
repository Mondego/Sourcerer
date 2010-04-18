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
package edu.uci.ics.sourcerer.util.io;

import edu.uci.ics.sourcerer.ml.db.tools.UsagePreCalculator;

/**
 * @author <a href="bajracharya@gmail.com">Sushil Bajracharya</a>
 * @created Mar 26, 2010
 *
 */
public class EntityTypeOptionProperty extends Property<UsagePreCalculator.EntityType> {

	public EntityTypeOptionProperty(String name, UsagePreCalculator.EntityType defaultValue, String category, String description) {
	    super(name, defaultValue, category, description);
	  }
	

	@Override
	public String getType() {
		return "Usage";
	}

	
	@Override
	protected UsagePreCalculator.EntityType parseString(String value) {
	    
		try{
		  return UsagePreCalculator.EntityType.valueOf(value.toUpperCase());
		}catch(Exception e){
			throw new IllegalArgumentException("Please choose a valid value for " + this.name + ", or skip the option to use the default value.");
		}
		 
	  }

}
