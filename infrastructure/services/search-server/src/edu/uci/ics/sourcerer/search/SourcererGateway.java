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
package edu.uci.ics.sourcerer.search;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.GetMethod;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import edu.uci.ics.sourcerer.db.util.JdbcDataSource;

import static edu.uci.ics.sourcerer.util.io.Logging.logger;

/**
 * 
 * @author <a href="bajracharya@gmail.com">Sushil Bajracharya</a>
 * @created Sep 14, 2009
 *
 */
public class SourcererGateway {
	
	
	
	String urlPart = "http://localhost:8983/solr/scs/mlt";
	String codeUrlPart = "http://localhost:9180/file-server";
	String dbDriver = "com.mysql.jdbc.Driver";
	String dbUri = "jdbc:mysql://localhost:3307/sourcerer_test";
	String dbUser = "sourcerer";
	String dbPassword = "";

	
	HttpClient client;
	JdbcDataSource ds;
	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	
	{
		 MultiThreadedHttpConnectionManager httpConnectionManager = new MultiThreadedHttpConnectionManager();
	     httpConnectionManager.getParams().setConnectionTimeout(500);
	     httpConnectionManager.getParams().setSoTimeout(1000);
	     client = new HttpClient(httpConnectionManager);
	}
	
	private void initDb(){
		ds = new JdbcDataSource();
	    Properties p = new Properties();
		p.put("driver", dbDriver);
		p.put("url", dbUri);
		p.put("user", dbUser);
		p.put("password", dbPassword);
		ds.init(p);
	}
	
	public SourcererGateway(){
		//initDb();
	}
	
	public SourcererGateway(String scsUrlPart, String codeUrlPart) {
		if(scsUrlPart!=null && scsUrlPart.length()>0) this.urlPart = scsUrlPart;
		if(codeUrlPart!=null && codeUrlPart.length()>0) this.codeUrlPart = codeUrlPart;
		//initDb();
	}
	
	public SourcererGateway(String scsUrlPart, String codeUrlPart,
			String dbUri, String dbUser, String dbPassword) {
		if(scsUrlPart!=null && scsUrlPart.length()>0) this.urlPart = scsUrlPart;
		if(codeUrlPart!=null && codeUrlPart.length()>0) this.codeUrlPart = codeUrlPart;
		setDb(dbUri, dbUser, dbPassword);
	}
	
	public void setDb(String uri, String user, String password){
		this.dbUri = uri;
		this.dbUser = user;
		this.dbPassword = password;
		initDb();
	}
	
	public void setCodeServerUrl(String url){
		this.codeUrlPart = url;
	}
	
	public void setMltServerUrl(String url){
		this.urlPart = url;
	}
	
	public static SourcererGateway getInstance(String scsUrl, String codeUrl){
		if (obj == null) obj = new SourcererGateway(scsUrl, codeUrl);
		
		return obj;
	}
	
	public static SourcererGateway getInstance(
			String scsUrl, String codeUrl,
			String dbUri, String dbUser, String dbPassword){
		if (obj == null) obj = new SourcererGateway(scsUrl, codeUrl, dbUri, dbUser, dbPassword);
		
		return obj;
	}
	
	private static SourcererGateway obj;
	
	// -- apis
	
	//{{{ -- start hack
	// TODO made to work with jar entities for now
	public String getCode(String entityId){
		
		String queryString = "jarEntityID=" + entityId;
		
		return sendGetCommand(queryString, codeUrlPart);
	}
	
	public String getJarComment(String jarCommentId) {
		String queryString = "jarCommentID=" + jarCommentId;
		return sendGetCommand(queryString, codeUrlPart);
	}
	
	///}}} -- end hack
	
	public String mltSnamesViaJdkUse(String entityId){
		return HitFqnEntityIdToString(searchMltViaJdkUse(entityId));
	}
	
	public String mltSnamesViaLibUse(String entityId){
		return HitFqnEntityIdToString(searchMltViaLibUse(entityId));
	}
	
//	public String mltSnamesViaLocalUse(String entityId){
//		return HitFqnEntityIdToString(searchMltViaLocalUse(entityId));
//	}
	
	public String mltSnamesViaJdkLibUse(String entityId) {
		return HitFqnEntityIdToString(searchMltViaJdkLibUse(entityId));
	}
	
//	public String mltSnamesViaAllUse(String entityId) {
//		return HitFqnEntityIdToString(searchMltViaAllUse(entityId));
//	}
	
	public String snamesViaSimEntitiesTC(String entityId) {
		
		if(ds==null) return "";
		
		String query = "SELECT e.fqn from similarity_tanimoto as stc" +
				" INNER JOIN entities as e on stc.rhs_eid=e.entity_id" +
				" WHERE stc.lhs_eid=" + entityId;
		return makeFqnsStringFromQueryResult(ds.getData(query));
	}
	
	
	public List<HitFqnEntityId> searchMltViaJdkUse(String entityId) {
		return getFqnEntityIdFromHits(searchMlt(entityId, "jdk_use_fqn_full"));
	}


	public List<HitFqnEntityId> searchMltViaLibUse(String entityId) {
		return getFqnEntityIdFromHits(searchMlt(entityId, "lib_use_fqn_full"));
	}


//	public List<HitFqnEntityId> searchMltViaLocalUse(String entityId) {
//		return getFqnEntityIdFromHits(searchMlt(entityId, "local_use_fqn_full"));
//	}
	
	public List<HitFqnEntityId> searchMltViaJdkLibUse(String entityId) {
		return getFqnEntityIdFromHits(searchMlt(entityId, "jdkLib_use_fqn_full"));
	}
	
//	public List<HitFqnEntityId> searchMltViaAllUse(String entityId) {
//		return getFqnEntityIdFromHits(searchMlt(entityId, "all_use_fqn_full"));
//	}
	
	// -- end apis
	
	private String HitFqnEntityIdToString(List<HitFqnEntityId> h){
		StringBuffer buf = new StringBuffer();
		for(HitFqnEntityId _h : h){
			buf.append(_h.fqn);
			buf.append(" ");
		}
		return buf.toString().trim();
	}
	
//	List<HitFqnEntityId> searchMltViaJdkLibUsage(String entityId) {
//		return getFqnEntityIdFromHits(searchMlt(entityId, "jdk_use_fqn_full,lib_use_fqn_full"));
//	}
//
//
//	List<HitFqnEntityId> searchMltViaAllUsage(String entityId) {
//		return getFqnEntityIdFromHits(searchMlt(entityId, "jdk_use_fqn_full,lib_use_fqn_full,local_use_fqn_full"));
//	}
	
	private String searchMlt(String entityId, String mltFields){
		
		String queryString = "start=0&rows=30&q=entity_id:" 
			+ entityId 
			+ "&mlt.fl=" 
			+ mltFields 
			+ "&mlt.mindf=3&mlt.mintf=1&fl=fqn_full,entity_id" 
			//+ "&mlt.boost=true" 
			;
		
		String result = "";
		result = sendGetCommand(queryString, urlPart);

		return result;
	}

	
	
	/**
     * Send the command to Solr using a GET
     * @param queryString
     * @param url
     * @return
     * @throws IOException
     */
    private String sendGetCommand(String queryString, String url)
    {
        String results = null;
        GetMethod get = new GetMethod(url);
        get.setQueryString(queryString.trim());
        
        get.addRequestHeader("Cache-Control", "no-cache");
        get.setFollowRedirects(false);
        
        get.getParams().setParameter("http.socket.timeout", new Integer(2000));
        try {
			client.executeMethod(get);
			
			 try
		        {
		            // Execute the method.
		            int statusCode = get.getStatusCode();

		            if (statusCode != HttpStatus.SC_OK)
		            {
		                System.err.println("Method failed: " + get.getStatusLine() + "\n" + queryString);
		                logger.log(Level.SEVERE, "Method failed: " + get.getStatusLine() + "\n" + queryString);
		                //results = "Method failed: " + get.getStatusLine();
		                results = "";
		            }
		            results = getStringFromStream(get.getResponseBodyAsStream());
		        }
		        catch (HttpException e)
		        {
		            System.err.println("[" + queryString  + "] Fatal protocol violation: " + e.getMessage());
		            e.printStackTrace();
		            logger.log(Level.SEVERE, "[" + queryString  + "] Fatal protocol violation: " + e.getMessage());
		        }
		        catch (IOException e)
		        {
		            System.err.println("[" + queryString  + "] IOE "  + e.getMessage());
		            e.printStackTrace();
		            logger.log(Level.SEVERE, "[" + queryString  + "] IOE "  + e.getMessage());
		        }
		        finally
		        {
		            // Release the connection.
		            get.releaseConnection();
		        }
			
		} catch (HttpException e1) {
			// TODO Auto-generated catch block
			//e1.printStackTrace();
			System.err.println("HttpExp: " + queryString);
			results = "";
			logger.log(Level.SEVERE,"HttpExp: " + queryString);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			// e1.printStackTrace();
			System.err.println("Timeout: " + queryString);
			results = "";
			logger.log(Level.SEVERE,"Timeout: " + queryString);
		}
       
        return results;
    }
    
    // got this method from sourcerer
	// query: string from input stream
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
	
	private List<HitFqnEntityId> getFqnEntityIdFromHits(String xmlResultInString) {

		List<HitFqnEntityId> hitsInfo = new LinkedList<HitFqnEntityId>();
		
		if(xmlResultInString == null || xmlResultInString.length()==0)
			return hitsInfo;
		
		Node responseNode = getResponseNode(xmlResultInString);
		
		if(responseNode==null) 
			return hitsInfo;
		
		org.w3c.dom.NodeList hits = responseNode.getChildNodes(); 
		
		for (int i = 0; i < hits.getLength(); i++) {
			String fqn = "";
			String entity_id = "";
			
			Node hit = hits.item(i);
			if(!hit.getNodeName().equals("doc")) continue;
			
			NodeList hitDocChildNodes = hit.getChildNodes();

			for (int j = 0; j < hitDocChildNodes.getLength(); j++) {
				Node hitDocChildNode = hitDocChildNodes.item(j);

				NamedNodeMap attrs = hitDocChildNode.getAttributes();
				Node _attributeNode = null;
				_attributeNode = attrs.getNamedItem("name");

				if (_attributeNode != null) {
					if (_attributeNode.getNodeValue().equals("entity_id")) {
						entity_id = hitDocChildNode.getFirstChild()
								.getNodeValue();
					} else if (_attributeNode.getNodeValue().equals("fqn_full")) {
						fqn = hitDocChildNode.getFirstChild().getNodeValue();
					}
				}
			}
			
			hitsInfo.add(new HitFqnEntityId(fqn, entity_id));
		}
		return hitsInfo;
	}
	
	private Node getResponseNode(String xmlResultInString){
		
		DocumentBuilder db = null;
		try {
			db = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			//System.err.println("parser error");
			return null;
		}
		InputSource inStream = new InputSource();
		inStream.setCharacterStream(new StringReader(xmlResultInString));
		Document doc = null;
		try {
			doc = db.parse(inStream);
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
		//Document doc = XMLParser.parse(xmlResultInString);
		NodeList results = doc.getElementsByTagName("result");

		for (int i = 0; i < results.getLength(); i++) {
			Node result = results.item(i);
			NamedNodeMap attrs = result.getAttributes();
			Node _attributeNode = null;
			_attributeNode = attrs.getNamedItem("name");

			if (_attributeNode != null) {
				if (_attributeNode.getNodeValue().equals("response")) {
					
					return result;
				}
			}
		}
	
		return null;
	}
	
	private String makeFqnsStringFromQueryResult(
			Iterator<Map<String, Object>> data) {
		StringBuffer sb = new StringBuffer();
		
		while(data.hasNext()){
			sb.append((String) (data.next().get("fqn")));
			sb.append(" ");
		}
		
		return sb.toString().trim();
	}

	


	
}

