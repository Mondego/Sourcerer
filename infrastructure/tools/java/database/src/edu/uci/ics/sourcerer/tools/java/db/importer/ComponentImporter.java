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
package edu.uci.ics.sourcerer.tools.java.db.importer;

import static edu.uci.ics.sourcerer.util.io.logging.Logging.logger;
import java.io.File;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import edu.uci.ics.sourcerer.tools.java.db.schema.ComponentRelationsTable;
import edu.uci.ics.sourcerer.tools.java.db.schema.ComponentsTable;
import edu.uci.ics.sourcerer.tools.java.db.schema.ProjectsTable;
import edu.uci.ics.sourcerer.tools.java.db.schema.TypeVersionsTable;
import edu.uci.ics.sourcerer.tools.java.db.schema.TypesTable;
import edu.uci.ics.sourcerer.tools.java.model.types.Project;
import edu.uci.ics.sourcerer.tools.java.utilization.model.ComponentRelationType;
import edu.uci.ics.sourcerer.tools.java.utilization.model.ComponentType;
import edu.uci.ics.sourcerer.tools.java.utilization.model.TypeType;
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
import edu.uci.ics.sourcerer.tools.java.utilization.repo.db.DatabaseImporter;
import edu.uci.ics.sourcerer.util.io.FileUtils;
import edu.uci.ics.sourcerer.util.io.logging.TaskProgressLogger;
import edu.uci.ics.sourcerer.utils.db.BatchInserter;
import edu.uci.ics.sourcerer.utils.db.DatabaseRunnable;
import edu.uci.ics.sourcerer.utils.db.sql.SelectQuery;
import edu.uci.ics.sourcerer.utils.db.sql.TypedQueryResult;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class ComponentImporter extends DatabaseRunnable {
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
  
  private ComponentImporter(JarCollection jars, ClusterCollection clusters, Repository repo) {
    this.jars = jars;
    this.clusters = clusters;
    this.repo = repo;
    tempDir = FileUtils.getTempDir();
  }
  
  public static void importComponents(JarCollection jars, ClusterCollection clusters, Repository repo) {
    new ComponentImporter(jars, clusters, repo).run();
  }
  
  @Override
  protected void action() {
    importClusters();
    importClusterVersions();
    loadJarMapping();
    importFqns();
    importFqnVersions();
    importComponentRelations();
  }
  
  private void importClusters() {
    TaskProgressLogger task = TaskProgressLogger.get();
    task.start("Importing clusters");
    
    BatchInserter inserter = exec.makeInFileInserter(tempDir, ComponentsTable.TABLE);
    
    task.start("Processing clusters", "clusters processed");
    for (Cluster cluster : clusters) {
      inserter.addInsert(ComponentsTable.createInsert(cluster));
      task.progress();
    }
    task.finish();
    
    task.start("Performing db insert into components table");
    inserter.insert();
    task.finish();
   
    task.start("Loading cluster mapping", "clusters loaded");
    clusterMap = new HashMap<>();
    try (SelectQuery query = exec.makeSelectQuery(ComponentsTable.TABLE)) {
      query.addSelects(ComponentsTable.COMPONENT_ID);
      query.andWhere(ComponentsTable.TYPE.compareEquals(ComponentType.CLUSTER));
      query.orderBy(ComponentsTable.COMPONENT_ID, true);
      
      TypedQueryResult result = query.select();
      for (Cluster cluster : clusters) {
        result.next();
        clusterMap.put(cluster, result.getResult(ComponentsTable.COMPONENT_ID));
        task.progress();
      }
    }
    task.finish();
    
    task.finish();
  }
  
  private void importClusterVersions() {
    TaskProgressLogger task = TaskProgressLogger.get();
    task.start("Importing cluster versions");
    
    BatchInserter inserter = exec.makeInFileInserter(tempDir, ComponentsTable.TABLE);
    
    task.start("Processing cluster versions", "cluster versions processed");
    for (Cluster cluster : clusters) {
      for (ClusterVersion version : cluster.getVersions()) {
        inserter.addInsert(ComponentsTable.createInsert(version));
        task.progress();
      }
    }
    task.finish();
    
    task.start("Performing db insert into components table");
    inserter.insert();
    task.finish();
   
    task.start("Loading cluster version mapping", "cluster verions loaded");
    clusterVersionMap = new HashMap<>();
    try (SelectQuery query = exec.makeSelectQuery(ComponentsTable.TABLE)) {
      query.addSelects(ComponentsTable.COMPONENT_ID);
      query.andWhere(ComponentsTable.TYPE.compareEquals(ComponentType.CLUSTER_VERSION));
      query.orderBy(ComponentsTable.COMPONENT_ID, true);
      
      TypedQueryResult result = query.select();
      for (Cluster cluster : clusters) {
        for (ClusterVersion version : cluster.getVersions()) {
          result.next();
          clusterVersionMap.put(version, result.getResult(ComponentsTable.COMPONENT_ID));
          task.progress();
        }
      }
    }
    task.finish();
        
    task.finish();
  }
  
  private void loadJarMapping() {
    TaskProgressLogger task = TaskProgressLogger.get();
    
    task.start("Loading jar mapping", "jars loaded");
    jarMap = new HashMap<>();
    try (SelectQuery query = exec.makeSelectQuery(ProjectsTable.TABLE)) {
      query.addSelects(ProjectsTable.PROJECT_ID, ProjectsTable.HASH);
      query.andWhere(ProjectsTable.PROJECT_TYPE.compareIn(EnumSet.of(Project.JAR, Project.MAVEN)));

      TypedQueryResult result = query.select();
      while (result.next()) {
        String hash = result.getResult(ProjectsTable.HASH);
        Jar jar = jars.getJar(hash);
        if (jar == null) {
          logger.severe("Unable to locate jar: " + hash);
        } else {
          jarMap.put(jar, result.getResult(ProjectsTable.PROJECT_ID));
        }
        task.progress();
      }
    }
    task.finish();
    
    task.finish();
  }
  
  private void importFqns() {
    TaskProgressLogger task = TaskProgressLogger.get();
    task.start("Importing fqns");
    
    BatchInserter inserter = exec.makeInFileInserter(tempDir, TypesTable.TABLE);
    
    task.start("Processing fqns", "fqns processed");
    for (Map.Entry<Cluster, Integer> entry : clusterMap.entrySet()) {
      for (VersionedFqnNode fqn : entry.getKey().getCoreFqns()) {
        inserter.addInsert(TypesTable.createInsert(TypeType.CORE, fqn, entry.getValue()));
        task.progress();
      }
      for (VersionedFqnNode fqn : entry.getKey().getVersionFqns()) {
        inserter.addInsert(TypesTable.createInsert(TypeType.VERSION, fqn, entry.getValue()));
        task.progress();
      }
    }
    task.finish();
    
    task.start("Performing db insert");
    inserter.insert();
    task.finish();
   
    task.start("Loading fqn mapping", "fqns loaded");
    fqnMap = new HashMap<>();
    try (SelectQuery query = exec.makeSelectQuery(TypesTable.TABLE)) {
      query.addSelects(TypesTable.TYPE_ID);
      query.orderBy(TypesTable.TYPE_ID, true);
      
      TypedQueryResult result = query.select();
      for (Map.Entry<Cluster, Integer> entry : clusterMap.entrySet()) {
        for (VersionedFqnNode fqn : entry.getKey().getCoreFqns()) {
          result.next();
          fqnMap.put(fqn, result.getResult(TypesTable.TYPE_ID));
          task.progress();
        }
        for (VersionedFqnNode fqn : entry.getKey().getVersionFqns()) {
          result.next();
          fqnMap.put(fqn, result.getResult(TypesTable.TYPE_ID));
          task.progress();
        }
      }
    }
    task.finish();
    
    task.finish();
  }
  
  private void importFqnVersions() {
    TaskProgressLogger task = TaskProgressLogger.get();
    task.start("Importing fqn versions");
    
    BatchInserter inserter = exec.makeInFileInserter(tempDir, TypeVersionsTable.TABLE);
    
    task.start("Processing fqn versions", "fqns processed");
    for (Map.Entry<VersionedFqnNode, Integer> entry : fqnMap.entrySet()) {
      for (FqnVersion version : entry.getKey().getVersions()) {
        inserter.addInsert(TypeVersionsTable.createInsert(entry.getValue(), version.getFingerprint().serialize()));
        task.progress();
      }
    }
    task.finish();
    
    task.start("Performing db insert");
    inserter.insert();
    task.finish();
   
    task.start("Loading fqn version mapping", "fqn versions loaded");
    fqnVersionMap = new HashMap<>();
    try (SelectQuery query = exec.makeSelectQuery(TypeVersionsTable.TABLE)) {
      query.addSelects(TypeVersionsTable.TYPE_VERSION_ID);
      query.orderBy(TypeVersionsTable.TYPE_VERSION_ID, true);
      
      TypedQueryResult result = query.select();
      for (Map.Entry<VersionedFqnNode, Integer> entry : fqnMap.entrySet()) {
        for (FqnVersion version : entry.getKey().getVersions()) {
          result.next();
          fqnVersionMap.put(version, result.getResult(TypeVersionsTable.TYPE_VERSION_ID));
          task.progress();
        }
      }
    }
    task.finish();
    
    task.finish();
  }
  
  private void importComponentRelations() {
    TaskProgressLogger task = TaskProgressLogger.get();
    task.start("Importing cluster relations");
    
    BatchInserter inserter = exec.makeInFileInserter(tempDir, ComponentRelationsTable.TABLE);
    
    task.start("Processing cluster to cluster version mapping", "mappings processed");
    for (Cluster cluster : clusters) {
      for (ClusterVersion version : cluster.getVersions()) {
        inserter.addInsert(ComponentRelationsTable.createInsert(ComponentRelationType.CLUSTER_CONTAINS_VERSION, clusterMap.get(cluster), clusterVersionMap.get(version)));
        task.progress();
      }
    }
    task.finish();
    
    task.start("Processing jar to cluster version mapping", "mappings processed");
    for (Map.Entry<ClusterVersion, Integer> entry : clusterVersionMap.entrySet()) {
      for (Jar jar : entry.getKey().getJars()) {
        inserter.addInsert(ComponentRelationsTable.createInsert(ComponentRelationType.JAR_CONTAINS_CLUSTER_VERSION, jarMap.get(jar), entry.getValue()));
        task.progress();
      }
    }
    task.finish();
    
    task.start("Processing jar to fqn version mapping", "mappings processed");
    for (Map.Entry<Jar, Integer> entry : jarMap.entrySet()) {
      for (FqnVersion fqn : entry.getKey().getFqns()) {
        inserter.addInsert(ComponentRelationsTable..createInsert(ComponentRelationType.JAR_CONTAINS_FQN_VERSION, entry.getValue(), fqnVersionMap.get(fqn)));
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
