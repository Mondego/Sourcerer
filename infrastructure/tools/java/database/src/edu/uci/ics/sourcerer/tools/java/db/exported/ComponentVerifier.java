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
package edu.uci.ics.sourcerer.tools.java.db.exported;

import static edu.uci.ics.sourcerer.util.io.logging.Logging.logger;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

import edu.uci.ics.sourcerer.tools.java.db.schema.ComponentRelationsTable;
import edu.uci.ics.sourcerer.tools.java.db.schema.ProjectsTable;
import edu.uci.ics.sourcerer.tools.java.model.types.ComponentRelation;
import edu.uci.ics.sourcerer.util.Averager;
import edu.uci.ics.sourcerer.util.CollectionUtils;
import edu.uci.ics.sourcerer.util.io.IOUtils;
import edu.uci.ics.sourcerer.util.io.LogFileWriter;
import edu.uci.ics.sourcerer.util.io.arguments.Argument;
import edu.uci.ics.sourcerer.util.io.arguments.Arguments;
import edu.uci.ics.sourcerer.util.io.arguments.RelativeFileArgument;
import edu.uci.ics.sourcerer.util.io.logging.TaskProgressLogger;
import edu.uci.ics.sourcerer.utils.db.DatabaseRunnable;
import edu.uci.ics.sourcerer.utils.db.sql.ConstantCondition;
import edu.uci.ics.sourcerer.utils.db.sql.QualifiedColumn;
import edu.uci.ics.sourcerer.utils.db.sql.QualifiedTable;
import edu.uci.ics.sourcerer.utils.db.sql.SelectQuery;
import edu.uci.ics.sourcerer.utils.db.sql.TypedQueryResult;


/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class ComponentVerifier {
  public static Argument<File> JACCARD_TABLE = new RelativeFileArgument("jaccard-table", "jaccard-table.txt", Arguments.OUTPUT, "Table of jaccard values.");
  public static Argument<File> JACCARD_LOG = new RelativeFileArgument("jaccard-log", "jaccard-log.txt", Arguments.OUTPUT, "Log of jaccard values.");
  
  public static void computeJaccard() {
    new DatabaseRunnable() {
      SelectQuery groupQuery = null;
      ConstantCondition<String> groupEquals = null;
      {
        groupQuery = exec.createSelectQuery(ProjectsTable.TABLE);
        groupQuery.addSelect(ProjectsTable.PROJECT_ID);
        groupEquals = ProjectsTable.GROUP.compareEquals();
        groupQuery.andWhere(groupEquals);
      }
      
      Set<Integer> ids = new HashSet<>();
      Set<String> groups = new HashSet<>();
      
      private double compute() {
        Set<Integer> other = new HashSet<>();
        for (String group : groups) {
          groupEquals.setValue(group);
          other.addAll(groupQuery.select().toCollection(ProjectsTable.PROJECT_ID));
        }
        // Jaccard is size of intersection over size of union
        return CollectionUtils.compuateJaccard(ids, other);
      }
      
      @Override
      protected void action() {
        TaskProgressLogger task = TaskProgressLogger.get();
        Averager<Double> jaccards = Averager.create();
        
        task.start("Processing libraries", "libraries processed", 500);
        // Get all the jars for each library
        QualifiedTable l2lv = ComponentRelationsTable.TABLE.qualify("a");
        QualifiedTable j2lv = ComponentRelationsTable.TABLE.qualify("b");
        try (SelectQuery query = exec.createSelectQuery(ComponentRelationsTable.TARGET_ID.qualify(l2lv).compareEquals(ComponentRelationsTable.TARGET_ID.qualify(j2lv)), ComponentRelationsTable.SOURCE_ID.qualify(j2lv).compareEquals(ProjectsTable.PROJECT_ID));
             LogFileWriter writer = IOUtils.createLogFileWriter(JACCARD_LOG)) {
          QualifiedColumn<Integer> libraryIDcol = ComponentRelationsTable.SOURCE_ID.qualify(l2lv);
          query.addSelects(libraryIDcol, ProjectsTable.PROJECT_ID, ProjectsTable.GROUP);
          query.andWhere(ComponentRelationsTable.TYPE.qualify(l2lv).compareEquals(ComponentRelation.LIBRARY_CONTAINS_LIBRARY_VERSION), ComponentRelationsTable.TYPE.qualify(j2lv).compareEquals(ComponentRelation.JAR_MATCHES_LIBRARY_VERSION));
          query.orderBy(libraryIDcol, true);
          
          TypedQueryResult result = query.select();
          Integer lastLibraryID = null;
          
          while (result.next()) {
            Integer libraryID = result.getResult(libraryIDcol);
            Integer id = result.getResult(ProjectsTable.PROJECT_ID);
            String group = result.getResult(ProjectsTable.GROUP);
            if (!libraryID.equals(lastLibraryID)) {
              // ID changed, can do the computation
              double jaccard = compute();
              writer.write(lastLibraryID + " " + jaccard);
              jaccards.addValue(jaccard);
              ids.clear();
              groups.clear();
              lastLibraryID = libraryID;
            }
            ids.add(id);
            groups.add(group);
            task.progress();
          }
          // Do the computation
          double jaccard = compute();
          writer.write(lastLibraryID + " " + jaccard);
          jaccards.addValue(jaccard);
        } catch (IOException e) {
          logger.log(Level.SEVERE, "Exception writing log file", e);
        }
        task.finish();
        
        task.report("AVG: " + jaccards.getMean() + " +-" + jaccards.getStandardDeviation());
        task.report("MIN: " + jaccards.getMin());
        task.report("MAX: " + jaccards.getMax());
        jaccards.writeValueMap(JACCARD_TABLE.getValue());
      }
    }.run();
  }
}
