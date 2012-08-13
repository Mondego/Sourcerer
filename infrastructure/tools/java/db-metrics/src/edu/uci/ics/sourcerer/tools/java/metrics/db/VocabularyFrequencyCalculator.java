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

import edu.uci.ics.sourcerer.tools.java.db.schema.EntityMetricsTable;
import edu.uci.ics.sourcerer.tools.java.db.schema.ProjectMetricsTable;
import edu.uci.ics.sourcerer.tools.java.db.type.ModeledEntity;
import edu.uci.ics.sourcerer.tools.java.db.type.ModeledStructuralEntity;
import edu.uci.ics.sourcerer.tools.java.db.type.TypeModel;
import edu.uci.ics.sourcerer.tools.java.metrics.db.MetricModelFactory.ProjectMetricModel;
import edu.uci.ics.sourcerer.tools.java.model.types.Metric;
import edu.uci.ics.sourcerer.util.Averager;
import edu.uci.ics.sourcerer.util.io.logging.TaskProgressLogger;
import edu.uci.ics.sourcerer.utils.db.QueryExecutor;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class VocabularyFrequencyCalculator extends Calculator {

  @Override
  public boolean shouldCalculate(ProjectMetricModel metrics) {
    return metrics.missingValue(Metric.BC_VOCABULARY_FREQUENCY);
  }

  @Override
  public void calculate(QueryExecutor exec, Integer projectID, ProjectMetricModel metrics, TypeModel model) {
    TaskProgressLogger task = TaskProgressLogger.get();
    
    task.start("Computing VocabularyFrequency");
    Averager<Double> avgVf = Averager.create();
    
    task.start("Processing entities", "entities processed");
    for (ModeledEntity entity : model.getEntities()) {
      if (projectID.equals(entity.getProjectID())) {
        Double value = metrics.getEntityValue(entity.getEntityID(), Metric.BC_VOCABULARY_FREQUENCY);
        if (value != null) {
          avgVf.addValue(value);
        } else {
          Double noi = metrics.getEntityValue(entity.getEntityID(), Metric.BC_NUMBER_OF_INSTRUCTIONS);
          Double vs = metrics.getEntityValue(entity.getEntityID(), Metric.BC_VOCABULARY_SIZE);
          if (noi != null && vs != null && noi > 0 && vs > 0) {
            value = noi / vs;
            Integer fileID = ((ModeledStructuralEntity) entity).getFileID();
            metrics.setEntityValue(entity.getEntityID(), fileID, Metric.BC_VOCABULARY_FREQUENCY, value);
            exec.insert(EntityMetricsTable.createInsert(projectID, fileID, entity.getEntityID(), Metric.BC_VOCABULARY_FREQUENCY, value));
            avgVf.addValue(value);
          }
        }
        task.progress();
      }
    }
    task.finish();
    
    if (metrics.missingValue(Metric.BC_VOCABULARY_FREQUENCY)) {
      metrics.setValue(Metric.BC_VOCABULARY_FREQUENCY, avgVf);
      exec.insert(ProjectMetricsTable.createInsert(projectID, Metric.BC_VOCABULARY_FREQUENCY, avgVf));
    }
    task.finish();
  }
}
