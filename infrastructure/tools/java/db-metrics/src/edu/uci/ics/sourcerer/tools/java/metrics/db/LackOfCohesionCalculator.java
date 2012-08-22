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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
import edu.uci.ics.sourcerer.tools.java.db.type.ModeledDuplicate;
import edu.uci.ics.sourcerer.tools.java.db.type.ModeledEntity;
import edu.uci.ics.sourcerer.tools.java.db.type.ModeledMethod;
import edu.uci.ics.sourcerer.tools.java.db.type.ModeledStructuralEntity;
import edu.uci.ics.sourcerer.tools.java.db.type.TypeModel;
import edu.uci.ics.sourcerer.tools.java.metrics.db.MetricModelFactory.ProjectMetricModel;
import edu.uci.ics.sourcerer.tools.java.model.types.Entity;
import edu.uci.ics.sourcerer.tools.java.model.types.Metric;
import edu.uci.ics.sourcerer.tools.java.model.types.Relation;
import edu.uci.ics.sourcerer.tools.java.model.types.RelationClass;
import edu.uci.ics.sourcerer.util.Averager;
import edu.uci.ics.sourcerer.util.UniqueStack;
import edu.uci.ics.sourcerer.util.io.logging.TaskProgressLogger;
import edu.uci.ics.sourcerer.utils.db.QueryExecutor;
import edu.uci.ics.sourcerer.utils.db.sql.SelectQuery;
import edu.uci.ics.sourcerer.utils.db.sql.TypedQueryResult;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class LackOfCohesionCalculator extends Calculator {
  @Override
  public boolean shouldCalculate(ProjectMetricModel metrics) {
    return metrics.missingValue(Metric.LACK_OF_COHESION_F, Metric.LACK_OF_COHESION_FM);
  }

  @Override
  public void calculate(QueryExecutor exec, Integer projectID, ProjectMetricModel metrics, TypeModel model) {
    TaskProgressLogger task = TaskProgressLogger.get();
    
    Pattern anon = Pattern.compile(".*\\$\\d+$");
    
    task.start("Computing lack of cohesion");
    Map<ModeledStructuralEntity, Set<ModeledStructuralEntity>> fieldMap = new HashMap<>();
    Map<ModeledStructuralEntity, Set<ModeledMethod>> methodMap = new HashMap<>();
    
    try (SelectQuery select = exec.createSelectQuery(RelationsTable.TABLE)) {
      select.addSelect(RelationsTable.LHS_EID, RelationsTable.RHS_EID);
      select.andWhere(RelationsTable.PROJECT_ID.compareEquals(projectID), RelationsTable.RELATION_CLASS.compareEquals(RelationClass.INTERNAL), RelationsTable.RELATION_TYPE.compareIn(EnumSet.of(Relation.READS, Relation.WRITES, Relation.CALLS)));
      
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
        UniqueStack<ModeledEntity> stack = UniqueStack.create(false);
        stack.push(lhs);
        while (stack.hasItems()) {
          ModeledEntity next = stack.pop();
          if (next instanceof ModeledStructuralEntity) {
            ModeledStructuralEntity struct = (ModeledStructuralEntity) next;
            if (next.getType().is(Entity.CONSTRUCTOR, Entity.METHOD)) {
              add(fieldMap, methodMap, struct, rhs); 
            } 
            if (next.getType() != Entity.PACKAGE) {
              stack.push(struct.getOwner());
            }
          }
        }
        task.progress();
      }
      task.finish();
    }
    
    Averager<Double> avgLocf = Averager.create();
    Averager<Double> avgLocfm = Averager.create();
    Averager<Double> avgLocd = Averager.create();
    
    task.start("Computing and adding the metrics");
    for (ModeledEntity entity : model.getEntities()) {
      if (projectID.equals(entity.getProjectID()) && entity.getType() == Entity.CLASS && !anon.matcher(entity.getFqn()).matches()) {
        ModeledDeclaredType dec = (ModeledDeclaredType) entity;
        
        ArrayList<ModeledStructuralEntity> children = new ArrayList<>();
        for (ModeledEntity child : dec.getChildren()) {
          if (child.getType().is(Entity.CONSTRUCTOR, Entity.METHOD)) {
            children.add((ModeledStructuralEntity) child);
          }
        }
        Collection<Set<ModeledStructuralEntity>> clusters = new LinkedList<>();
        int locf = 0;
        int locfm = 0;
        for (int i = 0; i < children.size(); i++) {
          Set<ModeledStructuralEntity> fieldSetA = fieldMap.get(children.get(i));
          if (fieldSetA == null) {
            fieldSetA = Collections.emptySet();
          }
          Set<ModeledMethod> methodSetA = methodMap.get(children.get(i));
          if (methodSetA== null) {
            methodSetA = Collections.emptySet();
          }
          
          for (int j = i + 1; j < children.size(); j++) {
            Set<ModeledStructuralEntity> fieldSetB = fieldMap.get(children.get(j));
            if (fieldSetB == null) {
              fieldSetB = Collections.emptySet();
            }
            Set<ModeledMethod> methodSetB = methodMap.get(children.get(j));
            if (methodSetB == null) {
              methodSetB = Collections.emptySet();
            }
            if (Collections.disjoint(fieldSetA, fieldSetB)) {
              locf++;
              if (Collections.disjoint(methodSetA, methodSetB)) {
                locfm++;
              } else {
                locfm--;
              }
            } else {
              locf--;
              locfm--;
            }
          }
          locf = locf < 0 ? 0 : locf;
          locfm = locfm < 0 ? 0 : locfm;
          
          boolean found = false;
          for (Set<ModeledStructuralEntity> cluster : clusters) {
            if (!Collections.disjoint(fieldSetA, cluster) || !Collections.disjoint(methodSetA, cluster)) {
              cluster.addAll(fieldSetA);
              cluster.addAll(methodSetA);
              found = true;
            }
          }
          if (!found) {
            Set<ModeledStructuralEntity> cluster = new HashSet<>();
            cluster.addAll(fieldSetA);
            cluster.addAll(methodSetA);
            clusters.add(cluster);
          }
        }
        
        double value = (double) clusters.size();
        if (metrics.missingEntityValue(dec.getEntityID(), Metric.LACK_OF_COHESION_D)) {
          metrics.setEntityValue(dec.getEntityID(), dec.getFileID(), Metric.LACK_OF_COHESION_D, value);
          exec.insert(EntityMetricsTable.createInsert(dec.getProjectID(), dec.getFileID(), dec.getEntityID(), Metric.LACK_OF_COHESION_D, value));
        }
        avgLocd.addValue(value);
        
        value = (double) locf;
        if (metrics.missingEntityValue(dec.getEntityID(), Metric.LACK_OF_COHESION_F)) {
          metrics.setEntityValue(dec.getEntityID(), dec.getFileID(), Metric.LACK_OF_COHESION_F, value);
          exec.insert(EntityMetricsTable.createInsert(dec.getProjectID(), dec.getFileID(), dec.getEntityID(), Metric.LACK_OF_COHESION_F, value));
        }
        avgLocf.addValue(value);
        
        value = (double) locfm;
        if (metrics.missingEntityValue(dec.getEntityID(), Metric.LACK_OF_COHESION_FM)) {
          metrics.setEntityValue(dec.getEntityID(), dec.getFileID(), Metric.LACK_OF_COHESION_FM, value);
          exec.insert(EntityMetricsTable.createInsert(dec.getProjectID(), dec.getFileID(), dec.getEntityID(), Metric.LACK_OF_COHESION_FM, value));
        }
        avgLocfm.addValue(value);
      }
    }
    
    if (metrics.missingValue(Metric.LACK_OF_COHESION_D)) {
      metrics.setValue(Metric.LACK_OF_COHESION_D, avgLocd);
      exec.insert(ProjectMetricsTable.createInsert(projectID, Metric.LACK_OF_COHESION_D, avgLocd));
    }
    if (metrics.missingValue(Metric.LACK_OF_COHESION_F)) {
      metrics.setValue(Metric.LACK_OF_COHESION_F, avgLocf);
      exec.insert(ProjectMetricsTable.createInsert(projectID, Metric.LACK_OF_COHESION_F, avgLocf));
    }
    if (metrics.missingValue(Metric.LACK_OF_COHESION_FM)) {
      metrics.setValue(Metric.LACK_OF_COHESION_FM, avgLocfm);
      exec.insert(ProjectMetricsTable.createInsert(projectID, Metric.LACK_OF_COHESION_FM, avgLocfm));
    }
    task.finish();
    
    task.finish();
  }
  
  private void add(Map<ModeledStructuralEntity, Set<ModeledStructuralEntity>> fieldMap, Map<ModeledStructuralEntity, Set<ModeledMethod>> methodMap, ModeledStructuralEntity entity, ModeledEntity target) {
    if (target.getType() == Entity.FIELD) {
      Set<ModeledStructuralEntity> fields = fieldMap.get(entity);
      if (fields == null) {
        fields = new HashSet<>();
        fieldMap.put(entity, fields);
      }
      // match the owner to the field
      ModeledStructuralEntity field = (ModeledStructuralEntity) target;
      if (entity.getOwner() == field.getOwner()) {
        fields.add(field);
      }
    } else if (target.getType() == Entity.METHOD) {
      Set<ModeledMethod> methods = methodMap.get(entity);
      if (methods == null) {
        methods = new HashSet<>();
        methodMap.put(entity, methods);
      }
      // match the owner to the method
      ModeledMethod method = (ModeledMethod) target;
      if (entity.getOwner() == method.getOwner()) {
        methods.add(method);
      }
    } else if (target.getType().is(Entity.CONSTRUCTOR, Entity.ENUM_CONSTANT, Entity.UNKNOWN)) {
      // Ignore
    } else if (target.getType().is(Entity.DUPLICATE, Entity.VIRTUAL_DUPLICATE)) {
      ModeledDuplicate dup = (ModeledDuplicate) target;
      for (ModeledEntity ent : dup.getMatches()) {
        add(fieldMap, methodMap, entity, ent);
      }
    } else {
      logger.severe("Unexpected entity type: " + target);
    }
  }
}
