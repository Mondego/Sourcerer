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

import java.util.regex.Pattern;

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
import edu.uci.ics.sourcerer.util.io.logging.TaskProgressLogger;
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
    TaskProgressLogger task = TaskProgressLogger.get();
    
    Pattern anon = Pattern.compile(".*\\$\\d+$");
    
    task.start("Computing InheritanceHierarchyDepth");
    Averager<Double> avgDoitcc = Averager.create();
    Averager<Double> avgDoiticc = Averager.create();
    Averager<Double> avgDoitci = Averager.create();
    Averager<Double> avgDoitici = Averager.create();
    Averager<Double> avgDoitii = Averager.create();
    Averager<Double> avgDoitiii = Averager.create();
    
    task.start("Processing declared types", "declared types processed", 0);
    for (ModeledEntity entity : model.getEntities()) {
      if (projectID.equals(entity.getProjectID()) && entity.getType().is(Entity.CLASS, Entity.INTERFACE, Entity.ENUM) && !anon.matcher(entity.getFqn()).matches()) {
        ModeledDeclaredType ent = (ModeledDeclaredType) entity;
        double interfaceDepth = 0;
        try {
          interfaceDepth = calculateInterfaceDepth(entity);
        } catch (StackOverflowError e) {
          logger.severe("Infinite stack for: " + entity);
        }
        double internalInterfaceDepth = 0;
        try {
          internalInterfaceDepth = calculateInternalInterfaceDepth(entity, projectID);
        } catch (StackOverflowError e) {
          logger.severe("Infinite stack for: " + entity);
        }
        if (entity.getType() == Entity.INTERFACE) {
          if (metrics.missingEntityValue(ent.getEntityID(), Metric.DEPTH_OF_INHERITANCE_TREE_II)) {
            metrics.setEntityValue(ent.getEntityID(), ent.getFileID(), Metric.DEPTH_OF_INHERITANCE_TREE_II, interfaceDepth);
            exec.insert(EntityMetricsTable.createInsert(ent.getProjectID(), ent.getFileID(), ent.getEntityID(), Metric.DEPTH_OF_INHERITANCE_TREE_II, interfaceDepth));
          }
          avgDoitii.addValue(interfaceDepth);
          if (metrics.missingEntityValue(ent.getEntityID(), Metric.DEPTH_OF_INHERITANCE_TREE_INTERNAL_II)) {
            metrics.setEntityValue(ent.getEntityID(), ent.getFileID(), Metric.DEPTH_OF_INHERITANCE_TREE_INTERNAL_II, internalInterfaceDepth);
            exec.insert(EntityMetricsTable.createInsert(ent.getProjectID(), ent.getFileID(), ent.getEntityID(), Metric.DEPTH_OF_INHERITANCE_TREE_INTERNAL_II, internalInterfaceDepth));
          }
          avgDoitiii.addValue(internalInterfaceDepth);
        } else {
          if (metrics.missingEntityValue(ent.getEntityID(), Metric.DEPTH_OF_INHERITANCE_TREE_CI)) {
            metrics.setEntityValue(ent.getEntityID(), ent.getFileID(), Metric.DEPTH_OF_INHERITANCE_TREE_CI, interfaceDepth);
            exec.insert(EntityMetricsTable.createInsert(ent.getProjectID(), ent.getFileID(), ent.getEntityID(), Metric.DEPTH_OF_INHERITANCE_TREE_CI, interfaceDepth));
          }
          avgDoitci.addValue(interfaceDepth);
          
          if (metrics.missingEntityValue(ent.getEntityID(), Metric.DEPTH_OF_INHERITANCE_TREE_INTERNAL_CI)) {
            metrics.setEntityValue(ent.getEntityID(), ent.getFileID(), Metric.DEPTH_OF_INHERITANCE_TREE_INTERNAL_CI, internalInterfaceDepth);
            exec.insert(EntityMetricsTable.createInsert(ent.getProjectID(), ent.getFileID(), ent.getEntityID(), Metric.DEPTH_OF_INHERITANCE_TREE_INTERNAL_CI, internalInterfaceDepth));
          }
          avgDoitici.addValue(internalInterfaceDepth);
          
          double classDepth = calculateClassDepth(entity);
          double internalClassDepth = calculateInternalClassDepth(entity, projectID);
          if (metrics.missingEntityValue(ent.getEntityID(), Metric.DEPTH_OF_INHERITANCE_TREE_CC)) {
            metrics.setEntityValue(ent.getEntityID(), ent.getFileID(), Metric.DEPTH_OF_INHERITANCE_TREE_CC, classDepth);
            exec.insert(EntityMetricsTable.createInsert(ent.getProjectID(), ent.getFileID(), ent.getEntityID(), Metric.DEPTH_OF_INHERITANCE_TREE_CC, classDepth));
          }
          avgDoiticc.addValue(internalClassDepth);
          if (metrics.missingEntityValue(ent.getEntityID(), Metric.DEPTH_OF_INHERITANCE_TREE_INTERNAL_CC)) {
            metrics.setEntityValue(ent.getEntityID(), ent.getFileID(), Metric.DEPTH_OF_INHERITANCE_TREE_INTERNAL_CC, internalClassDepth);
            exec.insert(EntityMetricsTable.createInsert(ent.getProjectID(), ent.getFileID(), ent.getEntityID(), Metric.DEPTH_OF_INHERITANCE_TREE_INTERNAL_CC, internalClassDepth));
          }
          avgDoitcc.addValue(classDepth);
        }
        task.progress();
      }
    }
    task.finish();
    
    if (metrics.missingValue(Metric.DEPTH_OF_INHERITANCE_TREE_II)) {
      metrics.setValue(Metric.DEPTH_OF_INHERITANCE_TREE_II, avgDoitii);
      exec.insert(ProjectMetricsTable.createInsert(projectID, Metric.DEPTH_OF_INHERITANCE_TREE_II, avgDoitii));
    }
    if (metrics.missingValue(Metric.DEPTH_OF_INHERITANCE_TREE_INTERNAL_II)) {
      metrics.setValue(Metric.DEPTH_OF_INHERITANCE_TREE_INTERNAL_II, avgDoitiii);
      exec.insert(ProjectMetricsTable.createInsert(projectID, Metric.DEPTH_OF_INHERITANCE_TREE_INTERNAL_II, avgDoitiii));
    }
    if (metrics.missingValue(Metric.DEPTH_OF_INHERITANCE_TREE_CI)) {
      metrics.setValue(Metric.DEPTH_OF_INHERITANCE_TREE_CI, avgDoitci);
      exec.insert(ProjectMetricsTable.createInsert(projectID, Metric.DEPTH_OF_INHERITANCE_TREE_CI, avgDoitci));
    }
    if (metrics.missingValue(Metric.DEPTH_OF_INHERITANCE_TREE_INTERNAL_CI)) {
      metrics.setValue(Metric.DEPTH_OF_INHERITANCE_TREE_INTERNAL_CI, avgDoitici);
      exec.insert(ProjectMetricsTable.createInsert(projectID, Metric.DEPTH_OF_INHERITANCE_TREE_INTERNAL_CI, avgDoitici));
    }
    if (metrics.missingValue(Metric.DEPTH_OF_INHERITANCE_TREE_CC)) {
      metrics.setValue(Metric.DEPTH_OF_INHERITANCE_TREE_CC, avgDoitcc);
      exec.insert(ProjectMetricsTable.createInsert(projectID, Metric.DEPTH_OF_INHERITANCE_TREE_CC, avgDoitcc));
    }
    if (metrics.missingValue(Metric.DEPTH_OF_INHERITANCE_TREE_INTERNAL_CC)) {
      metrics.setValue(Metric.DEPTH_OF_INHERITANCE_TREE_INTERNAL_CC, avgDoiticc);
      exec.insert(ProjectMetricsTable.createInsert(projectID, Metric.DEPTH_OF_INHERITANCE_TREE_INTERNAL_CC, avgDoiticc));
    }
    task.finish();
  }
  
  private int calculateClassDepth(ModeledEntity entity) {
    if (entity == null) {
      return 0;
    }
    if (entity.getType() == Entity.PARAMETERIZED_TYPE) {
      ModeledEntity base = ((ModeledParametrizedType) entity).getBaseType();
      if (base == null) {
        logger.severe("Null base type: " + entity);
        return 0;
      } else {
        entity = base;
      }
    }
    if (entity.getType().is(Entity.CLASS, Entity.INTERFACE, Entity.ENUM)) {
      return 1 + calculateClassDepth(((ModeledDeclaredType) entity).getSuperclass());
    } else if (entity.getType() == Entity.UNKNOWN) {
      return 2; // minimum of 2
    } else {
      logger.severe("Invalid entity type: " + entity);
      return 0;
    }
  }
  
  private int calculateInternalClassDepth(ModeledEntity entity, Integer projectID) {
    if (entity == null) {
      return 0;
    }
    if (entity.getType() == Entity.PARAMETERIZED_TYPE) {
      ModeledEntity base = ((ModeledParametrizedType) entity).getBaseType();
      if (base == null) {
        logger.severe("Null base type: " + entity);
        return 0;
      } else {
        entity = base;
      }
    }
    if (!projectID.equals(entity.getProjectID()) || entity.getType() == Entity.UNKNOWN) {
      return 0;
    } else if (entity.getType().is(Entity.CLASS, Entity.INTERFACE, Entity.ENUM)) {
      return 1 + calculateInternalClassDepth(((ModeledDeclaredType) entity).getSuperclass(), projectID);
    } else {
      logger.severe("Invalid entity type: " + entity);
      return 0;
    }
  }
  
  private int calculateInterfaceDepth(ModeledEntity entity) {
    if (entity.getType() == Entity.PARAMETERIZED_TYPE) {
      ModeledEntity base = ((ModeledParametrizedType) entity).getBaseType();
      if (base == null) {
        logger.severe("Null base type: " + entity);
        return 0;
      } else {
        entity = base;
      }
    }
    if (entity.getType().is(Entity.CLASS, Entity.INTERFACE, Entity.ENUM)) {
      ModeledDeclaredType ent = (ModeledDeclaredType) entity;
      int max = 0;
      for (ModeledEntity iface : ent.getInterfaces()) {
        max = Math.max(max, 1 + calculateInterfaceDepth(iface));
      }
      return max;
    } else if (entity.getType() == Entity.UNKNOWN) {
      return 1; // minimum of 1
    } else {
      logger.severe("Invalid entity type: " + entity);
      return 0;
    }
  }
  
  private int calculateInternalInterfaceDepth(ModeledEntity entity, Integer projectID) {
    if (entity.getType() == Entity.PARAMETERIZED_TYPE) {
      ModeledEntity base = ((ModeledParametrizedType) entity).getBaseType();
      if (base == null) {
        logger.severe("Null base type: " + entity);
        return 0;
      } else {
        entity = base;
      }
    }
    if (!projectID.equals(entity.getProjectID()) || entity.getType() == Entity.UNKNOWN) {
      return 0;
    } else if (entity.getType().is(Entity.CLASS, Entity.INTERFACE, Entity.ENUM)) {
      ModeledDeclaredType ent = (ModeledDeclaredType) entity;
      int max = 0;
      for (ModeledEntity iface : ent.getInterfaces()) {
        max = Math.max(max, 1 + calculateInternalInterfaceDepth(iface, projectID));
      }
      return max;
    } else if (entity.getType() == Entity.UNKNOWN) {
      return 1; // minimum of 1
    } else {
      logger.severe("Invalid entity type: " + entity);
      return 0;
    }
  }
}
