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
import java.util.PriorityQueue;
import java.util.logging.Level;

import edu.uci.ics.sourcerer.clusterer.cloning.basic.Confidence;
import edu.uci.ics.sourcerer.clusterer.cloning.basic.DetectionMethod;
import edu.uci.ics.sourcerer.clusterer.cloning.basic.File;
import edu.uci.ics.sourcerer.clusterer.cloning.basic.KeyMatch;
import edu.uci.ics.sourcerer.clusterer.cloning.basic.Project;
import edu.uci.ics.sourcerer.clusterer.cloning.basic.ProjectMap;
import edu.uci.ics.sourcerer.clusterer.cloning.method.combination.CombinedClusterer;
import edu.uci.ics.sourcerer.clusterer.cloning.method.fingerprint.FingerprintClusterer;
import edu.uci.ics.sourcerer.clusterer.cloning.method.fqn.FqnClusterer;
import edu.uci.ics.sourcerer.clusterer.cloning.method.hash.HashingClusterer;
import edu.uci.ics.sourcerer.clusterer.cloning.pairwise.MatchStatus;
import edu.uci.ics.sourcerer.clusterer.cloning.pairwise.MatchingProjects;
import edu.uci.ics.sourcerer.clusterer.cloning.pairwise.ProjectMatchSet;
import edu.uci.ics.sourcerer.clusterer.cloning.pairwise.ProjectMatches;
import edu.uci.ics.sourcerer.util.Averager;
import edu.uci.ics.sourcerer.util.ComparableObject;
import edu.uci.ics.sourcerer.util.Helper;
import edu.uci.ics.sourcerer.util.WeightedAverager;
import edu.uci.ics.sourcerer.util.io.FileUtils;
import edu.uci.ics.sourcerer.util.io.Property;
import edu.uci.ics.sourcerer.util.io.TablePrettyPrinter;
import edu.uci.ics.sourcerer.util.io.TablePrettyPrinter.Alignment;
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
  public static Property<String> HIGHEST_PERCENT_CLONES_FILE = new StringProperty("highest-percent-clones-file", "highest-percent-clones.txt", "");
  public static Property<String> LARGEST_CLONE_SIZE_FILE = new StringProperty("largest-clone-size-file", "largest-clone-size.txt", "");
  public static Property<String> LARGEST_CLONED_PROJECTS_FILE = new StringProperty("largest-cloned-projects-file", "largest-cloned-projects.txt", "");
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
        int uniqueCombinedFiles = 0;
        for (Project project : projects.getProjects()) {
          for (File file : project.getFiles()) {
            // Exclude all files that don't match
            if (file.hasAllKeys()) {
              if (++totalFiles % 100000 == 0) {
                logger.info("    " + totalFiles + " analyzed");
//                logger.log(Level.WARNING, "Free mem: " + Runtime.getRuntime().freeMemory());
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
              if (file.getCombinedKey().isUnique(Confidence.HIGH)) {
                uniqueCombinedFiles++;
              }
            }
          }
        }
        logger.info("  " + totalFiles + " analyzed");
        
        printer.beginTable(5);
        printer.addHeader("High Confidence");
        printer.addDividerRow();
        printer.addRow("", "Hash", "FQN", "Fingerprint", "Combined");
        printer.addDividerRow();
        printer.beginRow();
        printer.addCell("Total Files");
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
        printer.beginRow();
        printer.addCell("Duplicated Files");
        printer.addCell(totalFiles - uniqueHashFiles);
        printer.addCell(totalFiles - uniqueFqnFiles);
        printer.addCell(totalFiles - uniqueFingerprintFiles);
        printer.addCell(totalFiles - uniqueCombinedFiles);
        printer.beginRow();
        printer.addCell("Cloning Rate");
        printer.addCell(((double)(totalFiles - uniqueHashFiles) / totalFiles));
        printer.addCell(((double)(totalFiles - uniqueFqnFiles) / totalFiles));
        printer.addCell(((double)(totalFiles - uniqueFingerprintFiles) / totalFiles));
        printer.addCell(((double)(totalFiles - uniqueCombinedFiles) / totalFiles));
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
            }
          }
        }
        logger.info("  " + totalFiles + " analyzed");
        
        printer.beginTable(5);
        printer.addHeader("Medium Confidence");
        printer.addDividerRow();
        printer.addRow("", "Hash", "FQN", "Fingerprint", "Combined");
        printer.addDividerRow();
        printer.beginRow();
        printer.addCell("Total Files");
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
        printer.beginRow();
        printer.addCell("Duplicated Files");
        printer.addCell(totalFiles - uniqueHashFiles);
        printer.addCell(totalFiles - uniqueFqnFiles);
        printer.addCell(totalFiles - uniqueFingerprintFiles);
        printer.addCell(totalFiles - uniqueCombinedFiles);
        printer.beginRow();
        printer.addCell("Cloning Rate");
        printer.addCell(((double)(totalFiles - uniqueHashFiles) / totalFiles));
        printer.addCell(((double)(totalFiles - uniqueFqnFiles) / totalFiles));
        printer.addCell(((double)(totalFiles - uniqueFingerprintFiles) / totalFiles));
        printer.addCell(((double)(totalFiles - uniqueCombinedFiles) / totalFiles));
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
            }
          }
        }
        logger.info("  " + totalFiles + " analyzed");
        
        printer.beginTable(5);
        printer.addHeader("Low Confidence");
        printer.addDividerRow();
        printer.addRow("", "Hash", "FQN", "Fingerprint", "Combined");
        printer.addDividerRow();
        printer.beginRow();
        printer.addCell("Total Files");
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
        printer.beginRow();
        printer.addCell("Duplicated Files");
        printer.addCell(totalFiles - uniqueHashFiles);
        printer.addCell(totalFiles - uniqueFqnFiles);
        printer.addCell(totalFiles - uniqueFingerprintFiles);
        printer.addCell(totalFiles - uniqueCombinedFiles);
        printer.beginRow();
        printer.addCell("Cloning Rate");
        printer.addCell(((double)(totalFiles - uniqueHashFiles) / totalFiles));
        printer.addCell(((double)(totalFiles - uniqueFqnFiles) / totalFiles));
        printer.addCell(((double)(totalFiles - uniqueFingerprintFiles) / totalFiles));
        printer.addCell(((double)(totalFiles - uniqueCombinedFiles) / totalFiles));
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
            for (MatchingProjects matchingProjects : projectMatch.getMatchingProjects()) {
              int filesCloned = 0;
              for (MatchStatus status : matches.getFileMatch(matchingProjects.getProject(), projectMatch.getProject()).getMatchStatusSet()) {
                Confidence other = status.get(method);
                if (other != null && confidence.compareTo(other) <= 0) {
                  hasCloneProject = true;
                  filesCloned++;
                }
              }
              filesClonedPerProject.addValue(filesCloned);
            }
            if (hasCloneProject) {
              stats.projectsWithClones++;
              stats.averagefilesClonedPerProject.addValue(filesClonedPerProject.getMean());
              stats.maxFilesClonedPerProject.addValue(filesClonedPerProject.getMax());
              stats.averagePercentageFilesClonedPerProject.addValue(filesClonedPerProject.getMean() / (double) projectMatch.getProject().getFiles().size(), projectMatch.getProject().getFiles().size());
              double maxPercentCloned = filesClonedPerProject.getMax() / (double) projectMatch.getProject().getFiles().size();
              stats.maxPercentageFilesClonedPerProject.addValue(maxPercentCloned, projectMatch.getProject().getFiles().size());
              if (method == DetectionMethod.COMBINED && confidence == Confidence.HIGH) {
                mostCloning.add(new ComparableObject<ProjectMatches, Double>(projectMatch, maxPercentCloned));
                biggestCloneSize.add(new ComparableObject<ProjectMatches, Integer>(projectMatch, filesClonedPerProject.getMax()));
                if (maxPercentCloned >= .05) {
                  biggestSizeCloning.add(new ComparableObject<ProjectMatches, Integer>(projectMatch, projectMatch.getProject().getFiles().size()));
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
            int files = 0;
            for (File file : project.getFiles()) {
              if (file.hasAllKeys()) {
                files++;
                boolean found = false;
                for (KeyMatch match : file.getCombinedKey().getMatches()) {
                  if (match.getConfidence() == Confidence.HIGH && match.getFile().getProject() == otherProject) {
                    found = true;
                  }
                }
                if (found) {
                  count++;
                }
              }
            }
            if (count > 0) {
              bw.write("  " + otherProject + " " + count + " (" + ((double) count / (double) files) * 100 + "%)" );
              bw.newLine();
              for (File file : project.getFiles()) {
                if (file.hasAllKeys()) {
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
                } else {
                  bw.write("  X " + file.getPath());
                  bw.newLine();
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
            int files = 0;
            for (File file : project.getFiles()) {
              if (file.hasAllKeys()) {
                files++;
                boolean found = false;
                for (KeyMatch match : file.getCombinedKey().getMatches()) {
                  if (match.getConfidence() == Confidence.HIGH && match.getFile().getProject() == otherProject) {
                    found = true;
                  }
                }
                if (found) {
                  count++;
                }
              }
            }
            if (count > 0) {
              bw.write("  " + otherProject + " " + count + " (" + ((double) count / (double) files) * 100 + "%)" );
              bw.newLine();
              for (File file : project.getFiles()) {
                if (file.hasAllKeys()) {
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
                } else {
                  bw.write("  X " + file.getPath());
                  bw.newLine();
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
          bw.write(project + ":" + project.getFiles().size() + " files");
          bw.newLine();
          for (MatchingProjects matchingProjects : co.getObject().getMatchingProjects()) {
            Project otherProject = matchingProjects.getProject();
            int count = 0;
            int files = 0;
            for (File file : project.getFiles()) {
              if (file.hasAllKeys()) {
                files++;
                boolean found = false;
                for (KeyMatch match : file.getCombinedKey().getMatches()) {
                  if (match.getConfidence() == Confidence.HIGH && match.getFile().getProject() == otherProject) {
                    found = true;
                  }
                }
                if (found) {
                  count++;
                }
              }
            }
            if (count > 0) {
              bw.write("  " + otherProject + " " + count + " (" + ((double) count / (double) files) * 100 + "%)" );
              bw.newLine();
              for (File file : project.getFiles()) {
                if (file.hasAllKeys()) {
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
                } else {
//                  bw.write("  X " + file.getPath());
//                  bw.newLine();
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

  public static void performAnalysis() {
    ProjectMap projects = new ProjectMap();
//    logger.log(Level.WARNING, "Free mem: " + Runtime.getRuntime().freeMemory());
    HashingClusterer.loadFileListing(projects);
//    logger.log(Level.WARNING, "Free mem: " + Runtime.getRuntime().freeMemory());
    FqnClusterer.loadFileListing(projects);
//    logger.log(Level.WARNING, "Free mem: " + Runtime.getRuntime().freeMemory());
    FingerprintClusterer.loadFileListing(projects);
//    logger.log(Level.WARNING, "Free mem: " + Runtime.getRuntime().freeMemory());
    CombinedClusterer.computeCombinedKeys(projects);

//    logger.log(Level.WARNING, "Free mem: " + Runtime.getRuntime().freeMemory());
    compareFileSets(projects);
//    logger.log(Level.WARNING, "Free mem: " + Runtime.getRuntime().freeMemory());
    computeCloningStatistics(projects);
    computeProjectMatching(projects);
    
    logger.info("Done!");
  }
}
