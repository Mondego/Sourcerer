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
package edu.uci.ics.sourcerer.services.slicer.internal;

import java.io.Closeable;
import java.util.Collection;

import edu.uci.ics.sourcerer.tools.java.db.schema.EntitiesTable;
import edu.uci.ics.sourcerer.tools.java.db.schema.ImportsTable;
import edu.uci.ics.sourcerer.tools.java.db.schema.ProjectsTable;
import edu.uci.ics.sourcerer.tools.java.db.schema.RelationsTable;
import edu.uci.ics.sourcerer.tools.java.model.types.Entity;
import edu.uci.ics.sourcerer.tools.java.model.types.Project;
import edu.uci.ics.sourcerer.tools.java.model.types.Relation;
import edu.uci.ics.sourcerer.util.io.IOUtils;
import edu.uci.ics.sourcerer.utils.db.DatabaseConnection;
import edu.uci.ics.sourcerer.utils.db.DatabaseConnectionFactory;
import edu.uci.ics.sourcerer.utils.db.QueryExecutor;
import edu.uci.ics.sourcerer.utils.db.sql.ConstantCondition;
import edu.uci.ics.sourcerer.utils.db.sql.ResultConstructor;
import edu.uci.ics.sourcerer.utils.db.sql.SelectQuery;
import edu.uci.ics.sourcerer.utils.db.sql.TypedQueryResult;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
class SlicerDatabaseAccessor implements Closeable {
  private DatabaseConnection conn;
  
  private SlicerDatabaseAccessor() {
    conn = DatabaseConnectionFactory.INSTANCE.create();
  }
  
  protected static SlicerDatabaseAccessor create() {
    SlicerDatabaseAccessor acc = new SlicerDatabaseAccessor();
    if (acc.conn.open()) {
      acc.initialize();
      return acc;
    } else {
      return null;
    }
  }

  private SelectQuery getLibraryEntityID;
  private SelectQuery getEntity;
  
  private SelectQuery getRelationTargetsBySource;
  private SelectQuery getRelationSourcesByTarget;
  
  private SelectQuery getContained;
  
  private SelectQuery getImports;
  
  private ConstantCondition<Integer> entityID;
  private ConstantCondition<String> entityFQN;
  private ConstantCondition<Entity> entityType;
  
  private ConstantCondition<Relation> relationType;
  private ConstantCondition<Integer> relationSource;
  private ConstantCondition<Integer> relationTarget;
  
  private ConstantCondition<Integer> importFileID;
  
  private ResultConstructor<SlicedEntityImpl> entityConstructor = new ResultConstructor<SlicedEntityImpl>() {
    @Override
    public SlicedEntityImpl constructResult(TypedQueryResult result) {
      return new SlicedEntityImpl(result);
    }
  };
  
  private ResultConstructor<SlicedImportImpl> importConstructor = new ResultConstructor<SlicedImportImpl>() {
    @Override
    public SlicedImportImpl constructResult(TypedQueryResult result) {
      return new SlicedImportImpl(result);
    }
  };

  private void initialize() {
    QueryExecutor exec = conn.getExecutor();

    Collection<Integer> libraryProjects = null;
    try (SelectQuery select = exec.createSelectQuery(ProjectsTable.TABLE)) {
      select.addSelect(ProjectsTable.PROJECT_ID);
      select.andWhere(ProjectsTable.PROJECT_TYPE.compareEquals(Project.JAVA_LIBRARY));
      libraryProjects = select.select().toCollection(ProjectsTable.PROJECT_ID);
    }
    entityID = EntitiesTable.ENTITY_ID.compareEquals();
    entityFQN = EntitiesTable.FQN.compareEquals();
    entityType = EntitiesTable.ENTITY_TYPE.compareEquals();
        
    getEntity = exec.createSelectQuery(EntitiesTable.TABLE);
    getEntity.addSelect(EntitiesTable.FQN, EntitiesTable.MODIFIERS, EntitiesTable.ENTITY_ID, EntitiesTable.PROJECT_ID, EntitiesTable.ENTITY_TYPE, EntitiesTable.FILE_ID, EntitiesTable.OFFSET, EntitiesTable.LENGTH);
    getEntity.andWhere(entityID);
    
    getLibraryEntityID = exec.createSelectQuery(EntitiesTable.TABLE);
    getLibraryEntityID.addSelect(EntitiesTable.ENTITY_ID);
    getLibraryEntityID.andWhere(entityFQN, EntitiesTable.PROJECT_ID.compareIn(libraryProjects));
    
    relationType = RelationsTable.RELATION_TYPE.compareEquals();
    relationSource = RelationsTable.LHS_EID.compareEquals();
    relationTarget = RelationsTable.RHS_EID.compareEquals();
    
    getRelationTargetsBySource = exec.createSelectQuery(RelationsTable.TABLE);
    getRelationTargetsBySource.addSelect(RelationsTable.RHS_EID);
    getRelationTargetsBySource.andWhere(relationType, relationSource);
    
    getRelationSourcesByTarget = exec.createSelectQuery(RelationsTable.TABLE);
    getRelationSourcesByTarget.addSelect(RelationsTable.LHS_EID);
    getRelationSourcesByTarget.andWhere(relationType, relationTarget);
    
    getContained = exec.createSelectQuery(EntitiesTable.ENTITY_ID.compareEquals(RelationsTable.LHS_EID));
    getContained.addSelect(EntitiesTable.FQN, EntitiesTable.MODIFIERS, EntitiesTable.ENTITY_ID, EntitiesTable.PROJECT_ID, EntitiesTable.ENTITY_TYPE, EntitiesTable.FILE_ID, EntitiesTable.OFFSET, EntitiesTable.LENGTH);
    getContained.andWhere(entityType, relationTarget, RelationsTable.RELATION_TYPE.compareEquals(Relation.INSIDE), EntitiesTable.ENTITY_TYPE.compareEquals(Entity.INITIALIZER));
    
    importFileID = ImportsTable.FILE_ID.compareEquals();
    
    getImports = exec.createSelectQuery(ImportsTable.TABLE);
    getImports.addSelect(ImportsTable.STATIC, ImportsTable.ON_DEMAND, ImportsTable.EID, ImportsTable.OFFSET, ImportsTable.LENGTH);
    getImports.andWhere(importFileID);
  }
  
  public Integer getLibraryEntityID(String fqn) {
    entityFQN.setValue(fqn);
    return getLibraryEntityID.select().toSingleton(EntitiesTable.ENTITY_ID, true);
  }
  
  public Collection<Integer> getRelationTargetsBySource(Relation type, Integer entityID) {
    relationType.setValue(type);
    relationSource.setValue(entityID);
    return getRelationTargetsBySource.select().toCollection(RelationsTable.RHS_EID);
  }
  
  public Collection<Integer> getRelationSourcesByTarget(Relation type, Integer entityID) {
    relationType.setValue(type);
    relationTarget.setValue(entityID);
    return getRelationSourcesByTarget.select().toCollection(RelationsTable.LHS_EID);
  }
  
  public SlicedEntityImpl getEntity(Integer entityID) {
    this.entityID.setValue(entityID);
    return getEntity.select().toSingleton(entityConstructor, false);
  }
  
  public Collection<SlicedEntityImpl> getContained(Entity type, Integer entityID) {
    entityType.setValue(type);
    relationTarget.setValue(entityID);
    return getContained.select().toCollection(entityConstructor);
  }
  
  public Collection<SlicedImportImpl> getImports(Integer fileID) {
    importFileID.setValue(fileID);
    return getImports.select().toCollection(importConstructor);
  }
  
  @Override
  public void close() {
    IOUtils.close(conn);
  }
}
