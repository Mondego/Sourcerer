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

import java.util.regex.Pattern;

import edu.uci.ics.sourcerer.tools.java.db.schema.ProjectMetricsTable;
import edu.uci.ics.sourcerer.tools.java.db.type.ModeledDeclaredType;
import edu.uci.ics.sourcerer.tools.java.db.type.ModeledEntity;
import edu.uci.ics.sourcerer.tools.java.db.type.TypeModel;
import edu.uci.ics.sourcerer.tools.java.metrics.db.MetricModelFactory.ProjectMetricModel;
import edu.uci.ics.sourcerer.tools.java.model.types.Entity;
import edu.uci.ics.sourcerer.tools.java.model.types.Metric;
import edu.uci.ics.sourcerer.util.io.logging.TaskProgressLogger;
import edu.uci.ics.sourcerer.utils.db.QueryExecutor;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class NumberOfBaseClassesCalculator extends Calculator {
  @Override
  public boolean shouldCalculate(ProjectMetricModel metrics) {
    return metrics.missingValue(Metric.NUMBER_OF_BASE_CLASSES, Metric.NUMBER_OF_DERIVED_CLASSES, Metric.RATIO_OF_DERIVED_TO_BASE_CLASSES);
  }

  @Override
  public void calculate(QueryExecutor exec, Integer projectID, ProjectMetricModel metrics, TypeModel model) {
    TaskProgressLogger task = TaskProgressLogger.get();
    
    Pattern anon = Pattern.compile(".*\\$\\d+$");
    
    task.start("Computing NumberOfBaseClasses");
    double baseClassCount = 0;
    double derivedClassCount = 0;
    task.start("Processing classes", "classes processed", 0);
    for (ModeledEntity entity : model.getEntities()) {
      if (projectID.equals(entity.getProjectID()) && entity.getType() == Entity.CLASS && !anon.matcher(entity.getFqn()).matches()) {
        ModeledDeclaredType dec = (ModeledDeclaredType) entity;
        // Check if it's supertype is Object
        ModeledEntity sup = dec.getSuperclass();
        if (sup == null || sup.getFqn().equals("java.lang.Object")) {
          baseClassCount++;
        } else {
          derivedClassCount++;
        }
        task.progress();
      }
    }
    task.finish();
    if (metrics.missingValue(Metric.NUMBER_OF_BASE_CLASSES)) {
      metrics.setValue(Metric.NUMBER_OF_BASE_CLASSES, baseClassCount, null, null, null, null);
      exec.insert(ProjectMetricsTable.createInsert(projectID, Metric.NUMBER_OF_BASE_CLASSES, baseClassCount, null, null, null, null));
    }
    if (metrics.missingValue(Metric.NUMBER_OF_DERIVED_CLASSES)) {
      metrics.setValue(Metric.NUMBER_OF_DERIVED_CLASSES, derivedClassCount, null, null, null, null);
      exec.insert(ProjectMetricsTable.createInsert(projectID, Metric.NUMBER_OF_DERIVED_CLASSES, derivedClassCount, null, null, null, null));
    }
    if (metrics.missingValue(Metric.RATIO_OF_DERIVED_TO_BASE_CLASSES)) {
      Double value = derivedClassCount / baseClassCount;
      metrics.setValue(Metric.RATIO_OF_DERIVED_TO_BASE_CLASSES, value, null, null, null, null);
      exec.insert(ProjectMetricsTable.createInsert(projectID, Metric.RATIO_OF_DERIVED_TO_BASE_CLASSES, value, null, null, null, null));
    }
    task.finish();
  }
}
