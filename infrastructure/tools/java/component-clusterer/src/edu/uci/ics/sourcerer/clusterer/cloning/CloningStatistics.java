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
import java.util.Collection;
import java.util.Map;
import java.util.logging.Level;

import edu.uci.ics.sourcerer.clusterer.cloning.basic.Confidence;
import edu.uci.ics.sourcerer.clusterer.cloning.basic.File;
import edu.uci.ics.sourcerer.clusterer.cloning.basic.Project;
import edu.uci.ics.sourcerer.clusterer.cloning.basic.ProjectMap;
import edu.uci.ics.sourcerer.clusterer.cloning.method.fingerprint.FingerprintClusterer;
import edu.uci.ics.sourcerer.clusterer.cloning.method.fqn.FqnClusterer;
import edu.uci.ics.sourcerer.clusterer.cloning.method.hash.HashingClusterer;
import edu.uci.ics.sourcerer.clusterer.cloning.pairwise.FileMatching;
import edu.uci.ics.sourcerer.clusterer.cloning.pairwise.ProjectMatch;
import edu.uci.ics.sourcerer.clusterer.cloning.pairwise.ProjectMatchSet;
import edu.uci.ics.sourcerer.util.Helper;
import edu.uci.ics.sourcerer.util.io.FileUtils;
import edu.uci.ics.sourcerer.util.io.Properties;
import edu.uci.ics.sourcerer.util.io.Property;
import edu.uci.ics.sourcerer.util.io.TablePrettyPrinter;
import edu.uci.ics.sourcerer.util.io.properties.BooleanProperty;
import edu.uci.ics.sourcerer.util.io.properties.StringProperty;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class CloningStatistics {
  public static Property<Boolean> COMPARE_FILE_SETS = new BooleanProperty("compare-file-sets", false, "Compare the sets of files analyzed by each clustering method.");
  public static Property<String> FILE_SET_COMPARISON_FILE = new StringProperty("file-set-comparison-file", "file-set-comparison.txt", "");
  
  public static Property<Boolean> COMPUTE_CLONING_STATS = new BooleanProperty("compute-cloning-stats", true, "Computes the clong rates for each method.");
  public static Property<String> CLONING_STATS_FILE = new StringProperty("cloning-stats-file", "cloning-stats.txt", "");
  
  public static Property<Boolean> COMPUTE_PROJECT_MATCHING = new BooleanProperty("compute-project-matching", true, "Computes the project-project matching rates.");
  public static Property<String> PROJECT_MATCHING_STATS_FILE = new StringProperty("project-matching-stats-file", "project-matching-stats.txt", "");
  public static Property<String> PROJECT_MATCHING_FILE = new StringProperty("project-matching-file", "project-matching.txt", "");
  public static Property<String> AUGMENTED_MATCHING_FILE = new StringProperty("augmented-matching-file", "augmented-matching.txt", "");
  public static Property<String> LONELY_FQNS_FILE = new StringProperty("lonely-fqns-file", "lonely-fqns.txt", "");
  
  private static void compareFileSets(ProjectMap projects) {
    if (COMPARE_FILE_SETS.getValue()) {
      int missingHash = 0;
      int missingFqn = 0;
      int missingFingerprint = 0;
      logger.info("Comparing file sets...");
      BufferedWriter bw = null;
      try {
        bw = FileUtils.getBufferedWriter(FILE_SET_COMPARISON_FILE);
      
        for (Project project : projects.getProjects()) {
          for (File file : project.getFiles()) {
            if (!file.hasHashKey()) {
              missingHash++;
              bw.write("Missing hash: " + file);
              bw.newLine();
            }
            if (!file.hasFqnKey()) {
              missingFqn++;
              bw.write("Missing fqn: " + file);
              bw.newLine();
            }
            if (!file.hasFingerprintKey()) {
              missingFingerprint++;
              bw.write("Missing fingerprint: " + file);
              bw.newLine();
            }
          }
        }
      
        bw.write(missingHash + " files had no hash.");
        bw.newLine();
        bw.write(missingFqn + " files had no fqn.");
        bw.newLine();
        bw.write(missingFingerprint + " files had no fingerprint.");
      } catch (IOException e) {
        logger.log(Level.SEVERE, "Error writing file.", e);
      } finally {
        FileUtils.close(bw);
      }
    }
  }
  
  private static void computeCloningStatistics(ProjectMap projects) {
    if (COMPUTE_CLONING_STATS.getValue()) {
      logger.info("Computing basic cloning statistics...");
      TablePrettyPrinter printer = TablePrettyPrinter.getTablePrettyPrinter(CLONING_STATS_FILE);
      logger.info("  Computing high confidence...");
      {
        int totalFiles = 0;
        int uniqueHashFiles = 0;
        int uniqueFqnFiles = 0;
        int uniqueFingerprintFiles = 0;
        for (Project project : projects.getProjects()) {
          for (File file : project.getFiles()) {
            // Exclude all files that don't match
            if (file.hasAllKeys()) {
              if (++totalFiles % 100000 == 0) {
                logger.info("    " + totalFiles + " analyzed");
                logger.log(Level.WARNING, "Free mem: " + Runtime.getRuntime().freeMemory());
              }
              if (file.getHashKey().isUnique(Confidence.HIGH)) {
                uniqueHashFiles++;
              }
              if (file.getFqnKey().isUnique(Confidence.HIGH)) {
                uniqueFqnFiles++;
              }
              if (file.getFingerprintKey().isUnique(Confidence.HIGH)) {
                uniqueFingerprintFiles++;
              }
            }
          }
        }
        logger.info("  " + totalFiles + " analyzed");
        
        printer.beginTable(4);
        printer.addHeader("High Confidence");
        printer.addDividerRow();
        printer.addRow("", "Hash", "FQN", "Fingerprint");
        printer.addDividerRow();
        printer.beginRow();
        printer.addCell("Total Files");
        printer.addCell(totalFiles);
        printer.addCell(totalFiles);
        printer.addCell(totalFiles);
        printer.beginRow();
        printer.addCell("Unique Files");
        printer.addCell(uniqueHashFiles);
        printer.addCell(uniqueFqnFiles);
        printer.addCell(uniqueFingerprintFiles);
        printer.beginRow();
        printer.addCell("Duplicated Files");
        printer.addCell(totalFiles - uniqueHashFiles);
        printer.addCell(totalFiles - uniqueFqnFiles);
        printer.addCell(totalFiles - uniqueFingerprintFiles);
        printer.beginRow();
        printer.addCell("Cloning Rate");
        printer.addCell(((double)(totalFiles - uniqueHashFiles) / totalFiles));
        printer.addCell(((double)(totalFiles - uniqueFqnFiles) / totalFiles));
        printer.addCell(((double)(totalFiles - uniqueFingerprintFiles) / totalFiles));
        printer.addDividerRow();
        printer.endTable();
      }
      
      logger.info("  Computing medium confidence...");
      {
        int totalFiles = 0;
        int uniqueHashFiles = 0;
        int uniqueFqnFiles = 0;
        int uniqueFingerprintFiles = 0;
        for (Project project : projects.getProjects()) {
          for (File file : project.getFiles()) {
            // Exclude all files that don't match
            if (file.hasAllKeys()) {
              if (++totalFiles % 100000 == 0) {
                logger.info("    " + totalFiles + " analyzed");
                logger.log(Level.WARNING, "Free mem: " + Runtime.getRuntime().freeMemory());
              }
              if (file.getHashKey().isUnique(Confidence.MEDIUM)) {
                uniqueHashFiles++;
              }
              if (file.getFqnKey().isUnique(Confidence.MEDIUM)) {
                uniqueFqnFiles++;
              }
              if (file.getFingerprintKey().isUnique(Confidence.MEDIUM)) {
                uniqueFingerprintFiles++;
              }
            }
          }
        }
        logger.info("  " + totalFiles + " analyzed");
        
        printer.beginTable(4);
        printer.addHeader("Medium Confidence");
        printer.addDividerRow();
        printer.addRow("", "Hash", "FQN", "Fingerprint");
        printer.addDividerRow();
        printer.beginRow();
        printer.addCell("Total Files");
        printer.addCell(totalFiles);
        printer.addCell(totalFiles);
        printer.addCell(totalFiles);
        printer.beginRow();
        printer.addCell("Unique Files");
        printer.addCell(uniqueHashFiles);
        printer.addCell(uniqueFqnFiles);
        printer.addCell(uniqueFingerprintFiles);
        printer.beginRow();
        printer.addCell("Duplicated Files");
        printer.addCell(totalFiles - uniqueHashFiles);
        printer.addCell(totalFiles - uniqueFqnFiles);
        printer.addCell(totalFiles - uniqueFingerprintFiles);
        printer.beginRow();
        printer.addCell("Cloning Rate");
        printer.addCell(((double)(totalFiles - uniqueHashFiles) / totalFiles));
        printer.addCell(((double)(totalFiles - uniqueFqnFiles) / totalFiles));
        printer.addCell(((double)(totalFiles - uniqueFingerprintFiles) / totalFiles));
        printer.addDividerRow();
        printer.endTable();
      }
      
      logger.info("  Computing low confidence...");
      {
        int totalFiles = 0;
        int uniqueHashFiles = 0;
        int uniqueFqnFiles = 0;
        int uniqueFingerprintFiles = 0;
        for (Project project : projects.getProjects()) {
          for (File file : project.getFiles()) {
            // Exclude all files that don't match
            if (file.hasAllKeys()) {
              if (++totalFiles % 100000 == 0) {
                logger.info("    " + totalFiles + " analyzed");
                logger.log(Level.WARNING, "Free mem: " + Runtime.getRuntime().freeMemory());                
              }
              if (file.getHashKey().isUnique(Confidence.LOW)) {
                uniqueHashFiles++;
              }
              if (file.getFqnKey().isUnique(Confidence.LOW)) {
                uniqueFqnFiles++;
              }
              if (file.getFingerprintKey().isUnique(Confidence.LOW)) {
                uniqueFingerprintFiles++;
              }
            }
          }
        }
        logger.info("  " + totalFiles + " analyzed");
        
        printer.beginTable(4);
        printer.addHeader("Low Confidence");
        printer.addDividerRow();
        printer.addRow("", "Hash", "FQN", "Fingerprint");
        printer.addDividerRow();
        printer.beginRow();
        printer.addCell("Total Files");
        printer.addCell(totalFiles);
        printer.addCell(totalFiles);
        printer.addCell(totalFiles);
        printer.beginRow();
        printer.addCell("Unique Files");
        printer.addCell(uniqueHashFiles);
        printer.addCell(uniqueFqnFiles);
        printer.addCell(uniqueFingerprintFiles);
        printer.beginRow();
        printer.addCell("Duplicated Files");
        printer.addCell(totalFiles - uniqueHashFiles);
        printer.addCell(totalFiles - uniqueFqnFiles);
        printer.addCell(totalFiles - uniqueFingerprintFiles);
        printer.beginRow();
        printer.addCell("Cloning Rate");
        printer.addCell(((double)(totalFiles - uniqueHashFiles) / totalFiles));
        printer.addCell(((double)(totalFiles - uniqueFqnFiles) / totalFiles));
        printer.addCell(((double)(totalFiles - uniqueFingerprintFiles) / totalFiles));
        printer.addDividerRow();
        printer.endTable();
      }
      printer.close();
    }
  }
  
  private static void computeProjectMatching(ProjectMap projects) {
    if (COMPUTE_PROJECT_MATCHING.getValue()) {
      logger.info("Computing project matching...");
      ProjectMatchSet matches = projects.getProjectMatchSet();
      int total = 0;
      int withClones = 0;
      for (Map.Entry<Project, ProjectMatch> entry : matches.getProjectMatches()) {
        total++;
        Collection<Map.Entry<Project, FileMatching>> fileMatchings = entry.getValue().getFileMatchings();
        if (fileMatchings.size() > 0) {
          withClones++;
        }
      }
      
      TablePrettyPrinter printer = TablePrettyPrinter.getTablePrettyPrinter(PROJECT_MATCHING_STATS_FILE);
      printer.beginTable(2);
      printer.addDividerRow();
      printer.beginRow();
      printer.addCell("Total Projects");
      printer.addCell(total);
      printer.beginRow();
      printer.addCell("Projects with Clones");
      printer.addCell(withClones);
      printer.addDividerRow();
      printer.endTable();
      
//      BufferedWriter matchWriter = null;
//      BufferedWriter statsWriter = null;
//      BufferedWriter augmentedWriter = null;
//      BufferedWriter lonelyWriter = null;
//      
//      try {
//        matchWriter = FileUtils.getBufferedWriter(PROJECT_MATCHING_FILE);
//        statsWriter = FileUtils.getBufferedWriter(PROJECT_MATCHING_STATS_FILE);
//        augmentedWriter = FileUtils.getBufferedWriter(AUGMENTED_MATCHING_FILE);
//        lonelyWriter = FileUtils.getBufferedWriter(LONELY_FQNS_FILE);
//        
//        Collection<String> lonelyFqns = Helper.newHashSet();
//        
//        int total = 0;
//        int withClones = 0;
//        int pureShared = 0;
//        int pureFqn = 0;
//        int pureAugmented = 0;
//        int other = 0;
//        for (Map.Entry<Project, ProjectMatch> entry : matches.getProjectMatches()) {
//          total++;
//          Collection<Map.Entry<Project, FileMatch>> fileMatches = entry.getValue().getFileMatches();
//          if (fileMatches.size() > 0) {
//            withClones++;
//            matchWriter.write("Project " + entry.getKey() + " matches the following projects:");
//            matchWriter.newLine();
//            boolean pureSharedB = true;
//            boolean pureFqnB = true;
//            boolean pureAugmentedB = true;
//            for (Map.Entry<Project, FileMatch> fileEntry : fileMatches) {
//              FileMatch fileMatch = fileEntry.getValue();
//              matchWriter.write("  Project " + fileEntry.getKey() + " " + fileMatch.getSharedCount() + " " + fileMatch.getUniqueHashCount() + " " + fileMatch.getUniqueFqnCount());
//              matchWriter.newLine();
//              
//              if (fileMatch.getSharedCount() > 0 && fileMatch.getUniqueFqnCount() > 0) {
//                pureSharedB = false;
//                pureFqnB = false;
//              } else if (fileMatch.getSharedCount() > 0) {
//                pureAugmentedB = false;
//                pureFqnB = false;
//              } else if (fileMatch.getUniqueFqnCount() > 0) {
//                pureAugmentedB = false;
//                pureSharedB = false;
//              }
//              if (fileMatch.getSharedCount() == 0 && fileMatch.getUniqueFqnCount() < 5) {
//                for (Map.Entry<File, MatchStatus> statusEntry : fileMatch.getMatchStatusSet()) {
//                  if (statusEntry.getValue().fqn) {
//                    lonelyFqns.add(statusEntry.getKey().getFqnKey().getKey());
//                  }
//                }
//              }
//              
//              if (fileMatch.getSharedCount() > 5 && fileMatch.getUniqueFqnCount() > 0) {
//                augmentedWriter.write(entry.getKey() + "/" + fileEntry.getKey() + " matching augmented");
//                for (Map.Entry<File, MatchStatus> statusEntry : fileMatch.getMatchStatusSet()) {
//                  if (statusEntry.getValue().hash) {
//                    augmentedWriter.write("    " + statusEntry.getKey().getFqnKey().getKey());
//                    augmentedWriter.newLine();
//                  } else if (statusEntry.getValue().fqn) {
//                    augmentedWriter.write("  + " + statusEntry.getKey().getFqnKey().getKey());
//                    augmentedWriter.newLine();
//                  }
//                }
//              }
//            }
//            if (pureSharedB) {
//              pureShared++;
//            } else if (pureFqnB) {
//              pureFqn++;
//            } else if (pureAugmentedB) {
//              pureAugmented++;
//            } else {
//              other++;
//            }
//          }
//        }
//        
//        for (String fqn : lonelyFqns) {
//          lonelyWriter.write(fqn);
//          lonelyWriter.newLine();
//        }
//        
//        statsWriter.write("Total projects: " + total);
//        statsWriter.newLine();
//        statsWriter.write("Projects with clones: " + withClones);
//        statsWriter.newLine();
//        statsWriter.write("Pure shared: " + pureShared);
//        statsWriter.newLine();
//        statsWriter.write("Pure fqn: " + pureFqn);
//        statsWriter.newLine();
//        statsWriter.write("Pure augmented: " + pureAugmented);
//        statsWriter.newLine();
//        statsWriter.write("Mixed: " + other);
//        statsWriter.newLine();
//      } catch (IOException e) {
//        logger.log(Level.SEVERE, "Error opening lonely fqns file.", e);
//      } finally {
//        FileUtils.close(matchWriter);
//        FileUtils.close(statsWriter);
//        FileUtils.close(augmentedWriter);
//        FileUtils.close(lonelyWriter);
//      }
    }
  }

  public static void performAnalysis() {
    ProjectMap projects = new ProjectMap();
    logger.log(Level.WARNING, "Free mem: " + Runtime.getRuntime().freeMemory());
    HashingClusterer.loadFileListing(projects);
    logger.log(Level.WARNING, "Free mem: " + Runtime.getRuntime().freeMemory());
    FqnClusterer.loadFileListing(projects);
    logger.log(Level.WARNING, "Free mem: " + Runtime.getRuntime().freeMemory());
    FingerprintClusterer.loadFileListing(projects);

    logger.log(Level.WARNING, "Free mem: " + Runtime.getRuntime().freeMemory());
    compareFileSets(projects);
    logger.log(Level.WARNING, "Free mem: " + Runtime.getRuntime().freeMemory());
    computeCloningStatistics(projects);
    computeProjectMatching(projects);
    
    logger.info("Done!");
  }
}
