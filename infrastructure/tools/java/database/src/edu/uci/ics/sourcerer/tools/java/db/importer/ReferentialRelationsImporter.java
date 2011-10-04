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
import edu.uci.ics.sourcerer.tools.java.db.schema.RelationsTable;
import edu.uci.ics.sourcerer.tools.java.model.extracted.RelationEX;
import edu.uci.ics.sourcerer.tools.java.model.extracted.io.ReaderBundle;
import edu.uci.ics.sourcerer.tools.java.model.types.Relation;
import edu.uci.ics.sourcerer.utils.db.BatchInserter;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public abstract class ReferentialRelationsImporter extends RelationsImporter {
  private EntityMap external;
  
  protected ReferentialRelationsImporter(String taskName, LibraryEntityMap libraries) {
    super(taskName);
    this.external = new EntityMap(exec, libraries);
  }
  
  protected final void insert(ReaderBundle reader, Integer projectID) {
    loadFileMap(projectID);
    loadEntityMap(projectID);
    
    insertReferentialRelations(reader, projectID);
    
    fileMap.clear();
    entityMap.clear();
  }
  
  private void insertReferentialRelations(ReaderBundle reader, Integer projectID) {
    task.start("Inserting relations");
    
    BatchInserter inserter = exec.makeInFileInserter(tempDir, RelationsTable.TABLE);
    
    task.start("Processing referential relations", "relations processed");
    for (RelationEX relation : reader.getTransientRelations()) {
      if (relation.getType() == Relation.CALLS ||
          relation.getType() == Relation.READS ||
          relation.getType() == Relation.WRITES) {
        Integer fileID = getFileID(relation.getLocation());
        
        Integer lhs = getLHS(relation.getLhs());
        DatabaseEntity rhs = getRHS(relation.getRhs());
        
        if (lhs != null && rhs != null) {
          if (fileID == null) {
            inserter.addInsert(RelationsTable.makeInsert(relation.getType(), rhs.getRelationClass(), lhs, rhs.getEntityID(), projectID));
          } else {
            inserter.addInsert(RelationsTable.makeInsert(relation.getType(), rhs.getRelationClass(), lhs, rhs.getEntityID(), projectID, fileID, relation.getLocation()));
          }
          task.progress();
        }
      }
    }
    task.finish();
    
    task.start("Performing db insert");
    inserter.insert();
    task.finish();
    
    task.finish();
  }
  
  private DatabaseEntity getRHS(String fqn) {
    DatabaseEntity entity = entityMap.get(fqn);
    if (entity == null) {
      entity = external.getEntity(fqn);
      if (entity == null) {
        logger.severe("Unknown entity: " + fqn);
        return null;
      } else {
        return entity;
      }
    } else {
      return entity;
    }
  }
}
