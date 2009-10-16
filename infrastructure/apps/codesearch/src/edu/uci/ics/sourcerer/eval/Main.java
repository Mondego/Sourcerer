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

import static edu.uci.ics.sourcerer.eval.CalculatePrecisionRecall.PR_FILE;
import static edu.uci.ics.sourcerer.eval.CalculatePrecisionRecall.RELEVANT_LIST;
import static edu.uci.ics.sourcerer.eval.CalculateTopStats.TOP_STATS_FILE;
import static edu.uci.ics.sourcerer.util.io.Properties.INPUT;
import static edu.uci.ics.sourcerer.util.io.TablePrettyPrinter.CSV_MODE;
import edu.uci.ics.sourcerer.util.io.Logging;
import edu.uci.ics.sourcerer.util.io.Property;
import edu.uci.ics.sourcerer.util.io.PropertyManager;
import edu.uci.ics.sourcerer.util.io.properties.BooleanProperty;
import edu.uci.ics.sourcerer.util.io.properties.IntegerProperty;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class Main {
  public static final Property<Integer> TOP_K = new IntegerProperty("top-k", 10, "Evaluation Stats", "Number of top results to consider.");
  public static final Property<Boolean> TUPLE_MODE = new BooleanProperty("tuple-mode", false, "Evaluation Stats", "Tuple mode gives output suited for consumption by R.");
  
  public static final Property<Boolean> TOP_STATS = new BooleanProperty("top-stats", false, "Evaluation Stats", "Calculate counts for the top results for each heuristic.");
  public static final Property<Boolean> PR = new BooleanProperty("pr-stats", false, "Evaluation Stats", "Calculate precision/recall for the top results for each heuristic.");
  
  public static void main(String[] args) {
    PropertyManager.registerLoggingProperties();
    PropertyManager.initializeProperties(args);
    Logging.initializeLogger();
    
    boolean didSomething = false;
    
    if (TOP_STATS.getValue()) {
      PropertyManager.registerUsedProperties(TOP_STATS, TOP_K, TUPLE_MODE, CSV_MODE, INPUT, TOP_STATS_FILE);
      PropertyManager.verifyUsage();
      CalculateTopStats.calculate();
      didSomething = true;
    }
    if (PR.getValue()) {
      PropertyManager.registerUsedProperties(PR, TOP_K, TUPLE_MODE, CSV_MODE, INPUT, RELEVANT_LIST, PR_FILE);
      PropertyManager.verifyUsage();
      CalculatePrecisionRecall.calculate();
      didSomething = true;
    }
    if (!didSomething) {
      PropertyManager.registerUsedProperties(TOP_STATS, PR);
      PropertyManager.printUsage();
    }
  }
}
