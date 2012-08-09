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
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.logging.Level;

import edu.uci.ics.sourcerer.tools.java.db.importer.resolver.JavaLibraryTypeModel;
import edu.uci.ics.sourcerer.tools.java.db.importer.resolver.ModeledEntity;
import edu.uci.ics.sourcerer.tools.java.db.importer.resolver.ProjectTypeModel;
import edu.uci.ics.sourcerer.tools.java.db.importer.resolver.UnknownEntityCache;
import edu.uci.ics.sourcerer.tools.java.db.schema.CommentsTable;
import edu.uci.ics.sourcerer.tools.java.db.schema.EntitiesTable;
import edu.uci.ics.sourcerer.tools.java.db.schema.EntityMetricsTable;
import edu.uci.ics.sourcerer.tools.java.db.schema.ImportsTable;
import edu.uci.ics.sourcerer.tools.java.db.schema.RelationsTable;
import edu.uci.ics.sourcerer.tools.java.model.extracted.CommentEX;
import edu.uci.ics.sourcerer.tools.java.model.extracted.EntityEX;
import edu.uci.ics.sourcerer.tools.java.model.extracted.ImportEX;
import edu.uci.ics.sourcerer.tools.java.model.extracted.LocalVariableEX;
import edu.uci.ics.sourcerer.tools.java.model.extracted.RelationEX;
import edu.uci.ics.sourcerer.tools.java.model.extracted.io.ReaderBundle;
import edu.uci.ics.sourcerer.tools.java.model.types.Entity;
import edu.uci.ics.sourcerer.tools.java.model.types.LocalVariable;
import edu.uci.ics.sourcerer.tools.java.model.types.Metric;
import edu.uci.ics.sourcerer.tools.java.model.types.Metrics;
import edu.uci.ics.sourcerer.tools.java.model.types.Relation;
import edu.uci.ics.sourcerer.util.io.logging.TaskProgressLogger;
import edu.uci.ics.sourcerer.utils.db.BatchInserter;
import edu.uci.ics.sourcerer.utils.db.sql.ConstantCondition;
import edu.uci.ics.sourcerer.utils.db.sql.Querier;
import edu.uci.ics.sourcerer.utils.db.sql.Query;
import edu.uci.ics.sourcerer.utils.db.sql.SelectQuery;
import edu.uci.ics.sourcerer.utils.db.sql.Selectable;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public abstract class StructuralRelationsImporter extends RelationsImporter {
  protected Collection<Integer> libraryProjects;

  private Querier<Integer, Iterator<Integer>> getLocalVariables;
  
  protected StructuralRelationsImporter(String taskName, JavaLibraryTypeModel javaModel, UnknownEntityCache unknowns) {
    super(taskName, javaModel, unknowns);
  }
  
  protected final void initializeQueries() {
    getLocalVariables = new Querier<Integer, Iterator<Integer>>(exec) {
      SelectQuery query;
      ConstantCondition<Integer> cond;
      Selectable<Integer> select;
      @Override
      public Query initialize() {
        query = exec.createSelectQuery(EntitiesTable.TABLE);
        cond = EntitiesTable.PROJECT_ID.compareEquals();
        select = EntitiesTable.ENTITY_ID;
        
        query.addSelect(select);
        query.andWhere(cond, EntitiesTable.ENTITY_TYPE.compareIn(EnumSet.of(Entity.PARAMETER, Entity.LOCAL_VARIABLE)));
        query.orderBy(select, true);
        
        return query;
      }

      @Override
      protected Iterator<Integer> selectHelper(Integer input) {
        cond.setValue(input);
        return query.select().toIterable(select).iterator();
      }};
  }
  
  protected final void insert(ReaderBundle reader, Integer projectID, Collection<Integer> externalProjects) {
    loadFileMap(projectID);
    projectModel = ProjectTypeModel.createProjectTypeModel(task, exec, projectID, externalProjects, javaModel, unknowns);
    
    insertRemainingEntities(reader, projectID);
    insertEntityMetrics(reader, projectID);
    insertStructuralRelations(reader, projectID);
    insertImports(reader, projectID);
    insertComments(reader, projectID);
    
    fileMap.clear();
    projectModel = null;
  }
  
  private void insertRemainingEntities(ReaderBundle reader, Integer projectID) {
    task.start("Inserting local variables, parameters and type entities", "entities inserted");
    
    BatchInserter inserter = exec.makeInFileInserter(tempDir, EntitiesTable.TABLE);
    
    TaskProgressLogger processTask = task.createChild();
    processTask.start("Processing local variables and parameters file", "variables processed");
    for (LocalVariableEX var : reader.getTransientLocalVariables()) {
      // Get the file
      Integer fileID = getFileID(var.getLocation());
      
      if (fileID != null) {
        // Add the entity
        inserter.addInsert(EntitiesTable.createInsert(var, projectID, fileID));
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
  
  private void insertEntityMetrics(ReaderBundle reader, Integer projectID) {
    task.start("Inserting entity metrics");
    
    task.start("Processing entities", "entities processed");
    BatchInserter inserter = exec.makeInFileInserter(tempDir, EntityMetricsTable.TABLE);
    
    for (EntityEX entity : reader.getTransientEntities()) {
      Integer fileID = getFileID(entity.getLocation());
      String fqn = entity.getFqn();
      if (entity.getSignature() != null) {
        fqn += entity.getSignature();
      }
      Integer entityID = getDeclaredEntity(fqn, projectID);
      if (fileID != null && entityID != null) {
        Metrics metrics = entity.getMetrics();
        if (metrics != null) {
          for (Entry<Metric, Double> metric : metrics.getMetricValues()) {
            inserter.addInsert(EntityMetricsTable.createInsert(projectID, fileID, entityID, metric.getKey(), metric.getValue()));
          }
        }
      }
      task.progress();
    }
    task.finish();
    
    task.start("Performing db insert");
    inserter.insert();
    task.finish();
    
    task.finish();
  }
  
  private void insertStructuralRelations(ReaderBundle reader, Integer projectID) {
    task.start("Inserting relations");
    
    BatchInserter inserter = exec.makeInFileInserter(tempDir, RelationsTable.TABLE);
    
    task.start("Processing local variables & parameters", "variables processed");
    Iterator<Integer> localVars = getLocalVariables.select(projectID);
    for (LocalVariableEX var : reader.getTransientLocalVariables()) {
      if (localVars.hasNext()) {
        Integer entityID = localVars.next();
        Integer fileID = getFileID(var.getLocation());
        
        if (var.getType() == LocalVariable.PARAM) {
          projectModel.add(var.getParent() + "#" + var.getPosition(), entityID);
        }
        
        // Add the holds relation
        ModeledEntity type = projectModel.getEntity(var.getTypeFqn());
        if (type != null) {
          if (fileID == null) {
            inserter.addInsert(RelationsTable.makeInsert(Relation.HOLDS, type.getRelationClass(), entityID, type.getEntityID(exec, projectID), projectID));
          } else {
            inserter.addInsert(RelationsTable.makeInsert(Relation.HOLDS, type.getRelationClass(), entityID, type.getEntityID(exec, projectID), projectID, fileID, var.getLocation()));
          }
        }
        
        // Add the contains relation
        ModeledEntity parent = projectModel.getEntity(var.getParent());
        if (parent != null) {
          inserter.addInsert(RelationsTable.makeInsert(Relation.CONTAINS, parent.getRelationClass(), parent.getEntityID(exec, projectID), entityID, projectID, fileID));
        }
      } else {
        logger.log(Level.SEVERE, "Missing db local variable for " + var);
      }
      task.progress();
    }
    task.finish();
          
    task.start("Processing structural relations", "relations processed");
    for (RelationEX relation : reader.getTransientRelations()) {
      if (relation.getType() != Relation.CALLS &&
          relation.getType() != Relation.READS &&
          relation.getType() != Relation.WRITES) {
        Integer fileID = getFileID(relation.getLocation());
        
        Integer lhs = getLHS(relation.getLhs(), projectID);
        ModeledEntity rhs = projectModel.getEntity(relation.getRhs());
        
        if (lhs != null && rhs != null) {
          if (fileID == null) {
            inserter.addInsert(RelationsTable.makeInsert(relation.getType(), rhs.getRelationClass(), lhs, rhs.getEntityID(exec, projectID), projectID));
          } else {
            inserter.addInsert(RelationsTable.makeInsert(relation.getType(), rhs.getRelationClass(), lhs, rhs.getEntityID(exec, projectID), projectID, fileID, relation.getLocation()));
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
          inserter.addInsert(ImportsTable.makeInsert(imp.isStatic(), imp.isOnDemand(), imported.getEntityID(exec, projectID), projectID, fileID, imp.getLocation()));
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
        inserter.addInsert(CommentsTable.makeJavadocInsert(owner.getEntityID(exec, projectID), projectID, fileID, comment.getLocation().getOffset(), comment.getLocation().getLength()));
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
