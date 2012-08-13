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

import java.io.Closeable;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import edu.uci.ics.sourcerer.tools.java.db.schema.EntityMetricsTable;
import edu.uci.ics.sourcerer.tools.java.db.schema.FileMetricsTable;
import edu.uci.ics.sourcerer.tools.java.db.schema.ProjectMetricsTable;
import edu.uci.ics.sourcerer.tools.java.model.types.Metric;
import edu.uci.ics.sourcerer.util.Averager;
import edu.uci.ics.sourcerer.utils.db.QueryExecutor;
import edu.uci.ics.sourcerer.utils.db.sql.ConstantCondition;
import edu.uci.ics.sourcerer.utils.db.sql.SelectQuery;
import edu.uci.ics.sourcerer.utils.db.sql.TypedQueryResult;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
class MetricModelFactory implements Closeable {
  private final SelectQuery loadProjectMetrics;
  private final SelectQuery loadFileMetrics;
  private final SelectQuery loadEntityMetrics;
  
  private final ConstantCondition<Integer> pprojectID;
  private final ConstantCondition<Integer> fprojectID;
  private final ConstantCondition<Integer> eprojectID;
  
  MetricModelFactory(QueryExecutor exec) {
    loadProjectMetrics = exec.createSelectQuery(ProjectMetricsTable.TABLE);
    loadProjectMetrics.addSelect(ProjectMetricsTable.METRIC_TYPE, ProjectMetricsTable.SUM, ProjectMetricsTable.MEAN, ProjectMetricsTable.MEDIAN, ProjectMetricsTable.MIN, ProjectMetricsTable.MAX);
    pprojectID = ProjectMetricsTable.PROJECT_ID.compareEquals();
    loadProjectMetrics.andWhere(pprojectID);
    
    loadFileMetrics = exec.createSelectQuery(FileMetricsTable.TABLE);
    loadFileMetrics.addSelect(FileMetricsTable.FILE_ID, FileMetricsTable.METRIC_TYPE, FileMetricsTable.VALUE);
    fprojectID = FileMetricsTable.PROJECT_ID.compareEquals();
    loadFileMetrics.andWhere(fprojectID);
    
    loadEntityMetrics = exec.createSelectQuery(EntityMetricsTable.TABLE);
    loadEntityMetrics.addSelect(EntityMetricsTable.ENTITY_ID, EntityMetricsTable.FILE_ID, EntityMetricsTable.METRIC_TYPE, EntityMetricsTable.VALUE);
    eprojectID = EntityMetricsTable.PROJECT_ID.compareEquals();
    loadEntityMetrics.andWhere(eprojectID);
  }
  
  ProjectMetricModel createModel(Integer projectID) {
    ProjectMetricModel model = new ProjectMetricModel();
    
    // Load the project metrics
    pprojectID.setValue(projectID);
    TypedQueryResult result = loadProjectMetrics.select();
    while (result.next()) {
      model.setValue(result.getResult(ProjectMetricsTable.METRIC_TYPE), result.getResult(ProjectMetricsTable.SUM), result.getResult(ProjectMetricsTable.MEAN), result.getResult(ProjectMetricsTable.MEDIAN), result.getResult(ProjectMetricsTable.MIN), result.getResult(ProjectMetricsTable.MAX));
    }
    
    // Load the file metrics
    fprojectID.setValue(projectID);
    result = loadFileMetrics.select();
    while (result.next()) {
      model.setFileValue(result.getResult(FileMetricsTable.FILE_ID), result.getResult(FileMetricsTable.METRIC_TYPE), result.getResult(FileMetricsTable.VALUE));
    }
    
    // Load the entity metrics
    eprojectID.setValue(projectID);
    result = loadEntityMetrics.select();
    while (result.next()) {
      model.setEntityValue(result.getResult(EntityMetricsTable.ENTITY_ID), result.getResult(EntityMetricsTable.FILE_ID), result.getResult(EntityMetricsTable.METRIC_TYPE), result.getResult(EntityMetricsTable.VALUE));
    }
    
    return model;
  }
  
  public class ProjectMetricValue {
    Double sum;
    Double mean;
    Double median;
    Double min;
    Double max;
    
    ProjectMetricValue(Averager<Double> avg) {
      sum = avg.getSum();
      mean = avg.getMean();
      median = avg.getMean();
      min = avg.getMin();
      max = avg.getMax();
    }
    
    ProjectMetricValue(Double sum, Double mean, Double median, Double min, Double max) {
      this.sum = sum;
      this.mean = mean;
      this.median = median;
      this.max = max;
    }
  }
  
  class ProjectMetricModel {
    private EnumMap<Metric, ProjectMetricValue> metrics;
    private Map<Integer, FileMetricModel> fileMetrics;
    private Map<Integer, EntityMetricModel> entityMetrics;
    
    private ProjectMetricModel() {
      metrics = new EnumMap<>(Metric.class);
      fileMetrics = new HashMap<>();
      entityMetrics = new HashMap<>();
    }
    
    public void setValue(Metric metric, Averager<Double> avg) {
      metrics.put(metric, new ProjectMetricValue(avg));
    }
    
    public void setValue(Metric metric, Double sum, Double mean, Double median, Double min, Double max) {
      metrics.put(metric, new ProjectMetricValue(sum, mean, median, min, max));
    }
    
    public ProjectMetricValue getValue(Metric metric) {
      return metrics.get(metric);
    }
    
    public boolean missingValue(Metric ... metrics) {
      for (Metric metric : metrics) {
        if (!this.metrics.containsKey(metric)) {
          return true;
        }
      }
      return false;
    }
    
    public void setFileValue(Integer fileID, Metric metric, Double value) {
      FileMetricModel model = fileMetrics.get(fileID);
      if (model == null) {
        model = new FileMetricModel();
        fileMetrics.put(fileID, model);
      }
      model.metrics.put(metric, value);
    }
    
    public Double getFileValue(Integer fileID, Metric metric) {
      FileMetricModel model = fileMetrics.get(fileID);
      if (model != null) {
        return model.metrics.get(metric);
      } else {
        return null;
      }
    }
    
    public void setEntityValue(Integer entityID, Integer fileID, Metric metric, Double value) {
      FileMetricModel fModel = null; 
      if (fileID != null) {
        fModel = fileMetrics.get(fileID);
        if (fModel == null) {
          fModel = new FileMetricModel();
          fileMetrics.put(fileID, fModel);
        }
      }
      EntityMetricModel eModel = entityMetrics.get(entityID);
      if (eModel == null) {
        eModel = new EntityMetricModel();
        if (fModel != null) {
          fModel.entityMetrics.put(entityID, eModel);
        }
        entityMetrics.put(entityID, eModel);
      }
      eModel.metrics.put(metric, value);
    }
    
    public Double getEntityValue(Integer entityID, Metric metric) {
      EntityMetricModel model = entityMetrics.get(entityID);
      if (model != null) {
        return model.metrics.get(metric);
      } else {
        return null;
      }
    }
    
    public boolean missingEntityValue(Integer entityID, Metric metric) {
      EntityMetricModel model = entityMetrics.get(entityID);
      if (model != null) {
        return !model.metrics.containsKey(metric);
      } else {
        return true;
      }
    }
  }
  
  class FileMetricModel {
    private EnumMap<Metric, Double> metrics;
    private Map<Integer, EntityMetricModel> entityMetrics;
    
    private FileMetricModel() {
      metrics = new EnumMap<>(Metric.class);
      entityMetrics = new HashMap<>();
    }
  }
  
  class EntityMetricModel {
    private EnumMap<Metric, Double> metrics;
    
    private EntityMetricModel() {
      metrics = new EnumMap<>(Metric.class);
    }
  }
  
  @Override
  public void close() {
    loadEntityMetrics.close();
    loadFileMetrics.close();
    loadEntityMetrics.close();
  }
}
