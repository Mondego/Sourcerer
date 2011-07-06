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
package edu.uci.ics.sourcerer.db.queries;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

import edu.uci.ics.sourcerer.db.schema.EntitiesTable;
import edu.uci.ics.sourcerer.db.schema.FilesTable;
import edu.uci.ics.sourcerer.db.schema.ImportsTable;
import edu.uci.ics.sourcerer.db.schema.ProjectsTable;
import edu.uci.ics.sourcerer.db.schema.RelationsTable;
import edu.uci.ics.sourcerer.model.Entity;
import edu.uci.ics.sourcerer.model.Project;
import edu.uci.ics.sourcerer.model.Relation;
import edu.uci.ics.sourcerer.model.RelationClass;
import edu.uci.ics.sourcerer.model.db.FileFqn;
import edu.uci.ics.sourcerer.model.db.ImportFqn;
import edu.uci.ics.sourcerer.model.db.MediumEntityDB;
import edu.uci.ics.sourcerer.utils.db.BasicResultTranslator;
import edu.uci.ics.sourcerer.utils.db.QueryExecutor;
import edu.uci.ics.sourcerer.utils.db.ResultTranslator;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public final class JoinQueries extends Queries {
  protected JoinQueries(QueryExecutor executor) {
    super(executor);
  }
   
  public Collection<Integer> getJarProjectIDsByFqn(String fqn) {
    return executor.select(
        join(ProjectsTable.TABLE, EntitiesTable.TABLE) + on(ProjectsTable.PROJECT_ID.getEquals(EntitiesTable.PROJECT_ID)),
        ProjectsTable.PROJECT_ID.getQualifiedName(),
        and(ProjectsTable.PROJECT_TYPE.getIn(Project.MAVEN, Project.JAR), EntitiesTable.FQN.getEquals(fqn)),
        ResultTranslator.SIMPLE_INT_TRANSLATOR);
  }
  
  public Collection<Integer> getJarProjectIDsByPackage(String fqn) {
    return executor.select(
        join(ProjectsTable.TABLE, EntitiesTable.TABLE) + on(ProjectsTable.PROJECT_ID.getEquals(EntitiesTable.PROJECT_ID)),
        ProjectsTable.PROJECT_ID.getQualifiedName(),
        and(ProjectsTable.PROJECT_TYPE.getIn(Project.MAVEN, Project.JAR), EntitiesTable.FQN.getLike(fqn + ".%")),
        ResultTranslator.SIMPLE_INT_TRANSLATOR);
  }
  
  /**
   * Get all FQNs and positions of entities that are directly inside of a package.
   */
  public Iterable<FileFqn> getFileFqns() {
    return executor.selectStreamed(
        join(ProjectsTable.TABLE, FilesTable.TABLE, EntitiesTable.TABLE + " as e1", RelationsTable.TABLE, EntitiesTable.TABLE + " as e2") +
        on(ProjectsTable.PROJECT_ID.getEquals(FilesTable.PROJECT_ID), FilesTable.FILE_ID.getEquals(EntitiesTable.FILE_ID, "e1"), EntitiesTable.ENTITY_ID.getEquals("e1", RelationsTable.LHS_EID), RelationsTable.RHS_EID.getEquals(EntitiesTable.ENTITY_ID, "e2")),
        comma(EntitiesTable.FQN.getQualifiedName("e1"), EntitiesTable.ENTITY_ID.getQualifiedName("e1"), FilesTable.PATH.getQualifiedName(), FilesTable.FILE_ID.getQualifiedName(), ProjectsTable.PATH.getQualifiedName(), ProjectsTable.PROJECT_ID.getQualifiedName()),
        and(RelationsTable.RELATION_TYPE.getEquals(Relation.INSIDE), EntitiesTable.ENTITY_TYPE.getQualifierEquals(Entity.PACKAGE, "e2"), ProjectsTable.PROJECT_TYPE.getEquals(Project.CRAWLED)),
        new BasicResultTranslator<FileFqn>() {
          @Override
          public FileFqn translate(ResultSet result) throws SQLException {
            return new FileFqn(EntitiesTable.FQN.convertFromDB(result.getString(1)),
                EntitiesTable.ENTITY_ID.convertFromDB(result.getString(2)),
                FilesTable.PATH.convertFromDB(result.getString(3)),
                FilesTable.FILE_ID.convertFromDB(result.getString(4)),
                ProjectsTable.PATH.convertFromDB(result.getString(5)),
                ProjectsTable.PROJECT_ID.convertFromDB(result.getString(6)));
          }
        });
  }
  
  /**
   * Get the contained FQNs for a given entity.
   */
  public Collection<MediumEntityDB> getContainedEntities(Integer entityID) {
    return executor.select(
        join(RelationsTable.TABLE, EntitiesTable.TABLE) +
        on(RelationsTable.LHS_EID.getEquals(EntitiesTable.ENTITY_ID)),
        comma(EntitiesTable.ENTITY_ID.getQualifiedName(), EntitiesTable.ENTITY_TYPE.getQualifiedName(), EntitiesTable.FQN.getQualifiedName(), EntitiesTable.PROJECT_ID.getQualifiedName()),
        and(RelationsTable.RHS_EID.getEquals(entityID), RelationsTable.RELATION_TYPE.getEquals(Relation.INSIDE)), 
        new BasicResultTranslator<MediumEntityDB>() {
          @Override
          public MediumEntityDB translate(ResultSet result) throws SQLException {
            return new MediumEntityDB(EntitiesTable.ENTITY_ID.convertFromDB(result.getString(1)), EntitiesTable.ENTITY_TYPE.convertFromDB(result.getString(2)), EntitiesTable.FQN.convertFromDB(result.getString(3)), EntitiesTable.PROJECT_ID.convertFromDB(result.getString(4)));
          }});
  }
  
  /**
   * Get all of the used FQNs
   */
  public Iterable<MediumEntityDB> getUsedExternalFQNs() {
    return executor.selectStreamed(
        join(RelationsTable.TABLE, EntitiesTable.TABLE) +
        on(RelationsTable.RHS_EID.getEquals(EntitiesTable.ENTITY_ID)),
        comma(EntitiesTable.FQN.getQualifiedName(), EntitiesTable.ENTITY_ID.getQualifiedName(), EntitiesTable.ENTITY_TYPE.getQualifiedName(), EntitiesTable.PROJECT_ID.getQualifiedName()),
        and(RelationsTable.RELATION_CLASS.getIn(RelationClass.EXTERNAL, RelationClass.JAVA_LIBRARY, RelationClass.UNKNOWN), RelationsTable.RELATION_TYPE.getIn(Relation.CALLS, Relation.INSTANTIATES)),
        new BasicResultTranslator<MediumEntityDB>() {
          @Override
          public MediumEntityDB translate(ResultSet result) throws SQLException {
            return new MediumEntityDB(
                EntitiesTable.ENTITY_ID.convertFromDB(result.getString(2)), 
                EntitiesTable.ENTITY_TYPE.convertFromDB(result.getString(3)),
                EntitiesTable.FQN.convertFromDB(result.getString(1)),
                EntitiesTable.PROJECT_ID.convertFromDB(result.getString(4)));
          }
        });
  }
  
  public Iterable<ImportFqn> getImportFqns() {
    return executor.selectStreamed(
        join(ImportsTable.TABLE, EntitiesTable.TABLE) +
        on(ImportsTable.EID.getEquals(EntitiesTable.ENTITY_ID)),
        comma(ImportsTable.ON_DEMAND.getQualifiedName(), EntitiesTable.FQN.getQualifiedName(), ImportsTable.PROJECT_ID.getQualifiedName(), ImportsTable.FILE_ID.getQualifiedName()),
        null,
        new BasicResultTranslator<ImportFqn>() {
          @Override
          public ImportFqn translate(ResultSet result) throws SQLException {
            return new ImportFqn(
                ImportsTable.ON_DEMAND.convertFromDB(result.getString(1)),
                EntitiesTable.FQN.convertFromDB(result.getString(2)),
                ImportsTable.PROJECT_ID.convertFromDB(result.getString(3)),
                ImportsTable.FILE_ID.convertFromDB(result.getString(4)));
          }
        });
  }
}
