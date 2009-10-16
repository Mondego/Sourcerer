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

import static edu.uci.ics.sourcerer.eval.Main.TOP_K;
import static edu.uci.ics.sourcerer.eval.Main.TUPLE_MODE;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import edu.uci.ics.sourcerer.util.Averager;
import edu.uci.ics.sourcerer.util.Helper;
import edu.uci.ics.sourcerer.util.io.Property;
import edu.uci.ics.sourcerer.util.io.TablePrettyPrinter;
import edu.uci.ics.sourcerer.util.io.properties.StringProperty;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class CalculatePrecisionRecall {
  public static final Property<String> RELEVANT_LIST = new StringProperty("relevant-list", "FUL,PSP", "Evaluation Stats", "Vote options to consider relevant.");
  public static final Property<String> PR_FILE = new StringProperty("pr-file", "pr.txt", "Evaluation Stats", "Filename for precision/recall results.");
  
  public static void calculate() {
    // Compute the precision/recall for the top k
    int top = TOP_K.getValue();
    
    // The list of responses to consider relevant
    String relevantOptions = RELEVANT_LIST.getValue();
    Set<String> relevantOptionsSet = Helper.newHashSet();
    for (String string : relevantOptions.split(",")) {
      relevantOptionsSet.add(VoteOptions.getVoteFromAbbreviation(string));
    }
    
    // Tuple mode is for easier consumption by R
    boolean tupleMode = TUPLE_MODE.getValue();
    
    // Load the evaluation results
    EvaluationResults results = EvaluationResults.loadResults();
    
    // Initialize the table printer
    TablePrettyPrinter printer = TablePrettyPrinter.getTablePrettyPrinter(PR_FILE);
    printer.setFractionDigits(3);
    
    if (tupleMode) {
      printer.beginTable(6);
      printer.addDividerRow();
      printer.beginRow();
      printer.addCell("Query");
      printer.addCell("User");
      printer.addCell("Relevant");
      printer.addCell("Heuristic");
      printer.addCell("P");
      printer.addCell("R");
      printer.addDividerRow();
    }
    
    Map<String, Averager<Double>> globalPrecisionMap = Helper.newHashMap();
    Map<String, Averager<Double>> globalRecallMap = Helper.newHashMap();

    // Collect all the heuristics, in case a single query is missing some
    Collection<String> globalHeuristics = Helper.newHashSet();
    
    // Go through every query
    for (Query query : results.getQueries()) {
      // Look up the heuristics for this query
      Collection<String> heuristics = Helper.newLinkedList();
      heuristics.addAll(query.getHeuristics());
      globalHeuristics.addAll(heuristics);
      
      Map<String, Averager<Double>> queryPrecisionMap = Helper.newHashMap();
      Map<String, Averager<Double>> queryRecallMap = Helper.newHashMap();
      
      // Go through every vote (person) group for this query
      Collection<Votes> result = results.getVotes(query);
      for (Votes votes : result) {
         if (!tupleMode) {
          printer.beginTable(3);
          printer.addHeader("Query " + query.getName() + " for user " + votes.getUser());
          printer.addDividerRow();
          printer.beginRow();
          printer.addCell("Heuristic");
          printer.addCell("P");
          printer.addCell("R");
          printer.addDividerRow();
        }
        
        // For each heuristic
        for (String heuristic : heuristics) {
          printer.beginRow();
          if (tupleMode) {
           printer.addCell(query.getName());
           printer.addCell(votes.getUser());
           printer.addCell(relevantOptions);
          }
          printer.addCell(heuristic);
          
          // Compute the precision/recall
          // Get the relevant results
          Set<String> relevant = votes.getRelevant(query.getResults(), relevantOptionsSet);
          int unionCount = query.getUnionCount(relevant, heuristic, top);
          // Precision is the ratio of relevant responses in the top
          double p = ((double) unionCount) / ((double) top);
          // Recall is the ratio of relevant responses in the top to total relevant responses
          double r = ((double) unionCount) / ((double) relevant.size());
          printer.addCell(p);
          Helper.getFromMap(queryPrecisionMap, heuristic, Averager.class).addValue(p);
          Helper.getFromMap(globalPrecisionMap, heuristic, Averager.class).addValue(p);
          printer.addCell(r);
          Helper.getFromMap(queryRecallMap, heuristic, Averager.class).addValue(r);
          Helper.getFromMap(globalRecallMap, heuristic, Averager.class).addValue(r);
        }
        printer.addDividerRow();
        if (!tupleMode) {
          printer.endTable();
        }
      }
      
      // Print out the average results for this query
      if (!tupleMode && result.size() > 1) {
        printer.beginTable(3);
        printer.addHeader("Query " + query.getName() + " averaged results");
        printer.addDividerRow();
        printer.beginRow();
        printer.addCell("Heuristic");
        printer.addCell("P");
        printer.addCell("R");
        printer.addDividerRow();
        
        // For each heuristic
        for (String heuristic : heuristics) {
          printer.beginRow();
          printer.addCell(heuristic);
          printer.addCell(queryPrecisionMap.get(heuristic).getCellValueWithStandardDeviation());
          printer.addCell(queryRecallMap.get(heuristic).getCellValueWithStandardDeviation());
        }
        printer.addDividerRow();
        printer.endTable();
      }
    }
    
    // Print out the average results for this query
    if (!tupleMode) {
      printer.beginTable(3);
      printer.addHeader("Overall averaged results");
      printer.addDividerRow();
      printer.beginRow();
      printer.addCell("Heuristic");
      printer.addCell("P");
      printer.addCell("R");
      printer.addDividerRow();
      
      // For each heuristic
      for (String heuristic : globalHeuristics) {
        printer.beginRow();
        printer.addCell(heuristic);
        printer.addCell(globalPrecisionMap.get(heuristic).getCellValueWithStandardDeviation());
        printer.addCell(globalRecallMap.get(heuristic).getCellValueWithStandardDeviation());
      }
      printer.addDividerRow();
      printer.endTable();
    }
    
    if (tupleMode) {
      printer.endTable();
    }
  }
}
