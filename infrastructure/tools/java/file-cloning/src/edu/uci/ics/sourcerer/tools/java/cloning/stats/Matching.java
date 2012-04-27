package edu.uci.ics.sourcerer.tools.java.cloning.stats;
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
//package edu.uci.ics.sourcerer.clusterer.cloning.stats;
//
//import static edu.uci.ics.sourcerer.util.io.Logging.logger;
//
//import java.io.BufferedWriter;
//import java.io.IOException;
//import java.util.Arrays;
//import java.util.Collection;
//import java.util.Comparator;
//import java.util.Iterator;
//import java.util.Map;
//import java.util.logging.Level;
//
//import edu.uci.ics.sourcerer.util.Helper;
//import edu.uci.ics.sourcerer.util.io.FileUtils;
//
///**
// * @author Joel Ossher (jossher@uci.edu)
// */
//public class Matching implements Iterable<FileCluster> {
//  private Collection<FileCluster> files;
//  
//  public Matching() {
//    files = Helper.newHashSet();
//  }
//  
//  public void addCluster(FileCluster cluster) {
//    files.add(cluster);
//  }
//  
//  public MatchingStatistics getStatistics() {
//    int globalUniqueFiles = files.size();
//    
//    int totalFiles = 0;
//    int projectUniqueFiles = 0;
//    int singletonFiles = 0;
//    int uniqueDuplicateFiles = 0;
//    int totalDuplicateFiles = 0;
//
//    for (FileCluster cluster : files) {
//      totalFiles += cluster.getFileCount();
//      projectUniqueFiles += cluster.getProjectCount();
//      if (cluster.getFileCount() == 1) {
//        singletonFiles++;
//      } else {
//        uniqueDuplicateFiles++;
//        totalDuplicateFiles += cluster.getFileCount();
//      }
//    }
//    
//    return new MatchingStatistics(totalFiles, 0, projectUniqueFiles, singletonFiles, globalUniqueFiles, uniqueDuplicateFiles, totalDuplicateFiles, 0);
//  }
//
//  public void printCloningByProject(String file) {
//    class FileCounter {
//      int uniqueFiles = 0;
//      int duplicateFiles = 0;
//    }
//    Map<String, FileCounter> map = Helper.newHashMap();
//    for (FileCluster cluster : files) {
//      if (cluster.getProjectCount() == 1) {
//        for (String project : cluster.getProjects()) {
//          FileCounter counter = map.get(project);
//          if (counter == null) {
//            counter = new FileCounter();
//            map.put(project, counter);
//          }
//          counter.uniqueFiles++;
//        }
//      } else {
//        for (String project : cluster.getProjects()) {
//          FileCounter counter = map.get(project);
//          if (counter == null) {
//            counter = new FileCounter();
//            map.put(project, counter);
//          }
//          counter.duplicateFiles++;
//        }
//      }
//    }
//    
//    BufferedWriter bw = null;
//    try {
//      bw = FileUtils.getBufferedWriter(file);
//      for (Map.Entry<String, FileCounter> entry : map.entrySet()) {
//        bw.write(entry.getKey() + " " + entry.getValue().uniqueFiles + " " + entry.getValue().duplicateFiles + "\n");
//      }
//    } catch (IOException e) {
//      logger.log(Level.SEVERE, "Error printing cloning.", e);
//    } finally {
//      FileUtils.close(bw);
//    }
//  }
//  
//  public Iterable<FileCluster> getRankedClusters() {
//    FileCluster[] array = files.toArray(new FileCluster[files.size()]);
//    Arrays.sort(array, new Comparator<FileCluster>() {
//      @Override
//      public int compare(FileCluster o1, FileCluster o2) {
//        return o2.getProjectCount() - o1.getProjectCount();
//      }});
//    return Arrays.asList(array);
//  }
//  
//  @Override
//  public Iterator<FileCluster> iterator() {
//    return files.iterator();
//  }
//}
