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

import edu.uci.ics.sourcerer.tools.java.db.schema.CommentsTable;
import edu.uci.ics.sourcerer.tools.java.db.schema.ComponentRelationsTable;
import edu.uci.ics.sourcerer.tools.java.db.schema.ComponentsTable;
import edu.uci.ics.sourcerer.tools.java.db.schema.EntitiesTable;
import edu.uci.ics.sourcerer.tools.java.db.schema.EntityMetricsTable;
import edu.uci.ics.sourcerer.tools.java.db.schema.FileMetricsTable;
import edu.uci.ics.sourcerer.tools.java.db.schema.FilesTable;
import edu.uci.ics.sourcerer.tools.java.db.schema.ImportsTable;
import edu.uci.ics.sourcerer.tools.java.db.schema.ProblemsTable;
import edu.uci.ics.sourcerer.tools.java.db.schema.ProjectMetricsTable;
import edu.uci.ics.sourcerer.tools.java.db.schema.ProjectsTable;
import edu.uci.ics.sourcerer.tools.java.db.schema.RelationsTable;
import edu.uci.ics.sourcerer.tools.java.db.schema.TypeVersionsTable;
import edu.uci.ics.sourcerer.tools.java.db.schema.TypesTable;
import edu.uci.ics.sourcerer.tools.java.model.types.Entity;
import edu.uci.ics.sourcerer.util.io.logging.TaskProgressLogger;
import edu.uci.ics.sourcerer.utils.db.DatabaseRunnable;

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
            ComponentRelationsTable.TABLE,
            ComponentsTable.TABLE,
            EntitiesTable.TABLE,
            EntityMetricsTable.TABLE,
            FileMetricsTable.TABLE,
            FilesTable.TABLE,
            ImportsTable.TABLE,
            ProblemsTable.TABLE,
            ProjectMetricsTable.TABLE,
            ProjectsTable.TABLE,
            RelationsTable.TABLE,
            TypesTable.TABLE,
            TypeVersionsTable.TABLE);
        task.finish();
        
        task.start("Creating new tables");
        exec.createTables(
            CommentsTable.TABLE,
            ComponentRelationsTable.TABLE,
            ComponentsTable.TABLE,
            EntitiesTable.TABLE,
            EntityMetricsTable.TABLE,
            FileMetricsTable.TABLE,
            FilesTable.TABLE,
            ImportsTable.TABLE,
            ProblemsTable.TABLE,
            ProjectMetricsTable.TABLE,
            ProjectsTable.TABLE,
            RelationsTable.TABLE,
            TypesTable.TABLE,
            TypeVersionsTable.TABLE);
        task.finish();
        
        task.start("Adding the primitive types");
        Integer projectID = exec.insertWithKey(ProjectsTable.TABLE.makePrimitivesInsert());
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
        exec.insert(ProjectsTable.TABLE.makeUnknownsInsert());
        task.finish();
        
        task.finish();
      }
    }.run();
  }
}
