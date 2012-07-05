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
//package edu.uci.ics.sourcerer.tools.java.extractor;
//
//import static edu.uci.ics.sourcerer.util.io.logging.Logging.logger;
//
//import java.io.File;
//import java.util.Iterator;
//import java.util.logging.Level;
//
//import edu.uci.ics.sourcerer.tools.java.repo.model.JavaRepositoryFactory;
//import edu.uci.ics.sourcerer.tools.java.repo.model.extracted.ExtractedJarFile;
//import edu.uci.ics.sourcerer.tools.java.repo.model.extracted.ExtractedJavaRepository;
//import edu.uci.ics.sourcerer.util.TimeCounter;
//import edu.uci.ics.sourcerer.util.io.arguments.Argument;
//import edu.uci.ics.sourcerer.util.io.arguments.FileArgument;
//
///**
// * @author Joel Ossher (jossher@uci.edu)
// */
//public class ExtractionComparisonChecker {
//  public static final Argument<File> REPO_A = new FileArgument("repo-a", "Extracted Repository A");
//  public static final Argument<File> REPO_B = new FileArgument("repo-b", "Extracted Repository A");
//  
//  private final ExtractedJavaRepository repoA;
//  private final ExtractedJavaRepository repoB;
//  
//  private ExtractionComparisonChecker() {
//    logger.info("Loading Repository A from " + REPO_A.getValue().getAbsolutePath());
//    repoA = JavaRepositoryFactory.INSTANCE.loadExtractedJavaRepository(REPO_A);
//    logger.info("Loading Repository B from " + REPO_B.getValue().getAbsolutePath());
//    repoB = JavaRepositoryFactory.INSTANCE.loadExtractedJavaRepository(REPO_B);
//  }
//  
//  public static void compare() {
//    ExtractionComparisonChecker checker = new ExtractionComparisonChecker();
//    checker.processRepositories();
//  }
//  
//  private void processRepositories() {
//    TimeCounter mainTimer = new TimeCounter();
//
//    logger.info("Processing library jars...");
//    for (ExtractedJarFile jarA : repoA.getLibraryJarFiles()) {
//      examineJar(jarA);
//      mainTimer.increment();
//    }
//    mainTimer.logTimeAndCount(2, "library jars processed");
//    
//    logger.info("Processing project jars...");
//    for (ExtractedJarFile jarA : repoA.getProjectJarFiles()) {
//      examineJar(jarA);
//      mainTimer.increment();
//    }
//    mainTimer.logTimeAndCount(2, "project jars processed");
//    
//    mainTimer.logTotalTimeAndCount(2, "jars processed");
//  }
//  
//  private void examineJar(ExtractedJarFile jarA) {
//    logger.info("  Examining " + jarA);
//    ExtractedJarFile jarB = repoB.getJarFile(jarA.getProperties().HASH.getValue());
//    if (jarB == null) {
//      logger.log(Level.SEVERE, "Repo B is missing " + jarA);
//    } else {
//      logger.info("    Loading extracted info from Repository A");
//      ExtractedProject projectA = ExtractedProject.make(jarA);
//      logger.info("    Loading extracted info from Repository B");
//      ExtractedProject projectB = ExtractedProject.make(jarB);
//    
//      logger.info("    Comparing class files...");
//      {
//        TimeCounter timer = new TimeCounter();
//        Iterator<? extends ExtractedFile> aIter = projectA.getFiles().iterator();
//        Iterator<? extends ExtractedFile> bIter = projectB.getFiles().iterator();
//        ExtractedFile fileA = null;
//        ExtractedFile fileB = null;
//        while (aIter.hasNext() || bIter.hasNext()) {
//          if (fileA == null && aIter.hasNext()) {
//            fileA = aIter.next();
//          }
//          if (fileB == null && bIter.hasNext()) {
//            fileB = bIter.next();
//          }
//          if (fileA == null) {
//            logger.info("      Repo A is missing " + fileB.getClassFile());
//            fileB = null;
//          } else if (fileB == null) {
//            logger.info("      Repo B is missing " + fileA.getClassFile());
//            fileA = null;
//          } else {
//            int cmp = fileA.getClassFile().compareTo(fileB.getClassFile());
//            if (cmp == 0) {
//              fileA = null;
//              fileB = null;
//            } else if (cmp < 0) {
//              logger.info("      Repo B is missing " + fileA.getClassFile());
//              fileA = null;
//            } else {
//              logger.info("      Repo A is missing " + fileB.getClassFile());
//              fileB = null;
//            }
//          }
//          timer.increment();
//        }
//        timer.logTimeAndCount(6, "file(s) compared");
//      }
//      
//      logger.info("    Comparing entities...");
//      {
//        TimeCounter timer = new TimeCounter();
//        for (ExtractedFile fileA : projectA.getFiles()) {
//          ExtractedFile fileB = projectB.getMatchingFile(fileA);
//          if (fileB != null) {
//            EntitySet.destructiveCompare(new ComparisonMismatchReporter(5, "Examining " + fileA.getClassFile()), fileA.getEntitySet(), fileB.getEntitySet());
//            timer.increment();
//          }
//        }
//        timer.logTimeAndCount(6, "file(s') entities compared");
//      }
//      
//      logger.info("    Comparing relations...");
//      {
//        TimeCounter timer = new TimeCounter();
//        for (ExtractedFile fileA : projectA.getFiles()) {
//          ExtractedFile fileB = projectB.getMatchingFile(fileA);
//          if (fileB != null) {
//            RelationSet.destructiveCompare(new ComparisonMismatchReporter(5, "Examining " + fileA.getClassFile()), fileA.getRelationSet(), fileB.getRelationSet());
//            timer.increment();
//          }
//        }
//        timer.logTimeAndCount(6, "file(s') entities compared");
//      }
//      
//      logger.info("    Comparing local variables...");
//      {
//        TimeCounter timer = new TimeCounter();
//        for (ExtractedFile fileA : projectA.getFiles()) {
//          ExtractedFile fileB = projectB.getMatchingFile(fileA);
//          if (fileB != null) {
//            LocalVariableSet.destructiveCompare(new ComparisonMismatchReporter(5, "Examining " + fileA.getClassFile()), fileA.getLocalVariableSet(), fileB.getLocalVariableSet());
//            timer.increment();
//          }
//        }
//        timer.logTimeAndCount(6, "file(s') local variables compared");
//      }
//    }
//    
//  }
//}
