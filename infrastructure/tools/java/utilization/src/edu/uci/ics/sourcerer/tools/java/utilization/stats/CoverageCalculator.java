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
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;

import com.google.common.collect.EnumMultiset;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

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
  public static final Argument<File> JAR_REPO = new FileArgument("jar-repo", "Jar repo");
  public static final Argument<File> SOURCED_CACHE = new RelativeFileArgument("sourced-cache", "sources-cache.txt", Arguments.CACHE, "Cache for sources prefix tree.");
  public static final Argument<File> MISSING_FQNS_PER_PROJECT = new RelativeFileArgument("missing-fqns-per-project", "missing-fqns-per-project.txt", Arguments.OUTPUT, "Summary of missing fqns per project");
  public static final Argument<File> PROJECTS_PER_MISSING_FQN = new RelativeFileArgument("projects-per-missing-fqn", "projects-per-missing-fqn.txt", Arguments.OUTPUT, "Summary of projects per missing fqn");
  
  public static void calculateJarCoverage() {
    TaskProgressLogger task = TaskProgressLogger.get();
    
    task.start("Calculating coverage by " + JAR_REPO.getValue().getPath() + " of missing imports from " + JavaRepositoryFactory.INPUT_REPO.getValue().getPath());
    
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
      task.start("Processing maven jars", "jars processed", 1_000);
      for (JarFile jar : jarRepo.getMavenJarFiles()) {
        for (String fqn : FileUtils.getClassFilesFromJar(jar.getFile().toFile())) {
          root.getChild(fqn, '/').addSource(Source.MAVEN);
        }
        task.progress();
      }
      task.finish();
    
      task.start("Processing project jars", "jars processed", 1_000);
      for (JarFile jar : jarRepo.getProjectJarFiles()) {
        for (String fqn : FileUtils.getClassFilesFromJar(jar.getFile().toFile())) {
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
    }    
    
    // Load the extracted repo with the missing types
    ExtractedJavaRepository repo = JavaRepositoryFactory.INSTANCE.loadExtractedJavaRepository(JavaRepositoryFactory.INPUT_REPO);
    
    NumberFormat format = NumberFormat.getNumberInstance();
    format.setMaximumFractionDigits(2);
    {
      task.start("Processing extracted projects for missing types", "projects processed", 1_000);
      // The number of projects with missing/external types
      int projectsWithMissingTypes = 0;
      // Averager for missing FQNs per project
      Averager<Integer> missingFqns = Averager.create();
      // Averager for missing FQNs per project containing at least one
      Averager<Integer> missingFqnsNonEmpty = Averager.create();
      for (ExtractedJavaProject project : repo.getProjects()) {
        task.progress();
        ReaderBundle bundle = new ReaderBundle(project.getExtractionDir().toFile());
        int missingCount = 0;
        for (MissingTypeEX missing : bundle.getTransientMissingTypes()) {
          root.getChild(missing.getFqn(), '.').addSource(Source.MISSING);
          missingCount++;
        }
        if (missingCount > 0) {
          missingFqnsNonEmpty.addValue(missingCount);
          projectsWithMissingTypes++;
        }
        missingFqns.addValue(missingCount);
      }
      task.finish();
    
      task.finish();
      
      
      Averager<Integer> projectsPerFQN = Averager.create();
      for (SourcedFqnNode fqn : root.getPreOrderIterable()) {
        if (fqn.getCount(Source.MISSING) > 0) {
          projectsPerFQN.addValue(fqn.getCount(Source.MISSING));
        }
      }
      
      Percenterator percent = Percenterator.create(repo.getProjectCount());
      task.start("Reporting missing type information");
      task.report(percent.format(projectsWithMissingTypes) + " projects with missing types");
      task.report(format.format(missingFqns.getMean()) + " (" + format.format(missingFqns.getStandardDeviation()) + ") missing FQNs per project, on average");
      task.report(format.format(missingFqnsNonEmpty.getMean()) + " (" + format.format(missingFqnsNonEmpty.getStandardDeviation()) + ") missing FQNs per project containing at least one missing FQN, on average");
      task.finish();
      
      missingFqns.writeValueMap(MISSING_FQNS_PER_PROJECT.getValue());
      projectsPerFQN.writeValueMap(PROJECTS_PER_MISSING_FQN.getValue());
    }
    
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
    
    
    
    for (int threshold : new int[] { 1, 2, 10, 50, 100}) {
      Multiset<String> uniqueByString = HashMultiset.create(6);
      Multiset<String> totalByString = HashMultiset.create(6);
            
      int uniqueTotal = 0;
      int totalTotal = 0;
      for (SourcedFqnNode node : root.getPostOrderIterable()) {
        int missingCount = node.getCount(Source.MISSING);
        if (missingCount >= threshold) {
          uniqueTotal++;
          totalTotal += missingCount;
          int mavenCount = node.getCount(Source.MAVEN);
          int projectCount = node.getCount(Source.PROJECT);
          if (mavenCount > 0) {
            uniqueByString.add("Maven");
            totalByString.add("Maven", missingCount);
            if (projectCount == 0) {
              uniqueByString.add("Maven only");
              totalByString.add("Maven only", missingCount);
            } else {
              uniqueByString.add("Project");
              totalByString.add("Project", missingCount);
              uniqueByString.add("Maven and Project");
              totalByString.add("Maven and Project", missingCount);
            }
          } else if (projectCount > 0) {
            uniqueByString.add("Project");
            totalByString.add("Project", missingCount);
            uniqueByString.add("Project only");
            totalByString.add("Project only", missingCount);
          } else {
            uniqueByString.add("Nothing");
            totalByString.add("Nothing", missingCount);
          }
        }
      }
      
      Percenterator uniqueP = Percenterator.create(uniqueTotal);
      Percenterator totalP = Percenterator.create(totalTotal);

      task.start("Reporting FQN missing type coverage for threshold " + threshold);
      for (String condition : uniqueByString.elementSet()) {
        task.report(condition + ":");
        task.report("  Unique: " + uniqueP.format(uniqueByString.count(condition)));
        task.report("  Total:  " + totalP.format(totalByString.count(condition)));
      }
      task.report("Sum:");
      task.report("  Unique: " + uniqueTotal);
      task.report("  Total: " + totalTotal);
      task.finish();
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
