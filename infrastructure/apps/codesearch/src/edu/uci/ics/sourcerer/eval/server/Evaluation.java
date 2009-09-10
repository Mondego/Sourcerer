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
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import edu.uci.ics.sourcerer.eval.client.EvaluationProgress;
import edu.uci.ics.sourcerer.eval.client.Query;
import edu.uci.ics.sourcerer.eval.client.Result;
import edu.uci.ics.sourcerer.eval.client.Vote;

public class Evaluation {
  private String email;
  
  private QueryAggregator aggregator;
  
  private File outputDir;
  private Set<String> completed;
  private Set<String> partiallyCompleted;
  
  private Query query;

  private Iterator<Result> resultIterator;
  private Result result;
  private Set<String> partialResults;
  private int resultCount = 0;
  
  private File resultFile;
  private BufferedWriter writer;
  
  public Evaluation(String email, QueryAggregator aggregator, File outputDir) {
    this.email = email;
    this.aggregator = aggregator;
    this.outputDir = outputDir;
    outputDir.mkdirs();
    determineCompleted();
  }
  
  private void determineCompleted() {
    completed = new HashSet<String>();
    partiallyCompleted = new HashSet<String>();
    for (File voteFile : outputDir.listFiles()) {
      if (voteFile.getName().endsWith(".votes")) {
        String id = voteFile.getName();
        id = id.substring(0, id.lastIndexOf('.'));
        completed.add(id);
      } else if (voteFile.getName().endsWith(".tmp")) {
        String id = voteFile.getName();
        id = id.substring(0, id.lastIndexOf('.'));
        partiallyCompleted.add(id);
      }
    }
  }
  
  public synchronized EvaluationProgress getProgress() {
    return EvaluationProgress.getEvaluationProgress(email, completed, partiallyCompleted, aggregator.getQueries());
  }
   
  public synchronized Query getNextQuery(String newQuery) {
    if (query == null) {
      if (newQuery == null) {
        query = aggregator.getNextQuery(completed, partiallyCompleted);
      } else {
        query = aggregator.getQuery(newQuery);
      }
      if (query != null) {
        partiallyCompleted.add(query.getQueryID());
        query.addInProgress(email);
        query.setTotalResults(aggregator.getResultCount(query));
        resultIterator = aggregator.getResultIterator(query);
        resultCount = 0;
        resultFile = new File(outputDir, query.getQueryID() + ".tmp");
        partialResults = new HashSet<String>();
        if (resultFile.exists()) {
          try {
            BufferedReader br = new BufferedReader(new FileReader(resultFile));
            for (String line = br.readLine(); line != null; line = br.readLine()) {
              String[] parts = line.split(" ");
              partialResults.add(parts[1]);
            }
            br.close();
            resultCount += partialResults.size();
          } catch (IOException e) {
            e.printStackTrace();
          }
        }
        try {
          writer = new BufferedWriter(new FileWriter(resultFile, true));
        } catch (IOException e) {
          e.printStackTrace();
          resultIterator = null;
          resultFile = null;
          query = null;
          return null;
        }
      }
    }
    return query;
  }
  
  public synchronized Result getNextResult() {
    if (query == null) {
      return null;
    } else if (result == null) {
      while (result == null && resultIterator.hasNext()) {
        result = resultIterator.next();
        if (partialResults.contains(result.getEntityID())) {
          result = null;
        }
      }
      if (result == null) {
        try {
          writer.close();
          resultFile.renameTo(new File(outputDir, query.getQueryID() + ".votes"));
        } catch (IOException e) {
          e.printStackTrace();
        }
        completed.add(query.getQueryID());
        partiallyCompleted.remove(query.getQueryID());
        query.addCompleted(email);
        resultIterator = null;
        query = null;
        partialResults = null;
        writer = null;
        resultFile = null;
      } else {
        result.setNumber(++resultCount);
      }
    }

    return result;
  }
  
  public synchronized void reportVote(String id, Vote vote) {
    if (result != null && result.getEntityID().equals(id)) {
      result = null;
      String line = id + " " + vote.getVoteResult();
      if (writer == null) {
        System.out.println(line);
      } else {
        try {
          writer.write(line + "\n");
          writer.flush();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }
}
