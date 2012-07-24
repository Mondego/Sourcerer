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

import edu.uci.ics.sourcerer.tools.java.db.schema.EntityMetricsTable;
import edu.uci.ics.sourcerer.tools.java.db.schema.ProjectMetricsTable;
import edu.uci.ics.sourcerer.tools.java.db.type.ModeledDeclaredType;
import edu.uci.ics.sourcerer.tools.java.db.type.ModeledEntity;
import edu.uci.ics.sourcerer.tools.java.db.type.ModeledParametrizedType;
import edu.uci.ics.sourcerer.tools.java.db.type.TypeModel;
import edu.uci.ics.sourcerer.tools.java.metrics.db.MetricModelFactory.ProjectMetricModel;
import edu.uci.ics.sourcerer.tools.java.model.types.Entity;
import edu.uci.ics.sourcerer.tools.java.model.types.Metric;
import edu.uci.ics.sourcerer.util.Averager;
import edu.uci.ics.sourcerer.utils.db.QueryExecutor;


/**
 * @author Joel Ossher (jossher@uci.edu)
 */
class InheritanceHierarchyDepthCalculator extends Calculator {
  @Override
  public boolean shouldCalculate(ProjectMetricModel metrics) {
    return metrics.missingValue(Metric.DEPTH_OF_INHERITANCE_TREE_CC, Metric.DEPTH_OF_INHERITANCE_TREE_CI, Metric.DEPTH_OF_INHERITANCE_TREE_II);
  }

  @Override
  public void calculate(QueryExecutor exec, Integer projectID, ProjectMetricModel metrics, TypeModel model) {
    Averager<Double> avgDoitcc = Averager.create();
    Averager<Double> avgDoitci = Averager.create();
    Averager<Double> avgDoitii = Averager.create();
    
    for (ModeledEntity entity : model.getEntities()) {
      if (entity.getType().is(Entity.CLASS, Entity.INTERFACE, Entity.ENUM)) {
        ModeledDeclaredType ent = (ModeledDeclaredType) entity;
        double interfaceDepth = calculateInterfaceDepth(entity);
        if (entity.getType() == Entity.INTERFACE) {
          if (metrics.missingEntityValue(ent.getEntityID(), Metric.DEPTH_OF_INHERITANCE_TREE_II)) {
            metrics.setEntityValue(ent.getEntityID(), ent.getFileID(), Metric.DEPTH_OF_INHERITANCE_TREE_II, interfaceDepth);
            exec.insert(EntityMetricsTable.createInsert(ent.getProjectID(), ent.getFileID(), ent.getEntityID(), Metric.DEPTH_OF_INHERITANCE_TREE_II, interfaceDepth));
          }
          avgDoitii.addValue(interfaceDepth);
        } else {
          if (metrics.missingEntityValue(ent.getEntityID(), Metric.DEPTH_OF_INHERITANCE_TREE_CI)) {
            metrics.setEntityValue(ent.getEntityID(), ent.getFileID(), Metric.DEPTH_OF_INHERITANCE_TREE_CI, interfaceDepth);
            exec.insert(EntityMetricsTable.createInsert(ent.getProjectID(), ent.getFileID(), ent.getEntityID(), Metric.DEPTH_OF_INHERITANCE_TREE_CI, interfaceDepth));
          }
          avgDoitci.addValue(interfaceDepth);
          
          double classDepth = calculateClassDepth(entity);
          if (metrics.missingEntityValue(ent.getEntityID(), Metric.DEPTH_OF_INHERITANCE_TREE_CC)) {
            metrics.setEntityValue(ent.getEntityID(), ent.getFileID(), Metric.DEPTH_OF_INHERITANCE_TREE_CC, classDepth);
            exec.insert(EntityMetricsTable.createInsert(ent.getProjectID(), ent.getFileID(), ent.getEntityID(), Metric.DEPTH_OF_INHERITANCE_TREE_CC, classDepth));
          }
          avgDoitcc.addValue(classDepth);
        }
      }
    }
    
    if (metrics.missingValue(Metric.DEPTH_OF_INHERITANCE_TREE_II)) {
      metrics.setValue(Metric.DEPTH_OF_INHERITANCE_TREE_II, avgDoitii);
      exec.insert(ProjectMetricsTable.createInsert(projectID, Metric.DEPTH_OF_INHERITANCE_TREE_II, avgDoitii));
    }
    if (metrics.missingValue(Metric.DEPTH_OF_INHERITANCE_TREE_CI)) {
      metrics.setValue(Metric.DEPTH_OF_INHERITANCE_TREE_CI, avgDoitci);
      exec.insert(ProjectMetricsTable.createInsert(projectID, Metric.DEPTH_OF_INHERITANCE_TREE_CI, avgDoitci));
    }
    if (metrics.missingValue(Metric.DEPTH_OF_INHERITANCE_TREE_CC)) {
      metrics.setValue(Metric.DEPTH_OF_INHERITANCE_TREE_CC, avgDoitcc);
      exec.insert(ProjectMetricsTable.createInsert(projectID, Metric.DEPTH_OF_INHERITANCE_TREE_CC, avgDoitcc));
    }
  }
  
  private int calculateClassDepth(ModeledEntity entity) {
    if (entity == null) {
      return 0;
    }
    if (entity.getType() == Entity.PARAMETERIZED_TYPE) {
      entity = ((ModeledParametrizedType) entity).getBaseType();
    }
    if (entity.getType().is(Entity.CLASS, Entity.INTERFACE, Entity.ENUM)) {
      return 1 + calculateClassDepth(((ModeledDeclaredType) entity).getSuperclass());
    } else {
      logger.severe("Invalid entity type: " + entity);
      return -1;
    }
  }
  
  private int calculateInterfaceDepth(ModeledEntity entity) {
    if (entity.getType() == Entity.PARAMETERIZED_TYPE) {
      entity = ((ModeledParametrizedType) entity).getBaseType();
    }
    if (entity.getType().is(Entity.CLASS, Entity.INTERFACE, Entity.ENUM)) {
      ModeledDeclaredType ent = (ModeledDeclaredType) entity;
      int max = 0;
      for (ModeledEntity iface : ent.getInterfaces()) {
        max = Math.max(max, calculateInterfaceDepth(iface));
      }
      return max;
    } else {
      logger.severe("Invalid entity type: " + entity);
      return -1;
    }
  }
}
