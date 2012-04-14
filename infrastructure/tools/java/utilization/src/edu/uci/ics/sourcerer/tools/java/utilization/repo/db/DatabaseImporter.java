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

import edu.uci.ics.sourcerer.tools.java.db.schema.ClusterVersionsTable;
import edu.uci.ics.sourcerer.tools.java.db.schema.FqnVersionsTable;
import edu.uci.ics.sourcerer.tools.java.db.schema.LibraryVersionToLibraryTable;
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
import edu.uci.ics.sourcerer.tools.java.utilization.repo.db.schema.ClustersTable;
import edu.uci.ics.sourcerer.tools.java.utilization.repo.db.schema.FqnsTable;
import edu.uci.ics.sourcerer.tools.java.utilization.repo.db.schema.JarToFqnVerionTable;
import edu.uci.ics.sourcerer.tools.java.utilization.repo.db.schema.JarsTable;
import edu.uci.ics.sourcerer.tools.java.utilization.repo.db.schema.LibrariesTable;
import edu.uci.ics.sourcerer.tools.java.utilization.repo.db.schema.LibraryToClusterTable;
import edu.uci.ics.sourcerer.tools.java.utilization.repo.db.schema.LibraryVersionToClusterVersionTable;
import edu.uci.ics.sourcerer.tools.java.utilization.repo.db.schema.LibraryVersionToFqnVersionTable;
import edu.uci.ics.sourcerer.tools.java.utilization.repo.db.schema.LibraryVersionToJarTable;
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
    populateLibraryVersionToClusterVersionTable();
    populateLibraryVersionToJarTable();
    populateLibraryVersionToFqnVersionTable();
    populateLibraryVersionToLibraryTable();
    populateLibraryVersionToLibraryVersionTable();
    
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
  
  private void populateLibraryVersionToClusterVersionTable() {
    TaskProgressLogger task = TaskProgressLogger.get();
    task.start("Populating library_version_to_cluster table");
    
    BatchInserter inserter = exec.makeInFileInserter(tempDir, LibraryVersionToClusterVersionTable.TABLE);
    
    task.start("Processing mappings", "mappings processed");
    for (Map.Entry<LibraryVersion, Integer> entry : versionMap.entrySet()) {
      for (ClusterVersion clusterVersion : entry.getKey().getClusters()) {
        inserter.addInsert(LibraryVersionToClusterVersionTable.createInsert(entry.getValue(), clusterVersionMap.get(clusterVersion)));
        task.progress();
      }
    }
    task.finish();
    
    task.start("Performing db insert");
    inserter.insert();
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
