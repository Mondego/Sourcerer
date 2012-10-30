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

import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.regex.Pattern;

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
import edu.uci.ics.sourcerer.util.io.logging.TaskProgressLogger;
import edu.uci.ics.sourcerer.utils.db.QueryExecutor;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class NumberOfInterfaceRelativesCalculator extends Calculator {
  @Override
  public boolean shouldCalculate(ProjectMetricModel metrics) {
    return metrics.missingValue(Metric.NUMBER_OF_INTERFACE_CHILDREN, Metric.NUMBER_OF_DIRECT_INTERFACE_CHILDREN, Metric.NUMBER_OF_IMPLEMENTED_INTERFACES, Metric.NUMBER_OF_SUPER_INTERFACES);
  }
  
  @Override
  public void calculate(QueryExecutor exec, Integer projectID, ProjectMetricModel metrics, TypeModel model) {
    TaskProgressLogger task = TaskProgressLogger.get();
    
    Pattern anon = Pattern.compile(".*\\$\\d+$");
    
    task.start("Computing NumberOfInterfaceRelatives");
    Multiset<ModeledStructuralEntity> parents = HashMultiset.create();
    Multiset<ModeledDeclaredType> directParents = HashMultiset.create();
    Averager<Double> avgNoii = Averager.create();
    Averager<Double> avgNosi = Averager.create();
    task.start("Processing types", "types processed", 0);
    for (ModeledEntity entity : model.getEntities()) {
      if (projectID.equals(entity.getProjectID())) {
        if (entity.getType().is(Entity.CLASS, Entity.ENUM) && !anon.matcher(entity.getFqn()).matches()) {
          ModeledDeclaredType dec = (ModeledDeclaredType) entity;
          Double value = (double) dec.getInterfaces().size();
          if (metrics.missingEntityValue(dec.getEntityID(), Metric.NUMBER_OF_IMPLEMENTED_INTERFACES)) {
            metrics.setEntityValue(dec.getEntityID(), dec.getFileID(), Metric.NUMBER_OF_IMPLEMENTED_INTERFACES, value);
            exec.insert(EntityMetricsTable.createInsert(projectID, dec.getFileID(), dec.getEntityID(), Metric.NUMBER_OF_IMPLEMENTED_INTERFACES, value));
          }
          avgNoii.addValue(value);
          task.progress();
        } else if (entity.getType() == Entity.INTERFACE) {
          ModeledDeclaredType dec = (ModeledDeclaredType) entity;
          Double value = (double) dec.getInterfaces().size();
          if (metrics.missingEntityValue(dec.getEntityID(), Metric.NUMBER_OF_SUPER_INTERFACES)) {
            metrics.setEntityValue(dec.getEntityID(), dec.getFileID(), Metric.NUMBER_OF_SUPER_INTERFACES, value);
            exec.insert(EntityMetricsTable.createInsert(projectID, dec.getFileID(), dec.getEntityID(), Metric.NUMBER_OF_SUPER_INTERFACES, value));
          }
          avgNosi.addValue(value);
          Set<ModeledEntity> seen = new HashSet<>();
          for (ModeledEntity iface : dec.getInterfaces()) {
            Deque<ModeledEntity> stack = new LinkedList<>();
            stack.push(iface);
            boolean first = true;
            while (!stack.isEmpty()) {
              ModeledEntity next = stack.pop();
              if (!seen.contains(next)) {
                if (next.getType() == Entity.PARAMETERIZED_TYPE) {
                  ModeledEntity base =((ModeledParametrizedType) next).getBaseType();
                  if (base != null) {
                    stack.push(base);
                  }
                } else if (projectID.equals(next.getProjectID())) {
                  ModeledDeclaredType face = (ModeledDeclaredType) next;
                  if (first) {
                    directParents.add(face);
                    first = false;
                  }
                  seen.add(face);
                  parents.add(face);
                  stack.addAll(face.getInterfaces());
                }
              }
            }
          }
          task.progress();
        }
      }
    }
    task.finish();
    
    if (metrics.missingValue(Metric.NUMBER_OF_IMPLEMENTED_INTERFACES)) {
      metrics.setValue(Metric.NUMBER_OF_IMPLEMENTED_INTERFACES, avgNoii);
      exec.insert(ProjectMetricsTable.createInsert(projectID, Metric.NUMBER_OF_IMPLEMENTED_INTERFACES, avgNoii));
    }
    if (metrics.missingValue(Metric.NUMBER_OF_SUPER_INTERFACES)) {
      metrics.setValue(Metric.NUMBER_OF_SUPER_INTERFACES, avgNosi);
      exec.insert(ProjectMetricsTable.createInsert(projectID, Metric.NUMBER_OF_SUPER_INTERFACES, avgNosi));
    }
    Averager<Double> avgNoi = Averager.create();
    Averager<Double> avgDnoi = Averager.create();
    for (ModeledEntity entity : model.getEntities()) {
      if (projectID.equals(entity.getProjectID()) && entity.getType() == Entity.INTERFACE) {
        ModeledDeclaredType dec = (ModeledDeclaredType) entity;
        Double value = (double) parents.count(dec);
        if (metrics.missingEntityValue(dec.getEntityID(), Metric.NUMBER_OF_INTERFACE_CHILDREN)) {
          metrics.setEntityValue(dec.getEntityID(), dec.getFileID(), Metric.NUMBER_OF_INTERFACE_CHILDREN, value);
          exec.insert(EntityMetricsTable.createInsert(projectID, dec.getFileID(), dec.getEntityID(), Metric.NUMBER_OF_INTERFACE_CHILDREN, value));
        }
        avgNoi.addValue(value);
        
        value = (double) directParents.count(dec); 
        if (metrics.missingEntityValue(dec.getEntityID(), Metric.NUMBER_OF_DIRECT_INTERFACE_CHILDREN)) {
          metrics.setEntityValue(dec.getEntityID(), dec.getFileID(), Metric.NUMBER_OF_DIRECT_INTERFACE_CHILDREN, value);
          exec.insert(EntityMetricsTable.createInsert(projectID, dec.getFileID(), dec.getEntityID(), Metric.NUMBER_OF_DIRECT_INTERFACE_CHILDREN, value));
        }
        avgDnoi.addValue(value);
      }
    }
    if (metrics.missingValue(Metric.NUMBER_OF_INTERFACE_CHILDREN)) {
      metrics.setValue(Metric.NUMBER_OF_INTERFACE_CHILDREN, avgNoi);
      exec.insert(ProjectMetricsTable.createInsert(projectID, Metric.NUMBER_OF_INTERFACE_CHILDREN, avgNoi));
    }
    if (metrics.missingValue(Metric.NUMBER_OF_DIRECT_INTERFACE_CHILDREN)) {
      metrics.setValue(Metric.NUMBER_OF_DIRECT_INTERFACE_CHILDREN, avgDnoi);
      exec.insert(ProjectMetricsTable.createInsert(projectID, Metric.NUMBER_OF_DIRECT_INTERFACE_CHILDREN, avgDnoi));
    }
    task.finish();
  }
}
