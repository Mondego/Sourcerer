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

import edu.uci.ics.sourcerer.tools.java.db.schema.EntityMetricsTable;
import edu.uci.ics.sourcerer.tools.java.db.schema.ProjectMetricsTable;
import edu.uci.ics.sourcerer.tools.java.db.type.ModeledDeclaredType;
import edu.uci.ics.sourcerer.tools.java.db.type.ModeledEntity;
import edu.uci.ics.sourcerer.tools.java.db.type.ModeledStructuralEntity;
import edu.uci.ics.sourcerer.tools.java.db.type.TypeModel;
import edu.uci.ics.sourcerer.tools.java.metrics.db.MetricModelFactory.ProjectMetricModel;
import edu.uci.ics.sourcerer.tools.java.model.types.Entity;
import edu.uci.ics.sourcerer.tools.java.model.types.Metric;
import edu.uci.ics.sourcerer.util.Averager;
import edu.uci.ics.sourcerer.util.io.logging.TaskProgressLogger;
import edu.uci.ics.sourcerer.utils.db.QueryExecutor;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class WeightedMethodsPerClassCalculator extends Calculator {

  @Override
  public boolean shouldCalculate(ProjectMetricModel metrics) {
    return metrics.missingValue(Metric.BC_WEIGHTED_METHODS_PER_CLASS, Metric.BC_CYCLOMATIC_COMPLEXITY);
  }

  @Override
  public void calculate(QueryExecutor exec, Integer projectID, ProjectMetricModel metrics, TypeModel model) {
    TaskProgressLogger task = TaskProgressLogger.get();
    
    Pattern anon = Pattern.compile(".*\\$\\d+$");
    
    task.start("Computing WeightedMethodsPerClass");
    Averager<Double> avgWmc = Averager.create();
    Averager<Double> avgCyc = Averager.create();
    
    task.start("Processing entities", "entities processed");
    for (ModeledEntity entity : model.getEntities()) {
      if (projectID.equals(entity.getProjectID()) && entity.getType().is(Entity.CLASS, Entity.ENUM) && !anon.matcher(entity.getFqn()).matches()) {
        ModeledDeclaredType dec = (ModeledDeclaredType) entity;
        Double value = metrics.getEntityValue(entity.getEntityID(), Metric.BC_WEIGHTED_METHODS_PER_CLASS);
        if (value != null) {
          avgWmc.addValue(value);
          for (ModeledStructuralEntity child : dec.getChildren()) {
            if (child.getType() == Entity.METHOD) {
              value = metrics.getEntityValue(child.getEntityID(), Metric.BC_CYCLOMATIC_COMPLEXITY);
              if (value != null) {
                avgCyc.addValue(value);
              }
            }
          }
        } else {
          double wmc = 0;
          
          for (ModeledStructuralEntity child : dec.getChildren()) {
            if (child.getType() == Entity.METHOD) {
              value = metrics.getEntityValue(child.getEntityID(), Metric.BC_CYCLOMATIC_COMPLEXITY);
              if (value != null) {
                wmc += value.doubleValue();
                avgCyc.addValue(value);
              }
            }
          }
          if (wmc > 0) {
            value = wmc;
            metrics.setEntityValue(dec.getEntityID(), dec.getFileID(), Metric.BC_WEIGHTED_METHODS_PER_CLASS, value);
            exec.insert(EntityMetricsTable.createInsert(projectID, dec.getFileID(), dec.getEntityID(), Metric.BC_WEIGHTED_METHODS_PER_CLASS, value));
            avgWmc.addValue(value);
          }
        }
        task.progress();
      }
    }
    task.finish();
    
    if (metrics.missingValue(Metric.BC_WEIGHTED_METHODS_PER_CLASS)) {
      metrics.setValue(Metric.BC_WEIGHTED_METHODS_PER_CLASS, avgWmc);
      exec.insert(ProjectMetricsTable.createInsert(projectID, Metric.BC_WEIGHTED_METHODS_PER_CLASS, avgWmc));
    }
    if (metrics.missingValue(Metric.BC_CYCLOMATIC_COMPLEXITY)) {
      metrics.setValue(Metric.BC_CYCLOMATIC_COMPLEXITY, avgCyc);
      exec.insert(ProjectMetricsTable.createInsert(projectID, Metric.BC_CYCLOMATIC_COMPLEXITY, avgCyc));
    }
    task.finish();
  }

}
