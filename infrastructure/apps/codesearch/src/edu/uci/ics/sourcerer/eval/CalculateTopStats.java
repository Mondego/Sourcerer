package edu.uci.ics.sourcerer.eval;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import edu.uci.ics.sourcerer.util.Averager;
import edu.uci.ics.sourcerer.util.Helper;
import edu.uci.ics.sourcerer.util.io.Property;
import edu.uci.ics.sourcerer.util.io.PropertyManager;
import edu.uci.ics.sourcerer.util.io.TablePrettyPrinter;

public class CalculateTopStats {
  public static void calculate() {
    PropertyManager properties = PropertyManager.getProperties();
    int top = properties.getValueAsInt(Property.TOP_K);
    
    EvaluationResults results = EvaluationResults.loadResults();
    
    // Print out the results
    TablePrettyPrinter printer = TablePrettyPrinter.getTablePrettyPrinter(properties, Property.STATS_FILE);
    printer.setCSVMode(properties.isSet(Property.CSV_MODE));
    printer.setFractionDigits(3);
    
    // row -> heuristic -> averager map
    Map<String, Map<String, Averager<Integer>>> globalMap = Helper.newHashMap();
    
    Collection<String> globalHeuristics = Helper.newHashSet();
    
    // Go through every query
    for (Query query : results.getQueries()) {
      Map<String, Map<String, Averager<Integer>>> queryMap = Helper.newHashMap();
      // Look up the heuristics for this query
      Collection<String> heuristics = Helper.newLinkedList();
      heuristics.addAll(query.getHeuristics());
      globalHeuristics.addAll(heuristics);
      
      // Go through every vote group for this query
      for (Votes votes : results.getVotes(query)) {
        printer.beginTable(heuristics.size() + 1);
        printer.addHeader("Query " + query.getName() + " for user " + votes.getUser());
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
        printer.endTable();
      }
      
      // Print out the average results for this query
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
    
    // Print out the overall average results
    printer.beginTable(globalHeuristics.size() + 1);
    printer.addHeader("Overall averaged results");
    printer.addDividerRow();
    printer.beginRow();
    printer.addCell("");
    for (String heuristic : globalHeuristics) {
      printer.addCell(heuristic);
    }
    printer.addDividerRow();
    
    for (String option : Vote.getTextOptions()) {
      // Do the main option
      printer.beginRow();
      printer.addCell(option);
      Map<String, Averager<Integer>> globalHeuristicMap = Helper.getHashMapFromMap(globalMap, option);
      
      for (String heuristic : globalHeuristics) {
        printer.addCell(Helper.getFromMap(globalHeuristicMap, heuristic, Averager.class).getCellValueWithStandardDeviation());
      }
      
      // Do the suboptions
      for (String subOption : Vote.getSubOptions(option)) {
        printer.beginRow();
        printer.addCell(subOption);
        globalHeuristicMap = Helper.getHashMapFromMap(globalMap, option + subOption);
        
        for (String heuristic : globalHeuristics) {
          printer.addCell(Helper.getFromMap(globalHeuristicMap, heuristic, Averager.class).getCellValueWithStandardDeviation());
        }
      }
    }
    printer.addDividerRow();
    printer.endTable();
  }
}
