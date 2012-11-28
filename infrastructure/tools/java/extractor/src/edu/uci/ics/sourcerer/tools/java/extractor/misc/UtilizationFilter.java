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
package edu.uci.ics.sourcerer.tools.java.extractor.misc;

import java.io.BufferedWriter;
import java.io.IOException;

import edu.uci.ics.sourcerer.tools.java.db.schema.ComponentMetricsTable;
import edu.uci.ics.sourcerer.tools.java.db.schema.ComponentRelationsTable;
import edu.uci.ics.sourcerer.tools.java.db.schema.ProjectsTable;
import edu.uci.ics.sourcerer.tools.java.extractor.Extractor;
import edu.uci.ics.sourcerer.tools.java.model.types.ComponentMetric;
import edu.uci.ics.sourcerer.tools.java.model.types.ComponentRelation;
import edu.uci.ics.sourcerer.util.io.IOUtils;
import edu.uci.ics.sourcerer.util.io.logging.TaskProgressLogger;
import edu.uci.ics.sourcerer.utils.db.DatabaseRunnable;
import edu.uci.ics.sourcerer.utils.db.sql.SelectQuery;
import edu.uci.ics.sourcerer.utils.db.sql.TypedQueryResult;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class UtilizationFilter extends DatabaseRunnable {
  private UtilizationFilter() {}
  
  public static void createUtilizationJarFilter() {
    new UtilizationFilter().run();
  }

  @Override
  protected void action() {
    TaskProgressLogger task = TaskProgressLogger.get();
    
    task.start("Creating utilization-based jar filter");
    try (SelectQuery select = exec.createSelectQuery(ComponentMetricsTable.COMPONENT_ID.compareEquals(ComponentRelationsTable.SOURCE_ID), ComponentRelationsTable.TARGET_ID.compareEquals(ProjectsTable.PROJECT_ID));
         BufferedWriter bw = IOUtils.makeBufferedWriter(Extractor.JAR_FILTER)) {
      select.addSelect(ProjectsTable.HASH);
      select.andWhere(ComponentMetricsTable.METRIC_TYPE.compareEquals(ComponentMetric.PROJECTS_USING_FQN), ComponentRelationsTable.TYPE.compareEquals(ComponentRelation.LIBRARY_CONTAINS_JAR));
      
      task.start("Performing db query");
      TypedQueryResult result = select.select();
      task.finish();
      
      task.start("Writing jar filter", "jars written", 500);
      while (result.next()) {
        String hash = result.getResult(ProjectsTable.HASH);
        bw.write(hash);
        bw.newLine();
        task.progress();
      }
      task.finish();
      task.finish();
    } catch (IOException e) {
      task.exception(e);
    }
  }
}
