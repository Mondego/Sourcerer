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
import java.util.List;
import java.util.Map;

import org.apache.solr.handler.dataimport.Context;
import org.apache.solr.handler.dataimport.Transformer;

import edu.uci.ics.sourcerer.search.SourcererGateway;

/**
 * @author <a href="bajracharya@gmail.com">Sushil Bajracharya</a>
 * @created Dec 23, 2009
 *
 */
public class UsedEntityCommentTransformer extends Transformer {

	
	SourcererGateway sg = SourcererGateway.getInstance("", "", "");
	
	/* (non-Javadoc)
	 * @see org.apache.solr.handler.dataimport.Transformer#transformRow(java.util.Map, org.apache.solr.handler.dataimport.Context)
	 */
	@Override
	public Map<String, Object> transformRow(Map<String, Object> row, Context context) {
		String codeServerUrl = context.getEntityAttribute("code-server-url");
		if(codeServerUrl!=null) sg.setCodeServerUrl(codeServerUrl);
		
		String commentId =  ((BigInteger) row.get("commentId")) + "";
        //String ctype = (String) row.get("comment_type");
        
        if (commentId != null /*&& ctype!=null*/){             
        	
        	String commentText = sg.getComment(commentId);
        	
        	if(commentText!=null 
        			&& commentText.length()>0 
        			&& !commentText.startsWith("Unable to find location for")){
    			
    			//if(ctype.equals("JAVADOC")){
    	        	row.put("p_javadoc", commentText);
            	//} else {
    	        //	row.put("lp_comment",commentText);
            	//}
        	}
        }
		return row;
	}

}
