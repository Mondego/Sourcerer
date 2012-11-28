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

import java.util.Collections;
import java.util.EnumSet;
import java.util.logging.Level;

import edu.uci.ics.sourcerer.tools.java.db.schema.FilesTable;
import edu.uci.ics.sourcerer.tools.java.db.schema.ProjectsTable;
import edu.uci.ics.sourcerer.tools.java.db.type.TypeModel;
import edu.uci.ics.sourcerer.tools.java.db.type.TypeModelFactory;
import edu.uci.ics.sourcerer.tools.java.metrics.db.MetricModelFactory.ProjectMetricModel;
import edu.uci.ics.sourcerer.tools.java.model.types.File;
import edu.uci.ics.sourcerer.tools.java.model.types.Project;
import edu.uci.ics.sourcerer.util.io.logging.TaskProgressLogger;
import edu.uci.ics.sourcerer.utils.db.DatabaseRunnable;
import edu.uci.ics.sourcerer.utils.db.sql.ConstantCondition;
import edu.uci.ics.sourcerer.utils.db.sql.SelectQuery;
import edu.uci.ics.sourcerer.utils.db.sql.TypedQueryResult;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
class MetricsCalculator extends DatabaseRunnable {
  private final TaskProgressLogger task;
  private final TypeModel javaLibraryModel;
  private final Calculator[] calculators = {
//      new BytecodeSizeStatistics(),
//      new NumberOfBaseClassesCalculator(),
//      new NumberOfBaseInterfacesCalculator(),
//      new NumberOfClassChildrenCalculator(),
//      new NumberOfInterfaceRelativesCalculator(),
//      new InheritanceHierarchyDepthCalculator(),
//      new AverageSizeOfStatementsCalculator(),
//      new VocabularyFrequencyCalculator(),
//      new WeightedMethodsPerClassCalculator(),
      new InternalCouplingCalculator(),
//      new LackOfCohesionCalculator(),
//      new ResponseForClassCalculator(),
  };
  
  private MetricsCalculator() {
    task = TaskProgressLogger.get();
    javaLibraryModel = TypeModelFactory.createJavaLibraryTypeModel();
  }
  
  @Override
  protected void action() {
    // Get the list of projects
    try (SelectQuery selectProjects = exec.createSelectQuery(ProjectsTable.TABLE);
         SelectQuery selectJars = exec.createSelectQuery(FilesTable.HASH.compareEquals(ProjectsTable.HASH));
         MetricModelFactory mFact = new MetricModelFactory(exec);) {
      selectProjects.addSelect(ProjectsTable.PROJECT_ID, ProjectsTable.PROJECT_TYPE);
      selectProjects.andWhere(ProjectsTable.PROJECT_TYPE.compareIn(EnumSet.of(Project.JAVA_LIBRARY, Project.JAR, Project.MAVEN, Project.CRAWLED)));
      
      selectJars.addSelect(ProjectsTable.PROJECT_ID);
      ConstantCondition<Integer> equalsProjectID = FilesTable.PROJECT_ID.compareEquals();
      selectJars.andWhere(equalsProjectID, FilesTable.FILE_TYPE.compareEquals(File.JAR));
      
      task.start("Processing projects", "projects processed", 1);
      TypedQueryResult result = selectProjects.select();
      while (result.next()) {
        Integer projectID = result.getResult(ProjectsTable.PROJECT_ID);
        Project type = result.getResult(ProjectsTable.PROJECT_TYPE);

        task.progress("Processing project %d (" + projectID + ") in %s");
        TypeModel model = null;
        ProjectMetricModel metricModel = mFact.createModel(projectID);
        for (Calculator calc : calculators) {
          if (calc.shouldCalculate(metricModel)) {
            if (model == null) {
              switch (type) {
                case JAVA_LIBRARY:
                  model = javaLibraryModel;
                  break;
                case MAVEN:
                  model = TypeModelFactory.createJarTypeModel(Collections.singleton(projectID), javaLibraryModel);
                  break;
                case JAR:
                  model = TypeModelFactory.createJarTypeModel(Collections.singleton(projectID), javaLibraryModel);
                  break;
                case CRAWLED:
                  // Get the jars
                  equalsProjectID.setValue(projectID);
                  model = TypeModelFactory.createProjectTypeModel(projectID, TypeModelFactory.createJarTypeModel(selectJars.select().toCollection(ProjectsTable.PROJECT_ID), javaLibraryModel));
                  break;
                default:
                  task.report(Level.SEVERE, "Unexpected project type: " + type + " for " + projectID);
              } 
            }
            calc.calculate(exec, projectID, metricModel, model);
          }
        }
      }
      task.finish();
    }
  }
  
  public static void calculateMetrics() {
    new MetricsCalculator().run();
  }
}
