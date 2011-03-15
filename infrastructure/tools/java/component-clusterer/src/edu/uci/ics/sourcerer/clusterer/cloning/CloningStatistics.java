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
package edu.uci.ics.sourcerer.clusterer.cloning;

import static edu.uci.ics.sourcerer.util.io.Logging.logger;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;

import edu.uci.ics.sourcerer.clusterer.cloning.method.fqn.FqnClusterer;
import edu.uci.ics.sourcerer.clusterer.cloning.method.hash.HashingClusterer;
import edu.uci.ics.sourcerer.util.Helper;
import edu.uci.ics.sourcerer.util.io.FileUtils;
import edu.uci.ics.sourcerer.util.io.Property;
import edu.uci.ics.sourcerer.util.io.properties.BooleanProperty;
import edu.uci.ics.sourcerer.util.io.properties.StringProperty;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class CloningStatistics {
  public static Property<Boolean> COMPARE_FILE_SETS = new BooleanProperty("compare-file-sets", false, "Compare the sets of files analyzed by each clustering method.");
  public static Property<Boolean> COMPUTE_CLONING_STATS = new BooleanProperty("compute-cloning-stats", true, "Computes the clong rates for each method.");
  public static Property<Boolean> COMPUTE_PROJECT_MATCHING = new BooleanProperty("compute-project-matching", true, "Computes the project-project matching rates.");
  public static Property<String> LONELY_FQNS_FILE = new StringProperty("lonely-fqns-file", "lonely-fqns.txt", "");
  
  private static void compareFileSets(ProjectMap projects) {
    if (COMPARE_FILE_SETS.getValue()) {
      int missingHash = 0;
      int missingFqn = 0;
      logger.info("Comparing file sets...");
      for (Project project : projects.getProjects()) {
        for (File file : project.getFiles()) {
          if (!file.hasHashKey()) {
            missingHash++;
            logger.info("  Missing hash: " + file);
          }
          if (!file.hasFqnKey()) {
            missingFqn++;
            logger.info("  Missing fqn: " + file);
          }
        }
      }
      logger.info(missingHash + " files had no hash.");
      logger.info(missingFqn + " files has no fqn.");
      logger.info("---");
    }
  }
  
  private static void computeCloningStatistics(ProjectMap projects) {
    if (COMPUTE_CLONING_STATS.getValue()) {
      logger.info("Computing basic cloning statistics...");
      int totalFiles = 0;
      int uniqueHashFiles = 0;
      int uniqueFqnFiles = 0;
      for (Project project : projects.getProjects()) {
        for (File file : project.getFiles()) {
          // Exclude all files that don't match
          if (file.hasAllKeys()) {
            totalFiles++;
            if (file.getHashKey().isUnique()) {
              uniqueHashFiles++;
            }
            if (file.getFqnKey().isUnique()) {
              uniqueFqnFiles++;
            }
          }
        }
      }
      logger.info("  Non-empty files: " + totalFiles);
      logger.info("    Unique hash files: " + uniqueHashFiles);
      logger.info("    Duplicated hash files: " + (totalFiles - uniqueHashFiles));
      logger.info("    Hash cloning rate: " + ((double)(totalFiles - uniqueHashFiles) / totalFiles));
      logger.info("  ---");
      logger.info("    Unique fqn files: " + uniqueFqnFiles);
      logger.info("    Duplicated fqn files: " + (totalFiles - uniqueFqnFiles));
      logger.info("    Fqn cloning rate: " + ((double)(totalFiles - uniqueFqnFiles) / totalFiles));
      logger.info("---");
    }
  }
  
  private static void computeProjectMatching(ProjectMap projects) {
    if (COMPUTE_PROJECT_MATCHING.getValue()) {
      
      try {
        final BufferedWriter bw = FileUtils.getBufferedWriter(LONELY_FQNS_FILE);
        try {
          class ProjectMap {
            class FileStatusMap {
              class MatchStatus {
                boolean hash = false;
                boolean fqn = false;
              }
              
              Map<File, MatchStatus> map = Helper.newHashMap();
              
              private MatchStatus getStatus(File file) {
                MatchStatus status = map.get(file);
                if (status == null) {
                  status = new MatchStatus();
                  map.put(file, status);
                }
                return status;
              }
              
              public void addHashFile(File file) {
                getStatus(file).hash = true;
              }
              
              public void addFqnFile(File file) {
                getStatus(file).fqn = true;
              }
              
              public String print() {
                int sharedCount = 0;
                int hash = 0;
                int fqn = 0;
                for (MatchStatus status : map.values()) {
                  if (status.hash && status.fqn) {
                    sharedCount++;
                  }
                  if (status.hash) {
                    hash++;
                  }
                  if (status.fqn) {
                    fqn++;
                  }
                }
                if (sharedCount == 0 && fqn == 1) {
                  for (Map.Entry<File, MatchStatus> entry : map.entrySet()) {
                    if (entry.getValue().fqn) {
                      try {
                        bw.write(entry.getKey().toString());
                        bw.newLine();
                      } catch (IOException e) {
                        logger.log(Level.SEVERE, "Error writing to lonely fqns file.", e);
                      }
                    }
                  }
                }
                return sharedCount + " " + hash + " " + fqn;
              }
            }
      
            Map<Project, FileStatusMap> map = Helper.newHashMap();
            
            private FileStatusMap getFileStatusMap(Project project) {
              FileStatusMap fMap = map.get(project);
              if (fMap == null) {
                fMap = new FileStatusMap();
                map.put(project, fMap);
              }
              return fMap;
            }
            
            public void addHashFile(File file) {
              getFileStatusMap(file.getProject()).addHashFile(file);
            }
            
            public void addFqnFile(File file) {
              getFileStatusMap(file.getProject()).addFqnFile(file);
            }
            
            public void print(Project project) {
              if (map.size() > 0) {
                logger.info("  Project " + project + " matches the following projects:");
                for (Map.Entry<Project, FileStatusMap> entry : map.entrySet()) {
                  logger.info("    " + entry.getKey() + " " + entry.getValue().print());
                }
                map.clear();
              }
            }
          }
        
      
          logger.info("Computing project matching...");
      
          ProjectMap map = new ProjectMap();
          for (Project project : projects.getProjects()) {
            for (File file : project.getFiles()) {
              if (file.hasAllKeys()) {
                for (File otherFile : file.getHashKey().getFiles()) {
                  if (otherFile.getProject() != project) {
                    map.addHashFile(otherFile);
                  }
                }
                for (File otherFile : file.getFqnKey().getFiles()) {
                  if (otherFile.getProject() != project) {
                    map.addFqnFile(otherFile);
                  }
                }
                map.print(project);
              }
            }
          }
        } finally {
          FileUtils.close(bw);
        }
      } catch (IOException e) {
        logger.log(Level.SEVERE, "Error opening lonely fqns file.", e);
      }
      logger.info("---");
    }
  }

  public static void performAnalysis() {
    ProjectMap projects = new ProjectMap();
    HashingClusterer.loadFileListing(projects);
    FqnClusterer.loadFileListing(projects);
    
    compareFileSets(projects);
    computeCloningStatistics(projects);
    computeProjectMatching(projects);
  }
}
