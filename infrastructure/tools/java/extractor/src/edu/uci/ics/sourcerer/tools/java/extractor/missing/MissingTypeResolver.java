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
package edu.uci.ics.sourcerer.tools.java.extractor.missing;

import java.io.Closeable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import edu.uci.ics.sourcerer.tools.java.db.schema.ComponentRelationsTable;
import edu.uci.ics.sourcerer.tools.java.db.schema.ProjectsTable;
import edu.uci.ics.sourcerer.tools.java.db.schema.TypesTable;
import edu.uci.ics.sourcerer.tools.java.model.types.ComponentRelation;
import edu.uci.ics.sourcerer.tools.java.repo.model.JarFile;
import edu.uci.ics.sourcerer.tools.java.repo.model.JavaFile;
import edu.uci.ics.sourcerer.tools.java.repo.model.JavaRepository;
import edu.uci.ics.sourcerer.util.CollectionUtils;
import edu.uci.ics.sourcerer.util.io.logging.TaskProgressLogger;
import edu.uci.ics.sourcerer.utils.db.DatabaseConnection;
import edu.uci.ics.sourcerer.utils.db.DatabaseConnectionFactory;
import edu.uci.ics.sourcerer.utils.db.QueryExecutor;
import edu.uci.ics.sourcerer.utils.db.sql.ConstantCondition;
import edu.uci.ics.sourcerer.utils.db.sql.Querier;
import edu.uci.ics.sourcerer.utils.db.sql.Query;
import edu.uci.ics.sourcerer.utils.db.sql.SelectQuerier;
import edu.uci.ics.sourcerer.utils.db.sql.SelectQuery;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class MissingTypeResolver implements Closeable {
  private final DatabaseConnection conn;
  private final MissingTypeIdentifier identifier;
  private final JavaRepository repo;
  
  private Querier<String, Collection<Integer>> findClusterByType;
  private Querier<Integer, Collection<Integer>> findLibraryVersionByCluster;
  private Querier<Integer, Collection<Integer>> findClusterByLibraryVersion;
  private Querier<Integer, Integer> findTypeCountByCluster;
  private Querier<Integer, Collection<String>> findJarsByLibraryVersion;
  
  private MissingTypeResolver(JavaRepository repo) {
    identifier = MissingTypeIdentifier.create();
    conn = DatabaseConnectionFactory.INSTANCE.create();
    this.repo = repo;
  }
  
  public static MissingTypeResolver create(JavaRepository repo) {
    MissingTypeResolver resolver = new MissingTypeResolver(repo);
    if (resolver.conn.open()) {
      QueryExecutor exec = resolver.conn.getExecutor();

      resolver.findClusterByType = new SelectQuerier<String, Integer>(exec) {
        @Override
        public Query initialize() {
          query = exec.createSelectQuery(TypesTable.TABLE);
          cond = TypesTable.FQN.compareEquals();
          select = TypesTable.COMPONENT_ID;
          
          query.addSelect(select);
          query.andWhere(cond);
          return query;
        }
      };

      resolver.findLibraryVersionByCluster = new SelectQuerier<Integer, Integer>(exec) {
        @Override
        public Query initialize() {
          query = exec.createSelectQuery(ComponentRelationsTable.TABLE);
          cond = ComponentRelationsTable.TARGET_ID.compareEquals();
          select = ComponentRelationsTable.SOURCE_ID;
          
          query.addSelect(select);
          query.andWhere(cond, ComponentRelationsTable.TYPE.compareEquals(ComponentRelation.LIBRARY_VERSION_CONTAINS_CLUSTER));
          
          return query;
        }
      };

      resolver.findClusterByLibraryVersion = new SelectQuerier<Integer, Integer>(exec) {
        @Override
        public Query initialize() {
          query = exec.createSelectQuery(ComponentRelationsTable.TABLE);
          cond = ComponentRelationsTable.SOURCE_ID.compareEquals();
          select = ComponentRelationsTable.TARGET_ID;
          
          query.addSelect(select);
          query.andWhere(cond, ComponentRelationsTable.TYPE.compareEquals(ComponentRelation.LIBRARY_VERSION_CONTAINS_CLUSTER));
          
          return query;
        }
      };
      
      resolver.findTypeCountByCluster = new Querier<Integer, Integer>(exec) {
        SelectQuery query;
        ConstantCondition<Integer> cond;
        @Override
        public Query initialize() {
          query = exec.createSelectQuery(ComponentRelationsTable.TABLE);
          cond = ComponentRelationsTable.SOURCE_ID.compareEquals();
          
          query.setCount(true);
          query.setDistinct(true);
          query.addSelect(ComponentRelationsTable.TARGET_ID);
          query.andWhere(cond, ComponentRelationsTable.TYPE.compareIn(EnumSet.of(ComponentRelation.CLUSTER_CONTAINS_CORE_TYPE, ComponentRelation.CLUSTER_CONTAINS_VERSION_TYPE)));
          
          return query;
        }

        @Override
        public Integer selectHelper(Integer input) {
          cond.setValue(input);
          return query.select().toCount();
        }
      };
      
      resolver.findJarsByLibraryVersion = new SelectQuerier<Integer, String>(exec) {
        @Override
        public Query initialize() {
          query = exec.createSelectQuery(ComponentRelationsTable.SOURCE_ID.compareEquals(ProjectsTable.PROJECT_ID));
          cond = ComponentRelationsTable.TARGET_ID.compareEquals();
          select = ProjectsTable.HASH;
          
          query.addSelect(select);
          query.andWhere(cond, ComponentRelationsTable.TYPE.compareEquals(ComponentRelation.JAR_MATCHES_LIBRARY_VERSION));
          
          return query;
        }
        
      };

      return resolver;
    } else {
      return null;
    }
  }
    
  public Collection<JarFile> resolveMissingTypes(Collection<? extends JarFile> projectJars, Map<JavaFile, IFile> sourceFiles) {
    TaskProgressLogger task = TaskProgressLogger.get();
    
    task.start("Identifying missing types");
    MissingTypeCollection missingTypes = identifier.identifyMissingTypes(sourceFiles);
    task.report(missingTypes.getMissingTypeCount() + " missing types identified.");
    task.finish();
    
    Set<Integer> clusters = findMatchingClusters(missingTypes);
    Set<Integer> libraryVersions = matchClustersToLibraryVersions(clusters);
    Collection<JarFile> jars = matchLibraryVersionsToJarFiles(libraryVersions);
    
    return jars;
  }
  
  private Set<Integer> findMatchingClusters(MissingTypeCollection missingTypes) {
    TaskProgressLogger task = TaskProgressLogger.get();
    
    Set<Integer> clusters = new HashSet<>();
    task.start("Matching missing types to clusters");
    for (MissingType type : missingTypes.getMissingTypes()) {
      clusters.addAll(findClusterByType.select(type.getFqn()));
    }
    task.report(clusters.size() + " clusters identified.");
    task.finish();
    
    return clusters;
  }
  
  private Set<Integer> matchClustersToLibraryVersions(Set<Integer> clusters) {
    TaskProgressLogger task = TaskProgressLogger.get();
    
    task.start("Matching clusters to library versions");
    // Ideally we'd want to minimize the number of extra clusters included and the overlap of clusters between libraries
    // TODO augment this with some measure of how well the cluster matches the missing types: weight the clusters by how many types they contain
    final Multimap<Integer, Integer> lv2c = HashMultimap.create();
    final Multimap<Integer, Integer> c2lv = HashMultimap.create();
    final Map<Integer, Integer> clusterSizes = new HashMap<>();

    // Build the maps
    task.start("Building the maps"); 
    for (Integer cluster : clusters) {
      for (Integer libraryVersion : findLibraryVersionByCluster.select(cluster)) {
        c2lv.put(cluster, libraryVersion);
        for (Integer clus : findClusterByLibraryVersion.select(libraryVersion)) {
          lv2c.put(libraryVersion, clus);
        }
      }
    }
    task.report("Library versions by cluster: " + c2lv.keySet().size() + " keys, " + c2lv.size() + " entries");
    task.report("Cluster by library version: " + lv2c.keySet().size() + " keys, " + lv2c.size() + " entries");
    for (Integer clusterID : lv2c.values()) {
      if (!clusterSizes.containsKey(clusterID)) {
        clusterSizes.put(clusterID, findTypeCountByCluster.select(clusterID));
      }
    }
    task.finish();
    
    Set<Integer> coreLibraryVersions = new HashSet<>();
    Set<Integer> coveredClusters = new HashSet<>();
    
    // Start by picking all the library versions that don't contain extra clusters
    task.start("Checking for core library versions");
    for (Integer libraryVersionID : lv2c.keySet()) {
      boolean noExtra = true;
      for (Integer clusterID : lv2c.get(libraryVersionID)) {
        if (!clusters.contains(clusterID)) {
          noExtra = false;
        }
      }
      if (noExtra) {
        coreLibraryVersions.add(libraryVersionID);
        coveredClusters.addAll(lv2c.get(libraryVersionID));
      }
    }
    if (coveredClusters.retainAll(clusters)) {
      task.report("Retaining should have done nothing");
    }
    task.report(coveredClusters.size() + " of " + clusters.size() + " covered");
    task.finish();
    
    Set<Integer> finalLibraryVersions = new HashSet<>();
    Set<Integer> clustersToBeCovered = new HashSet<>(clusters);
    
    // If we covered all the clusters, skip this step
    if (coveredClusters.size() < clusters.size()) {
      task.start("Checking for additional library versions");
      final Set<Integer> missingClusters = new HashSet<>();
      for (Integer clusterID : clusters) {
        if (!coveredClusters.contains(clusterID)) {
          missingClusters.add(clusterID);
        }
      }
      task.report(missingClusters.size() + " missing clusters");
      Set<Integer> additionalLibraryVersions = new HashSet<>();
      // Find each library that can provide missing clusters, 
      // and measure their "cost per cluster" (number of extra clusters - number of clusters provided)
      // let's try measuring cost instead by number of extra types - number of types provided
      for (Integer clusterID : missingClusters) {
        Integer bestLibraryVersionID = null;
        int bestCost = Integer.MAX_VALUE;
        for (Integer libraryVersionID : c2lv.get(clusterID)) {
          Collection<Integer> clus = lv2c.get(libraryVersionID);
          int provided = 0;
          int extra = 0;
          for (Integer cluster : clus) {
            if (clusters.contains(cluster)) {
              provided += clusterSizes.get(cluster);
            } else {
              extra += clusterSizes.get(cluster);
            }
          }
          int cost = extra - provided;
          if (cost < bestCost) {
            bestLibraryVersionID = libraryVersionID;
            bestCost = cost;
          }
        }
        additionalLibraryVersions.add(bestLibraryVersionID);
      }
      task.report(additionalLibraryVersions.size() + " additional library versions identified");
      task.start("Sorting additional library versions");
      // Sort the additional library versions by the number of additional types they contain
      Integer[] arr = additionalLibraryVersions.toArray(new Integer[additionalLibraryVersions.size()]);
      Arrays.sort(arr, new Comparator<Integer>() {
        @Override
        public int compare(Integer o1, Integer o2) {
          return -Integer.compare(CollectionUtils.intersectionSize(lv2c.get(o1), missingClusters), CollectionUtils.intersectionSize(lv2c.get(o2), missingClusters));
        }
      });
      task.finish();
      task.start("Picking additional library versions");
      // Pick the libraries to actually add
      for (Integer libraryVersionID : arr) {
        Collection<Integer> clus = lv2c.get(libraryVersionID);
        if (CollectionUtils.containsAny(missingClusters, clus)) {
          finalLibraryVersions.add(libraryVersionID);
          missingClusters.removeAll(clus);
          clustersToBeCovered.removeAll(clus);
        }
      }
      task.report("Added " + finalLibraryVersions.size() + " library versions");
      task.finish();
      task.finish();
    }
    
    task.start("Sorting core library versions");
    // Now order the core libraries by the number of clusters they contain
    Integer[] arr = coreLibraryVersions.toArray(new Integer[coreLibraryVersions.size()]);
    Arrays.sort(arr, new Comparator<Integer>() {
      @Override
      public int compare(Integer o1, Integer o2) {
        return -Integer.compare(lv2c.get(o1).size(), lv2c.get(o2).size());
      }
    });
    task.finish();
    
    task.start("Picking core library versions");
    // Pick the core libraries to actually add
    for (Integer libraryVersionID : arr) {
      Collection<Integer> clus = lv2c.get(libraryVersionID);
      if (CollectionUtils.containsAny(clustersToBeCovered, clus)) {
        finalLibraryVersions.add(libraryVersionID);
        clustersToBeCovered.removeAll(clus);
      }
    }
    task.finish();
    
    task.report(finalLibraryVersions.size() + " library versions matched.");
    task.finish();
    
    return finalLibraryVersions;
  }
  
  private Collection<JarFile> matchLibraryVersionsToJarFiles(Set<Integer> libraryVerions) {
    TaskProgressLogger task = TaskProgressLogger.get();
    
    task.start("Matching library versions to jar files", "libraries versions processed", 1);
    Collection<JarFile> jars = new LinkedList<>();
    
    for (Integer libraryVersion : libraryVerions) {
      JarFile jar = null;
      task.report("Looking up jars for library version " + libraryVersion);
      for (String hash : findJarsByLibraryVersion.select(libraryVersion)) {
        task.report("getting jar file for hash: " + hash);
        jar = repo.getJarFile(hash);
        task.report("found jar file! " + jar);
        if (jar != null) {
          break;
        }
      }
      if (jar == null) {
        task.report("Unable to find jar for library version " + libraryVersion);
      } else {
        jars.add(jar);
      }
      task.progress();
    }
//    task.report(jars.size() + " library versions matched to jars.");
    task.finish();
    return jars;
  }
  
  @Override
  public void close() {
    conn.close();
  }
}
