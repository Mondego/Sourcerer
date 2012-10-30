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
package edu.uci.ics.sourcerer.tools.java.db.importer;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import edu.uci.ics.sourcerer.tools.java.db.schema.EntitiesTable;
import edu.uci.ics.sourcerer.tools.java.db.schema.EntityMetricsTable;
import edu.uci.ics.sourcerer.tools.java.db.schema.FileMetricsTable;
import edu.uci.ics.sourcerer.tools.java.db.schema.FilesTable;
import edu.uci.ics.sourcerer.tools.java.db.schema.ProjectsTable;
import edu.uci.ics.sourcerer.tools.java.model.extracted.EntityEX;
import edu.uci.ics.sourcerer.tools.java.model.extracted.FileEX;
import edu.uci.ics.sourcerer.tools.java.model.extracted.io.ReaderBundle;
import edu.uci.ics.sourcerer.tools.java.model.types.File;
import edu.uci.ics.sourcerer.tools.java.model.types.Location;
import edu.uci.ics.sourcerer.tools.java.model.types.Metric;
import edu.uci.ics.sourcerer.tools.java.model.types.Metrics;
import edu.uci.ics.sourcerer.tools.java.repo.model.extracted.ExtractedJarFile;
import edu.uci.ics.sourcerer.util.Nullerator;
import edu.uci.ics.sourcerer.utils.db.BatchInserter;
import edu.uci.ics.sourcerer.utils.db.sql.ConstantCondition;
import edu.uci.ics.sourcerer.utils.db.sql.SelectQuery;
import edu.uci.ics.sourcerer.utils.db.sql.TypedQueryResult;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class BytecodeMetricsImporter extends DatabaseImporter {
  private Nullerator<ExtractedJarFile> jars;
  
  protected BytecodeMetricsImporter(Nullerator<ExtractedJarFile> jars) {
    super("Adding bytecode metrics");
    this.jars = jars;
  }

  @Override
  protected void doImport() {
    try (SelectQuery projectQuery = exec.createSelectQuery(ProjectsTable.TABLE);
         SelectQuery filesQuery = exec.createSelectQuery(FilesTable.TABLE);
         SelectQuery fileMetricsQuery = exec.createSelectQuery(FileMetricsTable.TABLE);
         SelectQuery entityQuery = exec.createSelectQuery(EntitiesTable.TABLE);
         SelectQuery methodQuery = exec.createSelectQuery(EntitiesTable.TABLE);
         SelectQuery entityMetricsQuery = exec.createSelectQuery(EntityMetricsTable.TABLE)) {
      projectQuery.addSelect(ProjectsTable.PROJECT_ID);
      ConstantCondition<String> equalsHash = ProjectsTable.HASH.compareEquals();
      projectQuery.andWhere(equalsHash);
      
      filesQuery.addSelect(FilesTable.FILE_ID, FilesTable.PATH);
      ConstantCondition<Integer> equalsProjectID = FilesTable.PROJECT_ID.compareEquals();
      filesQuery.andWhere(FilesTable.FILE_TYPE.compareEquals(File.SOURCE), equalsProjectID);
      
      fileMetricsQuery.addSelect(FileMetricsTable.METRIC_TYPE);
      ConstantCondition<Integer> equalsFileID1 = FileMetricsTable.FILE_ID.compareEquals();
      fileMetricsQuery.andWhere(equalsFileID1);
      
      entityQuery.addSelect(EntitiesTable.ENTITY_ID);
      ConstantCondition<Integer> equalsFileID2 = EntitiesTable.FILE_ID.compareEquals();
      ConstantCondition<String> equalsFqn = EntitiesTable.FQN.compareEquals();
      entityQuery.andWhere(equalsFqn, equalsFileID2, EntitiesTable.PARAMS.compareNull());
      
      methodQuery.addSelect(EntitiesTable.ENTITY_ID);
      ConstantCondition<String> equalsParams = EntitiesTable.PARAMS.compareEquals();
      methodQuery.andWhere(equalsFqn, equalsParams, equalsFileID2);
      
      entityMetricsQuery.addSelect(EntityMetricsTable.METRIC_TYPE);
      ConstantCondition<Integer> equalsEntityID = EntityMetricsTable.ENTITY_ID.compareEquals();
      entityMetricsQuery.andWhere(equalsEntityID);
      
      ExtractedJarFile jar = null;
      while ((jar = jars.next()) != null) {
        String name = jar.getProperties().NAME.getValue();
        task.start("Adding " + name + "'s bytecode metrics");
        
        Integer projectID = null;
        
        task.start("Loading project");
        equalsHash.setValue(jar.getProperties().HASH.getValue());
        projectID = projectQuery.select().toSingleton(ProjectsTable.PROJECT_ID, true);
        task.finish();
        
        if (projectID == null) {
          task.report("Unable to locate project for: " + jar.getProperties().HASH.getValue());
        } else {
          Map<String, Integer> fileMap = new HashMap<>();
          {
            task.start("Loading source files", "files loaded");
            equalsProjectID.setValue(projectID);
            TypedQueryResult result = filesQuery.select();
            while (result.next()) {
              Integer fileID = result.getResult(FilesTable.FILE_ID);
              String path = result.getResult(FilesTable.PATH);
              fileMap.put(path, fileID);
              task.progress();
            }
            task.finish();
          }
          
          if (fileMap.size() > 0) {
            BatchInserter inserter = exec.makeInFileInserter(tempDir, FileMetricsTable.TABLE);
            ReaderBundle reader = ReaderBundle.create(jar.getExtractionDir().toFile(), jar.getCompressedFile().toFile());
            
            task.start("Adding bytecode metrics for " + fileMap.size() + " class files", "files processed");
            EnumSet<Metric> metrics = EnumSet.noneOf(Metric.class);
            for (FileEX file : reader.getTransientFiles()) {
              Integer fileID = fileMap.get(file.getPath());
              if (fileID != null) {
                metrics.clear();
                equalsFileID1.setValue(fileID);
                TypedQueryResult result = fileMetricsQuery.select();
                while (result.next()) {
                  metrics.add(result.getResult(FileMetricsTable.METRIC_TYPE));
                }
                Metrics mm = file.getMetrics();
                if (mm != null) {
                  for (Map.Entry<Metric, Double> entry : mm.getMetricValues()) {
                    if (!metrics.contains(entry.getKey())) {
                      inserter.addInsert(FileMetricsTable.createInsert(projectID, fileID, entry.getKey(), entry.getValue()));
                    }
                  }
                }
              }
              task.progress();
            }
            inserter.insert();
            task.finish();
            
            inserter = exec.makeInFileInserter(tempDir, EntityMetricsTable.TABLE);
            task.start("Adding bytecode metrics for entities", "entities processed");
            for (EntityEX entity : reader.getTransientEntities()) {
              Location loc = entity.getLocation();
              if (loc != null) {
                String path = loc.getClassFile();
                Integer fileID = fileMap.get(path);
                if (fileID != null) {
                  equalsFileID2.setValue(fileID);
                  equalsFqn.setValue(entity.getFqn());
                  TypedQueryResult result = null;
                  if (entity.getSignature() == null) {
                    result = entityQuery.select();
                  } else {
                    equalsParams.setValue(entity.getSignature());
                    result = methodQuery.select();
                  }
                  Integer entityID = null;
                  if (result.next()) {
                    entityID = result.getResult(EntitiesTable.ENTITY_ID);
                    if (result.next()) {
                      task.report(Level.SEVERE, "Duplicate entity: " + entity);
                      result.close();
                    }
                  }
                  if (entityID == null) {
                    task.report(Level.SEVERE, "Unable to find entity: " + entity);
                  } else {
                    metrics.clear();
                    equalsEntityID.setValue(entityID);
                    result = entityMetricsQuery.select();
                    while (result.next()) {
                      metrics.add(result.getResult(EntityMetricsTable.METRIC_TYPE));
                    }
                    Metrics mm = entity.getMetrics();
                    if (mm != null) {
                      for (Map.Entry<Metric, Double> entry : mm.getMetricValues()) {
                        if (!metrics.contains(entry.getKey())) {
                          inserter.addInsert(EntityMetricsTable.createInsert(projectID, fileID, entityID, entry.getKey(), entry.getValue()));
                        }
                      }
                    }
                  }
                }
                task.progress();
              }
            }
            inserter.insert();
            task.finish();
          }
        }
        task.finish();
      }
    }
  }
}
