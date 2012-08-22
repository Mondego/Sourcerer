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

import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
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
import edu.uci.ics.sourcerer.util.UniqueChecker;
import edu.uci.ics.sourcerer.util.UniqueStack;
import edu.uci.ics.sourcerer.util.io.logging.TaskProgressLogger;
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
    return metrics.missingValue(Metric.AFFERENT_COUPLING, Metric.AFFERENT_COUPLING_INTERNAL);
  }

  @Override
  public void calculate(QueryExecutor exec, Integer projectID, ProjectMetricModel metrics, TypeModel model) {
    TaskProgressLogger task = TaskProgressLogger.get();
    
    Pattern anon = Pattern.compile(".*\\$\\d+$"); 
    
    try (SelectQuery select = exec.createSelectQuery(RelationsTable.TABLE)) {
      select.addSelect(RelationsTable.LHS_EID);
      ConstantCondition<Integer> rhsEid = RelationsTable.RHS_EID.compareEquals();
      select.andWhere(rhsEid, RelationsTable.RELATION_TYPE.compareIn(EnumSet.of(Relation.HOLDS, Relation.RETURNS, Relation.READS, Relation.WRITES, Relation.CALLS, Relation.INSTANTIATES, Relation.CASTS, Relation.CHECKS, Relation.USES, Relation.EXTENDS, Relation.IMPLEMENTS)));

      task.start("Computing AfferentCoupling");
      Averager<Double> avgCoupling = Averager.create();
      Averager<Double> avgInternalCoupling = Averager.create();
      Map<ModeledStructuralEntity, Set<ModeledStructuralEntity>> pkgCoupling = new HashMap<>();
      
      task.start("Processing entities", "entities processed", 500);
      for (ModeledEntity entity : model.getEntities()) {
        if (projectID.equals(entity.getProjectID()) && entity.getType().is(Entity.CLASS, Entity.INTERFACE, Entity.ENUM) && !anon.matcher(entity.getFqn()).matches()) {
          ModeledDeclaredType dec = (ModeledDeclaredType) entity;
          
          Set<ModeledStructuralEntity> referencingTypes = new HashSet<>();
          {
            UniqueStack<ModeledStructuralEntity> stack = UniqueStack.create(true);
            stack.push(dec);
            while (stack.hasItems()) {
              ModeledStructuralEntity next = stack.pop();
              // All the referencing things
              rhsEid.setValue(next.getEntityID());
              TypedQueryResult result = select.select();
              while (result.next()) {
                Integer lhsEid = result.getResult(RelationsTable.LHS_EID);
                ModeledEntity ent = model.get(lhsEid);
                if (ent != null) {
                  add(referencingTypes, ent);
                } else {
                  logger.severe("Unable to find model element for: " + lhsEid);
                }
              }
              // Add the children
              stack.pushAll(next.getChildren());
            }
          }
          // Repeat the process for all the parametrized/array types referencing this type
          {
            UniqueStack<ModeledEntity> stack = UniqueStack.create(true);
            for (ModeledEntity ent : model.getEntities()) {
              if (ent.getType() == Entity.PARAMETERIZED_TYPE) {
                ModeledParametrizedType ptype = (ModeledParametrizedType) ent;
                if (entity == ptype.getBaseType()) {
                  stack.push(ptype);
                } else {
                  for (ModeledEntity arg : ptype.getTypeArgs()) {
                    if (entity == arg) {
                      stack.push(ptype);
                      break;
                    }
                  }
                }
              } else if (ent.getType() == Entity.ARRAY) {
                ModeledArrayType atype = (ModeledArrayType) ent;
                if (entity == atype.getElementType()) {
                  stack.push(atype);
                }
              }
            }
            
            while (stack.hasItems()) {
              ModeledEntity next = stack.pop();
              // All the referencing things
              rhsEid.setValue(next.getEntityID());
              TypedQueryResult result = select.select();
              while (result.next()) {
                Integer lhsEid = result.getResult(RelationsTable.LHS_EID);
                ModeledEntity ent = model.get(lhsEid);
                if (ent != null) {
                  add(referencingTypes, ent);
                } else {
                  logger.severe("Unable to find model element for: " + lhsEid);
                }
              }
            }
          }
          // Add to the pkg
          ModeledStructuralEntity pkg = dec.getOwner();
          if (pkg.getType() == Entity.PACKAGE) {
            Set<ModeledStructuralEntity> pkgReferencingTypes = pkgCoupling.get(pkg);
            if (pkgReferencingTypes == null) {
              pkgReferencingTypes = new HashSet<>();
              pkgCoupling.put(pkg, pkgReferencingTypes);
            }
            pkgReferencingTypes.addAll(referencingTypes);
          }
          
          int internalReferencingTypes = 0;
          for (ModeledEntity using : referencingTypes) {
            if (projectID.equals(using.getProjectID())) {
              internalReferencingTypes++;
            }
          }
          
          Double coupling = (double) referencingTypes.size();
          Double internalCoupling = (double) internalReferencingTypes;
          
          if (metrics.missingEntityValue(dec.getEntityID(), Metric.AFFERENT_COUPLING)) {
            metrics.setEntityValue(dec.getEntityID(), dec.getFileID(), Metric.AFFERENT_COUPLING, coupling);
            exec.insert(EntityMetricsTable.createInsert(projectID, dec.getFileID(), dec.getEntityID(), Metric.AFFERENT_COUPLING, coupling));
          }
          avgCoupling.addValue(coupling);
          
          if (metrics.missingEntityValue(dec.getEntityID(), Metric.AFFERENT_COUPLING_INTERNAL)) {
            metrics.setEntityValue(dec.getEntityID(), dec.getFileID(), Metric.AFFERENT_COUPLING_INTERNAL, internalCoupling);
            exec.insert(EntityMetricsTable.createInsert(projectID, dec.getFileID(), dec.getEntityID(), Metric.AFFERENT_COUPLING_INTERNAL, internalCoupling));
          }
          avgInternalCoupling.addValue(internalCoupling);
          
          task.progress();
        }
      }
      task.finish();

      if (metrics.missingValue(Metric.AFFERENT_COUPLING)) {
        metrics.setValue(Metric.AFFERENT_COUPLING, avgCoupling);
        exec.insert(ProjectMetricsTable.createInsert(projectID, Metric.AFFERENT_COUPLING, avgCoupling));
      }
      if (metrics.missingValue(Metric.AFFERENT_COUPLING_INTERNAL)) {
        metrics.setValue(Metric.AFFERENT_COUPLING_INTERNAL, avgInternalCoupling);
        exec.insert(ProjectMetricsTable.createInsert(projectID, Metric.AFFERENT_COUPLING_INTERNAL, avgInternalCoupling));
      }
      
      for (Map.Entry<ModeledStructuralEntity, Set<ModeledStructuralEntity>> entry : pkgCoupling.entrySet()) {
        ModeledStructuralEntity pkg = entry.getKey();
        int internalReferencingTypes = 0;
        for (ModeledEntity referencingType : entry.getValue()) {
          if (projectID.equals(referencingType.getEntityID())) {
            internalReferencingTypes++;
          }
        }
        
        Double coupling = (double) entry.getValue().size();
        Double internalCoupling = (double) internalReferencingTypes;
        
        if (metrics.missingEntityValue(pkg.getEntityID(), Metric.AFFERENT_COUPLING)) {
          metrics.setEntityValue(pkg.getEntityID(), null, Metric.AFFERENT_COUPLING, coupling);
          exec.insert(EntityMetricsTable.createInsert(projectID, null, pkg.getEntityID(), Metric.AFFERENT_COUPLING, coupling));
        }
        if (metrics.missingEntityValue(pkg.getEntityID(), Metric.AFFERENT_COUPLING_INTERNAL)) {
          metrics.setEntityValue(pkg.getEntityID(), null, Metric.AFFERENT_COUPLING_INTERNAL, internalCoupling);
          exec.insert(EntityMetricsTable.createInsert(projectID, null, pkg.getEntityID(), Metric.AFFERENT_COUPLING_INTERNAL, internalCoupling));
        }
      }
      task.finish();
    }
  }
  
  private void add(Set<ModeledStructuralEntity> set, ModeledEntity entity) {
    if (entity != null) {
      if (entity instanceof ModeledStructuralEntity) {
        ModeledStructuralEntity struct = (ModeledStructuralEntity) entity;
        if (struct.getType().is(Entity.CLASS, Entity.ENUM, Entity.INTERFACE, Entity.ANNOTATION)) {
          set.add((ModeledDeclaredType) struct);
        } else {
          UniqueChecker<ModeledEntity> checker = UniqueChecker.create(true);
          while (!struct.getType().is(Entity.CLASS, Entity.ENUM, Entity.INTERFACE, Entity.ANNOTATION)) {
            if (checker.isUnique(struct)) {
              struct = struct.getOwner();
            } else {
              return;
            }
          }
          add(set, (ModeledDeclaredType) struct);
        } 
      } else {
        logger.severe("Unexpected entity type: " + entity);
      }
    }
  }
}
