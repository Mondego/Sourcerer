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
package edu.uci.ics.sourcerer.db.tools;

import static edu.uci.ics.sourcerer.util.io.Logging.logger;

import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.logging.Level;

import edu.uci.ics.sourcerer.db.util.DatabaseConnection;
import edu.uci.ics.sourcerer.repo.extracted.ExtractedJar;
import edu.uci.ics.sourcerer.repo.extracted.ExtractedLibrary;
import edu.uci.ics.sourcerer.repo.extracted.ExtractedRepository;
import edu.uci.ics.sourcerer.repo.general.AbstractRepository;
import edu.uci.ics.sourcerer.util.Helper;
import edu.uci.ics.sourcerer.util.TimeCounter;
import edu.uci.ics.sourcerer.util.io.FileUtils;
import edu.uci.ics.sourcerer.util.io.Property;
import edu.uci.ics.sourcerer.util.io.properties.IntegerProperty;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class ParallelDatabaseImporter {
  public static Property<Integer> THREAD_COUNT = new IntegerProperty("thread-count", 4, "Database Importer", "Number of simultaneous threads");
  
  public void initializeDatabase() {
    logger.info("Initializing database...");
    logger.info("  Opening database connection...");
    DatabaseConnection connection = new DatabaseConnection();
    connection.open();
    InitializeDatabase init = new InitializeDatabase(connection);
    init.run();
  }
  
  public void importJavaLibraries() {
    logger.info("Importing Java libraries...");
    
    TimeCounter counter = new TimeCounter();
    
    logger.info("Loading extracted repository...");
    ExtractedRepository extracted = ExtractedRepository.getRepository();
    
    logger.info("Loading extracted Java libraries...");
    Collection<ExtractedLibrary> libraries = extracted.getLibraries();

    int numThreads = THREAD_COUNT.getValue();
    
    logger.info("Beginning stage one of import...");
    Iterable<ExtractedLibrary> iterable = createSynchronizedIterable(libraries.iterator());
    logger.info("  Initializing " + numThreads + " threads...");
    Collection<Thread> threads = Helper.newArrayList(numThreads);
    for (int i = 0; i < numThreads; i++) {
      logger.info("    Opening database connection...");
      DatabaseConnection connection = new DatabaseConnection();
      connection.open();
      ImportJavaLibrariesStageOne importJavaLibraries = new ImportJavaLibrariesStageOne(connection, iterable);
      logger.info("    Starting thread...");
      threads.add(importJavaLibraries.start());
    }
    
    logger.info("    Waiting for termination...");
    for (Thread t : threads) {
      try {
        t.join();
      } catch (InterruptedException e) {
        logger.log(Level.SEVERE, "Thread interrupted", e);
      }
    }
    logger.info(counter.reportTime(2, "Java library import stage one completed"));
    
    counter.lap();
    
    logger.info("Beginning stage two of import...");
    iterable = createSynchronizedIterable(libraries.iterator());
    DatabaseConnection unknownConnection = new DatabaseConnection();
    unknownConnection.open();
    SynchronizedUnknownsMap unknowns = new SynchronizedUnknownsMap(unknownConnection);
    
    logger.info("  Initializing " + numThreads + " threads...");
    threads.clear();
    for (int i = 0; i < numThreads; i++) {
      logger.info("    Opening database connection...");
      DatabaseConnection connection = new DatabaseConnection();
      connection.open();
      ImportJavaLibrariesStageTwo importJavaLibraries = new ImportJavaLibrariesStageTwo(connection, unknowns, iterable);
      logger.info("    Starting thread...");
      threads.add(importJavaLibraries.start());
    }
    
    logger.info("    Waiting for termination...");
    for (Thread t : threads) {
      try {
        t.join();
      } catch (InterruptedException e) {
        logger.log(Level.SEVERE, "Thread interrupted", e);
      }
    }
    unknowns.close();
    unknownConnection.close();
    logger.info(counter.reportTime(2, "Java library import stage two completed"));
    
    logger.info(counter.reportTotalTime(0, "Java library import completed"));
  }
  
  public void importJarFiles() {
    logger.info("Importing jar files...");
    
    TimeCounter counter = new TimeCounter();
    
    logger.info("Loading extracted repository...");
    ExtractedRepository extracted = ExtractedRepository.getRepository();
    
    logger.info("Loading extracted Java libraries...");
    Collection<ExtractedJar> jars = null;
    if (AbstractRepository.JAR_FILTER.hasValue()) {
      jars = extracted.getJars(FileUtils.getFileAsSet(AbstractRepository.JAR_FILTER.getValue()));
    } else { 
      jars = extracted.getJars();
    }

    int numThreads = THREAD_COUNT.getValue();
    
    logger.info("Beginning stage one of import...");
    Iterable<ExtractedJar> iterable = createSynchronizedIterable(jars.iterator());
    logger.info("  Initializing " + numThreads + " threads...");
    Collection<Thread> threads = Helper.newArrayList(numThreads);
    for (int i = 0; i < numThreads; i++) {
      logger.info("    Opening database connection...");
      DatabaseConnection connection = new DatabaseConnection();
      connection.open();
      ImportJarFilesStageOne importJavaLibraries = new ImportJarFilesStageOne(connection, iterable);
      logger.info("    Starting thread...");
      threads.add(importJavaLibraries.start());
    }
    
    logger.info("    Waiting for termination...");
    for (Thread t : threads) {
      try {
        t.join();
      } catch (InterruptedException e) {
        logger.log(Level.SEVERE, "Thread interrupted", e);
      }
    }
    logger.info(counter.reportTime(2, "Jar files import stage one completed"));
    
    counter.lap();
    
    logger.info("Beginning stage two of import...");
    iterable = createSynchronizedIterable(jars.iterator());
    DatabaseConnection unknownConnection = new DatabaseConnection();
    unknownConnection.open();
    SynchronizedUnknownsMap unknowns = new SynchronizedUnknownsMap(unknownConnection);
    
    logger.info("  Initializing " + numThreads + " threads...");
    threads.clear();
    for (int i = 0; i < numThreads; i++) {
      logger.info("    Opening database connection...");
      DatabaseConnection connection = new DatabaseConnection();
      connection.open();
      ImportJarFilesStageTwo importJavaLibraries = new ImportJarFilesStageTwo(connection, unknowns, iterable);
      logger.info("    Starting thread...");
      threads.add(importJavaLibraries.start());
    }
    
    logger.info("    Waiting for termination...");
    for (Thread t : threads) {
      try {
        t.join();
      } catch (InterruptedException e) {
        logger.log(Level.SEVERE, "Thread interrupted", e);
      }
    }
    unknowns.close();
    unknownConnection.close();
    logger.info(counter.reportTime(2, "Jar files import stage two completed"));
    
    logger.info(counter.reportTotalTime(0, "Jar files import completed"));
  }
  
  private <T> Iterable<T> createSynchronizedIterable(final Iterator<T> iterator) {
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
