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

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import edu.uci.ics.sourcerer.util.Averager;
import edu.uci.ics.sourcerer.util.Helper;
import edu.uci.ics.sourcerer.util.io.Property;
import edu.uci.ics.sourcerer.util.io.PropertyManager;
import edu.uci.ics.sourcerer.util.io.TablePrettyPrinter;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class CalculateTopStats {
  public static void calculate() {
    PropertyManager properties = PropertyManager.getProperties();
    int top = properties.getValueAsInt(Property.K);
    
    EvaluationResults results = EvaluationResults.loadResults();
    
    // Print out the results
    TablePrettyPrinter printer = TablePrettyPrinter.getTablePrettyPrinter(properties, Property.STATS_FILE);
    printer.setCSVMode(properties.isSet(Property.CSV_MODE));
    printer.setFractionDigits(3);
    
    // Collect the heuristics
    Collection<String> heuristics = Helper.newHashSet();
    for (Query query : results.getQueries()) {
      heuristics.addAll(query.getHeuristics());
    }
    
    boolean tupleMode = properties.isSet(Property.TUPLE_MODE);
    if (tupleMode) {
      printer.beginTable(heuristics.size() + 4);
      printer.addDividerRow();
      printer.beginRow();
      printer.addCell("Query");
      printer.addCell("User");
      printer.addCell("Vote");
      printer.addCell("Vote.Type");
      for (String heuristic : heuristics) {
        printer.addCell(heuristic);
      }
      printer.addDividerRow();
    }
    // option -> heuristic -> averager map
    Map<String, Map<String, Averager<Integer>>> globalMap = Helper.newHashMap();
    
    // Go through every query
    for (Query query : results.getQueries()) {
      Map<String, Map<String, Averager<Integer>>> queryMap = Helper.newHashMap();

      // Go through every vote group for this query
      for (Votes votes : results.getVotes(query)) {
         if (!tupleMode) {
          printer.beginTable(heuristics.size() + 1);
          printer.addHeader("Query " + query.getName() + " for user " + votes.getUser());
          printer.addDividerRow();
          printer.beginRow();
          printer.addCell("");
          for (String heuristic : heuristics) {
            printer.addCell(heuristic);
          }
          printer.addDividerRow();
        }
        
        
        for (String option : Vote.getTextOptions()) {
          // Do the main option
          printer.beginRow();
          if (tupleMode) {
           printer.addCell(query.getName());
           printer.addCell(votes.getUser());
           printer.addCell(VoteOptions.getVoteAbbreviation(option));
          }
          printer.addCell(option);
          Map<String, Averager<Integer>> globalHeuristicMap = Helper.getHashMapFromMap(globalMap, option);
          Map<String, Averager<Integer>> queryHeuristicMap = Helper.getHashMapFromMap(queryMap, option);
          
          for (String heuristic : heuristics) {
            Set<String> topResults = query.getTopResults(heuristic, top);
            int inTop = votes.getUnionCount(topResults, option, null);
            printer.addCell(inTop);
            Helper.getFromMap(globalHeuristicMap, heuristic, Averager.class).addValue(inTop);
            Helper.getFromMap(queryHeuristicMap, heuristic, Averager.class).addValue(inTop);
          }
          
          // Do the suboptions
          for (String subOption : Vote.getSubOptions(option)) {
            printer.beginRow();
            if (tupleMode) {
              printer.addCell(query.getName());
              printer.addCell(votes.getUser());
              printer.addCell(VoteOptions.getVoteAbbreviation(subOption));
             }
            printer.addCell(subOption);
            globalHeuristicMap = Helper.getHashMapFromMap(globalMap, option + subOption);
            queryHeuristicMap = Helper.getHashMapFromMap(queryMap, option + subOption);
            
            for (String heuristic : heuristics) {
              Set<String> topResults = query.getTopResults(heuristic, top);
              int inTop = votes.getUnionCount(topResults, option, subOption);
              printer.addCell(inTop);
              Helper.getFromMap(globalHeuristicMap, heuristic, Averager.class).addValue(inTop);
              Helper.getFromMap(queryHeuristicMap, heuristic, Averager.class).addValue(inTop);
            }
          }
        }
        printer.addDividerRow();
        if (!tupleMode) {
          printer.endTable();
        }
      }
      
      // Print out the average results for this query
      if (!tupleMode) {
        printer.beginTable(heuristics.size() + 1);
        printer.addHeader("Query " + query.getName() + " averaged results");
        printer.addDividerRow();
        printer.beginRow();
        printer.addCell("");

        for (String heuristic : heuristics) {
          printer.addCell(heuristic);
        }
        printer.addDividerRow();
        
        for (String option : Vote.getTextOptions()) {
          // Do the main option
          printer.beginRow();
          printer.addCell(option);
          Map<String, Averager<Integer>> queryHeuristicMap = Helper.getHashMapFromMap(queryMap, option);
          
          for (String heuristic : heuristics) {
            printer.addCell(Helper.getFromMap(queryHeuristicMap, heuristic, Averager.class).getCellValueWithStandardDeviation());
          }
        
          // Do the suboptions
          for (String subOption : Vote.getSubOptions(option)) {
            printer.beginRow();
            printer.addCell(subOption);
            queryHeuristicMap = Helper.getHashMapFromMap(queryMap, option + subOption);
            
            for (String heuristic : heuristics) {
              printer.addCell(Helper.getFromMap(queryHeuristicMap, heuristic, Averager.class).getCellValueWithStandardDeviation());
            }
          }
        }
        printer.addDividerRow();
        printer.endTable();
      }
    }
    
    if (!tupleMode) {
      // Print out the overall average results
      printer.beginTable(heuristics.size() + 1);
      printer.addHeader("Overall averaged results");
      printer.addDividerRow();
      printer.beginRow();
      printer.addCell("");
      for (String heuristic : heuristics) {
        printer.addCell(heuristic);
      }
      printer.addDividerRow();
      
      for (String option : Vote.getTextOptions()) {
        // Do the main option
        printer.beginRow();
        printer.addCell(option);
        Map<String, Averager<Integer>> globalHeuristicMap = Helper.getHashMapFromMap(globalMap, option);
        
        for (String heuristic : heuristics) {
          printer.addCell(Helper.getFromMap(globalHeuristicMap, heuristic, Averager.class).getCellValueWithStandardDeviation());
        }
        
        // Do the suboptions
        for (String subOption : Vote.getSubOptions(option)) {
          printer.beginRow();
          printer.addCell(subOption);
          globalHeuristicMap = Helper.getHashMapFromMap(globalMap, option + subOption);
          
          for (String heuristic : heuristics) {
            printer.addCell(Helper.getFromMap(globalHeuristicMap, heuristic, Averager.class).getCellValueWithStandardDeviation());
          }
        }
      }
      printer.addDividerRow();
      printer.endTable();
    }
    if (tupleMode) {
      printer.endTable();
    }
  }
}
