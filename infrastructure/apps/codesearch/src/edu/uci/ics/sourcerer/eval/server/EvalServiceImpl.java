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

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import edu.uci.ics.sourcerer.eval.client.EvalService;
import edu.uci.ics.sourcerer.eval.client.EvaluationProgress;
import edu.uci.ics.sourcerer.eval.client.Query;
import edu.uci.ics.sourcerer.eval.client.Result;
import edu.uci.ics.sourcerer.eval.client.Vote;

public class EvalServiceImpl extends RemoteServiceServlet implements EvalService {
  private static final long serialVersionUID = -4423428496883704335L;
  private Map<String, Evaluation> evalMap = new HashMap<String, Evaluation>();
  private QueryAggregator aggregator = null;

  public synchronized String getIntroductionText() {
    if (aggregator == null) {
      aggregator = QueryAggregator.getQueryAggregator(getServletContext());
    }
    return "Welcome to the Sourcerer Code Search Evaluation Tool! Please press the button to begin";
  }
  
  public Vote[] getVoteOptions() {
    Vote[] options = new Vote[3];
    options[0] = new Vote("2", "The result fully solves the problem", 
        "Code can be copied with little modifiction",
        "Contains everything I needed to know",
        "Contains all the APIs needed",
        "Contains additional related information");
    options[1] = new Vote("1", "The result partially solves the problem",
        "Following references looks promising",
        "Contains something I needed to know",
        "Contains some of the APIs needed");
    options[2] = new Vote("0", "The result is not useful at all",
        "Too long, did not read",
        "An implementation of the functionality rather than an API usage example");
    return options;
  }
  
  public synchronized EvaluationProgress getEvaluationProgress(String email) {
    try {
      Evaluation eval = evalMap.get(email);
      if (eval == null) {
        File file = new File(getServletContext().getRealPath("/evaluation/results/votes/" + email));
        file.getParentFile().mkdirs();
        eval = new Evaluation(email, aggregator, file);
        evalMap.put(email, eval);
      }
      return eval.getProgress();
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }
  
  public Query getNextQuery(String email, String query) {
    Evaluation eval = evalMap.get(email);
    return eval.getNextQuery(query);
  }
  
  public Result getNextResult(String email) {
    Evaluation eval = evalMap.get(email);
    return eval.getNextResult();
  }
  
  public void reportVote(String email, String id, Vote vote) {
    Evaluation eval = evalMap.get(email);
    eval.reportVote(id, vote);
  }
}
