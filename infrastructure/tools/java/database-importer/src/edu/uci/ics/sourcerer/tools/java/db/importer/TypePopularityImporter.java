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
//package edu.uci.ics.sourcerer.tools.java.db.importer;
//
//import edu.uci.ics.sourcerer.tools.java.component.identifier.stats.CountingFqnNode;
//import edu.uci.ics.sourcerer.tools.java.component.identifier.stats.PopularityCalculator;
//import edu.uci.ics.sourcerer.tools.java.db.schema.TypesTable;
//import edu.uci.ics.sourcerer.utils.db.DatabaseRunnable;
//import edu.uci.ics.sourcerer.utils.db.sql.Assignment;
//import edu.uci.ics.sourcerer.utils.db.sql.ConstantCondition;
//import edu.uci.ics.sourcerer.utils.db.sql.SetStatement;
//
///**
// * @author Joel Ossher (jossher@uci.edu)
// */
//public class TypePopularityImporter extends DatabaseRunnable {
//  private TypePopularityImporter() {}
//  
//  public static void importTypePopularity() {
//    new TypePopularityImporter().run();
//  }
//  
//  @Override
//  protected void action() {
//    CountingFqnNode root = PopularityCalculator.calculateImportPopularity();
//    try (SetStatement set = exec.createSetStatement(TypesTable.TABLE)) {
//      Assignment<Integer> count = set.addAssignment(TypesTable.IMPORT_COUNT);
//      ConstantCondition<String> fqnEquals = TypesTable.FQN.compareEquals();
//      set.andWhere(fqnEquals);
//      
//      for (CountingFqnNode fqn : root.getPostOrderIterable()) {
//        if (fqn.getProjectCount() > 0) {
//          count.setValue(fqn.getProjectCount());
//          fqnEquals.setValue(fqn.getFqn());
//          set.execute();
//        }
//      }
//    }
//  }
//}
