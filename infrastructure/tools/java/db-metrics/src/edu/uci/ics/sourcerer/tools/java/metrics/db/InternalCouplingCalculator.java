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
import edu.uci.ics.sourcerer.tools.java.db.type.ModeledDuplicate;
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
import edu.uci.ics.sourcerer.utils.db.sql.SelectQuery;
import edu.uci.ics.sourcerer.utils.db.sql.TypedQueryResult;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class InternalCouplingCalculator extends Calculator {
  @Override
  public boolean shouldCalculate(ProjectMetricModel metrics) {
    return metrics.missingValue(Metric.EFFERENT_COUPLING_INTERNAL, Metric.AFFERENT_COUPLING_INTERNAL);
  }

  @Override
  public void calculate(QueryExecutor exec, Integer projectID, ProjectMetricModel metrics, TypeModel model) {
    TaskProgressLogger task = TaskProgressLogger.get();
    
    Pattern anon = Pattern.compile(".*\\$\\d+$"); 

    task.start("Computing internal coupling");
    Map<ModeledDeclaredType, Set<ModeledDeclaredType>> affMap = new HashMap<>();
    Map<ModeledStructuralEntity, Set<ModeledDeclaredType>> pkgAffMap = new HashMap<>();
    
    Map<ModeledDeclaredType, Set<ModeledDeclaredType>> effMap = new HashMap<>();
    Map<ModeledStructuralEntity, Set<ModeledDeclaredType>> pkgEffMap = new HashMap<>();

    
    try (SelectQuery select = exec.createSelectQuery(RelationsTable.TABLE)) {
      select.addSelect(RelationsTable.LHS_EID, RelationsTable.RHS_EID);
      select.andWhere(RelationsTable.PROJECT_ID.compareEquals(projectID), RelationsTable.RELATION_TYPE.compareIn(EnumSet.of(Relation.HOLDS, Relation.RETURNS, Relation.READS, Relation.WRITES, Relation.CALLS, Relation.INSTANTIATES, Relation.CASTS, Relation.CHECKS, Relation.USES, Relation.EXTENDS, Relation.IMPLEMENTS)));
      
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
        // Handle the Efferent Coupling
        if (projectID.equals(rhs.getProjectID())) {
          UniqueStack<ModeledEntity> stack = UniqueStack.create(false);
          stack.push(lhs);
          while (stack.hasItems()) {
            ModeledEntity next = stack.pop();
            if (next instanceof ModeledStructuralEntity) {
              if (next.getType() == Entity.PACKAGE) {
                ModeledStructuralEntity pkg = (ModeledStructuralEntity) next;
                Set<ModeledDeclaredType> referencedTypes = pkgEffMap.get(pkg);
                if (referencedTypes == null) {
                  referencedTypes = new HashSet<>();
                  pkgEffMap.put(pkg, referencedTypes);
                }
                addEff(referencedTypes, projectID, rhs);  
              } else if (next.getType().is(Entity.CLASS, Entity.INTERFACE, Entity.ENUM) && !anon.matcher(next.getFqn()).matches()) {
                ModeledDeclaredType dec = (ModeledDeclaredType) next;
                Set<ModeledDeclaredType> referencedTypes = effMap.get(dec);
                if (referencedTypes == null) {
                  referencedTypes = new HashSet<>();
                  effMap.put(dec, referencedTypes);
                }
                addEff(referencedTypes, projectID, rhs);
                stack.push(dec.getOwner());
              } else {
                stack.push(((ModeledStructuralEntity) next).getOwner());
              }
            } else {
              logger.severe("Unexpected rhs: " + next);
            }
          }
        }
        // Handle the Afferent Coupling
        if (projectID.equals(rhs.getProjectID())) {
          // Add to the maps
          UniqueStack<ModeledEntity> stack = UniqueStack.create(false);
          stack.push(rhs);
          while (stack.hasItems()) {
            ModeledEntity next = stack.pop();
            if (projectID.equals(next.getProjectID())) {
              if (next.getType() == Entity.PACKAGE) {
                ModeledStructuralEntity pkg = (ModeledStructuralEntity) next;
                Set<ModeledDeclaredType> referencingTypes = pkgAffMap.get(pkg);
                if (referencingTypes == null) {
                  referencingTypes = new HashSet<>();
                  pkgAffMap.put(pkg, referencingTypes);
                }
                addAff(referencingTypes, lhs);
              } else if (next.getType() == Entity.PARAMETERIZED_TYPE) {
                ModeledParametrizedType pType = (ModeledParametrizedType) next;
                stack.push(pType.getBaseType());
                stack.pushAll(pType.getTypeArgs());
              } else if (next.getType() == Entity.ARRAY) {
                ModeledArrayType aType = (ModeledArrayType) next;
                stack.push(aType.getElementType());
              } else if (next.getType().is(Entity.CLASS, Entity.INTERFACE, Entity.ENUM)) {
                ModeledDeclaredType dec = (ModeledDeclaredType) next;
                if (!anon.matcher(next.getFqn()).matches()) {
                  Set<ModeledDeclaredType> referencingTypes = affMap.get(dec);
                  if (referencingTypes == null) {
                    referencingTypes = new HashSet<>();
                    affMap.put(dec, referencingTypes);
                  }
                  addAff(referencingTypes, lhs);
                }
                stack.push(dec.getOwner());
              } else if (next.getType().is(Entity.ANNOTATION, Entity.ANNOTATION_ELEMENT)) {
                // ignore
              } else if (next.getType().is(Entity.METHOD, Entity.CONSTRUCTOR, Entity.FIELD, Entity.ENUM_CONSTANT, Entity.INITIALIZER)) {
                stack.push(((ModeledStructuralEntity) next).getOwner());
              } else if (next.getType().is(Entity.TYPE_VARIABLE, Entity.WILDCARD)) {
                // ignore, but in theory should look at the bounds
              } else if (next.getType().is(Entity.DUPLICATE, Entity.VIRTUAL_DUPLICATE)) {
                ModeledDuplicate dup = (ModeledDuplicate) next;
                stack.pushAll(dup.getMatches());
              } else {
                logger.severe("Unexpected rhs: " + next);
              }
            }
          }
        }
        task.progress();
      }
      task.finish();
    }
    
    task.start("Adding the metrics");
    Averager<Double> avgInternalEffCoupling = Averager.create();
    Averager<Double> avgInternalAffCoupling = Averager.create();
    // Add the eff entity metrics
    for (Map.Entry<ModeledDeclaredType, Set<ModeledDeclaredType>> entry : effMap.entrySet()) {
      ModeledDeclaredType entity = entry.getKey();
      Double internalCoupling = (double) entry.getValue().size();
      
      if (metrics.missingEntityValue(entity.getEntityID(), Metric.EFFERENT_COUPLING_INTERNAL)) {
        metrics.setEntityValue(entity.getEntityID(), entity.getFileID(), Metric.EFFERENT_COUPLING_INTERNAL, internalCoupling);
        exec.insert(EntityMetricsTable.createInsert(projectID, entity.getFileID(), entity.getEntityID(), Metric.EFFERENT_COUPLING_INTERNAL, internalCoupling));
      }
      avgInternalEffCoupling.addValue(internalCoupling);
    }
    // Add the aff entity metrics
    for (Map.Entry<ModeledDeclaredType, Set<ModeledDeclaredType>> entry : affMap.entrySet()) {
      ModeledDeclaredType entity = entry.getKey();
      Double internalCoupling = (double) entry.getValue().size();
      
      if (metrics.missingEntityValue(entity.getEntityID(), Metric.AFFERENT_COUPLING_INTERNAL)) {
        metrics.setEntityValue(entity.getEntityID(), entity.getFileID(), Metric.AFFERENT_COUPLING_INTERNAL, internalCoupling);
        exec.insert(EntityMetricsTable.createInsert(projectID, entity.getFileID(), entity.getEntityID(), Metric.AFFERENT_COUPLING_INTERNAL, internalCoupling));
      }
      avgInternalAffCoupling.addValue(internalCoupling);
    }

    // Add the eff pkg metrics
    for (Map.Entry<ModeledStructuralEntity, Set<ModeledDeclaredType>> entry : pkgEffMap.entrySet()) {
      ModeledStructuralEntity pkg = entry.getKey();
      Double internalCoupling = (double) entry.getValue().size();
      
      if (metrics.missingEntityValue(pkg.getEntityID(), Metric.EFFERENT_COUPLING_INTERNAL)) {
        metrics.setEntityValue(pkg.getEntityID(), null, Metric.EFFERENT_COUPLING_INTERNAL, internalCoupling);
        exec.insert(EntityMetricsTable.createInsert(projectID, null, pkg.getEntityID(), Metric.EFFERENT_COUPLING_INTERNAL, internalCoupling));
      }
    }
    
    // Add the aff pkg metrics
    for (Map.Entry<ModeledStructuralEntity, Set<ModeledDeclaredType>> entry : pkgAffMap.entrySet()) {
      ModeledStructuralEntity pkg = entry.getKey();
      Double internalCoupling = (double) entry.getValue().size();
      
      if (metrics.missingEntityValue(pkg.getEntityID(), Metric.AFFERENT_COUPLING_INTERNAL)) {
        metrics.setEntityValue(pkg.getEntityID(), null, Metric.AFFERENT_COUPLING_INTERNAL, internalCoupling);
        exec.insert(EntityMetricsTable.createInsert(projectID, null, pkg.getEntityID(), Metric.AFFERENT_COUPLING_INTERNAL, internalCoupling));
      }
    }

    // Add the aff project metric
    if (metrics.missingValue(Metric.EFFERENT_COUPLING_INTERNAL)) {
      metrics.setValue(Metric.EFFERENT_COUPLING_INTERNAL, avgInternalEffCoupling);
      exec.insert(ProjectMetricsTable.createInsert(projectID, Metric.EFFERENT_COUPLING_INTERNAL, avgInternalEffCoupling));
    }

    // Add the aff project metric
    if (metrics.missingValue(Metric.AFFERENT_COUPLING_INTERNAL)) {
      metrics.setValue(Metric.AFFERENT_COUPLING_INTERNAL, avgInternalAffCoupling);
      exec.insert(ProjectMetricsTable.createInsert(projectID, Metric.AFFERENT_COUPLING_INTERNAL, avgInternalAffCoupling));
    }
    task.finish();

    task.finish();
  }
  
  private void addEff(Set<ModeledDeclaredType> set, Integer projectID, ModeledEntity entity) {
    if (entity != null && projectID.equals(entity.getProjectID())) {
      if (entity instanceof ModeledStructuralEntity) {
        if (entity.getType().is(Entity.CLASS, Entity.INTERFACE, Entity.ENUM, Entity.ANNOTATION)) {
          set.add((ModeledDeclaredType) entity);
        } else {
          ModeledStructuralEntity struct = (ModeledStructuralEntity) entity;
          UniqueChecker<ModeledEntity> checker = UniqueChecker.create(true);
          while (!struct.getType().is(Entity.CLASS, Entity.INTERFACE, Entity.ENUM, Entity.ANNOTATION)) {
            if (checker.isUnique(struct)) {
              struct = struct.getOwner();
              if (struct == null) {
                logger.severe("Cannot find declared type parent: " + entity);
                return;
              }
            } else {
              logger.severe("Cycle in structured type parent: " + entity);
              return;
            }
          }
          set.add((ModeledDeclaredType) struct);
        }
      } else if (entity.getType() == Entity.PARAMETERIZED_TYPE) {
        ModeledParametrizedType pEntity = (ModeledParametrizedType) entity;
        set.add(pEntity.getBaseType());
        for (ModeledEntity ent : pEntity.getTypeArgs()) {
          addEff(set, projectID, ent);
        }
      } else if (entity.getType() == Entity.ARRAY) {
        ModeledArrayType aEnttiy = (ModeledArrayType) entity;
        addEff(set, projectID, aEnttiy.getElementType());
      } else if (entity.getType().is(Entity.TYPE_VARIABLE, Entity.WILDCARD)) {
        // Ignore, but probably should do something
      } else if (entity.getType().is(Entity.DUPLICATE, Entity.VIRTUAL_DUPLICATE)) {
        ModeledDuplicate dup = (ModeledDuplicate) entity;
        for (ModeledEntity ent : dup.getMatches()) {
          addEff(set, projectID, ent);
        }
      } else {
        logger.severe("Unexpected entity type: " + entity);
      }
    }
  }
  
  private void addAff(Set<ModeledDeclaredType> set, ModeledEntity entity) {
    if (entity != null) {
      if (entity instanceof ModeledStructuralEntity) {
        ModeledStructuralEntity struct = (ModeledStructuralEntity) entity;
        if (struct.getType() == Entity.PACKAGE) {
          // ignore
        } else if (struct.getType().is(Entity.CLASS, Entity.ENUM, Entity.INTERFACE, Entity.ANNOTATION)) {
          set.add((ModeledDeclaredType) struct);
        } else {
          UniqueChecker<ModeledEntity> checker = UniqueChecker.create(true);
          while (!struct.getType().is(Entity.CLASS, Entity.ENUM, Entity.INTERFACE, Entity.ANNOTATION)) {
            if (checker.isUnique(struct)) {
              struct = struct.getOwner();
              if (struct == null) {
                logger.severe("Cannot find declared type parent: " + entity);
                return;
              }
            } else {
              logger.severe("Cycle in structured type parent: " + entity);
              return;
            }
          }
          set.add((ModeledDeclaredType) struct);
        } 
      } else {
        logger.severe("Unexpected entity type: " + entity);
      }
    }
  }
}
