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
package edu.uci.ics.sourcerer.scs.common;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;

import edu.uci.ics.sourcerer.scs.common.client.SearchHeuristic;
import edu.uci.ics.sourcerer.scs.common.client.UsedFqn;

/**
 * @author <a href="bajracharya@gmail.com">Sushil Bajracharya</a>
 * @created Sep 9, 2009
 *
 */
public class SourcererSearchAdapter {

	private static String SEARCH_SERVER = 
	//	"http://localhost:8984"; 
	// "http://kathmandu.ics.uci.edu:8984";
	"http://mine8.ics.uci.edu:8984";
	// "http://nile.ics.uci.edu:8984";
	private static String FACET_SERVER  = 
	//	"http://localhost:8984"; 
	 "http://mine8.ics.uci.edu:8984"; 
	// "http://nile.ics.uci.edu:8983"; 
	private static String MLT_SERVER    = 
	//	"http://localhost:8984"; 
	"http://mine8.ics.uci.edu:8984"; 
	// "http://nile.ics.uci.edu:8983";
	private static String FILE_SERVER   = 
		"http://nile.ics.uci.edu:9180/file-server";		
	//	"http://kathmandu.ics.uci.edu:8080/file-server";
	
	private static String QUERY_PART_SNAME_CONTENTS = "sname_contents^50.0%20";
	private static String QUERY_PART_JDKLIB_SIM_SNAME_CONTENTS =   
		  //	"sim_sname_contents_via_jdk_use^1.0%20sim_sname_contents_via_lib_use^50.0%20";
		"sim_sname_contents_via_jdkLib_use^50.0%20";
	private static String QUERY_PART_JDKLIB_TC_SIM_SNAME_CONTENTS =
			"simTC_sname_contents_via_jdkLib_use^50.0%20";
	private static String QUERY_PART_JDKLIB_HD_SIM_SNAME_CONTENTS =
		"simHD_sname_contents_via_jdkLib_use^50.0%20";
		
	// private static String QUERY_PART_LOCAL_SIM_SNAME_CONTENTS = 
	// "sim_sname_contents_via_local_use^1.0%20";	
	private static String QUERY_PART_FQN_CONTENTS = "fqn_contents^25.0%20";
	
	private static String QUERY_PART_USE_SNAME_CONTENTS = 
	//	"jdk_use_sname_contents^1.0%20lib_use_sname_contents^100.0%20";
		"jdkLib_use_sname_contents^100.0%20";
	//local_use_sname_contents^20.0%20";
	private static String QUERY_PART_USE_FQN_CONTENTS = 
	//	"jdk_use_fqn_contents^1.0%20lib_use_fqn_contents^50.0%20";
		"jdkLib_use_fqn_contents^50.0%20";
	//local_use_fqn_contents^10.0%20";
	
	private static String QUERY_PART_FULL_TEXT = "full_text^1.0%20"; // switch to 25
	private static String QUERY_PART_USED_JAVADOC = "jdkLib_use_javadoc^50.0%20";
	static JavaToHtml j2h = new JavaToHtml();
	
	public static String searchSCSServer(String query) {
		
		return getGETResult(query, 1, 10, SearchHeuristic.TEXT_USEDFQN_FQN_JdkLibSimSNAME_SNAME);
	}
	
	public static String getFormattedSnippet(String raw){
		j2h.reset();
		String result = "<div class=\"result_code\">" + j2h.process(raw) + "</div>";
		return result;
	}
	
	public static String getEntityCode(String entityId, Set<String> qterms,
			Set<String> usedEntities){
		
		String rawCode = getEntityCodeRaw(entityId);
		
		j2h.reset();
		// System.out.print(j2h.process(javaCode));
		
//		List<String> listQTerms = Arrays.asList(qterms.toArray(new String[qterms.size()]));
		
		String result = "<div class=\"result_code\">" + j2h.process(rawCode) + "</div>";
//		
//		return Highlighter.highlightSearchTerms(
//				result,
//				qterms);
		
//		String result = "<div class=\"result_code\">" + CodeViewer.highlightInlined(rawCode, listQTerms) + "</div>";
		return result;
	}
	
	public static String getEntityCodeRaw(String entityId){
		
		return(getEntityCodeRaw(entityId, "entityID"));
	}
	
	public static String getJarClassFileCodeRaw(String jarClassFileID){
		return(getEntityCodeRaw(jarClassFileID, "fileID"));
	}
	
	/**
	 * 
	 * @param entityId
	 * @param codeEntityType valid: jarEntity, jarClassFileID .. etc
	 * @return
	 */
	private static String getEntityCodeRaw(String entityId, String codeEntityType){
		//http://nile.ics.uci.edu:9180/file-server?jarClassFileID=100
		
		String urlPart = FILE_SERVER + "?" ;//?entityID=5096973";
		String queryString = codeEntityType + "=" + entityId;
		
		try {
			
			String javaCode = sendGetCommand(queryString, urlPart); 
			//System.out.println(" " + queryString + " " + urlPart);
			
			return javaCode;
			
		} catch (IOException e) {
			return "error: " + e.getMessage();
		}
	}

	private static String getGETResult(String query, long start, int rows, SearchHeuristic heuristic, HashSet<String> filterFqns) {
		
		String urlPart = SEARCH_SERVER + "/solr/scs/select/";
		
		String queryString = "start=" + start + "&rows=" + rows 
			// TODO refactor out as constant 
			// collapse duplicate FQNs
			+ "&collapse.field=fqn_full&collapse.max=1&collapse.type=normal"
			+ buildQueryPart(query, heuristic);
		

		// append fqn filter to query
		if(filterFqns!=null && filterFqns.size()>0){
			String queryFqnFilterPart = "";
			queryFqnFilterPart = buildQueryFqnFilterPart(filterFqns);
			queryString = queryString + queryFqnFilterPart;
		}
		
		String result = "";
		
		try {
			result = sendGetCommand(queryString, urlPart);
		} catch (IOException e) {
			result = e.getMessage();
		}

		return result;

	}
	
	
	private static String buildQueryFqnFilterPart(HashSet<String> filterFqns) {
		StringBuffer _buf = new StringBuffer();
		
		String _pre = "&fq=jdkLib_use_fqn_full:";
		
		for(String s : filterFqns){
			_buf.append(_pre);
			_buf.append(escapeLuceneQuerySyntaxChars(s));
		}
		
		return _buf.toString();
	}

	private static Object escapeLuceneQuerySyntaxChars(String s) {

		// lucene special characters
		// + - && || ! ( ) { } [ ] ^ " ~ * ? : \

		String pattern = "([\\(\\)\\[\\]\\*\\?])";
		s = s.replaceAll(pattern,"\\\\$1");
		
		return s;
	}

	private static String getGETResult(String query, long start, int rows, SearchHeuristic heuristic) {
		
		return getGETResult(query, start, rows, heuristic, null);

	}
	
	/**
     * Send the command to Solr using a GET
     * @param queryString
     * @param url
     * @return
     * @throws IOException
     */
    private static String sendGetCommand(String queryString, String url)
            throws IOException
    {
        String results = null;
        HttpClient client = new HttpClient();
        GetMethod get = new GetMethod(url);
        get.setQueryString(queryString.trim());

        client.executeMethod(get);
        try
        {
            // Execute the method.
            int statusCode = client.executeMethod(get);

            if (statusCode != HttpStatus.SC_OK)
            {
                System.err.println("Method failed: " + get.getStatusLine());
                results = "Method failed: " + get.getStatusLine();
            }

            results = getStringFromStream(get.getResponseBodyAsStream());
        }
        catch (HttpException e)
        {
            System.err.println("Fatal protocol violation: " + e.getMessage());
            e.printStackTrace();
        }
        catch (IOException e)
        {
            System.err.println("Fatal transport error: " + e.getMessage());
            e.printStackTrace();
        }
        finally
        {
            // Release the connection.
            get.releaseConnection();
        }
        return results;
    }
    
	private static String getStringFromStream(InputStream isData) throws IOException
	{
	    ByteArrayOutputStream baosData = new ByteArrayOutputStream();
	    
	    byte[] abyteBuffer = new byte[1024];
	    
	    int nBytesRead = 0; 
	    while ((nBytesRead = isData.read(abyteBuffer)) >= 0)
	    {
	      baosData.write(abyteBuffer, 0, nBytesRead);
	    }
	    baosData.close();
	    
	    return baosData.toString();
	}

	public static String searchSCSServer(String query, long start, int rows) {
		return getGETResult(query, start, rows, SearchHeuristic.TEXT_USEDFQN_FQN_JdkLibSimSNAME_SNAME);
	}
	
	public static String buildQueryPart(String queryTerms, SearchHeuristic h){
		
		String queryPart = "";
//		try {
//			queryTerms = URLEncoder.encode(queryTerms+ " ", "UTF-8");
//		} catch (UnsupportedEncodingException e) {
//			e.printStackTrace();
//		}
		
		queryTerms = queryTerms.trim().replaceAll("\\s", "%20");
		
		switch(h) {
		
		case NONE:
			queryPart = "&q=" + queryTerms
				+ "&fl=fqn_full,entity_id,score";
			break;
			
//		case TEXT:
//			queryPart = "&q=full_text:(" + queryTerms
//			+ ")&fl=fqn_full,entity_id,score";
//			break;
			
		case TEXT:
			queryPart = "&q=" + queryTerms 
			 + "&fl=fqn_full,entity_id,score&qt=dismax&qf=" +
			 		QUERY_PART_FULL_TEXT 
			 		;
			break;	
		
		case FQN_SNAME:
			queryPart = "&q=" + queryTerms 
				+ "&fl=fqn_full,entity_id,score&qt=dismax&qf=" +
					QUERY_PART_FQN_CONTENTS +
					QUERY_PART_SNAME_CONTENTS;
			
			break;
			
		case FQN_USEDFQN_SNAME:
			queryPart = "&q=" + queryTerms 
			 + "&fl=fqn_full,entity_id,score&qt=dismax&qf=" +
			 		QUERY_PART_FQN_CONTENTS +
			 		QUERY_PART_USE_FQN_CONTENTS + QUERY_PART_USE_SNAME_CONTENTS +
			 		QUERY_PART_SNAME_CONTENTS
			 		;			
			
			break;
			
		case TEXT_FQN_SNAME:
			queryPart = "&q=" + queryTerms 
			 + "&fl=fqn_full,entity_id,score&qt=dismax&qf=" +
			 		QUERY_PART_FULL_TEXT + 
			 		QUERY_PART_FQN_CONTENTS +
			 		QUERY_PART_SNAME_CONTENTS
			 		;
			break;
			
		case TEXT_SNAME:
			queryPart = "&q=" + queryTerms 
			 + "&fl=fqn_full,entity_id,score&qt=dismax&qf=" +
			 		QUERY_PART_FULL_TEXT + 
			 		QUERY_PART_SNAME_CONTENTS
			 		;
			break;	
		
//		case FQN_USEDFQN_SimSNAME_SNAME:
//			queryPart = "&q=" + queryTerms 
//			 + "&fl=fqn_full,entity_id,score&qt=dismax&qf=" +
//			 		QUERY_PART_FQN_CONTENTS +
//			 		QUERY_PART_USE_FQN_CONTENTS + QUERY_PART_USE_SNAME_CONTENTS +
//			 		QUERY_PART_JDKLIB_SIM_SNAME_CONTENTS +
//			 		QUERY_PART_LOCAL_SIM_SNAME_CONTENTS +
//			 		QUERY_PART_SNAME_CONTENTS
//			 		;
//			break;
		
		case FQN_USEDFQN_JdkLibSimSNAME_SNAME:
			queryPart = "&q=" + queryTerms 
			 + "&fl=fqn_full,entity_id,score&qt=dismax&qf=" +
			 		QUERY_PART_FQN_CONTENTS +
			 		QUERY_PART_USE_FQN_CONTENTS + QUERY_PART_USE_SNAME_CONTENTS +
			 		QUERY_PART_JDKLIB_SIM_SNAME_CONTENTS +
			 		QUERY_PART_SNAME_CONTENTS
			 		;
			break;
			
		case FQN_USEDFQN_JdkLibTcSimSNAME_SNAME:
			queryPart = "&q=" + queryTerms 
			 + "&fl=fqn_full,entity_id,score&qt=dismax&qf=" +
			 		QUERY_PART_FQN_CONTENTS +
			 		QUERY_PART_USE_FQN_CONTENTS + QUERY_PART_USE_SNAME_CONTENTS +
			 		QUERY_PART_JDKLIB_TC_SIM_SNAME_CONTENTS +
			 		QUERY_PART_SNAME_CONTENTS
			 		;
			break;
			
		case FQN_USEDFQN_JdkLibHdSimSNAME_SNAME:
			queryPart = "&q=" + queryTerms 
			 + "&fl=fqn_full,entity_id,score&qt=dismax&qf=" +
			 		QUERY_PART_FQN_CONTENTS +
			 		QUERY_PART_USE_FQN_CONTENTS + QUERY_PART_USE_SNAME_CONTENTS +
			 		QUERY_PART_JDKLIB_HD_SIM_SNAME_CONTENTS +
			 		QUERY_PART_SNAME_CONTENTS
			 		;
			break;
			
		case TEXT_USEDFQN_FQN_JdkLibSimSNAME_SNAME:
			queryPart = "&q=" + queryTerms 
			 + "&fl=fqn_full,entity_id,score&qt=dismax&qf=" +
			 		QUERY_PART_FULL_TEXT + 
			 		QUERY_PART_USE_FQN_CONTENTS + QUERY_PART_USE_SNAME_CONTENTS +
			 		QUERY_PART_FQN_CONTENTS +
			 		QUERY_PART_JDKLIB_SIM_SNAME_CONTENTS +
			 		QUERY_PART_SNAME_CONTENTS
			 		;
			break;
			
		case TEXT_USEDFQN_FQN_JdkLibTcSimSNAME_SNAME:
			queryPart = "&q=" + queryTerms 
			 + "&fl=fqn_full,entity_id,score&qt=dismax&qf=" +
			 		QUERY_PART_FULL_TEXT + 
			 		QUERY_PART_USE_FQN_CONTENTS + QUERY_PART_USE_SNAME_CONTENTS +
			 		QUERY_PART_FQN_CONTENTS +
			 		QUERY_PART_JDKLIB_TC_SIM_SNAME_CONTENTS +
			 		QUERY_PART_SNAME_CONTENTS
			 		;
			break;
			
		case TEXT_USEDFQN_FQN_JdkLibHdSimSNAME_SNAME:
			queryPart = "&q=" + queryTerms 
			 + "&fl=fqn_full,entity_id,score&qt=dismax&qf=" +
			 		QUERY_PART_FULL_TEXT +  
			 		QUERY_PART_USE_FQN_CONTENTS + QUERY_PART_USE_SNAME_CONTENTS +
			 		QUERY_PART_FQN_CONTENTS +
			 		QUERY_PART_JDKLIB_HD_SIM_SNAME_CONTENTS +
			 		QUERY_PART_SNAME_CONTENTS
			 		;
			break;
			
		case TEXT_USEDFQN_FQN_SNAME:
			queryPart = "&q=" + queryTerms 
			 + "&fl=fqn_full,entity_id,score&qt=dismax&qf=" +
			 		QUERY_PART_FULL_TEXT + 
			 		QUERY_PART_USE_FQN_CONTENTS + QUERY_PART_USE_SNAME_CONTENTS +
			 		QUERY_PART_FQN_CONTENTS +
			 		QUERY_PART_SNAME_CONTENTS
			 		;
			break;
			
		// text + javadoc
		
		case TEXT_UJDOC_USEDFQN_FQN_JdkLibSimSNAME_SNAME:
			queryPart = "&q=" + queryTerms 
			 + "&fl=fqn_full,entity_id,score&qt=dismax&qf=" +
			 		QUERY_PART_FULL_TEXT + QUERY_PART_USED_JAVADOC +
			 		QUERY_PART_USE_FQN_CONTENTS + QUERY_PART_USE_SNAME_CONTENTS +
			 		QUERY_PART_FQN_CONTENTS +
			 		QUERY_PART_JDKLIB_SIM_SNAME_CONTENTS +
			 		QUERY_PART_SNAME_CONTENTS
			 		;
			break;
			
		case TEXT_UJDOC_USEDFQN_FQN_JdkLibTcSimSNAME_SNAME:
			queryPart = "&q=" + queryTerms 
			 + "&fl=fqn_full,entity_id,score&qt=dismax&qf=" +
			 		QUERY_PART_FULL_TEXT + QUERY_PART_USED_JAVADOC +
			 		QUERY_PART_USE_FQN_CONTENTS + QUERY_PART_USE_SNAME_CONTENTS +
			 		QUERY_PART_FQN_CONTENTS +
			 		QUERY_PART_JDKLIB_TC_SIM_SNAME_CONTENTS +
			 		QUERY_PART_SNAME_CONTENTS
			 		;
			break;
			
		case TEXT_UJDOC_USEDFQN_FQN_JdkLibHdSimSNAME_SNAME:
			queryPart = "&q=" + queryTerms 
			 + "&fl=fqn_full,entity_id,score&qt=dismax&qf=" +
			 		QUERY_PART_FULL_TEXT +  QUERY_PART_USED_JAVADOC +
			 		QUERY_PART_USE_FQN_CONTENTS + QUERY_PART_USE_SNAME_CONTENTS +
			 		QUERY_PART_FQN_CONTENTS +
			 		QUERY_PART_JDKLIB_HD_SIM_SNAME_CONTENTS +
			 		QUERY_PART_SNAME_CONTENTS
			 		;
			break;
			
		case TEXT_UJDOC_USEDFQN_FQN_SNAME:
			queryPart = "&q=" + queryTerms 
			 + "&fl=fqn_full,entity_id,score&qt=dismax&qf=" +
			 		QUERY_PART_FULL_TEXT + QUERY_PART_USED_JAVADOC +
			 		QUERY_PART_USE_FQN_CONTENTS + QUERY_PART_USE_SNAME_CONTENTS +
			 		QUERY_PART_FQN_CONTENTS +
			 		QUERY_PART_SNAME_CONTENTS
			 		;
			break;
			
			
			
	   // END text + javadoc
			
			// javadoc
			// text + javadoc
			
		case UJDOC_USEDFQN_FQN_JdkLibSimSNAME_SNAME:
			queryPart = "&q=" + queryTerms 
			 + "&fl=fqn_full,entity_id,score&qt=dismax&qf=" +
			 		QUERY_PART_USED_JAVADOC +
			 		QUERY_PART_USE_FQN_CONTENTS + QUERY_PART_USE_SNAME_CONTENTS +
			 		QUERY_PART_FQN_CONTENTS +
			 		QUERY_PART_JDKLIB_SIM_SNAME_CONTENTS +
			 		QUERY_PART_SNAME_CONTENTS
			 		;
			break;
			
		case UJDOC_USEDFQN_FQN_JdkLibTcSimSNAME_SNAME:
			queryPart = "&q=" + queryTerms 
			 + "&fl=fqn_full,entity_id,score&qt=dismax&qf=" +
			 		QUERY_PART_USED_JAVADOC +
			 		QUERY_PART_USE_FQN_CONTENTS + QUERY_PART_USE_SNAME_CONTENTS +
			 		QUERY_PART_FQN_CONTENTS +
			 		QUERY_PART_JDKLIB_TC_SIM_SNAME_CONTENTS +
			 		QUERY_PART_SNAME_CONTENTS
			 		;
			break;
			
		case UJDOC_USEDFQN_FQN_JdkLibHdSimSNAME_SNAME:
			queryPart = "&q=" + queryTerms 
			 + "&fl=fqn_full,entity_id,score&qt=dismax&qf=" +
			 		QUERY_PART_USED_JAVADOC +
			 		QUERY_PART_USE_FQN_CONTENTS + QUERY_PART_USE_SNAME_CONTENTS +
			 		QUERY_PART_FQN_CONTENTS +
			 		QUERY_PART_JDKLIB_HD_SIM_SNAME_CONTENTS +
			 		QUERY_PART_SNAME_CONTENTS
			 		;
			break;
			
		case UJDOC_USEDFQN_FQN_SNAME:
			queryPart = "&q=" + queryTerms 
			 + "&fl=fqn_full,entity_id,score&qt=dismax&qf=" +
			 		QUERY_PART_USED_JAVADOC +
			 		QUERY_PART_USE_FQN_CONTENTS + QUERY_PART_USE_SNAME_CONTENTS +
			 		QUERY_PART_FQN_CONTENTS +
			 		QUERY_PART_SNAME_CONTENTS
			 		;
			break;
			
			// end javadoc
			
			
//		case FQN_USEDFQN_MIN_ONE_TERM_IN_SNAME:
//			queryPart = "";
//			//TODO handle this using query parser
//			String queryPartStr = queryTerms.replaceAll("\\W", " ");
//			queryPartStr = queryPartStr.replaceAll("\\sOR\\s", " ");
//			queryPartStr = queryPartStr.replaceAll("\\sAND\\s", " ");
//			queryPartStr = queryPartStr.replaceAll("\\s+", " ");
//			String[] queryParts = queryPartStr.split("\\s");
//			
//			StringBuilder sbSname = new StringBuilder();
//			sbSname.append("+(+sname_contents:(");
//			for(int i=0; i<queryParts.length; i++){
//				sbSname.append(queryParts[i]);
//				if(i<queryParts.length-1)
//					sbSname.append(")^3.0+OR+sname_contents:(");
//				else
//					sbSname.append(")^3.0)");
//			}
//			
//			queryPart= "&q=" + queryTerms + sbSname.toString();
//			break;
//			
		}
		
		return queryPart + "&fq=entity_type:METHOD";
	}

	public static String searchSCSServer(String query, long start, int rows,
			SearchHeuristic heuristic, HashSet<String> filterFqns) {
		return getGETResult(query, start, rows, heuristic, filterFqns);
	}
	

	public static String searchSCSServer(String query, long start, int rows,
			SearchHeuristic heuristic) {
		return getGETResult(query, start, rows, heuristic);
	}

	public String getEvaluationQueries() {
		return null;
	}

	private static String searchFacets(String query, SearchHeuristic heuristic){
		
		
		String urlPart = FACET_SERVER + "/solr/scs/select";
		
		String queryString = "start=" + 1 + "&rows=" + 0 
			+ buildQueryPart(query, heuristic)
			+ "&facet=true&facet.field=jdkLib_use_fqn_full&facet.field=jdkLib_use_fqn_full&facet.field=entity_type&facet.mincount=1";
		
		String result = "";
		
		try {
			result = sendGetCommand(queryString, urlPart);
		} catch (IOException e) {
			result = e.getMessage();
		}

		return result;
		//
	}
	
	private static String searchMlt(String entityId, String mltFields){
		
		String urlPart = MLT_SERVER + "/solr/scs/mlt";
		
		String queryString = "start=0&rows=10&q=entity_id:" 
			+ entityId 
			+ "&mlt.fl=" 
			+ mltFields 
			+ "&mlt.mindf=1&mlt.mintf=1&fl=fqn_full,entity_id," 
			+ mltFields 
			+ "&mlt.interestingTerms=list";
		
		String result = "";
		
		try {
			result = sendGetCommand(queryString, urlPart);
		} catch (IOException e) {
			result = e.getMessage();
		}

		return result;
	}
//lib_use_fqn_full
	public static String searchMltViaJdkUsage(String entityId) {
		return searchMlt(entityId, "jdk_use_fqn_full");
	}


	public static String searchMltViaLibUsage(String entityId) {
		return searchMlt(entityId, "jdkLib_use_fqn_full");
	}


	public static String searchMltViaLocalUsage(String entityId) {
		return searchMlt(entityId, "local_use_fqn_full");
	}
	
	public static String getUsageAsFacets(String query, SearchHeuristic heuristic){
		return searchFacets(query, heuristic);
	}
	
	public static Map<String, String> getRelatedWords(String query){
		
		//String mltResults = 
		return null;
	}
	

}

