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

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

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
public class NumberOfClassChildrenCalculator extends Calculator {
  @Override
  public boolean shouldCalculate(ProjectMetricModel metrics) {
    return metrics.missingValue(Metric.NUMBER_OF_CLASS_CHILDREN, Metric.NUMBER_OF_DIRECT_CLASS_CHILDREN);
  }
  
  @Override
  public void calculate(QueryExecutor exec, Integer projectID, ProjectMetricModel metrics, TypeModel model) {
    TaskProgressLogger task = TaskProgressLogger.get();

    Pattern anon = Pattern.compile(".*\\$\\d+$");
    
    task.start("Computing NumberOfClassChildren");
    Multiset<ModeledDeclaredType> parents = HashMultiset.create();
    Multiset<ModeledDeclaredType> directParents = HashMultiset.create();
    
    task.start("Processing classes", "classes processed");
    for (ModeledEntity entity : model.getEntities()) {
      if (projectID.equals(entity.getProjectID()) && entity.getType() == Entity.CLASS && !anon.matcher(entity.getFqn()).matches()) {
        ModeledDeclaredType dec = (ModeledDeclaredType) entity;
        ModeledEntity sup = dec.getSuperclass();
        boolean first = true;
        while (sup != null) {
          if (sup.getType() == Entity.PARAMETERIZED_TYPE) {
            sup = ((ModeledParametrizedType) sup).getBaseType();
          } else if (projectID.equals(sup.getProjectID())) {
            if (sup instanceof ModeledDeclaredType) {
              ModeledDeclaredType ssup = (ModeledDeclaredType) sup;
              if (first) {
                directParents.add(ssup);
                first = false;
              }
              parents.add(ssup);
              sup = ssup.getSuperclass();
            } else {
              logger.severe("Declared type expected: " + sup);
              sup = null;
            }
          } else {
            sup = null;
          }
        }
        task.progress();
      }
    }
    task.finish();
    Averager<Double> avgNoc = Averager.create();
    Averager<Double> avgDnoc = Averager.create();
    for (ModeledEntity entity : model.getEntities()) {
      if (projectID.equals(entity.getProjectID()) && entity.getType() == Entity.CLASS && !anon.matcher(entity.getFqn()).matches()) {
        ModeledDeclaredType dec = (ModeledDeclaredType) entity;
        Double value = metrics.getEntityValue(dec.getEntityID(), Metric.NUMBER_OF_CLASS_CHILDREN);
        if (value == null) {
          value = (double) parents.count(dec);
          metrics.setEntityValue(dec.getEntityID(), dec.getFileID(), Metric.NUMBER_OF_CLASS_CHILDREN, value);
          exec.insert(EntityMetricsTable.createInsert(projectID, dec.getFileID(), dec.getEntityID(), Metric.NUMBER_OF_CLASS_CHILDREN, value));
        }
        avgNoc.addValue(value);
        
        value = metrics.getEntityValue(dec.getEntityID(), Metric.NUMBER_OF_DIRECT_CLASS_CHILDREN);
        if (value == null) {
          value = (double) directParents.count(dec);
          metrics.setEntityValue(dec.getEntityID(), dec.getFileID(), Metric.NUMBER_OF_DIRECT_CLASS_CHILDREN, value);
          exec.insert(EntityMetricsTable.createInsert(projectID, dec.getFileID(), dec.getEntityID(), Metric.NUMBER_OF_DIRECT_CLASS_CHILDREN, value));
        }
        avgDnoc.addValue(value);
      }
    }
    if (metrics.missingValue(Metric.NUMBER_OF_CLASS_CHILDREN)) {
      metrics.setValue(Metric.NUMBER_OF_CLASS_CHILDREN, avgNoc);
      exec.insert(ProjectMetricsTable.createInsert(projectID, Metric.NUMBER_OF_CLASS_CHILDREN, avgNoc));
    }
    if (metrics.missingValue(Metric.NUMBER_OF_DIRECT_CLASS_CHILDREN)) {
      metrics.setValue(Metric.NUMBER_OF_DIRECT_CLASS_CHILDREN, avgDnoc);
      exec.insert(ProjectMetricsTable.createInsert(projectID, Metric.NUMBER_OF_DIRECT_CLASS_CHILDREN, avgDnoc));
    }
    task.finish();
  }
}
