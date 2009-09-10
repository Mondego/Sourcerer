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
package edu.uci.ics.sourcerer.eval.client;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class Query implements Serializable {
  private static final long serialVersionUID = 7972033461638414502L;
  
  private String id;
  private String text;
  private String description;
  private transient Set<String> completed;
  private transient Set<String> inProgress;
  private int totalResults;
  
  private Query() {}
  
  public static Query getQuery(String id, String text, String description) {
    Query query = new Query();
    query.id = id;
    query.text = text;
    query.description = description;
    query.completed = new HashSet<String>();
    query.inProgress = new HashSet<String>();
    return query;
  }
  
  public String getQueryID() {
    return id;
  }
  
  public String getQueryText() {
    return text;
  }
  
  public String getQueryDescription() {
    return description;
  }
  
  public void addCompleted(String email) {
    inProgress.remove(email);
    completed.add(email);
  }
  
  public void addInProgress(String email) {
    if (!completed.contains(email)) {
      inProgress.add(email);
    }
  }
    
  public int getScore() {
    return completed.size() + inProgress.size();
  }
  
  public void setTotalResults(int totalResults) {
    this.totalResults = totalResults;
  }
  public int getTotalResults() {
    return totalResults;
  }
}
