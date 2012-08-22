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
import edu.uci.ics.sourcerer.tools.java.db.type.ModeledArrayType;
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
public class InternalEfferentCouplingCalculator extends Calculator {
  @Override
  public boolean shouldCalculate(ProjectMetricModel metrics) {
    return metrics.missingValue(Metric.EFFERENT_COUPLING_INTERNAL);
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
      Averager<Double> avgInternalCoupling = Averager.create();
      Map<ModeledStructuralEntity, Set<ModeledEntity>> pkgCoupling = new HashMap<>();
      
      task.start("Processing entities", "entities processed");
      for (ModeledEntity entity : model.getEntities()) {
        if (projectID.equals(entity.getProjectID()) && entity.getType().is(Entity.CLASS, Entity.ENUM, Entity.INTERFACE) && !anon.matcher(entity.getFqn()).matches()) {
          ModeledDeclaredType dec = (ModeledDeclaredType) entity;
          Set<ModeledEntity> referencedTypes = new HashSet<>();
          
          Deque<ModeledStructuralEntity> stack = new LinkedList<>();
          stack.push(dec);
          while (!stack.isEmpty()) {
            ModeledStructuralEntity next = stack.pop();
            if (next.getType().is(Entity.CLASS, Entity.ENUM, Entity.INTERFACE)) {
              ModeledDeclaredType decNext = (ModeledDeclaredType) next;
              // Add the superclass
              add(referencedTypes, decNext.getSuperclass());
              // Add the interfaces
              for (ModeledEntity iface : decNext.getInterfaces()) {
                add(referencedTypes, iface);
              }
            }
            // All the referenced types
            lhsEid.setValue(next.getEntityID());
            TypedQueryResult result = select.select();
            while (result.next()) {
              Integer rhsEid = result.getResult(RelationsTable.RHS_EID);
              ModeledEntity rhs = model.get(rhsEid);
              if (rhs == null) {
                logger.severe("Unable to find model element for: " + rhsEid);
              } else {
                add(referencedTypes, rhs);
              }
            }
            
            // Add the children
            stack.addAll(next.getChildren());
          }
          
          // Add to the pkg
          ModeledStructuralEntity pkg = dec.getOwner();
          if (pkg.getType() == Entity.PACKAGE) {
            Set<ModeledEntity> pkgReferencedTypes = pkgCoupling.get(pkg);
            if (pkgReferencedTypes == null) {
              pkgReferencedTypes = new HashSet<>();
              pkgCoupling.put(pkg, pkgReferencedTypes);
            }
            pkgReferencedTypes.addAll(referencedTypes);
          }
          
          int internalReferencedTypes = 0;
          for (ModeledEntity used : referencedTypes) {
            if (projectID.equals(used.getProjectID())) {
              internalReferencedTypes++;
            }
          }
          
          Double coupling = (double) referencedTypes.size();
          Double internalCoupling = (double) internalReferencedTypes;
          
          if (metrics.missingEntityValue(dec.getEntityID(), Metric.EFFERENT_COUPLING)) {
            metrics.setEntityValue(dec.getEntityID(), dec.getFileID(), Metric.EFFERENT_COUPLING, coupling);
            exec.insert(EntityMetricsTable.createInsert(projectID, dec.getFileID(), dec.getEntityID(), Metric.EFFERENT_COUPLING, coupling));
          }
          avgCoupling.addValue(coupling);
          
          if (metrics.missingEntityValue(dec.getEntityID(), Metric.EFFERENT_COUPLING_INTERNAL)) {
            metrics.setEntityValue(dec.getEntityID(), dec.getFileID(), Metric.EFFERENT_COUPLING_INTERNAL, internalCoupling);
            exec.insert(EntityMetricsTable.createInsert(projectID, dec.getFileID(), dec.getEntityID(), Metric.EFFERENT_COUPLING_INTERNAL, internalCoupling));
          }
          avgInternalCoupling.addValue(internalCoupling);
          
          task.progress();
        }
      }
      task.finish();
      
      if (metrics.missingValue(Metric.EFFERENT_COUPLING)) {
        metrics.setValue(Metric.EFFERENT_COUPLING, avgCoupling);
        exec.insert(ProjectMetricsTable.createInsert(projectID, Metric.EFFERENT_COUPLING, avgCoupling));
      }
      if (metrics.missingValue(Metric.EFFERENT_COUPLING_INTERNAL)) {
        metrics.setValue(Metric.EFFERENT_COUPLING_INTERNAL, avgInternalCoupling);
        exec.insert(ProjectMetricsTable.createInsert(projectID, Metric.EFFERENT_COUPLING_INTERNAL, avgInternalCoupling));
      }

      for (Map.Entry<ModeledStructuralEntity, Set<ModeledEntity>> entry : pkgCoupling.entrySet()) {
        ModeledStructuralEntity pkg = entry.getKey();
        int internalReferencedTypes = 0;
        for (ModeledEntity referencedType : entry.getValue()) {
          if (projectID.equals(referencedType.getEntityID())) {
            internalReferencedTypes++;
          }
        }
        Double coupling = (double) entry.getValue().size();
        Double internalCoupling = (double) internalReferencedTypes;
        
        if (metrics.missingEntityValue(pkg.getEntityID(), Metric.EFFERENT_COUPLING)) {
          metrics.setEntityValue(pkg.getEntityID(), null, Metric.EFFERENT_COUPLING, coupling);
          exec.insert(EntityMetricsTable.createInsert(projectID, null, pkg.getEntityID(), Metric.EFFERENT_COUPLING, coupling));
        }
        if (metrics.missingEntityValue(pkg.getEntityID(), Metric.EFFERENT_COUPLING_INTERNAL)) {
          metrics.setEntityValue(pkg.getEntityID(), null, Metric.EFFERENT_COUPLING_INTERNAL, internalCoupling);
          exec.insert(EntityMetricsTable.createInsert(projectID, null, pkg.getEntityID(), Metric.EFFERENT_COUPLING_INTERNAL, internalCoupling));
        }
      }
      task.finish();
    }
  }
  
  private void add(Set<ModeledEntity> set, ModeledEntity entity) {
    if (entity != null) {
      if (entity.getType().is(Entity.CLASS, Entity.INTERFACE, Entity.ENUM, Entity.ANNOTATION)) {
        set.add(entity);
      } else if (entity.getType().is(Entity.CONSTRUCTOR, Entity.METHOD, Entity.FIELD, Entity.ENUM_CONSTANT)) {
        add(set, ((ModeledStructuralEntity) entity).getOwner());
      } else if (entity.getType() == Entity.PARAMETERIZED_TYPE) {
        ModeledParametrizedType pEntity = (ModeledParametrizedType) entity;
        set.add(pEntity.getBaseType());
        for (ModeledEntity ent : pEntity.getTypeArgs()) {
          add(set, ent);
        }
      } else if (entity.getType() == Entity.ARRAY) {
        ModeledArrayType aEntity = (ModeledArrayType) entity;
        set.add(aEntity.getElementType());
      }
    }
  }
}
