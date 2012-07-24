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

import edu.uci.ics.sourcerer.tools.java.db.schema.EntityMetricsTable;
import edu.uci.ics.sourcerer.tools.java.db.schema.ProjectMetricsTable;
import edu.uci.ics.sourcerer.tools.java.db.schema.RelationsTable;
import edu.uci.ics.sourcerer.tools.java.db.type.ModeledDeclaredType;
import edu.uci.ics.sourcerer.tools.java.db.type.ModeledEntity;
import edu.uci.ics.sourcerer.tools.java.db.type.ModeledStructuralEntity;
import edu.uci.ics.sourcerer.tools.java.db.type.TypeModel;
import edu.uci.ics.sourcerer.tools.java.metrics.db.MetricModelFactory.ProjectMetricModel;
import edu.uci.ics.sourcerer.tools.java.model.types.Entity;
import edu.uci.ics.sourcerer.tools.java.model.types.Metric;
import edu.uci.ics.sourcerer.tools.java.model.types.Relation;
import edu.uci.ics.sourcerer.util.Averager;
import edu.uci.ics.sourcerer.utils.db.QueryExecutor;
import edu.uci.ics.sourcerer.utils.db.sql.ConstantCondition;
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
    try (SelectQuery getCalls = exec.createSelectQuery(RelationsTable.TABLE)) {
      getCalls.addSelect(RelationsTable.RHS_EID);
      ConstantCondition<Integer> lhsEid = RelationsTable.LHS_EID.compareEquals();
      getCalls.andWhere(lhsEid, RelationsTable.RELATION_TYPE.compareEquals(Relation.CALLS));
      
      Averager<Double> avgRfc = Averager.create();
      for (ModeledEntity entity : model.getEntities()) {
        if (entity.getType().is(Entity.CLASS, Entity.ENUM)) {
          ModeledDeclaredType dec = (ModeledDeclaredType) entity;
          
          Set<Integer> called = new HashSet<>();
          Deque<ModeledStructuralEntity> entities = new LinkedList<>();
          entities.push(dec);
          while (!entities.isEmpty()) {
            ModeledStructuralEntity next = entities.pop();
            
            lhsEid.setValue(next.getEntityID());
            TypedQueryResult result = getCalls.select();
            while (result.next()) {
              called.add(result.getResult(RelationsTable.RHS_EID));
            }
            
            entities.addAll(next.getChildren());
          }
          
          double value = (double) called.size();
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
    }
  }
}
