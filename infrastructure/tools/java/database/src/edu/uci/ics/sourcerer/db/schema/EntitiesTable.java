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
package edu.uci.ics.sourcerer.db.schema;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

import edu.uci.ics.sourcerer.db.util.QueryExecutor;
import edu.uci.ics.sourcerer.db.util.ResultTranslator;
import edu.uci.ics.sourcerer.db.util.TableLocker;
import edu.uci.ics.sourcerer.model.Entity;
import edu.uci.ics.sourcerer.model.LocalVariable;
import edu.uci.ics.sourcerer.model.db.LimitedEntityDB;
import edu.uci.ics.sourcerer.model.db.LocationDB;
import edu.uci.ics.sourcerer.model.db.SlightlyLessLimitedEntityDB;
import edu.uci.ics.sourcerer.model.extracted.EntityEX;
import edu.uci.ics.sourcerer.model.extracted.LocalVariableEX;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public final class EntitiesTable extends DatabaseTable {
  private static final String NAME = "entities";
  protected EntitiesTable(QueryExecutor executor, TableLocker locker) {
    super(executor, locker, NAME);
  }

  /*  
   *  +-------------+-----------------+-------+--------+
   *  | Column name | Type            | Null? | Index? |
   *  +-------------+-----------------+-------+--------+
   *  | entity_id   | SERIAL          | No    | Yes    |
   *  | entity_type | ENUM(values)    | No    | Yes    |
   *  | fqn         | VARCHAR(2048)   | No    | Yes    |
   *  | modifiers   | INT UNSIGNED    | Yes   | No     |
   *  | multi       | INT UNSIGNED    | Yes   | No     |
   *  | project_id  | BIGINT UNSIGNED | No    | Yes    |
   *  | file_id     | BIGINT UNSIGNED | Yes   | Yes    |
   *  | offset      | INT UNSIGNED    | Yes   | No     |
   *  | length      | INT UNSIGNED    | Yes   | No     |
   *  +-------------+-----------------+-------+--------+
   */

  // ---- CREATE ----
  public void createTable() {
    executor.createTable(name,
        "entity_id SERIAL",
        "entity_type " + getEnumCreate(Entity.values()) + " NOT NULL",
        "fqn VARCHAR(8192) BINARY NOT NULL",
        "modifiers INT UNSIGNED",
        "multi INT UNSIGNED",
        "project_id BIGINT UNSIGNED NOT NULL",
        "file_id BIGINT UNSIGNED",
        "offset INT UNSIGNED",
        "length INT UNSIGNED",
        "INDEX(entity_type)",
        "INDEX(fqn(48))",
        "INDEX(project_id)",
        "INDEX(file_id)");
  }
  
  // ---- INSERT ----
  private String getInsertValue(Entity type, String fqn, String modifiers, String multi, String projectID, String fileID, String offset, String length) {
    return buildSerialInsertValue(
        convertNotNullVarchar(type.name()),
        convertNotNullVarchar(fqn),
        convertNumber(modifiers),
        convertNumber(multi),
        convertNotNullNumber(projectID),
        convertNumber(fileID),
        convertOffset(offset),
        convertLength(length));
  }
  
  private String getInsertValue(EntityEX entity, String projectID, String fileID) {
    return getInsertValue(entity.getType(), entity.getFqn(), entity.getMods(), null, projectID, fileID, entity.getStartPosition(), entity.getLength());
  }
 
  public void insert(EntityEX entity, String projectID, String fileID) {
    inserter.addValue(getInsertValue(entity, projectID, fileID));
  }
 
  public void insert(Entity type, String fqn, String projectID) {
    inserter.addValue(getInsertValue(type, fqn, null, null, projectID, null, null, null));
  }
  
  public void forceInsert(Entity type, String fqn, String projectID) {
    executor.insertSingle(name, getInsertValue(type, fqn, null, null, projectID, null, null, null));
  }
  
  public void insertArray(String fqn, int size, String projectID) {
    inserter.addValue(getInsertValue(Entity.ARRAY, fqn, null, "" + size, projectID, null, null, null));
  }
  
  public void insertLocalVariable(LocalVariableEX var, String projectID, String fileID) {
    Entity type = null;
    if (var.getType() == LocalVariable.LOCAL) {
      type = Entity.LOCAL_VARIABLE;
    } else if (var.getType() == LocalVariable.PARAM) {
      type = Entity.PARAMETER;
    }
    if (fileID == null) {
      inserter.addValue(getInsertValue(type, var.getName(), var.getModifiers(), var.getPosition(), projectID, null, null, null));
    } else {
      inserter.addValue(getInsertValue(type, var.getName(), var.getModifiers(), var.getPosition(), projectID, fileID, var.getStartPos(), var.getLength()));
    }
  }
  
  // ---- DELETE ----
  public void deleteByProjectID(String projectID) {
    executor.delete(name, "project_id=" + projectID);
  }
  
  // ---- SELECT ----
  public String getFileIDByEid(String eid) {
    return executor.selectSingle(name, "file_id", "entity_id="+eid);
  }
  
  protected static final ResultTranslator<LocationDB> LOCATION_RESULT_TRANSLATOR = new ResultTranslator<LocationDB>() {
    @Override
    public LocationDB translate(ResultSet result) throws SQLException {
      return new LocationDB(result.getString(1), result.getInt(2), result.getInt(3));
    }
    
    @Override
    public String getTable() {
      return NAME;
    }
    
    @Override
    public String getSelect() {
      return "file_id,offset,length";
    }
  };
  
  private static final ResultTranslator<LimitedEntityDB> LIMITED_ENTITY_TRANSLATOR = new ResultTranslator<LimitedEntityDB>() {
    @Override
    public LimitedEntityDB translate(ResultSet result) throws SQLException {
      return new LimitedEntityDB(result.getString(1), result.getString(2), Entity.valueOf(result.getString(3)));
    }
  };
  
  private static final ResultTranslator<SlightlyLessLimitedEntityDB> SLIGHTLY_LESS_LIMITED_ENTITY_TRANSLATOR = new ResultTranslator<SlightlyLessLimitedEntityDB>() {
    @Override
    public SlightlyLessLimitedEntityDB translate(ResultSet result) throws SQLException {
      return new SlightlyLessLimitedEntityDB(result.getString(1), result.getString(2), Entity.valueOf(result.getString(3)), result.getString(4));
    }
    
    @Override
    public String getTable() {
      return NAME;
    }
    
    @Override
    public String getSelect() {
      return "project_id,entity_id,entity_type,fqn";
    }
  };
  
  public Collection<String> getProjectIDsByFqn(String fqn) {
    return executor.select(name, "project_id", "fqn='" + fqn + "'");
  }
  
  public Collection<String> getMavenProjectIDsByFqn(String fqn) {
    return executor.execute("SELECT projects.project_id FROM projects INNER JOIN entities ON projects.project_id=entities.project_id WHERE project_type='MAVEN' AND fqn='" + fqn + "'", ResultTranslator.SIMPLE_RESULT_TRANSLATOR);
  }
  
  public Collection<String> getProjectIDsByPackage(String prefix) {
    return executor.select(name, "project_id", "fqn LIKE '" + prefix + ".%' AND entity_type='PACKAGE'");
  }
  
  public Collection<String> getMavenProjectIDsByPackage(String prefix) {
    return executor.execute("SELECT projects.project_id FROM projects INNER JOIN entities ON projects.project_id=entities.project_id WHERE project_type='MAVEN' AND fqn LIKE '" + prefix + ".%' AND entity_type='PACKAGE'", ResultTranslator.SIMPLE_RESULT_TRANSLATOR);
  }
  
  public Collection<LimitedEntityDB> getLimitedEntitiesByFqn(String fqn, String inClause) {
    return executor.select(name, "project_id,entity_id,entity_type", "fqn='" + fqn + "' AND project_id IN " + inClause, LIMITED_ENTITY_TRANSLATOR);
  }
  
  public Iterable<SlightlyLessLimitedEntityDB> getEntityMapByProject(String projectID) {
    return executor.selectStreamed("project_id=" + projectID + " AND entity_type NOT IN ('PARAMETER','LOCAL_VARIABLE')", SLIGHTLY_LESS_LIMITED_ENTITY_TRANSLATOR);
  }
  
  public Iterable<SlightlyLessLimitedEntityDB> getSyntheticEntitiesByProject(String projectID) {
    return executor.selectStreamed("entity_type IN ('ARRAY','WILDCARD','TYPE_VARIABLE','PARAMETERIZED_TYPE','DUPLICATE') AND project_id=" + projectID, SLIGHTLY_LESS_LIMITED_ENTITY_TRANSLATOR);
  }
  
  public Iterable<SlightlyLessLimitedEntityDB> getUnknownEntities(String projectID) {
    return executor.selectStreamed("entity_type='UNKNOWN' AND project_id=" + projectID, SLIGHTLY_LESS_LIMITED_ENTITY_TRANSLATOR);
  }
  
  public Iterable<SlightlyLessLimitedEntityDB> getEntityFqns() {
    return executor.selectStreamed("entity_type IN ('PACKAGE','CLASS','INTERFACE','ENUM','ANNOTATION')", SLIGHTLY_LESS_LIMITED_ENTITY_TRANSLATOR);
  }
  
  public Iterable<SlightlyLessLimitedEntityDB> getCrawledEntityFqns() {
    return executor.executeStreamed("SELECT projects.project_id,entity_id,entity_type,fqn FROM entities INNER JOIN projects ON entities.project_id=projects.project_id WHERE entity_type IN ('PACKAGE','CLASS','INTERFACE','ENUM','ANNOTATION') AND project_type='CRAWLED'", SLIGHTLY_LESS_LIMITED_ENTITY_TRANSLATOR);
  }
  
  public Iterable<LimitedEntityDB> getLocalVariablesByProject(String projectID) {
    return executor.executeStreamed("SELECT project_id,entity_id,entity_type FROM " + name + " WHERE entity_type IN ('LOCAL_VARIABLE','PARAMETER') AND project_id=" + projectID + " ORDER BY entity_id ASC", LIMITED_ENTITY_TRANSLATOR);
  }
  
  public String getEntityIDByFqnAndProject(String fqn, String projectID) {
    return executor.selectSingle(name, "entity_id", "project_id=" + projectID + " AND fqn='" + fqn + "'");
  }
  
  public LocationDB getLocationByEntityID(String entityID) {
    return executor.selectSingle(name, LOCATION_RESULT_TRANSLATOR.getSelect(), "entity_id=" + entityID, LOCATION_RESULT_TRANSLATOR); 
  }
}
