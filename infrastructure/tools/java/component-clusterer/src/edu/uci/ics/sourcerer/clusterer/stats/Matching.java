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
package edu.uci.ics.sourcerer.clusterer.stats;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import edu.uci.ics.sourcerer.util.Helper;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class Matching implements Iterable<FileCluster> {
  private Collection<FileCluster> files;
  
  public Matching() {
    files = Helper.newHashSet();
  }
  
  public void addCluster(FileCluster cluster) {
    files.add(cluster);
  }
  
  public MatchingStatistics getStatistics() {
    int globalUniqueFiles = files.size();
    
    int totalFiles = 0;
    int projectUniqueFiles = 0;
    int singletonFiles = 0;
    int uniqueDuplicateFiles = 0;
    int totalDuplicateFiles = 0;
    class FileCounter {
      int uniqueCount = 0;
      int totalCount = 0;
    }
    Map<String, FileCounter> projects = Helper.newHashMap();
    
    for (FileCluster cluster : files) {
      totalFiles += cluster.getFileCount();
      projectUniqueFiles += cluster.getProjectCount();
      if (cluster.getProjectCount() == 1) {
        singletonFiles++;
        for (String project : cluster.getProjects()) {
          FileCounter counter = projects.get(project);
          if (counter == null) {
            counter = new FileCounter();
            projects.put(project, counter);
          }
          counter.uniqueCount++;
          counter.totalCount++;
        }
      } else {
        uniqueDuplicateFiles++;
        totalDuplicateFiles += cluster.getProjectCount();
        for (String project : cluster.getProjects()) {
          FileCounter counter = projects.get(project);
          if (counter == null) {
            counter = new FileCounter();
            projects.put(project, counter);
          }
          counter.totalCount++;
        }
      }
    }
    
    double runningDupRate = 0;
    for (FileCounter counter : projects.values()) {
      runningDupRate += (double) (counter.totalCount - counter.uniqueCount) / counter.totalCount;
    }
    return new MatchingStatistics(totalFiles, projects.size(), projectUniqueFiles, singletonFiles, globalUniqueFiles, uniqueDuplicateFiles, totalDuplicateFiles, runningDupRate / projects.size());
  }

  @Override
  public Iterator<FileCluster> iterator() {
    return files.iterator();
  }
}
