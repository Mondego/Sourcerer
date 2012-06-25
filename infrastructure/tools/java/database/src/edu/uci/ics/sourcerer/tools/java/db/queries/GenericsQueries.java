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
package edu.uci.ics.sourcerer.tools.java.db.queries;

import java.io.File;
import java.io.IOException;

import edu.uci.ics.sourcerer.tools.java.db.schema.EntitiesTable;
import edu.uci.ics.sourcerer.tools.java.db.schema.ProjectsTable;
import edu.uci.ics.sourcerer.tools.java.db.schema.RelationsTable;
import edu.uci.ics.sourcerer.tools.java.model.types.Entity;
import edu.uci.ics.sourcerer.tools.java.model.types.Project;
import edu.uci.ics.sourcerer.tools.java.model.types.Relation;
import edu.uci.ics.sourcerer.util.io.IOUtils;
import edu.uci.ics.sourcerer.util.io.LogFileWriter;
import edu.uci.ics.sourcerer.util.io.arguments.Argument;
import edu.uci.ics.sourcerer.util.io.arguments.Arguments;
import edu.uci.ics.sourcerer.util.io.arguments.Command;
import edu.uci.ics.sourcerer.util.io.arguments.RelativeFileArgument;
import edu.uci.ics.sourcerer.util.io.logging.TaskProgressLogger;
import edu.uci.ics.sourcerer.utils.db.DatabaseConnectionFactory;
import edu.uci.ics.sourcerer.utils.db.DatabaseRunnable;
import edu.uci.ics.sourcerer.utils.db.sql.ConstantCondition;
import edu.uci.ics.sourcerer.utils.db.sql.SelectQuery;
import edu.uci.ics.sourcerer.utils.db.sql.TypedQueryResult;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class GenericsQueries {
  public static Argument<File> NON_TRIVIAL_EXTENSIONS = new RelativeFileArgument("non-trivial-extensions", "non-trivial-extensions.txt", Arguments.OUTPUT, "");
  
  public static void countNonTrivialExtensions() {
    new DatabaseRunnable() {
      @Override
      protected void action() {
        TaskProgressLogger task = TaskProgressLogger.get();
        task.start("Counting the number of classes extending something other than java.lang.Object");
        Integer objectEntityID = null;
        try (SelectQuery select = exec.createSelectQuery(ProjectsTable.PROJECT_ID.compareEquals(EntitiesTable.PROJECT_ID))) {
          select.addSelect(EntitiesTable.ENTITY_ID);
          select.andWhere(EntitiesTable.FQN.compareEquals("java.lang.Object"), ProjectsTable.PROJECT_TYPE.compareEquals(Project.JAVA_LIBRARY));
          task.start("Getting the entityID for java.lang.Object");
          objectEntityID = select.select().toSingleton(EntitiesTable.ENTITY_ID, false);
          task.finish();
        }
        
        try (SelectQuery getProjects = exec.createSelectQuery(ProjectsTable.TABLE);
             SelectQuery getClasses = exec.createSelectQuery(EntitiesTable.TABLE);
             SelectQuery getExtends = exec.createSelectQuery(RelationsTable.TABLE);
             LogFileWriter log = IOUtils.createLogFileWriter(NON_TRIVIAL_EXTENSIONS)) {
          getProjects.addSelect(ProjectsTable.PROJECT_ID, ProjectsTable.NAME);
          getProjects.andWhere(ProjectsTable.SOURCE.compareEquals("Apache"));
          
          getClasses.addSelect(EntitiesTable.ENTITY_ID);
          ConstantCondition<Integer> entPID= EntitiesTable.PROJECT_ID.compareEquals();
          getClasses.andWhere(EntitiesTable.ENTITY_TYPE.compareEquals(Entity.CLASS), entPID);
          
          getExtends.setCount(true);
          ConstantCondition<Integer> relEID = RelationsTable.LHS_EID.compareEquals();
          getExtends.andWhere(RelationsTable.RELATION_TYPE.compareEquals(Relation.EXTENDS), relEID, RelationsTable.RHS_EID.compareNotEquals(objectEntityID));
          
          task.start("Getting the Apache projects");
          TypedQueryResult result = getProjects.select();
          task.finish();
          task.start("Processing Apache projects", "projects processed", 1);
          while (result.next()) {
            Integer projectID = result.getResult(ProjectsTable.PROJECT_ID);
            String name = result.getResult(ProjectsTable.NAME);
            entPID.setValue(projectID);
            int totalClasses = 0;
            int nonTrivialExtensions = 0;
            for (Integer classID : getClasses.select().toCollection(EntitiesTable.ENTITY_ID)) {
              totalClasses++;
              relEID.setValue(classID);
              if (getExtends.select().toCount() > 0) {
                nonTrivialExtensions++;
              }
            }
            log.write(projectID + "\t" + name + "\t" + nonTrivialExtensions + "\t" + totalClasses);
            task.progress();
          }
          task.finish();
        } catch (IOException e) {
          task.exception(e);
        }
        
        task.finish();
      }
    }.run();
  }
  
  public static final Command NON_TRIVIAL = new Command("non-trivial", "") {
    @Override
    protected void action() {
      countNonTrivialExtensions();
    }
  }.setProperties(NON_TRIVIAL_EXTENSIONS, DatabaseConnectionFactory.DATABASE_URL, DatabaseConnectionFactory.DATABASE_USER, DatabaseConnectionFactory.DATABASE_PASSWORD);
  
  public static void main(String[] args) {
    Command.execute(args, GenericsQueries.class);
  }
}
