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
package edu.uci.ics.sourcerer.eval.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import edu.uci.ics.sourcerer.eval.client.Query;
import edu.uci.ics.sourcerer.eval.client.Result;
import edu.uci.ics.sourcerer.scs.common.JavaToHtml;
import edu.uci.ics.sourcerer.scs.common.SourcererSearchAdapter;
import edu.uci.ics.sourcerer.scs.common.client.SearchHeuristic;
import edu.uci.ics.sourcerer.scs.server.SourcererSearchServiceImpl;

public class QueryAggregator {
  private static QueryAggregator instance = null;

  private int count = 0;
  
  private Map<String, Query> queries;
  private Map<Query, Collection<Result>> resultMap;
  
  private File queryResultsDir;

  private QueryAggregator() {
  }

  private void asyncInit(final ServletContext context) {
    new Thread(new Runnable() {
      public void run() {
        queryResultsDir = new File(context.getRealPath("evaluation/results/queries"));
        queryResultsDir.mkdirs();
        initializeFromFile(context);
      }
    }).start();
  }

  public synchronized void initializeFromFile(ServletContext context) {
    queries = new HashMap<String, Query>();
    resultMap = new HashMap<Query, Collection<Result>>();

    try {
      BufferedReader br = new BufferedReader(new InputStreamReader(context.getResourceAsStream("/evaluation/queries.txt")));
      // first line should be number of queries
      count = Integer.parseInt(br.readLine());
      for (String line = br.readLine(); line != null; line = br.readLine()) {
        String[] parts = line.split("\\|");
        queries.put(parts[0], Query.getQuery(parts[0], parts[1], parts[2]));
      }
      File voteDir = new File(context.getRealPath("/evaluation/results/votes/"));
      if (voteDir.exists()) {
        for (File email : voteDir.listFiles()) {
          if (email.isDirectory()) {
            for (File vote : email.listFiles()) {
              if (vote.isFile() && vote.getName().endsWith(".votes")) {
                String id = vote.getName();
                id = id.substring(0, id.lastIndexOf('.'));
                queries.get(id).addCompleted(email.getName());
              }
            }
          }
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public synchronized Query getQuery(String query) {
    return queries.get(query);
  }
  
  public synchronized Query getNextQuery(Set<String> completed, Set<String> partiallyCompleted) {
    if (partiallyCompleted.isEmpty()) {
      Query lowest = null;
      for (Query query : queries.values()) {
        if (!completed.contains(query)) {
          if (lowest == null) {
            lowest = query;
          } else if (query.getScore() < lowest.getScore()) {
            lowest = query;
          }
        }
      }
      return lowest;
    } else {
      return queries.get(partiallyCompleted.iterator().next());
    }
  }
  
  public synchronized Iterator<Result> getResultIterator(Query query) {
    Collection<Result> results = resultMap.get(query);
    if (results == null) {
      results = aggregateResults(query);
      resultMap.put(query, results);
    }
    return results.iterator();
  }
  
  public synchronized int getQueryCount() {
    return queries.size();
  }
  
  public synchronized Collection<String> getQueries() {
    LinkedList<String> retval = new LinkedList<String>(queries.keySet());
    Collections.sort(retval, new Comparator<String>() {
      public int compare(String a, String b) {
        int anum = Integer.parseInt(a.substring(1));
        int bnum = Integer.parseInt(b.substring(1));
        return anum - bnum;
      }
    });
    return retval;
  }
  
  public synchronized int getResultCount(Query query) {
    Collection<Result> results = resultMap.get(query);
    if (results == null) {
      results = aggregateResults(query);
      resultMap.put(query, results);
    }
    return results.size();
  }

  private Collection<Result> aggregateResults(Query query) {
    Collection<Result> results = new HashSet<Result>();
    File queryResults = new File(queryResultsDir, query.getQueryID());
    if (!queryResults.exists()) {
      queryResults.mkdir();
    }
      
    try {
      XPath xpath = XPathFactory.newInstance().newXPath();
      
      for (SearchHeuristic heuristic : SearchHeuristic.values()) {
        if (heuristic == SearchHeuristic.NONE) {
          continue;
        }
        
        File resultFile = new File(queryResults, heuristic.name() + ".scs");
        if (resultFile.exists()) {
          BufferedReader br = new BufferedReader(new FileReader(resultFile));
          for (String line = br.readLine(); line != null; line = br.readLine()) {
            String entityID = line.substring(0, line.indexOf(' '));
            String fqn = line.substring(line.indexOf(' ') + 1);
            String formattedCode = SourcererSearchAdapter.getEntityCode(entityID);
            Result result = Result.getResult(entityID, fqn, formattedCode);
            results.add(result);
          }
          br.close();
        } else {
          String resultXML = SourcererSearchAdapter.searchSCSServer(query.getQueryText(), 0, count, heuristic);
          InputSource input = new InputSource(new StringReader(resultXML));
          NodeList nodes = (NodeList)xpath.evaluate("/response/result[@name='response']/doc", input, XPathConstants.NODESET);
          
          BufferedWriter out = new BufferedWriter(new FileWriter(resultFile));
        
          for (int index = 0; index < nodes.getLength(); index++) {
            Node node = (Node)nodes.item(index);
            String entityID = (String)xpath.evaluate("./long[@name='entity_id']", node, XPathConstants.STRING);
            String fqn = (String)xpath.evaluate("./str[@name='fqn_full']", node, XPathConstants.STRING);
            String formattedCode = SourcererSearchAdapter.getEntityCode(entityID, null, null);
            out.write(entityID + " " + fqn + "\n");
            Result result = Result.getResult(entityID, fqn, formattedCode);
            results.add(result);
          }
          
          out.close();
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
      results.clear();
    }

    return results;
  }

//  private String executeQuery(String query, SearchHeuristic heuristic) {
//    String urlPart = "http://kathmandu.ics.uci.edu:8984/solr/scs/select/";
//
//    String queryString = "start=0&rows=30" + SourcererSearchServiceImpl.buildQueryPart(query, heuristic);
//
//    String result = "";
//
//    try {
//      result = sendGetCommand(queryString, urlPart);
//    } catch (IOException e) {
//      result = e.getMessage();
//    }
//
//    return result;
//  }
  
//  private String getEntityCode(String entityId){
//    String urlPart = "http://nile.ics.uci.edu:9180/repofileserver/" ;//?entityID=5096973";
//    String queryString = "entityID=" + entityId;
//    
//    try {
//      
//      String javaCode = sendGetCommand(queryString, urlPart); 
//      
//      j2h.reset();
//      
//      return "<div class=\"result_code\">" + j2h.process(javaCode) + "</div>";
//      
//    } catch (IOException e) {
//      return "error: " + e.getMessage();
//    }
//  }
  
//  private String sendGetCommand(String queryString, String url) throws IOException {
//    String results = null;
//    HttpClient client = new HttpClient();
//    GetMethod get = new GetMethod(url);
//    get.setQueryString(queryString.trim());
//
//    client.executeMethod(get);
//    try {
//      // Execute the method.
//      int statusCode = client.executeMethod(get);
//
//      if (statusCode != HttpStatus.SC_OK) {
//        System.err.println("Method failed: " + get.getStatusLine());
//        results = "Method failed: " + get.getStatusLine();
//      }
//
//      results = getStringFromStream(get.getResponseBodyAsStream());
//    } catch (HttpException e) {
//      System.err.println("Fatal protocol violation: " + e.getMessage());
//      e.printStackTrace();
//    } catch (IOException e) {
//      System.err.println("Fatal transport error: " + e.getMessage());
//      e.printStackTrace();
//    } finally {
//      // Release the connection.
//      get.releaseConnection();
//    }
//    return results;
//  }
  
//  private static String getStringFromStream(InputStream in) throws IOException {
//    ByteArrayOutputStream out = new ByteArrayOutputStream();
//
//    byte[] buff = new byte[1024];
//    int read = 0;
//    while ((read = in.read(buff)) >= 0) {
//      out.write(buff, 0, read);
//    }
//
//    return out.toString();
//  }

  public synchronized static QueryAggregator getQueryAggregator(ServletContext context) {
    if (instance == null) {
      instance = new QueryAggregator();
      instance.asyncInit(context);
    }
    return instance;
  }
}
