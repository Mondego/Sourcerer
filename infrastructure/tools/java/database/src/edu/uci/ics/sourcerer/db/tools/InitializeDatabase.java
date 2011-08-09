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
package edu.uci.ics.sourcerer.db.tools;

import static edu.uci.ics.sourcerer.util.io.Logging.logger;

import edu.uci.ics.sourcerer.model.Entity;
import edu.uci.ics.sourcerer.util.db.DatabaseConnection;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class InitializeDatabase extends ParallelDatabaseImporterThread {
  protected InitializeDatabase(DatabaseConnection connection) {
    super(connection);
  }

  @Override
  public void run() {
    logger.info("  Dropping old tables...");
    executor.dropTables(
        projectsTable,
        projectMetricsTable,
        filesTable,
        fileMetricsTable,
        importsTable,
        problemsTable,
        commentsTable,
        entitiesTable,
        entityMetricsTable,
        relationsTable);
    
    logger.info("  Adding new tables...");
    projectsTable.createTable();
    projectMetricsTable.createTable();
    filesTable.createTable();
    fileMetricsTable.createTable();
    importsTable.createTable();
    problemsTable.createTable();
    commentsTable.createTable();
    entitiesTable.createTable();
    entityMetricsTable.createTable();
    relationsTable.createTable();
    
    logger.info("  Adding the primitive types...");
    locker.addWrites(projectsTable, entitiesTable);
    locker.lock();
    
    Integer projectID = projectsTable.insertPrimitivesProject();
    entitiesTable.forceInsert(Entity.PRIMITIVE, "boolean", projectID);
    entitiesTable.forceInsert(Entity.PRIMITIVE, "char", projectID);
    entitiesTable.forceInsert(Entity.PRIMITIVE, "byte", projectID);
    entitiesTable.forceInsert(Entity.PRIMITIVE, "short", projectID);
    entitiesTable.forceInsert(Entity.PRIMITIVE, "int", projectID);
    entitiesTable.forceInsert(Entity.PRIMITIVE, "long", projectID);
    entitiesTable.forceInsert(Entity.PRIMITIVE, "float", projectID);
    entitiesTable.forceInsert(Entity.PRIMITIVE, "double", projectID);
    entitiesTable.forceInsert(Entity.PRIMITIVE, "void", projectID);
    
    logger.info("  Adding the unknowns project...");
    projectsTable.insertUnknownsProject();
    
    locker.unlock();
    logger.info("  Initialization complete.");
    close();
    closeConnection();
  }
}
