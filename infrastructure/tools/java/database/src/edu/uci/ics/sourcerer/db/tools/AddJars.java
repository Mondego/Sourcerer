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
import java.util.Set;
import java.util.logging.Level;

import edu.uci.ics.sourcerer.db.schema.JarEntitiesTable;
import edu.uci.ics.sourcerer.db.schema.JarRelationsTable;
import edu.uci.ics.sourcerer.db.schema.JarsTable;
import edu.uci.ics.sourcerer.db.schema.LibraryEntitiesTable;
import edu.uci.ics.sourcerer.db.util.DatabaseAccessor;
import edu.uci.ics.sourcerer.db.util.DatabaseConnection;
import edu.uci.ics.sourcerer.db.util.InsertBatcher;
import edu.uci.ics.sourcerer.db.util.KeyInsertBatcher;
import edu.uci.ics.sourcerer.model.Entity;
import edu.uci.ics.sourcerer.model.Relation;
import edu.uci.ics.sourcerer.model.db.TypedEntityID;
import edu.uci.ics.sourcerer.model.extracted.EntityEX;
import edu.uci.ics.sourcerer.model.extracted.LocalVariableEX;
import edu.uci.ics.sourcerer.model.extracted.RelationEX;
import edu.uci.ics.sourcerer.repo.extracted.ExtractedJar;
import edu.uci.ics.sourcerer.repo.extracted.ExtractedRepository;
import edu.uci.ics.sourcerer.repo.extracted.io.ExtractedReader;
import edu.uci.ics.sourcerer.util.Helper;
import edu.uci.ics.sourcerer.util.io.Logging;
import edu.uci.ics.sourcerer.util.io.PropertyOld;
import edu.uci.ics.sourcerer.util.io.PropertyManager;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class AddJars extends DatabaseAccessor {
  public AddJars(DatabaseConnection connection) {
    super(connection);
  }
  
  public void addJars() {
    Set<String> completed = Logging.initializeResumeLogger();
    
    PropertyManager properties = PropertyManager.getProperties();
    ExtractedRepository extracted = ExtractedRepository.getRepository(properties.getValueAsFile(PropertyOld.INPUT));
    
    logger.info("Adding jars to database for " + extracted);
    
    // Get the jar list
    Collection<ExtractedJar> jars = extracted.getJars();
    
    logger.info("Found " + jars.size() + " extracted jars to insert.");
    
    int count = 0;
    for (ExtractedJar jar : jars) {
      logger.info("------------------------");
      logger.info("Inserting " + jar.getName() + "(" + ++count + " of " + jars.size() + ")");
      if (completed.contains(jar.getName())) {
        logger.info("Already completed!");
      } else {
        addJar(jar);
        logger.log(Logging.RESUME, jar.getName());
      }
    }
  }
  
  public void addJar(ExtractedJar jar) {
    // Check if the jar was added already
    String oldID = JarsTable.getJarIDByHash(executor, jar.getHash());
    if (oldID != null) {
      logger.info("Deleting existing jar...");
      JarsTable.deleteJar(executor, oldID);
    }
      
    // Add jar to the database
    final String jarID = JarsTable.insert(executor, jar);
    
    // Lock some tables
    executor.execute("LOCK TABLES jar_entities WRITE, jar_relations WRITE, library_entities READ;");
    
    // Add the entities to the database
    final Map<String, TypedEntityID> entityMap = Helper.newHashMap();
    {
      logger.info("Beginning insert of entities...");
      int count = 0;
      final Map<String, Collection<TypedEntityID>> packageMap = Helper.newHashMap();
      KeyInsertBatcher<String> batcher = JarEntitiesTable.<String>getKeyInsertBatcher(executor, new KeyInsertBatcher.KeyProcessor<String>() {
        public void processKey(String key, String fqn) {
          TypedEntityID eid = TypedEntityID.getLibraryEntityID(key);
          entityMap.put(fqn, eid);
          String pkg = Entity.getPossiblePackage(fqn);
          Collection<TypedEntityID> members = packageMap.get(pkg);
          if (members == null) {
            members = Helper.newLinkedList();
            packageMap.put(pkg, members);
          }
          members.add(eid);
        }
      });
      for (EntityEX entity : ExtractedReader.getJarEntityReader(jar)) {
        // Add the entity
        JarEntitiesTable.insert(batcher, entity, jarID, entity.getFqn());
        count++;
      }
      batcher.insert();
      logger.info(count + " entities inserted.");
      
      // Add the packages to the database
      count = 0;
      final InsertBatcher relationBatcher = JarRelationsTable.getInsertBatcher(executor);
      batcher.setProcessor(new KeyInsertBatcher.KeyProcessor<String>() {
        public void processKey(String key, String pkg) {
          // Add the inside relations
          for (TypedEntityID member : packageMap.get(pkg)) {
            JarRelationsTable.insert(relationBatcher, Relation.INSIDE, member.getID(), TypedEntityID.getJarEntityID(key), jarID);
          }
        }
      });
      for (Map.Entry<String, Collection<TypedEntityID>> pkg : packageMap.entrySet()) {
        if (!entityMap.containsKey(pkg.getKey())) {
          // If it's not an entity, then assume it's a package
          JarEntitiesTable.insertPackage(batcher, pkg.getKey(), jarID, pkg.getKey());
          count++;
        }
      }
      batcher.insert();
      relationBatcher.insert();
      logger.info(count + " packages inserted.");
    }
    
    // Add the local variables to the database
    {
      InsertBatcher batcher = JarRelationsTable.getInsertBatcher(executor);
      logger.info("Beginning insert of parameters...");
      int count = 0;
      for (LocalVariableEX param : ExtractedReader.getJarLocalVariableReader(jar)) {
        // Add the entity
        String eid = JarEntitiesTable.insertParam(executor, param.getName(), param.getModifiers(), param.getPosition(), jarID);
        
        // Add the holds relation
        TypedEntityID typeEid = getEid(batcher, entityMap, jarID, param.getTypeFqn());
        JarRelationsTable.insert(batcher, Relation.HOLDS, eid, typeEid, jarID);
        
        // Add the inside relation
        TypedEntityID parentEid = getEid(batcher, entityMap, jarID, param.getParent());
        JarRelationsTable.insert(batcher, Relation.INSIDE, eid, parentEid, jarID);
        
        count++;
      }
      // Add the relations to the database
      logger.info("Beginning insert of relations...");
      count = 0;
      
      for (RelationEX relation : ExtractedReader.getJarRelationReader(jar)) {
        // Look up the lhs eid
        TypedEntityID lhsEid = entityMap.get(relation.getLhs());
        if (lhsEid == null) {
          logger.log(Level.SEVERE, "Missing lhs for a relation! " + relation.getLhs());
          continue;
        }
        
        // Look up the rhs eid
        TypedEntityID rhsEid = getEid(batcher, entityMap, jarID, relation.getRhs());
        
        // Add the relation
        JarRelationsTable.insert(batcher, relation.getType(), lhsEid.getID(), rhsEid, jarID);
        count++;
      }
      batcher.insert();
      logger.info(count + " relations inserted.");
    }
    // Unlock the tables
    executor.execute("UNLOCK TABLES;");
    executor.reset();
  }
  
  private TypedEntityID getEid(InsertBatcher batcher, Map<String, TypedEntityID> entityMap, String jarID, String fqn) {
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
        TypedEntityID eid = TypedEntityID.getJarEntityID(JarEntitiesTable.insertArray(executor, elementFqn, dimensions, jarID));
            
        TypedEntityID elementEid = getEid(batcher, entityMap, jarID, elementFqn);
        JarRelationsTable.insert(batcher, Relation.HAS_ELEMENTS_OF, eid.getID(), elementEid, jarID);
        entityMap.put(fqn, eid);
        return eid;
      }
      
      // Or maybe it's a wildcard type
      if (fqn.startsWith("<?") && fqn.endsWith(">")) {
        TypedEntityID eid = TypedEntityID.getJarEntityID(JarEntitiesTable.insert(executor, Entity.WILDCARD, fqn, jarID));
        
        if (!fqn.equals("<?>")) {
          boolean isLower = TypeUtils.isLowerBound(fqn);
          TypedEntityID bound = getEid(batcher, entityMap, jarID, TypeUtils.getWildcardBound(fqn));
          if (isLower) {
            JarRelationsTable.insert(batcher, Relation.HAS_LOWER_BOUND, eid.getID(), bound, jarID);
          } else {
            JarRelationsTable.insert(batcher, Relation.HAS_UPPER_BOUND, eid.getID(), bound, jarID);
          }
        }
        
        entityMap.put(fqn, eid);
        return eid;
      }
      
      // Or maybe it's a new type variable!
      if (fqn.startsWith("<") && fqn.endsWith(">")) {
        TypedEntityID eid = TypedEntityID.getJarEntityID(JarEntitiesTable.insert(executor, Entity.TYPE_VARIABLE, fqn, jarID));
        
        for (String bound : TypeUtils.breakTypeVariable(fqn)) {
          TypedEntityID boundEid = getEid(batcher, entityMap, jarID, bound);
          JarRelationsTable.insert(batcher, Relation.HAS_UPPER_BOUND, eid.getID(), boundEid, jarID);
        }
        
        entityMap.put(fqn, eid);
        return eid;
      }
      
      // Or a new parametrized type!
      int baseIndex = fqn.indexOf("<");
      if (baseIndex > 0 && fqn.indexOf('>') > baseIndex) {
        TypedEntityID eid = TypedEntityID.getJarEntityID(JarEntitiesTable.insert(executor, Entity.PARAMETERIZED_TYPE, fqn, jarID));
        
        String baseType = TypeUtils.getBaseType(fqn);
        TypedEntityID baseTypeEid = getEid(batcher, entityMap, jarID, baseType);
        
        JarRelationsTable.insert(batcher, Relation.HAS_BASE_TYPE, eid.getID(), baseTypeEid, jarID);
        
        for (String arg : TypeUtils.breakParametrizedType(fqn)) {
          TypedEntityID argEid = getEid(batcher, entityMap, jarID, arg);
          JarRelationsTable.insert(batcher, Relation.HAS_TYPE_ARGUMENT, eid.getID(), argEid, jarID);
        }
        
        entityMap.put(fqn, eid);
        return eid;
      }
    }
    
    // Some Java library reference?
    String eid = LibraryEntitiesTable.getEntityIDByFqn(executor, fqn);
    if (eid != null) {
      TypedEntityID teid = TypedEntityID.getLibraryEntityID(eid);
      entityMap.put(fqn, teid);
      return teid;
    }
    
    // Well, I give up
    TypedEntityID teid = TypedEntityID.getJarEntityID(JarEntitiesTable.insert(executor, Entity.UNKNOWN, fqn, jarID));
    entityMap.put(fqn, teid);
    return teid;
  }

  public static void main(String[] args) {
    PropertyManager.initializeProperties(args);
    Logging.initializeLogger();
    
    DatabaseConnection connection = new DatabaseConnection();
    connection.open();
    
    AddJars accessor = new AddJars(connection);
    accessor.addJars();
    
    connection.close();
  }

}
