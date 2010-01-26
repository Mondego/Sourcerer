package edu.uci.ics.sourcerer.db.tools;

import static edu.uci.ics.sourcerer.util.io.Logging.logger;

import java.util.Collection;
import java.util.Map;
import java.util.logging.Level;

import edu.uci.ics.sourcerer.db.schema.DatabaseAccessor;
import edu.uci.ics.sourcerer.db.util.DatabaseConnection;
import edu.uci.ics.sourcerer.db.util.InsertBatcher;
import edu.uci.ics.sourcerer.db.util.KeyInsertBatcher;
import edu.uci.ics.sourcerer.model.Entity;
import edu.uci.ics.sourcerer.model.extracted.EntityEX;
import edu.uci.ics.sourcerer.model.extracted.FileEX;
import edu.uci.ics.sourcerer.model.extracted.ProblemEX;
import edu.uci.ics.sourcerer.repo.extracted.ExtractedLibrary;
import edu.uci.ics.sourcerer.repo.extracted.ExtractedRepository;
import edu.uci.ics.sourcerer.repo.extracted.io.ExtractedReader;
import edu.uci.ics.sourcerer.util.Helper;

public class DatabaseImporter extends DatabaseAccessor {

  protected DatabaseImporter(DatabaseConnection connection) {
    super(connection);
  }

  protected void initializeDatabase() {
    logger.info("Initializing database...");
    
    logger.info("  Dropping old tables...");
    executor.dropTables(
        projectsTable,
        filesTable,
        importsTable,
        problemsTable,
        commentsTable,
        entitiesTable,
        relationsTable);
    
    logger.info("  Adding new tables...");
    projectsTable.createTable();
    filesTable.createTable();
    importsTable.createTable();
    problemsTable.createTable();
    commentsTable.createTable();
    entitiesTable.createTable();
    relationsTable.createTable();
    
    logger.info("  Adding the primitive types...");
    locker.addWrites(projectsTable, entitiesTable);
    locker.lock();
    
    String projectID = projectsTable.insertPrimitivesProject();
    entitiesTable.insert(Entity.PRIMITIVE, "boolean", projectID);
    entitiesTable.insert(Entity.PRIMITIVE, "char", projectID);
    entitiesTable.insert(Entity.PRIMITIVE, "byte", projectID);
    entitiesTable.insert(Entity.PRIMITIVE, "short", projectID);
    entitiesTable.insert(Entity.PRIMITIVE, "int", projectID);
    entitiesTable.insert(Entity.PRIMITIVE, "long", projectID);
    entitiesTable.insert(Entity.PRIMITIVE, "float", projectID);
    entitiesTable.insert(Entity.PRIMITIVE, "double", projectID);
    entitiesTable.insert(Entity.PRIMITIVE, "void", projectID);
    
    locker.unlock();
    logger.info("  Initialization complete.");
  }
  
  protected void importJavaLibrary() {
    logger.info("Importing Java library...");
    
    logger.info("  Loading extracted repository...");
    ExtractedRepository extracted = ExtractedRepository.getRepository();
    
    logger.info("  Loading extracted Java libraries...");
    Collection<ExtractedLibrary> libraries = extracted.getLibraries();
    
    logger.info("  Importing " + libraries.size() + " libraries...");
    final Map<String, String> fileMap = Helper.newHashMap();
    final Map<String, String> entityMap = Helper.newHashMap();
    
    // Do the entities first, since there can be references between
    // java libraries
    for (ExtractedLibrary library : libraries) {
      logger.info("  Partial import of " + library.getName());
      logger.info("    Inserting project...");
      final String projectID = projectsTable.insert(library);
      
      logger.info("    Inserting files...");
      {
        int count = 0;
        KeyInsertBatcher<FileEX> batcher = filesTable.getKeyInsertBatcher(new KeyInsertBatcher.KeyProcessor<FileEX>() {
          @Override
          public void processKey(String key, FileEX value) {
            if (fileMap.containsKey(value.getRelativePath())) {
              logger.log(Level.SEVERE, "File collision: " + value.getRelativePath());
            } else {
              fileMap.put(value.getRelativePath(), key);
            }
          }
        });
        for (FileEX file : library.getFileReader()) {
          filesTable.insert(batcher, file, projectID, file);
          count++;
        }
        batcher.insert();
        logger.info("      " + count + " files inserted.");
      }
      
      logger.info("    Inserting problems...");
      {
        int count = 0;
        InsertBatcher batcher = problemsTable.getInsertBatcher();
        for (ProblemEX problem : library.getProblemReader()) {
          String fileID = fileMap.get(problem.getRelativePath());
          if (fileID == null) {
            logger.log(Level.SEVERE, "Unknown file: " + problem.getRelativePath() + " for " + problem);
          } else {
            problemsTable.insert(batcher, problem, projectID, fileID);
            count++;
          }
        }
        batcher.insert();
        logger.info("      " + count + " problems inserted.");
      }
      
      logger.info("    Inserting entities....");
      {
        int count = 0;
        KeyInsertBatcher<EntityEX> batcher = entitiesTable.getKeyInsertBatcher(new KeyInsertBatcher.KeyProcessor<EntityEX>() {
          @Override
          public void processKey(String key, EntityEX value) {
            if (entityMap.containsKey(value.getFqn())) {
              logger.log(Level.SEVERE, "FQN collision: " + value.getFqn());
              entityMap.put(value.getFqn(), "COLLISION");
            } else {
              entityMap.put(value.getFqn(), key);
            }
          }
        });
        for (EntityEX entity : library.getEntityReader()) {
          String fileID = null;
          if (entity.getPath() != null) {
            fileID = fileMap.get(entity.getPath());
            if (fileID == null) {
              logger.log(Level.SEVERE, "Unknown file: " + entity.getPath() + " for " + entity);
            }
          }
          entitiesTable.insert(batcher, entity, projectID, fileID, entity);
          count++;
        }
        batcher.insert();
        logger.info("      " + count + " entities inserted.");
      }
    }
    
    for (ExtractedLibrary library : libraries) {
      
    }
  }
}
