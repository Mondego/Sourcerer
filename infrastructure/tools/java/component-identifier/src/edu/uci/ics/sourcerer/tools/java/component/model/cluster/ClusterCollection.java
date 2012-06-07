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
package edu.uci.ics.sourcerer.tools.java.component.model.cluster;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class ClusterCollection implements Iterable<Cluster> {
  private ArrayList<Cluster> clusters;
  private ClusterMatcher matcher;
  
  private ClusterCollection() {}
  
  public static ClusterCollection create(Collection<Cluster> clusters) {
    ClusterCollection collection = new ClusterCollection();
    
    collection.clusters = new ArrayList<>(clusters);
    
    return collection;
  }
  
//  public static ClusterCollection load(File file, JarCollection jars) {
//    TaskProgressLogger task = TaskProgressLogger.get();
//    
//    ClusterCollection collection = new ClusterCollection();
//    
//    collection.clusters = new ArrayList<>(1000);
//    task.start("Loading cluster collection", "clusters loaded", 500);
//    try (SimpleDeserializer deserializer = IOUtils.makeSimpleDeserializer(file)) {
//      for (Cluster cluster : deserializer.deserializeToIterable(Cluster.makeDeserializer(jars), true)) {
//        task.progress();
//        collection.clusters.add(cluster);
//      }
//      // Would use a finally block, but that crashes eclipse!
//      task.finish();
//      return collection;
//    } catch (IOException e) {
//      logger.log(Level.SEVERE, "Error loading cluster collection", e);
//      task.finish();
//      return null;
//    }
//  }
  
  public void reset(Collection<Cluster> clusters) {
    this.clusters = new ArrayList<>(clusters);
    matcher = null;
  }
  
//  public void save(File file) {
//    try (SimpleSerializer serializer = IOUtils.makeSimpleSerializer(file)) {
//      serializer.serialize(clusters);
//    } catch (IOException e) {
//      logger.log(Level.SEVERE, "Error saving cluster collection", e);
//    }
//  }
  
  public Collection<Cluster> getClusters() {
    return clusters;
  }

  @Override
  public Iterator<Cluster> iterator() {
    return clusters.iterator();
  }
  
  public Iterable<Cluster> byDescendingSize() {
    Cluster[] clus = clusters.toArray(new Cluster[clusters.size()]);
    Arrays.sort(clus, Cluster.DESCENDING_SIZE_COMPARATOR);
    return Arrays.asList(clus);
  }

  public int size() {
    return clusters.size();
  }
  
  public ClusterMatcher getClusterMatcher() {
    if (matcher == null) {
      matcher = ClusterMatcher.create(this);
    }
    return matcher;
  }
}
