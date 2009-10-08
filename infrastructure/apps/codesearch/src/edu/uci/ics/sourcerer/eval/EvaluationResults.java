package edu.uci.ics.sourcerer.eval;

import static edu.uci.ics.sourcerer.util.io.Logging.logger;

import java.io.File;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

import edu.uci.ics.sourcerer.util.Helper;
import edu.uci.ics.sourcerer.util.io.Property;
import edu.uci.ics.sourcerer.util.io.PropertyManager;

public class EvaluationResults {
  private Map<String, Query> queries;
  private Map<String, Collection<Votes>> votesByQuery;
  
  public EvaluationResults() {
    votesByQuery = Helper.newHashMap();
    queries = Helper.newHashMap();
  }
  
  public Query getQuery(String name) {
    Query query = queries.get(name);
    if (query == null) {
      query = new Query(name);
      queries.put(name, query);
    }
    return query;
  }
  
  public void addVotes(String user, File log) {
    String query = log.getName();
    query = query.substring(0, query.lastIndexOf('.'));
    
    Collection<Votes> votes = votesByQuery.get(query);
    if (votes == null) {
      votes = Helper.newLinkedList();
      votesByQuery.put(query, votes);
    }
    
    Votes result = new Votes(user);
    result.addVotes(log);
    votes.add(result);
  }
  
  public Collection<Votes> getVotes(Query query) {
    return Helper.getFromMap(votesByQuery, query.getName(), LinkedList.class);
  }

  public static EvaluationResults loadResults() {
    PropertyManager properties = PropertyManager.getProperties();
    
    // Load the evaluation results
    File input = properties.getValueAsFile(Property.INPUT);
   
    EvaluationResults results = new EvaluationResults();
    
    // Load the queries
    File queryDir = new File(input, "queries");
    if (!queryDir.exists()) {
      logger.severe("Query directory does not exist: " + queryDir.getPath());
      return null;
    }
    
    for (File dir : queryDir.listFiles()) {
      if (dir.isDirectory()) {
        Query query = results.getQuery(dir.getName());
        for (File heuristic : dir.listFiles()) {
          if (heuristic.isFile() && heuristic.getName().endsWith(".scs")) {
            query.addResults(heuristic);
          }
        }
      }
    }
    
    // Load the votes
    File voteDir = new File(input, "votes");
    if (!voteDir.exists()) {
      logger.severe("Votes directory does not exist: " + voteDir.getPath());
      return null;
    }
    
    for (File user : voteDir.listFiles()) {
      if (user.isDirectory()) {
        for (File log : user.listFiles()) { 
          if (log.isFile() && log.getName().endsWith(".votes")) {
            results.addVotes(user.getName(), log);
          }
        }
      }
    }
    
    return results;
  }
  
  public Iterable<Query> getQueries() {
    return queries.values();
  }
}
