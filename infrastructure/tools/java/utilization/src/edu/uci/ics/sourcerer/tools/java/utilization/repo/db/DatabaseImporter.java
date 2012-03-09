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
import edu.uci.ics.sourcerer.tools.java.utilization.model.jar.FqnVersion;
import edu.uci.ics.sourcerer.tools.java.utilization.model.jar.Jar;
import edu.uci.ics.sourcerer.tools.java.utilization.model.jar.JarCollection;
import edu.uci.ics.sourcerer.tools.java.utilization.model.jar.VersionedFqnNode;
import edu.uci.ics.sourcerer.tools.java.utilization.repo.Library;
import edu.uci.ics.sourcerer.tools.java.utilization.repo.LibraryVersion;
import edu.uci.ics.sourcerer.tools.java.utilization.repo.Repository;
import edu.uci.ics.sourcerer.tools.java.utilization.repo.db.schema.ClusterFqnType;
import edu.uci.ics.sourcerer.tools.java.utilization.repo.db.schema.ClusterToJarTable;
import edu.uci.ics.sourcerer.tools.java.utilization.repo.db.schema.ClustersTable;
import edu.uci.ics.sourcerer.tools.java.utilization.repo.db.schema.FqnVersionsTable;
import edu.uci.ics.sourcerer.tools.java.utilization.repo.db.schema.FqnsTable;
import edu.uci.ics.sourcerer.tools.java.utilization.repo.db.schema.JarsTable;
import edu.uci.ics.sourcerer.tools.java.utilization.repo.db.schema.LibrariesTable;
import edu.uci.ics.sourcerer.tools.java.utilization.repo.db.schema.LibraryDependencyTable;
import edu.uci.ics.sourcerer.tools.java.utilization.repo.db.schema.LibraryToClusterTable;
import edu.uci.ics.sourcerer.tools.java.utilization.repo.db.schema.VersionDependencyTable;
import edu.uci.ics.sourcerer.tools.java.utilization.repo.db.schema.VersionToFqnVersionTable;
import edu.uci.ics.sourcerer.tools.java.utilization.repo.db.schema.VersionToJarTable;
import edu.uci.ics.sourcerer.tools.java.utilization.repo.db.schema.VersionsTable;
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
        FqnsTable.TABLE,
        JarsTable.TABLE,
        ClusterToJarTable.TABLE,
        FqnVersionsTable.TABLE,
        LibrariesTable.TABLE,
        LibraryToClusterTable.TABLE,
        LibraryDependencyTable.TABLE,
        VersionsTable.TABLE,
        VersionToJarTable.TABLE,
        VersionToFqnVersionTable.TABLE,
        VersionDependencyTable.TABLE
        );
    task.finish();
    
    task.start("Creating new tables");
    exec.createTables(
        ClustersTable.TABLE,
        FqnsTable.TABLE,
        JarsTable.TABLE,
        ClusterToJarTable.TABLE,
        FqnVersionsTable.TABLE,
        LibrariesTable.TABLE,
        LibraryToClusterTable.TABLE,
        LibraryDependencyTable.TABLE,
        VersionsTable.TABLE,
        VersionToJarTable.TABLE,
        VersionToFqnVersionTable.TABLE,
        VersionDependencyTable.TABLE
        );
    task.finish();
    
    populateClustersTable();
    populateFqnsTable();
    populateJarsTable();
    populateClusterToJarTable();
    populateFqnVersionsTable();
    populateLibrariesTable();
    populateLibraryToClusterTable();
    populateLibraryDependencyTable();
    populateVersionsTable();
    populateVersionToJarTable();
    populateVersionToFqnVersionTable();
    populateVersionDependencyTable();
    
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
      
      Iterator<Cluster> iter = clusters.iterator();
      TypedQueryResult result = query.select();
      while (result.next()) {
        clusterMap.put(iter.next(), result.getResult(ClustersTable.CLUSTER_ID));
        task.progress();
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

      Iterator<Jar> iter = jars.iterator();
      TypedQueryResult result = query.select();
      while (result.next()) {
        jarMap.put(iter.next(), result.getResult(JarsTable.JAR_ID));
        task.progress();
      }
    }
    task.finish();
    
    task.finish();
  }
  
  private void populateClusterToJarTable() {
    TaskProgressLogger task = TaskProgressLogger.get();
    task.start("Populating cluster_to_jar table");
    
    BatchInserter inserter = exec.makeInFileInserter(tempDir, ClusterToJarTable.TABLE);
    
    task.start("Processing mappings", "mappings processed");
    for (Map.Entry<Cluster, Integer> entry : clusterMap.entrySet()) {
      for (Jar jar : entry.getKey().getJars()) {
        inserter.addInsert(ClusterToJarTable.createInsert(entry.getValue(), jarMap.get(jar)));
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
      
      Iterator<VersionedFqnNode> iter = jars.getRoot().getPostOrderIterable().iterator();
      Iterator<FqnVersion> iter2 = null;
      TypedQueryResult result = query.select();
      while (result.next()) {
        while (iter2 == null || !iter2.hasNext()) {
          iter2 = iter.next().getVersions().iterator();
        }
        fqnVersionMap.put(iter2.next(), result.getResult(FqnVersionsTable.FQN_VERSION_ID));
        task.progress();
      }
    }
    task.finish();
    
    task.finish();
  }
  
  private void populateLibrariesTable() {
    TaskProgressLogger task = TaskProgressLogger.get();
    task.start("Populating libraries table");
    
    BatchInserter inserter = exec.makeInFileInserter(tempDir, LibrariesTable.TABLE);
    
    task.start("Processing libraries", "libraries processed");
    for (Library library : repo.getLibraries()) {
      inserter.addInsert(LibrariesTable.createInsert(library));
    }
    task.finish();
    
    task.start("Performing db insert");
    inserter.insert();
    task.finish();
   
    task.start("Loading library mapping", "libraries loaded");
    libraryMap = new HashMap<>();
    try (SelectQuery query = exec.makeSelectQuery(LibrariesTable.TABLE)) {
      query.addSelects(LibrariesTable.LIBRARY_ID);
      
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
      for (Cluster cluster : entry.getKey().getClusters()) {
        inserter.addInsert(LibraryToClusterTable.createInsert(entry.getValue(), clusterMap.get(cluster)));
      }
    }
    task.finish();
    
    task.start("Performing db insert");
    inserter.insert();
    task.finish();
    
    task.finish();
  }
  
  private void populateLibraryDependencyTable() {
    TaskProgressLogger task = TaskProgressLogger.get();
    task.start("Populating library_dep table");
    
    BatchInserter inserter = exec.makeInFileInserter(tempDir, LibraryDependencyTable.TABLE);
    
    task.start("Processing mappings", "mappings processed");
    for (Map.Entry<Library, Integer> entry : libraryMap.entrySet()) {
      for (Library dep : entry.getKey().getDependencies()) {
        inserter.addInsert(LibraryDependencyTable.createInsert(entry.getValue(), libraryMap.get(dep)));
      }
    }
    task.finish();
    
    task.start("Performing db insert");
    inserter.insert();
    task.finish();
    
    task.finish();
  }
  
  private void populateVersionsTable() {
    TaskProgressLogger task = TaskProgressLogger.get();
    task.start("Populating versions table");
    
    BatchInserter inserter = exec.makeInFileInserter(tempDir, VersionsTable.TABLE);
    
    task.start("Processing versions", "versions processed");
    for (Map.Entry<Library, Integer> entry : libraryMap.entrySet()) {
      for (LibraryVersion version : entry.getKey().getVersions()) {
        inserter.addInsert(VersionsTable.createInsert(version, entry.getValue()));
      }
    }
    task.finish();
    
    task.start("Performing db insert");
    inserter.insert();
    task.finish();
   
    task.start("Loading version mapping", "versions loaded");
    versionMap = new HashMap<>();
    try (SelectQuery query = exec.makeSelectQuery(VersionsTable.TABLE)) {
      query.addSelects(VersionsTable.VERSION_ID);
      
      TypedQueryResult result = query.select();
      for (Map.Entry<Library, Integer> entry : libraryMap.entrySet()) {
        for (LibraryVersion version : entry.getKey().getVersions()) {
          result.next();
          versionMap.put(version, result.getResult(VersionsTable.VERSION_ID));
          task.progress();
        }
      }
    }
    task.finish();
    
    task.finish();
  }
  
  private void populateVersionToJarTable() {
    TaskProgressLogger task = TaskProgressLogger.get();
    task.start("Populating version_to_jar table");
    
    BatchInserter inserter = exec.makeInFileInserter(tempDir, VersionToJarTable.TABLE);
    
    task.start("Processing mappings", "mappings processed");
    for (Map.Entry<LibraryVersion, Integer> entry : versionMap.entrySet()) {
      for (Jar jar : entry.getKey().getJars()) {
        inserter.addInsert(VersionToJarTable.createInsert(entry.getValue(), jarMap.get(jar)));
      }
    }
    task.finish();
    
    task.start("Performing db insert");
    inserter.insert();
    task.finish();
    
    task.finish();
  }
  
  private void populateVersionToFqnVersionTable() {
    TaskProgressLogger task = TaskProgressLogger.get();
    task.start("Populating version_to_fqn_version table");
    
    BatchInserter inserter = exec.makeInFileInserter(tempDir, VersionToFqnVersionTable.TABLE);
    
    task.start("Processing mappings", "mappings processed");
    for (Map.Entry<LibraryVersion, Integer> entry : versionMap.entrySet()) {
      for (FqnVersion fqn : entry.getKey().getFqnVersions()) {
        inserter.addInsert(VersionToFqnVersionTable.createInsert(entry.getValue(), fqnVersionMap.get(fqn)));
      }
    }
    task.finish();
    
    task.start("Performing db insert");
    inserter.insert();
    task.finish();
    
    task.finish();
  }
  
  private void populateVersionDependencyTable() {
    TaskProgressLogger task = TaskProgressLogger.get();
    task.start("Populating version_dep table");
    
    BatchInserter inserter = exec.makeInFileInserter(tempDir, VersionDependencyTable.TABLE);
    
    task.start("Processing mappings", "mappings processed");
    for (Map.Entry<LibraryVersion, Integer> entry : versionMap.entrySet()) {
      for (LibraryVersion dep : entry.getKey().getDependencies()) {
        inserter.addInsert(VersionToFqnVersionTable.createInsert(entry.getValue(), versionMap.get(dep)));
      }
    }
    task.finish();
    
    task.start("Performing db insert");
    inserter.insert();
    task.finish();
    
    task.finish();
  }
}
