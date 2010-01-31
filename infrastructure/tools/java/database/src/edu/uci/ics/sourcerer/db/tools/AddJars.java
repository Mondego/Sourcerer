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

import static edu.uci.ics.sourcerer.repo.general.AbstractRepository.INPUT_REPO;
import static edu.uci.ics.sourcerer.util.io.Logging.RESUME;
import static edu.uci.ics.sourcerer.util.io.Logging.logger;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import edu.uci.ics.sourcerer.db.schema.JarClassFilesTable;
import edu.uci.ics.sourcerer.db.schema.JarCommentsTable;
import edu.uci.ics.sourcerer.db.schema.JarEntitiesTable;
import edu.uci.ics.sourcerer.db.schema.JarImportsTable;
import edu.uci.ics.sourcerer.db.schema.JarProblemsTable;
import edu.uci.ics.sourcerer.db.schema.JarRelationsTable;
import edu.uci.ics.sourcerer.db.schema.JarsTable;
import edu.uci.ics.sourcerer.db.schema.LibraryEntitiesTable;
import edu.uci.ics.sourcerer.db.schema.RelationsTable;
import edu.uci.ics.sourcerer.db.schema.UsedJarsTable;
import edu.uci.ics.sourcerer.db.util.DatabaseAccessor;
import edu.uci.ics.sourcerer.db.util.DatabaseConnection;
import edu.uci.ics.sourcerer.db.util.InsertBatcher;
import edu.uci.ics.sourcerer.db.util.KeyInsertBatcher;
import edu.uci.ics.sourcerer.model.Comment;
import edu.uci.ics.sourcerer.model.Entity;
import edu.uci.ics.sourcerer.model.Relation;
import edu.uci.ics.sourcerer.model.db.TypedEntityID;
import edu.uci.ics.sourcerer.model.extracted.CommentEX;
import edu.uci.ics.sourcerer.model.extracted.EntityEX;
import edu.uci.ics.sourcerer.model.extracted.FileEX;
import edu.uci.ics.sourcerer.model.extracted.ImportEX;
import edu.uci.ics.sourcerer.model.extracted.LocalVariableEX;
import edu.uci.ics.sourcerer.model.extracted.ProblemEX;
import edu.uci.ics.sourcerer.model.extracted.RelationEX;
import edu.uci.ics.sourcerer.model.extracted.UsedJarEX;
import edu.uci.ics.sourcerer.repo.extracted.ExtractedJar;
import edu.uci.ics.sourcerer.repo.extracted.ExtractedRepository;
import edu.uci.ics.sourcerer.repo.extracted.io.ExtractedReader;
import edu.uci.ics.sourcerer.util.Helper;
import edu.uci.ics.sourcerer.util.io.Logging;
import edu.uci.ics.sourcerer.util.io.Property;
import edu.uci.ics.sourcerer.util.io.properties.BooleanProperty;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class AddJars extends DatabaseAccessor {
  public Property<Boolean> ENTITIES_ONLY = new BooleanProperty("entities-only", false, "Database", "Only add entities to database");
  private Set<String> completed;

  public AddJars(DatabaseConnection connection) {
    super(connection);
    completed = Logging.initializeResumeLogger();
  }

  public void addJars() {
    ExtractedRepository extracted = ExtractedRepository.getRepository(INPUT_REPO.getValue());
    
    logger.info("Locating all the extracted jars...");
    
    // Get the jar list
    Collection<ExtractedJar> jars = extracted.getJars();
    
    logger.info("--- Inserting " + jars.size() + " jars into database ---");
    
    int count = 0;
    int size = jars.size();
    if (ENTITIES_ONLY.getValue()) {
      logger.info("-- Inserting jars --");
      for (ExtractedJar jar : jars) {
        if (jar.extracted()) {
          logger.info("Inserting " + jar.getName() + "'s entities " + "(" + ++count + " of " + size + ")");
          addEntities(jar);
        } else {
          logger.info("Skipping " + jar.getName() + "'s entities " + "(" + ++count + " of " + size + ")");
        }
      }
    } else {
      logger.info("-- Inserting lone jars --");
      for (ExtractedJar jar : jars) {
        // in the first go, skip jars that use jars
        if (!jar.usesJars()) {
          if (jar.extracted()) {
            logger.info("Inserting " + jar.getName() + " " + "(" + ++count + " of " + size + ")");
            addJar(jar);
          } else {
            logger.info("Skipping " + jar.getName() + " " + "(" + ++count + " of " + size + ")");
          }
        }
      }
      int newCount = 0;
      logger.info("-- Inserting " + (size - count) + " remaining entities --");
      for (ExtractedJar jar : jars) {
        if (jar.usesJars()) {
          if (jar.extracted()) {
            logger.info("Inserting " + jar.getName() + "'s entities " + "(" + (++newCount + count) + " of " + size + ")");
            addEntities(jar);
          } else {
            logger.info("Skipping " + jar.getName() + " " + "(" + (++newCount + count) + " of " + size + ")");
          }
        }
      }
      newCount = 0;
      logger.info("-- Inserting " + (size - count) + " remaining relations --");
      for (ExtractedJar jar : jars) {
        if (jar.usesJars()) {
          if (jar.extracted()) {
            logger.info("Inserting " + jar.getName() + "'s relations " + "(" + (++newCount + count) + " of " + size + ")");
            addRelations(jar);
          } else {
            logger.info("Skipping " + jar.getName() + " " + "(" + (++newCount + count) + " of " + size + ")");
          }
        }
      }
    }
  }
  
  private void addJar(ExtractedJar jar) {
    Map<String, String> fileMap = Helper.newHashMap();
    Map<String, TypedEntityID> entityMap = Helper.newHashMap();
    String jarID = addEntities(jar, fileMap, entityMap);
    addRelations(jar, jarID, fileMap, entityMap);
  }
  
  private void addEntities(ExtractedJar jar) {
    Map<String, String> fileMap = Helper.newHashMap();
    addEntities(jar, fileMap, null);
  }
  
  private String addEntities(ExtractedJar jar, final Map<String, String> fileMap, final Map<String, TypedEntityID> entityMap) {
    // Check if the jar was added already
    String oldID = JarsTable.getJarIDByHash(executor, jar.getHash());
    if (oldID != null) {
      if (completed.contains("e-" + jar.getHash())) {
        logger.info("  Already inserted.");
        return null;
      } else {
        logger.info("  Deleting existing jar...");
        JarsTable.deleteJar(executor, oldID);
      }
    }
    
    // Add jar to the database
    String jarID = JarsTable.insert(executor, jar);
    
    if (entityMap != null) {
      // Add the files to the database
      {
        logger.info("  Beginning insert of class files...");
        locker.addWrite(JarClassFilesTable.TABLE);
        locker.lock();
        int count = 0;
        KeyInsertBatcher<FileEX> batcher = JarClassFilesTable.getKeyInsertBatcher(executor, new KeyInsertBatcher.KeyProcessor<FileEX>() {
          public void processKey(String key, FileEX file) {
            fileMap.put(file.getRelativePath(), key);
          }
        });
        for (FileEX file : ExtractedReader.getFileReader(jar)) {
          JarClassFilesTable.insert(batcher, file, jarID, file);
          count++;
        }
        batcher.insert();
        locker.unlock();
        logger.info("    " + count + " files inserted.");
      }
      
      // Add the problems to the database
      {
        logger.info("  Beginning insert of problems...");
        locker.addWrite(JarProblemsTable.TABLE);
        locker.lock();
        int count = 0;
        InsertBatcher batcher = JarProblemsTable.getInsertBatcher(executor);
        for (ProblemEX problem : ExtractedReader.getProblemReader(jar)) {
          String fileID = fileMap.get(problem.getRelativePath());
          if (fileID != null) {
            try {
              JarProblemsTable.insert(batcher, problem, jarID, fileID);
              count++;
            } catch (IllegalArgumentException e) {
              logger.log(Level.SEVERE, "Unable to insert problem: " + problem, e);
            }
          } else {
            logger.log(Level.SEVERE, "Unknown file: " + problem.getRelativePath());
          }
        }
        batcher.insert();
        locker.unlock();
        logger.info("    " + count + " problems inserted.");
      }
      
      // Add the entities to the database
      {
        logger.info("  Beginning insert of entities...");
        locker.addWrite(JarEntitiesTable.TABLE);
        locker.lock();
        int count = 0;
        KeyInsertBatcher<EntityEX> batcher = JarEntitiesTable.<EntityEX>getKeyInsertBatcher(executor, new KeyInsertBatcher.KeyProcessor<EntityEX>() {
          @Override
          public void processKey(String key, EntityEX entity) {
            entityMap.put(entity.getFqn(), TypedEntityID.getJarEntityID(key));
          }
        });
        for (EntityEX entity : ExtractedReader.getEntityReader(jar)) {
          String fileID = null;
          if (entity.getPath() != null) {
            fileID = fileMap.get(entity.getPath());
            if (fileID == null) {
              logger.log(Level.SEVERE, "Unknown file: " + entity.getPath());
            }
          }
          
          try {
            if (fileID == null) {
              JarEntitiesTable.insert(batcher, entity, jarID, entity);
            } else {
              JarEntitiesTable.insert(batcher, entity, jarID, fileID, entity);
            }
            count++;
          } catch (IllegalArgumentException e) {
            logger.log(Level.SEVERE, "Unable to insert entity: " + entity, e);
          }
        }
        batcher.insert();
        locker.unlock();
        logger.info("    " + count + " entities inserted.");
      }
    } else {
      if (ENTITIES_ONLY.getValue()) {
        logger.info("  Beginning insert of entities...");
        locker.addWrite(JarEntitiesTable.TABLE);
        locker.lock();
        InsertBatcher batcher = JarEntitiesTable.getInsertBatcher(executor);
        int count = 0;
        for (EntityEX entity : ExtractedReader.getEntityReader(jar)) {
          try {
            JarEntitiesTable.insert(batcher, entity, jarID);
            count++;
          } catch (IllegalArgumentException e) {
            logger.log(Level.SEVERE, "Unable to insert entity: " + entity, e);
          }
        }
        batcher.insert();
        locker.unlock();
        logger.info("    " + count + " entities inserted.");
      } else {
        // Add the files to the database
        {
          logger.info("  Beginning insert of class files...");
          locker.addWrite(JarClassFilesTable.TABLE);
          locker.lock();
          int count = 0;
          KeyInsertBatcher<FileEX> batcher = JarClassFilesTable.getKeyInsertBatcher(executor, new KeyInsertBatcher.KeyProcessor<FileEX>() {
            public void processKey(String key, FileEX file) {
              fileMap.put(file.getRelativePath(), key);
            }
          });
          for (FileEX file : ExtractedReader.getFileReader(jar)) {
            JarClassFilesTable.insert(batcher, file, jarID, file);
            count++;
          }
          batcher.insert();
          locker.unlock();
          logger.info("    " + count + " files inserted.");
        }
        
        // Add the problems to the database
        {
          logger.info("  Beginning insert of problems...");
          locker.addWrite(JarProblemsTable.TABLE);
          locker.lock();
          int count = 0;
          InsertBatcher batcher = JarProblemsTable.getInsertBatcher(executor);
          for (ProblemEX problem : ExtractedReader.getProblemReader(jar)) {
            String fileID = fileMap.get(problem.getRelativePath());
            if (fileID != null) {
              try {
                JarProblemsTable.insert(batcher, problem, jarID, fileID);
                count++;
              } catch (IllegalArgumentException e) {
                logger.log(Level.SEVERE, "Unable to insert problem: " + problem, e);
              }
            } else {
              logger.log(Level.SEVERE, "Unknown file: " + problem.getRelativePath());
            }
          }
          batcher.insert();
          locker.unlock();
          logger.info("    " + count + " problems inserted.");
        }
        
        // Add the entities to the database
        {
          logger.info("  Beginning insert of entities...");
          locker.addWrite(JarEntitiesTable.TABLE);
          locker.lock();
          int count = 0;
          InsertBatcher batcher = JarEntitiesTable.getInsertBatcher(executor);
          for (EntityEX entity : ExtractedReader.getEntityReader(jar)) {
            String fileID = null;
            if (entity.getPath() != null) {
              fileID = fileMap.get(entity.getPath());
              if (fileID == null) {
                logger.log(Level.SEVERE, "Unknown file: " + entity.getPath());
              }
            }
            
            try {
              if (fileID == null) {
                JarEntitiesTable.insert(batcher, entity, jarID);
              } else {
                JarEntitiesTable.insert(batcher, entity, jarID, fileID);
              }
              count++;
            } catch (IllegalArgumentException e) {
              logger.log(Level.SEVERE, "Unable to insert entity: " + entity, e);
            }
          }
          batcher.insert();
          locker.unlock();
          logger.info("    " + count + " entities inserted.");
        }
      }
    }
    logger.log(RESUME, "e-" + jar.getHash());
    return jarID;
  }
  
  public void addRelations(ExtractedJar jar) {
    String jarID = JarsTable.getJarIDByHash(executor, jar.getHash());
    if (jarID == null) {
      logger.log(Level.SEVERE, "May not insert relations before entities: " + jar.getName());
    } else {
      Map<String, String> fileMap = JarClassFilesTable.getFileMap(executor);
      Map<String, TypedEntityID> entityMap = Helper.newHashMap();
      addRelations(jar, jarID, fileMap, entityMap);
    }
  }
  
  public void addRelations(ExtractedJar jar, final String jarID, final Map<String, String> fileMap, final Map<String, TypedEntityID> entityMap) {
    // Check if the jar was added already
    if (completed.contains("r-" + jar.getHash())) {
      logger.info("  Already inserted.");
      return;
    }
    
    // Look up the used jars
    String inClause = null;
    {
      executor.lock(UsedJarsTable.getWriteLock(), JarsTable.getReadLock());
      StringBuilder inClauseBuilder = new StringBuilder("(");
      for (UsedJarEX used : ExtractedReader.getUsedJarReader(jar)) {
        String usedID = JarsTable.getJarIDByHash(executor, used.getHash());
        if (usedID == null) {
          logger.log(Level.SEVERE, "Unable to locate jar: " + used.getHash());
        } else {
          inClauseBuilder.append(usedID);
          // Add it to the used_jars table
          UsedJarsTable.insert(executor, usedID, jarID, null);
        }
      }
      executor.unlock();
      if (inClauseBuilder.length() > 1) {
        inClauseBuilder.setCharAt(inClauseBuilder.length() - 1, ')');
        inClause = inClauseBuilder.toString();
      }
    }
      
    // Add the local variables to the database
    {
      locker.addWrites(JarEntitiesTable.TABLE, JarRelationsTable.TABLE);
      locker.addRead(LibraryEntitiesTable.TABLE);
      locker.lock();
      logger.info("  Beginning insert of local variables / parameters...");
      int count = 0;
      InsertBatcher batcher = JarRelationsTable.getInsertBatcher(executor);
      for (LocalVariableEX var : ExtractedReader.getLocalVariableReader(jar)) {
        String fileID = null;
        if (var.getPath() != null) {
          fileID = fileMap.get(var.getPath());
          if (fileID == null) {
            logger.log(Level.SEVERE, "Unknown file: " + var.getPath());
          }
        }
        // Add the entity
        String eid = null;
        try {
          if (fileID == null) {
            eid = JarEntitiesTable.insertLocal(executor, var, jarID);
          } else {
            eid = JarEntitiesTable.insertLocal(executor, var, jarID, fileID);
          }
          
          // Add the holds relation
          TypedEntityID typeEid = getEid(batcher, entityMap, inClause, jarID, var.getTypeFqn());
          if (fileID == null) {
            JarRelationsTable.insert(batcher, Relation.HOLDS, eid, typeEid, jarID);
          } else {
            JarRelationsTable.insert(batcher, Relation.HOLDS, eid, typeEid, jarID, fileID, var.getTypeStartPos(), var.getTypeLength());
          }
          
          // Add the inside relation
          TypedEntityID parentEid = getEid(batcher, entityMap, inClause, jarID, var.getParent());
          JarRelationsTable.insert(batcher, Relation.INSIDE, eid, parentEid, jarID);
        } catch (IllegalArgumentException e) {
          logger.log(Level.SEVERE, "Unable to insert local: " + var, e);
        }
        
        count++;
      }
      batcher.insert();
      locker.unlock();
      logger.info("    " + count + " local variables / parameters inserted.");
    }
    
    // Add the relations to the database
    {
      logger.info("  Beginning insert of relations...");
      locker.addWrites(JarEntitiesTable.TABLE, JarRelationsTable.TABLE);
      locker.addRead(LibraryEntitiesTable.TABLE);
      locker.lock();
      int count = 0;
      InsertBatcher batcher = JarRelationsTable.getInsertBatcher(executor);
      for (RelationEX relation : ExtractedReader.getRelationReader(jar)) {
        String fileID = null;
        if (relation.getPath() != null) {
          fileID = fileMap.get(relation.getPath());
          if (fileID == null) {
            logger.log(Level.SEVERE, "Unknown file: " + relation.getPath());
          }
        }
        
        // Look up the lhs eid
        TypedEntityID lhsEid = checkEntityMap(entityMap, relation.getLhs(), jarID);
        if (lhsEid == null) {
          logger.log(Level.SEVERE, "Missing lhs for a relation! " + relation.getLhs());
          continue;
        }
        
        // Look up the rhs eid
        TypedEntityID rhsEid = getEid(batcher, entityMap, inClause, jarID, relation.getRhs());
        
        // Add the relation
        try {
          if (fileID == null) {
            JarRelationsTable.insert(batcher, relation.getType(), lhsEid.getID(), rhsEid, jarID);
          } else {
            JarRelationsTable.insert(batcher, relation.getType(), lhsEid.getID(), rhsEid, jarID, fileID, relation.getStartPosition(), relation.getLength());
          }
          count++;
        } catch (IllegalArgumentException e) {
          logger.log(Level.SEVERE, "Unable to insert relation: " + relation, e);
        }
      }
      batcher.insert();
      locker.unlock();
      logger.info("    " + count + " relations inserted.");
    }
    
    // Add the imports to the database
    {
      logger.info("  Beginning insert of imports...");
      locker.addWrites(JarImportsTable.TABLE, JarEntitiesTable.TABLE, JarRelationsTable.TABLE);
      locker.addRead(LibraryEntitiesTable.TABLE);
      locker.lock();
      int count = 0;
      InsertBatcher batcher = JarImportsTable.getInsertBatcher(executor);
      InsertBatcher relBatcher = RelationsTable.getInsertBatcher(executor);
      for (ImportEX imp : ExtractedReader.getImportReader(jar)) {
        String fileID = fileMap.get(imp.getFile());
        if (fileID == null) {
          logger.log(Level.SEVERE, "Missing file for import: " + imp.getFile());
        } else {
          // Look up the entity
          TypedEntityID eid = getEid(relBatcher, entityMap, inClause, jarID, imp.getImported());
          try {
            JarImportsTable.insert(batcher, imp.isStatic(), imp.isOnDemand(), eid, jarID, fileID, imp.getOffset(), imp.getLength());
            count++;
          } catch (IllegalArgumentException e) {
            logger.log(Level.SEVERE, "Unable to insert import: " + imp, e);
          }
        }
      }
      batcher.insert();
      relBatcher.insert();
      locker.unlock();
      logger.info("    " + count + " imports inserted.");
    }
    
    // Add the comments to the database
    {
      logger.info("  Beginning insert of comments...");
      locker.addWrite(JarCommentsTable.TABLE);
      locker.addRead(JarEntitiesTable.TABLE);
      locker.lock();
      int count = 0;
      InsertBatcher batcher = JarCommentsTable.getInsertBatcher(executor);
      for (CommentEX comment : ExtractedReader.getCommentReader(jar)) {
        // Look up the file id
        String fileID = fileMap.get(comment.getPath());

        if (fileID == null) {
          logger.log(Level.SEVERE, "Missing file for comment: " + comment.getPath());
        } else {
          try {
            // If it's a javadoc comment
            if (comment.getType() == Comment.JAVADOC) {
              // Look up the eid
              TypedEntityID eid = checkEntityMap(entityMap, comment.getFqn(), jarID);
              if (eid == null) {
                logger.log(Level.SEVERE, "Missing declared type for comment: " + comment.getFqn());
              } else {
                JarCommentsTable.insertJavadoc(batcher, eid.getID(), jarID, fileID, comment.getOffset(), comment.getLength());
              }
            } else if (comment.getType() == Comment.UJAVADOC) {
              JarCommentsTable.insertUnassociatedJavadoc(batcher, jarID, fileID, comment.getOffset(), comment.getLength());
            } else {
              JarCommentsTable.insertComment(batcher, comment.getType(), jarID, fileID, comment.getOffset(), comment.getLength());
            }
            count++;
          } catch (IllegalArgumentException e) {
            logger.log(Level.SEVERE, "Unable to insert comment: " + comment, e);
          }
        }
      }
      batcher.insert();
      locker.unlock();
      logger.info("    " + count + " comments inserted.");
    }

    logger.log(RESUME, "r-" + jar.getHash());
    executor.reset();
  }
  
  private TypedEntityID checkEntityMap(Map<String, TypedEntityID> entityMap, String fqn, String jarID) {
    if (entityMap.containsKey(fqn)) {
      return entityMap.get(fqn);
    }
    
    // Maybe it's from this project
    String eid = JarEntitiesTable.getEntityIDByFqn(executor, fqn, jarID);
    if (eid == null) {
      return null;
    } else {
      TypedEntityID teid = TypedEntityID.getJarEntityID(eid);
      entityMap.put(fqn, teid);
      return teid;
    }
  }
  
  private TypedEntityID getEid(InsertBatcher batcher, Map<String, TypedEntityID> entityMap, String inClause, String jarID, String fqn) {
    // Maybe it's just in the map
    TypedEntityID teid = checkEntityMap(entityMap, fqn, jarID);
    if (teid != null) {
      return teid;
    }
    
    // If it's a method, skip the type entities
    if (!(fqn.contains("(") && fqn.endsWith(")"))) {
      // Could it be an array we haven't seen?
      if (fqn.endsWith("[]")) {
        int arrIndex = fqn.indexOf("[]");
        String elementFqn = fqn.substring(0, arrIndex);
        int dimensions = (fqn.length() - arrIndex) / 2;
        TypedEntityID eid = TypedEntityID.getJarEntityID(JarEntitiesTable.insertArray(executor, fqn, dimensions, jarID));
            
        TypedEntityID elementEid = getEid(batcher, entityMap, inClause, jarID, elementFqn);
        JarRelationsTable.insert(batcher, Relation.HAS_ELEMENTS_OF, eid.getID(), elementEid, jarID);
        entityMap.put(fqn, eid);
        return eid;
      }
      
      // Or maybe it's a wildcard type
      if (fqn.startsWith("<?") && fqn.endsWith(">")) {
        TypedEntityID eid = TypedEntityID.getJarEntityID(JarEntitiesTable.insert(executor, Entity.WILDCARD, fqn, jarID));
        
        if (!fqn.equals("<?>")) {
          boolean isLower = TypeUtils.isLowerBound(fqn);
          TypedEntityID bound = getEid(batcher, entityMap, inClause, jarID, TypeUtils.getWildcardBound(fqn));
          if (isLower) {
            JarRelationsTable.insert(batcher, Relation.HAS_LOWER_BOUND, eid.getID(), bound, jarID);
          } else {
            JarRelationsTable.insert(batcher, Relation.HAS_UPPER_BOUND, eid.getID(), bound, jarID);
          }
        }
        
        entityMap.put(fqn, eid);
        return eid;
      }
      
      // Or maybe it's a new type variable!
      if (fqn.startsWith("<") && fqn.endsWith(">")) {
        TypedEntityID eid = TypedEntityID.getJarEntityID(JarEntitiesTable.insert(executor, Entity.TYPE_VARIABLE, fqn, jarID));
        
        for (String bound : TypeUtils.breakTypeVariable(fqn)) {
          TypedEntityID boundEid = getEid(batcher, entityMap, inClause, jarID, bound);
          JarRelationsTable.insert(batcher, Relation.HAS_UPPER_BOUND, eid.getID(), boundEid, jarID);
        }
        
        entityMap.put(fqn, eid);
        return eid;
      }
      
      // Or a new parametrized type!
      int baseIndex = fqn.indexOf("<");
      if (baseIndex > 0 && fqn.indexOf('>') > baseIndex) {
        TypedEntityID eid = TypedEntityID.getJarEntityID(JarEntitiesTable.insert(executor, Entity.PARAMETERIZED_TYPE, fqn, jarID));
        
        String baseType = TypeUtils.getBaseType(fqn);
        TypedEntityID baseTypeEid = getEid(batcher, entityMap, inClause, jarID, baseType);
        
        JarRelationsTable.insert(batcher, Relation.HAS_BASE_TYPE, eid.getID(), baseTypeEid, jarID);
        
        for (String arg : TypeUtils.breakParametrizedType(fqn)) {
          TypedEntityID argEid = getEid(batcher, entityMap, inClause, jarID, arg);
          JarRelationsTable.insert(batcher, Relation.HAS_TYPE_ARGUMENT, eid.getID(), argEid, jarID);
        }
        
        entityMap.put(fqn, eid);
        return eid;
      }
    }
    
    // Some Java library reference?
    String eid = LibraryEntitiesTable.getFilteredEntityIDByFqn(executor, fqn);
    if (eid != null) {
      teid = TypedEntityID.getLibraryEntityID(eid);
      entityMap.put(fqn, teid);
      return teid;
    }
    
    // Some jar reference?
    Collection<TypedEntityID> eids = JarEntitiesTable.getFilteredEntityIDsByFqn(executor, fqn, inClause);
    if (!eids.isEmpty()) {
      if (eids.size() == 1) {
        teid = eids.iterator().next();
        entityMap.put(fqn, teid);
        return teid;
      } else {
        String dup = JarEntitiesTable.insert(executor, Entity.DUPLICATE, fqn, jarID);
        for (TypedEntityID jeid : eids) {
          JarRelationsTable.insert(batcher, Relation.MATCHES, dup, jeid, jarID);
        }
        teid = TypedEntityID.getJarEntityID(dup);
        entityMap.put(fqn, teid);
        return teid;
      }
    }
    
    // Well, I give up
    teid = TypedEntityID.getJarEntityID(JarEntitiesTable.insert(executor, Entity.UNKNOWN, fqn, jarID));
    entityMap.put(fqn, teid);
    return teid;
  }
}
