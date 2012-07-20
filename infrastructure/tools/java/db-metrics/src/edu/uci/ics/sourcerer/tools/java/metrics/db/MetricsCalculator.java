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
package edu.uci.ics.sourcerer.tools.java.metrics.db;

import java.util.EnumSet;
import java.util.logging.Level;

import edu.uci.ics.sourcerer.tools.java.db.schema.ProjectsTable;
import edu.uci.ics.sourcerer.tools.java.db.type.TypeModel;
import edu.uci.ics.sourcerer.tools.java.db.type.TypeModelFactory;
import edu.uci.ics.sourcerer.tools.java.metrics.db.MetricModelFactory.ProjectMetricModel;
import edu.uci.ics.sourcerer.tools.java.model.types.Project;
import edu.uci.ics.sourcerer.util.io.logging.TaskProgressLogger;
import edu.uci.ics.sourcerer.utils.db.DatabaseRunnable;
import edu.uci.ics.sourcerer.utils.db.sql.SelectQuery;
import edu.uci.ics.sourcerer.utils.db.sql.TypedQueryResult;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
class MetricsCalculator extends DatabaseRunnable {
  private final TaskProgressLogger task;
  private final TypeModel javaLibraryModel;
  private final Calculator[] calculators = {
      new NumberOfBaseClassesCalculator(),
      new NumberOfChildrenCalculator(),
  };
  
  private MetricsCalculator() {
    task = TaskProgressLogger.get();
    javaLibraryModel = TypeModelFactory.createJavaLibraryTypeModel();
  }
  
  @Override
  protected void action() {
    // Get the list of projects
    try (SelectQuery selectProjects = exec.createSelectQuery(ProjectsTable.TABLE);
         MetricModelFactory mFact = new MetricModelFactory(exec);) {
      selectProjects.addSelect(ProjectsTable.PROJECT_ID, ProjectsTable.PROJECT_TYPE);
      selectProjects.andWhere(ProjectsTable.PROJECT_TYPE.compareIn(EnumSet.of(Project.JAVA_LIBRARY, Project.JAR, Project.MAVEN, Project.CRAWLED)));
      
      task.start("Processing projects", "projects processed", 0);
      TypedQueryResult result = selectProjects.select();
      while (result.next()) {
        Integer projectID = result.getResult(ProjectsTable.PROJECT_ID);
        Project type = result.getResult(ProjectsTable.PROJECT_TYPE);

        TypeModel model = null;
        ProjectMetricModel metricModel = mFact.createModel(projectID);
        for (Calculator calc : calculators) {
          if (calc.shouldCalculate(metricModel)) {
            if (model == null) {
              switch (type) {
                case JAVA_LIBRARY:
                  model = TypeModelFactory.createProjectTypeModel(projectID, javaLibraryModel);
                  break;
                default:
                  task.report(Level.SEVERE, "Unexpected project type: " + type + " for " + projectID);
              } 
            }
            calc.calculate(exec, projectID, metricModel, model);
          }
        }
        task.progress();
      }
      task.finish();
    }
  }
  
  public static void calculateMetrics() {
    new MetricsCalculator().run();
  }
}
