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

import java.io.File;

import edu.uci.ics.sourcerer.tools.java.repo.model.JavaRepositoryFactory;
import edu.uci.ics.sourcerer.tools.java.utilization.identifier.ClusterIdentifier;
import edu.uci.ics.sourcerer.tools.java.utilization.identifier.ClusterMerger;
import edu.uci.ics.sourcerer.tools.java.utilization.model.cluster.ClusterCollection;
import edu.uci.ics.sourcerer.tools.java.utilization.model.jar.Fingerprint;
import edu.uci.ics.sourcerer.tools.java.utilization.model.jar.JarCollection;
import edu.uci.ics.sourcerer.tools.java.utilization.repo.Repository;
import edu.uci.ics.sourcerer.tools.java.utilization.repo.RepositoryBuilder;
import edu.uci.ics.sourcerer.tools.java.utilization.repo.db.DatabaseImporter;
import edu.uci.ics.sourcerer.tools.java.utilization.stats.ClusterStats;
import edu.uci.ics.sourcerer.tools.java.utilization.stats.CoverageCalculator;
import edu.uci.ics.sourcerer.tools.java.utilization.stats.PopularityCalculator;
import edu.uci.ics.sourcerer.util.MemoryStatsReporter;
import edu.uci.ics.sourcerer.util.io.FileUtils;
import edu.uci.ics.sourcerer.util.io.arguments.Argument;
import edu.uci.ics.sourcerer.util.io.arguments.Command;
import edu.uci.ics.sourcerer.util.io.arguments.FileArgument;
import edu.uci.ics.sourcerer.util.io.logging.TaskProgressLogger;
import edu.uci.ics.sourcerer.utils.db.DatabaseConnectionFactory;

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
  
//  public static final Command IDENTIFY_LIBRARIES = new Command("identify-libraries", "Identified libraries.") {
//    @Override
//    public void action() {
//      TaskProgressLogger task = TaskProgressLogger.get();
//      task.start("Identifying libraries");
//      
//      JarCollection jars = JarCollection.create();
//      
//      ClusterCollection clusters = ClusterIdentifier.identifyClusters(jars);
//      ClusterMerger.mergeClusters(clusters);
//      ExemplarIdentifier.identifyExemplars(clusters);
//      
//      GeneralStats.calculate(jars, clusters);
//      JarStats.calculate(jars, clusters);
//      ClusterStats.calculate(jars, clusters);
//      
//      DecomposeJars.decomposeJars(clusters);
//      
//      task.finish();
//    }
//  }.setProperties(
//      JavaRepositoryFactory.INPUT_REPO,
//      Fingerprint.FINGERPRINT_MODE,
//      ClusterIdentifier.COMPATIBILITY_THRESHOLD,
//      ClusterMerger.CLUSTER_MERGE_METHOD,
//      ClusterMerger.CLUSTER_MERGING_LOG,
//      ExemplarIdentifier.EXEMPLAR_THRESHOLD, 
//      ExemplarIdentifier.EXEMPLAR_LOG,
//      GeneralStats.GENERAL_STATS, 
//      GeneralStats.POPULAR_FQNS,
//      JarStats.JAR_LISTING,
//      ClusterStats.CLUSTER_LISTING,
//      GeneralStats.MAX_TABLE_COLUMNS,
//      DecomposeJars.TEST_REPO,
//      DecomposeJars.TEST_REPO_CACHE,
//      DecomposeJars.DECOMPOSED_JAR_LISTING);
  
  public static final Argument<File> JAR_FILTER_FILE = new FileArgument("jar-filter-file", null, "Jar filter file");
  public static final Command CLUSTER_JARS = new Command("cluster-jars", "Cluster jars.") {
    @Override
    public void action() {
      TaskProgressLogger task = TaskProgressLogger.get();
      task.start("Clustering jars");
      
      JarCollection jars = null;
      if (JAR_FILTER_FILE.getValue() != null) {
        jars = JarCollection.create(FileUtils.readFileToCollection(JAR_FILTER_FILE.getValue()));
      } else {
        jars = JarCollection.create();
      }

      ClusterCollection clusters = ClusterIdentifier.identifyFullyMatchingClusters(jars);
      ClusterMerger.mergeByVersions(clusters);
      
      Repository repo = RepositoryBuilder.buildRepository(jars, clusters);
      
      DatabaseImporter importer = DatabaseImporter.create(jars, clusters, repo);
      importer.run();
      
//      ClusterStats.calculate(jars, clusters);
      
      task.finish();
    }
  }.setProperties(
      JavaRepositoryFactory.INPUT_REPO,
      Fingerprint.FINGERPRINT_MODE,
      JAR_FILTER_FILE,
//      GeneralStats.MAX_TABLE_COLUMNS,
      ClusterStats.CLUSTER_LISTING,
      FileUtils.TEMP_DIR,
      DatabaseConnectionFactory.DATABASE_URL,
      DatabaseConnectionFactory.DATABASE_USER,
      DatabaseConnectionFactory.DATABASE_PASSWORD
      );
  
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
      PopularityCalculator.calculateImportPopularity();
    }
  }.setProperties(JavaRepositoryFactory.INPUT_REPO);
  
  public static final Command CALCULATE_JAR_COVERAGE = new Command("calculate-jar-coverage", "Calculates the coverage of missing types by the jar collection.") {
    @Override
    protected void action() {
      CoverageCalculator.calculateJarCoverage();
    }
  }.setProperties(JavaRepositoryFactory.INPUT_REPO, CoverageCalculator.JAR_REPO, CoverageCalculator.SOURCED_CACHE, CoverageCalculator.MISSING_FQNS_PER_PROJECT, CoverageCalculator.PROJECTS_PER_MISSING_FQN);  
  
  public static void main(String[] args) {
    Command.execute(args, Main.class);
  }
}
