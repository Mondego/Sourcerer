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
package edu.uci.ics.sourcerer.tools.java.component.identifier.stats;

import static edu.uci.ics.sourcerer.util.io.logging.Logging.logger;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.TreeSet;
import java.util.logging.Level;

import edu.uci.ics.sourcerer.tools.java.model.extracted.ImportEX;
import edu.uci.ics.sourcerer.tools.java.model.extracted.MissingTypeEX;
import edu.uci.ics.sourcerer.tools.java.model.extracted.io.ReaderBundle;
import edu.uci.ics.sourcerer.tools.java.repo.model.JavaRepositoryFactory;
import edu.uci.ics.sourcerer.tools.java.repo.model.extracted.ExtractedJavaProject;
import edu.uci.ics.sourcerer.tools.java.repo.model.extracted.ExtractedJavaRepository;
import edu.uci.ics.sourcerer.util.io.IOUtils;
import edu.uci.ics.sourcerer.util.io.LogFileWriter;
import edu.uci.ics.sourcerer.util.io.arguments.Argument;
import edu.uci.ics.sourcerer.util.io.arguments.Arguments;
import edu.uci.ics.sourcerer.util.io.arguments.RelativeFileArgument;
import edu.uci.ics.sourcerer.util.io.logging.TaskProgressLogger;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class PopularityCalculator {
  public static final Argument<File> IMPORT_LIST = new RelativeFileArgument("imports-list", "imports.txt", Arguments.OUTPUT, "List of imports, ordered by popularity.");
  public static final Argument<File> MISSING_TYPES_LIST = new RelativeFileArgument("missing-types-list", "missing-types.txt", Arguments.OUTPUT, "List of missing types, ordered by popularity.");
  
  public static CountingFqnNode calculateImportPopularity() {
    TaskProgressLogger task = TaskProgressLogger.get();
    
    task.start("Calculating import popularity");
    
    ExtractedJavaRepository repo = JavaRepositoryFactory.INSTANCE.loadExtractedJavaRepository(JavaRepositoryFactory.INPUT_REPO);
    
    task.start("Processing projects for imports", "projects processed", 500);
    CountingFqnNode root = CountingFqnNode.createRoot();
    for (ExtractedJavaProject project : repo.getProjects()) {
      ReaderBundle bundle = ReaderBundle.create(project.getExtractionDir().toFile(), project.getCompressedFile().toFile());
      for (ImportEX imp : bundle.getTransientImports()) {
        root.add(imp.getImported(), project.getLocation().toString());
      }
      task.progress();
    }
    task.finish();
    
    task.finish();
    
    return root;
  }
  
  public static void printImportPopularity() {
    TaskProgressLogger task = TaskProgressLogger.get();
    
    CountingFqnNode root = calculateImportPopularity();
    
    task.start("Sorting FQNs by total import count");
    TreeSet<CountingFqnNode> sorted = new TreeSet<>(new Comparator<CountingFqnNode>() {
      @Override
      public int compare(CountingFqnNode o1, CountingFqnNode o2) {
        int cmp = Integer.compare(o1.getTotalCount(), o2.getTotalCount());
        if (cmp == 0) {
          return Integer.compare(o1.hashCode(), o2.hashCode());
        } else {
          return cmp;
        }
      }});
    for (CountingFqnNode node : root.getPostOrderIterable()) {
      sorted.add(node);
    }
    task.finish();
    
    task.start("Writing sorted imports to file");
    try (LogFileWriter writer = IOUtils.createLogFileWriter(new File(Arguments.OUTPUT.getValue(), "imports-by-total-count"))) {
      for (CountingFqnNode node : sorted.descendingSet()) {
        writer.write(node.getTotalCount() + " " + node.getProjectCount() + " " + node.getFqn());
      }
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Error writing imports to file", e);
    }
    task.finish();
    
    task.start("Sorting FQNs by project import count");
    sorted = new TreeSet<>(new Comparator<CountingFqnNode>() {
      @Override
      public int compare(CountingFqnNode o1, CountingFqnNode o2) {
        int cmp = Integer.compare(o1.getProjectCount(), o2.getProjectCount());
        if (cmp == 0) {
          return Integer.compare(o1.hashCode(), o2.hashCode());
        } else {
          return cmp;
        }
      }});
    for (CountingFqnNode node : root.getPostOrderIterable()) {
      if (node.getTotalCount() > 0) {
        sorted.add(node);
      }
    }
    task.finish();
    
    task.start("Writing sorted imports to file");
    try (LogFileWriter writer = IOUtils.createLogFileWriter(IMPORT_LIST)) {
      for (CountingFqnNode node : sorted.descendingSet()) {
        writer.write(node.getTotalCount() + " " + node.getProjectCount() + " " + node.getFqn());
      }
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Error writing imports to file", e);
    }
    task.finish();
  }
  
  public static void calculateMissingTypePopularity() {
    TaskProgressLogger task = TaskProgressLogger.get();
    
    task.start("Loading repository");
    ExtractedJavaRepository repo = JavaRepositoryFactory.INSTANCE.loadExtractedJavaRepository(JavaRepositoryFactory.INPUT_REPO);
    task.finish();

    int count = 0;
    task.start("Processing extracted projects for missing types", "projects processed", 500);
    CountingFqnNode root = CountingFqnNode.createRoot();
    for (ExtractedJavaProject project : repo.getProjects()) {
      task.progress();
      ReaderBundle bundle = ReaderBundle.create(project.getExtractionDir().toFile(), project.getCompressedFile().toFile());
      for (MissingTypeEX missing : bundle.getTransientMissingTypes()) {
        count++;
        root.add(missing.getFqn(), project.getLocation().toString());
      }
    }
    task.finish();
    
    task.report(count + " types processed.");
    
    task.start("Sorting FQNs by total count");
    TreeSet<CountingFqnNode> sorted = new TreeSet<>(new Comparator<CountingFqnNode>() {
      @Override
      public int compare(CountingFqnNode o1, CountingFqnNode o2) {
        int cmp = Integer.compare(o1.getProjectCount(), o2.getProjectCount());
        if (cmp == 0) {
          return Integer.compare(o1.hashCode(), o2.hashCode());
        } else {
          return cmp;
        }
      }});
    for (CountingFqnNode node : root.getPostOrderIterable()) {
      if (node.getTotalCount() > 0) {
        sorted.add(node);
      }
    }
    task.finish();
    
    task.start("Writing sorted types to file");
    try (LogFileWriter writer = IOUtils.createLogFileWriter(new File(Arguments.OUTPUT.getValue(), "imports-by-total-count"))) {
      for (CountingFqnNode node : sorted.descendingSet()) {
        writer.write(node.getTotalCount() + " " + node.getProjectCount() + " " + node.getFqn());
      }
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Error writing imports to file", e);
    }
    task.finish();
    
    task.start("Sorting FQNs by project import count");
    sorted = new TreeSet<>(new Comparator<CountingFqnNode>() {
      @Override
      public int compare(CountingFqnNode o1, CountingFqnNode o2) {
        int cmp = Integer.compare(o1.getProjectCount(), o2.getProjectCount());
        if (cmp == 0) {
          return Integer.compare(o1.hashCode(), o2.hashCode());
        } else {
          return cmp;
        }
      }});
    for (CountingFqnNode node : root.getPostOrderIterable()) {
      sorted.add(node);
    }
    task.finish();
    
    task.start("Writing sorted imports to file");
    try (LogFileWriter writer = IOUtils.createLogFileWriter(MISSING_TYPES_LIST)) {
      for (CountingFqnNode node : sorted.descendingSet()) {
        writer.write(node.getProjectCount() + " " + node.getFqn());
      }
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Error writing imports to file", e);
    }
    task.finish();
  }
}
