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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import edu.uci.ics.sourcerer.tools.java.db.schema.EntityMetricsTable;
import edu.uci.ics.sourcerer.tools.java.db.schema.ProjectMetricsTable;
import edu.uci.ics.sourcerer.tools.java.db.schema.RelationsTable;
import edu.uci.ics.sourcerer.tools.java.db.type.ModeledDeclaredType;
import edu.uci.ics.sourcerer.tools.java.db.type.ModeledDuplicate;
import edu.uci.ics.sourcerer.tools.java.db.type.ModeledEntity;
import edu.uci.ics.sourcerer.tools.java.db.type.ModeledMethod;
import edu.uci.ics.sourcerer.tools.java.db.type.ModeledStructuralEntity;
import edu.uci.ics.sourcerer.tools.java.db.type.TypeModel;
import edu.uci.ics.sourcerer.tools.java.metrics.db.MetricModelFactory.ProjectMetricModel;
import edu.uci.ics.sourcerer.tools.java.model.types.Entity;
import edu.uci.ics.sourcerer.tools.java.model.types.Metric;
import edu.uci.ics.sourcerer.tools.java.model.types.Relation;
import edu.uci.ics.sourcerer.util.Averager;
import edu.uci.ics.sourcerer.util.UniqueStack;
import edu.uci.ics.sourcerer.util.io.logging.TaskProgressLogger;
import edu.uci.ics.sourcerer.utils.db.QueryExecutor;
import edu.uci.ics.sourcerer.utils.db.sql.SelectQuery;
import edu.uci.ics.sourcerer.utils.db.sql.TypedQueryResult;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class ResponseForClassCalculator extends Calculator {
  @Override
  public boolean shouldCalculate(ProjectMetricModel metrics) {
    return metrics.missingValue(Metric.RESPONSE_FOR_CLASS);
  }

  @Override
  public void calculate(QueryExecutor exec, Integer projectID, ProjectMetricModel metrics, TypeModel model) {
    TaskProgressLogger task = TaskProgressLogger.get();
    
    Pattern anon = Pattern.compile(".*\\$\\d+$");
    
    task.start("Computing response for a class");
    Map<ModeledDeclaredType, Set<ModeledMethod>> methodMap = new HashMap<>();
    
    try (SelectQuery select = exec.createSelectQuery(RelationsTable.TABLE)) {
      select.addSelect(RelationsTable.LHS_EID, RelationsTable.RHS_EID);
      select.andWhere(RelationsTable.PROJECT_ID.compareEquals(projectID), RelationsTable.RELATION_TYPE.compareEquals(Relation.CALLS));
      
      task.start("Processing relations", "relations processed", 100_000);
      TypedQueryResult result = select.select();
      while (result.next()) {
        Integer lhsEid = result.getResult(RelationsTable.LHS_EID);
        Integer rhsEid = result.getResult(RelationsTable.RHS_EID);
        
        ModeledEntity lhs = model.get(lhsEid);
        if (lhs == null) {
          logger.severe("Unable to find model element for: " + lhsEid);
          continue;
        }
        ModeledEntity rhs = model.get(rhsEid);
        if (rhs == null) {
          logger.severe("Unable to find model element for: " + rhsEid);
          continue;
        }
        
        UniqueStack<ModeledEntity> stack = UniqueStack.create(true);
        stack.push(lhs);
        while (stack.hasItems()) {
          ModeledEntity next = stack.pop();
          if (next instanceof ModeledStructuralEntity) {
            ModeledStructuralEntity struct = (ModeledStructuralEntity) next;
            if (struct.getType().is(Entity.CLASS, Entity.ENUM)) {
              ModeledDeclaredType dec = (ModeledDeclaredType) struct;
              Set<ModeledMethod> methods = methodMap.get(dec);
              if (methods == null) {
                methods = new HashSet<>();
                methodMap.put(dec, methods);
              }
              add(methods, rhs);
            }
            if (struct.getType() != Entity.PACKAGE) {
              stack.push(struct.getOwner());
            }
          } else {
            logger.severe("Unexpected lhs: " + next);
          }
        }
        task.progress();
      }
      task.finish();
    }
    
    task.start("Computing and adding the metrics");
    Averager<Double> avgRfc = Averager.create();
    for (ModeledEntity entity : model.getEntities()) {
      if (projectID.equals(entity.getProjectID()) && entity.getType().is(Entity.CLASS, Entity.ENUM) && !anon.matcher(entity.getFqn()).matches()) {
        ModeledDeclaredType dec = (ModeledDeclaredType) entity;
        Set<ModeledMethod> methods = methodMap.get(dec);
        if (methods == null) {
          methods = Collections.emptySet();
        }
          
        double value = (double) methods.size();
        if (metrics.missingEntityValue(dec.getEntityID(), Metric.RESPONSE_FOR_CLASS)) {
          metrics.setEntityValue(dec.getEntityID(), dec.getFileID(), Metric.RESPONSE_FOR_CLASS, value);
          exec.insert(EntityMetricsTable.createInsert(dec.getProjectID(), dec.getFileID(), dec.getEntityID(), Metric.RESPONSE_FOR_CLASS, value));
        }
        avgRfc.addValue(value);
      }
    }
      
    if (metrics.missingValue(Metric.RESPONSE_FOR_CLASS)) {
      metrics.setValue(Metric.RESPONSE_FOR_CLASS, avgRfc);
      exec.insert(ProjectMetricsTable.createInsert(projectID, Metric.RESPONSE_FOR_CLASS, avgRfc));
    }
    task.finish();
    
    task.finish();
  }
  
  private void add(Set<ModeledMethod> methods, ModeledEntity method) {
    if (method.getType().is(Entity.METHOD, Entity.CONSTRUCTOR)) {
      methods.add((ModeledMethod) method);
    } else if (method.getType().is(Entity.DUPLICATE, Entity.VIRTUAL_DUPLICATE)) {
      ModeledDuplicate dup = (ModeledDuplicate) method;
      for (ModeledEntity ent : dup.getMatches()) {
        add(methods, ent);
      }
    }
  }
}
