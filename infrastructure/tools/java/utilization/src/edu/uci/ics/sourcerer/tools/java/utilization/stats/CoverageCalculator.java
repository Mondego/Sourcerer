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

import java.io.File;

import edu.uci.ics.sourcerer.tools.java.model.extracted.MissingTypeEX;
import edu.uci.ics.sourcerer.tools.java.model.extracted.io.ReaderBundle;
import edu.uci.ics.sourcerer.tools.java.repo.model.JarFile;
import edu.uci.ics.sourcerer.tools.java.repo.model.JavaRepository;
import edu.uci.ics.sourcerer.tools.java.repo.model.JavaRepositoryFactory;
import edu.uci.ics.sourcerer.tools.java.repo.model.extracted.ExtractedJavaProject;
import edu.uci.ics.sourcerer.tools.java.repo.model.extracted.ExtractedJavaRepository;
import edu.uci.ics.sourcerer.tools.java.utilization.stats.SourcedFqnNode.Source;
import edu.uci.ics.sourcerer.util.Counter;
import edu.uci.ics.sourcerer.util.CounterSet;
import edu.uci.ics.sourcerer.util.io.FileUtils;
import edu.uci.ics.sourcerer.util.io.arguments.Argument;
import edu.uci.ics.sourcerer.util.io.arguments.FileArgument;
import edu.uci.ics.sourcerer.util.io.logging.TaskProgressLogger;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class CoverageCalculator {
  public static final Argument<File> JAR_REPO = new FileArgument("jar-repo", "Jar repo");
  
  public static void calculateJarCoverage() {
    TaskProgressLogger task = TaskProgressLogger.get();
    
    task.start("Calculating coverage by " + JAR_REPO.getValue().getPath() + " of missing imports from " + JavaRepositoryFactory.INPUT_REPO.getValue().getPath());
    
    // Load the jar repo
    JavaRepository jarRepo = JavaRepositoryFactory.INSTANCE.loadJavaRepository(JAR_REPO);
    
    task.start("Populating the prefix tree");
    SourcedFqnNode root = SourcedFqnNode.createRoot();
    
    task.start("Processing maven jars", "jars processed", 500);
    for (JarFile jar : jarRepo.getMavenJarFiles()) {
      for (String fqn : FileUtils.getClassFilesFromJar(jar.getFile().toFile())) {
        root.getChild(fqn, '.').addSource(Source.MAVEN);
      }
      task.progress();
    }
    task.finish();
    
    task.start("Processing project jars", "jars processed", 500);
    for (JarFile jar : jarRepo.getProjectJarFiles()) {
      for (String fqn : FileUtils.getClassFilesFromJar(jar.getFile().toFile())) {
        root.getChild(fqn, '.').addSource(Source.PROJECT);
      }
      task.progress();
    }
    task.finish();
    
    // Load the extracted repo with the missing types
    ExtractedJavaRepository repo = JavaRepositoryFactory.INSTANCE.loadExtractedJavaRepository(JavaRepositoryFactory.INPUT_REPO);
    
    task.start("Processing extracted projects for missing types", "projects processed", 500);
    for (ExtractedJavaProject project : repo.getProjects()) {
      task.progress();
      ReaderBundle bundle = new ReaderBundle(project.getExtractionDir().toFile());
      for (MissingTypeEX missing : bundle.getTransientMissingTypes()) {
        root.getChild(missing.getFqn(), '.').addSource(Source.MISSING);
      }
    }
    task.finish();
    
    task.finish();
    
    task.start("Evaluating FQN coverage");
    CounterSet<String> counters = new CounterSet<>();
    for (SourcedFqnNode fqn : root.getPostOrderIterable()) {
      counters.increment(fqn.getSources().toString());
    }
    task.finish();
    
    for (Counter<String> counter : counters.getCounters()) {
      task.report(counter.getObject() + " " + counter.getCount());
    }
  }
}
