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
//import java.io.File;
//import java.io.FileWriter;
//import java.io.IOException;
//import java.util.Map;
//import java.util.logging.Level;
//
//import edu.uci.ics.sourcerer.clusterer.cloning.method.dir.DirectoryClusterer;
//import edu.uci.ics.sourcerer.clusterer.cloning.method.fingerprint.FingerprintClusterer;
//import edu.uci.ics.sourcerer.clusterer.cloning.method.fqn.FqnClusterer;
//import edu.uci.ics.sourcerer.clusterer.cloning.method.hash.HashingClusterer;
//import edu.uci.ics.sourcerer.util.Counter;
//import edu.uci.ics.sourcerer.util.Helper;
//import edu.uci.ics.sourcerer.util.io.FileUtils;
//import edu.uci.ics.sourcerer.util.io.Properties;
//import edu.uci.ics.sourcerer.util.io.Property;
//import edu.uci.ics.sourcerer.util.io.TablePrettyPrinter;
//import edu.uci.ics.sourcerer.util.io.properties.StringProperty;
//
///**
// * @author Joel Ossher (jossher@uci.edu)
// */
//public class Aggregator {
//  public static final Property<String> GENERAL_STATISTICS = new StringProperty("general-statistics", "general-statistics.txt", "General statistics for all cloning methods.");
//  public static final Property<String> FILTERED_STATISTICS = new StringProperty("filtered-statistics", "filtered-statistics.txt", "Filtered general statistics for all cloning methods.");
//  public static final Property<String> MATCHING_COMPARISON = new StringProperty("matching-comparison", "matching-comparison.txt", "Pairwise comparison of matching between methods");
//  public static final Property<String> COMPARISON_STATISTICS = new StringProperty("comparison-statistics", "comparison-statistics.txt", "Statistics for pairwise comparison of matching between methods");
//  
//  public static void computeGeneralStats() {
//    MatchingStatistics dir80stats = DirectoryClusterer.getMatching80().getStatistics();
//    MatchingStatistics dir50stats = DirectoryClusterer.getMatching50().getStatistics();
//    MatchingStatistics dir30stats = DirectoryClusterer.getMatching30().getStatistics();
//    MatchingStatistics hashingStats = HashingClusterer.getMatching().getStatistics();
//    MatchingStatistics fqnStats = FqnClusterer.getMatching().getStatistics();
//    MatchingStatistics fingerprintStats = FingerprintClusterer.getMatching().getStatistics();
//    
//    TablePrettyPrinter printer = TablePrettyPrinter.getTablePrettyPrinter(GENERAL_STATISTICS);
//    printer.beginTable(7);
//    printer.addDividerRow();
//    printer.addRow("", "Dir 80", "Dir 50", "Dir 30", "Hashing", "FQN", "Fingerprint");
//    printer.addDividerRow();
//    
//    // Total number of files
//    printer.beginRow();
//    printer.addCell("Total Files");
//    printer.addCell(dir80stats.getTotalFiles());
//    printer.addCell(dir50stats.getTotalFiles());
//    printer.addCell(dir30stats.getTotalFiles());
//    printer.addCell(hashingStats.getTotalFiles());
//    printer.addCell(fqnStats.getTotalFiles());
//    printer.addCell(fingerprintStats.getTotalFiles());
//    
//    // Total number of projects
//    printer.beginRow();
//    printer.addCell("Total Projects");
//    printer.addCell(dir80stats.getTotalProjects());
//    printer.addCell(dir50stats.getTotalProjects());
//    printer.addCell(dir30stats.getTotalProjects());
//    printer.addCell(hashingStats.getTotalProjects());
//    printer.addCell(fqnStats.getTotalProjects());
//    printer.addCell(fingerprintStats.getTotalProjects());
//    
//    // Eliminate exact duplicates within projects
//    printer.beginRow();
//    printer.addCell("Project Unique Files");
//    printer.addCell(dir80stats.getProjectUniqueFiles());
//    printer.addCell(dir50stats.getProjectUniqueFiles());
//    printer.addCell(dir30stats.getProjectUniqueFiles());
//    printer.addCell(hashingStats.getProjectUniqueFiles());
//    printer.addCell(fqnStats.getProjectUniqueFiles());
//    printer.addCell(fingerprintStats.getProjectUniqueFiles());
//    
//    // Eliminate exact duplicates globally
//    printer.beginRow();
//    printer.addCell("Global Unique Files");
//    printer.addCell(dir80stats.getGlobalUniqueFiles());
//    printer.addCell(dir50stats.getGlobalUniqueFiles());
//    printer.addCell(dir30stats.getGlobalUniqueFiles());
//    printer.addCell(hashingStats.getGlobalUniqueFiles());
//    printer.addCell(fqnStats.getGlobalUniqueFiles());
//    printer.addCell(fingerprintStats.getGlobalUniqueFiles());
//    
//    // Files that only occur in one place
//    printer.beginRow();
//    printer.addCell("Singleton Files");
//    printer.addCell(dir80stats.getSingletonFiles());
//    printer.addCell(dir50stats.getSingletonFiles());
//    printer.addCell(dir30stats.getSingletonFiles());
//    printer.addCell(hashingStats.getSingletonFiles());
//    printer.addCell(fqnStats.getSingletonFiles());
//    printer.addCell(fingerprintStats.getSingletonFiles());
//    
//    // Unique files that occur in more than one project
//    printer.beginRow();
//    printer.addCell("Unique Duplicate Files");
//    printer.addCell(dir80stats.getUniqueDuplicateFiles());
//    printer.addCell(dir50stats.getUniqueDuplicateFiles());
//    printer.addCell(dir30stats.getUniqueDuplicateFiles());
//    printer.addCell(hashingStats.getUniqueDuplicateFiles());
//    printer.addCell(fqnStats.getUniqueDuplicateFiles());
//    printer.addCell(fingerprintStats.getUniqueDuplicateFiles());
//    
//    // Total count of files that occur in more than one project
//    printer.beginRow();
//    printer.addCell("Total Duplicate Files");
//    printer.addCell(dir80stats.getTotalDuplicateFiles());
//    printer.addCell(dir50stats.getTotalDuplicateFiles());
//    printer.addCell(dir30stats.getTotalDuplicateFiles());
//    printer.addCell(hashingStats.getTotalDuplicateFiles());
//    printer.addCell(fqnStats.getTotalDuplicateFiles());
//    printer.addCell(fingerprintStats.getTotalDuplicateFiles());
//    
//    // Percent of files that occur in more than one project
//    // Total Duplicate Files / Project Unique Files
//    printer.beginRow();
//    printer.addCell("Duplication Rate");
//    printer.addCell(dir80stats.getDupRate());
//    printer.addCell(dir50stats.getDupRate());
//    printer.addCell(dir30stats.getDupRate());
//    printer.addCell(hashingStats.getDupRate());
//    printer.addCell(fqnStats.getDupRate());
//    printer.addCell(fingerprintStats.getDupRate());
//    
//    // Average number of times a duplicate file appears
//    printer.beginRow();
//    printer.addCell("Occurance Rate");
//    printer.addCell(dir80stats.getDupOccuranceRate());
//    printer.addCell(dir50stats.getDupOccuranceRate());
//    printer.addCell(dir30stats.getDupOccuranceRate());
//    printer.addCell(hashingStats.getDupOccuranceRate());
//    printer.addCell(fqnStats.getDupOccuranceRate());
//    printer.addCell(fingerprintStats.getDupOccuranceRate());
//    
//    // Average percent of duplication at a project level
//    printer.beginRow();
//    printer.addCell("Project Duplication Rate");
//    printer.addCell(dir80stats.getProjectDupRate());
//    printer.addCell(dir50stats.getProjectDupRate());
//    printer.addCell(dir30stats.getProjectDupRate());
//    printer.addCell(hashingStats.getProjectDupRate());
//    printer.addCell(fqnStats.getDupOccuranceRate());
//    printer.addCell(fingerprintStats.getDupOccuranceRate());
//    
//    printer.addDividerRow();
//    printer.endTable();
//    printer.close();
//    
//    logger.info("Done!");
//  }
//  
//  private static void processMatching(Matching matching, BufferedWriter bw, String byProject) throws IOException {
//    matching.printCloningByProject(byProject);
//    MatchingStatistics stats = matching.getStatistics();
//    // Total number of files
//    bw.write("Total Files: " + stats.getTotalFiles() + "\n");
//    
//    // Total number of projects
//    bw.write("Total Projects: " + stats.getTotalProjects() + "\n");
//    
//    // Eliminate exact duplicates within projects
//    bw.write("Project Unique Files: " + stats.getProjectUniqueFiles() + "\n");
//    
//    // Eliminate exact duplicates globally
//    bw.write("Global Unique Files: " + stats.getGlobalUniqueFiles() + "\n");
//    
//    // Files that only occur in one place
//    bw.write("Singleton Files: " + stats.getSingletonFiles() + "\n");
//    
//    // Unique files that occur in more than one project
//    bw.write("Unique Duplicate Files: " + stats.getUniqueDuplicateFiles() + "\n");
//    
//    // Total count of files that occur in more than one project
//    bw.write("Total Duplicate Files: " + stats.getTotalDuplicateFiles() + "\n");
//    
//    // Percent of files that occur in more than one project
//    // Total Duplicate Files / Project Unique Files
//    bw.write("Duplication Rate: " + stats.getDupRate() + "\n");
//    
//    // Average number of times a duplicate file appears
//    bw.write("Occurance Rate: " + stats.getDupOccuranceRate() + "\n");
//    
//    // Average percent of duplication at a project level
//    bw.write("Project Duplication Rate: " + stats.getProjectDupRate() + "\n");
//    bw.write("\nRanked clusters\n");
//    int[] rankingHisto = null;
//    for (FileCluster cluster : matching.getRankedClusters()) {
//      if (rankingHisto == null) {
//        rankingHisto = new int[cluster.getProjectCount() + 1];
//      }
//      rankingHisto[cluster.getProjectCount()]++;
//      StringBuilder builder = new StringBuilder();
//      builder.append(cluster.getProjectCount());
//      for (String path : cluster.getPaths()) {
//        builder.append(" ").append(path);
//      }
//      builder.append("\n");
//      bw.write(builder.toString());
//    }
//    bw.write("\n");
//    for (int i = 0; i < rankingHisto.length; i++) {
//      bw.write(i + " " + rankingHisto[i] + "\n");
//    }
//  }
//  
//  public static void computeFilteredStats() {
//    BufferedWriter bw = null;
//    try {
//      logger.info("Processing dir80");
//      bw = FileUtils.getBufferedWriter("stats-dir80.txt");
//      processMatching(DirectoryClusterer.getFilteredMatching80(), bw, "clones-dir80.txt");
//    } catch (IOException e) {
//      logger.log(Level.SEVERE, "Error in dir80 stats");
//    } finally {
//      FileUtils.close(bw);
//    }
//    
////    try {
////      logger.info("Processing dir50");
////      bw = FileUtils.getBufferedWriter("stats-dir50.txt");
////      processMatching(DirectoryClusterer.getFilteredMatching50(), bw);
////    } catch (IOException e) {
////      logger.log(Level.SEVERE, "Error in dir50 stats");
////    } finally {
////      FileUtils.close(bw);
////    }
////    
////    try {
////      logger.info("Processing dir30");
////      bw = FileUtils.getBufferedWriter("stats-dir30.txt");
////      processMatching(DirectoryClusterer.getFilteredMatching30(), bw);
////    } catch (IOException e) {
////      logger.log(Level.SEVERE, "Error in dir30 stats");
////    } finally {
////      FileUtils.close(bw);
////    }
//    
//    try {
//      logger.info("Processing hashing");
//      bw = FileUtils.getBufferedWriter("stats-hashing.txt");
//      processMatching(HashingClusterer.getFilteredMatching(), bw, "clones-hashing.txt");
//    } catch (IOException e) {
//      logger.log(Level.SEVERE, "Error in hashing stats");
//    } finally {
//      FileUtils.close(bw);
//    }
//    
//    try {
//      logger.info("Processing fqn");
//      bw = FileUtils.getBufferedWriter("stats-fqn.txt");
//      processMatching(FqnClusterer.getFilteredMatching(), bw, "clones-fqn.txt");
//    } catch (IOException e) {
//      logger.log(Level.SEVERE, "Error in fqn stats");
//    } finally {
//      FileUtils.close(bw);
//    }
//    
//    try {
//      logger.info("Processing fingerprint");
//      bw = FileUtils.getBufferedWriter("stats-fingerprint.txt");
//      processMatching(FingerprintClusterer.getFilteredMatching(), bw, "clones-fingerprint.txt");
//    } catch (IOException e) {
//      logger.log(Level.SEVERE, "Error in fingerprint stats");
//    } finally {
//      FileUtils.close(bw);
//    }
//    logger.info("Done!");
//  }
//
//  private static final int DIR_80_METHOD = 0x01;
//  private static final int DIR_50_METHOD = 0x02;
//  private static final int DIR_30_METHOD = 0x04;
//  private static final int HASH_METHOD = 0x08;
//  private static final int FQN_METHOD = 0x10;
//  private static final int FINGERPRINT_METHOD = 0x20;
//  
//  public static void generateMatchingComparison() {
//    Map<String, Integer> matches = Helper.newHashMap();
//    
//    Integer newValue = DIR_80_METHOD;
//    for (FileCluster cluster : DirectoryClusterer.getFilteredMatching80()) {
//      for (String a : cluster.getPaths()) {
//        for (String b : cluster.getPaths()) {
//          if (a.compareTo(b) < 0) {
//            matches.put(a + " " + b, newValue);
//          }
//        }
//      }
//    }
////    
////    Integer dir5080 = DIR_80_METHOD | DIR_50_METHOD;
////    newValue = DIR_50_METHOD;
////    for (FileCluster cluster : DirectoryClusterer.getFilteredMatching50(filter)) {
////      for (String a : cluster.getPaths()) {
////        for (String b : cluster.getPaths()) {
////          if (a.compareTo(b) < 0) {
////            String key = a + " " + b;
////            if (matches.containsKey(key)) {
////              matches.put(key, dir5080);
////            } else {
////              matches.put(key, newValue);
////            }
////          }
////        }
////      }
////    }
//    
////    Integer newValue = DIR_30_METHOD;
////    for (FileCluster cluster : DirectoryClusterer.getFilteredMatching30(filter)) {
////      for (String a : cluster.getPaths()) {
////        for (String b : cluster.getPaths()) {
////          if (a.compareTo(b) < 0) {
////            String key = a + " " + b;
////            if (matches.containsKey(key)) {
////              matches.put(key, matches.get(key).intValue() | DIR_30_METHOD);
////            } else {
////              matches.put(key, newValue);
////            }
////          }
////        }
////      }
////    }
//    
//    newValue = HASH_METHOD;
//    for (FileCluster cluster : HashingClusterer.getFilteredMatching()) {
//      for (String a : cluster.getPaths()) {
//        for (String b : cluster.getPaths()) {
//          if (a.compareTo(b) < 0) {
//            String key = a + " " + b;
//            if (matches.containsKey(key)) {
//              matches.put(key, matches.get(key).intValue() | HASH_METHOD);
//            } else {
//              matches.put(key, newValue);
//            }
//          }
//        }
//      }
//    }
//    
//    newValue = FQN_METHOD;
//    for (FileCluster cluster : FqnClusterer.getFilteredMatching()) {
//      for (String a : cluster.getPaths()) {
//        for (String b : cluster.getPaths()) {
//          if (a.compareTo(b) < 0) {
//            String key = a + " " + b;
//            if (matches.containsKey(key)) {
//              matches.put(key, matches.get(key).intValue() | FQN_METHOD);
//            } else {
//              matches.put(key, newValue);
//            }
//          }
//        }
//      }
//    }
//    
//    newValue = FINGERPRINT_METHOD;
//    for (FileCluster cluster : FingerprintClusterer.getFilteredMatching()) {
//      for (String a : cluster.getPaths()) {
//        for (String b : cluster.getPaths()) {
//          if (a.compareTo(b) < 0) {
//            String key = a + " " + b;
//            if (matches.containsKey(key)) {
//              matches.put(key, matches.get(key).intValue() | FINGERPRINT_METHOD);
//            } else {
//              matches.put(key, newValue);
//            }
//          }
//        }
//      }
//    }
//    
//    Map<Integer, Counter<Integer>> results = Helper.newHashMap();
//    
//    BufferedWriter bw = null;
//    try {
//      bw = new BufferedWriter(new FileWriter(new File(Properties.OUTPUT.getValue(), MATCHING_COMPARISON.getValue())));
//      
//      for (Map.Entry<String, Integer> entry : matches.entrySet()) {
//        bw.write(entry.getKey() + " " + entry.getValue() + "\n");
//        Counter<Integer> counter = results.get(entry.getValue());
//        if (counter == null) {
//          counter = new Counter<Integer>(entry.getValue());
//          results.put(entry.getValue(), counter);
//        } 
//        counter.increment();
//      }
//    } catch (IOException e) {
//      logger.log(Level.SEVERE, "Unable to write matching comparison file", e);
//      return;
//    } finally {
//      FileUtils.close(bw);
//    }
//    
//    TablePrettyPrinter printer = TablePrettyPrinter.getTablePrettyPrinter(COMPARISON_STATISTICS);
//    printer.beginTable(2);
//    printer.addDividerRow();
//    printer.addRow("Types", "Count");
//    printer.addDividerRow();
//    for (Counter<Integer> counter : results.values()) {
//      printer.beginRow();
//      int val = counter.getObject().intValue();
//      StringBuilder types = new StringBuilder();
//      if ((val & DIR_30_METHOD) == DIR_30_METHOD) {
//        types.append("D30 ");
//      }
//      if ((val & DIR_50_METHOD) == DIR_50_METHOD) {
//        types.append("D50 ");
//      }
//      if ((val & DIR_80_METHOD) == DIR_80_METHOD) {
//        types.append("D80 ");
//      }
//      if ((val & HASH_METHOD) == HASH_METHOD) {
//        types.append("hash ");
//      }
//      if ((val & FQN_METHOD) == FQN_METHOD) {
//        types.append("fqn ");
//      }
//      if ((val & FINGERPRINT_METHOD) == FINGERPRINT_METHOD) {
//        types.append("fingerprint ");
//      }
//      printer.addCell(types.toString());
//      printer.addCell(counter.getCount());
//    }
//    printer.addDividerRow();
//    printer.close();
//  }
//}
