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
import java.util.Comparator;
import java.util.NoSuchElementException;
import java.util.TreeSet;
import java.util.logging.Level;

import edu.uci.ics.sourcerer.tools.java.model.extracted.MissingTypeEX;
import edu.uci.ics.sourcerer.tools.java.model.extracted.io.ReaderBundle;
import edu.uci.ics.sourcerer.tools.java.repo.model.JarFile;
import edu.uci.ics.sourcerer.tools.java.repo.model.JavaRepository;
import edu.uci.ics.sourcerer.tools.java.repo.model.JavaRepositoryFactory;
import edu.uci.ics.sourcerer.tools.java.repo.model.extracted.ExtractedJavaProject;
import edu.uci.ics.sourcerer.tools.java.repo.model.extracted.ExtractedJavaRepository;
import edu.uci.ics.sourcerer.tools.java.utilization.stats.SourcedFqnNode.Source;
import edu.uci.ics.sourcerer.util.Averager;
import edu.uci.ics.sourcerer.util.Counter;
import edu.uci.ics.sourcerer.util.CounterSet;
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
  
  public static void calculateJarCoverage() {
    TaskProgressLogger task = TaskProgressLogger.get();
    
    task.start("Calculating coverage by " + JAR_REPO.getValue().getPath() + " of missing/external imports from " + JavaRepositoryFactory.INPUT_REPO.getValue().getPath());
    
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
    
    task.start("Processing extracted projects for missing/external types", "projects processed", 1_000);
    // The number of projects with missing/external types
    int projectsWithTypes = 0;
    // Averager for missing/external FQNs per project
    Averager<Integer> missingFqns = Averager.create();
    // Averager for missing/external FQNs per project containing at least one
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
        projectsWithTypes++;
      }
      missingFqns.addValue(missingCount);
    }
    task.finish();
    
    task.finish();
    
    task.start("Reporting missing/external type information)
    task.start("Evaluating FQN coverage");
    TreeSet<SourcedFqnNode> sortedMissing = new TreeSet<>(new Comparator<SourcedFqnNode>() {
      @Override
      public int compare(SourcedFqnNode o1, SourcedFqnNode o2) {
        int cmp = Integer.compare(o1.getCount(Source.MISSING), o2.getCount(Source.MISSING));
        if (cmp == 0) {
          return o1.compareTo(o2);
        } else {
          return cmp;
        }
      }
    });
    CounterSet<String> counters = new CounterSet<>();
    CounterSet<String> filteredCounters = new CounterSet<>();
    for (SourcedFqnNode fqn : root.getPostOrderIterable()) {
      counters.increment(fqn.getSources().toString());
      // Add to the filtered counters only if it occurs in missing in more than one project
      if (fqn.getCount(Source.MISSING) > 1) {
        filteredCounters.increment(fqn.getSources().toString());
      }
      if (fqn.getCount(Source.MAVEN) == 0 && fqn.getCount(Source.MISSING) >= 1) {
        sortedMissing.add(fqn);
      }
    }
    task.finish();
    
    task.start("Reporting basic coverage");
    for (Counter<String> counter : counters.getCounters()) {
      task.report(counter.getObject() + " " + counter.getCount());
    }
    task.finish();
    
    task.start("Reporting filtered coverage");
    for (Counter<String> counter : filteredCounters.getCounters()) {
      task.report(counter.getObject() + " " + counter.getCount());
    }
    task.finish();
    
    task.start("Logging missing missing types listing");
    
    
    try (LogFileWriter writer = IOUtils.createLogFileWriter(MAVEN_MISSING_TYPES_LISTING)) {
      for (SourcedFqnNode fqn : sortedMissing.descendingSet()) {
        writer.write(fqn.getCount(Source.MISSING) + "\t" + fqn.getFqn());
      }
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Error writing file", e);
    }
    task.finish();
    
    task.finish();
  }
}
