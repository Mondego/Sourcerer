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

import java.util.Deque;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import edu.uci.ics.sourcerer.tools.java.db.schema.EntityMetricsTable;
import edu.uci.ics.sourcerer.tools.java.db.schema.ProjectMetricsTable;
import edu.uci.ics.sourcerer.tools.java.db.schema.RelationsTable;
import edu.uci.ics.sourcerer.tools.java.db.type.ModeledDeclaredType;
import edu.uci.ics.sourcerer.tools.java.db.type.ModeledEntity;
import edu.uci.ics.sourcerer.tools.java.db.type.ModeledParametrizedType;
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
public class AfferentCouplingCalculator extends Calculator {
  @Override
  public boolean shouldCalculate(ProjectMetricModel metrics) {
    return metrics.missingValue(Metric.EFFERENT_COUPLING);
  }

  @Override
  public void calculate(QueryExecutor exec, Integer projectID, ProjectMetricModel metrics, TypeModel model) {
    try (SelectQuery select = exec.createSelectQuery(RelationsTable.TABLE)) {
      select.addSelect(RelationsTable.LHS_EID);
      ConstantCondition<Integer> rhsEid = RelationsTable.RHS_EID.compareEquals();
      select.andWhere(rhsEid, RelationsTable.RELATION_TYPE.compareIn(EnumSet.of(Relation.HOLDS, Relation.RETURNS, Relation.READS, Relation.WRITES, Relation.CALLS, Relation.INSTANTIATES, Relation.CASTS, Relation.CHECKS, Relation.USES, Relation.EXTENDS, Relation.IMPLEMENTS)));
      
      Averager<Double> avgCoupling = Averager.create();
      Map<ModeledStructuralEntity, Set<ModeledStructuralEntity>> pkgCoupling = new HashMap<>();
      for (ModeledEntity entity : model.getEntities()) {
        if (entity.getType().is(Entity.CLASS, Entity.INTERFACE, Entity.ENUM)) {
          ModeledDeclaredType sEntity = (ModeledDeclaredType) entity;
          
          Set<ModeledDeclaredType> uses = new HashSet<>();
          {
            Deque<ModeledStructuralEntity> entities = new LinkedList<>();
            entities.push(sEntity);
            while (!entities.isEmpty()) {
              ModeledStructuralEntity next = entities.pop();
              // All the referencing things
              rhsEid.setValue(next.getEntityID());
              TypedQueryResult result = select.select();
              while (result.next()) {
                Integer lhsEid = result.getResult(RelationsTable.LHS_EID);
                ModeledEntity ent = model.get(lhsEid);
                if (ent != null) {
                  add(uses, ent);
                } else {
                  logger.severe("Unable to find model element for: " + rhsEid);
                }
              }
              // Add the children
              entities.addAll(next.getChildren());
            }
          }
          // Repeat the process for all the parametrized types referencing this type
          {
            Deque<ModeledParametrizedType> entities = new LinkedList<>();
            for (ModeledEntity ent : model.getEntities()) {
              if (ent.getType() == Entity.PARAMETERIZED_TYPE) {
                ModeledParametrizedType ptype = (ModeledParametrizedType) ent;
                if (entity == ptype.getBaseType()) {
                  entities.push(ptype);
                } else {
                  for (ModeledEntity arg : ptype.getTypeArgs()) {
                    if (entity == arg) {
                      entities.push(ptype);
                      break;
                    }
                  }
                }
              }
            }
            
            while (!entities.isEmpty()) {
              ModeledParametrizedType next = entities.pop();
              // All the referencing things
              rhsEid.setValue(next.getEntityID());
              TypedQueryResult result = select.select();
              while (result.next()) {
                Integer lhsEid = result.getResult(RelationsTable.LHS_EID);
                ModeledEntity ent = model.get(lhsEid);
                if (ent != null) {
                  add(uses, ent);
                } else {
                  logger.severe("Unable to find model element for: " + rhsEid);
                }
              }
            }
          }
          // Add to the pkg
          ModeledStructuralEntity owner = sEntity.getOwner();
          if (owner.getType() == Entity.PACKAGE) {
            Set<ModeledStructuralEntity> pkg = pkgCoupling.get(owner);
            if (pkg == null) {
              pkg = new HashSet<>();
              pkgCoupling.put(owner, pkg);
            }
            pkg.addAll(uses);
          }
          Double value = (double) uses.size();
          metrics.setEntityValue(sEntity.getEntityID(), sEntity.getFileID(), Metric.AFFERENT_COUPLING, value);
          exec.insert(EntityMetricsTable.createInsert(projectID, sEntity.getFileID(), sEntity.getEntityID(), Metric.AFFERENT_COUPLING, value));
          avgCoupling.addValue(value);
        }
      }

      for (Map.Entry<ModeledStructuralEntity, Set<ModeledStructuralEntity>> entry : pkgCoupling.entrySet()) {
        ModeledStructuralEntity entity = entry.getKey();
        Double value = (double) entry.getValue().size();
        
        metrics.setEntityValue(entity.getEntityID(), entity.getFileID(), Metric.AFFERENT_COUPLING, value);
        exec.insert(EntityMetricsTable.createInsert(projectID, entity.getFileID(), entity.getEntityID(), Metric.AFFERENT_COUPLING, value));
      }
      metrics.setValue(Metric.AFFERENT_COUPLING, avgCoupling);
      exec.insert(ProjectMetricsTable.createInsert(projectID, Metric.AFFERENT_COUPLING, avgCoupling));
    }
  }
  
  
  
  private void add(Set<ModeledDeclaredType> set, ModeledEntity entity) {
    if (entity != null) {
      if (entity instanceof ModeledStructuralEntity) {
        ModeledStructuralEntity struct = (ModeledStructuralEntity) entity;
        if (struct.getType().is(Entity.CLASS, Entity.ENUM, Entity.INTERFACE, Entity.ANNOTATION)) {
          set.add((ModeledDeclaredType) struct);
        } else {
          add(set, struct.getOwner());
        } 
      } else {
        logger.severe("Unexpected entity type: " + entity);
      }
    }
  }
}
