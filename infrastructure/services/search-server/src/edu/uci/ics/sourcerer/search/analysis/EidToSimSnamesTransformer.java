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
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.solr.handler.dataimport.Context;
import org.apache.solr.handler.dataimport.Transformer;

import edu.uci.ics.sourcerer.search.SourcererGateway;

/**
 * @author <a href="bajracharya@gmail.com">Sushil Bajracharya</a>
 * @created Aug 18, 2009
 *
 */
public class EidToSimSnamesTransformer extends Transformer{
	// use default urls
	SourcererGateway sg = SourcererGateway.getInstance("", "", "");
	
	public Object transformRow(Map<String, Object> row, Context context)     {
        String codeServerUrl = context.getEntityAttribute("code-server-url");
		if(codeServerUrl!=null) sg.setCodeServerUrl(codeServerUrl);
		
		String mltServerUrl = context.getEntityAttribute("mlt-server-url");
		if(mltServerUrl!=null) sg.setMltServerUrl(mltServerUrl);
		
		String simServerUrl = context.getEntityAttribute("sim-server-url");
		if(simServerUrl!=null) sg.setSimServerUrl(simServerUrl);
		
		String timeout = context.getEntityAttribute("http-timeout");
		if(timeout!=null) sg.setTimeout(timeout);
		
		String eid =  ((BigInteger) row.get("eid")) + "";
        String etype = (String) row.get("etype");
        
//        Logger.getLogger(this.getClass().getName()).log(Level.INFO, 
//    			"sim server: " + simServerUrl + " eid: " + eid);
//        
        
        if (eid != null && etype!=null){             
        	
        	if(etype.equals("CLASS") 
        			|| etype.equals("METHOD") 
        			|| etype.equals("CONSTRUCTOR")
        			// || etype.equals("UNKNOWN")
        			){
        		
        		String simMlt = sg.eidsViaMlt(eid);
	        	
        		row.put("simMLT_eids_via_jdkLib_use", simMlt);
//	        	Logger.getLogger(this.getClass().getName()).log(Level.INFO, 
//	        			"Got sim fqns via MLT: " + simMlt);
//	        	
	        	String simTC = sg.eidsViaSimEntitiesTC(eid); 
	        	row.put("simTC_eids_via_jdkLib_use", simTC);
//	        	Logger.getLogger(this.getClass().getName()).log(Level.INFO, 
//	        			"Got sim eids via TC: " + simTC);
	        	
	        	String simHD = sg.eidsViaSimEntitiesHD(eid);
	        	row.put("simHD_eids_via_jdkLib_use", simHD);
//	        	Logger.getLogger(this.getClass().getName()).log(Level.INFO, 
//	        			"Got sim eids via HD: " + simHD);
	        	
	        	if(codeServerUrl!=null && codeServerUrl.length()>0){
	        		String code = sg.getCode(eid);
	        		if(code!=null 
	        				&& code.length()>0 
	        				&& !code.startsWith("Unable to find")){
	        			row.put("code_text", code);
	        		}
	        	}
        	}
        }

        return row;
}

}
