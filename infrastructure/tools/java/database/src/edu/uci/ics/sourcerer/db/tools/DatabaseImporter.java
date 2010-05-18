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
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import edu.uci.ics.sourcerer.db.schema.DatabaseAccessor;
import edu.uci.ics.sourcerer.db.util.DatabaseConnection;
import edu.uci.ics.sourcerer.model.Comment;
import edu.uci.ics.sourcerer.model.Entity;
import edu.uci.ics.sourcerer.model.Relation;
import edu.uci.ics.sourcerer.model.db.LimitedEntityDB;
import edu.uci.ics.sourcerer.model.db.LimitedProjectDB;
import edu.uci.ics.sourcerer.model.db.SlightlyLessLimitedEntityDB;
import edu.uci.ics.sourcerer.model.extracted.CommentEX;
import edu.uci.ics.sourcerer.model.extracted.EntityEX;
import edu.uci.ics.sourcerer.model.extracted.FileEX;
import edu.uci.ics.sourcerer.model.extracted.ImportEX;
import edu.uci.ics.sourcerer.model.extracted.LocalVariableEX;
import edu.uci.ics.sourcerer.model.extracted.ModelEX;
import edu.uci.ics.sourcerer.model.extracted.ProblemEX;
import edu.uci.ics.sourcerer.model.extracted.RelationEX;
import edu.uci.ics.sourcerer.model.extracted.UsedJarEX;
import edu.uci.ics.sourcerer.repo.extracted.Extracted;
import edu.uci.ics.sourcerer.repo.extracted.ExtractedJar;
import edu.uci.ics.sourcerer.repo.extracted.ExtractedLibrary;
import edu.uci.ics.sourcerer.repo.extracted.ExtractedProject;
import edu.uci.ics.sourcerer.repo.extracted.ExtractedRepository;
import edu.uci.ics.sourcerer.repo.general.AbstractRepository;
import edu.uci.ics.sourcerer.util.Helper;
import edu.uci.ics.sourcerer.util.Pair;
import edu.uci.ics.sourcerer.util.TimeCounter;
import edu.uci.ics.sourcerer.util.io.FileUtils;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class DatabaseImporter extends DatabaseAccessor {
  private String unknownProject;
  private Map<String, String> fileMap;
  private Map<String, Ent> entityMap;
  private Set<String> pendingEntities;
  private Collection<RelationEX> newTypeRelations;
  private java.io.File tempDir;
  
  protected DatabaseImporter(DatabaseConnection connection) {
    super(connection);
    tempDir = FileUtils.getTempDir();
    fileMap = Helper.newHashMap();
    entityMap = Helper.newHashMap();
    pendingEntities = Helper.newHashSet();
    newTypeRelations = Helper.newArrayList();
  }

  protected void initializeDatabase() {
    logger.info("Initializing database...");
    
    logger.info("  Dropping old tables...");
    executor.dropTables(
        projectsTable,
        filesTable,
        importsTable,
        problemsTable,
        commentsTable,
        entitiesTable,
        relationsTable);
    
    logger.info("  Adding new tables...");
    projectsTable.createTable();
    filesTable.createTable();
    importsTable.createTable();
    problemsTable.createTable();
    commentsTable.createTable();
    entitiesTable.createTable();
    relationsTable.createTable();
    
    logger.info("  Adding the primitive types...");
    locker.addWrites(projectsTable, entitiesTable);
    locker.lock();
    
    String projectID = projectsTable.insertPrimitivesProject();
    entitiesTable.forceInsert(Entity.PRIMITIVE, "boolean", projectID);
    entitiesTable.forceInsert(Entity.PRIMITIVE, "char", projectID);
    entitiesTable.forceInsert(Entity.PRIMITIVE, "byte", projectID);
    entitiesTable.forceInsert(Entity.PRIMITIVE, "short", projectID);
    entitiesTable.forceInsert(Entity.PRIMITIVE, "int", projectID);
    entitiesTable.forceInsert(Entity.PRIMITIVE, "long", projectID);
    entitiesTable.forceInsert(Entity.PRIMITIVE, "float", projectID);
    entitiesTable.forceInsert(Entity.PRIMITIVE, "double", projectID);
    entitiesTable.forceInsert(Entity.PRIMITIVE, "void", projectID);
    
    logger.info("  Adding the unknowns project...");
    projectsTable.insertUnknownsProject();
    
    locker.unlock();
    logger.info("  Initialization complete.");
  }
  
  protected void importJavaLibraries() {
    logger.info("Importing Java libraries...");
    
    TimeCounter counter = new TimeCounter();
    
    logger.info("  Loading extracted repository...");
    ExtractedRepository extracted = ExtractedRepository.getRepository();
    
    logger.info("  Loading extracted Java libraries...");
    Collection<ExtractedLibrary> libraries = extracted.getLibraries();
    
    logger.info("  Importing " + libraries.size() + " libraries...");
    
    unknownProject = projectsTable.getUnknownsProject();
    
    for (ExtractedLibrary library : libraries) {
      logger.info("  Partial import of " + library.getName());
      
      logger.info("    Inserting project...");
      String projectID = projectsTable.insert(library);
      
      locker.addWrite(filesTable);
      locker.lock();
      insertFiles(library, projectID);
      locker.unlock();
      
      locker.addRead(filesTable);
      locker.lock();
      loadFileMap(projectID);
      locker.unlock();
      
      locker.addWrite(problemsTable);
      locker.lock();
      insertProblems(library, projectID);
      locker.unlock();
      
      locker.addWrite(entitiesTable);
      locker.lock();
      insertEntities(library, projectID);
      locker.unlock();

      fileMap.clear();
      counter.increment();
    }
    
    String inClause = buildInClause(Collections.singleton(projectsTable.getPrimitiveProject()));
    
    for (ExtractedLibrary library : libraries) {
      logger.info("  Remaining import of " + library.getName());
    
      String projectID = projectsTable.getProjectIDByName(library.getName());

      locker.addRead(entitiesTable);
      locker.lock();
      loadEntityMap(projectID);
      locker.unlock();
      
      locker.addRead(filesTable);
      locker.lock();
      loadFileMap(projectID);
      locker.unlock();
      
      locker.addWrite(entitiesTable);
      locker.lock();
      insertRemainingEntities(library, projectID, inClause);
      locker.unlock();
      
      locker.addRead(entitiesTable);
      locker.lock();
      loadRemainingEntityMap(projectID);
      locker.unlock();
      
      locker.addWrites(relationsTable);
      locker.lock();
      insertRelations(library, projectID);
      locker.unlock();
      
      locker.addWrite(importsTable);
      locker.lock();
      insertImports(library, projectID);
      locker.unlock();
      
      locker.addWrite(commentsTable);
      locker.lock();
      insertComments(library, projectID);
      locker.unlock();
      
      fileMap.clear();
      entityMap.clear();
    }
    
    logger.info(counter.reportTimeAndCount(2, "libraries imported"));
  }
  
  protected void importJarFiles() {
    logger.info("Importing jar files...");
    
    TimeCounter counter = new TimeCounter();
    
    logger.info("  Loading extracted repository...");
    ExtractedRepository extracted = ExtractedRepository.getRepository();
    
    logger.info("  Loading extracted jar files...");
    Collection<ExtractedJar> jars = null;
    if (AbstractRepository.JAR_FILTER.hasValue()) {
      jars = extracted.getJars(FileUtils.getFileAsSet(AbstractRepository.JAR_FILTER.getValue()));
    } else { 
      jars = extracted.getJars();
    }
    
    logger.info("  Importing " + jars.size() + " jars...");
    fileMap = Helper.newHashMap();
    entityMap = Helper.newHashMap();
    
    unknownProject = projectsTable.getUnknownsProject();
    Collection<String> projectIDs = projectsTable.getJavaLibraryProjects();
    projectIDs.add(projectsTable.getPrimitiveProject());
    
    
    for (ExtractedJar jar : jars) {
      logger.info("  Partial import of " + jar.getName());
      logger.info("    Verifying that jar should be imported...");
      if (!jar.extracted()) {
        logger.info("      Extraction not completed... skipping");
        continue;
      }
      if (!jar.reallyExtracted()) {
        logger.info("      Extraction copied... skipping");
        continue;
      }
      LimitedProjectDB project = projectsTable.getLimitedProjectByHash(jar.getHash());
      if (project != null) {
        if (project.firstStageCompleted()) {
          logger.info("      Import already completed... skipping");
          continue;
        } else {
          logger.info("      Import not completed... deleting");
          deleteByProject(project.getProjectID());
        }
      }
      
      logger.info("    Inserting project...");
      String projectID = projectsTable.insert(jar);

      locker.addWrite(filesTable);
      locker.lock();
      insertFiles(jar, projectID);
      locker.unlock();
      
      locker.addRead(filesTable);
      locker.lock();
      loadFileMap(projectID);
      locker.unlock();
      
      locker.addWrite(problemsTable);
      locker.lock();
      insertProblems(jar, projectID);
      locker.unlock();
      
      locker.addWrite(entitiesTable);
      locker.lock();
      insertEntities(jar, projectID);
      locker.unlock();
      
      projectsTable.endFirstStageJarProjectInsert(projectID);
      counter.increment();
      fileMap.clear();
    }
    
    for (ExtractedJar jar : jars) {
      if (!jar.extracted()) {
        continue;
      }
      if (!jar.reallyExtracted()) {
        continue;
      }
      LimitedProjectDB project = projectsTable.getLimitedProjectByHash(jar.getHash());
      if (project != null) {
        if (project.completed()) {
          continue;
        }
      }
      logger.info("  Remaining import of " + jar.getName());
      
      String inClause = buildInClause(Helper.newHashSet(projectIDs), jar);
      String projectID = projectsTable.getProjectIDByHash(jar.getHash());

      projectsTable.beginSecondStageJarProjectInsert(projectID);
      
      locker.addRead(entitiesTable);
      locker.lock();
      loadEntityMap(projectID);
      locker.unlock();
      
      locker.addRead(filesTable);
      locker.lock();
      loadFileMap(projectID);
      locker.unlock();
      
      locker.addWrite(entitiesTable);
      locker.lock();
      insertRemainingEntities(jar, projectID, inClause);
      locker.unlock();
      
      locker.addRead(entitiesTable);
      locker.addWrite(relationsTable);
      locker.lock();
      loadRemainingEntityMap(projectID);
      locker.unlock();
      
      locker.addWrites(relationsTable);
      locker.lock();
      insertRelations(jar, projectID);
      locker.unlock();
      
      locker.addWrite(importsTable);
      locker.lock();
      insertImports(jar, projectID);
      locker.unlock();
      
      locker.addWrite(commentsTable);
      locker.lock();
      insertComments(jar, projectID);
      locker.unlock();
      
      projectsTable.completeJarProjectInsert(projectID);
      entityMap.clear();
      fileMap.clear();
    }
    
    logger.info(counter.reportTimeAndCount(2, "jars imported"));
  }
  
  protected void importProjects() {
    logger.info("Importing projects...");
    
    TimeCounter counter = new TimeCounter();
    
    logger.info("  Loading exracted repository...");
    ExtractedRepository extracted = ExtractedRepository.getRepository();
    
    logger.info("  Loading exracted projects...");
    Collection<ExtractedProject> projects = null;
    if (AbstractRepository.PROJECT_FILTER.hasValue()) {
      projects = extracted.getProjects(FileUtils.getFileAsSet(AbstractRepository.PROJECT_FILTER.getValue()));
    } else {
      projects = extracted.getProjects();
    }
    
    logger.info("  Importing " + projects.size() + " projects...");
    
    unknownProject = projectsTable.getUnknownsProject();
    Collection<String> projectIDs = projectsTable.getJavaLibraryProjects();
    projectIDs.add(projectsTable.getPrimitiveProject());
    
    locker.addWrites(projectsTable, filesTable, problemsTable, entitiesTable);
    locker.lock();
    for (ExtractedProject project : projects) {
      logger.info("  Partial import of " + project.getName());
      logger.info("    Verifying that project should be imported...");
      if (!project.extracted()) {
        logger.info("      Extraction not completed... skipping");
        continue;
      }
      if (!project.reallyExtracted()) {
        logger.info("      Extraction copied... skipping");
        continue;
      }
      LimitedProjectDB oldProject = projectsTable.getLimitedProjectByPath(project.getProjectPath());
      if (oldProject != null) {
        if (oldProject.firstStageCompleted()) {
          logger.info("      Import already completed... skipping");
          continue;
        } else {
          logger.info("      Import not completed... deleting");
          deleteByProject(oldProject.getProjectID());
        }
      }
      
      logger.info("    Inserting project...");
      String projectID = projectsTable.insert(project);

      locker.addWrite(filesTable);
      locker.lock();
      insertFiles(project, projectID);
      locker.unlock();
      
      locker.addRead(filesTable);
      locker.lock();
      loadFileMap(projectID);
      locker.unlock();
      
      locker.addWrite(problemsTable);
      locker.lock();
      insertProblems(project, projectID);
      locker.unlock();
      
      locker.addWrite(entitiesTable);
      locker.lock();
      insertEntities(project, projectID);
      locker.unlock();
      
      projectsTable.endFirstStageCrawledProjectInsert(projectID);
      counter.increment();
      fileMap.clear();
    }
    
    for (ExtractedProject project : projects) {
      if (!project.extracted()) {
        continue;
      }
      if (!project.reallyExtracted()) {
        continue;
      }
      LimitedProjectDB oldProject = projectsTable.getLimitedProjectByPath(project.getProjectPath());
      if (oldProject != null) {
        if (oldProject.completed()) {
          continue;
        }
      }
      logger.info("  Remaining import of " + project.getName());

      String inClause = buildInClause(Helper.newHashSet(projectIDs), project);
      String projectID = projectsTable.getProjectIDByPath(project.getProjectPath());

      projectsTable.beginSecondStageCrawledProjectInsert(projectID);
      
      locker.addRead(entitiesTable);
      locker.lock();
      loadEntityMap(projectID);
      locker.unlock();
      
      locker.addRead(filesTable);
      locker.lock();
      loadFileMap(projectID);
      locker.unlock();
      
      locker.addWrite(entitiesTable);
      locker.lock();
      insertRemainingEntities(project, projectID, inClause);
      locker.unlock();
      
      locker.addRead(entitiesTable);
      locker.lock();
      loadRemainingEntityMap(projectID);
      locker.unlock();
      
      locker.addWrites(relationsTable);
      locker.lock();
      insertRelations(project, projectID);
      locker.unlock();
      
      locker.addWrite(importsTable);
      locker.lock();
      insertImports(project, projectID);
      locker.unlock();
      
      locker.addWrite(commentsTable);
      locker.lock();
      insertComments(project, projectID);
      locker.unlock();
      
      projectsTable.completeCrawledProjectInsert(projectID);
      entityMap.clear();
      fileMap.clear();
    }
    
    logger.info(counter.reportTimeAndCount(2, "projects imported"));
  }
  
  private String buildInClause(Collection<String> projectIDs, Extracted extracted) {
    for (UsedJarEX usedJar : extracted.getUsedJarReader()) {
      projectIDs.add(projectsTable.getProjectIDByHash(usedJar.getHash()));
    }
    return buildInClause(projectIDs);
  }
  
  private String buildInClause(Collection<String> projectIDs) {
    StringBuilder builder = new StringBuilder("(");
    for (String projectID : projectIDs) {
      builder.append(projectID).append(',');
    }
    builder.setCharAt(builder.length() - 1, ')');
    return builder.toString();
  }
  
  private void insertFiles(Extracted extracted, String projectID) {
    logger.info("    Inserting files...");
    
    TimeCounter counter = new TimeCounter();
    
    filesTable.initializeInserter(tempDir);
    for (FileEX file : extracted.getFileReader()) {
      filesTable.insert(file, projectID);
      counter.increment();
    }
    filesTable.flushInserts();

    logger.info(counter.reportTimeAndCount(6, "files inserted"));
  }
  
  private void loadFileMap(String projectID) {
    logger.info("    Populating file map...");

    TimeCounter counter = new TimeCounter();
    
    filesTable.populateFileMap(fileMap, projectID);
    counter.setCount(fileMap.size());
    logger.info(counter.reportTimeAndCount(6, "files loaded"));
  }
  
  private void insertProblems(Extracted extracted, String projectID) {
    logger.info("    Inserting problems...");

    TimeCounter counter = new TimeCounter();
    
    problemsTable.initializeInserter(tempDir);
    for (ProblemEX problem : extracted.getProblemReader()) {
      String fileID = fileMap.get(problem.getRelativePath());
      if (fileID == null) {
        logger.log(Level.SEVERE, "Unknown file: " + problem.getRelativePath() + " for " + problem);
      } else {
        problemsTable.insert(problem, projectID, fileID);
        counter.increment();
      }
    }
    problemsTable.flushInserts();
    
    logger.info(counter.reportTimeAndCount(6, "problems inserted"));
  }
  
  private void insertEntities(Extracted extracted, String projectID) {
    logger.info("    Inserting entities....");

    TimeCounter counter = new TimeCounter();
    
    entitiesTable.initializeInserter(tempDir);
    
    logger.info("      Processing from entities file...");
    for (EntityEX entity : extracted.getEntityReader()) {
      // Get the file
      String fileID = getFileID(entity.getPath(), entity);
      
      // Add the entity
      entitiesTable.insert(entity, projectID, fileID);
        
      counter.increment();
    }
    logger.info(counter.reportTimeAndCount(8, "entities processed"));
    
    counter.lap();
    
    logger.info("      Performing db insert...");
    entitiesTable.flushInserts();
    logger.info(counter.reportTimeAndTotalCount(8, "entities inserted"));
    
    logger.info(counter.reportTotalTimeAndCount(6, "entities processed and inserted"));
  }
  
  private void insertRemainingEntities(Extracted extracted, String projectID, String inClause) {
    logger.info("    Inserting type entities....");

    TimeCounter counter = new TimeCounter();
    
    entitiesTable.initializeInserter(tempDir);
    
    counter.lap();
    
    logger.info("      Processing from local variables / parameters file...");
    for (LocalVariableEX local : extracted.getLocalVariableReader()) {
      // Get the file
      String fileID = getFileID(local.getPath(), local);
      
      // Add the entity
      entitiesTable.insertLocalVariable(local, projectID, fileID);
        
      // Resolve the type fqn
      resolveEid(counter, local.getTypeFqn(), projectID, inClause);
      
      counter.increment();
    }
    logger.info(counter.reportTimeAndCount(8, "entities processed"));
    
    counter.lap();
    
    logger.info("      Processing from relations file...");
    for (RelationEX relation : extracted.getRelationReader()) {
      // Resolve the rhs fqn
      resolveEid(counter, relation.getRhs(), projectID, inClause);
    }
    logger.info(counter.reportTimeAndCount(8, "entities processed"));
    
    counter.lap();
    
    logger.info("      Processing from imports file...");
    for (ImportEX imp : extracted.getImportReader()) {
      // Resolve the type fqn
      resolveEid(counter, imp.getImported(), projectID, inClause);
    }
    logger.info(counter.reportTimeAndCount(8, "entities processed"));
    
    counter.lap();
    
    logger.info("      Performing db insert...");
    entitiesTable.flushInserts();
    logger.info(counter.reportTimeAndTotalCount(8, "entities inserted"));
    
    logger.info(counter.reportTotalTimeAndCount(6, "entities processed and inserted"));
  }
   
  public void loadEntityMap(String projectID) {
    logger.info("    Populating entity map...");
    
    TimeCounter counter = new TimeCounter();
    
    for (SlightlyLessLimitedEntityDB entity : entitiesTable.getEntityMapByProject(projectID)) {
      Ent ent = entityMap.get(entity.getFqn());
      if (ent == null) {
        ent = new Ent(entity.getFqn());
        entityMap.put(entity.getFqn(), ent);
        ent.addPair(entity);
        counter.increment();
      } else {
        logger.severe("      FQN conflict! " + entity.getFqn());
      }
    }
    logger.info(counter.reportTimeAndCount(6, "entities loaded"));
  }
  
  public void loadRemainingEntityMap(String projectID) {
    logger.info("    Updating entity map...");
    
    TimeCounter counter = new TimeCounter();
    relationsTable.initializeInserter(tempDir);
    
    logger.info("      Loading project entities...");
    for (SlightlyLessLimitedEntityDB entity : entitiesTable.getSyntheticEntitiesByProject(projectID)) {
      if (entity.notDuplicate()) {
        Ent ent = entityMap.get(entity.getFqn());
        if (ent == null) {
          ent = new Ent(entity.getFqn());
          entityMap.put(entity.getFqn(), ent);
          ent.addPair(entity);
          counter.increment();
        } else {
          logger.severe("      FQN conflict! " + entity.getFqn());
        }
      } else {
        if (entityMap.containsKey(entity.getFqn())) {
          entityMap.get(entity.getFqn()).updateDuplicate(entity.getEntityID(), projectID);
        } else {
          logger.severe("Missing fqn for duplicate " + entity.getFqn());
        }
      }
    }
    logger.info(counter.reportTimeAndCount(8, "synthetic entities loaded"));
    
    counter.reset();
    
    logger.info("      Performing db insert on duplicate relations...");
    relationsTable.flushInserts();
    logger.info(counter.reportTime(8, "Db insert performed"));
    
    counter.reset();
    
    logger.info("      Loading unknown entities...");
    for (SlightlyLessLimitedEntityDB entity : entitiesTable.getUnknownEntities(unknownProject)) {
      Ent ent = entityMap.get(entity.getFqn()); 
      if (ent == null) {
        ent = new Ent(entity.getFqn());
        entityMap.put(entity.getFqn(), ent);
        ent.addPair(entity);
        counter.increment();
      }
    }
    logger.info(counter.reportTimeAndCount(8, "unknown entities loaded"));
     
    pendingEntities.clear();
    
    logger.info(counter.reportTotalTime(6, "Entity map updated"));
  }
  
  private void insertRelations(Extracted extracted, String projectID) {
    logger.info("    Inserting relations...");
    
    TimeCounter counter = new TimeCounter();
    relationsTable.initializeInserter(tempDir);
    
    logger.info("      Processing type relations...");
    for (RelationEX relation : newTypeRelations) {
      LimitedEntityDB lhs = getEid(relation.getLhs(), projectID);
      LimitedEntityDB rhs = getEid(relation.getRhs(), projectID);
      
      if (lhs != null && rhs != null) {
        relationsTable.insert(relation.getType(), lhs.getEntityID(), rhs.getEntityID(), rhs.isInternal(projectID), projectID);
        counter.increment();
      }
    }
    newTypeRelations.clear();
    logger.info(counter.reportTimeAndCount(8, "relations processed"));
      
    counter.lap();
      
    logger.info("      Processing relations file...");
    for (RelationEX relation : extracted.getRelationReader()) {
      String fileID = getFileID(relation.getPath(), relation);
      
      LimitedEntityDB lhs = getEid(relation.getLhs(), projectID);
      LimitedEntityDB rhs = getEid(relation.getRhs(), projectID);
      
      if (lhs != null && rhs != null) {
        if (fileID == null) {
          relationsTable.insert(relation.getType(), lhs.getEntityID(), rhs.getEntityID(), rhs.isInternal(projectID), projectID);
        } else {
          relationsTable.insert(relation.getType(), lhs.getEntityID(), rhs.getEntityID(), rhs.isInternal(projectID), projectID, fileID, relation.getStartPosition(), relation.getLength());
        }
        counter.increment();
      }
    }
    logger.info(counter.reportTimeAndCount(8, "relations processed"));
    
    logger.info("      Processing local variables / parameters file...");
    Iterator<LocalVariableEX> iter = extracted.getLocalVariableReader().iterator();
    for (LimitedEntityDB entity : entitiesTable.getLocalVariablesByProject(projectID)) {
      if (iter.hasNext()) {
        LocalVariableEX local = iter.next();
        
        String fileID = getFileID(local.getPath(), local);
        
        // Add the holds relation
        LimitedEntityDB type = getEid(local.getTypeFqn(), projectID);
        if (type != null) {
          if (fileID == null) {
            relationsTable.insert(Relation.HOLDS, entity.getEntityID(), type.getEntityID(), type.isInternal(projectID), projectID);
          } else {
            relationsTable.insert(Relation.HOLDS, entity.getEntityID(), type.getEntityID(), type.isInternal(projectID), projectID, fileID, local.getStartPos(), local.getLength());
          }
          counter.increment();
        }
        
        // Add the inside relation
        LimitedEntityDB parent = getEid(local.getParent(), projectID);
        if (parent != null) {
          relationsTable.insert(Relation.INSIDE, entity.getEntityID(), parent.getEntityID(), null, projectID, fileID, null, null);
        }
        
        counter.increment();
      } else {
        logger.log(Level.SEVERE, "Missing db local variable for " + entity);
      }
    }
    logger.info(counter.reportTimeAndCount(8, "relations processed"));
    
    logger.info("      Performing db insert...");
    relationsTable.flushInserts();
    logger.info(counter.reportTimeAndTotalCount(8, "relations inserted"));
  }
  
  private void insertImports(Extracted extracted, String projectID) {
    logger.info("    Inserting imports...");
    
    TimeCounter counter = new TimeCounter();
    importsTable.initializeInserter(tempDir);
    
    logger.info("      Processing imports file...");
    for (ImportEX imp : extracted.getImportReader()) {
      String fileID = getFileID(imp.getPath(), imp);
      
      if (fileID != null) {
        // Look up the imported entity
        LimitedEntityDB imported = getEid(imp.getImported(), projectID);
        
        // Add the import
        if (imported != null) {
          importsTable.insert(imp.isStatic(), imp.isOnDemand(), imported.getEntityID(), projectID, fileID, imp.getOffset(), imp.getLength());
          counter.increment();
        }
      }
    }
    logger.info(counter.reportTimeAndCount(8, "imports processed"));
    
    counter.lap();
    
    logger.info("      Performing db insert...");
    importsTable.flushInserts();
    logger.info(counter.reportTimeAndTotalCount(8, "imports inserted"));
  }
  
  private void insertComments(Extracted extracted, String projectID) {
    logger.info("    Inserting comments...");
    
    TimeCounter counter = new TimeCounter();
    commentsTable.initializeInserter(tempDir);
    
    logger.info("      Processing comments file...");
    for (CommentEX comment : extracted.getCommentReader()) {
      String fileID = getFileID(comment.getPath(), comment);
      
      if (fileID != null) {
        if (comment.getType() == Comment.JAVADOC) {
          // Look up the entity
          LimitedEntityDB commented = getEid(comment.getFqn(), projectID);
          
          // Add the comment
          if (commented != null) {
            commentsTable.insertJavadoc(commented.getEntityID(), projectID, fileID, comment.getOffset(), comment.getLength());
            counter.increment();
          }
        } else if (comment.getType() == Comment.UJAVADOC) {
          // Add the comment
          commentsTable.insertUnassociatedJavadoc(projectID, fileID, comment.getOffset(), comment.getLength());
          counter.increment();
        } else {
          commentsTable.insertComment(comment.getType(), projectID, fileID, comment.getOffset(), comment.getLength());
          counter.increment();
        }
      }
    }
    logger.info(counter.reportTimeAndCount(8, "comments processed"));
    
    
    logger.info("      Performing db insert...");
    commentsTable.flushInserts();
    logger.info(counter.reportTimeAndTotalCount(8, "comments inserted"));
  }
  
  private String getFileID(String path, ModelEX model) {
    if (path == null) {
      return null;
    } else {
      String fileID = fileMap.get(path);
      if (fileID == null) {
        logger.log(Level.SEVERE, "Unknown file: " + path + " for " + model);
      }
      return fileID;
    }
  }
  
//  private LimitedEntityDB getLocalEid(String fqn, String projectID) {
//    // Maybe it's in the map
//    if (entityMap.containsKey(fqn)) {
//      Ent ent = entityMap.get(fqn);
//      LimitedEntityDB entity = ent.getMain(projectID);
//      if (projectID.equals(entity.getProjectID())) {
//        return entity;
//      } else if (entity.getProjectID() == null && ent.isSingle()) {
//        return entity;
//      }
//    }
//    String eid = entitiesTable.insert(Entity.UNKNOWN, fqn, projectID);
//    Ent result = new Ent(fqn);
//    result.addPair(projectID, eid, Entity.UNKNOWN);
//    entityMap.put(fqn, result);
//    return result.getMain(projectID);
//  }
  
  private void resolveEid(TimeCounter counter, String fqn, String projectID, String inClause) {
    if (pendingEntities.contains(fqn)) {
      return;
    }
    // Maybe it's in the map
    if (entityMap.containsKey(fqn)) {
      if (entityMap.get(fqn).resolveDuplicates(projectID)) {
        counter.increment();
      }
      pendingEntities.add(fqn);
      return; 
    }
    
    // If it's a method, skip the type entities
    if (!TypeUtils.isMethod(fqn)) {
      if (TypeUtils.isArray(fqn)) {
        Pair<String, Integer> arrayInfo = TypeUtils.breakArray(fqn);
        
        // Insert the array entity
        entitiesTable.insertArray(fqn, arrayInfo.getSecond(), projectID);
        
        // Add has elements of relation
        resolveEid(counter, arrayInfo.getFirst(), projectID, inClause);
        newTypeRelations.add(RelationEX.getSyntheticRelation(Relation.HAS_ELEMENTS_OF, fqn, arrayInfo.getFirst()));
        
        counter.increment();
        pendingEntities.add(fqn);
        return;
      }
      
      if (TypeUtils.isWildcard(fqn)) {
        // Insert the wildcard entity
        entitiesTable.insert(Entity.WILDCARD, fqn, projectID);
      
        // If it's bounded, add the bound relation
        if (!TypeUtils.isUnboundedWildcard(fqn)) {
          String bound = TypeUtils.getWildcardBound(fqn);
          resolveEid(counter, bound, projectID, inClause);
          if (TypeUtils.isLowerBound(fqn)) {
            newTypeRelations.add(RelationEX.getSyntheticRelation(Relation.HAS_LOWER_BOUND, fqn, bound));
          } else {
            newTypeRelations.add(RelationEX.getSyntheticRelation(Relation.HAS_UPPER_BOUND, fqn, bound));
          }
        }
        
        counter.increment();
        pendingEntities.add(fqn);
        return;
      }
      
      if (TypeUtils.isTypeVariable(fqn)) {
        // Insert the type variable entity
        entitiesTable.insert(Entity.TYPE_VARIABLE, fqn, projectID);
        
        // Insert the bound relations
        for (String bound : TypeUtils.breakTypeVariable(fqn)) {
          resolveEid(counter, bound, projectID, inClause);
          newTypeRelations.add(RelationEX.getSyntheticRelation(Relation.HAS_UPPER_BOUND, fqn, bound));
        }
        
        counter.increment();
        pendingEntities.add(fqn);
        return;
      }
      
      if (TypeUtils.isParametrizedType(fqn)) {
        // Insert the parametrized type entity
        entitiesTable.insert(Entity.PARAMETERIZED_TYPE, fqn, projectID);
        
        // Add the has base type relation
        String baseType = TypeUtils.getBaseType(fqn);
        resolveEid(counter, baseType, projectID, inClause);
        newTypeRelations.add(RelationEX.getSyntheticRelation(Relation.HAS_BASE_TYPE, fqn, baseType));
        
        // Insert the type arguments
        for (String arg : TypeUtils.breakParametrizedType(fqn)) {
          resolveEid(counter, arg, projectID, inClause);
          newTypeRelations.add(RelationEX.getSyntheticRelation(Relation.HAS_TYPE_ARGUMENT, fqn, arg));
        }
        
        counter.increment();
        pendingEntities.add(fqn);
        return; 
      }
    }

    // Some external reference?
    Collection<LimitedEntityDB> entities = entitiesTable.getLimitedEntitiesByFqn(fqn, inClause);
    if (!entities.isEmpty()) {
      Ent result = new Ent(fqn);
      for (LimitedEntityDB entity : entities) {
        result.addPair(entity);
      }
      result.resolveDuplicates(projectID);
      entityMap.put(fqn, result);
      counter.increment();
      pendingEntities.add(fqn);
      return;
    }
    
    // Give up
    // Check if it's an already known unknown
    String eid = entitiesTable.getEntityIDByFqnAndProject(fqn, unknownProject);
    if (eid == null) {
      entitiesTable.insert(Entity.UNKNOWN, fqn, unknownProject);
    }
    counter.increment();
    pendingEntities.add(fqn);
  }
  
  public LimitedEntityDB getEid(String fqn, String projectID) {
    Ent ent = entityMap.get(fqn);
    if (ent == null) {
//      // Check for unknown entity
//      String eid = entitiesTable.getEntityIDByFqnAndProject(fqn, unknownProject);
//      if (eid == null) {
        logger.severe("Unknown entity: " + fqn);
        return null;
//      } else {
//        ent = new Ent(fqn);
//        entityMap.put(fqn, ent);
//        LimitedEntityDB entity = new LimitedEntityDB(unknownProject, eid, Entity.UNKNOWN);
//        ent.addPair(entity);
//        return entity;
//      }
    } else {
      return ent.getEntity(projectID);
    }
  }
  
//  private LimitedEntityDB getEid(String fqn, String projectID, String inClause) {
//    // Maybe it's in the map
//    if (entityMap.containsKey(fqn)) {
//      return entityMap.get(fqn).getMain(projectID); 
//    }
//    
//    // If it's a method, skip the type entities
//    if (!TypeUtils.isMethod(fqn)) {
//      if (TypeUtils.isArray(fqn)) {
//        Pair<String, Integer> arrayInfo = TypeUtils.breakArray(fqn);
//        
//        // Insert the array entity
//        String eid = entitiesTable.insertArray(fqn, arrayInfo.getSecond(), projectID);
//        
//        // Insert has elements of relation
//        LimitedEntityDB element = getEid(arrayInfo.getFirst(), projectID, inClause);
//        relationsTable.insert(Relation.HAS_ELEMENTS_OF, eid, element.getEntityID(), element.isInternal(projectID), projectID);
//        
//        Ent result = new Ent(fqn);
//        result.addPair(projectID, eid, Entity.ARRAY);
//        entityMap.put(fqn, result);
//        return result.getMain(projectID);
//      }
//      
//      if (TypeUtils.isWildcard(fqn)) {
//        // Insert the wildcard entity
//        String eid = entitiesTable.insert(Entity.WILDCARD, fqn, projectID);
//      
//        // If it's bounded, insert the bound relation
//        if (!TypeUtils.isUnboundedWildcard(fqn)) {
//          LimitedEntityDB bound = getEid(TypeUtils.getWildcardBound(fqn), projectID, inClause);
//          if (TypeUtils.isLowerBound(fqn)) {
//            relationsTable.insert(Relation.HAS_LOWER_BOUND, eid, bound.getEntityID(), bound.isInternal(projectID), projectID);
//          } else {
//            relationsTable.insert(Relation.HAS_UPPER_BOUND, eid, bound.getEntityID(), bound.isInternal(projectID), projectID);
//          }
//        }
//        
//        Ent result = new Ent(fqn);
//        result.addPair(projectID, eid, Entity.WILDCARD);
//        entityMap.put(fqn, result);
//        return result.getMain(projectID);
//      }
//      
//      if (TypeUtils.isTypeVariable(fqn)) {
//        // Insert the type variable entity
//        String eid = entitiesTable.insert(Entity.TYPE_VARIABLE, fqn, projectID);
//        
//        // Insert the bound relations
//        for (String boundFqn : TypeUtils.breakTypeVariable(fqn)) {
//          LimitedEntityDB bound = getEid(boundFqn, projectID, inClause);
//          relationsTable.insert(Relation.HAS_UPPER_BOUND, eid, bound.getEntityID(), bound.isInternal(projectID), projectID);
//        }
//        
//        Ent result = new Ent(fqn);
//        result.addPair(projectID, eid, Entity.TYPE_VARIABLE);
//        entityMap.put(fqn, result);
//        return result.getMain(projectID);
//      }
//      
//      if (TypeUtils.isParametrizedType(fqn)) {
//        // Insert the parametrized type entity
//        String eid = entitiesTable.insert(Entity.PARAMETERIZED_TYPE, fqn, projectID);
//        
//        LimitedEntityDB baseType = getEid(TypeUtils.getBaseType(fqn), projectID, inClause);
//        
//        // Insert the has base type relation
//        relationsTable.insert(Relation.HAS_BASE_TYPE, eid, baseType.getEntityID(), baseType.isInternal(projectID), projectID);
//        
//        // Insert the type arguments
//        for (String argFqn : TypeUtils.breakParametrizedType(fqn)) {
//          LimitedEntityDB arg = getEid(argFqn, projectID, inClause);
//          relationsTable.insert(Relation.HAS_TYPE_ARGUMENT, eid, arg.getEntityID(), arg.isInternal(projectID), projectID);
//        }
//        
//        Ent result = new Ent(fqn);
//        result.addPair(projectID, eid, Entity.PARAMETERIZED_TYPE);
//        entityMap.put(fqn, result);
//        return result.getMain(projectID);
//      }
//    }
//    
//    // Some external reference?
//    Collection<LimitedEntityDB> entities = entitiesTable.getLimitedEntitiesByFqn(fqn, inClause);
//    if (!entities.isEmpty()) {
//      Ent result = new Ent(fqn);
//      for (LimitedEntityDB entity : entities) {
//        result.addPair(entity);
//      }
//      entityMap.put(fqn, result);
//      return result.getMain(projectID);
//    }
//    
//    // Give up
//    // Check if it's an already known unknown
//    String eid = entitiesTable.getEntityIDByFqnAndProject(fqn, unknownProject);
//    if (eid == null) {
//      eid = entitiesTable.insertUnknown(fqn, unknownProject);
//    }
//    Ent result = new Ent(fqn);
//    result.addPair(projectID, eid, Entity.UNKNOWN);
//    entityMap.put(fqn, result);
//    return result.getMain(projectID);
//  }
  
  private class Ent {
    private String fqn;
    
    private LimitedEntityDB main = null;
    private Map<String, LimitedEntityDB> entities = null;

    public Ent(String fqn) {
      this.fqn = fqn;
    }
    
//    public void addPair(String projectID, String entityID, Entity type) {
//      LimitedEntityDB entity = new LimitedEntityDB(projectID, entityID, type);
//      addPair(entity);
//    }
    
    public void addPair(LimitedEntityDB entity) {
      if (entities == null && main == null) {
        main = entity;
      } else {
        if (entities == null) {
          entities = Helper.newHashMap();
          main = null;
        }
        entities.put(entity.getProjectID(), entity);
      }
    }
    
    public boolean resolveDuplicates(String projectID) {
      if (entities != null && !entities.containsKey(projectID)) {
        entitiesTable.insert(Entity.DUPLICATE, fqn, projectID);
        return true;
      } else {
        return false;
      }
    }
    
    public void updateDuplicate(String eid, String projectID) {
      for (LimitedEntityDB entity : entities.values()) {
        if (entity.notDuplicate()) {
          relationsTable.insert(Relation.MATCHES, eid, entity.getEntityID(), false, projectID);
        }
      }
      entities.put(projectID, new LimitedEntityDB(projectID, eid, Entity.DUPLICATE));
    }
    
    public LimitedEntityDB getEntity(String projectID) {
      if (entities == null) {
        return main;
      } else {
        return entities.get(projectID);
      }
    }
  }
}
