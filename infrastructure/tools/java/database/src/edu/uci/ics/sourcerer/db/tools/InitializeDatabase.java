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
package edu.uci.ics.sourcerer.db.tools;

import static edu.uci.ics.sourcerer.util.io.Logging.logger;

import java.util.Collection;
import java.util.Map;
import java.util.logging.Level;

import edu.uci.ics.sourcerer.db.schema.EntitiesTable;
import edu.uci.ics.sourcerer.db.schema.FilesTable;
import edu.uci.ics.sourcerer.db.schema.ImportsTable;
import edu.uci.ics.sourcerer.db.schema.JarEntitiesTable;
import edu.uci.ics.sourcerer.db.schema.JarRelationsTable;
import edu.uci.ics.sourcerer.db.schema.JarUsesTable;
import edu.uci.ics.sourcerer.db.schema.JarsTable;
import edu.uci.ics.sourcerer.db.schema.LibrariesTable;
import edu.uci.ics.sourcerer.db.schema.LibraryEntitiesTable;
import edu.uci.ics.sourcerer.db.schema.LibraryRelationsTable;
import edu.uci.ics.sourcerer.db.schema.ProblemsTable;
import edu.uci.ics.sourcerer.db.schema.ProjectsTable;
import edu.uci.ics.sourcerer.db.schema.RelationsTable;
import edu.uci.ics.sourcerer.db.util.DatabaseAccessor;
import edu.uci.ics.sourcerer.db.util.DatabaseConnection;
import edu.uci.ics.sourcerer.db.util.InsertBatcher;
import edu.uci.ics.sourcerer.db.util.KeyInsertBatcher;
import edu.uci.ics.sourcerer.model.Entity;
import edu.uci.ics.sourcerer.model.Relation;
import edu.uci.ics.sourcerer.model.extracted.EntityEX;
import edu.uci.ics.sourcerer.model.extracted.LocalVariableEX;
import edu.uci.ics.sourcerer.model.extracted.RelationEX;
import edu.uci.ics.sourcerer.repo.extracted.ExtractedLibrary;
import edu.uci.ics.sourcerer.repo.extracted.ExtractedRepository;
import edu.uci.ics.sourcerer.repo.extracted.io.ExtractedReader;
import edu.uci.ics.sourcerer.util.Helper;
import edu.uci.ics.sourcerer.util.io.Property;
import edu.uci.ics.sourcerer.util.io.PropertyManager;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class InitializeDatabase extends DatabaseAccessor {
  public InitializeDatabase(DatabaseConnection connection) {
    super(connection);
  }
  
  public void initializeDatabase() {
    PropertyManager properties = PropertyManager.getProperties();
    ExtractedRepository extracted = ExtractedRepository.getRepository(properties.getValueAsFile(Property.INPUT));
    logger.info("Initializing database...");
    {
      executor.dropTables(
          LibrariesTable.TABLE,
          LibraryEntitiesTable.TABLE,
          LibraryRelationsTable.TABLE,
          JarsTable.TABLE,
          JarEntitiesTable.TABLE,
          JarRelationsTable.TABLE,
          JarUsesTable.TABLE,
          ProjectsTable.TABLE,
          FilesTable.TABLE,
          ProblemsTable.TABLE,
          ImportsTable.TABLE,
          EntitiesTable.TABLE,
          RelationsTable.TABLE);
      LibrariesTable.createTable(executor);
      LibraryEntitiesTable.createTable(executor);
      LibraryRelationsTable.createTable(executor);
      JarsTable.createTable(executor);
      JarEntitiesTable.createTable(executor);
      JarRelationsTable.createTable(executor);
      JarUsesTable.createTable(executor);
      ProjectsTable.createTable(executor);
      ImportsTable.createTable(executor);
      EntitiesTable.createTable(executor);
      RelationsTable.createTable(executor);
    }
    
    // The database was initialized, so add the library jars
    logger.info("Locating all the extracted library jars...");
    Collection<ExtractedLibrary> libraryJars = extracted.getLibraries();
    
    logger.info("Found " + libraryJars.size() + " extracted libraries to insert.");
    
    int count = 0;
    // Do all the entities first
    Map<String, String> entityMap = Helper.newHashMap();
    // Lock some tables
    executor.execute("LOCK TABLES libraries WRITE, library_entities WRITE, library_relations WRITE;");
    // Do the entities first
    for (ExtractedLibrary libraryJar : libraryJars) {
      logger.info("------------------------");
      logger.info("Inserting " + libraryJar.getName() + "'s entities (" + ++count + " of " + libraryJars.size() + ")");
      insertLibraryEntities(libraryJar, entityMap);
    }
    
    count = 0;
    for (ExtractedLibrary libraryJar : libraryJars) {
      logger.info("------------------------");
      logger.info("Inserting " + libraryJar.getName() + "'s params and relations (" + ++count + " of " + libraryJars.size() + ")");
      insertLibraryParamsAndRelations(libraryJar, entityMap);
    }
    
    // Unlock the tables
    executor.execute("UNLOCK TABLES;");
    executor.reset();
  }
  
  private void insertLibraryEntities(ExtractedLibrary library, final Map<String, String> entityMap) {
    String name = library.getName();
    final String libraryID = LibrariesTable.insert(executor, name);
    
    // Add the entities to the database
    {
      logger.info("Beginning insert of entities...");
      int count = 0;
      final Map<String, Collection<String>> packageMap = Helper.newHashMap();
      KeyInsertBatcher<String> batcher = LibraryEntitiesTable.<String>getKeyInsertBatcher(executor, new KeyInsertBatcher.KeyProcessor<String>() {
        public void processKey(String key, String fqn) {
          entityMap.put(fqn, key);
          String pkg = Entity.getPossiblePackage(fqn);
          Collection<String> members = packageMap.get(pkg);
          if (members == null) {
            members = Helper.newLinkedList();
            packageMap.put(pkg, members);
          }
          members.add(key);
        }
      });
      for (EntityEX entity : ExtractedReader.getJarEntityReader(library)) {
        LibraryEntitiesTable.insert(batcher, entity, libraryID, entity.getFqn());
        count++;
      }
      batcher.insert();
      logger.info(count + " entities inserted.");
        
      // Add the packages to the database
      count = 0;
      final InsertBatcher relationBatcher = LibraryRelationsTable.getInsertBatcher(executor);
      batcher.setProcessor(new KeyInsertBatcher.KeyProcessor<String>() {
        public void processKey(String key, String pkg) {
          // Add the inside relations
          for (String member : packageMap.get(pkg)) {
            LibraryRelationsTable.insert(relationBatcher, Relation.INSIDE, member, key, libraryID);
          }
        }
      });
      for (Map.Entry<String, Collection<String>> pkg : packageMap.entrySet()) {
        if (!entityMap.containsKey(pkg.getKey())) {
          // If it's not an entity, then assume it's a package
          LibraryEntitiesTable.insertPackage(batcher, pkg.getKey(), libraryID, pkg.getKey());
          count++;
        }
      }
      batcher.insert();
      relationBatcher.insert();
      logger.info(count + " packages inserted.");
    }
  }
  
  private void insertLibraryParamsAndRelations(ExtractedLibrary library, Map<String, String> entityMap) {
    String name = library.getName();
    String libraryID = LibrariesTable.getLibraryIDByName(executor, name);
    {
      InsertBatcher batcher = LibraryRelationsTable.getInsertBatcher(executor);
      
      // Add the local variables to the database
      logger.info("Beginning insert of parameters...");
      int count = 0;
      for (LocalVariableEX param : ExtractedReader.getJarLocalVariableReader(library)) {
        // Add the entity
        String eid = LibraryEntitiesTable.insertParam(executor, param.getName(), param.getModifiers(), param.getPosition(), libraryID);
        
        // Add the holds relation
        String typeEid = getEid(batcher, entityMap, libraryID, param.getTypeFqn());
        LibraryRelationsTable.insert(batcher, Relation.HOLDS, eid, typeEid, libraryID);
        
        // Add the inside relation
        String parentEid = getEid(batcher, entityMap, libraryID, param.getParent());
        LibraryRelationsTable.insert(batcher, Relation.INSIDE, eid, parentEid, libraryID);
        
        count++;
      }
      // Add the relations to the database
      logger.info("Beginning insert of relations...");
      count = 0;
      
      for (RelationEX relation : ExtractedReader.getJarRelationReader(library)) {
        // Look up the lhs eid
        String lhsEid = entityMap.get(relation.getLhs());
        if (lhsEid == null) {
          logger.log(Level.SEVERE, "Missing lhs for a relation! " + relation.getLhs());
          continue;
        }
        
        // Look up the rhs eid
        String rhsEid = getEid(batcher, entityMap, libraryID, relation.getRhs());
        
        // Add the relation
        LibraryRelationsTable.insert(batcher, relation.getType(), lhsEid, rhsEid, libraryID);
        count++;
      }
      batcher.insert();
      logger.info(count + " relations inserted.");
    }
  }
  
  private String getEid(InsertBatcher batcher, Map<String, String> entityMap, String libraryID, String fqn) {
    // Maybe it's just in the map
    if (entityMap.containsKey(fqn)) {
      return entityMap.get(fqn);
    }
    
    // If it's a method, skip the type entities
    if (!(fqn.contains("(") && fqn.endsWith(")"))) {
      // Could it be an array we haven't seen?
      if (fqn.endsWith("[]")) {
        int arrIndex = fqn.indexOf("[]");
        String elementFqn = fqn.substring(0, arrIndex);
        int dimensions = (fqn.length() - arrIndex) / 2;
        String eid = LibraryEntitiesTable.insertArray(executor, elementFqn, dimensions, libraryID);
            
        String elementEid = getEid(batcher, entityMap, libraryID, elementFqn);
        LibraryRelationsTable.insert(batcher, Relation.HAS_ELEMENTS_OF, eid, elementEid, libraryID);
        
        entityMap.put(fqn, eid);
        return eid;
      }
      
      // Or maybe it's a wildcard type
      if (fqn.startsWith("<?") && fqn.endsWith(">")) {
        String eid = LibraryEntitiesTable.insert(executor, Entity.WILDCARD, fqn, libraryID);
        
        if (!fqn.equals("<?>")) {
          boolean isLower = TypeUtils.isLowerBound(fqn);
          String bound = getEid(batcher, entityMap, libraryID, TypeUtils.getWildcardBound(fqn));
          if (isLower) {
            LibraryRelationsTable.insert(batcher, Relation.HAS_LOWER_BOUND, eid, bound, libraryID);
          } else {
            LibraryRelationsTable.insert(batcher, Relation.HAS_UPPER_BOUND, eid, bound, libraryID);
          }
        }
        
        entityMap.put(fqn, eid);
        return eid;
      }
      
      // Or maybe it's a new type variable!
      if (fqn.startsWith("<") && fqn.endsWith(">")) {
        String eid = LibraryEntitiesTable.insert(executor, Entity.TYPE_VARIABLE, fqn, libraryID);
        
        for (String bound : TypeUtils.breakTypeVariable(fqn)) {
          String boundEid = getEid(batcher, entityMap, libraryID, bound);
          LibraryRelationsTable.insert(batcher, Relation.HAS_UPPER_BOUND, eid, boundEid, libraryID);
        }
        
        entityMap.put(fqn, eid);
        return eid;
      }
      
      // Or a new parametrized type!
      int baseIndex = fqn.indexOf("<");
      if (baseIndex > 0 && fqn.indexOf('>') > baseIndex) {
        String eid = LibraryEntitiesTable.insert(executor, Entity.PARAMETRIZED_TYPE, fqn, libraryID);
        
        String baseType = getEid(batcher, entityMap, libraryID, TypeUtils.getBaseType(fqn));
        LibraryRelationsTable.insert(batcher, Relation.HAS_BASE_TYPE, eid, baseType, libraryID);
        
        for (String arg : TypeUtils.breakParametrizedType(fqn)) {
          String argEid = getEid(batcher, entityMap, libraryID, arg);
          LibraryRelationsTable.insert(batcher, Relation.HAS_UPPER_BOUND, eid, argEid, libraryID);
        }
        
        entityMap.put(fqn, eid);
        return eid;
      }
    }
    
    // Well, I give up
    String eid = LibraryEntitiesTable.insert(executor, Entity.UNKNOWN, fqn, libraryID);
    entityMap.put(fqn, eid);
    return eid;
  }
}
