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

import static edu.uci.ics.sourcerer.util.io.logging.Logging.logger;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.logging.Level;

import edu.uci.ics.sourcerer.repo.base.Repository;
import edu.uci.ics.sourcerer.repo.general.AbstractRepository;
import edu.uci.ics.sourcerer.tools.java.cloning.method.Confidence;
import edu.uci.ics.sourcerer.tools.java.cloning.method.DetectionMethod;
import edu.uci.ics.sourcerer.tools.java.cloning.method.File;
import edu.uci.ics.sourcerer.tools.java.cloning.method.KeyMatch;
import edu.uci.ics.sourcerer.tools.java.cloning.method.Project;
import edu.uci.ics.sourcerer.tools.java.cloning.method.ProjectMap;
import edu.uci.ics.sourcerer.tools.java.cloning.method.combination.CombinedClusterer;
import edu.uci.ics.sourcerer.tools.java.cloning.method.dir.DirectoryClusterer;
import edu.uci.ics.sourcerer.tools.java.cloning.method.fingerprint.FingerprintClusterer;
import edu.uci.ics.sourcerer.tools.java.cloning.method.fqn.FqnClusterer;
import edu.uci.ics.sourcerer.tools.java.cloning.method.hash.HashingClusterer;
import edu.uci.ics.sourcerer.tools.java.cloning.pairwise.MatchStatus;
import edu.uci.ics.sourcerer.tools.java.cloning.pairwise.MatchingProjects;
import edu.uci.ics.sourcerer.tools.java.cloning.pairwise.ProjectMatchSet;
import edu.uci.ics.sourcerer.tools.java.cloning.pairwise.ProjectMatches;
import edu.uci.ics.sourcerer.util.Averager;
import edu.uci.ics.sourcerer.util.ComparableObject;
import edu.uci.ics.sourcerer.util.Helper;
import edu.uci.ics.sourcerer.util.WeightedAverager;
import edu.uci.ics.sourcerer.util.io.FileUtils;
import edu.uci.ics.sourcerer.util.io.TablePrettyPrinter;
import edu.uci.ics.sourcerer.util.io.TablePrettyPrinter.Alignment;
import edu.uci.ics.sourcerer.util.io.arguments.Argument;
import edu.uci.ics.sourcerer.util.io.arguments.BooleanArgument;
import edu.uci.ics.sourcerer.util.io.arguments.IOFileArgumentFactory;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class CloningStatistics {
  public static Argument<Boolean> COMPARE_FILE_SETS = new BooleanArgument("compare-file-sets", false, "Compare the sets of files analyzed by each clustering method.");
  public static IOFileArgumentFactory FILE_SET_COMPARISON_FILE = new IOFileArgumentFactory("file-set-comparison-file", "file-set-comparison.txt", "");

  public static Argument<Boolean> PRINT_CLONE_PAIRS = new BooleanArgument("compute-randomized-clone-pairs", true, "Prints out the clone pairings in a randomized order");
  public static IOFileArgumentFactory HIGH_CONFIDENCE_CLONE_PAIRS_FILE = new IOFileArgumentFactory("high-confidence-clone-pairs-file", "high-confidence-clone-pairs.txt", "");
  public static IOFileArgumentFactory HIGH_CONFIDENCE_FQN_CLONE_PAIRS_FILE = new IOFileArgumentFactory("high-confidence-fqn-clone-pairs-file", "high-confidence-fqn-clone-pairs.txt", "");
  public static IOFileArgumentFactory HIGH_CONFIDENCE_FINGERPRINT_CLONE_PAIRS_FILE = new IOFileArgumentFactory("high-confidence-fingerprint-clone-pairs-file", "high-confidence-fingerprint-clone-pairs.txt", "");
  
  public static Argument<Boolean> COMPUTE_CLONING_STATS = new BooleanArgument("compute-cloning-stats", true, "Computes the clong rates for each method.");
  public static IOFileArgumentFactory CLONING_STATS_FILE = new IOFileArgumentFactory("cloning-stats-file", "cloning-stats.txt", "");
  
  public static Argument<Boolean> COMPUTE_PROJECT_MATCHING = new BooleanArgument("compute-project-matching", true, "Computes the project-project matching rates.");
  public static IOFileArgumentFactory PROJECT_MATCHING_STATS_FILE = new IOFileArgumentFactory("project-matching-stats-file", "project-matching-stats.txt", "");
  public static IOFileArgumentFactory HIGHEST_PERCENT_CLONES_FILE = new IOFileArgumentFactory("highest-percent-clones-file", "highest-percent-clones.txt", "");
  public static IOFileArgumentFactory LARGEST_CLONE_SIZE_FILE = new IOFileArgumentFactory("largest-clone-size-file", "largest-clone-size.txt", "");
  public static IOFileArgumentFactory LARGEST_CLONED_PROJECTS_FILE = new IOFileArgumentFactory("largest-cloned-projects-file", "largest-cloned-projects.txt", "");
  
  public static IOFileArgumentFactory PROJECT_CLONE_RATES_FILE = new IOFileArgumentFactory("project-clone-rates-file", "project-clone-rates.txt", "");
  
  public static IOFileArgumentFactory AUGMENTED_MATCHING_FILE = new IOFileArgumentFactory("augmented-matching-file", "augmented-matching.txt", "");
  public static IOFileArgumentFactory LONELY_FQNS_FILE = new IOFileArgumentFactory("lonely-fqns-file", "lonely-fqns.txt", "");
  
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
  
  private static void printClonePairs(ProjectMap projects) {
    if (PRINT_CLONE_PAIRS.getValue()) {
      logger.info("Printing randomized clone pairs");
      ProjectMatchSet matches = projects.getProjectMatchSet();
      Repository repo = Repository.getRepository(AbstractRepository.INPUT_REPO.getValue()); 
      BufferedWriter high = null;
      try {
        high = FileUtils.getBufferedWriter(HIGH_CONFIDENCE_CLONE_PAIRS_FILE);
        for (Project project : projects.getProjects()) {
          for (File file : project.getFiles()) {
            if (file.hasAllKeys()) {
              for (KeyMatch match : file.getCombinedKey().getMatches()) {
                if (match.getConfidence() == Confidence.HIGH) {
                  boolean isHash = matches.getFileMatch(project, match.getFile().getProject()).getMatchStatus(match.getFile()).get(DetectionMethod.HASH) == Confidence.HIGH;
                  high.write((isHash ? "H: " : "") + repo.getFilePath(project.getName(), file.getPath()) + " " + repo.getFilePath(match.getFile().getProject().getName(), match.getFile().getPath()));
                  high.newLine();
                }
              }
            }
          }
        }
      } catch (IOException e) {
        logger.log(Level.SEVERE, "Error writing file.", e);
      } finally {
        FileUtils.close(high);
      }
      
      try {
        high = FileUtils.getBufferedWriter(HIGH_CONFIDENCE_FQN_CLONE_PAIRS_FILE);
        for (Project project : projects.getProjects()) {
          for (File file : project.getFiles()) {
            if (file.hasAllKeys()) {
              for (KeyMatch match : file.getFqnKey().getMatches()) {
                if (file != match.getFile()) {
                  if (match.getConfidence() == Confidence.HIGH) {
                    MatchingProjects a = matches.getFileMatch(project, match.getFile().getProject());
                    boolean isHash = a == null ? false : a.getMatchStatus(match.getFile()).get(DetectionMethod.HASH) == Confidence.HIGH;
                    high.write((isHash ? "H: " : "") + repo.getFilePath(project.getName(), file.getPath()) + " " + repo.getFilePath(match.getFile().getProject().getName(), match.getFile().getPath()));
                    high.newLine();
                  }
                }
              }
            }
          }
        }
      } catch (IOException e) {
        logger.log(Level.SEVERE, "Error writing file.", e);
      } finally {
        FileUtils.close(high);
      }
      
      try {
        high = FileUtils.getBufferedWriter(HIGH_CONFIDENCE_FINGERPRINT_CLONE_PAIRS_FILE);
        for (Project project : projects.getProjects()) {
          for (File file : project.getFiles()) {
            if (file.hasAllKeys()) {
              for (KeyMatch match : file.getFingerprintKey().getMatches()) {
                if (file != match.getFile()) {
                  if (match.getConfidence() == Confidence.HIGH) {
                    MatchingProjects a = matches.getFileMatch(project, match.getFile().getProject());
                    boolean isHash = a == null ? false : a.getMatchStatus(match.getFile()).get(DetectionMethod.HASH) == Confidence.HIGH;
                    high.write((isHash ? "H: " : "") + repo.getFilePath(project.getName(), file.getPath()) + " " + repo.getFilePath(match.getFile().getProject().getName(), match.getFile().getPath()));
                    high.newLine();
                  }
                }
              }
            }
          }
        }
      } catch (IOException e) {
        logger.log(Level.SEVERE, "Error writing file.", e);
      } finally {
        FileUtils.close(high);
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
        int uniqueCombinedFiles = 0;
        int uniqueDirFiles = 0;
        int dirMissing = 0;
        
        BufferedWriter bw = null;
        try {
          bw = FileUtils.getBufferedWriter(PROJECT_CLONE_RATES_FILE);
          for (Project project : projects.getProjects()) {
            int projHashFiles = 0;
            int projFqnFiles = 0;
            int projFingerprintFiles = 0;
            int projCombinedFiles = 0;
            int projDirFiles = 0;
            int projSize = 0;
            for (File file : project.getFiles()) {
              // Exclude all files that don't match
              if (file.hasAllKeys()) {
                projSize++;
                if (++totalFiles % 100000 == 0) {
                  logger.info("    " + totalFiles + " analyzed");
                }
                if (file.getHashKey().isUnique(Confidence.HIGH)) {
                  projHashFiles++;
                }
                if (file.getFqnKey().isUnique(Confidence.HIGH)) {
                  projFqnFiles++;
                }
                if (file.getFingerprintKey().isUnique(Confidence.HIGH)) {
                  projFingerprintFiles++;
                }
                if (file.getCombinedKey().isUnique(Confidence.HIGH)) {
                  projCombinedFiles++;
                }
                if (file.hasDirKey()) {
                  if (file.getDirKey().isUnique(Confidence.HIGH)) {
                    projDirFiles++;
                  }
                } else {
                  dirMissing++;
                  logger.log(Level.SEVERE, "Dir missing: " + file.getProject().getName() + " " + file.getPath());
                }
              }
            }
            bw.write(project.getName() + " " + projSize + " " + projHashFiles + " " + projFqnFiles + " " + projFingerprintFiles + " " + projCombinedFiles + " " + projDirFiles);
            bw.newLine();
            uniqueHashFiles += projHashFiles;
            uniqueFqnFiles += projFqnFiles;
            uniqueFingerprintFiles += projFingerprintFiles;
            uniqueCombinedFiles += projCombinedFiles;
            uniqueDirFiles += projDirFiles;
          }
          logger.info("  " + totalFiles + " analyzed");
          logger.info("Dir missing " + dirMissing);
        } catch (IOException e) {
          logger.log(Level.SEVERE, "Error writing file");
        } finally {
          FileUtils.close(bw);
        }
        
        printer.beginTable(6);
        printer.addHeader("High Confidence");
        printer.addDividerRow();
        printer.addRow("", "Hash", "FQN", "Fingerprint", "Combined", "Dir");
        printer.addDividerRow();
        printer.beginRow();
        printer.addCell("Total Files");
        printer.addCell(totalFiles);
        printer.addCell(totalFiles);
        printer.addCell(totalFiles);
        printer.addCell(totalFiles);
        printer.addCell(totalFiles);
        printer.beginRow();
        printer.addCell("Unique Files");
        printer.addCell(uniqueHashFiles);
        printer.addCell(uniqueFqnFiles);
        printer.addCell(uniqueFingerprintFiles);
        printer.addCell(uniqueCombinedFiles);
        printer.addCell(uniqueDirFiles);
        printer.beginRow();
        printer.addCell("Duplicated Files");
        printer.addCell(totalFiles - uniqueHashFiles);
        printer.addCell(totalFiles - uniqueFqnFiles);
        printer.addCell(totalFiles - uniqueFingerprintFiles);
        printer.addCell(totalFiles - uniqueCombinedFiles);
        printer.addCell(totalFiles - uniqueDirFiles);
        printer.beginRow();
        printer.addCell("Cloning Rate");
        printer.addCell(((double)(totalFiles - uniqueHashFiles) / totalFiles));
        printer.addCell(((double)(totalFiles - uniqueFqnFiles) / totalFiles));
        printer.addCell(((double)(totalFiles - uniqueFingerprintFiles) / totalFiles));
        printer.addCell(((double)(totalFiles - uniqueCombinedFiles) / totalFiles));
        printer.addCell(((double)(totalFiles - uniqueDirFiles) / totalFiles));
        printer.addDividerRow();
        printer.endTable();
      }
      
      logger.info("  Computing medium confidence...");
      {
        int totalFiles = 0;
        int uniqueHashFiles = 0;
        int uniqueFqnFiles = 0;
        int uniqueFingerprintFiles = 0;
        int uniqueCombinedFiles = 0;
        int uniqueDirFiles = 0;
        for (Project project : projects.getProjects()) {
          for (File file : project.getFiles()) {
            // Exclude all files that don't match
            if (file.hasAllKeys()) {
              if (++totalFiles % 100000 == 0) {
                logger.info("    " + totalFiles + " analyzed");
//                logger.log(Level.WARNING, "Free mem: " + Runtime.getRuntime().freeMemory());
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
              if (file.getCombinedKey().isUnique(Confidence.MEDIUM)) {
                uniqueCombinedFiles++;
              }
              if (file.hasDirKey()) {
                if (file.getDirKey().isUnique(Confidence.MEDIUM)) {
                  uniqueDirFiles++;
                }
              }
            }
          }
        }
        logger.info("  " + totalFiles + " analyzed");
        
        printer.beginTable(6);
        printer.addHeader("Medium Confidence");
        printer.addDividerRow();
        printer.addRow("", "Hash", "FQN", "Fingerprint", "Combined", "Dir");
        printer.addDividerRow();
        printer.beginRow();
        printer.addCell("Total Files");
        printer.addCell(totalFiles);
        printer.addCell(totalFiles);
        printer.addCell(totalFiles);
        printer.addCell(totalFiles);
        printer.addCell(totalFiles);
        printer.beginRow();
        printer.addCell("Unique Files");
        printer.addCell(uniqueHashFiles);
        printer.addCell(uniqueFqnFiles);
        printer.addCell(uniqueFingerprintFiles);
        printer.addCell(uniqueCombinedFiles);
        printer.addCell(uniqueDirFiles);
        printer.beginRow();
        printer.addCell("Duplicated Files");
        printer.addCell(totalFiles - uniqueHashFiles);
        printer.addCell(totalFiles - uniqueFqnFiles);
        printer.addCell(totalFiles - uniqueFingerprintFiles);
        printer.addCell(totalFiles - uniqueCombinedFiles);
        printer.addCell(totalFiles - uniqueDirFiles);
        printer.beginRow();
        printer.addCell("Cloning Rate");
        printer.addCell(((double)(totalFiles - uniqueHashFiles) / totalFiles));
        printer.addCell(((double)(totalFiles - uniqueFqnFiles) / totalFiles));
        printer.addCell(((double)(totalFiles - uniqueFingerprintFiles) / totalFiles));
        printer.addCell(((double)(totalFiles - uniqueCombinedFiles) / totalFiles));
        printer.addCell(((double)(totalFiles - uniqueDirFiles) / totalFiles));
        printer.addDividerRow();
        printer.endTable();
      }
      
      logger.info("  Computing low confidence...");
      {
        int totalFiles = 0;
        int uniqueHashFiles = 0;
        int uniqueFqnFiles = 0;
        int uniqueFingerprintFiles = 0;
        int uniqueCombinedFiles = 0;
        int uniqueDirFiles = 0;
        for (Project project : projects.getProjects()) {
          for (File file : project.getFiles()) {
            // Exclude all files that don't match
            if (file.hasAllKeys()) {
              if (++totalFiles % 100000 == 0) {
                logger.info("    " + totalFiles + " analyzed");
//                logger.log(Level.WARNING, "Free mem: " + Runtime.getRuntime().freeMemory());                
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
              if (file.getCombinedKey().isUnique(Confidence.LOW)) {
                uniqueCombinedFiles++;
              }
              if (file.hasDirKey()) {
                if (file.getDirKey().isUnique(Confidence.LOW)) {
                  uniqueDirFiles++;
                }
              }
            }
          }
        }
        logger.info("  " + totalFiles + " analyzed");
        
        printer.beginTable(6);
        printer.addHeader("Low Confidence");
        printer.addDividerRow();
        printer.addRow("", "Hash", "FQN", "Fingerprint", "Combined", "Dir");
        printer.addDividerRow();
        printer.beginRow();
        printer.addCell("Total Files");
        printer.addCell(totalFiles);
        printer.addCell(totalFiles);
        printer.addCell(totalFiles);
        printer.addCell(totalFiles);
        printer.addCell(totalFiles);
        printer.beginRow();
        printer.addCell("Unique Files");
        printer.addCell(uniqueHashFiles);
        printer.addCell(uniqueFqnFiles);
        printer.addCell(uniqueFingerprintFiles);
        printer.addCell(uniqueCombinedFiles);
        printer.addCell(uniqueDirFiles);
        printer.beginRow();
        printer.addCell("Duplicated Files");
        printer.addCell(totalFiles - uniqueHashFiles);
        printer.addCell(totalFiles - uniqueFqnFiles);
        printer.addCell(totalFiles - uniqueFingerprintFiles);
        printer.addCell(totalFiles - uniqueCombinedFiles);
        printer.addCell(totalFiles - uniqueDirFiles);
        printer.beginRow();
        printer.addCell("Cloning Rate");
        printer.addCell(((double)(totalFiles - uniqueHashFiles) / totalFiles));
        printer.addCell(((double)(totalFiles - uniqueFqnFiles) / totalFiles));
        printer.addCell(((double)(totalFiles - uniqueFingerprintFiles) / totalFiles));
        printer.addCell(((double)(totalFiles - uniqueCombinedFiles) / totalFiles));
        printer.addCell(((double)(totalFiles - uniqueDirFiles) / totalFiles));
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
      
      int totalProjects = matches.getProjectMatches().size();
      
      class Stats {
        int projectsWithClones = 0;
        Averager<Double> averagefilesClonedPerProject = new Averager<Double>();
        Averager<Integer> maxFilesClonedPerProject = new Averager<Integer>();
        Averager<Integer> totalFilesClonedPerProject = new Averager<Integer>();
        WeightedAverager<Double> percentageTotalFilesClonedPerProject = new WeightedAverager<Double>();
        WeightedAverager<Double> averagePercentageFilesClonedPerProject = new WeightedAverager<Double>();
        WeightedAverager<Double> maxPercentageFilesClonedPerProject = new WeightedAverager<Double>();
      }
      PriorityQueue<ComparableObject<ProjectMatches, Double>> mostCloning = new PriorityQueue<ComparableObject<ProjectMatches,Double>>(totalProjects, ComparableObject.<ProjectMatches, Double>getReverseComparator());
      PriorityQueue<ComparableObject<ProjectMatches, Integer>> biggestCloneSize = new PriorityQueue<ComparableObject<ProjectMatches,Integer>>(totalProjects, ComparableObject.<ProjectMatches, Integer>getReverseComparator());
      PriorityQueue<ComparableObject<ProjectMatches, Integer>> biggestSizeCloning = new PriorityQueue<ComparableObject<ProjectMatches,Integer>>(totalProjects, ComparableObject.<ProjectMatches, Integer>getReverseComparator());
      
      Map<Confidence, Map<DetectionMethod, Stats>> statsMap = Helper.newEnumMap(Confidence.class);
      for (Confidence confidence : Confidence.values()) {
        Map<DetectionMethod, Stats> innerMap = Helper.newEnumMap(DetectionMethod.class);
        for (DetectionMethod method : DetectionMethod.values()) {
          // For this specific confidence level and detection method, calculate the statistics
          Stats stats = new Stats();
          for (ProjectMatches projectMatch : matches.getProjectMatches()) {
            boolean hasCloneProject = false;
            Averager<Integer> filesClonedPerProject = new Averager<Integer>();
            Collection<File> clonedFiles = Helper.newHashSet();
            for (MatchingProjects matchingProjects : projectMatch.getMatchingProjects()) {
              int filesCloned = 0;
              for (MatchStatus status : matches.getFileMatch(matchingProjects.getProject(), projectMatch.getProject()).getMatchStatusSet()) {
                Confidence other = status.get(method);
                if (other != null && confidence.compareTo(other) <= 0) {
                  hasCloneProject = true;
                  filesCloned++;
                  clonedFiles.add(status.getFile());
                }
              }
              filesClonedPerProject.addValue(filesCloned);
            }
            
            if (hasCloneProject) {
              int size = projectMatch.getProject().getFiles().size();
              stats.projectsWithClones++;
              stats.totalFilesClonedPerProject.addValue(clonedFiles.size());
              stats.percentageTotalFilesClonedPerProject.addValue((double) clonedFiles.size() / (double) size, size);
              stats.averagefilesClonedPerProject.addValue(filesClonedPerProject.getMean());
              stats.maxFilesClonedPerProject.addValue(filesClonedPerProject.getMax());
              stats.averagePercentageFilesClonedPerProject.addValue(filesClonedPerProject.getMean() / (double) size, size);
              double maxPercentCloned = filesClonedPerProject.getMax() / (double) size;
              stats.maxPercentageFilesClonedPerProject.addValue(maxPercentCloned, size);
              if (method == DetectionMethod.COMBINED && confidence == Confidence.HIGH) {
                mostCloning.add(new ComparableObject<ProjectMatches, Double>(projectMatch, maxPercentCloned));
                biggestCloneSize.add(new ComparableObject<ProjectMatches, Integer>(projectMatch, filesClonedPerProject.getMax()));
                if (maxPercentCloned >= .05) {
                  biggestSizeCloning.add(new ComparableObject<ProjectMatches, Integer>(projectMatch, size));
                }
              }
              if (filesClonedPerProject.getMax() > projectMatch.getProject().getFiles().size()) {
                logger.log(Level.SEVERE, filesClonedPerProject.getMax() + " cloned files out of " + projectMatch.getProject().getFiles().size() + " total files");
                logger.log(Level.SEVERE, "  " + projectMatch.getProject().toString());
              }
            }
          }
          innerMap.put(method, stats);
        }
        statsMap.put(confidence, innerMap);
      }
      
      // Print out the project-project matching report
      BufferedWriter bw = null;
      try {
        bw = FileUtils.getBufferedWriter(HIGHEST_PERCENT_CLONES_FILE);
        for (ComparableObject<ProjectMatches, Double> co : mostCloning) {
          Project project = co.getObject().getProject();
          bw.write(project + " at " + (co.getComp().doubleValue() * 100) + "% of " + project.getFiles().size() + " files");
          bw.newLine();
          for (MatchingProjects matchingProjects : co.getObject().getMatchingProjects()) {
            Project otherProject = matchingProjects.getProject();
            int count = 0;
            for (File file : project.getFiles()) {
              for (KeyMatch match : file.getCombinedKey().getMatches()) {
                if (match.getFile().getProject() == otherProject && match.getConfidence() == Confidence.HIGH) {
                  count++;
                  break;
                }
              }
            }
            if (count > 0) {
              bw.write("  " + otherProject + " " + count + " (" + ((double) count / (double) project.getFiles().size()) * 100 + "%)" );
              bw.newLine();
              for (File file : project.getFiles()) {
                boolean first = true;
                for (KeyMatch match : file.getCombinedKey().getMatches()) {
                  if (match.getConfidence() == Confidence.HIGH && match.getFile().getProject() == otherProject) {
                    if (first) {
                      first = false;
                      bw.write("    " + file.getPath());
                      bw.newLine();
                    }
                    // check if it's a perfect match
                    MatchStatus status = matchingProjects.getMatchStatus(match.getFile());
                    if (status.get(DetectionMethod.HASH) == Confidence.HIGH) {
                      bw.write("    H " + match.getFile().getPath());
                    } else {
                      bw.write("      " + match.getFile().getPath());
                    }
                    bw.newLine();
                  }
                }
              }
            }
          }
        }
      } catch (IOException e) {
        logger.log(Level.SEVERE, "Unable to write project matching file", e);
      } finally {
        FileUtils.close(bw);
      }
      
      try {
        bw = FileUtils.getBufferedWriter(LARGEST_CLONE_SIZE_FILE);
        for (ComparableObject<ProjectMatches, Integer> co : biggestCloneSize) {
          Project project = co.getObject().getProject();
          bw.write(project + " at " + co.getComp() + " of " + project.getFiles().size() + " files");
          bw.newLine();
          for (MatchingProjects matchingProjects : co.getObject().getMatchingProjects()) {
            Project otherProject = matchingProjects.getProject();
            int count = 0;
            for (File file : project.getFiles()) {
              for (KeyMatch match : file.getCombinedKey().getMatches()) {
                if (match.getFile().getProject() == otherProject && match.getConfidence() == Confidence.HIGH) {
                  count++;
                  break;
                }
              }
            }
            if (count > 0) {
              bw.write("  " + otherProject + " " + count + " (" + ((double) count / (double) project.getFiles().size()) * 100 + "%)" );
              bw.newLine();
              for (File file : project.getFiles()) {
                boolean first = true;
                for (KeyMatch match : file.getCombinedKey().getMatches()) {
                  if (match.getConfidence() == Confidence.HIGH && match.getFile().getProject() == otherProject) {
                    if (first) {
                      first = false;
                      bw.write("    " + file.getPath());
                      bw.newLine();
                    }
                    // check if it's a perfect match
                    MatchStatus status = matchingProjects.getMatchStatus(match.getFile());
                    if (status.get(DetectionMethod.HASH) == Confidence.HIGH) {
                      bw.write("    H " + match.getFile().getPath());
                    } else {
                      bw.write("      " + match.getFile().getPath());
                    }
                    bw.newLine();
                  }
                }
              }
            }
          }
        }
      } catch (IOException e) {
        logger.log(Level.SEVERE, "Unable to write project matching file", e);
      } finally {
        FileUtils.close(bw);
      }
      
      try {
        bw = FileUtils.getBufferedWriter(LARGEST_CLONED_PROJECTS_FILE);
        for (ComparableObject<ProjectMatches, Integer> co : biggestSizeCloning) {
          Project project = co.getObject().getProject();
          bw.write(project + ": " + project.getFiles().size() + " files");
          bw.newLine();
          for (MatchingProjects matchingProjects : co.getObject().getMatchingProjects()) {
            Project otherProject = matchingProjects.getProject();
            int count = 0;
            for (File file : project.getFiles()) {
              for (KeyMatch match : file.getCombinedKey().getMatches()) {
                if (match.getFile().getProject() == otherProject && match.getConfidence() == Confidence.HIGH) {
                  count++;
                  break;
                }
              }
            }
            if (count > 0) {
              bw.write("  " + otherProject + " " + count + " (" + ((double) count / (double) project.getFiles().size()) * 100 + "%)" );
              bw.newLine();
              for (File file : project.getFiles()) {
                boolean first = true;
                for (KeyMatch match : file.getCombinedKey().getMatches()) {
                  if (match.getConfidence() == Confidence.HIGH && match.getFile().getProject() == otherProject) {
                    if (first) {
                      first = false;
                      bw.write("    " + file.getPath());
                      bw.newLine();
                    }
                    // check if it's a perfect match
                    MatchStatus status = matchingProjects.getMatchStatus(match.getFile());
                    if (status.get(DetectionMethod.HASH) == Confidence.HIGH) {
                      bw.write("    H " + match.getFile().getPath());
                    } else {
                      bw.write("      " + match.getFile().getPath());
                    }
                    bw.newLine();
                  }
                }
              }
            }
          }
        }
      } catch (IOException e) {
        logger.log(Level.SEVERE, "Unable to write project matching file", e);
      } finally {
        FileUtils.close(bw);
      }
      
      TablePrettyPrinter printer = TablePrettyPrinter.getTablePrettyPrinter(PROJECT_MATCHING_STATS_FILE);
      printer.setFractionDigits(2);
      int numConf = Confidence.values().length;
      int numMeth = DetectionMethod.values().length;
      printer.beginTable(numConf * numMeth + 1);
      printer.addDividerRow();
      printer.beginRow();
      printer.addCell("");
      for (Confidence confidence : Confidence.values()) {
        printer.addCell(confidence.toString(), DetectionMethod.values().length, Alignment.CENTER);
      }
      printer.beginRow();
      printer.addCell("");
      for (int i = 0; i < numConf; i++) {
        for (DetectionMethod method : DetectionMethod.values()) {
          printer.addCell(method.toString());
        }
      }
      printer.addDividerRow();
      printer.beginRow();
      printer.addCell("Total Projects");
      for (int i = 0, max = numConf * numMeth; i < max; i++) {
        printer.addCell(totalProjects);
      }
      printer.beginRow();
      printer.addCell("Project with Clones");
      for (Confidence confidence : Confidence.values()) {
        for (DetectionMethod method : DetectionMethod.values()) {
          printer.addCell(statsMap.get(confidence).get(method).projectsWithClones);
        }
      }
      printer.beginRow();
      printer.addCell("Avg Total Clones Per Project");
      for (Confidence confidence : Confidence.values()) {
        for (DetectionMethod method : DetectionMethod.values()) {
          Stats stats = statsMap.get(confidence).get(method);
          printer.addCellMeanSTD(stats.totalFilesClonedPerProject.getMean(), stats.totalFilesClonedPerProject.getStandardDeviation());
        }
      }
      printer.beginRow();
      printer.addCell("Avg % Clones Per Project");
      for (Confidence confidence : Confidence.values()) {
        for (DetectionMethod method : DetectionMethod.values()) {
          Stats stats = statsMap.get(confidence).get(method);
          printer.addCellMeanSTD(stats.percentageTotalFilesClonedPerProject.getMean(), stats.percentageTotalFilesClonedPerProject.getStandardDeviation());
        }
      }
      printer.beginRow();
      printer.addCell("Avg W% Clones Per Project");
      for (Confidence confidence : Confidence.values()) {
        for (DetectionMethod method : DetectionMethod.values()) {
          Stats stats = statsMap.get(confidence).get(method);
          printer.addCellMeanSTD(stats.percentageTotalFilesClonedPerProject.getWeightedMean(), stats.percentageTotalFilesClonedPerProject.getWeightedStandardDeviation());
        }
      }
      printer.beginRow();
      printer.addCell("Avg # Files Cloned Per Project Pair");
      for (Confidence confidence : Confidence.values()) {
        for (DetectionMethod method : DetectionMethod.values()) {
          Stats stats = statsMap.get(confidence).get(method);
          printer.addCellMeanSTD(stats.averagefilesClonedPerProject.getMean(), stats.averagefilesClonedPerProject.getStandardDeviation());
        }
      }
      printer.beginRow();
      printer.addCell("Avg # Files Cloned Per Project Max");
      for (Confidence confidence : Confidence.values()) {
        for (DetectionMethod method : DetectionMethod.values()) {
          Stats stats = statsMap.get(confidence).get(method);
          printer.addCellMeanSTD(stats.maxFilesClonedPerProject.getMean(), stats.maxFilesClonedPerProject.getStandardDeviation());
        }
      }
      printer.beginRow();
      printer.addCell("Max # Files Cloned Per Project Max");
      for (Confidence confidence : Confidence.values()) {
        for (DetectionMethod method : DetectionMethod.values()) {
          Stats stats = statsMap.get(confidence).get(method);
          printer.addCell(stats.maxFilesClonedPerProject.getMax());
        }
      }
      printer.beginRow();
      printer.addCell("Avg % Files Cloned Per Project Pair");
      for (Confidence confidence : Confidence.values()) {
        for (DetectionMethod method : DetectionMethod.values()) {
          Stats stats = statsMap.get(confidence).get(method);
          printer.addCellMeanSTD(stats.averagePercentageFilesClonedPerProject.getMean(), stats.averagePercentageFilesClonedPerProject.getStandardDeviation());
        }
      }
      printer.beginRow();
      printer.addCell("Avg W% Files Cloned Per Project Pair");
      for (Confidence confidence : Confidence.values()) {
        for (DetectionMethod method : DetectionMethod.values()) {
          Stats stats = statsMap.get(confidence).get(method);
          printer.addCellMeanSTD(stats.averagePercentageFilesClonedPerProject.getWeightedMean(), stats.averagePercentageFilesClonedPerProject.getWeightedStandardDeviation());
        }
      }
      printer.beginRow();
      printer.addCell("Avg % Files Cloned Per Project Max");
      for (Confidence confidence : Confidence.values()) {
        for (DetectionMethod method : DetectionMethod.values()) {
          Stats stats = statsMap.get(confidence).get(method);
          printer.addCellMeanSTD(stats.maxPercentageFilesClonedPerProject.getMean(), stats.maxPercentageFilesClonedPerProject.getStandardDeviation());
        }
      }
      printer.beginRow();
      printer.addCell("Avg W% Files Cloned Per Project Max");
      for (Confidence confidence : Confidence.values()) {
        for (DetectionMethod method : DetectionMethod.values()) {
          Stats stats = statsMap.get(confidence).get(method);
          printer.addCellMeanSTD(stats.maxPercentageFilesClonedPerProject.getWeightedMean(), stats.maxPercentageFilesClonedPerProject.getWeightedStandardDeviation());
        }
      }
      printer.beginRow();
      printer.addCell("Max % Files Cloned Per Project Max");
      for (Confidence confidence : Confidence.values()) {
        for (DetectionMethod method : DetectionMethod.values()) {
          Stats stats = statsMap.get(confidence).get(method);
          if (stats.maxPercentageFilesClonedPerProject.getMax() > 1) {
            logger.log(Level.SEVERE, ">1!" + stats.maxPercentageFilesClonedPerProject.getMax());
          }
          printer.addCell(stats.maxPercentageFilesClonedPerProject.getMax());
        }
      }
      printer.addDividerRow();
      printer.close();
      
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

  private static void generateFilter(ProjectMap projects) {
    FileFilter filter = new FileFilter();
    for (Project project : projects.getProjects()) {
      for (File file : project.getFiles()) {
        filter.addFile(project.getName(), file.getPath());
      }
    }
    
    DirectoryClusterer.generateFilteredListing(filter);
  }
  
  public static void performAnalysis() {
    ProjectMap projects = new ProjectMap();
    HashingClusterer.loadFileListing(projects);
    FqnClusterer.loadFileListing(projects);
    FingerprintClusterer.loadFileListing(projects);
//    generateFilter(projects);
    DirectoryClusterer.loadMatching(projects);
    
    compareFileSets(projects);
    projects.filterFiles();
    
    CombinedClusterer.computeCombinedKeys(projects);

    printClonePairs(projects);
    computeCloningStatistics(projects);
    computeProjectMatching(projects);

    logger.info("Done!");
  }
}
