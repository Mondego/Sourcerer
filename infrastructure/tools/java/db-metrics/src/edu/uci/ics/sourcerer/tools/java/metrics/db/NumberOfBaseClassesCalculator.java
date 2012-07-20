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

import edu.uci.ics.sourcerer.tools.java.db.schema.ProjectMetricsTable;
import edu.uci.ics.sourcerer.tools.java.db.type.ModeledDeclaredType;
import edu.uci.ics.sourcerer.tools.java.db.type.ModeledEntity;
import edu.uci.ics.sourcerer.tools.java.db.type.TypeModel;
import edu.uci.ics.sourcerer.tools.java.metrics.db.MetricModelFactory.ProjectMetricModel;
import edu.uci.ics.sourcerer.tools.java.model.types.Entity;
import edu.uci.ics.sourcerer.tools.java.model.types.Metric;
import edu.uci.ics.sourcerer.utils.db.QueryExecutor;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class NumberOfBaseClassesCalculator extends Calculator {
  @Override
  public boolean shouldCalculate(ProjectMetricModel metrics) {
    return metrics.getValue(Metric.NUMBER_OF_BASE_CLASSES) == null ||
        metrics.getValue(Metric.NUMBER_OF_DERIVED_CLASSES) == null ||
        metrics.getValue(Metric.RATIO_DERIVED_TO_BASE) == null;
  }

  @Override
  public void calculate(QueryExecutor exec, Integer projectID, ProjectMetricModel metrics, TypeModel model) {
    double baseClassCount = 0;
    double derivedClassCount = 0;
    for (ModeledEntity entity : model.getEntities()) {
      if (entity.getType() == Entity.CLASS) {
        ModeledDeclaredType dec = (ModeledDeclaredType) entity;
        // Check if it's supertype is Object
        ModeledEntity sup = dec.getSuperclass();
        if (sup == null || sup.getFqn().equals("java.lang.Object")) {
          baseClassCount++;
        } else {
          derivedClassCount++;
        }
      }
    }
    if (metrics.getValue(Metric.NUMBER_OF_BASE_CLASSES) == null) {
      metrics.setValue(Metric.NUMBER_OF_BASE_CLASSES, baseClassCount);
      exec.insert(ProjectMetricsTable.createInsert(projectID, Metric.NUMBER_OF_BASE_CLASSES, baseClassCount));
      
    }
    if (metrics.getValue(Metric.NUMBER_OF_DERIVED_CLASSES) == null) {
      metrics.setValue(Metric.NUMBER_OF_DERIVED_CLASSES, derivedClassCount);
      exec.insert(ProjectMetricsTable.createInsert(projectID, Metric.NUMBER_OF_DERIVED_CLASSES, derivedClassCount));
    }
    if (metrics.getValue(Metric.RATIO_DERIVED_TO_BASE) == null) {
      metrics.setValue(Metric.RATIO_DERIVED_TO_BASE, derivedClassCount / baseClassCount);
      exec.insert(ProjectMetricsTable.createInsert(projectID, Metric.NUMBER_OF_DERIVED_CLASSES, derivedClassCount / baseClassCount));
    }
  }
}
