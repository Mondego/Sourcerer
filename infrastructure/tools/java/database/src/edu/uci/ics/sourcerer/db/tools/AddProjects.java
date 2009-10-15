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
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import edu.uci.ics.sourcerer.db.schema.CommentsTable;
import edu.uci.ics.sourcerer.db.schema.EntitiesTable;
import edu.uci.ics.sourcerer.db.schema.FilesTable;
import edu.uci.ics.sourcerer.db.schema.ImportsTable;
import edu.uci.ics.sourcerer.db.schema.JarEntitiesTable;
import edu.uci.ics.sourcerer.db.schema.JarUsesTable;
import edu.uci.ics.sourcerer.db.schema.JarsTable;
import edu.uci.ics.sourcerer.db.schema.LibraryEntitiesTable;
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
import edu.uci.ics.sourcerer.model.db.TypedEntityID;
import edu.uci.ics.sourcerer.model.extracted.CommentEX;
import edu.uci.ics.sourcerer.model.extracted.EntityEX;
import edu.uci.ics.sourcerer.model.extracted.FileEX;
import edu.uci.ics.sourcerer.model.extracted.ImportEX;
import edu.uci.ics.sourcerer.model.extracted.JarEX;
import edu.uci.ics.sourcerer.model.extracted.LocalVariableEX;
import edu.uci.ics.sourcerer.model.extracted.ProblemEX;
import edu.uci.ics.sourcerer.model.extracted.RelationEX;
import edu.uci.ics.sourcerer.repo.JarIndex;
import edu.uci.ics.sourcerer.repo.extracted.ExtractedProject;
import edu.uci.ics.sourcerer.repo.extracted.ExtractedRepository;
import edu.uci.ics.sourcerer.repo.extracted.io.ExtractedReader;
import edu.uci.ics.sourcerer.util.Helper;
import edu.uci.ics.sourcerer.util.io.Logging;
import edu.uci.ics.sourcerer.util.io.PropertyOld;
import edu.uci.ics.sourcerer.util.io.PropertyManager;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class AddProjects extends DatabaseAccessor {
  public AddProjects(DatabaseConnection connection) {
    super(connection);
  }
  
  public void addProjects() {
    Set<String> completed = Logging.initializeResumeLogger();
    
    PropertyManager properties = PropertyManager.getProperties();
    ExtractedRepository extracted = ExtractedRepository.getRepository(properties.getValueAsFile(PropertyOld.REPO_ROOT));
    
    logger.info("Addings jars to database for " + extracted);

    // Get the project list
    Collection<ExtractedProject> projects = extracted.getProjects();
      
    logger.info("Found " + projects.size() + " projects to insert.");
      
    int count = 0;
    for (ExtractedProject project : projects) {
      String name = project.getName();
      logger.info("------------------------");
      logger.info("Inserting " + name + " (" + ++count + " of " + projects.size() + ")");
      if (completed.contains(name) && ProjectsTable.getProjectIDByName(executor, name) != null) {
        logger.info("Already completed!");
      } else {
        addProject(project, extracted.getJarIndex());
      }
    }
  
    logger.info("Done!");
  }
  
  private void addProject(ExtractedProject project, JarIndex jarIndex) {
    String name = project.getName();
    
    // Check if the project was added already
    String oldID = ProjectsTable.getProjectIDByName(executor, name);
    if (oldID != null) {
      logger.info("Deleting existing project...");
      ProjectsTable.deleteProject(executor, oldID);
    }
    
    // Add the project to database
    final String projectID = ProjectsTable.insert(executor, name, project.getRelativePath());

    // Add the files to database
    final Map<String, String> fileMap = Helper.newHashMap();
    {
      executor.execute("LOCK TABLES files WRITE;");
      logger.info("Beginning insert of files...");
      int count = 0;
      KeyInsertBatcher<FileEX> batcher = FilesTable.getKeyInsertBatcher(executor, new KeyInsertBatcher.KeyProcessor<FileEX>() {
        public void processKey(String key, FileEX file) {
          fileMap.put(file.getRelativePath(), key);
        }
      }); 
      for (FileEX file : ExtractedReader.getFileReader(project)) {
        FilesTable.insert(batcher, file, projectID, file);
        count++;
      }
      batcher.insert();
      executor.execute("UNLOCK TABLES;");
      logger.info(count + " files inserted.");
    }
    
    // Add the problems to the database
    {
      executor.execute("LOCK TABLES problems WRITE;");
      logger.info("Beginning insert of problems...");
      int count = 0;
      InsertBatcher batcher = ProblemsTable.getInsertBatcher(executor);
      for (ProblemEX problem : ExtractedReader.getProblemReader(project)) {
        String fileID = fileMap.get(problem.getRelativePath());
        if (fileID != null) {
          ProblemsTable.insert(batcher, problem, fileID, projectID);
          count++;
        } else {
          logger.log(Level.SEVERE, "Unknown file: " + problem.getRelativePath());
        }
      }
      batcher.insert();
      executor.execute("UNLOCK TABLES;");
      logger.info(count + " problems inserted.");
    }
    
    // Add the entities to database
    final Map<String, TypedEntityID> entityMap = Helper.newHashMap();
    {
      executor.execute("LOCK TABLES entities WRITE;");
      logger.info("Beginning insert of entities...");
      int count = 0;
      KeyInsertBatcher<EntityEX> batcher = EntitiesTable.getKeyInsertBatcher(executor, new KeyInsertBatcher.KeyProcessor<EntityEX>() {
        public void processKey(String key, EntityEX entity) {
          entityMap.put(entity.getFqn(), TypedEntityID.getSourceEntityID(key));
        }
      });
      for (EntityEX entity : ExtractedReader.getEntityReader(project)) {
        String fileID = getFileID(fileMap, entity.getPath());
        EntitiesTable.insert(batcher, entity, fileID, projectID, entity);
        count++;
      }
      batcher.insert();
      executor.execute("UNLOCK TABLES;");
      logger.info(count + " entities inserted.");
    }
    
    // Look up the used jars
    String inClause = null;
    {
      StringBuilder inClauseBuilder = new StringBuilder("(");
      for (JarEX jar : ExtractedReader.getJarReader(project)) {
        String jarID = JarsTable.getJarIDByHash(executor, jar.getHash());
        if (jarID == null) {
          logger.log(Level.SEVERE, "Unable to locate jar: " + jar.getHash());
        } else {
          inClauseBuilder.append(jarID).append(',');
          // Add it to the jar uses table
          JarUsesTable.insert(executor, jarID, projectID);
        }
      }
      if (inClauseBuilder.length() > 1) {
        inClauseBuilder.setCharAt(inClauseBuilder.length() - 1, ')');
        inClause = inClauseBuilder.toString();
      }
    }
    
    // Add the imports to the database
    {
      logger.info("Beginning insert of imports...");
      executor.execute("LOCK TABLES imports WRITE, entities WRITE, library_entities READ, jar_entities READ;");
      int count = 0;
      InsertBatcher batcher = ImportsTable.getInsertBatcher(executor);
      InsertBatcher relBatcher = RelationsTable.getInsertBatcher(executor);
      for (ImportEX imp : ExtractedReader.getImportReader(project)) {
        // Look up the file
        String fileID = fileMap.get(imp.getFile());
        if (fileID == null) {
          logger.log(Level.SEVERE, "Missing file for import: " + imp.getFile());
        } else {
          // Look up the entity
          TypedEntityID eid = getEid(relBatcher, entityMap, inClause, projectID, imp.getImported(), false);
          ImportsTable.insert(batcher, imp.isStatic(), imp.isOnDemand(), eid, fileID, projectID);
          count++;
        }
      }
      batcher.insert();
      executor.execute("UNLOCK TABLES;");
      logger.info(count + " imports inserted.");
    }
    
    // Add the local variables to the database
    {
      executor.execute("LOCK TABLES entities WRITE, relations WRITE library_entities READ, jar_entities READ;");
      logger.info("Beginning insert of local variables / parameters...");
      int count = 0;
      InsertBatcher batcher = RelationsTable.getInsertBatcher(executor);
      for (LocalVariableEX var : ExtractedReader.getLocalVariableReader(project)) {
        // Get the file
        String fileID = getFileID(fileMap, var.getPath());
        
        // Add the entity
        String eid = EntitiesTable.insertLocalVariable(executor, var, fileID, projectID);
        
        // Add the holds relation
        TypedEntityID typeEid = getEid(batcher, entityMap, inClause, projectID, var.getTypeFqn(), false);
        RelationsTable.insert(batcher, Relation.HOLDS, eid, typeEid, projectID, fileID, var.getTypeStartPos(), var.getTypeLength());
        
        count++;
      }
      batcher.insert();
      executor.execute("UNLOCK TABLES;");
      logger.info(count + " local variables / parameters inserted.");
    }
    
    // Add the relations to database
    {
      executor.execute("LOCK TABLES relations WRITE, entities WRITE, library_entities READ, jar_entities READ;");
      logger.info("Beginning insert of relations...");
      int count = 0;
      InsertBatcher batcher = RelationsTable.getInsertBatcher(executor);
      for (RelationEX relation : ExtractedReader.getRelationReader(project)) {
        // Get the file
        String fileID = getFileID(fileMap, relation.getPath());

        // Look up the lhs eid
        TypedEntityID lhsEid = entityMap.get(relation.getLhs());
        if (lhsEid == null) {
          logger.log(Level.SEVERE, "Missing lhs for a relation! " + relation.getLhs());
          continue;
        }
        
        // Look up the rhs eid
        TypedEntityID rhsEid = getEid(batcher, entityMap, inClause, projectID, relation.getRhs(), relation.getType() == Relation.INSIDE);
        
        // Add the relation      
        RelationsTable.insert(batcher, relation.getType(), lhsEid.getID(), rhsEid, projectID, fileID, relation.getStartPosition(), relation.getLength());

        count++;
      }
      batcher.insert();
      executor.execute("UNLOCK TABLES;");
      logger.info(count + " relations inserted.");
    }
    
    // Add the comments to the database
    {
      executor.execute("LOCK TABLES comments WRITE, entities READ;");
      logger.info("Beginning insert of comments...");
      int count = 0;
      InsertBatcher batcher = CommentsTable.getInsertBatcher(executor);
      for (CommentEX comment : ExtractedReader.getCommentReader(project)) {
        // If it's a javadoc comment
        if (comment.getType() == Comment.JAVADOC) {
          // Look up the eid
          TypedEntityID eid = entityMap.get(comment.getPath());
          if (eid == null) {
            logger.log(Level.SEVERE, "Missing declared type for comment: " + comment.getPath());
          } else {
            // Look up the file id
            String fid = EntitiesTable.getFileIDByEid(executor, eid.getID());
            
            // Add the comment
            CommentsTable.insertJavadoc(batcher, eid.getID(), projectID, fid, comment.getOffset(), comment.getLength());
          }
        } else {
          // Look up the file id
          String fid = fileMap.get(comment.getPath());
          if (fid == null) {
            logger.log(Level.SEVERE, "Missing file for comment: " + comment.getPath());
          } else {
            // Add the comment
            if (comment.getType() == Comment.UJAVADOC) {
              CommentsTable.insertUnassociatedJavadoc(batcher, projectID, fid, comment.getOffset(), comment.getLength());
            } else {
              CommentsTable.insertComment(batcher, comment.getType(), projectID, fid, comment.getOffset(), comment.getLength());
            }
          }
        }
        count++;
      }
      batcher.insert();
      executor.execute("UNLOCK TABLES;");
      logger.info(count + " comments inserted.");
    }
    
    logger.log(Logging.RESUME, name);
    executor.reset();
  }
  
  private TypedEntityID getEid(InsertBatcher batcher, Map<String, TypedEntityID> entityMap, String inClause, String projectID, String fqn, boolean maybePackage) {
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
        TypedEntityID eid = TypedEntityID.getSourceEntityID(EntitiesTable.insertArray(executor, fqn, dimensions, projectID));
        
        TypedEntityID elementEid = getEid(batcher, entityMap, inClause, projectID, elementFqn, false);
        RelationsTable.insert(batcher, Relation.HAS_ELEMENTS_OF, eid.getID(), elementEid, projectID);
        
        entityMap.put(fqn, eid);
        return eid;
      }
      
      // Or maybe it's a wildcard type
      if (fqn.startsWith("<?") && fqn.endsWith(">")) {
        TypedEntityID eid = TypedEntityID.getSourceEntityID(EntitiesTable.insert(executor, Entity.WILDCARD, fqn, projectID));
        
        if (!fqn.equals("<?>")) {
          boolean isLower = TypeUtils.isLowerBound(fqn);
          TypedEntityID bound = getEid(batcher, entityMap, inClause, projectID, TypeUtils.getWildcardBound(fqn), false);
          if (isLower) {
            RelationsTable.insert(batcher, Relation.HAS_LOWER_BOUND, eid.getID(), bound, projectID);
          } else {
            RelationsTable.insert(batcher, Relation.HAS_UPPER_BOUND, eid.getID(), bound, projectID);
          }
        }
        
        entityMap.put(fqn, eid);
        return eid;
      }
      
      // Or maybe it's a new type variable!
      if (fqn.startsWith("<") && fqn.endsWith(">")) {
        TypedEntityID eid = TypedEntityID.getSourceEntityID(EntitiesTable.insert(executor, Entity.TYPE_VARIABLE, fqn, projectID));
        
        for (String bound : TypeUtils.breakTypeVariable(fqn)) {
          TypedEntityID boundEid = getEid(batcher, entityMap, inClause, projectID, bound, false);
          RelationsTable.insert(batcher, Relation.HAS_UPPER_BOUND, eid.getID(), boundEid, projectID);
        }
        
        entityMap.put(fqn, eid);
        return eid;
      }
      
      // Or a new parametrized type!
      int baseIndex = fqn.indexOf("<");
      if (baseIndex > 0 && fqn.indexOf('>') > baseIndex) {
        TypedEntityID eid = TypedEntityID.getSourceEntityID(EntitiesTable.insert(executor, Entity.PARAMETERIZED_TYPE, fqn, projectID));
        
        String baseType = TypeUtils.getBaseType(fqn);
        TypedEntityID baseTypeEid = getEid(batcher, entityMap, inClause, projectID, baseType, false);
        
        RelationsTable.insert(batcher, Relation.HAS_BASE_TYPE, eid.getID(), baseTypeEid, projectID);
        
        for (String arg : TypeUtils.breakParametrizedType(fqn)) {
          TypedEntityID argEid = getEid(batcher, entityMap, inClause, projectID, arg, false);
          RelationsTable.insert(batcher, Relation.HAS_TYPE_ARGUMENT, eid.getID(), argEid, projectID);
        }
        
        entityMap.put(fqn, eid);
        return eid;
      }
      
      // Perhaps a package
      if (maybePackage) {
        TypedEntityID eid = TypedEntityID.getSourceEntityID(EntitiesTable.insert(executor, Entity.PACKAGE , fqn, projectID));
        entityMap.put(fqn, eid);
        return eid;
      }
    }
    
    // Some Java library reference?
    String eid = LibraryEntitiesTable.getEntityIDByFqn(executor, fqn);
    if (eid != null) {
      TypedEntityID teid = TypedEntityID.getLibraryEntityID(eid);
      entityMap.put(fqn, teid);
      return teid;
    }
    
    // Some jar reference?
    Collection<TypedEntityID> eids = JarEntitiesTable.getEntityIDsByFqn(executor, fqn, inClause);
    if (!eids.isEmpty()) {
      if (eids.size() == 1) {
        TypedEntityID teid = eids.iterator().next();
        entityMap.put(fqn, teid);
        return teid;
      } else {
        String dup = EntitiesTable.insert(executor, Entity.DUPLICATE, fqn, projectID);
        for (TypedEntityID jeid : eids) {
          RelationsTable.insert(batcher, Relation.MATCHES, dup, jeid, projectID);
        }
        entityMap.put(fqn, TypedEntityID.getSourceEntityID(dup));
      }
    }

    // Well, I give up
    TypedEntityID teid = TypedEntityID.getSourceEntityID(EntitiesTable.insert(executor, Entity.UNKNOWN, fqn, projectID));
    entityMap.put(fqn, teid);
    return teid;
  }
  
  private String getFileID(Map<String, String> fileMap, String path) {
    if (fileMap.containsKey(path)) {
      return fileMap.get(path);
    } else {
      // Report the problem
      logger.log(Level.SEVERE, "File not found: " + path);
      return "NULL";
    }
  }
}
