package edu.uci.ics.sourcerer.clusterer.fqn;
///* 
// * Sourcerer: an infrastructure for large-scale source code analysis.
// * Copyright (C) by contributors. See CONTRIBUTORS.txt for full list.
// *
// * This program is free software: you can redistribute it and/or modify
// * it under the terms of the GNU General Public License as published by
// * the Free Software Foundation, either version 3 of the License, or
// * (at your option) any later version.
//
// * This program is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// * GNU General Public License for more details.
//
// * You should have received a copy of the GNU General Public License
// * along with this program. If not, see <http://www.gnu.org/licenses/>.
// */
//package edu.uci.ics.sourcerer.clusterer.stats;
//
//import static edu.uci.ics.sourcerer.util.io.Logging.logger;
//import edu.uci.ics.sourcerer.clusterer.db.ClustererDatabaseAccessor;
//import edu.uci.ics.sourcerer.model.db.SlightlyLessLimitedEntityDB;
//import edu.uci.ics.sourcerer.util.io.Property;
//import edu.uci.ics.sourcerer.util.io.TablePrettyPrinter;
//import edu.uci.ics.sourcerer.util.io.properties.BooleanProperty;
//import edu.uci.ics.sourcerer.util.io.properties.IntegerProperty;
//import edu.uci.ics.sourcerer.util.io.properties.StringProperty;
//
///**
// * @author Joel Ossher (jossher@uci.edu)
// */
//public class FqnStats {
//  public static Property<Boolean> CRAWLED_ONLY = new BooleanProperty("crawled-only", false, "Clusterer", "Only look at crawled projects.");
//  public static Property<Integer> TOP_FQN_COUNT = new IntegerProperty("top-fqn-count", 1000, "Clusterer", "Number of top fqns to gather.");
//  public static Property<String> TOP_FQNS_FILE = new StringProperty("top-fqns-file", "top-fqns.txt", "Clusterer", "Filename of top count.");
//  private FqnTree fqns;
//  
//  public FqnStats() {
//  }
//  
//  public void gatherFqnStats(ClustererDatabaseAccessor db) {
//    fqns = new FqnTree();
//    
//    logger.info("Reading entities from the database...");
//    int count = 0;
//    int reportCount = 1;
//    for (SlightlyLessLimitedEntityDB entity : (CRAWLED_ONLY.getValue() ? db.getCrawledEntityFqns() : db.getEntityFqns())) {
//      fqns.addFqn(entity.getFqn(), entity.getProjectID());
//      if (++count % reportCount == 0) {
//        if (reportCount < 100000) {
//          reportCount *= 10;
//        }
//        logger.info("  " + count + " entities processed");
//      }
//    }
//    
//    logger.info("Writing FQN tree to disk");
//    fqns.writeToDisk();
//    
//    logger.info("Done!");
//  }
//  
//  public void loadFqnStats() {
//    fqns = new FqnTree();
//    fqns.readFromDisk();
//  }
//  
//  public void printTopFqns() {
//    FqnFragment[] topFqns = fqns.getTopFqns(TOP_FQN_COUNT.getValue());
//    TablePrettyPrinter printer = TablePrettyPrinter.getTablePrettyPrinter(TOP_FQNS_FILE);
//    printer.beginTable(2);
//    printer.addHeader("Top Fqns By Number of Projects");
//    printer.addDividerRow();
//    printer.addRow("FQN", "Project Count");
//    printer.addDividerRow();
//    for (FqnFragment fqn : topFqns) {
//      printer.beginRow();
//      printer.addCell(fqn.getFqn());
//      printer.addCell(fqn.getProjectCount());
//    }
//    printer.addDividerRow();
//    printer.endTable();
//    printer.close();
//
//  }
//}
