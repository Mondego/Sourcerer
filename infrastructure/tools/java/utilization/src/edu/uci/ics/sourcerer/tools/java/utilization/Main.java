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
package edu.uci.ics.sourcerer.tools.java.utilization;

import edu.uci.ics.sourcerer.tools.java.repo.model.JavaRepositoryFactory;
import edu.uci.ics.sourcerer.tools.java.utilization.identifier.ClusterIdentifier;
import edu.uci.ics.sourcerer.tools.java.utilization.identifier.ClusterMerger;
import edu.uci.ics.sourcerer.tools.java.utilization.identifier.ExemplarIdentifier;
import edu.uci.ics.sourcerer.tools.java.utilization.model.cluster.ClusterCollection;
import edu.uci.ics.sourcerer.tools.java.utilization.model.jar.Fingerprint;
import edu.uci.ics.sourcerer.tools.java.utilization.model.jar.JarCollection;
import edu.uci.ics.sourcerer.tools.java.utilization.popularity.ImportPopularityCalculator;
import edu.uci.ics.sourcerer.tools.java.utilization.stats.ClusterStats;
import edu.uci.ics.sourcerer.tools.java.utilization.stats.GeneralStats;
import edu.uci.ics.sourcerer.tools.java.utilization.stats.JarStats;
import edu.uci.ics.sourcerer.util.io.arguments.Command;
import edu.uci.ics.sourcerer.util.io.logging.TaskProgressLogger;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class Main {
//  public static final Command TEST_CLUSTER_NAMES = new Command("test-cluster-names", "Tests cluster names.") {
//    @Override
//    public void action() {
//      MatchClusterNames.matchClusterNames();
//    }
//  }.setProperties(
//      JavaRepositoryFactory.INPUT_REPO,
//      Identifier.CLUSTER_MERGING_LOG,
//      Identifier.EXEMPLAR_LOG,
//      Identifier.MERGE_METHOD, 
//      Fingerprint.FINGERPRINT_MODE, 
//      ClusterCollection.CLUSTER_COLLECTION_CACHE.asInput(), 
//      ClusterCollection.CLUSTER_COLLECTION_CACHE.asOutput(),
//      ClusterCollection.JAR_LOG,
//      ClusterCollection.CLUSTER_LOG,
//      MatchClusterNames.TEST_REPO);
  
  public static final Command IDENTIFY_LIBRARIES = new Command("identify-libraries", "Identified libraries.") {
    @Override
    public void action() {
      TaskProgressLogger task = TaskProgressLogger.get();
      task.start("Identifying libraries");
      
      JarCollection jars = JarCollection.create();
      
      ClusterCollection clusters = ClusterIdentifier.identifyClusters(jars);
      ClusterMerger.mergeClusters(clusters);
      ExemplarIdentifier.identifyExemplars(clusters);
      
      GeneralStats.calculate(jars, clusters);
      JarStats.calculate(jars, clusters);
      ClusterStats.calculate(jars, clusters);
      
      task.finish();
    }
  }.setProperties(
      JavaRepositoryFactory.INPUT_REPO,
      Fingerprint.FINGERPRINT_MODE,
      ClusterIdentifier.COMPATIBILITY_THRESHOLD,
      ClusterMerger.CLUSTER_MERGE_METHOD,
      ClusterMerger.CLUSTER_MERGING_LOG,
      ExemplarIdentifier.EXEMPLAR_THRESHOLD, 
      ExemplarIdentifier.EXEMPLAR_LOG,
      GeneralStats.GENERAL_STATS, 
      GeneralStats.POPULAR_FQNS,
      JarStats.JAR_LISTING,
      ClusterStats.CLUSTER_LISTING,
      GeneralStats.MAX_TABLE_COLUMNS);
  
//  public static final Command COMPARE_LIBRARY_IDENTIFICATION = new Command("compare-library-identification", "Compare methods for clustering and identifying the libraries.") {
//    @Override
//    protected void action() {
//      final TaskProgressLogger task = TaskProgressLogger.create();
//      task.start("Comparing library identification methods");
//      final JarCollection jars = JarCollection.make(task);
//      jars.printStatistics(task);
//      Action action = new Action() {
//        @Override
//        public void doMe() {
//          ClusterMergeMethod method = Identifier.MERGE_METHOD.getValue();
//          
//          Identifier.CLUSTER_MERGING_LOG.setValue(method.toString());
//          ClusterCollection clusters = Identifier.identifyClusters(task, jars);
//          
//          ClusterCollection.JAR_LOG.setValue("jars+" + method);
//          ClusterCollection.CLUSTER_LOG.setValue("clusters+" + method);
//          clusters.printStatistics(task);
//        }
//      };
//      for (ClusterMergeMethod method : ClusterMergeMethod.values()) {
//        method.doForEachVersion(action);
//      }
//      task.finish();
//    }
//  }.setProperties(
//      JavaRepositoryFactory.INPUT_REPO,
//      Identifier.CLUSTER_MERGING_LOG,
//      Identifier.EXEMPLAR_LOG,
//      Identifier.MERGE_METHOD, 
//      Fingerprint.FINGERPRINT_MODE, 
//      ClusterCollection.JAR_LOG,
//      ClusterCollection.CLUSTER_LOG);
  
//  public static final Command COMPARE_FILTERED_LIBRARY_IDENTIFICATION = new Command("compare-filtered-library-identification", "Compare methods for clustering and identifying the libraries.") {
//    @Override
//    protected void action() {
//      final TaskProgressLogger task = TaskProgressLogger.create();
//      task.start("Comparing library identification methods");
//      final JarCollection jars = JarCollection.make(task, JarIdentifier.loadIdentifiedJars());
//      jars.printStatistics(task);
//      Action action = new Action() {
//        @Override
//        public void doMe() {
//          ClusterMergeMethod method = Identifier.MERGE_METHOD.getValue();
//          
//          Identifier.CLUSTER_MERGING_LOG.setValue(method.toString());
//          ClusterCollection clusters = Identifier.identifyClusters(task, jars);
//          
//          ClusterCollection.JAR_LOG.setValue("jars+" + method);
//          ClusterCollection.CLUSTER_LOG.setValue("clusters+" + method);
//          clusters.printStatistics(task);
//        }
//      };
//      for (ClusterMergeMethod method : ClusterMergeMethod.values()) {
//        method.doForEachVersion(action);
//      }
//      task.finish();
//    }
//  }.setProperties(
//      JavaRepositoryFactory.INPUT_REPO,
//      Identifier.CLUSTER_MERGING_LOG,
//      Identifier.EXEMPLAR_LOG,
//      Identifier.MERGE_METHOD, 
//      Fingerprint.FINGERPRINT_MODE, 
//      ClusterCollection.JAR_LOG,
//      ClusterCollection.CLUSTER_LOG);
  
  public static final Command CALCULATE_IMPORT_POPULARITY = new Command("calculate-import-popularity", "Calculates the popularity of FQNs based on import statements.") {
    @Override
    protected void action() {
      ImportPopularityCalculator.calculateImportPopularity();
    }
  }.setProperties(JavaRepositoryFactory.INPUT_REPO);
  
  public static void main(String[] args) {
    Command.execute(args, Main.class);
  }
}
