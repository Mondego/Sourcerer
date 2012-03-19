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
package edu.uci.ics.sourcerer.tools.java.utilization.repo.db;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import edu.uci.ics.sourcerer.tools.java.utilization.model.cluster.Cluster;
import edu.uci.ics.sourcerer.tools.java.utilization.model.cluster.ClusterCollection;
import edu.uci.ics.sourcerer.tools.java.utilization.model.cluster.ClusterVersion;
import edu.uci.ics.sourcerer.tools.java.utilization.model.jar.FqnVersion;
import edu.uci.ics.sourcerer.tools.java.utilization.model.jar.Jar;
import edu.uci.ics.sourcerer.tools.java.utilization.model.jar.JarCollection;
import edu.uci.ics.sourcerer.tools.java.utilization.model.jar.VersionedFqnNode;
import edu.uci.ics.sourcerer.tools.java.utilization.repo.Library;
import edu.uci.ics.sourcerer.tools.java.utilization.repo.LibraryVersion;
import edu.uci.ics.sourcerer.tools.java.utilization.repo.Repository;
import edu.uci.ics.sourcerer.tools.java.utilization.repo.db.schema.ClusterFqnType;
import edu.uci.ics.sourcerer.tools.java.utilization.repo.db.schema.ClusterVersionToFqnVersionTable;
import edu.uci.ics.sourcerer.tools.java.utilization.repo.db.schema.ClusterVersionToJarTable;
import edu.uci.ics.sourcerer.tools.java.utilization.repo.db.schema.ClusterVersionsTable;
import edu.uci.ics.sourcerer.tools.java.utilization.repo.db.schema.ClustersTable;
import edu.uci.ics.sourcerer.tools.java.utilization.repo.db.schema.FqnVersionsTable;
import edu.uci.ics.sourcerer.tools.java.utilization.repo.db.schema.FqnsTable;
import edu.uci.ics.sourcerer.tools.java.utilization.repo.db.schema.JarToFqnVerionTable;
import edu.uci.ics.sourcerer.tools.java.utilization.repo.db.schema.JarsTable;
import edu.uci.ics.sourcerer.tools.java.utilization.repo.db.schema.LibrariesTable;
import edu.uci.ics.sourcerer.tools.java.utilization.repo.db.schema.LibraryToClusterTable;
import edu.uci.ics.sourcerer.tools.java.utilization.repo.db.schema.LibraryVersionToFqnVersionTable;
import edu.uci.ics.sourcerer.tools.java.utilization.repo.db.schema.LibraryVersionToJarTable;
import edu.uci.ics.sourcerer.tools.java.utilization.repo.db.schema.LibraryVersionToLibraryTable;
import edu.uci.ics.sourcerer.tools.java.utilization.repo.db.schema.LibraryVersionToLibraryVersionTable;
import edu.uci.ics.sourcerer.tools.java.utilization.repo.db.schema.LibraryVersionsTable;
import edu.uci.ics.sourcerer.util.io.FileUtils;
import edu.uci.ics.sourcerer.util.io.logging.TaskProgressLogger;
import edu.uci.ics.sourcerer.utils.db.BatchInserter;
import edu.uci.ics.sourcerer.utils.db.DatabaseRunnable;
import edu.uci.ics.sourcerer.utils.db.sql.SelectQuery;
import edu.uci.ics.sourcerer.utils.db.sql.TypedQueryResult;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class DatabaseImporter extends DatabaseRunnable {
  private final JarCollection jars;
  private final ClusterCollection clusters;
  private final Repository repo;
  
  private final File tempDir;
  
  private Map<Cluster, Integer> clusterMap;
  private Map<ClusterVersion, Integer> clusterVersionMap;
  private Map<VersionedFqnNode, Integer> fqnMap;
  private Map<Jar, Integer> jarMap;
  private Map<FqnVersion, Integer> fqnVersionMap;
  private Map<Library, Integer> libraryMap;
  private Map<LibraryVersion, Integer> versionMap;
  
  private DatabaseImporter(JarCollection jars, ClusterCollection clusters, Repository repo) {
    this.jars = jars;
    this.clusters = clusters;
    this.repo = repo;
    tempDir = FileUtils.getTempDir();
  }
  
  public static DatabaseImporter create(JarCollection jars, ClusterCollection clusters, Repository repo) {
    return new DatabaseImporter(jars, clusters, repo);
  }
  
  @Override
  protected void action() {
    TaskProgressLogger task = TaskProgressLogger.get();
    task.start("Initializing database");
    
    task.start("Dropping old tables");
    exec.dropTables(
        ClustersTable.TABLE,
        ClusterVersionsTable.TABLE,
        FqnsTable.TABLE,
        JarsTable.TABLE,
        ClusterVersionToJarTable.TABLE,
        FqnVersionsTable.TABLE,
        JarToFqnVerionTable.TABLE,
        ClusterVersionToFqnVersionTable.TABLE,
        LibrariesTable.TABLE,
        LibraryToClusterTable.TABLE,
        LibraryVersionsTable.TABLE,
        LibraryVersionToJarTable.TABLE,
        LibraryVersionToFqnVersionTable.TABLE,
        LibraryVersionToLibraryTable.TABLE,
        LibraryVersionToLibraryVersionTable.TABLE
        );
    task.finish();
    
    task.start("Creating new tables");
    exec.createTables(
        ClustersTable.TABLE,
        ClusterVersionsTable.TABLE,
        FqnsTable.TABLE,
        JarsTable.TABLE,
        ClusterVersionToJarTable.TABLE,
        FqnVersionsTable.TABLE,
        JarToFqnVerionTable.TABLE,
        ClusterVersionToFqnVersionTable.TABLE,
        LibrariesTable.TABLE,
        LibraryToClusterTable.TABLE,
        LibraryVersionsTable.TABLE,
        LibraryVersionToJarTable.TABLE,
        LibraryVersionToFqnVersionTable.TABLE,
        LibraryVersionToLibraryTable.TABLE,
        LibraryVersionToLibraryVersionTable.TABLE
        );
    task.finish();
    
    populateClustersTable();
    populateClusterVersionsTable();
    populateFqnsTable();
    populateJarsTable();
    populateClusterVersionToJarTable();
    populateFqnVersionsTable();
    populateJarToFqnVersionTable();
    populateClusterVersionToFqnVersionTable();
    populateLibrariesTable();
    populateLibraryToClusterTable();
    populateLibraryVersionsTable();
    populateLibraryVersionToJarTable();
    populateLibraryVersionToFqnVersionTable();
    populateLibraryVersionToLibraryTable();
    populateLibraryVersionToLibraryVersionTable();
    
    task.finish();
  }

  private void populateClustersTable() {
    TaskProgressLogger task = TaskProgressLogger.get();
    task.start("Populating clusters table");
    
    BatchInserter inserter = exec.makeInFileInserter(tempDir, ClustersTable.TABLE);
    
    task.start("Processing clusters", "clusters processed");
    for (Cluster cluster : clusters) {
      inserter.addInsert(ClustersTable.createInsert(cluster));
      task.progress();
    }
    task.finish();
    
    task.start("Performing db insert");
    inserter.insert();
    task.finish();
   
    task.start("Loading cluster mapping", "clusters loaded");
    clusterMap = new HashMap<>();
    try (SelectQuery query = exec.makeSelectQuery(ClustersTable.TABLE)) {
      query.addSelects(ClustersTable.CLUSTER_ID);
      query.orderBy(ClustersTable.CLUSTER_ID, true);
      
      TypedQueryResult result = query.select();
      for (Cluster cluster : clusters) {
        result.next();
        clusterMap.put(cluster, result.getResult(ClustersTable.CLUSTER_ID));
        task.progress();
      }
    }
    task.finish();
    
    task.finish();
  }
  
  private void populateClusterVersionsTable() {
    TaskProgressLogger task = TaskProgressLogger.get();
    task.start("Populating cluster_versions table");
    
    BatchInserter inserter = exec.makeInFileInserter(tempDir, ClusterVersionsTable.TABLE);
    
    task.start("Processing cluster versions", "cluster version processed");
    for (Cluster cluster : clusters) {
      Integer clusterID = clusterMap.get(cluster);
      for (ClusterVersion version : cluster.getVersions()) {
        inserter.addInsert(ClusterVersionsTable.createInsert(version, clusterID));
        task.progress();
      }
    }
    task.finish();
    
    task.start("Performing db insert");
    inserter.insert();
    task.finish();
   
    task.start("Loading cluster version mapping", "cluster verions loaded");
    clusterVersionMap = new HashMap<>();
    try (SelectQuery query = exec.makeSelectQuery(ClusterVersionsTable.TABLE)) {
      query.addSelects(ClusterVersionsTable.CLUSTER_VERSION_ID);
      query.orderBy(ClusterVersionsTable.CLUSTER_VERSION_ID, true);
      
      TypedQueryResult result = query.select();
      for (Cluster cluster : clusters) {
        for (ClusterVersion version : cluster.getVersions()) {
          result.next();
          clusterVersionMap.put(version, result.getResult(ClusterVersionsTable.CLUSTER_VERSION_ID));
          task.progress();
        }
      }
    }
    task.finish();
    
    task.finish();
  }
  
  private void populateFqnsTable() {
    TaskProgressLogger task = TaskProgressLogger.get();
    task.start("Populating fqns table");
    
    BatchInserter inserter = exec.makeInFileInserter(tempDir, FqnsTable.TABLE);
    
    task.start("Processing fqns", "fqns processed");
    for (Map.Entry<Cluster, Integer> entry : clusterMap.entrySet()) {
      for (VersionedFqnNode fqn : entry.getKey().getCoreFqns()) {
        inserter.addInsert(FqnsTable.createInsert(fqn, entry.getValue(), ClusterFqnType.CORE));
        task.progress();
      }
      for (VersionedFqnNode fqn : entry.getKey().getVersionFqns()) {
        inserter.addInsert(FqnsTable.createInsert(fqn, entry.getValue(), ClusterFqnType.VERSION));
        task.progress();
      }
    }
    task.finish();
    
    task.start("Performing db insert");
    inserter.insert();
    task.finish();
   
    task.start("Loading fqn mapping", "fqns loaded");
    fqnMap = new HashMap<>();
    try (SelectQuery query = exec.makeSelectQuery(FqnsTable.TABLE)) {
      query.addSelects(FqnsTable.FQN_ID);
      query.orderBy(FqnsTable.FQN_ID, true);
      
      TypedQueryResult result = query.select();
      for (Map.Entry<Cluster, Integer> entry : clusterMap.entrySet()) {
        for (VersionedFqnNode fqn : entry.getKey().getCoreFqns()) {
          result.next();
          fqnMap.put(fqn, result.getResult(FqnsTable.FQN_ID));
          task.progress();
        }
        for (VersionedFqnNode fqn : entry.getKey().getVersionFqns()) {
          result.next();
          fqnMap.put(fqn, result.getResult(FqnsTable.FQN_ID));
          task.progress();
        }
      }
    }
    task.finish();
    
    task.finish();
  }
  
  private void populateJarsTable() {
    TaskProgressLogger task = TaskProgressLogger.get();
    
    task.start("Populating jars table");
    
    BatchInserter inserter = exec.makeInFileInserter(tempDir, JarsTable.TABLE);
    
    task.start("Processing jars", "jars processed");
    for (Jar jar : jars) {
      inserter.addInsert(JarsTable.createInsert(jar));
      task.progress();
    }
    task.finish();
    
    task.start("Performing db insert");
    inserter.insert();
    task.finish();
    
    task.start("Loading jar mapping", "jars loaded");
    jarMap = new HashMap<>();
    try (SelectQuery query = exec.makeSelectQuery(JarsTable.TABLE)) {
      query.addSelects(JarsTable.JAR_ID);
      query.orderBy(JarsTable.JAR_ID, true);

      TypedQueryResult result = query.select();
      for (Jar jar : jars) {
        result.next();
        jarMap.put(jar, result.getResult(JarsTable.JAR_ID));
        task.progress();
      }
    }
    task.finish();
    
    task.finish();
  }
  
  private void populateClusterVersionToJarTable() {
    TaskProgressLogger task = TaskProgressLogger.get();
    task.start("Populating cluster_version_to_jar table");
    
    BatchInserter inserter = exec.makeInFileInserter(tempDir, ClusterVersionToJarTable.TABLE);
    
    task.start("Processing mappings", "mappings processed");
    for (Map.Entry<ClusterVersion, Integer> entry : clusterVersionMap.entrySet()) {
      for (Jar jar : entry.getKey().getJars()) {
        inserter.addInsert(ClusterVersionToJarTable.createInsert(entry.getValue(), jarMap.get(jar)));
        task.progress();
      }
    }
    task.finish();
    
    task.start("Performing db insert");
    inserter.insert();
    task.finish();
    
    task.finish();
  }
  
  private void populateFqnVersionsTable() {
    TaskProgressLogger task = TaskProgressLogger.get();
    task.start("Populating fqn_versions table");
    
    BatchInserter inserter = exec.makeInFileInserter(tempDir, FqnVersionsTable.TABLE);
    
    task.start("Processing fqns", "fqns processed");
    for (Map.Entry<VersionedFqnNode, Integer> entry : fqnMap.entrySet()) {
      for (FqnVersion version : entry.getKey().getVersions()) {
        inserter.addInsert(FqnVersionsTable.createInsert(entry.getValue(), version.getFingerprint().serialize()));
        task.progress();
      }
    }
    task.finish();
    
    task.start("Performing db insert");
    inserter.insert();
    task.finish();
   
    task.start("Loading fqn version mapping", "fqn versions loaded");
    fqnVersionMap = new HashMap<>();
    try (SelectQuery query = exec.makeSelectQuery(FqnVersionsTable.TABLE)) {
      query.addSelects(FqnVersionsTable.FQN_VERSION_ID);
      query.orderBy(FqnVersionsTable.FQN_VERSION_ID, true);
      
      TypedQueryResult result = query.select();
      for (Map.Entry<VersionedFqnNode, Integer> entry : fqnMap.entrySet()) {
        for (FqnVersion version : entry.getKey().getVersions()) {
          result.next();
          fqnVersionMap.put(version, result.getResult(FqnVersionsTable.FQN_VERSION_ID));
          task.progress();
        }
      }
    }
    task.finish();
    
    task.finish();
  }
  
  private void populateJarToFqnVersionTable() {
    TaskProgressLogger task = TaskProgressLogger.get();
    task.start("Populating jar_to_fqn_version table");
    
    BatchInserter inserter = exec.makeInFileInserter(tempDir, JarToFqnVerionTable.TABLE);
    
    task.start("Processing mappings", "mapping processed");
    for (Map.Entry<Jar, Integer> entry : jarMap.entrySet()) {
      for (FqnVersion fqn : entry.getKey().getFqns()) {
        inserter.addInsert(JarToFqnVerionTable.createInsert(entry.getValue(), fqnVersionMap.get(fqn)));
        task.progress();
      }
    }
    task.finish();
    
    task.start("Performing db insert");
    inserter.insert();
    task.finish();
    
    task.finish();
  }
  
  private void populateClusterVersionToFqnVersionTable() {
    TaskProgressLogger task = TaskProgressLogger.get();
    task.start("Populating cluster_version_to_fqn_version table");
    
    BatchInserter inserter = exec.makeInFileInserter(tempDir, ClusterVersionToFqnVersionTable.TABLE);
    
    task.start("Processing mappings", "mappings processed");
    for (Map.Entry<ClusterVersion, Integer> entry : clusterVersionMap.entrySet()) {
      for (FqnVersion fqn : entry.getKey().getFqns()) {
        inserter.addInsert(ClusterVersionToFqnVersionTable.createInsert(entry.getValue(), fqnVersionMap.get(fqn)));
        task.progress();
      }
    }
    task.finish();
    
    task.start("Performing db insert");
    inserter.insert();
    task.finish();
    
    task.finish();
  }
  
  private void populateLibrariesTable() {
    TaskProgressLogger task = TaskProgressLogger.get();
    task.start("Populating libraries table");
    
    BatchInserter inserter = exec.makeInFileInserter(tempDir, LibrariesTable.TABLE);
    
    task.start("Processing libraries", "libraries processed");
    for (Library library : repo.getLibraries()) {
      inserter.addInsert(LibrariesTable.createInsert(library, clusterMap.get(library.getCoreCluster())));
      task.progress();
    }
    task.finish();
    
    task.start("Performing db insert");
    inserter.insert();
    task.finish();
   
    task.start("Loading library mapping", "libraries loaded");
    libraryMap = new HashMap<>();
    try (SelectQuery query = exec.makeSelectQuery(LibrariesTable.TABLE)) {
      query.addSelects(LibrariesTable.LIBRARY_ID);
      query.orderBy(LibrariesTable.LIBRARY_ID, true);
      
      Iterator<Library> iter = repo.getLibraries().iterator();
      TypedQueryResult result = query.select();
      while (result.next()) {
        libraryMap.put(iter.next(), result.getResult(LibrariesTable.LIBRARY_ID));
        task.progress();
      }
    }
    task.finish();
    
    task.finish();
  }
  
  private void populateLibraryToClusterTable() {
    TaskProgressLogger task = TaskProgressLogger.get();
    task.start("Populating library_to_cluster table");
    
    BatchInserter inserter = exec.makeInFileInserter(tempDir, LibraryToClusterTable.TABLE);
    
    task.start("Processing mappings", "mappings processed");
    for (Map.Entry<Library, Integer> entry : libraryMap.entrySet()) {
      for (Cluster cluster : entry.getKey().getSecondaryClusters()) {
        inserter.addInsert(LibraryToClusterTable.createInsert(entry.getValue(), clusterMap.get(cluster)));
        task.progress();
      }
    }
    task.finish();
    
    task.start("Performing db insert");
    inserter.insert();
    task.finish();
    
    task.finish();
  }
  
  private void populateLibraryVersionsTable() {
    TaskProgressLogger task = TaskProgressLogger.get();
    task.start("Populating versions table");
    
    BatchInserter inserter = exec.makeInFileInserter(tempDir, LibraryVersionsTable.TABLE);
    
    task.start("Processing versions", "versions processed");
    for (Map.Entry<Library, Integer> entry : libraryMap.entrySet()) {
      for (LibraryVersion version : entry.getKey().getVersions()) {
        inserter.addInsert(LibraryVersionsTable.createInsert(version, entry.getValue()));
        task.progress();
      }
    }
    task.finish();
    
    task.start("Performing db insert");
    inserter.insert();
    task.finish();
   
    task.start("Loading version mapping", "versions loaded");
    versionMap = new HashMap<>();
    try (SelectQuery query = exec.makeSelectQuery(LibraryVersionsTable.TABLE)) {
      query.addSelects(LibraryVersionsTable.LIBRARY_VERSION_ID);
      query.orderBy(LibraryVersionsTable.LIBRARY_VERSION_ID, true);
      
      TypedQueryResult result = query.select();
      for (Map.Entry<Library, Integer> entry : libraryMap.entrySet()) {
        for (LibraryVersion version : entry.getKey().getVersions()) {
          result.next();
          versionMap.put(version, result.getResult(LibraryVersionsTable.LIBRARY_VERSION_ID));
          task.progress();
        }
      }
    }
    task.finish();
    
    task.finish();
  }
  
  private void populateLibraryVersionToJarTable() {
    TaskProgressLogger task = TaskProgressLogger.get();
    task.start("Populating version_to_jar table");
    
    BatchInserter inserter = exec.makeInFileInserter(tempDir, LibraryVersionToJarTable.TABLE);
    
    task.start("Processing mappings", "mappings processed");
    for (Map.Entry<LibraryVersion, Integer> entry : versionMap.entrySet()) {
      for (Jar jar : entry.getKey().getJars()) {
        inserter.addInsert(LibraryVersionToJarTable.createInsert(entry.getValue(), jarMap.get(jar)));
        task.progress();
      }
    }
    task.finish();
    
    task.start("Performing db insert");
    inserter.insert();
    task.finish();
    
    task.finish();
  }
  
  private void populateLibraryVersionToFqnVersionTable() {
    TaskProgressLogger task = TaskProgressLogger.get();
    task.start("Populating version_to_fqn_version table");
    
    BatchInserter inserter = exec.makeInFileInserter(tempDir, LibraryVersionToFqnVersionTable.TABLE);
    
    task.start("Processing mappings", "mappings processed");
    for (Map.Entry<LibraryVersion, Integer> entry : versionMap.entrySet()) {
      for (FqnVersion fqn : entry.getKey().getFqnVersions()) {
        inserter.addInsert(LibraryVersionToFqnVersionTable.createInsert(entry.getValue(), fqnVersionMap.get(fqn)));
        task.progress();
      }
    }
    task.finish();
    
    task.start("Performing db insert");
    inserter.insert();
    task.finish();
    
    task.finish();
  }
  
  private void populateLibraryVersionToLibraryTable() {
    TaskProgressLogger task = TaskProgressLogger.get();
    task.start("Populating library_version_to_library table");
    
    BatchInserter inserter = exec.makeInFileInserter(tempDir, LibraryVersionToLibraryTable.TABLE);
    
    task.start("Processing mappings", "mappings processed");
    for (Map.Entry<LibraryVersion, Integer> entry : versionMap.entrySet()) {
      for (Library dep : entry.getKey().getLibraryDependencies()) {
        inserter.addInsert(LibraryVersionToLibraryTable.createInsert(entry.getValue(), libraryMap.get(dep)));
        task.progress();
      }
    }
    task.finish();
    
    task.start("Performing db insert");
    inserter.insert();
    task.finish();
    
    task.finish();
  }
  
  private void populateLibraryVersionToLibraryVersionTable() {
    TaskProgressLogger task = TaskProgressLogger.get();
    task.start("Populating library_version_to_library_version table");
    
    BatchInserter inserter = exec.makeInFileInserter(tempDir, LibraryVersionToLibraryVersionTable.TABLE);
    
    task.start("Processing mappings", "mappings processed");
    for (Map.Entry<LibraryVersion, Integer> entry : versionMap.entrySet()) {
      for (LibraryVersion dep : entry.getKey().getVersionDependencies()) {
        inserter.addInsert(LibraryVersionToLibraryVersionTable.createInsert(entry.getValue(), versionMap.get(dep)));
        task.progress();
      }
    }
    task.finish();
    
    task.start("Performing db insert");
    inserter.insert();
    task.finish();
    
    task.finish();
  }
}
