package edu.uci.ics.sourcerer.db.tools;

import static edu.uci.ics.sourcerer.util.io.Logging.logger;

import java.util.Collection;
import java.util.Map;
import java.util.logging.Level;

import edu.uci.ics.sourcerer.db.schema.DatabaseAccessor;
import edu.uci.ics.sourcerer.db.util.DatabaseConnection;
import edu.uci.ics.sourcerer.db.util.KeyInsertBatcher;
import edu.uci.ics.sourcerer.model.Comment;
import edu.uci.ics.sourcerer.model.Entity;
import edu.uci.ics.sourcerer.model.Relation;
import edu.uci.ics.sourcerer.model.db.LimitedEntityDB;
import edu.uci.ics.sourcerer.model.extracted.CommentEX;
import edu.uci.ics.sourcerer.model.extracted.EntityEX;
import edu.uci.ics.sourcerer.model.extracted.FileEX;
import edu.uci.ics.sourcerer.model.extracted.ImportEX;
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
    
    logger.info("  Adding the unknowns project...");
    projectsTable.insertUnknownsProject();
    
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

      insertLocalVariables(library, projectID, null);
      insertRelations(library, projectID, null);
      insertImports(library, projectID, null);
      insertComments(library, projectID, null);
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
          ent = new Ent(value.getFqn());
          entityMap.put(value.getFqn(), ent);
        } else {
          logger.log(Level.SEVERE, "FQN collision: " + value.getFqn());
        }
        ent.addPair(projectID, key, value.getType());
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
  
  private void insertLocalVariables(Extracted extracted, String projectID, String inClause) {
    logger.info("    Inserting local variables / parameters...");

    int count = 0;
    for (LocalVariableEX local : extracted.getLocalVariableReader()) {
      // Get the file
      String fileID = getFileID(local.getPath(), local);
      
      // Add the entity
      String eid = entitiesTable.insertLocalVariable(local, projectID, fileID);
      
      // Add the holds relation
      LimitedEntityDB type = getEid(local.getTypeFqn(), projectID, inClause);
      if (fileID == null) {
        relationsTable.insert(Relation.HOLDS, eid, type.getEntityID(), type.isInternal(projectID), projectID);
      } else {
        relationsTable.insert(Relation.HOLDS, eid, type.getEntityID(), type.isInternal(projectID), projectID, fileID, local.getStartPos(), local.getLength());
      }
      
      // Add the inside relation
      LimitedEntityDB parent = getLocalEid(local.getParent(), projectID);
      relationsTable.insert(Relation.INSIDE, eid, parent.getEntityID(), null, projectID, fileID, null, null);
      
      count++;
    }
    relationsTable.flushInserts();
    logger.info("      " + count + " local variables / parameters inserted.");
  }
  
  private void insertRelations(Extracted extracted, String projectID, String inClause) {
    logger.info("    Inserting relations...");
    
    int count = 0;
    for (RelationEX relation : extracted.getRelationReader()) {
      // Get the file
      String fileID = getFileID(relation.getPath(), relation);
      
      // Look up the lhs eid
      LimitedEntityDB lhs = getLocalEid(relation.getLhs(), projectID);
      
      // Look up the rhs eid
      LimitedEntityDB rhs = getEid(relation.getRhs(), projectID, inClause);
      
      // Add the relation
      relationsTable.insert(relation.getType(), lhs.getEntityID(), rhs.getEntityID(), relation.getType() == Relation.INSIDE ? null : rhs.isInternal(projectID), projectID, fileID, relation.getStartPosition(), relation.getLength());
      
      count++;
    }
    relationsTable.flushInserts();
    logger.info("      " + count + " relations inserted.");
  }
  
  private void insertImports(Extracted extracted, String projectID, String inClause) {
    logger.info("    Inserting imports...");
    
    int count = 0;
    for (ImportEX imp : extracted.getImportReader()) {
      // Get the file
      String fileID = getFileID(imp.getPath(), imp);
      
      // Look up the imported entity
      LimitedEntityDB imported = getEid(imp.getImported(), projectID, inClause);
      
      // Add the import
      importsTable.insert(imp.isStatic(), imp.isOnDemand(), imported.getEntityID(), projectID, fileID, imp.getOffset(), imp.getLength());
      
      count++;
    }
    importsTable.flushInserts();
    logger.info("      " + count + " imports inserted.");
  }
  
  private void insertComments(Extracted extracted, String projectID, String inClause) {
    logger.info("    Inserting comments...");
    
    int count = 0;
    for (CommentEX comment : extracted.getCommentReader()) {
      // Get the file
      String fileID = getFileID(comment.getPath(), comment);
      
      if (comment.getType() == Comment.JAVADOC) {
        // Look up the entity
        LimitedEntityDB commented = getLocalEid(comment.getFqn(), projectID);
        
        // Add the comment
        commentsTable.insertJavadoc(commented.getEntityID(), projectID, fileID, comment.getOffset(), comment.getLength());
      } else if (comment.getType() == Comment.UJAVADOC) {
        // Add the comment
        commentsTable.insertUnassociatedJavadoc(projectID, fileID, comment.getOffset(), comment.getLength());
      } else {
        commentsTable.insertComment(comment.getType(), projectID, fileID, comment.getOffset(), comment.getLength());
      }
      count++;
    }
    commentsTable.flushInserts();
    logger.info("      " + count + " comments inserted.");
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
  
  private LimitedEntityDB getLocalEid(String fqn, String projectID) {
    // Maybe it's in the map
    if (entityMap.containsKey(fqn)) {
      Ent ent = entityMap.get(fqn);
      LimitedEntityDB entity = ent.getMain(projectID);
      if (projectID.equals(entity.getProjectID())) {
        return entity;
      } else if (entity.getProjectID() == null && ent.isSingle()) {
        return entity;
      }
    }
    String eid = entitiesTable.insert(Entity.UNKNOWN, fqn, projectID);
    Ent result = new Ent(fqn);
    result.addPair(projectID, eid, Entity.UNKNOWN);
    entityMap.put(fqn, result);
    return result.getMain(projectID);
  }
  
  private LimitedEntityDB getEid(String fqn, String projectID, String inClause) {
    // Maybe it's in the map
    if (entityMap.containsKey(fqn)) {
      return entityMap.get(fqn).getMain(projectID); 
    }
    
    // If it's a method, skip the type entities
    if (!TypeUtils.isMethod(fqn)) {
      if (TypeUtils.isArray(fqn)) {
        Pair<String, Integer> arrayInfo = TypeUtils.breakArray(fqn);
        
        // Insert the array entity
        String eid = entitiesTable.insertArray(fqn, arrayInfo.getSecond(), projectID);
        
        // Insert has elements of relation
        LimitedEntityDB element = getEid(arrayInfo.getFirst(), projectID, inClause);
        relationsTable.insert(Relation.HAS_ELEMENTS_OF, eid, element.getEntityID(), element.isInternal(projectID), projectID);
        
        Ent result = new Ent(fqn);
        result.addPair(projectID, eid, Entity.ARRAY);
        entityMap.put(fqn, result);
        return result.getMain(projectID);
      }
      
      if (TypeUtils.isWildcard(fqn)) {
        // Insert the wildcard entity
        String eid = entitiesTable.insert(Entity.WILDCARD, fqn, projectID);
      
        // If it's bounded, insert the bound relation
        if (!TypeUtils.isUnboundedWildcard(fqn)) {
          LimitedEntityDB bound = getEid(TypeUtils.getWildcardBound(fqn), projectID, inClause);
          if (TypeUtils.isLowerBound(fqn)) {
            relationsTable.insert(Relation.HAS_LOWER_BOUND, eid, bound.getEntityID(), bound.isInternal(projectID), projectID);
          } else {
            relationsTable.insert(Relation.HAS_UPPER_BOUND, eid, bound.getEntityID(), bound.isInternal(projectID), projectID);
          }
        }
        
        Ent result = new Ent(fqn);
        result.addPair(projectID, eid, Entity.WILDCARD);
        entityMap.put(fqn, result);
        return result.getMain(projectID);
      }
      
      if (TypeUtils.isTypeVariable(fqn)) {
        // Insert the type variable entity
        String eid = entitiesTable.insert(Entity.TYPE_VARIABLE, fqn, projectID);
        
        // Insert the bound relations
        for (String boundFqn : TypeUtils.breakTypeVariable(fqn)) {
          LimitedEntityDB bound = getEid(boundFqn, projectID, inClause);
          relationsTable.insert(Relation.HAS_UPPER_BOUND, eid, bound.getEntityID(), bound.isInternal(projectID), projectID);
        }
        
        Ent result = new Ent(fqn);
        result.addPair(projectID, eid, Entity.TYPE_VARIABLE);
        entityMap.put(fqn, result);
        return result.getMain(projectID);
      }
      
      if (TypeUtils.isParametrizedType(fqn)) {
        // Insert the parametrized type entity
        String eid = entitiesTable.insert(Entity.PARAMETERIZED_TYPE, fqn, projectID);
        
        LimitedEntityDB baseType = getEid(TypeUtils.getBaseType(fqn), projectID, inClause);
        
        // Insert the has base type relation
        relationsTable.insert(Relation.HAS_BASE_TYPE, eid, baseType.getEntityID(), baseType.isInternal(projectID), projectID);
        
        // Insert the type arguments
        for (String argFqn : TypeUtils.breakParametrizedType(fqn)) {
          LimitedEntityDB arg = getEid(argFqn, projectID, inClause);
          relationsTable.insert(Relation.HAS_TYPE_ARGUMENT, eid, arg.getEntityID(), arg.isInternal(projectID), projectID);
        }
        
        Ent result = new Ent(fqn);
        result.addPair(projectID, eid, Entity.PARAMETERIZED_TYPE);
        entityMap.put(fqn, result);
        return result.getMain(projectID);
      }
    }
    
    // Some external reference?
    Collection<LimitedEntityDB> entities = entitiesTable.getLimitedEntitiesByFqn(fqn, inClause);
    if (!entities.isEmpty()) {
      Ent result = new Ent(fqn);
      for (LimitedEntityDB entity : entities) {
        result.addPair(entity);
      }
      entityMap.put(fqn, result);
      return result.getMain(projectID);
    }
    
    // Give up
    String eid = entitiesTable.insert(Entity.UNKNOWN, fqn, projectID);
    Ent result = new Ent(fqn);
    result.addPair(projectID, eid, Entity.UNKNOWN);
    entityMap.put(fqn, result);
    return result.getMain(projectID);
  }
  
  private class Ent {
    private String fqn;
    
    private LimitedEntityDB main = null;
    private Collection<LimitedEntityDB> entities = null;
    private Map<String, LimitedEntityDB> mainMap = null;

    public Ent(String fqn) {
      this.fqn = fqn;
    }
    
    public void addPair(String projectID, String entityID, Entity type) {
      LimitedEntityDB entity = new LimitedEntityDB(projectID, entityID, type);
      addPair(entity);
    }
    
    public void addPair(LimitedEntityDB entity) {
      if (entities == null && main == null) {
        main = entity;
      } else {
        if (entities == null) {
          entities = Helper.newLinkedList();
          entities.add(main);
          mainMap = Helper.newHashMap();
          main = null;
        }
        entities.add(entity);
      }
    }
    
    public LimitedEntityDB getMain(String projectID) {
      if (entities == null) {
        return main;
      } else {
        LimitedEntityDB best = mainMap.get(projectID);
        if (best != null) {
          return best;
        } else {
          for (LimitedEntityDB entity : entities) {
            if (projectID.equals(entity.getProjectID())) {
              mainMap.put(projectID, entity);
              return entity;
            }
          }
          
          String eid = entitiesTable.insert(Entity.DUPLICATE, fqn, projectID);
          if (eid == null) {
            return null;
          }
          for (LimitedEntityDB pair : entities) {
            relationsTable.insert(Relation.MATCHES, eid, pair.getEntityID(), false, projectID);
          }
          best = new LimitedEntityDB(projectID, eid, Entity.DUPLICATE);
          mainMap.put(projectID, best);
          return best;
        }
      }
    }
    
    public boolean isSingle() {
      return entities == null;
    }
  }
}
