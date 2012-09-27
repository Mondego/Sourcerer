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
import java.util.logging.Level;

import edu.uci.ics.sourcerer.tools.java.component.model.cluster.Cluster;
import edu.uci.ics.sourcerer.tools.java.component.model.cluster.ClusterCollection;
import edu.uci.ics.sourcerer.tools.java.component.model.cluster.ClusterVersion;
import edu.uci.ics.sourcerer.tools.java.component.model.jar.FqnVersion;
import edu.uci.ics.sourcerer.tools.java.component.model.jar.Jar;
import edu.uci.ics.sourcerer.tools.java.component.model.jar.JarCollection;
import edu.uci.ics.sourcerer.tools.java.component.model.jar.VersionedFqnNode;
import edu.uci.ics.sourcerer.tools.java.component.model.repo.ComponentRepository;
import edu.uci.ics.sourcerer.tools.java.component.model.repo.Library;
import edu.uci.ics.sourcerer.tools.java.component.model.repo.LibraryVersion;
import edu.uci.ics.sourcerer.tools.java.db.schema.ComponentMetricsTable;
import edu.uci.ics.sourcerer.tools.java.db.schema.ComponentRelationsTable;
import edu.uci.ics.sourcerer.tools.java.db.schema.ComponentsTable;
import edu.uci.ics.sourcerer.tools.java.db.schema.ProjectsTable;
import edu.uci.ics.sourcerer.tools.java.db.schema.TypeVersionsTable;
import edu.uci.ics.sourcerer.tools.java.db.schema.TypesTable;
import edu.uci.ics.sourcerer.tools.java.model.types.Component;
import edu.uci.ics.sourcerer.tools.java.model.types.ComponentRelation;
import edu.uci.ics.sourcerer.tools.java.model.types.Project;
import edu.uci.ics.sourcerer.tools.java.model.types.Type;
import edu.uci.ics.sourcerer.tools.java.repo.model.JarProperties;
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
  private final ComponentRepository repo;
  
  private final File tempDir;
  
  private Map<Cluster, Integer> clusterMap;
  private Map<ClusterVersion, Integer> clusterVersionMap;
  private Map<VersionedFqnNode, Integer> fqnMap;
  private Map<Jar, Integer> jarMap;
  private Map<FqnVersion, Integer> fqnVersionMap;
  private Map<Library, Integer> libraryMap;
  private Map<LibraryVersion, Integer> libraryVersionMap;
  
  private ComponentImporter(ComponentRepository repo) {
    this.jars = repo.getJars();
    this.clusters = repo.getClusters();
    this.repo = repo;
    tempDir = FileUtils.getTempDir();
  }
  
  public static void importComponents(ComponentRepository repo) {
    new ComponentImporter(repo).run();
  }
  
  @Override
  protected void action() {
    TaskProgressLogger task = TaskProgressLogger.get();
    task.start("Importing component repository to database");
    
    initializeTables();
    importClusters();
    importClusterVersions();
    loadJarMapping();
    importFqns();
    importFqnVersions();
    importLibraries();
    importLibraryVersions();
    importComponentRelations();
    
    task.finish();
  }
  
  private void initializeTables() {
    exec.dropTables(
        ComponentRelationsTable.TABLE,
        ComponentMetricsTable.TABLE,
        ComponentsTable.TABLE,
        TypesTable.TABLE,
        TypeVersionsTable.TABLE);
    exec.createTables(
        ComponentRelationsTable.TABLE,
        ComponentMetricsTable.TABLE,
        ComponentsTable.TABLE,
        TypesTable.TABLE,
        TypeVersionsTable.TABLE);
  }
  
  private void importClusters() {
    TaskProgressLogger task = TaskProgressLogger.get();
    task.start("Importing clusters");
    
    BatchInserter inserter = exec.makeInFileInserter(tempDir, ComponentsTable.TABLE);
    
    task.start("Processing clusters", "clusters processed");
    for (@SuppressWarnings("unused") Cluster cluster : clusters) {
      inserter.addInsert(ComponentsTable.createInsert(Component.CLUSTER, null));
      task.progress();
    }
    task.finish();
    
    task.start("Performing db insert into components table");
    inserter.insert();
    task.finish();
   
    task.start("Loading cluster mapping", "clusters loaded");
    clusterMap = new HashMap<>();
    try (SelectQuery query = exec.createSelectQuery(ComponentsTable.TABLE)) {
      query.addSelect(ComponentsTable.COMPONENT_ID);
      query.andWhere(ComponentsTable.TYPE.compareEquals(Component.CLUSTER));
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
      for (@SuppressWarnings("unused") ClusterVersion version : cluster.getVersions()) {
        inserter.addInsert(ComponentsTable.createInsert(Component.CLUSTER_VERSION, null));
        task.progress();
      }
    }
    task.finish();
    
    task.start("Performing db insert into components table");
    inserter.insert();
    task.finish();
   
    task.start("Loading cluster version mapping", "cluster verions loaded");
    clusterVersionMap = new HashMap<>();
    try (SelectQuery query = exec.createSelectQuery(ComponentsTable.TABLE)) {
      query.addSelect(ComponentsTable.COMPONENT_ID);
      query.andWhere(ComponentsTable.TYPE.compareEquals(Component.CLUSTER_VERSION));
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
    try (SelectQuery query = exec.createSelectQuery(ProjectsTable.TABLE)) {
      query.addSelect(ProjectsTable.PROJECT_ID, ProjectsTable.HASH);
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
    
    if (jars.size() > jarMap.size()) {
      task.start("Importing additional jars");
      BatchInserter inserter = exec.makeInFileInserter(tempDir, ProjectsTable.TABLE);

      task.start("Processing jars", "jars processed");
      for (Jar jar : jars) {
        if (!jarMap.containsKey(jar)) {
        JarProperties props = jar.getJar().getProperties();
        Project type = null;
        switch (props.SOURCE.getValue()) {
          case JAVA_LIBRARY: type = Project.JAVA_LIBRARY; break;
          case MAVEN: type = Project.MAVEN; break;
          case PROJECT: type = Project.JAR; break;
        }
        inserter.addInsert(ProjectsTable.createRowInsert(
            type,
            props.NAME.getValue(), 
            null, 
            props.VERSION.getValue(), 
            props.GROUP.getValue(), 
            ProjectsTable.ProjectState.COMPONENT.name(), // no path
            null, // no source
            props.HASH.getValue(), 
            false)); // not sure if it has source
          task.progress();
        }
      }
      task.finish();
      
      task.start("Performing db insert");
      inserter.insert();
      task.finish();

      task.start("Reloading jar mapping", "jars loaded");
      jarMap = new HashMap<>();
      try (SelectQuery query = exec.createSelectQuery(ProjectsTable.TABLE)) {
        query.addSelect(ProjectsTable.PROJECT_ID, ProjectsTable.HASH);
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
      
      if (jars.size() > jarMap.size()) {
        logger.log(Level.SEVERE, "Unable to insert sufficient jars into database: " + jars.size() + " vs " + jarMap.size());
      }
      task.finish();
    }
  }
  
  private void importFqns() {
    TaskProgressLogger task = TaskProgressLogger.get();
    task.start("Importing fqns");
    
    BatchInserter inserter = exec.makeInFileInserter(tempDir, TypesTable.TABLE);
    
    task.start("Processing fqns", "fqns processed");
    for (Map.Entry<Cluster, Integer> entry : clusterMap.entrySet()) {
      for (VersionedFqnNode fqn : entry.getKey().getCoreFqns()) {
        inserter.addInsert(TypesTable.createInsert(Type.CORE, fqn.getFqn(), entry.getValue()));
        task.progress();
      }
      for (VersionedFqnNode fqn : entry.getKey().getVersionFqns()) {
        inserter.addInsert(TypesTable.createInsert(Type.VERSION, fqn.getFqn(), entry.getValue()));
        task.progress();
      }
    }
    task.finish();
    
    task.start("Performing db insert");
    inserter.insert();
    task.finish();
   
    task.start("Loading fqn mapping", "fqns loaded");
    fqnMap = new HashMap<>();
    try (SelectQuery query = exec.createSelectQuery(TypesTable.TABLE)) {
      query.addSelect(TypesTable.TYPE_ID);
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
    try (SelectQuery query = exec.createSelectQuery(TypeVersionsTable.TABLE)) {
      query.addSelect(TypeVersionsTable.TYPE_VERSION_ID);
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
  
  private void importLibraries() {
    TaskProgressLogger task = TaskProgressLogger.get();
    task.start("Importing libraries");
    
    BatchInserter inserter = exec.makeInFileInserter(tempDir, ComponentsTable.TABLE);
    
    task.start("Processing libraries", "librariesprocessed");
    for (@SuppressWarnings("unused") Library library : repo.getLibraries()) {
      inserter.addInsert(ComponentsTable.createInsert(Component.LIBRARY, null));
      task.progress();
    }
    task.finish();
    
    task.start("Performing db insert into components table");
    inserter.insert();
    task.finish();
   
    task.start("Loading library mapping", "libraries loaded");
    libraryMap = new HashMap<>();
    try (SelectQuery query = exec.createSelectQuery(ComponentsTable.TABLE)) {
      query.addSelect(ComponentsTable.COMPONENT_ID);
      query.andWhere(ComponentsTable.TYPE.compareEquals(Component.LIBRARY));
      query.orderBy(ComponentsTable.COMPONENT_ID, true);
      
      TypedQueryResult result = query.select();
      for (Library library : repo.getLibraries()) {
        result.next();
        libraryMap.put(library, result.getResult(ComponentsTable.COMPONENT_ID));
        task.progress();
      }
    }
    task.finish();
    
    task.finish();
  }
  
  private void importLibraryVersions() {
    TaskProgressLogger task = TaskProgressLogger.get();
    task.start("Importing library versions");
    
    BatchInserter inserter = exec.makeInFileInserter(tempDir, ComponentsTable.TABLE);
    
    task.start("Processing library versions", "library versions processed");
    for (Map.Entry<Library, Integer> entry : libraryMap.entrySet()) {
      for (@SuppressWarnings("unused") LibraryVersion version : entry.getKey().getVersions()) {
        inserter.addInsert(ComponentsTable.createInsert(Component.LIBRARY_VERSION, null));
        task.progress();
      }
    }
    task.finish();
    
    task.start("Performing db insert into components table");
    inserter.insert();
    task.finish();
   
    task.start("Loading library version mapping", "versions loaded");
    libraryVersionMap = new HashMap<>();
    try (SelectQuery query = exec.createSelectQuery(ComponentsTable.TABLE)) {
      query.addSelect(ComponentsTable.COMPONENT_ID);
      query.andWhere(ComponentsTable.TYPE.compareEquals(Component.LIBRARY_VERSION));
      query.orderBy(ComponentsTable.COMPONENT_ID, true);
      
      TypedQueryResult result = query.select();
      for (Map.Entry<Library, Integer> entry : libraryMap.entrySet()) {
        for (LibraryVersion version : entry.getKey().getVersions()) {
          result.next();
          libraryVersionMap.put(version, result.getResult(ComponentsTable.COMPONENT_ID));
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
        inserter.addInsert(ComponentRelationsTable.createInsert(ComponentRelation.CLUSTER_CONTAINS_CLUSTER_VERSION, clusterMap.get(cluster), clusterVersionMap.get(version)));
        task.progress();
      }
    }
    task.finish();
    
    task.start("Processing cluster to type mapping", "mappings processed");
    for (Cluster cluster : clusters) {
      for (VersionedFqnNode fqn : cluster.getCoreFqns()) {
        inserter.addInsert(ComponentRelationsTable.createInsert(ComponentRelation.CLUSTER_CONTAINS_CORE_TYPE, clusterMap.get(cluster), fqnMap.get(fqn)));
        task.progress();
      }
      for (VersionedFqnNode fqn : cluster.getVersionFqns()) {
        inserter.addInsert(ComponentRelationsTable.createInsert(ComponentRelation.CLUSTER_CONTAINS_VERSION_TYPE, clusterMap.get(cluster), fqnMap.get(fqn)));
        task.progress();
      }
    }
    task.finish();
    
    task.start("Processing cluster version to fqn version mapping", "mappings processed");
    for (Map.Entry<ClusterVersion, Integer> entry : clusterVersionMap.entrySet()) {
      for (FqnVersion fqn : entry.getKey().getFqns()) {
        inserter.addInsert(ComponentRelationsTable.createInsert(ComponentRelation.CLUSTER_VERSION_CONTAINS_TYPE_VERSION, entry.getValue(), fqnVersionMap.get(fqn)));
        task.progress();
      }
    }
    task.finish();
    
    task.start("Processing library to cluster mapping", "mappings processed");
    for (Map.Entry<Library, Integer> entry : libraryMap.entrySet()) {
      Cluster coreCluster = entry.getKey().getCoreCluster();
      if (coreCluster != null) {
        inserter.addInsert(ComponentRelationsTable.createInsert(ComponentRelation.LIBRARY_MATCHES_CLUSTER, entry.getValue(), clusterMap.get(coreCluster)));
        task.progress();
      }
      for (Cluster cluster : entry.getKey().getSecondaryClusters()) {
        inserter.addInsert(ComponentRelationsTable.createInsert(ComponentRelation.LIBRARY_CONTAINS_CLUSTER, entry.getValue(), clusterMap.get(cluster)));
        task.progress();
      }
    }
    task.finish();
    
    task.start("Processing library to library version mapping", "mappings processed");
    for (Map.Entry<Library, Integer> entry : libraryMap.entrySet()) {
      for (LibraryVersion version : entry.getKey().getVersions()) {
        inserter.addInsert(ComponentRelationsTable.createInsert(ComponentRelation.LIBRARY_CONTAINS_LIBRARY_VERSION, entry.getValue(), libraryVersionMap.get(version)));
        task.progress();
      }
    }
    task.finish();
    
    task.start("Processing library version to library mapping", "mappings processed");
    for (Map.Entry<LibraryVersion, Integer> entry : libraryVersionMap.entrySet()) {
      for (Library dep : entry.getKey().getLibraryDependencies()) {
        inserter.addInsert(ComponentRelationsTable.createInsert(ComponentRelation.LIBRARY_VERSION_CONTAINS_LIBRARY, entry.getValue(), libraryMap.get(dep)));
        task.progress();
      }
    }
    task.finish();
    
    task.start("Processing library version to library version mapping", "mappings processed");
    for (Map.Entry<LibraryVersion, Integer> entry : libraryVersionMap.entrySet()) {
      for (LibraryVersion dep : entry.getKey().getVersionDependencies()) {
        inserter.addInsert(ComponentRelationsTable.createInsert(ComponentRelation.LIBRARY_VERSION_CONTAINS_LIBRARY_VERSION, entry.getValue(), libraryVersionMap.get(dep)));
        task.progress();
      }
    }
    task.finish();
    
    task.start("Processing library version to cluster version mapping", "mappings processed");
    for (Map.Entry<LibraryVersion, Integer> entry : libraryVersionMap.entrySet()) {
      for (ClusterVersion clusterVersion : entry.getKey().getClusters()) {
        inserter.addInsert(ComponentRelationsTable.createInsert(ComponentRelation.LIBRARY_VERSION_CONTAINS_CLUSTER_VERSION, entry.getValue(), clusterVersionMap.get(clusterVersion)));
        task.progress();
      }
    }
    task.finish();
    
    task.start("Processing library version to fqn version mapping", "mappings processed");
    for (Map.Entry<LibraryVersion, Integer> entry : libraryVersionMap.entrySet()) {
      for (FqnVersion fqn : entry.getKey().getFqnVersions()) {
        inserter.addInsert(ComponentRelationsTable.createInsert(ComponentRelation.LIBRARY_VERSION_CONTAINS_TYPE_VERSION, entry.getValue(), fqnVersionMap.get(fqn)));
        task.progress();
      }
    }
    task.finish();
    
    task.start("Processing library version to cluster mapping", "mappings processed");
    for (Map.Entry<LibraryVersion, Integer> entry : libraryVersionMap.entrySet()) {
      for (ClusterVersion clusterVersion : entry.getKey().getClusters()) {
        inserter.addInsert(ComponentRelationsTable.createInsert(ComponentRelation.LIBRARY_VERSION_CONTAINS_CLUSTER, entry.getValue(), clusterMap.get(clusterVersion.getCluster())));
        task.progress();
      }
    }
    task.finish();
    
    task.start("Processing jar to cluster version mapping", "mappings processed");
    for (Map.Entry<ClusterVersion, Integer> entry : clusterVersionMap.entrySet()) {
      for (Jar jar : entry.getKey().getJars()) {
        inserter.addInsert(ComponentRelationsTable.createInsert(ComponentRelation.JAR_CONTAINS_CLUSTER_VERSION, jarMap.get(jar), entry.getValue()));
        task.progress();
      }
    }
    task.finish();
    
    task.start("Processing jar to fqn version mapping", "mappings processed");
    for (Map.Entry<Jar, Integer> entry : jarMap.entrySet()) {
      for (FqnVersion fqn : entry.getKey().getFqns()) {
        inserter.addInsert(ComponentRelationsTable.createInsert(ComponentRelation.JAR_CONTAINS_TYPE_VERSION, entry.getValue(), fqnVersionMap.get(fqn)));
        task.progress();
      }
    }
    task.finish();
    
    task.start("Processing jar to library version mapping", "mappings processed");
    for (Map.Entry<LibraryVersion, Integer> entry : libraryVersionMap.entrySet()) {
      for (Jar jar : entry.getKey().getJars()) {
        inserter.addInsert(ComponentRelationsTable.createInsert(ComponentRelation.JAR_MATCHES_LIBRARY_VERSION, jarMap.get(jar), entry.getValue()));
        task.progress();
      }
    }
    task.finish();
    
    task.start("Processing jar to cluster mapping", "mappings processed");
    for (Map.Entry<Cluster, Integer> entry : clusterMap.entrySet()) {
      for (Jar jar : entry.getKey().getJars()) {
        inserter.addInsert(ComponentRelationsTable.createInsert(ComponentRelation.JAR_CONTAINS_CLUSTER, jarMap.get(jar), entry.getValue()));
        task.progress();
      }
    }
    task.finish();
    
    task.start("Processing library to jar mapping", "mappings processed");
    for (Map.Entry<Library, Integer> entry : libraryMap.entrySet()) {
      for (Jar jar : entry.getKey().getJars()) {
        inserter.addInsert(ComponentRelationsTable.createInsert(ComponentRelation.LIBRARY_CONTAINS_JAR, entry.getValue(), jarMap.get(jar)));
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
