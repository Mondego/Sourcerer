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
import edu.uci.ics.sourcerer.model.Relation;
import edu.uci.ics.sourcerer.model.extracted.EntityEX;
import edu.uci.ics.sourcerer.model.extracted.FileEX;
import edu.uci.ics.sourcerer.model.extracted.LocalVariableEX;
import edu.uci.ics.sourcerer.model.extracted.ModelEX;
import edu.uci.ics.sourcerer.model.extracted.ProblemEX;
import edu.uci.ics.sourcerer.model.extracted.RelationEX;
import edu.uci.ics.sourcerer.repo.extracted.Extracted;
import edu.uci.ics.sourcerer.repo.extracted.ExtractedLibrary;
import edu.uci.ics.sourcerer.repo.extracted.ExtractedRepository;
import edu.uci.ics.sourcerer.util.Helper;
import edu.uci.ics.sourcerer.util.Pair;

public class DatabaseImporter extends DatabaseAccessor {
  private Map<String, String> fileMap;
  private Map<String, Ent> entityMap;
  
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
    fileMap = Helper.newHashMap();
    entityMap = Helper.newHashMap();
    
    locker.addWrites(projectsTable, filesTable, problemsTable, entitiesTable);
    locker.lock();
    for (ExtractedLibrary library : libraries) {
      logger.info("  Partial import of " + library.getName());
      
      logger.info("    Inserting project...");
      final String projectID = projectsTable.insert(library);
      
      insertFiles(library, projectID);
      insertProblems(library, projectID);
      insertEntities(library, projectID);
    }
    locker.unlock();
    
    locker.addReads(projectsTable);
    locker.addWrites();
    locker.lock();
    for (ExtractedLibrary library : libraries) {
      logger.info("  Remaining import of " + library.getName());
      
      String projectID = projectsTable.getProjectIDByName(library.getName());

      insertLocalVariables(library, projectID);
      //insertRelations(library, projectID);
      //insertImports(library, projectID);
      //insertComments(library, projectID);
    }
  }
  
  private void insertFiles(Extracted extracted, String projectID) {
    logger.info("    Inserting files...");
    
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
    for (FileEX file : extracted.getFileReader()) {
      filesTable.insert(batcher, file, projectID, file);
      count++;
    }
    batcher.insert();
    logger.info("      " + count + " files inserted.");
  }
  
  private void insertProblems(Extracted extracted, String projectID) {
    logger.info("    Inserting problems...");

    int count = 0;
    for (ProblemEX problem : extracted.getProblemReader()) {
      String fileID = fileMap.get(problem.getRelativePath());
      if (fileID == null) {
        logger.log(Level.SEVERE, "Unknown file: " + problem.getRelativePath() + " for " + problem);
      } else {
        problemsTable.insert(problem, projectID, fileID);
        count++;
      }
    }
    problemsTable.flushInserts();
    logger.info("      " + count + " problems inserted.");
  }
  
  private void insertEntities(Extracted extracted, final String projectID) {
    logger.info("    Inserting entities....");

    int count = 0;
    KeyInsertBatcher<EntityEX> batcher = entitiesTable.getKeyInsertBatcher(new KeyInsertBatcher.KeyProcessor<EntityEX>() {
      @Override
      public void processKey(String key, EntityEX value) {
        Ent ent = entityMap.get(value.getFqn());
        if (ent == null) {
          ent = new Ent(value.getFqn(), projectID, key);
          entityMap.put(value.getFqn(), ent);
        } else {
          logger.log(Level.SEVERE, "FQN collision: " + value.getFqn());
          ent.addPair(projectID, key);
        }
      }
    });
    for (EntityEX entity : extracted.getEntityReader()) {
      String fileID = getFileID(entity.getPath(), entity);
      entitiesTable.insert(batcher, entity, projectID, fileID, entity);
      count++;
    }
    batcher.insert();
    logger.info("      " + count + " entities inserted.");
  }
  
  private void insertLocalVariables(Extracted extracted, String projectID) {
    logger.info("    Inserting local variables / parameters...");

    int count = 0;
    for (LocalVariableEX local : extracted.getLocalVariableReader()) {
      String fileID = getFileID(local.getPath(), local);
      
      // Add the entity
      String eid = entitiesTable.insertLocalVariable(local, projectID, fileID);
      
      // Add the holds relation
      Pair<String, Boolean> typeEid = getEid(local.getTypeFqn());
      if (fileID == null) {
        relationsTable.insert(batcher, Relation.HOLDS, eid, typeEid.getFirst(), typeEid.getSecond(), projectID);
      } else {
        relationsTable.insert(batcher, Relation.HOLDS, eid, typeEid.getFirst(), typeEid.getSecond(), projectID, fileID, local.getStartPos(), local.getLength());
      }
      
      // Add the inside relation
      Pair<String, Boolean> parentEid = getEid(local.getParent());
      relationsTable.insert(batcher, Relation.INSIDE, eid, parentEid.getFirst(), parentEid.getSecond(), projectID);
      
      count++;
    }
    batcher.insert();
    logger.info("      " + count + " local variables / parameters inserted.");
  }
  
  private String getFileID(String path, ModelEX model) {
    if (path == null) {
      return null;
    } else {
      String fileID = fileMap.get(path);
      if (fileID == null) {
        logger.log(Level.SEVERE, "Unknown file: " + path + " for " + model);
      }
      return fileID;
    }
  }
  
  private Pair<String, Boolean> getEid(String fqn, String projectID, boolean withinProject) {
    // Maybe it's in the map
    if (entityMap.containsKey(fqn)) {
      Ent ent = entityMap.get(fqn);
      
      // If it was a collision
      if (COLLISION.equals(entity.getSecond())) {
        
      }
    }
    return null;
  }
  
  private class EntPair extends Pair<String, String> {
    public EntPair(String projectID, String entityID) {
      super(projectID, entityID);
    }
    
    public String getProjectID() {
      return getFirst();
    }
    
    public String getEntityID() {
      return getSecond();
    }
  }
  
  private class Ent {
    private String fqn;
    
    private EntPair main = null;
    private Collection<EntPair> pairs = null;
    private Map<String, EntPair> mainMap = null;

    public Ent(String fqn, String projectID, String entityID) {
      this.fqn = fqn;
      addPair(projectID, entityID);
    }
    
    public void addPair(String projectID, String entityID) {
      EntPair pair = new EntPair(projectID, entityID);
      if (pairs == null && main == null) {
        main = pair;
      } else {
        if (pairs == null) {
          pairs = Helper.newLinkedList();
          pairs.add(main);
          mainMap = Helper.newHashMap();
          main = null;
        }
        pairs.add(pair);
      }
    }
    
    public EntPair getMain(String projectID) {
      if (pairs == null) {
        return main;
      } else {
        EntPair best = mainMap.get(projectID);
        if (best != null) {
          return best;
        } else {
          for (EntPair pair : pairs) {
            if (projectID.equals(pair.getProjectID())) {
              mainMap.put(projectID, pair);
              return pair;
            }
          }
          
          String eid = entitiesTable.insert(Entity.DUPLICATE, fqn, projectID);
          if (eid == null) {
            return null;
          }
          for (EntPair pair : pairs) {
            relationsTable.insert(Relation.MATCHES, eid, pair.getEntityID(), false, projectID);
          }
          best = new EntPair(null, eid);
          mainMap.put(projectID, best);
          return best;
        }
      }
    }
  }
}
