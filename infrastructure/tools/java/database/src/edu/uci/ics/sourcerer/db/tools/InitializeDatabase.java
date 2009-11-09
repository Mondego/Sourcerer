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
import static edu.uci.ics.sourcerer.util.io.Logging.logger;

import java.util.Collection;
import java.util.Map;
import java.util.logging.Level;

import edu.uci.ics.sourcerer.db.schema.CommentsTable;
import edu.uci.ics.sourcerer.db.schema.EntitiesTable;
import edu.uci.ics.sourcerer.db.schema.FilesTable;
import edu.uci.ics.sourcerer.db.schema.ImportsTable;
import edu.uci.ics.sourcerer.db.schema.JarClassFilesTable;
import edu.uci.ics.sourcerer.db.schema.JarCommentsTable;
import edu.uci.ics.sourcerer.db.schema.JarEntitiesTable;
import edu.uci.ics.sourcerer.db.schema.JarImportsTable;
import edu.uci.ics.sourcerer.db.schema.JarProblemsTable;
import edu.uci.ics.sourcerer.db.schema.JarRelationsTable;
import edu.uci.ics.sourcerer.db.schema.JarUsesTable;
import edu.uci.ics.sourcerer.db.schema.JarsTable;
import edu.uci.ics.sourcerer.db.schema.LibrariesTable;
import edu.uci.ics.sourcerer.db.schema.LibraryClassFilesTable;
import edu.uci.ics.sourcerer.db.schema.LibraryCommentsTable;
import edu.uci.ics.sourcerer.db.schema.LibraryEntitiesTable;
import edu.uci.ics.sourcerer.db.schema.LibraryImportsTable;
import edu.uci.ics.sourcerer.db.schema.LibraryProblemsTable;
import edu.uci.ics.sourcerer.db.schema.LibraryRelationsTable;
import edu.uci.ics.sourcerer.db.schema.ProblemsTable;
import edu.uci.ics.sourcerer.db.schema.ProjectsTable;
import edu.uci.ics.sourcerer.db.schema.RelationsTable;
import edu.uci.ics.sourcerer.db.util.DatabaseAccessor;
import edu.uci.ics.sourcerer.db.util.DatabaseConnection;
import edu.uci.ics.sourcerer.db.util.InsertBatcher;
import edu.uci.ics.sourcerer.db.util.KeyInsertBatcher;
import edu.uci.ics.sourcerer.model.Comment;
import edu.uci.ics.sourcerer.model.Entity;
import edu.uci.ics.sourcerer.model.Relation;
import edu.uci.ics.sourcerer.model.extracted.CommentEX;
import edu.uci.ics.sourcerer.model.extracted.EntityEX;
import edu.uci.ics.sourcerer.model.extracted.FileEX;
import edu.uci.ics.sourcerer.model.extracted.ImportEX;
import edu.uci.ics.sourcerer.model.extracted.LocalVariableEX;
import edu.uci.ics.sourcerer.model.extracted.ProblemEX;
import edu.uci.ics.sourcerer.model.extracted.RelationEX;
import edu.uci.ics.sourcerer.repo.extracted.ExtractedLibrary;
import edu.uci.ics.sourcerer.repo.extracted.ExtractedRepository;
import edu.uci.ics.sourcerer.repo.extracted.io.ExtractedReader;
import edu.uci.ics.sourcerer.util.Helper;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class InitializeDatabase extends DatabaseAccessor {
  private Map<String, String> fileMap;
  private Map<String, String> entityMap;
  
  public InitializeDatabase(DatabaseConnection connection) {
    super(connection);
  }
  
  public void initializeDatabase() {
    ExtractedRepository extracted = ExtractedRepository.getRepository(INPUT_REPO.getValue());
    logger.info("Initializing database...");
    {
      executor.dropTables(
          LibrariesTable.TABLE,
          LibraryEntitiesTable.TABLE,
          LibraryRelationsTable.TABLE,
          LibraryClassFilesTable.TABLE,
          LibraryCommentsTable.TABLE,
          LibraryImportsTable.TABLE,
          LibraryProblemsTable.TABLE,
          JarsTable.TABLE,
          JarEntitiesTable.TABLE,
          JarRelationsTable.TABLE,
          JarClassFilesTable.TABLE,
          JarCommentsTable.TABLE,
          JarImportsTable.TABLE,
          JarProblemsTable.TABLE,
          JarUsesTable.TABLE,
          ProjectsTable.TABLE,
          FilesTable.TABLE,
          ProblemsTable.TABLE,
          ImportsTable.TABLE,
          EntitiesTable.TABLE,
          RelationsTable.TABLE,
          CommentsTable.TABLE);
      LibrariesTable.createTable(executor);
      LibraryEntitiesTable.createTable(executor);
      LibraryRelationsTable.createTable(executor);
      LibraryClassFilesTable.createTable(executor);
      LibraryCommentsTable.createTable(executor);
      LibraryImportsTable.createTable(executor);
      LibraryProblemsTable.createTable(executor);
      JarsTable.createTable(executor);
      JarEntitiesTable.createTable(executor);
      JarRelationsTable.createTable(executor);
      JarClassFilesTable.createTable(executor);
      JarCommentsTable.createTable(executor);
      JarImportsTable.createTable(executor);
      JarUsesTable.createTable(executor);
      JarProblemsTable.createTable(executor);
      ProjectsTable.createTable(executor);
      FilesTable.createTable(executor);
      ProblemsTable.createTable(executor);
      ImportsTable.createTable(executor);
      EntitiesTable.createTable(executor);
      RelationsTable.createTable(executor);
      CommentsTable.createTable(executor);
      
      // Add the primitives
      locker.addWrites(LibrariesTable.TABLE, LibraryEntitiesTable.TABLE);
      locker.lock();
      String projectID = LibrariesTable.insertPrimitivesProject(executor);
      InsertBatcher batcher = LibraryEntitiesTable.getInsertBatcher(executor);
      LibraryEntitiesTable.insertPrimitive(batcher, "boolean", projectID);
      LibraryEntitiesTable.insertPrimitive(batcher, "char", projectID);
      LibraryEntitiesTable.insertPrimitive(batcher, "byte", projectID);
      LibraryEntitiesTable.insertPrimitive(batcher, "short", projectID);
      LibraryEntitiesTable.insertPrimitive(batcher, "int", projectID);
      LibraryEntitiesTable.insertPrimitive(batcher, "long", projectID);
      LibraryEntitiesTable.insertPrimitive(batcher, "float", projectID);
      LibraryEntitiesTable.insertPrimitive(batcher, "double", projectID);
      LibraryEntitiesTable.insertPrimitive(batcher, "void", projectID);
      batcher.insert();
      locker.unlock();
    }
    
    // The database was initialized, so add the library jars
    logger.info("Locating all the extracted library jars...");
    Collection<ExtractedLibrary> libraryJars = extracted.getLibraries();
    
    logger.info("--- Inserting " + libraryJars.size() + " libraries into database ---");
    
    int count = 0;
    // Do all the entities first
    fileMap = Helper.newHashMap();
    entityMap = Helper.newHashMap();
    // Lock some tables
    locker.addWrites(LibrariesTable.TABLE, 
        LibraryEntitiesTable.TABLE, 
        LibraryRelationsTable.TABLE, 
        LibraryClassFilesTable.TABLE,
        LibraryProblemsTable.TABLE,
        LibraryImportsTable.TABLE,
        LibraryCommentsTable.TABLE);
    locker.lock();
    // Do the entities first
    for (ExtractedLibrary libraryJar : libraryJars) {
      logger.info("Inserting " + libraryJar.getName() + "'s entities (" + ++count + " of " + libraryJars.size() + ")");
      insertLibraryFirst(libraryJar);
    }
    
    count = 0;
    for (ExtractedLibrary libraryJar : libraryJars) {
      logger.info("Inserting " + libraryJar.getName() + "'s params and relations (" + ++count + " of " + libraryJars.size() + ")");
      insertLibraryRemainder(libraryJar);
    }
    
    // Unlock the tables
    locker.unlock();
    executor.reset();
  }
  
  private void insertLibraryFirst(ExtractedLibrary library) {
    // Add the library to the database
    final String libraryID = LibrariesTable.insert(executor, library);
    
    // Add the files to the database
    {
      logger.info("  Beginning insert of class files...");
      int count = 0;
      KeyInsertBatcher<FileEX> batcher = LibraryClassFilesTable.getKeyInsertBatcher(executor, new KeyInsertBatcher.KeyProcessor<FileEX>() {
        public void processKey(String key, FileEX file) {
          fileMap.put(file.getRelativePath(), key);
        }
      });
      for (FileEX file : ExtractedReader.getFileReader(library)) {
        LibraryClassFilesTable.insert(batcher, file, libraryID, file);
        count++;
      }
      batcher.insert();
      logger.info("    " + count + " files inserted.");
    }
    
    // Add the problems to the database
    {
      logger.info("  Beginning insert of problems...");
      int count = 0;
      InsertBatcher batcher = LibraryProblemsTable.getInsertBatcher(executor);
      for (ProblemEX problem : ExtractedReader.getProblemReader(library)) {
        String fileID = fileMap.get(problem.getRelativePath());
        if (fileID != null) {
          LibraryProblemsTable.insert(batcher, problem, fileID, libraryID);
          count++;
        } else {
          logger.log(Level.SEVERE, "Unknown file: " + problem.getRelativePath() + " for " + problem);
        }
      }
      batcher.insert();
      logger.info("    " + count + " problems inserted.");
    }
    
    // Add the entities to the database
    {
      logger.info("  Beginning insert of entities...");
      int count = 0;
      KeyInsertBatcher<EntityEX> batcher = LibraryEntitiesTable.<EntityEX>getKeyInsertBatcher(executor, new KeyInsertBatcher.KeyProcessor<EntityEX>() {
        public void processKey(String key, EntityEX entity) {
          entityMap.put(entity.getFqn(), key);
        }
      });
      for (EntityEX entity : ExtractedReader.getEntityReader(library)) {
        String fileID = null;
        if (entity.getPath() != null) {
          fileID = fileMap.get(entity.getPath());
          if (fileID == null) {
            logger.log(Level.SEVERE, "Unknown file: " + entity.getPath() + " for " + entity);
          }
        }
        if (fileID == null) {
          LibraryEntitiesTable.insert(batcher, entity, libraryID, entity);
        } else {
          LibraryEntitiesTable.insert(batcher, entity, libraryID, fileID, entity);
        }
        count++;
      }
      batcher.insert();
      logger.info("    " + count + " entities inserted.");
    }
  }
  
  private void insertLibraryRemainder(ExtractedLibrary library) {
    String name = library.getName();
    String libraryID = LibrariesTable.getLibraryIDByName(executor, name);
    {
      InsertBatcher relBatcher = LibraryRelationsTable.getInsertBatcher(executor);     
      
      // Add the local variables to the database
      {
        logger.info("  Beginning insert of local variables / parameters...");
        int count = 0;
        for (LocalVariableEX local: ExtractedReader.getLocalVariableReader(library)) {
          String fileID = null;
          if (local.getPath() != null) {
            fileID = fileMap.get(local.getPath());
            if (fileID == null) {
              logger.log(Level.SEVERE, "Unknown file: " + local.getPath());
            }
          }
          // Add the entity
          String eid = null;
          if (fileID == null) {
            eid = LibraryEntitiesTable.insertLocal(executor, local, libraryID);
          } else {
            eid = LibraryEntitiesTable.insertLocal(executor, local, fileID, libraryID);
          }
        
          // Add the holds relation
          String typeEid = getEid(relBatcher, libraryID, local.getTypeFqn(), false);
          if (fileID == null) {
            LibraryRelationsTable.insert(relBatcher, Relation.HOLDS, eid, typeEid, libraryID);
          } else {
            LibraryRelationsTable.insert(relBatcher, Relation.HOLDS, eid, typeEid, libraryID, fileID, local.getTypeStartPos(), local.getTypeLength());
          }
        
          // Add the inside relation
          String parentEid = getEid(relBatcher, libraryID, local.getParent(), false);
          LibraryRelationsTable.insert(relBatcher, Relation.INSIDE, eid, parentEid, libraryID);
        
          count++;
        }
        relBatcher.insert();
        logger.info("    " + count + " local variables / parameters inserted.");
      }
      
      // Add the relations to the database
      {
        logger.info(  "Beginning insert of relations...");
        int count = 0;
      
        for (RelationEX relation : ExtractedReader.getRelationReader(library)) {
          // Get the file
          String fileID = null;
          if (relation.getPath() != null) {
            fileID = fileMap.get(relation.getPath());
            if (fileID == null) {
              logger.log(Level.SEVERE, "Unknown file: " + relation.getPath());
            }
          }
          
          // Look up the lhs eid
          String lhsEid = entityMap.get(relation.getLhs());
          if (lhsEid == null) {
            logger.log(Level.SEVERE, "Missing lhs for a relation! " + relation.getLhs());
            continue;
          }
          
          // Look up the rhs eid
          String rhsEid = getEid(relBatcher, libraryID, relation.getRhs(), relation.getType() == Relation.INSIDE);
          
          // Add the relation
          LibraryRelationsTable.insert(relBatcher, relation.getType(), lhsEid, rhsEid, libraryID);
          count++;
        }
        relBatcher.insert();
        logger.info("    " + count + " relations inserted.");
      }
      
      // Add the imports to the database
      {
        logger.info("  Beginning insert of imports...");
        int count = 0;
        InsertBatcher batcher = LibraryImportsTable.getInsertBatcher(executor);

        for (ImportEX imp : ExtractedReader.getImportReader(library)) {
          String fileID = fileMap.get(imp.getFile());
          if (fileID != null) {
            String leid = getEid(relBatcher, libraryID, imp.getImported(), imp.isOnDemand());
            LibraryImportsTable.insert(batcher, imp.isStatic(), imp.isOnDemand(), leid, libraryID, fileID, imp.getOffset(), imp.getLength());
            count++;
          } else {
            logger.log(Level.SEVERE, "Unknown file: " + imp.getFile());
          }
        }
        batcher.insert();
        logger.info("    " + count + " imports inserted.");
      }
      
      // Add the comments to the database
      {
        logger.info("  Beginning insert of comments...");
        int count = 0;
        InsertBatcher batcher = LibraryCommentsTable.getInsertBatcher(executor);
        for (CommentEX comment : ExtractedReader.getCommentReader(library)) {
          // Look up the file id
          String fileID = fileMap.get(comment.getPath());
          
          if (fileID == null) {
            logger.log(Level.SEVERE, "Missing file for comment: " + comment.getPath());
          } else {
            // If it's a javadoc comment
            if (comment.getType() == Comment.JAVADOC) {
              // Look up the eid
              String eid = entityMap.get(comment.getFqn());
              if (eid == null) {
                logger.log(Level.SEVERE, "Missing declared type for comment: " + comment.getFqn());
              } else {
                logger.log(Level.SEVERE, "Inserting comment to: " + eid);
                LibraryCommentsTable.insertJavadoc(batcher, eid, libraryID, fileID, comment.getOffset(), comment.getLength());
              }
            } else if (comment.getType() == Comment.UJAVADOC) {
              LibraryCommentsTable.insertUnassociatedJavadoc(batcher, libraryID, fileID, comment.getOffset(), comment.getLength());
            } else {
              LibraryCommentsTable.insertComment(batcher, comment.getType(), libraryID, fileID, comment.getOffset(), comment.getLength());
            }
            count++;
          }
        }
        batcher.insert();
        logger.info("    " + count + " comments inserted.");
      }
    }
  }
  
  private String getEid(InsertBatcher batcher, String libraryID, String fqn, boolean maybePackage) {
    // Maybe it's just in the map
    if (entityMap.containsKey(fqn)) {
      return entityMap.get(fqn);
    }
    
    // If it's a method, skip the type entities
    if (!(fqn.contains("(") && fqn.endsWith(")"))) {
      // Could it be an array we haven't seen?
      if (fqn.endsWith("[]")) {
        int arrIndex = fqn.indexOf("[]");
        String elementFqn = fqn.substring(0, arrIndex);
        int dimensions = (fqn.length() - arrIndex) / 2;
        String eid = LibraryEntitiesTable.insertArray(executor, fqn, dimensions, libraryID);
            
        String elementEid = getEid(batcher, libraryID, elementFqn, false);
        LibraryRelationsTable.insert(batcher, Relation.HAS_ELEMENTS_OF, eid, elementEid, libraryID);
        
        entityMap.put(fqn, eid);
        return eid;
      }
      
      // Or maybe it's a wildcard type
      if (fqn.startsWith("<?") && fqn.endsWith(">")) {
        String eid = LibraryEntitiesTable.insert(executor, Entity.WILDCARD, fqn, libraryID);
        
        if (!fqn.equals("<?>")) {
          boolean isLower = TypeUtils.isLowerBound(fqn);
          String bound = getEid(batcher, libraryID, TypeUtils.getWildcardBound(fqn), false);
          if (isLower) {
            LibraryRelationsTable.insert(batcher, Relation.HAS_LOWER_BOUND, eid, bound, libraryID);
          } else {
            LibraryRelationsTable.insert(batcher, Relation.HAS_UPPER_BOUND, eid, bound, libraryID);
          }
        }
        
        entityMap.put(fqn, eid);
        return eid;
      }
      
      // Or maybe it's a new type variable!
      if (fqn.startsWith("<") && fqn.endsWith(">")) {
        String eid = LibraryEntitiesTable.insert(executor, Entity.TYPE_VARIABLE, fqn, libraryID);
        
        for (String bound : TypeUtils.breakTypeVariable(fqn)) {
          String boundEid = getEid(batcher, libraryID, bound, false);
          LibraryRelationsTable.insert(batcher, Relation.HAS_UPPER_BOUND, eid, boundEid, libraryID);
        }
        
        entityMap.put(fqn, eid);
        return eid;
      }
      
      // Or a new parametrized type!
      int baseIndex = fqn.indexOf("<");
      if (baseIndex > 0 && fqn.indexOf('>') > baseIndex) {
        String eid = LibraryEntitiesTable.insert(executor, Entity.PARAMETERIZED_TYPE, fqn, libraryID);
        
        String baseType = getEid(batcher, libraryID, TypeUtils.getBaseType(fqn), false);
        LibraryRelationsTable.insert(batcher, Relation.HAS_BASE_TYPE, eid, baseType, libraryID);
        
        for (String arg : TypeUtils.breakParametrizedType(fqn)) {
          String argEid = getEid(batcher, libraryID, arg, false);
          LibraryRelationsTable.insert(batcher, Relation.HAS_UPPER_BOUND, eid, argEid, libraryID);
        }
        
        entityMap.put(fqn, eid);
        return eid;
      }
      
      // Perhaps a package
      if (maybePackage) {
        String eid = LibraryEntitiesTable.insert(executor, Entity.PACKAGE , fqn, libraryID);
        entityMap.put(fqn, eid);
        return eid;
      }
    }
    
    // Well, I give up
    String eid = LibraryEntitiesTable.insert(executor, Entity.UNKNOWN, fqn, libraryID);
    entityMap.put(fqn, eid);
    return eid;
  }
}
