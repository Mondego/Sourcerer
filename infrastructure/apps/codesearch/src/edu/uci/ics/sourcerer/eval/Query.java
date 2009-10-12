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

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
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
