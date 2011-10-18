/* 
 * Sourcerer: an infrastructure for large-scale source code analysis.
 * Copyright (C) by contributors. See CONTRIBUTORS.txt for full list.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package edu.uci.ics.sourcerer.tools.java.utilization.fqn;


import java.util.Collection;

import edu.uci.ics.sourcerer.util.Pair;
import edu.uci.ics.sourcerer.util.io.TablePrettyPrinter;
import edu.uci.ics.sourcerer.util.io.TaskProgressLogger;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class FqnUsageStatistics {
  public static <Source> void printFqnUsageStatistics(TaskProgressLogger task, FqnUsageTree<Source> tree) {
    Pair<Integer, Integer> counts = tree.getFqnCounts();
    task.report("Total FQNs: " + counts.getFirst());
    task.report("Total unique FQNs: " + counts.getSecond());
    try (TablePrettyPrinter printer = TablePrettyPrinter.getLoggerPrettyPrinter()) {
      {
        task.start("Computing top FQN fragments");
        Collection<FqnUsageTreeNode<Source>> topFragments = tree.getTopFragments(100);
        task.finish();
        
        printer.addHeader("Top 100 FQN Fragments");
        printer.beginTable(2);
        printer.addDividerRow();
        printer.addRow("Fragment", "Sources");
        printer.addDividerRow();
        for (FqnUsageTreeNode<Source> node : topFragments) {
          printer.beginRow();
          printer.addCell(node.getFQN());
          printer.addCell(node.getSecondarySources().size());
        }
        printer.addDividerRow();
        printer.endTable();
      }
      
      {
        task.start("Computing top FQNs");
        Collection<FqnUsageTreeNode<Source>> topFqns = tree.getTopFqns(100);
        task.finish();
        
        printer.addHeader("Top 100 FQNs");
        printer.beginTable(2);
        printer.addDividerRow();
        printer.addRow("FQN", "Sources");
        printer.addDividerRow();
        for (FqnUsageTreeNode<Source> node : topFqns) {
          printer.beginRow();
          printer.addCell(node.getFQN());
          printer.addCell(node.getSources().size());
        }
        printer.addDividerRow();
        printer.endTable();
      }
    }
  }
}
