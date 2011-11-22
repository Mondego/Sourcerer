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

import static edu.uci.ics.sourcerer.util.io.Logging.logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;

import edu.uci.ics.sourcerer.tools.java.db.resolver.JavaLibraryTypeModel;
import edu.uci.ics.sourcerer.tools.java.db.resolver.UnknownEntityCache;
import edu.uci.ics.sourcerer.tools.java.db.schema.CommentsTable;
import edu.uci.ics.sourcerer.tools.java.db.schema.EntitiesTable;
import edu.uci.ics.sourcerer.tools.java.db.schema.EntityMetricsTable;
import edu.uci.ics.sourcerer.tools.java.db.schema.FileMetricsTable;
import edu.uci.ics.sourcerer.tools.java.db.schema.FilesTable;
import edu.uci.ics.sourcerer.tools.java.db.schema.ImportsTable;
import edu.uci.ics.sourcerer.tools.java.db.schema.ProblemsTable;
import edu.uci.ics.sourcerer.tools.java.db.schema.ProjectMetricsTable;
import edu.uci.ics.sourcerer.tools.java.db.schema.ProjectsTable;
import edu.uci.ics.sourcerer.tools.java.db.schema.RelationsTable;
import edu.uci.ics.sourcerer.tools.java.model.types.Entity;
import edu.uci.ics.sourcerer.tools.java.repo.model.JavaRepositoryFactory;
import edu.uci.ics.sourcerer.tools.java.repo.model.extracted.ExtractedJarFile;
import edu.uci.ics.sourcerer.tools.java.repo.model.extracted.ExtractedJavaProject;
import edu.uci.ics.sourcerer.tools.java.repo.model.extracted.ExtractedJavaRepository;
import edu.uci.ics.sourcerer.util.Nullerator;
import edu.uci.ics.sourcerer.util.io.TaskProgressLogger;
import edu.uci.ics.sourcerer.util.io.arguments.Argument;
import edu.uci.ics.sourcerer.util.io.arguments.IntegerArgument;
import edu.uci.ics.sourcerer.utils.db.DatabaseRunnable;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public final class ParallelDatabaseImporter {
  public static Argument<Integer> THREAD_COUNT = new IntegerArgument("thread-count", 4, "Number of simultaneous threads");
  
  private ParallelDatabaseImporter() {}
  
  public static void initializeDatabase() {
    new DatabaseRunnable() {
      @Override
      public void action() {
        TaskProgressLogger task = new TaskProgressLogger();
        task.start("Initializing database");
        
        task.start("Dropping old tables");
        exec.dropTables(
            CommentsTable.TABLE,
            EntitiesTable.TABLE,
            EntityMetricsTable.TABLE,
            FileMetricsTable.TABLE,
            FilesTable.TABLE,
            ImportsTable.TABLE,
            ProblemsTable.TABLE,
            ProjectMetricsTable.TABLE,
            ProjectsTable.TABLE,
            RelationsTable.TABLE);
        task.finish();
        
        task.start("Creating new tables");
        exec.createTable(CommentsTable.TABLE);
        exec.createTable(EntitiesTable.TABLE);
        exec.createTable(EntityMetricsTable.TABLE);
        exec.createTable(FileMetricsTable.TABLE);
        exec.createTable(FilesTable.TABLE);
        exec.createTable(ImportsTable.TABLE);
        exec.createTable(ProblemsTable.TABLE);
        exec.createTable(ProjectMetricsTable.TABLE);
        exec.createTable(ProjectsTable.TABLE);
        exec.createTable(RelationsTable.TABLE);
        task.finish();
        
        task.start("Adding the primitive types");
        Integer projectID = exec.insertWithKey(ProjectsTable.TABLE.makePrimitivesInsert());
        exec.insert(EntitiesTable.makeInsert(Entity.PRIMITIVE, "boolean",  projectID));
        exec.insert(EntitiesTable.makeInsert(Entity.PRIMITIVE, "char", projectID));
        exec.insert(EntitiesTable.makeInsert(Entity.PRIMITIVE, "byte", projectID));
        exec.insert(EntitiesTable.makeInsert(Entity.PRIMITIVE, "short", projectID));
        exec.insert(EntitiesTable.makeInsert(Entity.PRIMITIVE, "int", projectID));
        exec.insert(EntitiesTable.makeInsert(Entity.PRIMITIVE, "long", projectID));
        exec.insert(EntitiesTable.makeInsert(Entity.PRIMITIVE, "float", projectID));
        exec.insert(EntitiesTable.makeInsert(Entity.PRIMITIVE, "double", projectID));
        exec.insert(EntitiesTable.makeInsert(Entity.PRIMITIVE, "void", projectID));
        task.finish();
        
        task.start("Adding the unknowns project");
        exec.insert(ProjectsTable.TABLE.makeUnknownsInsert());
        task.finish();
        
        task.finish();
      }
    }.run();
  }
  
  public static void importJavaLibraries() {
    TaskProgressLogger task = new TaskProgressLogger();
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
    
    Nullerator<ExtractedJarFile> nullerator = Nullerator.makeNullerator(libs, task, "Thread %s now processing: %s");
    
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
    
    nullerator = Nullerator.makeNullerator(libs, task, "Thread %s now processing: %s");
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
    
    javaModel = JavaLibraryTypeModel.makeJavaLibraryTypeModel(task);
    
    nullerator = Nullerator.makeNullerator(libs, task, "Thread %s now processing: %s");
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
    
    task.finish();
  }
  
  public static void importJarFiles() {
    TaskProgressLogger task = new TaskProgressLogger();
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
      nullerator = Nullerator.makeNullerator(mavenJars, task, "Thread %s now processing: %s");
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
      nullerator = Nullerator.makeNullerator(projectJars, task, "Thread %s now processing: %s");
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
      nullerator = Nullerator.makeNullerator(mavenJars, task, "Thread %s now processing: %s");
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
      nullerator = Nullerator.makeNullerator(projectJars, task, "Thread %s now processing: %s");
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
    
    if (!mavenJars.isEmpty()) {
      nullerator = Nullerator.makeNullerator(mavenJars, task, "Thread %s now processing: %s");
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
      nullerator = Nullerator.makeNullerator(projectJars, task, "Thread %s now processing: %s");
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
    
    task.finish();
  }
  
  public static void importProjects() {
    TaskProgressLogger task = new TaskProgressLogger();
    task.start("Importing projects");
    
    task.start("Loading extracted repository");
    ExtractedJavaRepository repo = JavaRepositoryFactory.INSTANCE.loadExtractedJavaRepository(JavaRepositoryFactory.INPUT_REPO);
    task.finish();
       
    task.start("Loading extracted projects");
    Collection<? extends ExtractedJavaProject> projects = repo.getProjects();
    task.finish();

    int numThreads = THREAD_COUNT.getValue();
    Nullerator<ExtractedJavaProject> nullerator = Nullerator.makeNullerator(projects, task, "Thread %s now processing: %s");
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
    
    nullerator = Nullerator.makeNullerator(projects, task, "Thread %s now processing: %s");
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
    
    nullerator = Nullerator.makeNullerator(projects, task, "Thread %s now processing: %s");
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
    
    task.finish();
  }
}
