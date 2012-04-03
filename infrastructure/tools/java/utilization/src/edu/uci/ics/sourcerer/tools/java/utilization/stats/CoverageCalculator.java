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
package edu.uci.ics.sourcerer.tools.java.utilization.stats;

import static edu.uci.ics.sourcerer.util.io.logging.Logging.logger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;

import com.google.common.collect.EnumMultiset;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

import edu.uci.ics.sourcerer.tools.java.model.extracted.ImportEX;
import edu.uci.ics.sourcerer.tools.java.model.extracted.MissingTypeEX;
import edu.uci.ics.sourcerer.tools.java.model.extracted.io.ReaderBundle;
import edu.uci.ics.sourcerer.tools.java.repo.model.JarFile;
import edu.uci.ics.sourcerer.tools.java.repo.model.JavaRepository;
import edu.uci.ics.sourcerer.tools.java.repo.model.JavaRepositoryFactory;
import edu.uci.ics.sourcerer.tools.java.repo.model.extracted.ExtractedJavaProject;
import edu.uci.ics.sourcerer.tools.java.repo.model.extracted.ExtractedJavaRepository;
import edu.uci.ics.sourcerer.tools.java.utilization.stats.SourcedFqnNode.Source;
import edu.uci.ics.sourcerer.util.Averager;
import edu.uci.ics.sourcerer.util.Percenterator;
import edu.uci.ics.sourcerer.util.io.FileUtils;
import edu.uci.ics.sourcerer.util.io.IOUtils;
import edu.uci.ics.sourcerer.util.io.LogFileWriter;
import edu.uci.ics.sourcerer.util.io.arguments.Argument;
import edu.uci.ics.sourcerer.util.io.arguments.Arguments;
import edu.uci.ics.sourcerer.util.io.arguments.FileArgument;
import edu.uci.ics.sourcerer.util.io.arguments.RelativeFileArgument;
import edu.uci.ics.sourcerer.util.io.logging.TaskProgressLogger;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class CoverageCalculator {
  public static final Argument<File> EXTERNAL_REPO = new FileArgument("external-repo", "External repo");
  public static final Argument<File> MISSING_REPO = new FileArgument("external-repo", "Missing repo");
  public static final Argument<File> JAR_REPO = new FileArgument("jar-repo", "Jar repo");
  public static final Argument<File> SOURCED_CACHE = new RelativeFileArgument("sourced-cache", "sourced-cache.txt", Arguments.CACHE, "Cache for sources prefix tree.");
//  public static final Argument<File> MISSING_FQNS_PER_PROJECT = new RelativeFileArgument("missing-fqns-per-project", "missing-fqns-per-project.txt", Arguments.OUTPUT, "Summary of missing fqns per project");
//  public static final Argument<File> PROJECTS_PER_MISSING_FQN = new RelativeFileArgument("projects-per-missing-fqn", "projects-per-missing-fqn.txt", Arguments.OUTPUT, "Summary of projects per missing fqn");
  
  public static void calculateJarCoverage() {
    TaskProgressLogger task = TaskProgressLogger.get();
    
    task.start("Calculating coverage by " + JAR_REPO.getValue().getPath() + " of external imports from " + EXTERNAL_REPO.getValue() + " and missing imports from " + MISSING_REPO.getValue());
    
    // Load the jar repo
    JavaRepository jarRepo = JavaRepositoryFactory.INSTANCE.loadJavaRepository(JAR_REPO);
    
    task.start("Populating the prefix tree");
    SourcedFqnNode root = SourcedFqnNode.createRoot();

    boolean loaded = false;
    if (SOURCED_CACHE.getValue().exists()) {
      task.start("Loading cache");
      try (BufferedReader reader = IOUtils.makeBufferedReader(SOURCED_CACHE.getValue())) {
        root.createLoader().load(reader);
        loaded = true;
      } catch (IOException | NoSuchElementException e) {
        logger.log(Level.SEVERE, "Error loading cache", e);
        root = SourcedFqnNode.createRoot();
      }
      task.finish();
    }
    if (!loaded) {
      int nonEmptyMaven = 0;
      task.start("Processing maven jars", "jars processed", 10_000);
      for (JarFile jar : jarRepo.getMavenJarFiles()) {
        boolean go = true;
        for (String fqn : FileUtils.getClassFilesFromJar(jar.getFile().toFile())) {
          if (go) {
            nonEmptyMaven++;
            go = false;
          }
          root.getChild(fqn, '/').addSource(Source.MAVEN);
        }
        task.progress();
      }
      task.finish();
    
      int nonEmptyProject = 0;
      task.start("Processing project jars", "jars processed", 10_000);
      for (JarFile jar : jarRepo.getProjectJarFiles()) {
        boolean go = true;
        for (String fqn : FileUtils.getClassFilesFromJar(jar.getFile().toFile())) {
          if (go) {
            nonEmptyProject++;
            go = false;
          }
          root.getChild(fqn, '/').addSource(Source.PROJECT);
        }
        task.progress();
      }
      task.finish();
      
      // Save the prefix tree
      task.start("Saving prefix tree cache");
      try (BufferedWriter writer = IOUtils.makeBufferedWriter(FileUtils.ensureWriteable(SOURCED_CACHE.getValue()))) {
        root.createSaver().save(writer);
      } catch (IOException e) {
        logger.log(Level.SEVERE, "Error writing log", e);
        FileUtils.delete(SOURCED_CACHE.getValue());
      }
      task.finish();
      
      int mavenClassFiles = 0;
      int projectClassFiles = 0;
      int mavenUnique = 0;
      int projectUnique = 0;
      Set<SourcedFqnNode> mavenPackages = new HashSet<>();
      Set<SourcedFqnNode> projectPackages = new HashSet<>();
      for (SourcedFqnNode node : root.getPostOrderIterable()) {
        if (node.has(Source.MAVEN)) {
          mavenClassFiles += node.getCount(Source.MAVEN);
          mavenUnique++;
          mavenPackages.add(node.getParent());
        }
        if (node.has(Source.PROJECT)) {
          projectClassFiles += node.getCount(Source.PROJECT);
          projectUnique++;
          projectPackages.add(node.getParent());
        }
      }
      task.start("Reporting statistics on jars");
      task.start("Maven");
      task.report(nonEmptyMaven + " non-empty jars");
      task.report(mavenClassFiles + " class files");
      task.report(mavenUnique + " unique types");
      task.report(mavenPackages.size() + " packages");
      task.finish();
      task.start("Project");
      task.report(nonEmptyProject + " non-empty jars");
      task.report(projectClassFiles + " class files");
      task.report(projectUnique + " unique types");
      task.report(projectPackages.size() + " packages");
      task.finish();
      task.finish();
    }
    
    // Load the external repo
    ExtractedJavaRepository externalRepo = JavaRepositoryFactory.INSTANCE.loadExtractedJavaRepository(EXTERNAL_REPO);
    // load the missing repo
    ExtractedJavaRepository missingRepo = JavaRepositoryFactory.INSTANCE.loadExtractedJavaRepository(MISSING_REPO);
    
    NumberFormat format = NumberFormat.getNumberInstance();
    format.setMaximumFractionDigits(2);
    {
      task.start("Processing extracted projects for missing and external types", "projects processed", 10_000);
      // Averager for external FQNs per project
      Averager<Integer> externalFqns = Averager.create();
      // Averager for missing FQNs per project
      Averager<Integer> missingFqns = Averager.create();
      for (ExtractedJavaProject externalProject : externalRepo.getProjects()) {
        ExtractedJavaProject missingProject = missingRepo.getProject(externalProject.getLocation());
        
        ReaderBundle externalBundle = new ReaderBundle(externalProject.getExtractionDir().toFile());
        ReaderBundle missingBundle = new ReaderBundle(missingProject.getExtractionDir().toFile());
        
        int externalCount = 0;
        int missingCount = 0;
        
        // Add all the imports for this project
        for (ImportEX imp : externalBundle.getTransientImports()) {
          root.getChild(imp.getImported(), '.').addSource(Source.IMPORTED);
        }
        
        Set<String> validMissing = new HashSet<>();
        // Add the external types
        for (MissingTypeEX missing : externalBundle.getTransientMissingTypes()) {
          validMissing.add(missing.getFqn());
          root.getChild(missing.getFqn(), '.').addSource(Source.EXTERNAL);
          externalCount++;
        }
        
        // Add the missing types
        for (MissingTypeEX missing : missingBundle.getTransientMissingTypes()) {
          if (validMissing.contains(missing.getFqn())) {
            root.getChild(missing.getFqn(), '.').addSource(Source.MISSING);
            missingCount++;
          }
        }
        
        externalFqns.addValue(externalCount);
        missingFqns.addValue(missingCount);
        
        task.progress();
      }
      task.finish();
      
//      Averager<Integer> projectsPerFQN = Averager.create();
//      for (SourcedFqnNode fqn : root.getPreOrderIterable()) {
//        if (fqn.getCount(Source.MISSING) > 0) {
//          projectsPerFQN.addValue(fqn.getCount(Source.MISSING));
//        }
//      }
            
      Percenterator percent = Percenterator.create(externalRepo.getProjectCount());
      task.start("Reporting missing type information");
      task.report(percent.format(externalFqns.getNonZeroCount()) + " projects with external types");
      task.report(percent.format(missingFqns.getNonZeroCount()) + " projects with missing types");
      task.report(format.format(externalFqns.getMean()) + " (" + format.format(externalFqns.getStandardDeviation()) + ") imported external types per project, on average");
      task.report(format.format(externalFqns.getNonZeroMean()) + " (" + format.format(externalFqns.getNonZeroStandardDeviation()) + ") imported external types per project containing at least one external type, on average");
      task.report(format.format(missingFqns.getMean()) + " (" + format.format(missingFqns.getStandardDeviation()) + ") imported missing types per project, on average");
      task.report(format.format(missingFqns.getNonZeroMean()) + " (" + format.format(missingFqns.getNonZeroStandardDeviation()) + ") missing FQNs per project containing at least one missing FQN, on average");
      task.finish();
      
//      missingFqns.writeValueMap(MISSING_FQNS_PER_PROJECT.getValue());
//      projectsPerFQN.writeValueMap(PROJECTS_PER_MISSING_FQN.getValue());
    }
    
    // Report general statistics
    {
      int uniqueTotal = 0;
      Multiset<Source> uniqueByType = EnumMultiset.create(Source.class);
      Multiset<Source> totalByType = EnumMultiset.create(Source.class);
      for (SourcedFqnNode node : root.getPostOrderIterable()) {
        if (node.hasSource()) {
          uniqueTotal++;
        }
        for (Source source : Source.values()) {
          int count = node.getCount(source);
          if (count > 0) {
            uniqueByType.add(source);
            totalByType.add(source, count);
          }
        }
      }
      
      Percenterator uniqueP = Percenterator.create(uniqueTotal);
      
      task.start("Reporting FQN counts broken down by source");
      for (Source source : Source.values()) {
        task.report(source.name() + ":");
        task.report("  Unique: " + uniqueP.format(uniqueByType.count(source)));
        task.report("  Total:  " + totalByType.count(source));
      }
      task.report("Sum:");
      task.report("  Unique: " + uniqueTotal);
      task.finish();
    }
    
    // Identify the most popular imported types and packages
    {
      for (final Source source : EnumSet.of(Source.IMPORTED, Source.EXTERNAL, Source.MISSING)) {
        {
          TreeSet<SourcedFqnNode> popularTypes = new TreeSet<>(new Comparator<SourcedFqnNode>() {
            @Override
            public int compare(SourcedFqnNode o1, SourcedFqnNode o2) {
              int cmp = Integer.compare(o1.getCount(source), o2.getCount(source));
              if (cmp == 0) {
                return o1.compareTo(o2);
              } else {
                return cmp;
              }
            }
          });
          
          for (SourcedFqnNode fqn : root.getPostOrderIterable()) {
            if (fqn.has(source)) {
              popularTypes.add(fqn);
            }
          }
          
          task.start("Logging popular types listing for " + source.name());
          try (LogFileWriter writer = IOUtils.createLogFileWriter(new File(Arguments.OUTPUT.getValue(), source.name() + "-popular-types.txt"))) {
            for (SourcedFqnNode fqn : popularTypes.descendingSet()) {
              writer.write(fqn.getCount(source) + "\t" + fqn.getFqn());
            }
          } catch (IOException e) {
            logger.log(Level.SEVERE, "Error writing file", e);
          }
          task.finish();
        }
        
        {
          final Multiset<SourcedFqnNode> packages = HashMultiset.create();
          
          for (SourcedFqnNode fqn : root.getPostOrderIterable()) {
            if (fqn.has(source)) {
              packages.add(fqn.getParent(), fqn.getCount(source));
            }
          }
          
          List<SourcedFqnNode> sorted = new ArrayList<>(packages);
          Collections.sort(sorted, new Comparator<SourcedFqnNode>() {
            @Override
            public int compare(SourcedFqnNode o1, SourcedFqnNode o2) {
              int cmp = -Integer.compare(packages.count(o1), packages.count(o2));
              if (cmp == 0) {
                return o1.compareTo(o2);
              } else {
                return cmp;
              }
            }});
          
          task.start("Logging popular packages listing for " + source.name());
          try (LogFileWriter writer = IOUtils.createLogFileWriter(new File(Arguments.OUTPUT.getValue(), source.name() + "-popular-packages.txt"))) {
            for (SourcedFqnNode fqn : sorted) {
              writer.write(packages.count(fqn) + "\t" + fqn.getFqn());
            }
          } catch (IOException e) {
            logger.log(Level.SEVERE, "Error writing file", e);
          }
          task.finish();
        }
      }
    }
    
    // Identify the most popular imported type found in maven, project and nothing
    {
      for (final Source importSource : EnumSet.of(Source.IMPORTED, Source.EXTERNAL, Source.MISSING)) {
        for (final Source typeSource : EnumSet.of(Source.MAVEN, Source.PROJECT)) {
          {
            TreeSet<SourcedFqnNode> popularTypes = new TreeSet<>(new Comparator<SourcedFqnNode>() {
              @Override
              public int compare(SourcedFqnNode o1, SourcedFqnNode o2) {
                int cmp = Integer.compare(o1.getCount(importSource), o2.getCount(importSource));
                if (cmp == 0) {
                  return o1.compareTo(o2);
                } else {
                  return cmp;
                }
              }
            });
            
            for (SourcedFqnNode fqn : root.getPostOrderIterable()) {
              if (fqn.has(importSource) && fqn.has(typeSource)) {
                popularTypes.add(fqn);
              }
            }
            
            task.start("Logging popular types listing for " + importSource.name() + " and " + typeSource.name());
            try (LogFileWriter writer = IOUtils.createLogFileWriter(new File(Arguments.OUTPUT.getValue(), importSource.name() + "-" + typeSource.name() + "-popular-types.txt"))) {
              for (SourcedFqnNode fqn : popularTypes.descendingSet()) {
                writer.write(fqn.getCount(importSource) + "\t" + fqn.getFqn());
              }
            } catch (IOException e) {
              logger.log(Level.SEVERE, "Error writing file", e);
            }
            task.finish();
          }
          {
            final Multiset<SourcedFqnNode> packages = HashMultiset.create();
            
            for (SourcedFqnNode fqn : root.getPostOrderIterable()) {
              if (fqn.has(importSource) && fqn.has(typeSource)) {
                packages.add(fqn.getParent(), fqn.getCount(importSource));
              }
            }
            
            List<SourcedFqnNode> sorted = new ArrayList<>(packages);
            Collections.sort(sorted, new Comparator<SourcedFqnNode>() {
              @Override
              public int compare(SourcedFqnNode o1, SourcedFqnNode o2) {
                int cmp = -Integer.compare(packages.count(o1), packages.count(o2));
                if (cmp == 0) {
                  return o1.compareTo(o2);
                } else {
                  return cmp;
                }
              }});
            
            task.start("Logging popular packages listing for " + importSource.name() + " and " + typeSource.name());
            try (LogFileWriter writer = IOUtils.createLogFileWriter(new File(Arguments.OUTPUT.getValue(), importSource.name() + "-" + typeSource.name() + "-popular-packages.txt"))) {
              for (SourcedFqnNode fqn : sorted) {
                writer.write(packages.count(fqn) + "\t" + fqn.getFqn());
              }
            } catch (IOException e) {
              logger.log(Level.SEVERE, "Error writing file", e);
            }
            task.finish();
          }
        }
      }
    }
    
    for (int threshold : new int[] { 1, 2, 10, 50, 100}) {
      Multiset<String> externalUniqueByString = HashMultiset.create(6);
      Multiset<String> externalTotalByString = HashMultiset.create(6);
      Multiset<String> missingUniqueByString = HashMultiset.create(6);
      Multiset<String> missingTotalByString = HashMultiset.create(6);
            
      int externalUniqueTotal = 0;
      int externalTotalTotal = 0;
      int missingUniqueTotal = 0;
      int missingTotalTotal = 0;
      for (SourcedFqnNode node : root.getPostOrderIterable()) {
        int externalCount = node.getCount(Source.EXTERNAL);
        if (externalCount >= threshold) {
          externalUniqueTotal++;
          externalTotalTotal += externalCount;
          int mavenCount = node.getCount(Source.MAVEN);
          int projectCount = node.getCount(Source.PROJECT);
          if (mavenCount > 0) {
            externalUniqueByString.add("Maven");
            externalTotalByString.add("Maven", externalCount);
            if (projectCount == 0) {
              externalUniqueByString.add("Maven only");
              externalTotalByString.add("Maven only", externalCount);
            } else {
              externalUniqueByString.add("Project");
              externalTotalByString.add("Project", externalCount);
              externalUniqueByString.add("Maven and Project");
              externalTotalByString.add("Maven and Project", externalCount);
            }
          } else if (projectCount > 0) {
            externalUniqueByString.add("Project");
            externalTotalByString.add("Project", externalCount);
            externalUniqueByString.add("Project only");
            externalTotalByString.add("Project only", externalCount);
          } else {
            externalUniqueByString.add("Nothing");
            externalTotalByString.add("Nothing", externalCount);
          }
        }
        
        int missingCount = node.getCount(Source.MISSING);
        if (missingCount >= threshold) {
          missingUniqueTotal++;
          missingTotalTotal += missingCount;
          int mavenCount = node.getCount(Source.MAVEN);
          int projectCount = node.getCount(Source.PROJECT);
          if (mavenCount > 0) {
            missingUniqueByString.add("Maven");
            missingTotalByString.add("Maven", missingCount);
            if (projectCount == 0) {
              missingUniqueByString.add("Maven only");
              missingTotalByString.add("Maven only", missingCount);
            } else {
              missingUniqueByString.add("Project");
              missingTotalByString.add("Project", missingCount);
              missingUniqueByString.add("Maven and Project");
              missingTotalByString.add("Maven and Project", missingCount);
            }
          } else if (projectCount > 0) {
            missingUniqueByString.add("Project");
            missingTotalByString.add("Project", missingCount);
            missingUniqueByString.add("Project only");
            missingTotalByString.add("Project only", missingCount);
          } else {
            missingUniqueByString.add("Nothing");
            missingTotalByString.add("Nothing", missingCount);
          }
        }
      }
      
      Percenterator externalUniqueP = Percenterator.create(externalUniqueTotal);
      Percenterator missingUniqueP = Percenterator.create(missingUniqueTotal);
      Percenterator externalTotalP = Percenterator.create(externalTotalTotal);
      Percenterator missingTotalP = Percenterator.create(externalTotalTotal);

      task.start("Reporting external import coverage for threshold " + threshold);
      for (String condition : externalUniqueByString.elementSet()) {
        task.report(condition + ":");
        task.report("  Unique: " + externalUniqueP.format(externalUniqueByString.count(condition)));
        task.report("  Total:  " + externalTotalP.format(externalTotalByString.count(condition)));
      }
      task.report("Sum:");
      task.report("  Unique: " + externalUniqueTotal);
      task.report("  Total: " + externalTotalTotal);
      task.finish();
      task.start("Reporting missing import coverage for threshold " + threshold);
      for (String condition : missingUniqueByString.elementSet()) {
        task.report(condition + ":");
        task.report("  Unique: " + missingUniqueP.format(missingUniqueByString.count(condition)));
        task.report("  Total:  " + missingTotalP.format(missingTotalByString.count(condition)));
      }
      task.report("Sum:");
      task.report("  Unique: " + missingUniqueTotal);
      task.report("  Total: " + missingTotalTotal);
      task.finish();
    }

    {
      // Find all the most popular fqns per source
      for (final Source source : Source.values()) {
        TreeSet<SourcedFqnNode> sorted = new TreeSet<>(new Comparator<SourcedFqnNode>() {
          @Override
          public int compare(SourcedFqnNode o1, SourcedFqnNode o2) {
            int cmp = Integer.compare(o1.getCount(source), o2.getCount(source));
            if (cmp == 0) {
              return o1.compareTo(o2);
            } else {
              return cmp;
            }
          }
        });
        
        for (SourcedFqnNode node : root.getPostOrderIterable()) {
          if (node.has(source)) {
            sorted.add(node);
          }
        }
        
        task.start("Logging popular types listing for " + source.name());
        try (LogFileWriter writer = IOUtils.createLogFileWriter(new File(Arguments.OUTPUT.getValue(), source.name() + "-popular.txt"))) {
          for (SourcedFqnNode fqn : sorted.descendingSet()) {
            writer.write(fqn.getCount(source) + "\t" + fqn.getFqn());
          }
        } catch (IOException e) {
          logger.log(Level.SEVERE, "Error writing file", e);
        }
        task.finish();
      }
    }
    
    {
      // Find all the fqns unique to that source
      for (final Source source : Source.values()) {
        TreeSet<SourcedFqnNode> sorted = new TreeSet<>(new Comparator<SourcedFqnNode>() {
          @Override
          public int compare(SourcedFqnNode o1, SourcedFqnNode o2) {
            int cmp = Integer.compare(o1.getCount(source), o2.getCount(source));
            if (cmp == 0) {
              return o1.compareTo(o2);
            } else {
              return cmp;
            }
          }
        });
        
        Set<Source> expected = EnumSet.of(Source.MISSING, source);
        for (SourcedFqnNode node : root.getPostOrderIterable()) {
          Set<Source> sources = node.getSources();
          if (sources.containsAll(expected) && expected.containsAll(sources)) {
            sorted.add(node);
          }
        }
        
        task.start("Logging missing types listing");
        try (LogFileWriter writer = IOUtils.createLogFileWriter(new File(Arguments.OUTPUT.getValue(), source.name() + "-missing.txt"))) {
          for (SourcedFqnNode fqn : sorted.descendingSet()) {
            writer.write(fqn.getCount(Source.MISSING) + "\t" + fqn.getFqn());
          }
        } catch (IOException e) {
          logger.log(Level.SEVERE, "Error writing file", e);
        }
        task.finish();
      }
    }
    
    {
      final Multiset<SourcedFqnNode> maven = HashMultiset.create();
      final Multiset<SourcedFqnNode> project = HashMultiset.create();
      final Multiset<SourcedFqnNode> mavenProject= HashMultiset.create();
      final Multiset<SourcedFqnNode> missing = HashMultiset.create();
      
      
      // Find the package specific info
      for (SourcedFqnNode node : root.getPostOrderIterable()) {
        int missingCount = node.getCount(Source.MISSING);
        if (missingCount > 0) {
          int mavenCount = node.getCount(Source.MAVEN);
          int projectCount = node.getCount(Source.PROJECT);
          if (mavenCount > 0) {
            if (projectCount == 0) {
              maven.add(node.getParent());
            } else {
              mavenProject.add(node.getParent());
            }
          } else if (projectCount > 0) {
            project.add(node.getParent());
          } else {
            missing.add(node.getParent());
          }
        }
      }
      
      task.start("Reporting package breakdown");
      task.report("Maven only:        " + maven.elementSet().size());
      task.report("Project only:      " + project.elementSet().size());
      task.report("Maven and Project: " + mavenProject.elementSet().size());
      task.report("Missing:           " + missing.elementSet().size());
      task.finish();
      
      task.start("Logging package popularity");
      // Maven
      SourcedFqnNode[] nodes = maven.elementSet().toArray(new SourcedFqnNode[maven.elementSet().size()]);
      Arrays.sort(nodes, new Comparator<SourcedFqnNode>() {
        @Override
        public int compare(SourcedFqnNode o1, SourcedFqnNode o2) {
          int cmp = Integer.compare(maven.count(o2), maven.count(o1));
          if (cmp == 0) {
            return o1.compareTo(o2);
          } else {
            return cmp;
          }
        }
      });
      try (LogFileWriter writer = IOUtils.createLogFileWriter(new File(Arguments.OUTPUT.getValue(), "maven-pkgs.txt"))) {
        for (SourcedFqnNode pkg : nodes) {
          writer.write(maven.count(pkg) + "\t" + pkg.getFqn());
        }
      } catch (IOException e) {
        logger.log(Level.SEVERE, "Error writing file", e);
      }
      
      // Project
      nodes = project.elementSet().toArray(new SourcedFqnNode[project.elementSet().size()]);
      Arrays.sort(nodes, new Comparator<SourcedFqnNode>() {
        @Override
        public int compare(SourcedFqnNode o1, SourcedFqnNode o2) {
          int cmp = Integer.compare(project.count(o2), project.count(o1));
          if (cmp == 0) {
            return o1.compareTo(o2);
          } else {
            return cmp;
          }
        }
      });
      try (LogFileWriter writer = IOUtils.createLogFileWriter(new File(Arguments.OUTPUT.getValue(), "project-pkgs.txt"))) {
        for (SourcedFqnNode pkg : nodes) {
          writer.write(project.count(pkg) + "\t" + pkg.getFqn());
        }
      } catch (IOException e) {
        logger.log(Level.SEVERE, "Error writing file", e);
      }
      
      // Maven/Project
      nodes = mavenProject.elementSet().toArray(new SourcedFqnNode[mavenProject.elementSet().size()]);
      Arrays.sort(nodes, new Comparator<SourcedFqnNode>() {
        @Override
        public int compare(SourcedFqnNode o1, SourcedFqnNode o2) {
          int cmp = Integer.compare(mavenProject.count(o2), mavenProject.count(o1));
          if (cmp == 0) {
            return o1.compareTo(o2);
          } else {
            return cmp;
          }
        }
      });
      try (LogFileWriter writer = IOUtils.createLogFileWriter(new File(Arguments.OUTPUT.getValue(), "maven-project-pkgs.txt"))) {
        for (SourcedFqnNode pkg : nodes) {
          writer.write(mavenProject.count(pkg) + "\t" + pkg.getFqn());
        }
      } catch (IOException e) {
        logger.log(Level.SEVERE, "Error writing file", e);
      }
      
      nodes = missing.elementSet().toArray(new SourcedFqnNode[missing.elementSet().size()]);
      Arrays.sort(nodes, new Comparator<SourcedFqnNode>() {
        @Override
        public int compare(SourcedFqnNode o1, SourcedFqnNode o2) {
          int cmp = Integer.compare(missing.count(o2), missing.count(o1));
          if (cmp == 0) {
            return o1.compareTo(o2);
          } else {
            return cmp;
          }
        }
      });
      try (LogFileWriter writer = IOUtils.createLogFileWriter(new File(Arguments.OUTPUT.getValue(), "missing-pkgs.txt"))) {
        for (SourcedFqnNode pkg : nodes) {
          writer.write(missing.count(pkg) + "\t" + pkg.getFqn());
        }
      } catch (IOException e) {
        logger.log(Level.SEVERE, "Error writing file", e);
      }
      task.finish();
    }
    
    task.finish();
  }
}
