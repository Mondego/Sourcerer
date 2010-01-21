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
package edu.uci.ics.sourcerer.search.analysis;

import java.util.List;
import java.util.Map;

import org.apache.solr.handler.dataimport.Context;
import org.apache.solr.handler.dataimport.Transformer;

/**
 * @author <a href="bajracharya@gmail.com">Sushil Bajracharya</a>
 * @created Dec 2, 2009
 *
 */
public class FqnCleaningTransformer extends Transformer { 

    public Map<String, Object> transformRow(Map<String, Object> row, Context context) {
            List<Map<String, String>> fields = context.getAllEntityFields();

            for (Map<String, String> field : fields) {
                    // Check if this field has trim="true" specified in the data-config.xml
                    String clean = field.get("clean-fqn");
                    if ("true".equals(clean))        {
                            // Apply fqn cleaning on this field
                            String columnName = field.get("column");
                            // Get this field's value from the current row
                            String value = (String) row.get(columnName);
                            // Trim and put the updated value back in the current row
                            if (value != null){
                            	value = value.replaceAll("_UNRESOLVED_.", ".");
                            	value = value.replaceAll("\\(UNKNOWN\\)", "");
                            	value = value.replaceAll("\\(1UNKNOWN\\)", "");
                                row.put(columnName, value);
                            }
                    }
                    
                    String fixConstructor = field.get("fix-init");
                    if("true".equals(fixConstructor)){
                    	// Apply fqn cleaning on this field
                        String columnName = field.get("column");
                        // Get this field's value from the current row
                        String value = (String) row.get(columnName);
                        // Trim and put the updated value back in the current row
                        if (value != null){
                        	value = value.replaceAll("\\.<init>\\(", ".(");
                        	value = value.replaceAll("\\.main\\(", ".(");
                            row.put(columnName, value);
                        }
                    }
            }

            return row;
    }

}

