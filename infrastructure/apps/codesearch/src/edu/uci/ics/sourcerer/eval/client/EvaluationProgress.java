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
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;

public class EvaluationProgress implements Serializable {
  private static final long serialVersionUID = 1551418873535398732L;
  
  private String email;
  private Collection<String> completedQueries;
  private Collection<String> partialQueries;
  private Collection<String> allQueries;
  
  private EvaluationProgress() {}
  
  public static EvaluationProgress getEvaluationProgress(String email, Collection<String> completedQueries, Collection<String> partialQueries, Collection<String> allQueries) {
    EvaluationProgress progress = new EvaluationProgress();
    progress.email = email;
    progress.completedQueries = completedQueries;
    progress.partialQueries = partialQueries;
    progress.allQueries = allQueries;
    return progress;
  }

  public String getEmail() {
    return email;
  }

  public int getTotalQueries() {
    return allQueries.size();
  }
  
  public Collection<String> getCompletedQueries() {
    return completedQueries;
  }

  public Collection<String> getPartialQueries() {
    return partialQueries;
  }
  
  public Collection<String> getNewQueries() {
    Collection<String> newQueries = new LinkedList<String>();
    newQueries.addAll(allQueries);
    newQueries.removeAll(completedQueries);
    newQueries.removeAll(partialQueries);
    return newQueries;
  }
}
