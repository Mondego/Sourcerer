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
package edu.uci.ics.sourcerer.tools.java.cloning.stats;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class MatchingStatistics {
  private final int totalFiles;
  private final int totalProjects;
  private final int projectUniqueFiles;
  private final int singletonFiles;
  private final int globalUniqueFiles;
  private final int uniqueDuplicateFiles;
  private final int totalDuplicateFiles;
  private final double projectDupRate;
  
  public MatchingStatistics(int totalFiles, int totalProjects, int projectUniqueFiles, int singletonFiles, int globalUniqueFiles, int uniqueDuplicateFiles, int totalDuplicateFiles, double projectDupRate) {
    this.totalFiles = totalFiles;
    this.totalProjects = totalProjects;
    this.projectUniqueFiles = projectUniqueFiles;
    this.singletonFiles = singletonFiles;
    this.globalUniqueFiles = globalUniqueFiles;
    this.uniqueDuplicateFiles = uniqueDuplicateFiles;
    this.totalDuplicateFiles = totalDuplicateFiles;
    this.projectDupRate = projectDupRate;
  }

  public int getTotalFiles() {
    return totalFiles;
  }
  
  public int getTotalProjects() {
    return totalProjects;
  }

  public int getProjectUniqueFiles() {
    return projectUniqueFiles;
  }

  public int getSingletonFiles() {
    return singletonFiles;
  }

  public int getGlobalUniqueFiles() {
    return globalUniqueFiles;
  }

  public int getUniqueDuplicateFiles() {
    return uniqueDuplicateFiles;
  }

  public int getTotalDuplicateFiles() {
    return totalDuplicateFiles;
  }
  
  public double getProjectDupRate() {
    return projectDupRate;
  }
  
  public double getDupRate() {
    return (double) totalDuplicateFiles / projectUniqueFiles;
  }
  
  public double getDupOccuranceRate() {
    return (double) totalDuplicateFiles / uniqueDuplicateFiles;
  }
}
