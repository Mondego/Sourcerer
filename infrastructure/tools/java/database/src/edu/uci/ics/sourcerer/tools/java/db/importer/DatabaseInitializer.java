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

import java.util.EnumSet;

import edu.uci.ics.sourcerer.tools.java.db.schema.CommentsTable;
import edu.uci.ics.sourcerer.tools.java.db.schema.EntitiesTable;
import edu.uci.ics.sourcerer.tools.java.db.schema.EntityMetricsTable;
import edu.uci.ics.sourcerer.tools.java.db.schema.FileMetricsTable;
import edu.uci.ics.sourcerer.tools.java.db.schema.FilesTable;
import edu.uci.ics.sourcerer.tools.java.db.schema.ImportsTable;
import edu.uci.ics.sourcerer.tools.java.db.schema.ProblemsTable;
import edu.uci.ics.sourcerer.tools.java.db.schema.ProjectMetricsTable;
import edu.uci.ics.sourcerer.tools.java.db.schema.ProjectsTable;
import edu.uci.ics.sourcerer.tools.java.db.schema.RelationsTable;
import edu.uci.ics.sourcerer.tools.java.model.types.Entity;
import edu.uci.ics.sourcerer.tools.java.model.types.Project;
import edu.uci.ics.sourcerer.util.io.logging.TaskProgressLogger;
import edu.uci.ics.sourcerer.utils.db.DatabaseRunnable;
import edu.uci.ics.sourcerer.utils.db.sql.DeleteStatement;
import edu.uci.ics.sourcerer.utils.db.sql.SelectQuery;
import edu.uci.ics.sourcerer.utils.db.sql.SetStatement;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class DatabaseInitializer {
  private DatabaseInitializer() {}
  
  public static void initializeDatabase() {
    new DatabaseRunnable() {
      @Override
      public void action() {
        TaskProgressLogger task = TaskProgressLogger.get();
        task.start("Initializing database");
        
        task.start("Dropping old tables");
        exec.dropTables(
            CommentsTable.TABLE,
            EntitiesTable.TABLE,
            EntityMetricsTable.TABLE,
            FileMetricsTable.TABLE,
            FilesTable.TABLE,
            ImportsTable.TABLE,
            ProblemsTable.TABLE,
            ProjectMetricsTable.TABLE,
            ProjectsTable.TABLE,
            RelationsTable.TABLE);
        task.finish();
        
        task.start("Creating new tables");
        exec.createTables(
            CommentsTable.TABLE,
            EntitiesTable.TABLE,
            EntityMetricsTable.TABLE,
            FileMetricsTable.TABLE,
            FilesTable.TABLE,
            ImportsTable.TABLE,
            ProblemsTable.TABLE,
            ProjectMetricsTable.TABLE,
            ProjectsTable.TABLE,
            RelationsTable.TABLE);
        task.finish();
        
        task.start("Adding the primitive types");
        Integer projectID = exec.insertWithKey(ProjectsTable.createPrimitivesInsert());
        exec.insert(EntitiesTable.makeInsert(Entity.PRIMITIVE, "boolean",  projectID));
        exec.insert(EntitiesTable.makeInsert(Entity.PRIMITIVE, "char", projectID));
        exec.insert(EntitiesTable.makeInsert(Entity.PRIMITIVE, "byte", projectID));
        exec.insert(EntitiesTable.makeInsert(Entity.PRIMITIVE, "short", projectID));
        exec.insert(EntitiesTable.makeInsert(Entity.PRIMITIVE, "int", projectID));
        exec.insert(EntitiesTable.makeInsert(Entity.PRIMITIVE, "long", projectID));
        exec.insert(EntitiesTable.makeInsert(Entity.PRIMITIVE, "float", projectID));
        exec.insert(EntitiesTable.makeInsert(Entity.PRIMITIVE, "double", projectID));
        exec.insert(EntitiesTable.makeInsert(Entity.PRIMITIVE, "void", projectID));
        task.finish();
        
        task.start("Adding the unknowns project");
        exec.insert(ProjectsTable.createUnknownsInsert());
        task.finish();
        
        task.finish();
      }
    }.run();
  }
  
  public static void cleanExtractionData() {
    new DatabaseRunnable() {
      @Override
      protected void action() {
        TaskProgressLogger task = TaskProgressLogger.get();
        task.start("Cleaning database of extraction results");
        
        task.start("Dropping old tables");
        exec.dropTables(
            CommentsTable.TABLE,
            EntitiesTable.TABLE,
            EntityMetricsTable.TABLE,
            FileMetricsTable.TABLE,
            FilesTable.TABLE,
            ImportsTable.TABLE,
            ProblemsTable.TABLE,
            ProjectMetricsTable.TABLE,
            RelationsTable.TABLE);
        task.finish();
        
        task.start("Creating new tables");
        exec.createTables(
            CommentsTable.TABLE,
            EntitiesTable.TABLE,
            EntityMetricsTable.TABLE,
            FileMetricsTable.TABLE,
            FilesTable.TABLE,
            ImportsTable.TABLE,
            ProblemsTable.TABLE,
            ProjectMetricsTable.TABLE,
            RelationsTable.TABLE);
        task.finish();
        
        task.start("Cleaning projects table");
        try (DeleteStatement del = exec.createDeleteStatement(ProjectsTable.TABLE)) {
          del.andWhere(ProjectsTable.PROJECT_TYPE.compareIn(EnumSet.of(Project.JAVA_LIBRARY, Project.CRAWLED)));
          del.execute();
        }
        try (SetStatement set = exec.createSetStatement(ProjectsTable.TABLE)) {
          set.addAssignment(ProjectsTable.PATH, ProjectsTable.ProjectState.COMPONENT.name());
          set.andWhere(ProjectsTable.PROJECT_TYPE.compareIn(EnumSet.of(Project.JAR, Project.MAVEN)));
          set.execute();
        }
        task.finish();
        
        task.start("Adding the primitive types");
        try (SelectQuery query = exec.createSelectQuery(ProjectsTable.TABLE)) {
          query.addSelect(ProjectsTable.PROJECT_ID);
          query.andWhere(ProjectsTable.NAME.compareEquals(ProjectsTable.PRIMITIVES_PROJECT).and(ProjectsTable.PROJECT_TYPE.compareEquals(Project.SYSTEM)));
          Integer projectID = query.select().toSingleton(ProjectsTable.PROJECT_ID, false); 
          exec.insert(EntitiesTable.makeInsert(Entity.PRIMITIVE, "boolean",  projectID));
          exec.insert(EntitiesTable.makeInsert(Entity.PRIMITIVE, "char", projectID));
          exec.insert(EntitiesTable.makeInsert(Entity.PRIMITIVE, "byte", projectID));
          exec.insert(EntitiesTable.makeInsert(Entity.PRIMITIVE, "short", projectID));
          exec.insert(EntitiesTable.makeInsert(Entity.PRIMITIVE, "int", projectID));
          exec.insert(EntitiesTable.makeInsert(Entity.PRIMITIVE, "long", projectID));
          exec.insert(EntitiesTable.makeInsert(Entity.PRIMITIVE, "float", projectID));
          exec.insert(EntitiesTable.makeInsert(Entity.PRIMITIVE, "double", projectID));
          exec.insert(EntitiesTable.makeInsert(Entity.PRIMITIVE, "void", projectID));
        }
        task.finish();
        
        task.finish();
      }
    }.run();
  }
}
