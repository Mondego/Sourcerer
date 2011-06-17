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

import edu.uci.ics.sourcerer.db.util.QueryExecutor;
import edu.uci.ics.sourcerer.db.util.TableLocker;
import edu.uci.ics.sourcerer.db.util.columns.Column;
import edu.uci.ics.sourcerer.db.util.columns.EnumColumn;
import edu.uci.ics.sourcerer.db.util.columns.IntColumn;
import edu.uci.ics.sourcerer.db.util.columns.StringColumn;
import edu.uci.ics.sourcerer.model.Entity;
import edu.uci.ics.sourcerer.model.LocalVariable;
import edu.uci.ics.sourcerer.model.extracted.EntityEX;
import edu.uci.ics.sourcerer.model.extracted.LocalVariableEX;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public final class EntitiesTable extends DatabaseTable {
  public static final String TABLE = "entities";
  
  public EntitiesTable(QueryExecutor executor, TableLocker locker) {
    super(executor, locker, TABLE);
  }

  /*  
   *  +-------------+-----------------+-------+--------+
   *  | Column name | Type            | Null? | Index? |
   *  +-------------+-----------------+-------+--------+
   *  | entity_id   | SERIAL          | No    | Yes    |
   *  | entity_type | ENUM(values)    | No    | Yes    |
   *  | fqn         | VARCHAR(8192)   | No    | Yes    |
   *  | modifiers   | INT UNSIGNED    | Yes   | No     |
   *  | multi       | INT UNSIGNED    | Yes   | No     |
   *  | project_id  | BIGINT UNSIGNED | No    | Yes    |
   *  | file_id     | BIGINT UNSIGNED | Yes   | Yes    |
   *  | offset      | INT UNSIGNED    | Yes   | No     |
   *  | length      | INT UNSIGNED    | Yes   | No     |
   *  +-------------+-----------------+-------+--------+
   */

  public static final Column<Integer> ENTITY_ID = IntColumn.getSerial("entity_id", TABLE);
  public static final Column<Entity> ENTITY_TYPE = new EnumColumn<Entity>("entity_type", TABLE, Entity.values(), false) {
    @Override
    public Entity convertFromDB(String value) {
      return Entity.valueOf(value);
    }
  }.addIndex();
  public static final Column<String> FQN = StringColumn.getVarchar8192NotNull("fqn", TABLE).addIndex(48);
  public static final Column<Integer> MODIFIERS = IntColumn.getUnsignedInt("modifiers", TABLE);
  public static final Column<Integer> MULTI = IntColumn.getUnsignedInt("multi", TABLE);
  public static final Column<Integer> PROJECT_ID = IntColumn.getID("project_id", TABLE).addIndex();
  public static final Column<Integer> FILE_ID = IntColumn.getOptionalID("file_id", TABLE).addIndex();
  public static final Column<Integer> OFFSET = IntColumn.getUnsignedInt("offset", TABLE);
  public static final Column<Integer> LENGTH = IntColumn.getUnsignedInt("length", TABLE);
  
  // ---- CREATE ----
  @Override
  public void createTable() {
    executor.createTable(table,
        ENTITY_ID,
        ENTITY_TYPE,
        FQN,
        MODIFIERS,
        MULTI,
        PROJECT_ID,
        FILE_ID,
        OFFSET,
        LENGTH);
  }
  
  // ---- INSERT ----
  private String getInsertValue(Entity type, String fqn, Integer modifiers, Integer multi, Integer projectID, Integer fileID, Integer offset, Integer length) {
    return buildSerialInsertValue(
        ENTITY_TYPE.convertToDB(type),
        FQN.convertToDB(fqn),
        MODIFIERS.convertToDB(modifiers),
        MULTI.convertToDB(multi),
        PROJECT_ID.convertToDB(projectID),
        FILE_ID.convertToDB(fileID),
        OFFSET.convertToDB(offset),
        LENGTH.convertToDB(length));
  }
  
  private String getInsertValue(EntityEX entity, Integer projectID, Integer fileID) {
    return getInsertValue(entity.getType(), entity.getFqn(), entity.getMods(), null, projectID, fileID, entity.getOffset(), entity.getLength());
  }
 
  public void insert(EntityEX entity, Integer projectID, Integer fileID) {
    inserter.addValue(getInsertValue(entity, projectID, fileID));
  }
 
  public void insert(Entity type, String fqn, Integer projectID) {
    inserter.addValue(getInsertValue(type, fqn, null, null, projectID, null, null, null));
  }
  
  public void forceInsert(Entity type, String fqn, Integer projectID) {
    executor.insertSingle(table, getInsertValue(type, fqn, null, null, projectID, null, null, null));
  }
  
  public Integer forceInsertUnknown(String fqn, Integer projectID) {
    return executor.insertSingleWithKey(table, getInsertValue(Entity.UNKNOWN, fqn, null, null, projectID, null, null, null));
  }
  
  public void insertArray(String fqn, int size, Integer projectID) {
    inserter.addValue(getInsertValue(Entity.ARRAY, fqn, null, size, projectID, null, null, null));
  }
  
  public void insertLocalVariable(LocalVariableEX var, Integer projectID, Integer fileID) {
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
  public void deleteByProjectID(Integer projectID) {
    executor.delete(table, PROJECT_ID.getEquals(projectID));
  }
  
  // ---- SELECT ----
//  public String getFileIDByEid(String eid) {
//    return executor.selectSingle(name, "file_id", "entity_id="+eid);
//  }
//  
//  private static final BasicResultTranslator<LimitedEntityDB> LIMITED_ENTITY_TRANSLATOR = new BasicResultTranslator<LimitedEntityDB>() {
//    @Override
//    public LimitedEntityDB translate(ResultSet result) throws SQLException {
//      return new LimitedEntityDB(result.getString(1), result.getString(2), Entity.valueOf(result.getString(3)));
//    }
//  };
//  
//  private static final BasicResultTranslator<SlightlyLessLimitedEntityDB> SLIGHTLY_LESS_LIMITED_ENTITY_TRANSLATOR = new BasicResultTranslator<SlightlyLessLimitedEntityDB>() {
//    @Override
//    public SlightlyLessLimitedEntityDB translate(ResultSet result) throws SQLException {
//      return new SlightlyLessLimitedEntityDB(result.getString(1), result.getString(2), Entity.valueOf(result.getString(3)), result.getString(4));
//    }
//    
//    @Override
//    public String getTable() {
//      return NAME;
//    }
//    
//    @Override
//    public String getSelect() {
//      return "project_id,entity_id,entity_type,fqn";
//    }
//  };
//  
//  public Collection<String> getProjectIDsByFqn(String fqn) {
//    return executor.select(name, "project_id", "fqn='" + fqn + "'");
//  }
//  
//  public Collection<String> getMavenProjectIDsByFqn(String fqn) {
//    return executor.execute("SELECT projects.project_id FROM projects INNER JOIN entities ON projects.project_id=entities.project_id WHERE project_type='MAVEN' AND fqn='" + fqn + "'", BasicResultTranslator.SIMPLE_RESULT_TRANSLATOR);
//  }
//  
//  public Collection<String> getJarProjectIDsByFqn(String fqn) {
//    return executor.execute("SELECT projects.project_id FROM projects INNER JOIN entities ON projects.project_id=entities.project_id WHERE project_type in ('MAVEN','JAR') AND fqn='" + fqn + "'", BasicResultTranslator.SIMPLE_RESULT_TRANSLATOR);
//  }
//  
//  public Collection<String> getProjectIDsByPackage(String prefix) {
//    return executor.select(name, "project_id", "fqn LIKE '" + prefix + ".%' AND entity_type='PACKAGE'");
//  }
//  
//  public Collection<String> getMavenProjectIDsByPackage(String prefix) {
//    return executor.execute("SELECT projects.project_id FROM projects INNER JOIN entities ON projects.project_id=entities.project_id WHERE project_type='MAVEN' AND fqn LIKE '" + prefix + ".%' AND entity_type='PACKAGE'", BasicResultTranslator.SIMPLE_RESULT_TRANSLATOR);
//  }
//  
//  public Collection<String> getJarProjectIDsByPackage(String prefix) {
//    return executor.execute("SELECT projects.project_id FROM projects INNER JOIN entities ON projects.project_id=entities.project_id WHERE project_type in ('MAVEN','JAR') AND fqn LIKE '" + prefix + ".%' AND entity_type='PACKAGE'", BasicResultTranslator.SIMPLE_RESULT_TRANSLATOR);
//  }
//  
//  public Collection<LimitedEntityDB> getLimitedEntitiesByFqn(String fqn, String inClause) {
//    return executor.select(name, "project_id,entity_id,entity_type", "fqn='" + fqn + "' AND project_id IN " + inClause, LIMITED_ENTITY_TRANSLATOR);
//  }
//  
//  public Iterable<SlightlyLessLimitedEntityDB> getEntityMapByProject(String projectID) {
//    return executor.selectStreamed("project_id=" + projectID + " AND entity_type NOT IN ('PARAMETER','LOCAL_VARIABLE')", SLIGHTLY_LESS_LIMITED_ENTITY_TRANSLATOR);
//  }
//  
//  public Iterable<SlightlyLessLimitedEntityDB> getSyntheticEntitiesByProject(String projectID) {
//    return executor.selectStreamed("entity_type IN ('ARRAY','WILDCARD','TYPE_VARIABLE','PARAMETERIZED_TYPE','DUPLICATE') AND project_id=" + projectID, SLIGHTLY_LESS_LIMITED_ENTITY_TRANSLATOR);
//  }
//  
//  public Iterable<SlightlyLessLimitedEntityDB> getUnknownEntities(String projectID) {
//    return executor.selectStreamed("entity_type='UNKNOWN' AND project_id=" + projectID, SLIGHTLY_LESS_LIMITED_ENTITY_TRANSLATOR);
//  }
//  
//  public Iterable<SlightlyLessLimitedEntityDB> getEntityFqns() {
//    return executor.selectStreamed("entity_type IN ('PACKAGE','CLASS','INTERFACE','ENUM','ANNOTATION')", SLIGHTLY_LESS_LIMITED_ENTITY_TRANSLATOR);
//  }
//  
//  public Iterable<SlightlyLessLimitedEntityDB> getCrawledEntityFqns() {
//    return executor.executeStreamed("SELECT projects.project_id,entity_id,entity_type,fqn FROM entities INNER JOIN projects ON entities.project_id=projects.project_id WHERE entity_type IN ('PACKAGE','CLASS','INTERFACE','ENUM','ANNOTATION') AND project_type='CRAWLED'", SLIGHTLY_LESS_LIMITED_ENTITY_TRANSLATOR);
//  }
//  
//  public Iterable<LimitedEntityDB> getLocalVariablesByProject(String projectID) {
//    return executor.executeStreamed("SELECT project_id,entity_id,entity_type FROM " + name + " WHERE entity_type IN ('LOCAL_VARIABLE','PARAMETER') AND project_id=" + projectID + " ORDER BY entity_id ASC", LIMITED_ENTITY_TRANSLATOR);
//  }
//  
//  public String getEntityIDByFqnAndProject(String fqn, String projectID) {
//    return executor.selectSingle(name, "entity_id", "project_id=" + projectID + " AND fqn='" + fqn + "'");
//  }
//  
//  public LocationDB getLocationByEntityID(String entityID) {
//    return executor.selectSingle(name, LOCATION_RESULT_TRANSLATOR.getSelect(), "entity_id=" + entityID, LOCATION_RESULT_TRANSLATOR); 
//  }
}
