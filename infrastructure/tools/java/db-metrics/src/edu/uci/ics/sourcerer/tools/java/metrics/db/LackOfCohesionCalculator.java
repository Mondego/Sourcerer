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

import java.util.Collection;
import java.util.Collections;
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
import edu.uci.ics.sourcerer.tools.java.db.type.ModeledField;
import edu.uci.ics.sourcerer.tools.java.db.type.ModeledMethod;
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
public class LackOfCohesionCalculator extends Calculator {

  @Override
  public boolean shouldCalculate(ProjectMetricModel metrics) {
    return metrics.missingValue(Metric.LACK_OF_COHESION_F, Metric.LACK_OF_COHESION_FM);
  }

  @Override
  public void calculate(QueryExecutor exec, Integer projectID, ProjectMetricModel metrics, TypeModel model) {
    try (SelectQuery getFields = exec.createSelectQuery(RelationsTable.TABLE);
         SelectQuery getMethods = exec.createSelectQuery(RelationsTable.TABLE)) {
      ConstantCondition<Integer> lhsEid = RelationsTable.LHS_EID.compareEquals();
      
      getFields.addSelect(RelationsTable.RHS_EID);
      getFields.andWhere(lhsEid, RelationsTable.RELATION_TYPE.compareIn(EnumSet.of(Relation.READS, Relation.WRITES)));
      
      getMethods.addSelect(RelationsTable.RHS_EID);
      getMethods.andWhere(lhsEid, RelationsTable.RELATION_TYPE.compareEquals(Relation.CALLS));
      
      Averager<Double> avgLocf = Averager.create();
      Averager<Double> avgLocfm = Averager.create();
      Averager<Double> avgLocd = Averager.create();
      for (ModeledEntity entity : model.getEntities()) {
        if (entity.getType().is(Entity.CLASS, Entity.ENUM)) {
          ModeledDeclaredType dec = (ModeledDeclaredType) entity;
          
          Map<ModeledEntity, Set<ModeledField>> fieldMap = new HashMap<>();
          Map<ModeledEntity, Set<ModeledMethod>> methodMap = new HashMap<>();
          
          for (ModeledEntity child : dec.getChildren()) {
            if (child.getType().is(Entity.CONSTRUCTOR, Entity.METHOD)) {
              lhsEid.setValue(child.getEntityID());
              
              Set<ModeledField> fieldSet = new HashSet<>();
              TypedQueryResult result = getFields.select();
              while (result.next()) {
                Integer fieldID = result.getResult(RelationsTable.RHS_EID);
                ModeledEntity field = model.get(fieldID);
                if (field != null) {
                  fieldSet.add((ModeledField) field);
                } else {
                  logger.severe("Unable to find field: " + fieldID);
                }
              }
              fieldMap.put(entity, fieldSet);
              
              Set<ModeledMethod> methodSet = new HashSet<>();
              result = getMethods.select();
              while (result.next()) {
                Integer methodID = result.getResult(RelationsTable.RHS_EID);
                ModeledEntity method = model.get(methodID);
                if (method != null) {
                  methodSet.add((ModeledMethod) method);
                } else {
                  logger.severe("Unable to find field: " + methodID);
                }
              }
              methodMap.put(entity, methodSet);
            }
          }
          
          // Compute the cohesion numbers
          Collection<Set<ModeledEntity>> clusters = new LinkedList<>();
          ModeledEntity[] entities = fieldMap.keySet().toArray(new ModeledEntity[fieldMap.size()]);
          int locf = 0;
          int locfm = 0;
          for (int i = 0; i < entities.length; i++) {
            Set<ModeledField> fieldSetA = fieldMap.get(entities[i]);
            Set<ModeledMethod> methodSetA = methodMap.get(entities[i]);
            boolean found = false;
            for (Set<ModeledEntity> cluster : clusters) {
              if (!Collections.disjoint(fieldSetA, cluster) || !Collections.disjoint(methodSetA, cluster)) {
                cluster.addAll(fieldSetA);
                cluster.addAll(methodSetA);
                found = true;
              }
            }
            if (!found) {
              Set<ModeledEntity> cluster = new HashSet<>();
              cluster.addAll(fieldSetA);
              cluster.addAll(methodSetA);
              clusters.add(cluster);
            }
            for (int j = i + 1; j < entities.length; j++) {
              Set<ModeledField> fieldSetB = fieldMap.get(entities[j]);
              Set<ModeledMethod> methodSetB = methodMap.get(entities[j]);
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
          }
          locf = locf < 0 ? 0 : locf;
          locfm = locfm < 0 ? 0 : locfm;
          
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
    }
  }
}
