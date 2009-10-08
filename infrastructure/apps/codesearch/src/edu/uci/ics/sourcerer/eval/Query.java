package edu.uci.ics.sourcerer.eval;

import static edu.uci.ics.sourcerer.util.io.Logging.logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import edu.uci.ics.sourcerer.util.Helper;

public class Query {
  private String name;
  private Map<String, Collection<String>> resultsByHeuristic;
  private Set<String> results;
  
  public Query(String name) {
    this.name = name;
    resultsByHeuristic = Helper.newHashMap();
    results = Helper.newHashSet();
  }
  
  public String getName() {
    return name;
  }
  
  public void addResults(File file) {
    String heuristic = file.getName();
    heuristic = heuristic.substring(0, heuristic.lastIndexOf('.'));
    Collection<String> queryResult = getQueryResult(heuristic);
    try {
      BufferedReader br = new BufferedReader(new FileReader(file));
      for (String line = br.readLine(); line != null; line = br.readLine()) {
        String[] parts = line.split(" ");
        queryResult.add(parts[0]);
        results.add(parts[0]);
      }
      br.close();
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Error reading scs file: " + file.getPath(), e);
    }
  }
  
  private Collection<String> getQueryResult(String heuristic) {
    Collection<String> queryResult = resultsByHeuristic.get(heuristic);
    if (queryResult == null) {
      queryResult = Helper.newLinkedList();
      resultsByHeuristic.put(heuristic, queryResult);
    }
    return queryResult;
  }
  
  public Set<String> getTopResults(String heuristic, int top) {
    Collection<String> results = getQueryResult(heuristic);
    Set<String> topResults = Helper.newHashSet();
    for (String result : results) {
      if (top-- <= 0) {
        break;
      }
      topResults.add(result);
    }
    return topResults;
  }
  
  public Collection<String> getHeuristics() {
    return resultsByHeuristic.keySet();
  }
}
