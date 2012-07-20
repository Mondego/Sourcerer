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
import java.util.Map;

import edu.uci.ics.sourcerer.tools.java.db.schema.EntityMetricsTable;
import edu.uci.ics.sourcerer.tools.java.db.schema.FileMetricsTable;
import edu.uci.ics.sourcerer.tools.java.db.schema.ProjectMetricsTable;
import edu.uci.ics.sourcerer.tools.java.model.types.Metric;
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
    loadProjectMetrics.addSelect(ProjectMetricsTable.METRIC_TYPE, ProjectMetricsTable.VALUE);
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
      model.setValue(result.getResult(ProjectMetricsTable.METRIC_TYPE), result.getResult(ProjectMetricsTable.VALUE));
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
  
  class ProjectMetricModel {
    private EnumMap<Metric, Double> metrics;
    private Map<Integer, FileMetricModel> fileMetrics;
    private Map<Integer, EntityMetricModel> entityMetrics;
    
    private ProjectMetricModel() {}
    
    public void setValue(Metric metric, Double value) {
      metrics.put(metric, value);
    }
    
    public Double getValue(Metric metric) {
      return metrics.get(metric);
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
        return metrics.get(metric);
      } else {
        return null;
      }
    }
    
    public void setEntityValue(Integer entityID, Integer fileID, Metric metric, Double value) {
      FileMetricModel fModel = fileMetrics.get(fileID);
      if (fModel == null) {
        fModel = new FileMetricModel();
        fileMetrics.put(fileID, fModel);
      }
      EntityMetricModel eModel = fModel.entityMetrics.get(entityID);
      if (eModel == null) {
        eModel = new EntityMetricModel();
        fModel.entityMetrics.put(entityID, eModel);
        entityMetrics.put(entityID, eModel);
      }
      eModel.metrics.put(metric, value);
    }
    
    public Double getEntityValue(Integer entityID, Metric metric) {
      EntityMetricModel model = entityMetrics.get(entityID);
      if (model != null) {
        return metrics.get(metric);
      } else {
        return null;
      }
    }
  }
  
  class FileMetricModel {
    private EnumMap<Metric, Double> metrics;
    private Map<Integer, EntityMetricModel> entityMetrics;
    
    private FileMetricModel() {}
  }
  
  class EntityMetricModel {
    private EnumMap<Metric, Double> metrics;
    
    private EntityMetricModel() {}
  }
  
  @Override
  public void close() {
    loadEntityMetrics.close();
    loadFileMetrics.close();
    loadEntityMetrics.close();
  }
}
