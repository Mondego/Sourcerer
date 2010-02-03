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

import edu.uci.ics.sourcerer.db.util.KeyInsertBatcher;
import edu.uci.ics.sourcerer.db.util.QueryExecutor;
import edu.uci.ics.sourcerer.db.util.ResultTranslator;
import edu.uci.ics.sourcerer.db.util.TableLocker;
import edu.uci.ics.sourcerer.model.Entity;
import edu.uci.ics.sourcerer.model.LocalVariable;
import edu.uci.ics.sourcerer.model.db.LimitedEntityDB;
import edu.uci.ics.sourcerer.model.extracted.EntityEX;
import edu.uci.ics.sourcerer.model.extracted.LocalVariableEX;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public final class EntitiesTable extends DatabaseTable {
  protected EntitiesTable(QueryExecutor executor, TableLocker locker) {
    super(executor, locker, "entities", false);
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
        "fqn VARCHAR(2048) BINARY NOT NULL",
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
  
  public <T> void insert(KeyInsertBatcher<T> batcher, EntityEX entity, String projectID, String fileID, T pairing) {
    batcher.addValue(getInsertValue(entity.getType(), entity.getFqn(), entity.getMods(), null, projectID, fileID, entity.getStartPosition(), entity.getLength()), pairing);
  }
  
  public String insert(Entity type, String fqn, String projectID) {
    return executor.insertSingleWithKey(name, getInsertValue(type, fqn, null, null, projectID, null, null, null));
  }
  
  public String insertUnknown(String fqn, String unknownProject) {
    return insert(Entity.UNKNOWN, fqn, unknownProject);
  }
  
  public String insertArray(String fqn, int size, String projectID) {
    return executor.insertSingleWithKey(name, getInsertValue(Entity.ARRAY, fqn, null, "" + size, projectID, null, null, null));
  }
  
  public String insertLocalVariable(LocalVariableEX var, String projectID, String fileID) {
    Entity type = null;
    if (var.getType() == LocalVariable.LOCAL) {
      type = Entity.LOCAL_VARIABLE;
    } else if (var.getType() == LocalVariable.PARAM) {
      type = Entity.PARAMETER;
    }
    if (fileID == null) {
      return executor.insertSingleWithKey(name, getInsertValue(type, var.getName(), var.getModifiers(), var.getPosition(), projectID, null, null, null));
    } else {
      return executor.insertSingleWithKey(name, getInsertValue(type, var.getName(), var.getModifiers(), var.getPosition(), projectID, fileID, var.getStartPos(), var.getLength()));
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
  
  private static final ResultTranslator<LimitedEntityDB> LIMITED_ENTITY_TRANSLATOR = new ResultTranslator<LimitedEntityDB>() {
    @Override
    public LimitedEntityDB translate(ResultSet result) throws SQLException {
      return new LimitedEntityDB(result.getString(1), result.getString(2), Entity.valueOf(result.getString(3)));
    }
  };
  
  public Collection<String> getProjectIDsByFqn(String fqn) {
    return executor.select(name, "project_id", "fqn='" + fqn + "'");
  }
  
  public Collection<String> getProjectIDsByPackage(String prefix) {
    return executor.select(name, "project_id", "fqn LIKE '" + prefix + ".%' AND entity_type='PACKAGE'");
  }
  
  public Collection<LimitedEntityDB> getLimitedEntitiesByFqn(String fqn, String inClause) {
    return executor.select(name, "project_id,entity_id,entity_type", "fqn='" + fqn + "' AND projectID IN " + inClause, LIMITED_ENTITY_TRANSLATOR);
  }
}
