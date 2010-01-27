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
import edu.uci.ics.sourcerer.model.Project;
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
  private String getInsertValue(Project type, String name, String version, String group, String path, String hash, boolean hasSource) {
    return buildSerialInsertValue(
        convertNotNullVarchar(type.name()),
        convertNotNullVarchar(name),
        convertVarchar(version),
        convertVarchar(group),
        convertVarchar(path),
        convertVarchar(hash),
        convertBoolean(hasSource));
  }
  
  public String insertPrimitivesProject() {
    return executor.insertSingleWithKey(name,
        getInsertValue(Project.JAVA_LIBRARY, 
            "primitives",
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
              null, // no version
              null, // no group 
              null, // no path 
              jar.getHash(), 
              jar.hasSource()));
    } else {
      return executor.insertSingleWithKey(name,
          getInsertValue(
              Project.MAVEN,
              jar.getName(),
              jar.getVersion(),
              jar.getGroup(), 
              null, // no path
              jar.getHash(),
              jar.hasSource()));
    }
  }
  
  public String insert(ExtractedProject project) {
    return executor.insertSingleWithKey(name, 
        getInsertValue(
            Project.CRAWLED,
            project.getName(),
            null, // no version
            null, // no group
            project.getRelativePath(),
            null, // no hash
            true));
  }
  
  // ---- DELETE ----
  public void deleteProject(String projectID) {
    executor.delete(name, "project_id=" + projectID);
  }
  
  // ---- SELECT ----
  public String getProjectCount() {
    return executor.getRowCount(name);
  }
  
  public String getProjectIDByName(String name) {
    return executor.selectSingle(name, "project_id", "name='" + name + "'");
  }
}
