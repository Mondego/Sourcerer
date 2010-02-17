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
import edu.uci.ics.sourcerer.model.Project;
import edu.uci.ics.sourcerer.model.db.LimitedProjectDB;
import edu.uci.ics.sourcerer.repo.extracted.ExtractedJar;
import edu.uci.ics.sourcerer.repo.extracted.ExtractedLibrary;
import edu.uci.ics.sourcerer.repo.extracted.ExtractedProject;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public final class ProjectsTable extends DatabaseTable {
  protected ProjectsTable(QueryExecutor executor, TableLocker locker) {
    super(executor, locker, "projects", false);
  }
  /*  
   *  +--------------+---------------+-------+--------+
   *  | Column name  | Type          | Null? | Index? |
   *  +--------------+---------------+-------+--------+
   *  | project_id   | SERIAL        | No    | Yes    |
   *  | project_type | ENUM(values)  | No    | Yes    |
   *  | name         | VARCHAR(1024) | No    | Yes    |
   *  | description  | VARCHAR(4096) | Yes   | No     |
   *  | version      | VARCHAR(1024) | Yes   | No     |
   *  | groop        | VARCHAR(1024) | Yes   | Yes    |
   *  | path         | VARCHAR(1024) | Yes   | No     |
   *  | hash         | VARCHAR(32)   | Yes   | Yes    |
   *  | has_source   | BOOLEAN       | No    | Yes    |
   *  +--------------+---------------+-------+--------+
   */
  
  // ---- CREATE ----
  public void createTable() {
    executor.createTable(name,
        "project_id SERIAL",
        "project_type " + getEnumCreate(Project.values()) + " NOT NULL",
        "name VARCHAR(1024) BINARY NOT NULL",
        "description VARCHAR(4096) BINARY",
        "version VARCHAR(1024) BINARY",
        "groop VARCHAR(1024) BINARY",
        "path VARCHAR(1024) BINARY",
        "hash VARCHAR(32) BINARY",
        "has_source BOOLEAN NOT NULL",
        "INDEX(project_type)",
        "INDEX(name(48))",
        "INDEX(groop(48))",
        "INDEX(hash)",
        "INDEX(has_source)");
  }
  
  // ---- INSERT ----
  private String getInsertValue(Project type, String name, String description, String version, String group, String path, String hash, boolean hasSource) {
    return buildSerialInsertValue(
        convertNotNullVarchar(type.name()),
        convertNotNullVarchar(name),
        convertVarchar(description),
        convertVarchar(version),
        convertVarchar(group),
        convertVarchar(path),
        convertVarchar(hash),
        convertNotNullBoolean(hasSource));
  }
  
  public String insertPrimitivesProject() {
    return executor.insertSingleWithKey(name,
        getInsertValue(Project.SYSTEM, 
            "primitives",
            "Primitive types",
            null, // no version 
            null, // no group 
            null, // no path
            null, // no hash 
            false));
  }
  
  public String insertUnknownsProject() {
    return executor.insertSingleWithKey(name,
        getInsertValue(Project.SYSTEM,
            "unknowns",
            "Project for unknown entities",
            null, // no version
            null, // no group
            null, // no path
            null, // no hash
            false));
  }
  
  public String insert(ExtractedLibrary library) {
    return executor.insertSingleWithKey(name, 
        getInsertValue(Project.JAVA_LIBRARY,
            library.getName(),
            null, // no description
            null, // no version
            null, // no group
            library.getRelativePath(),
            null, // no hash
            library.hasSource()));
  }
  
  public String insert(ExtractedJar jar) {
    if (jar.getGroup() == null) {
      return executor.insertSingleWithKey(name, 
          getInsertValue(
              Project.JAR, 
              jar.getName(),
              null, // no description
              null, // no version
              null, // no group 
              "INVALID", // no path 
              jar.getHash(), 
              jar.hasSource()));
    } else {
      return executor.insertSingleWithKey(name,
          getInsertValue(
              Project.MAVEN,
              jar.getName(),
              null, // no description
              jar.getVersion(),
              jar.getGroup(), 
              "INVALID", // no path
              jar.getHash(),
              jar.hasSource()));
    }
  }
  
  public String insert(ExtractedProject project) {
    return executor.insertSingleWithKey(name, 
        getInsertValue(
            Project.CRAWLED,
            project.getName(),
            project.getDescription(),
            null, // no version
            null, // no group
            project.getRelativePath(),
            "INVALID", // no hash
            true));
  }
  
  public void completeCrawledProjectInsert(String projectID) {
    executor.executeUpdate("UPDATE " + name + " SET hash = NULL where project_id=" + projectID);
  }
  
  public void completeJarProjectInsert(String projectID) {
    executor.executeUpdate("UPDATE " + name + " SET path = NULL where project_id=" + projectID); 
  }
  
  // ---- DELETE ----
  public void deleteProject(String projectID) {
    executor.delete(name, "project_id=" + projectID);
  }
  
  // ---- SELECT ----
  private ResultTranslator<LimitedProjectDB> LIMITED_PROJECT_TRANSLATOR = new ResultTranslator<LimitedProjectDB>() {
    @Override
    public LimitedProjectDB translate(ResultSet result) throws SQLException {
      return new LimitedProjectDB(result.getString(1), Project.valueOf(result.getString(2)), result.getString(3), result.getString(4));
    }
  };
  
  public String getProjectCount() {
    return executor.getRowCount(name);
  }
  
  public LimitedProjectDB getLimitedProjectByPath(String path) {
    return executor.selectSingle(name, "project_id,project_type,path,hash", "path='" + path + "'", LIMITED_PROJECT_TRANSLATOR);
  }
  
  public LimitedProjectDB getLimitedProjectByHash(String hash) {
    return executor.selectSingle(name, "project_id,project_type,path,hash", "hash='" + hash + "'", LIMITED_PROJECT_TRANSLATOR);
  }
  
  public String getProjectIDByName(String project) {
    return executor.selectSingle(name, "project_id", "name='" + project + "'");
  }
  
  public String getHashByProjectID(String projectID) {
    return executor.selectSingle(name, "hash", "project_id=" + projectID);
  }
  
  public String getProjectIDByHash(String hash) {
    return executor.selectSingle(name, "project_id", "hash='" + hash + "'");
  }
  
  public String getUnknownsProject() {
    return executor.selectSingle(name, "project_id", "name='unknowns'");
  }
  
  public String getPrimitiveProject() {
    return executor.selectSingle(name, "project_id", "name='primitives'");
  }
  
  public Collection<String> getJavaLibraryProjects() {
    return executor.select(name, "project_id", "project_type='" + Project.JAVA_LIBRARY + "'");
  }
}
