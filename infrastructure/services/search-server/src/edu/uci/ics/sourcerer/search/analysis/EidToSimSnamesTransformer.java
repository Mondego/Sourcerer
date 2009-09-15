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

import java.math.BigInteger;
import java.util.Map;

import edu.uci.ics.sourcerer.search.SourcererGateway;

/**
 * @author <a href="bajracharya@gmail.com">Sushil Bajracharya</a>
 * @created Aug 18, 2009
 *
 */
public class EidToSimSnamesTransformer {
	// use default urls
	SourcererGateway sg = SourcererGateway.getInstance("", "");
	
	public Object transformRow(Map<String, Object> row)     {
        String eid =  ((BigInteger) row.get("eid")) + "";
        String etype = (String) row.get("etype");
        if (eid != null && etype!=null){             
        	
        	if(etype.equals("CLASS") || etype.equals("METHOD")){
	        	row.put("sim_fqns_via_jdk_use", sg.mltSnamesViaJdkUse(eid));
	        	row.put("sim_fqns_via_lib_use", sg.mltSnamesViaLibUse(eid));
	        	row.put("sim_fqns_via_local_use", sg.mltSnamesViaLocalUse(eid));
	        	row.put("code_text", sg.getCode(eid));
        	}
        }

        return row;
}

}
