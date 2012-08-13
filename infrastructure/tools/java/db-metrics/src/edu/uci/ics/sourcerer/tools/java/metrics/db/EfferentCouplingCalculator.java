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
import java.util.regex.Pattern;

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
import edu.uci.ics.sourcerer.util.io.logging.TaskProgressLogger;
import edu.uci.ics.sourcerer.utils.db.QueryExecutor;
import edu.uci.ics.sourcerer.utils.db.sql.ConstantCondition;
import edu.uci.ics.sourcerer.utils.db.sql.SelectQuery;
import edu.uci.ics.sourcerer.utils.db.sql.TypedQueryResult;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class EfferentCouplingCalculator extends Calculator {
  @Override
  public boolean shouldCalculate(ProjectMetricModel metrics) {
    return metrics.getValue(Metric.EFFERENT_COUPLING) == null;
  }

  @Override
  public void calculate(QueryExecutor exec, Integer projectID, ProjectMetricModel metrics, TypeModel model) {
    TaskProgressLogger task = TaskProgressLogger.get();
    
    Pattern anon = Pattern.compile(".*\\$\\d+$");
    
    try (SelectQuery select = exec.createSelectQuery(RelationsTable.TABLE)) {
      select.addSelect(RelationsTable.RHS_EID);
      ConstantCondition<Integer> lhsEid = RelationsTable.LHS_EID.compareEquals();
      select.andWhere(lhsEid, RelationsTable.RELATION_TYPE.compareIn(EnumSet.of(Relation.HOLDS, Relation.RETURNS, Relation.READS, Relation.WRITES, Relation.CALLS, Relation.INSTANTIATES, Relation.CASTS, Relation.CHECKS, Relation.USES)));
      
      task.start("Computing EfferentCoupling");
      Averager<Double> avgCoupling = Averager.create();
      Map<ModeledStructuralEntity, Set<ModeledStructuralEntity>> pkgCoupling = new HashMap<>();
      
      task.start("Processing entities", "entities processed");
      for (ModeledEntity entity : model.getEntities()) {
        if (projectID.equals(entity.getProjectID()) && entity.getType().is(Entity.CLASS, Entity.ENUM, Entity.INTERFACE) && !anon.matcher(entity.getFqn()).matches()) {
          ModeledStructuralEntity sEntity = (ModeledStructuralEntity) entity;
          
          Set<ModeledDeclaredType> used = new HashSet<>();
          Deque<ModeledStructuralEntity> entities = new LinkedList<>();
          entities.add(sEntity);
          while (!entities.isEmpty()) {
            ModeledStructuralEntity next = entities.pop();
            if (next.getType().is(Entity.CLASS, Entity.ENUM, Entity.INTERFACE)) {
              ModeledDeclaredType dec = (ModeledDeclaredType) next;
              // Add the superclass
              add(used, dec.getSuperclass());
              // Add the interfaces
              for (ModeledEntity iface : dec.getInterfaces()) {
                add(used, iface);
              }
            }
            // All the referenced things
            lhsEid.setValue(next.getEntityID());
            TypedQueryResult result = select.select();
            while (result.next()) {
              Integer rhsEid = result.getResult(RelationsTable.RHS_EID);
              ModeledEntity ent = model.get(rhsEid);
              if (ent != null) {
                add(used, entity);
              } else {
                logger.severe("Unable to find model element for: " + rhsEid);
              }
            }
            // Add the children
            entities.addAll(next.getChildren());
          }
          // Add to the pkg
          ModeledStructuralEntity owner = sEntity.getOwner();
          if (owner.getType() == Entity.PACKAGE) {
            Set<ModeledStructuralEntity> pkg = pkgCoupling.get(owner);
            if (pkg == null) {
              pkg = new HashSet<>();
              pkgCoupling.put(owner, pkg);
            }
            pkg.addAll(used);
          }
          Double value = (double) used.size();
          int internalUsed = 0;
          for (ModeledDeclaredType)
          if (metrics.missingEntityValue(sEntity.getEntityID(), Metric.EFFERENT_COUPLING)) {
            metrics.setEntityValue(sEntity.getEntityID(), sEntity.getFileID(), Metric.EFFERENT_COUPLING, value);
            exec.insert(EntityMetricsTable.createInsert(projectID, sEntity.getFileID(), sEntity.getEntityID(), Metric.EFFERENT_COUPLING, value));
          }
          avgCoupling.addValue(value);
          
          task.progress();
        }
      }

      for (Map.Entry<ModeledStructuralEntity, Set<ModeledStructuralEntity>> entry : pkgCoupling.entrySet()) {
        ModeledStructuralEntity entity = entry.getKey();
        Double value = (double) entry.getValue().size();
        
        if (metrics.missingEntityValue(entity.getEntityID(), Metric.EFFERENT_COUPLING)) {
          metrics.setEntityValue(entity.getEntityID(), entity.getFileID(), Metric.EFFERENT_COUPLING, value);
          exec.insert(EntityMetricsTable.createInsert(projectID, entity.getFileID(), entity.getEntityID(), Metric.EFFERENT_COUPLING, value));
        }
      }
      if (metrics.missingValue(Metric.EFFERENT_COUPLING)) {
        metrics.setValue(Metric.EFFERENT_COUPLING, avgCoupling);
        exec.insert(ProjectMetricsTable.createInsert(projectID, Metric.EFFERENT_COUPLING, avgCoupling));
      }
    }
  }
  
  
  
  private void add(Set<ModeledDeclaredType> set, ModeledEntity entity) {
    if (entity != null) {
      if (entity.getType().is(Entity.CLASS, Entity.INTERFACE, Entity.ENUM, Entity.ANNOTATION)) {
        set.add((ModeledDeclaredType) entity);
      } else if (entity.getType().is(Entity.CONSTRUCTOR, Entity.METHOD, Entity.))
      if (entity instanceof ModeledStructuralEntity) {
        ModeledStructuralEntity struct = (ModeledStructuralEntity) entity;
        if (struct.getType().is(Entity.CLASS, Entity.INTERFACE, Entity.ENUM, Entity.ANNOTATION)) {
          
        } else {
          add(set, struct.getOwner());
        } 
      } else if (entity instanceof ModeledParametrizedType) {
        ModeledParametrizedType pEntity = (ModeledParametrizedType) entity;
        set.add(pEntity.getBaseType());
        for (ModeledEntity ent : pEntity.getTypeArgs()) {
          add(set, ent);
        }
      }
    }
  }
}
