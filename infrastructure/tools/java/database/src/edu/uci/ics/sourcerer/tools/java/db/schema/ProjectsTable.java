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
   *  | source       | VARCHAR(1024) | Yes   | Yes    |
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
  public static final Column<String> SOURCE = TABLE.addVarcharColumn("source", 1024, true);
  public static final Column<String> HASH = TABLE.addVarcharColumn("hash", 32, true).addIndex();
  public static final Column<Boolean> HAS_SOURCE = TABLE.addBooleanColumn("has_source", false).addIndex();
 
  private ProjectsTable() {
    super("projects");
  }
  
  public static enum ProjectState {
    COMPONENT,
    BEGIN_ENTITY,
    END_ENTITY,
    BEGIN_STRUCTURAL,
    END_STRUCTURAL,
    BEGIN_REFERENTIAL,
    ;
    
    public static ProjectState parse(String value) {
      if (value == null) {
        return null;
      } else {
        return valueOf(value);
      }
    }
  }
  
  public static final String PRIMITIVES_PROJECT = "primitives";
  public static final String UNKNOWNS_PROJECT = "unknowns";
  
  // ---- INSERT ----
  public static Insert createRowInsert(Project type, String name, String description, String version, String group, String path, String source, String hash, boolean hasSource) {
    return TABLE.createInsert(
        PROJECT_TYPE.to(type),
        NAME.to(name),
        DESCRIPTION.to(description),
        VERSION.to(version),
        GROUP.to(group),
        PATH.to(path),
        SOURCE.to(source),
        HASH.to(hash),
        HAS_SOURCE.to(hasSource));
  }
  
  public static Insert createPrimitivesInsert() {
    return createRowInsert(Project.SYSTEM, 
            PRIMITIVES_PROJECT,
            "Primitive types",
            null, // no version 
            null, // no group 
            null, // no path
            null, // no source
            null, // no hash 
            false);
  }
  
  public static Insert createUnknownsInsert() {
    return createRowInsert(Project.SYSTEM,
            UNKNOWNS_PROJECT,
            "Project for unknown entities",
            null, // no version
            null, // no group
            null, // no path
            null, // no source
            null, // no hash
            false);
  }
}