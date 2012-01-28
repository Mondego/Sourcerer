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

import java.util.Collection;
import java.util.EnumSet;
import java.util.logging.Level;

import edu.uci.ics.sourcerer.tools.java.db.resolver.JavaLibraryTypeModel;
import edu.uci.ics.sourcerer.tools.java.db.resolver.ModeledEntity;
import edu.uci.ics.sourcerer.tools.java.db.resolver.ProjectTypeModel;
import edu.uci.ics.sourcerer.tools.java.db.resolver.UnknownEntityCache;
import edu.uci.ics.sourcerer.tools.java.db.schema.CommentsTable;
import edu.uci.ics.sourcerer.tools.java.db.schema.EntitiesTable;
import edu.uci.ics.sourcerer.tools.java.db.schema.ImportsTable;
import edu.uci.ics.sourcerer.tools.java.db.schema.RelationsTable;
import edu.uci.ics.sourcerer.tools.java.model.extracted.CommentEX;
import edu.uci.ics.sourcerer.tools.java.model.extracted.ImportEX;
import edu.uci.ics.sourcerer.tools.java.model.extracted.LocalVariableEX;
import edu.uci.ics.sourcerer.tools.java.model.extracted.RelationEX;
import edu.uci.ics.sourcerer.tools.java.model.extracted.io.ReaderBundle;
import edu.uci.ics.sourcerer.tools.java.model.types.Entity;
import edu.uci.ics.sourcerer.tools.java.model.types.LocalVariable;
import edu.uci.ics.sourcerer.tools.java.model.types.Relation;
import edu.uci.ics.sourcerer.util.io.logging.TaskProgressLogger;
import edu.uci.ics.sourcerer.utils.db.BatchInserter;
import edu.uci.ics.sourcerer.utils.db.sql.ConstantCondition;
import edu.uci.ics.sourcerer.utils.db.sql.SelectQuery;
import edu.uci.ics.sourcerer.utils.db.sql.TypedQueryResult;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public abstract class StructuralRelationsImporter extends RelationsImporter {
  protected Collection<Integer> libraryProjects;
    
  private SelectQuery localVariablesQuery;
  private ConstantCondition<Integer> localVariablesQueryProjectID;
  
  protected StructuralRelationsImporter(String taskName, JavaLibraryTypeModel javaModel, UnknownEntityCache unknowns) {
    super(taskName, javaModel, unknowns);
  }
  
//  @Override
//  protected void init() {
//    super.init();
//    try (SelectQuery query = exec.makeSelectQuery(ProjectsTable.TABLE)) {
//      // Get the Java Library projectIDs
//      query.addSelect(ProjectsTable.PROJECT_ID);
//      query.andWhere(ProjectsTable.PROJECT_TYPE.compareEquals(Project.JAVA_LIBRARY));
//      
//      libraryProjects = query.select().toCollection(ProjectsTable.PROJECT_ID);
//      
//      // Get the primitives projectID
//      query.clearWhere();
//
//      query.andWhere(ProjectsTable.NAME.compareEquals(ProjectsTable.PRIMITIVES_PROJECT).and(
//          ProjectsTable.PROJECT_TYPE.compareEquals(Project.SYSTEM)));
//      
//      libraryProjects.add(query.select().toSingleton(ProjectsTable.PROJECT_ID));
//    }
//  }
  
  protected final void initializeQueries() {
    localVariablesQuery = exec.makeSelectQuery(EntitiesTable.TABLE);
    localVariablesQuery.addSelects(EntitiesTable.ENTITY_ID);
    localVariablesQueryProjectID = EntitiesTable.PROJECT_ID.compareEquals();
    localVariablesQuery.andWhere(localVariablesQueryProjectID);
    localVariablesQuery.andWhere(EntitiesTable.ENTITY_TYPE.compareIn(EnumSet.of(Entity.PARAMETER, Entity.LOCAL_VARIABLE)));
    localVariablesQuery.orderBy(EntitiesTable.ENTITY_ID, true);
  }
  
  protected final void insert(ReaderBundle reader, Integer projectID, Collection<Integer> externalProjects) {
    loadFileMap(projectID);
    projectModel = ProjectTypeModel.makeProjectTypeModel(task, exec, projectID, externalProjects, javaModel, unknowns);
    
    insertRemainingEntities(reader, projectID);
    insertStructuralRelations(reader, projectID);
    insertImports(reader, projectID);
    insertComments(reader, projectID);
    
    fileMap.clear();
    projectModel = null;
  }
  
  private void insertRemainingEntities(ReaderBundle reader, Integer projectID) {
    task.start("Inserting local variables, parameters and type entities", "entities inserted");
    
    BatchInserter inserter = exec.makeInFileInserter(tempDir, EntitiesTable.TABLE);
    
    TaskProgressLogger processTask = task.spawnChild();
    processTask.start("Processing local variables and parameters file", "variables processed");
    for (LocalVariableEX var : reader.getTransientLocalVariables()) {
      // Get the file
      Integer fileID = getFileID(var.getLocation());
      
      if (fileID != null) {
        // Add the entity
        inserter.addInsert(EntitiesTable.makeInsert(var, projectID, fileID));
        task.progress();
      }
      processTask.progress();
    }
    processTask.finish();
        
    task.start("Performing db insert");
    inserter.insert();
    task.finish();
    
    task.finish();
  }
  
  private void insertStructuralRelations(ReaderBundle reader, Integer projectID) {
    task.start("Inserting relations");
    
    BatchInserter inserter = exec.makeInFileInserter(tempDir, RelationsTable.TABLE);
    
    task.start("Processing local variables & parameters", "variables processed");
    localVariablesQueryProjectID.setValue(projectID);
    try (TypedQueryResult result = localVariablesQuery.selectStreamed()) {
      for (LocalVariableEX var : reader.getTransientLocalVariables()) {
        if (result.next()) {
          Integer entityID = result.getResult(EntitiesTable.ENTITY_ID);
          Integer fileID = getFileID(var.getLocation());
          
          if (var.getType() == LocalVariable.PARAM) {
            projectModel.add(var.getParent() + "#" + var.getPosition(), entityID);
          }
          
          // Add the holds relation
          ModeledEntity type = projectModel.getEntity(var.getTypeFqn());
          if (type != null) {
            if (fileID == null) {
              inserter.addInsert(RelationsTable.makeInsert(Relation.HOLDS, type.getRelationClass(), entityID, type.getEntityID(), projectID));
            } else {
              inserter.addInsert(RelationsTable.makeInsert(Relation.HOLDS, type.getRelationClass(), entityID, type.getEntityID(), projectID, fileID, var.getLocation()));
            }
          }
          
          // Add the inside relation
          ModeledEntity parent = projectModel.getEntity(var.getParent());
          if (parent != null) {
            inserter.addInsert(RelationsTable.makeInsert(Relation.INSIDE, parent.getRelationClass(), entityID, parent.getEntityID(), projectID, fileID));
          }
        } else {
          logger.log(Level.SEVERE, "Missing db local variable for " + var);
        }
        task.progress();
      }
    }
    task.finish();
          
    task.start("Processing structural relations", "relations processed");
    for (RelationEX relation : reader.getTransientRelations()) {
      if (relation.getType() != Relation.CALLS &&
          relation.getType() != Relation.READS &&
          relation.getType() != Relation.WRITES) {
        Integer fileID = getFileID(relation.getLocation());
        
        Integer lhs = getLHS(relation.getLhs());
        ModeledEntity rhs = projectModel.getEntity(relation.getRhs());
        
        if (lhs != null && rhs != null) {
          if (fileID == null) {
            inserter.addInsert(RelationsTable.makeInsert(relation.getType(), rhs.getRelationClass(), lhs, rhs.getEntityID(), projectID));
          } else {
            inserter.addInsert(RelationsTable.makeInsert(relation.getType(), rhs.getRelationClass(), lhs, rhs.getEntityID(), projectID, fileID, relation.getLocation()));
          }
          task.progress();
        }
      }
    }
    task.finish();
    
    task.start("Performing db insert");
    inserter.insert();
    task.finish();
    
    task.finish();
  }
  
  private void insertImports(ReaderBundle reader, Integer projectID) {
    task.start("Inserting imports");
    
    BatchInserter inserter = exec.makeInFileInserter(tempDir, ImportsTable.TABLE);
    
    task.start("Processing imports", "imports processed");
    for (ImportEX imp : reader.getTransientImports()) {
      Integer fileID = getFileID(imp.getLocation());
      ModeledEntity imported = projectModel.getEntity(imp.getImported());
      
      if (imported != null) {
        if (fileID == null) {
          logger.severe("Missing fileID for: " + imp.getLocation());
        } else {
          inserter.addInsert(ImportsTable.makeInsert(imp.isStatic(), imp.isOnDemand(), imported.getEntityID(), projectID, fileID, imp.getLocation()));
        }
        task.progress();
      }
    }
    task.finish();
    
    task.start("Performing db insert");
    inserter.insert();
    task.finish();
    
    task.finish();
  }
  
  private void insertComments(ReaderBundle reader, Integer projectID) {
    task.start("Inserting comments");
    
    BatchInserter inserter = exec.makeInFileInserter(tempDir, CommentsTable.TABLE);
    
    task.start("Processing comments", "comments processed");
    for (CommentEX comment : reader.getTransientComments()) {
      Integer fileID = getFileID(comment.getLocation());
      ModeledEntity owner = null;
      if (comment.getFqn() != null) {
        owner = projectModel.getEntity(comment.getFqn());
      }
      
      if (fileID == null) {
        logger.severe("Null file for a comment " + comment);
      } else if (owner == null) {
        inserter.addInsert(CommentsTable.makeCommentInsert(comment.getType(), projectID, fileID, comment.getLocation().getOffset(), comment.getLocation().getLength()));
      } else {
        inserter.addInsert(CommentsTable.makeJavadocInsert(owner.getEntityID(), projectID, fileID, comment.getLocation().getOffset(), comment.getLocation().getLength()));
      }
      task.progress();
    }
    task.finish();
    
    task.start("Performing db insert");
    inserter.insert();
    task.finish();
    
    task.finish();
  }
}
