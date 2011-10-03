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

import static edu.uci.ics.sourcerer.util.io.Logging.logger;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

import edu.uci.ics.sourcerer.tools.java.db.schema.EntitiesTable;
import edu.uci.ics.sourcerer.tools.java.db.schema.FilesTable;
import edu.uci.ics.sourcerer.tools.java.db.schema.ProblemsTable;
import edu.uci.ics.sourcerer.tools.java.model.extracted.EntityEX;
import edu.uci.ics.sourcerer.tools.java.model.extracted.FileEX;
import edu.uci.ics.sourcerer.tools.java.model.extracted.ProblemEX;
import edu.uci.ics.sourcerer.tools.java.model.extracted.io.ReaderBundle;
import edu.uci.ics.sourcerer.tools.java.model.types.Entity;
import edu.uci.ics.sourcerer.utils.db.BatchInserter;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public abstract class EntitiesImporter extends NewDatabaseImporter {
  protected EntitiesImporter(String taskName) {
    super(taskName);
    fileMap = new HashMap<>();
  }

  protected final void insert(ReaderBundle reader, Integer projectID) {
    insertFiles(reader, projectID);
    loadFileMap(projectID);
    insertFileMetrics(reader, projectID);
    insertProblems(reader, projectID);
    insertEntities(reader, projectID);
    fileMap.clear();
  }
  
  private void insertFiles(ReaderBundle reader, Integer projectID) {
    task.start("Inserting files");
    
    task.start("Processing files", "files processed");
    BatchInserter inserter = exec.makeInFileInserter(tempDir, FilesTable.TABLE);
    
    for (FileEX file : reader.getTransientFiles()) {
      inserter.addInsert(FilesTable.makeInsert(file, projectID));
      task.progress();
    }
    task.finish();
    
    task.start("Performing db insert");
    inserter.insert();
    task.finish();
    
    task.finish();
  }
  
  private void insertFileMetrics(ReaderBundle reader, Integer projectID) {
//    task.start("Inserting project metrics");
//    
//    task.start("Processing project metrics", "projects processed");
//    BatchInserter inserter = exec.makeInFileInserter(tempDir, ProjectMetricsTable.TABLE);
//    
  }
  
  private void insertProblems(ReaderBundle reader, Integer projectID) {
    task.start("Inserting problems");

    task.start("Processing problems", "problems processed");
    BatchInserter inserter = exec.makeInFileInserter(tempDir, ProblemsTable.TABLE);
    
    for (ProblemEX problem : reader.getTransientProblems()) {
      Integer fileID = fileMap.get(problem.getPath());
      if (fileID == null) {
        logger.log(Level.SEVERE, "Unknown file: " + problem.getPath() + " for " + problem);
      } else {
        inserter.addInsert(ProblemsTable.makeInsert(problem, projectID, fileID));
        task.progress();
      }
    }
    task.finish();
        
    task.start("Performing db insert");
    inserter.insert();
    task.finish();
    
    task.finish();
  }
  
  private void insertEntities(ReaderBundle reader, Integer projectID) {
    task.start("Inserting entities");

    task.start("Processing entities", "entities processed");
    BatchInserter inserter = exec.makeInFileInserter(tempDir, EntitiesTable.TABLE);
    
    Set<String> usedFqns = new HashSet<>();
    for (EntityEX entity : reader.getTransientEntities()) {
      if (entity.getType() == Entity.PACKAGE) {
        if (usedFqns.contains(entity.getFqn())) {
          continue;
        } else {
          usedFqns.add(entity.getFqn());
        }
      }
      Integer fileID = getFileID(entity.getLocation());
      inserter.addInsert(EntitiesTable.makeInsert(entity, projectID, fileID));
      task.progress();
      
    }
    task.finish();
    
    task.start("Performing db insert");
    inserter.insert();
    task.finish();
    
    task.finish();
  }
}
