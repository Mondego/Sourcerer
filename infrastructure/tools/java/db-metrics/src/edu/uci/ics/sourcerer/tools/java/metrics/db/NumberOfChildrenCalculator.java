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

import static edu.uci.ics.sourcerer.util.io.logging.Logging.logger;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

import edu.uci.ics.sourcerer.tools.java.db.schema.EntityMetricsTable;
import edu.uci.ics.sourcerer.tools.java.db.schema.ProjectMetricsTable;
import edu.uci.ics.sourcerer.tools.java.db.type.ModeledDeclaredType;
import edu.uci.ics.sourcerer.tools.java.db.type.ModeledEntity;
import edu.uci.ics.sourcerer.tools.java.db.type.ModeledParametrizedType;
import edu.uci.ics.sourcerer.tools.java.db.type.ModeledStructuralEntity;
import edu.uci.ics.sourcerer.tools.java.db.type.TypeModel;
import edu.uci.ics.sourcerer.tools.java.metrics.db.MetricModelFactory.ProjectMetricModel;
import edu.uci.ics.sourcerer.tools.java.model.types.Entity;
import edu.uci.ics.sourcerer.tools.java.model.types.Metric;
import edu.uci.ics.sourcerer.util.Averager;
import edu.uci.ics.sourcerer.utils.db.QueryExecutor;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class NumberOfChildrenCalculator extends Calculator {
  @Override
  public boolean shouldCalculate(ProjectMetricModel metrics) {
    return metrics.getValue(Metric.NUMBER_OF_CHILDREN) == null;
  }
  
  @Override
  public void calculate(QueryExecutor exec, Integer projectID, ProjectMetricModel metrics, TypeModel model) {
    Multiset<ModeledStructuralEntity> parents = HashMultiset.create();
    for (ModeledEntity entity : model.getEntities()) {
      if (entity.getType() == Entity.CLASS) {
        ModeledDeclaredType dec = (ModeledDeclaredType) entity;
        ModeledEntity sup = dec.getSuperclass();
        if (sup != null && sup.getType() == Entity.PARAMETERIZED_TYPE) {
          sup = ((ModeledParametrizedType) sup).getBaseType();
        }
        if (sup != null && projectID.equals(sup.getProjectID())) {
          if (sup instanceof ModeledStructuralEntity) {
            parents.add((ModeledStructuralEntity) sup);
          } else {
            logger.severe("Expacted structural entity, got: " + sup);
          }
        }
      }
    }
    Averager<Double> avgNOC = Averager.create();
    for (ModeledStructuralEntity entity : parents.elementSet()) {
      if (metrics.getEntityValue(entity.getEntityID(), Metric.NUMBER_OF_CHILDREN) == null) {
        Double value = (double) parents.count(entity);
        metrics.setEntityValue(entity.getEntityID(), entity.getFileID(), Metric.NUMBER_OF_CHILDREN, value);
        exec.insert(EntityMetricsTable.createInsert(projectID, entity.getFileID(), entity.getEntityID(), Metric.NUMBER_OF_CHILDREN, value));
        avgNOC.addValue(value);
      }
    }
    if (metrics.getValue(Metric.NUMBER_OF_CHILDREN) == null) {
      Double value = avgNOC.getMean();
      metrics.setValue(Metric.NUMBER_OF_CHILDREN, value);
      exec.insert(ProjectMetricsTable.createInsert(projectID, Metric.NUMBER_OF_CHILDREN, value));
    }
  }

}
