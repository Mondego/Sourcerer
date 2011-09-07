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
package edu.uci.ics.sourcerer.tools.java.db.schema;

import edu.uci.ics.sourcerer.tools.java.model.types.Project;
import edu.uci.ics.sourcerer.tools.java.repo.model.extracted.ExtractedJarFile;
import edu.uci.ics.sourcerer.tools.java.repo.model.extracted.ExtractedJarProperties;
import edu.uci.ics.sourcerer.utils.db.Insert;
import edu.uci.ics.sourcerer.utils.db.sql.Column;
import edu.uci.ics.sourcerer.utils.db.sql.DatabaseTable;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public final class ProjectsTable extends DatabaseTable {
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
  public static final ProjectsTable TABLE = new ProjectsTable();
  
  public static final Column<Integer> PROJECT_ID = TABLE.addSerialColumn("project_id");
  public static final Column<Project> PROJECT_TYPE = TABLE.addEnumColumn("project_type", Project.values(), false).addIndex();
  public static final Column<String> NAME = TABLE.addVarcharColumn("name", 1024, false).addIndex(48);
  public static final Column<String> DESCRIPTION = TABLE.addVarcharColumn("description", 4096, true);
  public static final Column<String> VERSION = TABLE.addVarcharColumn("version", 1024, true);
  public static final Column<String> GROUP = TABLE.addVarcharColumn("groop", 1024, true).addIndex(48);
  public static final Column<String> PATH = TABLE.addVarcharColumn("path", 1024, true);
  public static final Column<String> HASH = TABLE.addVarcharColumn("hash", 32, true).addIndex();
  public static final Column<Boolean> HAS_SOURCE = TABLE.addBooleanColumn("has_source", false).addIndex();
 
  private ProjectsTable() {
    super("projects");
  }
  
  public static final String PRIMITIVES_PROJECT = "primitives";
  public static final String UNKNOWNS_PROJECT = "unknowns";
  
  // ---- INSERT ----
  private Insert makeRowInsert(Project type, String name, String description, String version, String group, String path, String hash, boolean hasSource) {
    return makeInsert(
        PROJECT_TYPE.to(type),
        NAME.to(name),
        DESCRIPTION.to(description),
        VERSION.to(version),
        GROUP.to(group),
        PATH.to(path),
        HASH.to(hash),
        HAS_SOURCE.to(hasSource));
  }
  
  public Insert makePrimitivesInsert() {
    return makeRowInsert(Project.SYSTEM, 
            PRIMITIVES_PROJECT,
            "Primitive types",
            null, // no version 
            null, // no group 
            null, // no path
            null, // no hash 
            false);
  }
  
  public Insert makeUnknownsInsert() {
    return makeRowInsert(Project.SYSTEM,
            UNKNOWNS_PROJECT,
            "Project for unknown entities",
            null, // no version
            null, // no group
            null, // no path
            null, // no hash
            false);
  }
  
  public Insert makeInsert(ExtractedJarFile jar) {
    ExtractedJarProperties props = jar.getProperties();
    Project type = null;
    switch (props.SOURCE.getValue()) {
      case JAVA_LIBRARY: type = Project.JAVA_LIBRARY; break;
      case MAVEN: type = Project.MAVEN; break;
      case PROJECT: type = Project.JAR; break;
    }
    return makeRowInsert(
        type,
        props.NAME.getValue(), 
        null, 
        props.VERSION.getValue(), 
        props.GROUP.getValue(), 
        null, // no path 
        props.HASH.getValue(), 
        props.HAS_SOURCE.getValue());

  }
//  public Integer insert(Extracted item) {
//    if (item instanceof ExtractedLibrary) {
//      return executor.insertSingleWithKey(table, 
//          getInsertValue(Project.JAVA_LIBRARY,
//              item.getName(),
//              null, // no description
//              null, // no version
//              null, // no group
//              item.getRelativePath(),
//              null, // no hash
//              item.hasSource()));
//    } else if (item instanceof ExtractedJar) {
//      if (item.getGroup() == null) {
//        return executor.insertSingleWithKey(table, 
//            getInsertValue(
//                Project.JAR, 
//                item.getName(),
//                null, // no description
//                null, // no version
//                null, // no group 
//                "INVALID", // no path 
//                item.getHash(), 
//                item.hasSource()));
//      } else {
//        return executor.insertSingleWithKey(table,
//            getInsertValue(
//                Project.MAVEN,
//                item.getName(),
//                null, // no description
//                item.getVersion(),
//                item.getGroup(), 
//                "INVALID", // no path
//                item.getHash(),
//                item.hasSource()));
//      }
//    } else if (item instanceof ExtractedProject) {
//      return executor.insertSingleWithKey(table, 
//          getInsertValue(
//              Project.CRAWLED,
//              item.getName(),
//              item.getDescription(),
//              null, // no version
//              null, // no group
//              item.getRelativePath(),
//              "INVALID", // no hash
//              true));
//    } else {
//      logger.log(Level.SEVERE, "Import failed: " + item);
//      return null;
//    }
//  }
//  
//  public void endFirstStageCrawledProjectInsert(Integer projectID) {
//    executor.executeUpdate(table, HASH.getEquals("END_FIRST"), PROJECT_ID.getEquals(projectID));
//  }
//  
//  public void endFirstStageJarProjectInsert(Integer projectID) {
//    executor.executeUpdate(table, PATH.getEquals("END_FIRST"), PROJECT_ID.getEquals(projectID)); 
//  }
//  
//  public void beginSecondStageCrawledProjectInsert(Integer projectID) {
//    executor.executeUpdate(table, HASH.getEquals("BEGIN_SECOND"), PROJECT_ID.getEquals(projectID));
//  }
//  
//  public void beginSecondStageJarProjectInsert(Integer projectID) {
//    executor.executeUpdate(table, PATH.getEquals("BEGIN_SECOND"), PROJECT_ID.getEquals(projectID)); 
//  }
//  
//  public void completeCrawledProjectInsert(Integer projectID) {
//    executor.executeUpdate(table, HASH.setNull(), PROJECT_ID.getEquals(projectID));
//  }
//  
//  public void completeJarProjectInsert(Integer projectID) {
//    executor.executeUpdate(table, PATH.setNull(), PROJECT_ID.getEquals(projectID)); 
//  }
//  
//  // ---- DELETE ----
//  public void deleteProject(Integer projectID) {
//    executor.delete(table, PROJECT_ID.getEquals(projectID));
//  }
  
  // ---- SELECT ----
//  public String getProjectCount() {
//    return executor.getRowCount(name);
//  }
//  
//  public SmallProjectDB getLimitedProjectByPath(String path) {
//    return executor.selectSingle(name, LIMITED_PROJECT_TRANSLATOR.getSelect(), "path='" + path + "'", LIMITED_PROJECT_TRANSLATOR);
//  }
//  
//  public SmallProjectDB getLimitedProjectByHash(String hash) {
//    return executor.selectSingle(name, LIMITED_PROJECT_TRANSLATOR.getSelect(), "hash='" + hash + "'", LIMITED_PROJECT_TRANSLATOR);
//  }
//  
//  public LargeProjectDB getProjectByProjectID(String projectID) {
//    return executor.selectSingle(name, PROJECT_TRANSLATOR.getSelect(), "project_id=" + projectID, PROJECT_TRANSLATOR);
//  }
//  
//  public String getProjectIDByPath(String path) {
//    return executor.selectSingle(name, "project_id", "path='" + path + "'");
//  }
//  
//  public String getProjectIDByName(String project) {
//    return executor.selectSingle(name, "project_id", "name='" + project + "'");
//  }
//  
//  public String getHashByProjectID(String projectID) {
//    return executor.selectSingle(name, "hash", "project_id=" + projectID);
//  }
//  
//  public String getProjectIDByHash(String hash) {
//    return executor.selectSingle(name, "project_id", "hash='" + hash + "'");
//  }
//  
//  public String getUnknownsProject() {
//    return executor.selectSingle(name, "project_id", "name='unknowns'");
//  }
//  
//  public String getPrimitiveProject() {
//    return executor.selectSingle(name, "project_id", "name='primitives'");
//  }
//  
//  public Collection<String> getJavaLibraryProjects() {
//    return executor.select(name, "project_id", "project_type='" + Project.JAVA_LIBRARY + "'");
//  }
}
