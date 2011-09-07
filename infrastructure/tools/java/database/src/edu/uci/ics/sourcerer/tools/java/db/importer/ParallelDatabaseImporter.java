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
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.logging.Level;

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
import edu.uci.ics.sourcerer.tools.java.repo.model.extracted.ExtractedJavaRepository;
import edu.uci.ics.sourcerer.util.io.Logging;
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
        TaskProgressLogger task = new TaskProgressLogger("Initializing database");
        
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
        exec.insert(EntitiesTable.makeInsert(Entity.PRIMITIVE, "boolean", projectID));
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
    TaskProgressLogger task = new TaskProgressLogger("Importing Java libraries");
    
    task.start("Loading extracted repository");
    ExtractedJavaRepository repo = JavaRepositoryFactory.INSTANCE.loadExtractedJavaRepository(JavaRepositoryFactory.INPUT_REPO);
    task.finish();
    
    task.start("Loading extracted Java libraries");
    Collection<? extends ExtractedJarFile> libs = repo.getLibraryJarFiles();
    task.finish();

    int numThreads = THREAD_COUNT.getValue();
    
    Iterable<? extends ExtractedJarFile> iterable = createSynchronizedIterable(task, libs.iterator());
    task.start("Performing import stage one with " + numThreads + " threads");
    Collection<Thread> threads = new ArrayList<>(numThreads);
    for (int i = 0; i < numThreads; i++) {
      ImportJavaLibrariesStageOne importJavaLibraries = new ImportJavaLibrariesStageOne(iterable);
      threads.add(importJavaLibraries.start());
    }
    
    for (Thread t : threads) {
      try {
        t.join();
      } catch (InterruptedException e) {
        logger.log(Level.SEVERE, "Thread interrupted", e);
      }
    }
    task.finish();
    
    SynchronizedUnknownsMap unknowns = new SynchronizedUnknownsMap(task);
    
    iterable = createSynchronizedIterable(task, libs.iterator());
    task.start("Performing import stage two with " + numThreads + " threads");

    threads.clear();
    for (int i = 0; i < numThreads; i++) {
      ImportJavaLibrariesStageTwo importJavaLibraries = new ImportJavaLibrariesStageTwo(iterable, unknowns);
      threads.add(importJavaLibraries.start());
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
//  
//  @SuppressWarnings("unchecked")
//  public static void importJarFiles() {
//    logger.info("Importing jar files...");
//    
//    TimeCounter counter = new TimeCounter();
//    
//    logger.info("Loading extracted repository...");
//    ExtractedRepository extracted = ExtractedRepository.getRepository(INPUT_REPO.getValue());
//    
//    logger.info("Loading extracted jar files...");
//    Collection<ExtractedJar> jars = extracted.getJars();
//
//    int numThreads = THREAD_COUNT.getValue();
//    
//    logger.info("Beginning stage one of import...");
//    Iterable<Extracted> iterable = (Iterable<Extracted>)(Object)createSynchronizedIterable(jars.iterator());
//    logger.info("  Initializing " + numThreads + " threads...");
//    Collection<Thread> threads = Helper.newArrayList(numThreads);
//    for (int i = 0; i < numThreads; i++) {
//      DatabaseConnection connection = new DatabaseConnection();
//      connection.open();
//      ImportStageOne importStageOne = new ImportStageOne(connection, iterable);
//      threads.add(importStageOne.start());
//    }
//    
//    for (Thread t : threads) {
//      try {
//        t.join();
//      } catch (InterruptedException e) {
//        logger.log(Level.SEVERE, "Thread interrupted", e);
//      }
//    }
//    logger.info(counter.reportTime(2, "Jar files import stage one completed"));
//    
//    counter.lap();
//    
//    logger.info("Beginning stage two of import...");
//    iterable = (Iterable<Extracted>)(Object)createSynchronizedIterable(jars.iterator());
//    DatabaseConnection unknownConnection = new DatabaseConnection();
//    unknownConnection.open();
//    SynchronizedUnknownsMap unknowns = new SynchronizedUnknownsMap(unknownConnection);
//    
//    logger.info("  Initializing " + numThreads + " threads...");
//    threads.clear();
//    for (int i = 0; i < numThreads; i++) {
//      DatabaseConnection connection = new DatabaseConnection();
//      connection.open();
//      ImportStageTwo importStageTwo = new ImportStageTwo(connection, unknowns, iterable);
//      threads.add(importStageTwo.start());
//    }
//    
//    for (Thread t : threads) {
//      try {
//        t.join();
//      } catch (InterruptedException e) {
//        logger.log(Level.SEVERE, "Thread interrupted", e);
//      }
//    }
//    logger.info(counter.reportTime(2, "Jar files import stage two completed"));
//    
//    logger.info(counter.reportTotalTime(0, "Jar files import completed"));
//  }
//  
//  @SuppressWarnings("unchecked")
//  public static void importProjects() {
//    logger.info("Importing projects...");
//    
//    TimeCounter counter = new TimeCounter();
//    
//    logger.info("Loading extracted repository...");
//    ExtractedRepository extracted = ExtractedRepository.getRepository(INPUT_REPO.getValue());
//    
//    logger.info("Loading extracted projects...");
//    Collection<ExtractedProject> projects = extracted.getProjects();
//
//    int numThreads = THREAD_COUNT.getValue();
//    
//    logger.info("Beginning stage one of import...");
//    Iterable<Extracted> iterable = (Iterable<Extracted>)(Object)createSynchronizedIterable(projects.iterator());
//    logger.info("  Initializing " + numThreads + " threads...");
//    Collection<Thread> threads = Helper.newArrayList(numThreads);
//    for (int i = 0; i < numThreads; i++) {
//      DatabaseConnection connection = new DatabaseConnection();
//      connection.open();
//      ImportStageOne importStageOne = new ImportStageOne(connection, iterable);
//      threads.add(importStageOne.start());
//    }
//    
//    for (Thread t : threads) {
//      try {
//        t.join();
//      } catch (InterruptedException e) {
//        logger.log(Level.SEVERE, "Thread interrupted", e);
//      }
//    }
//    logger.info(counter.reportTime(2, "Projects import stage one completed"));
//    
//    counter.lap();
//    
//    logger.info("Beginning stage two of import...");
//    iterable = (Iterable<Extracted>)(Object)createSynchronizedIterable(projects.iterator());
//    DatabaseConnection unknownConnection = new DatabaseConnection();
//    unknownConnection.open();
//    SynchronizedUnknownsMap unknowns = new SynchronizedUnknownsMap(unknownConnection);
//    
//    logger.info("  Initializing " + numThreads + " threads...");
//    threads.clear();
//    for (int i = 0; i < numThreads; i++) {
//      DatabaseConnection connection = new DatabaseConnection();
//      connection.open();
//      ImportStageTwo importStageTwo = new ImportStageTwo(connection, unknowns, iterable);
//      threads.add(importStageTwo.start());
//    }
//    
//    for (Thread t : threads) {
//      try {
//        t.join();
//      } catch (InterruptedException e) {
//        logger.log(Level.SEVERE, "Thread interrupted", e);
//      }
//    }
//    logger.info(counter.reportTime(2, "Project import stage two completed"));
//    
//    logger.info(counter.reportTotalTime(0, "Projects import completed"));
//  }
//  
  private static <T> Iterable<T> createSynchronizedIterable(final TaskProgressLogger task, final Iterator<T> iterator) {
    return new Iterable<T>() {
      @Override
      public Iterator<T> iterator() {
        return new Iterator<T>() {
          private T next = null;
          
          @Override
          public boolean hasNext() {
            synchronized (iterator) {
              if (next == null) {
                if (iterator.hasNext()) {
                  next = iterator.next();
                  return true;
                } else {
                  return false;
                }
              } else {
                return true;
              }
            }
          }

          @Override
          public T next() {
            synchronized (iterator) {
              if (hasNext()) {
                T toReturn = next;
                next = null;
                
                task.report(Logging.THREAD_INFO, "Thread " + Thread.currentThread() + " processing " + toReturn);
                return toReturn;
              } else {
                throw new NoSuchElementException();
              }
            }
          }

          @Override
          public void remove() {
            throw new UnsupportedOperationException();
          }
        };
      }
    };
  }
}
