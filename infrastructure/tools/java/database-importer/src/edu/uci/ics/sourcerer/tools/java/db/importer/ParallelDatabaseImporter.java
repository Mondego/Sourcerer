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
package edu.uci.ics.sourcerer.tools.java.db.importer;

import static edu.uci.ics.sourcerer.util.io.logging.Logging.logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;

import edu.uci.ics.sourcerer.tools.java.db.resolver.JavaLibraryTypeModel;
import edu.uci.ics.sourcerer.tools.java.db.resolver.UnknownEntityCache;
import edu.uci.ics.sourcerer.tools.java.repo.model.JavaRepositoryFactory;
import edu.uci.ics.sourcerer.tools.java.repo.model.extracted.ExtractedJarFile;
import edu.uci.ics.sourcerer.tools.java.repo.model.extracted.ExtractedJavaProject;
import edu.uci.ics.sourcerer.tools.java.repo.model.extracted.ExtractedJavaRepository;
import edu.uci.ics.sourcerer.util.Nullerator;
import edu.uci.ics.sourcerer.util.io.arguments.Argument;
import edu.uci.ics.sourcerer.util.io.arguments.BooleanArgument;
import edu.uci.ics.sourcerer.util.io.arguments.IntegerArgument;
import edu.uci.ics.sourcerer.util.io.logging.TaskProgressLogger;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public final class ParallelDatabaseImporter {
  public static Argument<Boolean> STRUCTURAL_ONLY = new BooleanArgument("structural-only", false, "Only import entities and structural relations");
  public static Argument<Integer> THREAD_COUNT = new IntegerArgument("thread-count", 4, "Number of simultaneous threads");
  
  private ParallelDatabaseImporter() {}
  
  public static void importJavaLibraries() {
    TaskProgressLogger task = TaskProgressLogger.get();
    task.start("Importing Java libraries");
    
    task.start("Loading extracted repository");
    ExtractedJavaRepository repo = JavaRepositoryFactory.INSTANCE.loadExtractedJavaRepository(JavaRepositoryFactory.INPUT_REPO);
    task.finish();
    
    if (repo == null) {
      task.finish();
      return;
    }
    
    task.start("Loading extracted Java libraries");
    Collection<? extends ExtractedJarFile> libs = repo.getLibraryJarFiles();
    task.finish();

    if (libs.isEmpty()) {
      task.report("No libraries to import");
      task.finish();
      return;
    }
    
    int numThreads = THREAD_COUNT.getValue();
    
    Nullerator<ExtractedJarFile> nullerator = Nullerator.createNullerator(libs, task, "Thread %s now processing: %s");
    
    task.start("Performing entity import with " + numThreads + " threads");
    Collection<Thread> threads = new ArrayList<>(numThreads);
    for (int i = 0; i < numThreads; i++) {
      JavaLibraryEntitiesImporter importer = new JavaLibraryEntitiesImporter(nullerator);
      threads.add(importer.start());
    }
    
    for (Thread t : threads) {
      try {
        t.join();
      } catch (InterruptedException e) {
        logger.log(Level.SEVERE, "Thread interrupted", e);
      }
    }
    task.finish();
    
    JavaLibraryTypeModel javaModel = JavaLibraryTypeModel.makeJavaLibraryTypeModel(task);
    UnknownEntityCache unknowns = UnknownEntityCache.makeUnknownEntityCache(task);
    
    nullerator = Nullerator.createNullerator(libs, task, "Thread %s now processing: %s");
    task.start("Performing structural relation import with " + numThreads + " threads");

    threads.clear();
    for (int i = 0; i < numThreads; i++) {
      JavaLibraryStructuralRelationsImporter importer = new JavaLibraryStructuralRelationsImporter(nullerator, javaModel, unknowns);
      threads.add(importer.start());
    }
    
    for (Thread t : threads) {
      try {
        t.join();
      } catch (InterruptedException e) {
        logger.log(Level.SEVERE, "Thread interrupted", e);
      }
    }
    task.finish();
    
    if (STRUCTURAL_ONLY.getValue()) {
      task.report("Skipping referential relation import");
    } else {
      javaModel = JavaLibraryTypeModel.makeJavaLibraryTypeModel(task);
      
      nullerator = Nullerator.createNullerator(libs, task, "Thread %s now processing: %s");
      task.start("Performing referential relation import with " + numThreads + " threads");
  
      threads.clear();
      for (int i = 0; i < numThreads; i++) {
        JavaLibraryReferentialRelationsImporter importer = new JavaLibraryReferentialRelationsImporter(nullerator, javaModel, unknowns);
        threads.add(importer.start());
      }
      
      for (Thread t : threads) {
        try {
          t.join();
        } catch (InterruptedException e) {
          logger.log(Level.SEVERE, "Thread interrupted", e);
        }
      }
      task.finish();
    }
    
    task.finish();
  }
  
  public static void importJarFiles() {
    TaskProgressLogger task = TaskProgressLogger.get();
    task.start("Importing jar files");
    
    task.start("Loading extracted repository");
    ExtractedJavaRepository repo = JavaRepositoryFactory.INSTANCE.loadExtractedJavaRepository(JavaRepositoryFactory.INPUT_REPO);
    task.finish();
       
    task.start("Loading extracted maven jar files");
    Collection<? extends ExtractedJarFile> mavenJars = repo.getMavenJarFiles();
    task.finish();

    int numThreads = THREAD_COUNT.getValue();
    Nullerator<ExtractedJarFile> nullerator = null;
    Collection<Thread> threads = new ArrayList<>(numThreads);
    
    if (!mavenJars.isEmpty()) {
      nullerator = Nullerator.createNullerator(mavenJars, task, "%s now processing: %s");
      task.start("Performing entity import with " + numThreads + " threads");
      
      threads.clear();
      for (int i = 0; i < numThreads; i++) {
        JarEntitiesImporter importer = new JarEntitiesImporter(nullerator);
        threads.add(importer.start());
      }
      
      for (Thread t : threads) {
        try {
          t.join();
        } catch (InterruptedException e) {
          logger.log(Level.SEVERE, "Thread interrupted", e);
        }
      }
      task.finish();
    }
    
    task.start("Loading extracted project jar files");
    Collection<? extends ExtractedJarFile> projectJars = repo.getProjectJarFiles();
    task.finish();
    
    if (!projectJars.isEmpty()) {
      nullerator = Nullerator.createNullerator(projectJars, task, "%s now processing: %s");
      task.start("Performing entity import with " + numThreads + " threads");
      
      threads.clear();
      for (int i = 0; i < numThreads; i++) {
        JarEntitiesImporter importer = new JarEntitiesImporter(nullerator);
        threads.add(importer.start());
      }
      
      for (Thread t : threads) {
        try {
          t.join();
        } catch (InterruptedException e) {
          logger.log(Level.SEVERE, "Thread interrupted", e);
        }
      }
      task.finish();
    }
    
    JavaLibraryTypeModel javaModel = JavaLibraryTypeModel.makeJavaLibraryTypeModel(task);
    UnknownEntityCache unknowns = UnknownEntityCache.makeUnknownEntityCache(task);
    
    if (!mavenJars.isEmpty()) {
      nullerator = Nullerator.createNullerator(mavenJars, task, "%s now processing: %s");
      task.start("Performing structural relation import with " + numThreads + " threads");

      threads.clear();
      for (int i = 0; i < numThreads; i++) {
        JarStructuralRelationsImporter importer = new JarStructuralRelationsImporter(nullerator, javaModel, unknowns);
        threads.add(importer.start());
      }
      
      for (Thread t : threads) {
        try {
          t.join();
        } catch (InterruptedException e) {
          logger.log(Level.SEVERE, "Thread interrupted", e);
        }
      }
      task.finish();
    }
    
    if (!projectJars.isEmpty()) {
      nullerator = Nullerator.createNullerator(projectJars, task, "Thread %s now processing: %s");
      task.start("Performing structural relation import with " + numThreads + " threads");

      threads.clear();
      for (int i = 0; i < numThreads; i++) {
        JarStructuralRelationsImporter importer = new JarStructuralRelationsImporter(nullerator, javaModel, unknowns);
        threads.add(importer.start());
      }
      
      for (Thread t : threads) {
        try {
          t.join();
        } catch (InterruptedException e) {
          logger.log(Level.SEVERE, "Thread interrupted", e);
        }
      }
      task.finish();
    }
    
    if (STRUCTURAL_ONLY.getValue()) {
      task.report("Skipping referential relation import");
    } else {
      if (!mavenJars.isEmpty()) {
        nullerator = Nullerator.createNullerator(mavenJars, task, "Thread %s now processing: %s");
        task.start("Performing referential relation import with " + numThreads + " threads");
  
        threads.clear();
        for (int i = 0; i < numThreads; i++) {
          JarReferentialRelationsImporter importer = new JarReferentialRelationsImporter(nullerator, javaModel, unknowns);
          threads.add(importer.start());
        }
      
        for (Thread t : threads) {
          try {
            t.join();
          } catch (InterruptedException e) {
            logger.log(Level.SEVERE, "Thread interrupted", e);
          }
        }
        task.finish();
      }
      
      if (!projectJars.isEmpty()) {
        nullerator = Nullerator.createNullerator(projectJars, task, "Thread %s now processing: %s");
        task.start("Performing referential relation import with " + numThreads + " threads");
  
        threads.clear();
        for (int i = 0; i < numThreads; i++) {
          JarReferentialRelationsImporter importer = new JarReferentialRelationsImporter(nullerator, javaModel, unknowns);
          threads.add(importer.start());
        }
      
        for (Thread t : threads) {
          try {
            t.join();
          } catch (InterruptedException e) {
            logger.log(Level.SEVERE, "Thread interrupted", e);
          }
        }
        task.finish();
      }
    }
    
    task.finish();
  }
  
  public static void importProjects() {
    TaskProgressLogger task = TaskProgressLogger.get();
    task.start("Importing projects");
    
    task.start("Loading extracted repository");
    ExtractedJavaRepository repo = JavaRepositoryFactory.INSTANCE.loadExtractedJavaRepository(JavaRepositoryFactory.INPUT_REPO);
    task.finish();
       
    task.start("Loading extracted projects");
    Collection<? extends ExtractedJavaProject> projects = repo.getProjects();
    task.finish();

    int numThreads = THREAD_COUNT.getValue();
    Nullerator<ExtractedJavaProject> nullerator = Nullerator.createNullerator(projects, task, "Thread %s now processing: %s");
    Collection<Thread> threads = new ArrayList<>(numThreads);
    
    task.start("Performing entity import with " + numThreads + " threads");
    threads.clear();
    for (int i = 0; i < numThreads; i++) {
      ProjectEntitiesImporter importer = new ProjectEntitiesImporter(nullerator);
      threads.add(importer.start());
    }
      
    for (Thread t : threads) {
      try {
        t.join();
      } catch (InterruptedException e) {
        logger.log(Level.SEVERE, "Thread interrupted", e);
      }
    }
    task.finish();
    
    JavaLibraryTypeModel javaModel = JavaLibraryTypeModel.makeJavaLibraryTypeModel(task);
    UnknownEntityCache unknowns = UnknownEntityCache.makeUnknownEntityCache(task);
    
    nullerator = Nullerator.createNullerator(projects, task, "Thread %s now processing: %s");
    task.start("Performing structural relation import with " + numThreads + " threads");

    threads.clear();
    for (int i = 0; i < numThreads; i++) {
      ProjectStructuralRelationsImporter importer = new ProjectStructuralRelationsImporter(nullerator, javaModel, unknowns);
      threads.add(importer.start());
    }
      
    for (Thread t : threads) {
      try {
        t.join();
      } catch (InterruptedException e) {
        logger.log(Level.SEVERE, "Thread interrupted", e);
      }
    }
    task.finish();
    
    if (STRUCTURAL_ONLY.getValue()) {
      task.report("Skipping referential relation import");
    } else {
      nullerator = Nullerator.createNullerator(projects, task, "Thread %s now processing: %s");
      task.start("Performing referential relation import with " + numThreads + " threads");
  
      threads.clear();
      for (int i = 0; i < numThreads; i++) {
        ProjectReferentialRelationsImporter importer = new ProjectReferentialRelationsImporter(nullerator, javaModel, unknowns);
        threads.add(importer.start());
      }
      
      for (Thread t : threads) {
        try {
          t.join();
        } catch (InterruptedException e) {
          logger.log(Level.SEVERE, "Thread interrupted", e);
        }
      }
      task.finish();
    }
    
    task.finish();
  }
}
