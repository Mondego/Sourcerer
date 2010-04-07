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
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.GetMethod;

import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.util.JavaBinCodec;
import org.apache.solr.common.util.NamedList;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import edu.uci.ics.sourcerer.db.util.JdbcDataSource;
// import static edu.uci.ics.sourcerer.util.io.LoggingUtils.formatError;
// import static edu.uci.ics.sourcerer.util.io.LoggingUtils.formatInfo;



/**
 * 
 * @author <a href="bajracharya@gmail.com">Sushil Bajracharya</a>
 * @created Sep 14, 2009
 * 
 */
public class SourcererGateway {

	Logger logger = Logger.getLogger(this.getClass().getName());
	
	String simUrlPart = "http://loalhost:9180/similarity-server";
	String urlPart = "http://localhost:8983/solr/scs/mlt";
	String codeUrlPart = "http://localhost:9180/file-server";
	String dbDriver = "com.mysql.jdbc.Driver";
	String dbUri = "jdbc:mysql://localhost:3307/sourcerer_test";
	String dbUser = "sourcerer";
	String dbPassword = "";
	String timeout = "2500";
	
	HttpClient client;
	JdbcDataSource ds;
	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

	{
		MultiThreadedHttpConnectionManager httpConnectionManager = new MultiThreadedHttpConnectionManager();
		httpConnectionManager.getParams().setConnectionTimeout(1000);
		httpConnectionManager.getParams().setSoTimeout(1500);
		client = new HttpClient(httpConnectionManager);
	}

	private void initDb() {
		ds = new JdbcDataSource();
		Properties p = new Properties();
		p.put("driver", dbDriver);
		p.put("url", dbUri);
		p.put("user", dbUser);
		p.put("password", dbPassword);
		ds.init(p);
	}

	public SourcererGateway() {
//		initLogger();
	}

//	private void initLogger() {
//		 Formatter formatter = new Formatter() {
//		      @Override
//		      public String format(LogRecord record) {
//		        
//		      if (record.getLevel() == Level.INFO) {
//		          return formatInfo(record);
//		        } else {
//		          return formatError(record);
//		        }
//		      }
//		    };
//		  for(Handler h : logger.getHandlers()){
//			  h.setFormatter(formatter);
//		  }
//	}

	public SourcererGateway(String scsUrlPart, String codeUrlPart, String simUrlPart) {
		//this();
		if (scsUrlPart != null && scsUrlPart.length() > 0)
			this.urlPart = scsUrlPart;
		if (codeUrlPart != null && codeUrlPart.length() > 0)
			this.codeUrlPart = codeUrlPart;
		
		if (simUrlPart != null && simUrlPart.length() > 0)
			this.simUrlPart = simUrlPart;
		// initDb();
		
		
	}

	public SourcererGateway(String scsUrlPart, String codeUrlPart, String simUrlPart,
			String dbUri, String dbUser, String dbPassword) {
		//this();
		if (scsUrlPart != null && scsUrlPart.length() > 0)
			this.urlPart = scsUrlPart;
		if (codeUrlPart != null && codeUrlPart.length() > 0)
			this.codeUrlPart = codeUrlPart;
		if (simUrlPart != null && simUrlPart.length() > 0)
			this.simUrlPart = simUrlPart;
		setDb(dbUri, dbUser, dbPassword);
	}

	public void setDb(String uri, String user, String password) {
		this.dbUri = uri;
		this.dbUser = user;
		this.dbPassword = password;
		initDb();
	}

	public void setCodeServerUrl(String url) {
		this.codeUrlPart = url;
	}
	
	public void setSimServerUrl(String url) {
		this.simUrlPart = url;
	}

	public void setMltServerUrl(String url) {
		this.urlPart = url;
	}

	public static SourcererGateway getInstance(String scsUrl, String codeUrl, String simUrl) {
		if (obj == null)
			obj = new SourcererGateway(scsUrl, codeUrl, simUrl);

		return obj;
	}

	public static SourcererGateway getInstance(String scsUrl, String codeUrl, String simUrl,
			String dbUri, String dbUser, String dbPassword) {
		if (obj == null)
			obj = new SourcererGateway(scsUrl, codeUrl, simUrl, dbUri, dbUser,
					dbPassword);

		return obj;
	}

	private static SourcererGateway obj;

	// -- apis

	
	public String getCode(String entityId) {

		String queryString = "entityID=" + entityId;
		return sendGetCommand(queryString, codeUrlPart);
	}

	public String getComment(String jarCommentId) {
		String queryString = "commentID=" + jarCommentId;
		return sendGetCommand(queryString, codeUrlPart);
	}

	
	public String mltSnamesViaJdkUse(String entityId) {
		return HitFqnEntityIdToString(searchMltViaJdkUse(entityId));
	}

	public String mltSnamesViaLibUse(String entityId) {
		return HitFqnEntityIdToString(searchMltViaLibUse(entityId));
	}

	// public String mltSnamesViaLocalUse(String entityId){
	// return HitFqnEntityIdToString(searchMltViaLocalUse(entityId));
	// }

	public String mltSnamesViaJdkLibUse(String entityId) {
		return HitFqnEntityIdToString(searchMltViaJdkLibUse(entityId));
	}

	// public String mltSnamesViaAllUse(String entityId) {
	// return HitFqnEntityIdToString(searchMltViaAllUse(entityId));
	// }

	public String eidsViaMlt(String entityId) {
		return searchMltViaUse4eids(entityId);
	}
	
	public String eidsViaSimEntitiesTC(String entityId) {
		return searchSim4eids(entityId, "TC");
	}
	
	public String eidsViaSimEntitiesHD(String entityId) {
		return searchSim4eids(entityId, "HD");
	}
	
	public String snamesViaSimEntitiesTC(String entityId) {
		return searchSim(entityId, "TC");
	}
	
	public String snamesViaSimEntitiesHD(String entityId) {
		return searchSim(entityId, "HD");
	}

	
	private String searchMltViaUse4eids(String entityId) {
		return getEidsFromHits(searchMltJavabin4eids(entityId, "jdkLib_use_fqn_full"));
	}
	
	private String searchSim(String entityId, String simType) {
		if (ds == null)
			return "''";
		
		String eids = searchSim4eids(entityId, simType);
		
		if (eids.equals(""))
			return "''";
		
		String query = "SELECT e.fqn from entities as e"
			+ " WHERE entity_id in (" + eids + ")";
	
		return makeFqnsStringFromQueryResult(ds.getData(query));
	}

	/**
	 * @param entityId
	 * @param simType
	 * @return
	 */
	private String searchSim4eids(String entityId, String simType) {
		String queryString = "entityID=" + entityId
		+ "&simType=" + simType;

		String eids = "";
		eids = sendGetCommand(queryString, simUrlPart);

		if(eids == null){
			logger.log(Level.SEVERE,
					"Encountered null eids (which seems to be impossible). " +
					"EntityID: " + entityId + "Sim. Type: " + simType);
			return "''";
		}
		
		if(eids.length()<1 || eids.startsWith("Error"))
			return "''";
		
		return eids;
	}
	

	public List<HitFqnEntityId> searchMltViaJdkUse(String entityId) {
		return getFqnEntityIdFromHits(searchMltJavabin(entityId, "jdk_use_fqn_full"));
	}

	public List<HitFqnEntityId> searchMltViaLibUse(String entityId) {
		return getFqnEntityIdFromHits(searchMltJavabin(entityId, "lib_use_fqn_full"));
	}

	// public List<HitFqnEntityId> searchMltViaLocalUse(String entityId) {
	// return getFqnEntityIdFromHits(searchMlt(entityId, "local_use_fqn_full"));
	// }

	public List<HitFqnEntityId> searchMltViaJdkLibUse(String entityId) {
		return getFqnEntityIdFromHits(searchMltJavabin(entityId, "jdkLib_use_fqn_full"));
	}



	// public List<HitFqnEntityId> searchMltViaAllUse(String entityId) {
	// return getFqnEntityIdFromHits(searchMlt(entityId, "all_use_fqn_full"));
	// }

	// -- end apis

	private String getEidsFromHits(Object javabin) {

		if(javabin == null){
			return "''";
		}
		
		StringBuilder sb = new StringBuilder();
		
		NamedList<Object> nl = (NamedList<Object>) javabin;

		SolrDocumentList list = (SolrDocumentList) nl.getVal(2);

		if(list == null || list.size()<1){
			return "''";
		}
		
		for (int i = 0; i < list.size(); i++) {
			SolrDocument doc = list.get(i);
			sb.append(doc.getFieldValue("entity_id"));
			sb.append(", ");
		}
		
		if (sb.length()>0)
			return sb.substring(0, sb.length()-2);
		else 
			return "''";
	}
	
	private List<HitFqnEntityId> getFqnEntityIdFromHits(Object javabin) {

		List<HitFqnEntityId> hitsInfo = new LinkedList<HitFqnEntityId>();
		
		if(javabin == null) return hitsInfo;
		
		NamedList<Object> nl = (NamedList<Object>) javabin;
		
		

		SolrDocumentList list = (SolrDocumentList) nl.getVal(2);

		if(list == null || list.size()<1){
			return hitsInfo;
		}
		
		for (int i = 0; i < list.size(); i++) {
			SolrDocument doc = list.get(i);
			hitsInfo.add(new HitFqnEntityId(
					(String) doc.getFieldValue("fqn_full"),
					((Long) doc.getFieldValue("entity_id")).longValue() + "")
			);
		}
		
		return hitsInfo;

	}

	private String HitFqnEntityIdToString(List<HitFqnEntityId> h) {
		StringBuffer buf = new StringBuffer();
		for (HitFqnEntityId _h : h) {
			buf.append(_h.fqn);
			buf.append(" ");
		}
		return buf.toString().trim();
	}

	// List<HitFqnEntityId> searchMltViaJdkLibUsage(String entityId) {
	// return getFqnEntityIdFromHits(searchMlt(entityId,
	// "jdk_use_fqn_full,lib_use_fqn_full"));
	// }
	//
	//
	// List<HitFqnEntityId> searchMltViaAllUsage(String entityId) {
	// return getFqnEntityIdFromHits(searchMlt(entityId,
	// "jdk_use_fqn_full,lib_use_fqn_full,local_use_fqn_full"));
	// }

//	private String searchMlt(String entityId, String mltFields) {
//
//		String queryString = "start=0&rows=100&q=entity_id:" + entityId
//				+ "&mlt.fl=" + mltFields
//				+ "&mlt.mindf=3&mlt.mintf=1&fl=fqn_full,entity_id"
//		// + "&mlt.boost=true"
//		;
//
//		String result = "";
//		result = sendGetCommand(queryString, urlPart);
//
//		return result;
//	}

	private Object searchMltJavabin(String eid, String mltFields) {
		String queryString = "start=0&rows=100&q=entity_id:" + eid + "&mlt.fl="
				+ mltFields + "&mlt.mindf=3&mlt.mintf=1&fl=fqn_full,entity_id"
				+ "&wt=javabin"
		// + "&mlt.boost=true"
		;

		Object result = null;
		result = getStreamResult(queryString, urlPart, true);
		return result;
	}
	
	private Object searchMltJavabin4eids(String eid, String mltFields) {
		String queryString = "start=0&rows=100&q=entity_id:" + eid + "&mlt.fl="
				+ mltFields + "&mlt.mindf=3&mlt.mintf=1&fl=entity_id"
				+ "&wt=javabin"
		// + "&mlt.boost=true"
		;

		Object result = null;
		result = getStreamResult(queryString, urlPart, true);
		
		return result;
	}

	/**
	 * Send the command to Solr using a GET
	 * 
	 * @param queryString
	 * @param url
	 * @return
	 * @throws IOException
	 */
	private String sendGetCommand(String queryString, String url) {
		Object results = (String) getStreamResult(queryString, url, false);
		
		if(results == null )
			return "";
		else
			return (String) results;
	}

	private Object getStreamResult(String queryString, String url,
			boolean binary) {
		Object result = null;

		GetMethod get = new GetMethod(url);
		get.setQueryString(queryString.trim());

		get.addRequestHeader("Cache-Control", "no-cache");
		get.setFollowRedirects(false);

		get.getParams().setParameter("http.socket.timeout", getTimeout());
		try {
			client.executeMethod(get);

			try {
				// Execute the method.
				int statusCode = get.getStatusCode();

				if (statusCode != HttpStatus.SC_OK) {
					logger.log(Level.SEVERE, "Method failed: "
							+ get.getStatusLine() + " [" + queryString + "]");
				}

				if (binary) {
					result = getJavabinFromStream(get.getResponseBodyAsStream());
				} else {

					result = getStringFromStream(get.getResponseBodyAsStream());
				}
			} catch (HttpException e) {
				logger.log(Level.SEVERE, "[" + queryString
						+ "] Fatal protocol violation: " + e.getMessage());
			} catch (IOException e) {
				logger.log(Level.SEVERE, "[" + queryString + "] IOE "
						+ e.getMessage());
			} finally {
				// Release the connection.
				get.releaseConnection();
			}

		} catch (HttpException e1) {
			logger.log(Level.SEVERE, "HttpExp: [" + queryString + "] " + e1.getMessage());
		} catch (IOException e1) {
			logger.log(Level.SEVERE, "Timeout: [" + queryString + "] " + e1.getMessage());
		}

		return result;
	}

	private Object getTimeout() {
		return new Integer(this.timeout);
	}
	
	public void setTimeout(String strTimeout){
		if(strTimeout.matches("[0-9]+")){
			this.timeout = strTimeout;
			logger.info("Timeout set to " + strTimeout + " ms");
		} else {
			logger.warning("Invalid value for Timeout " + strTimeout +  ". Using default value of " + this.getTimeout());
		}
	}

	private Object getJavabinFromStream(InputStream is) {

		Object m1 = null;
		try {
			m1 = new JavaBinCodec().unmarshal(is);
		} catch (IOException e) {
			logger.log(Level.SEVERE,
					"IOE: " + e.getMessage());
		}

		return m1;
	}

	// got this method from sourcerer
	// query: string from input stream
	private static String getStringFromStream(InputStream isData)
			throws IOException {
		ByteArrayOutputStream baosData = new ByteArrayOutputStream();

		byte[] abyteBuffer = new byte[1024];

		int nBytesRead = 0;
		while ((nBytesRead = isData.read(abyteBuffer)) >= 0) {
			baosData.write(abyteBuffer, 0, nBytesRead);
		}
		baosData.close();

		return baosData.toString();
	}

	private List<HitFqnEntityId> getFqnEntityIdFromHits(String xmlResultInString) {

		List<HitFqnEntityId> hitsInfo = new LinkedList<HitFqnEntityId>();

		if (xmlResultInString == null || xmlResultInString.length() == 0)
			return hitsInfo;

		Node responseNode = getResponseNode(xmlResultInString);

		if (responseNode == null)
			return hitsInfo;

		org.w3c.dom.NodeList hits = responseNode.getChildNodes();

		for (int i = 0; i < hits.getLength(); i++) {
			String fqn = "";
			String entity_id = "";

			Node hit = hits.item(i);
			if (!hit.getNodeName().equals("doc"))
				continue;

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

	private Node getResponseNode(String xmlResultInString) {

		DocumentBuilder db = null;
		try {
			db = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			// System.err.println("parser error");
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

		// Document doc = XMLParser.parse(xmlResultInString);
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

		while (data.hasNext()) {
			sb.append((String) (data.next().get("fqn")));
			sb.append(" ");
		}

		return sb.toString().trim();
	}

}
