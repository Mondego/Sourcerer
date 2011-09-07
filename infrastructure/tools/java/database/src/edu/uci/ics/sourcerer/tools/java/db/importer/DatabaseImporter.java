package edu.uci.ics.sourcerer.tools.java.db.importer;

import static edu.uci.ics.sourcerer.util.io.Logging.logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import edu.uci.ics.sourcerer.tools.java.db.schema.EntitiesTable;
import edu.uci.ics.sourcerer.tools.java.db.schema.FilesTable;
import edu.uci.ics.sourcerer.tools.java.db.schema.ProblemsTable;
import edu.uci.ics.sourcerer.tools.java.db.schema.ProjectsTable;
import edu.uci.ics.sourcerer.tools.java.db.schema.RelationsTable;
import edu.uci.ics.sourcerer.tools.java.model.extracted.EntityEX;
import edu.uci.ics.sourcerer.tools.java.model.extracted.FileEX;
import edu.uci.ics.sourcerer.tools.java.model.extracted.ImportEX;
import edu.uci.ics.sourcerer.tools.java.model.extracted.LocalVariableEX;
import edu.uci.ics.sourcerer.tools.java.model.extracted.ProblemEX;
import edu.uci.ics.sourcerer.tools.java.model.extracted.RelationEX;
import edu.uci.ics.sourcerer.tools.java.model.extracted.io.ReaderBundle;
import edu.uci.ics.sourcerer.tools.java.model.types.Entity;
import edu.uci.ics.sourcerer.tools.java.model.types.File;
import edu.uci.ics.sourcerer.tools.java.model.types.LocalVariable;
import edu.uci.ics.sourcerer.tools.java.model.types.Location;
import edu.uci.ics.sourcerer.tools.java.model.types.Project;
import edu.uci.ics.sourcerer.tools.java.model.types.Relation;
import edu.uci.ics.sourcerer.tools.java.model.types.RelationClass;
import edu.uci.ics.sourcerer.util.Pair;
import edu.uci.ics.sourcerer.util.io.FileUtils;
import edu.uci.ics.sourcerer.util.io.IOUtils;
import edu.uci.ics.sourcerer.util.io.Logging;
import edu.uci.ics.sourcerer.util.io.TaskProgressLogger;
import edu.uci.ics.sourcerer.utils.db.BatchInserter;
import edu.uci.ics.sourcerer.utils.db.ParallelDatabaseRunnable;
import edu.uci.ics.sourcerer.utils.db.sql.ConstantCondition;
import edu.uci.ics.sourcerer.utils.db.sql.SelectQuery;
import edu.uci.ics.sourcerer.utils.db.sql.TypedQueryResult;

public abstract class DatabaseImporter extends ParallelDatabaseRunnable {
  private String taskName;
  protected TaskProgressLogger task;
  private java.io.File tempDir;
  private Map<String, Integer> fileMap;
  private Map<String, DatabaseEntity> entityMap;
  private Set<String> pendingEntities;
  private Collection<RelationEX> newTypeRelations;
  
  private SynchronizedUnknownsMap unknowns;
  
  protected Collection<Integer> libraryProjects;
  
  private SelectQuery externalEntitiesQuery;
  private ConstantCondition<String> externalEntitiesQueryFqn;
  
  private SelectQuery externalErasedEntitiesQuery;
  private ConstantCondition<String> externalErasedEntitiesQueryFqn;
  
  private SelectQuery newEntitiesQuery;
  private ConstantCondition<Integer> newEntitiesQueryProjectID;
  
  private SelectQuery localVariablesQuery;
  private ConstantCondition<Integer> localVariablesQueryProjectID;
  
//  protected RelationClassifier classifier;
  
//  private String inClause;
  
  protected DatabaseImporter(String taskName) {
    this.taskName = taskName;
  }
  
  protected DatabaseImporter(String taskName, SynchronizedUnknownsMap unknowns) {
    this.taskName = taskName;
    this.unknowns = unknowns;
  }
  
//  protected DatabaseImporter(DatabaseConnection connection, SynchronizedUnknownsMap unknowns) {
//    this(connection);
//    this.unknowns = unknowns;
//  }
 
  @Override
  public final void action() {
    Logging.addThreadLogger();
    tempDir = FileUtils.getTempDir();
    fileMap = new HashMap<>();
    entityMap = new HashMap<>();
    pendingEntities = new HashSet<>();
    newTypeRelations = new ArrayList<>();
        
    try (SelectQuery query = exec.makeSelectQuery(ProjectsTable.TABLE)) {
      // Get the Java Library projectIDs
      query.addSelect(ProjectsTable.PROJECT_ID);
      query.addWhere(ProjectsTable.PROJECT_TYPE.compareEquals(), Project.JAVA_LIBRARY);
      
      libraryProjects = query.select().toCollection(ProjectsTable.PROJECT_ID);
      
      // Get the primitives projectID
      query.clearWhere();

      query.addWhere(ProjectsTable.NAME.compareEquals(), ProjectsTable.PRIMITIVES_PROJECT);
      query.addWhere(ProjectsTable.PROJECT_TYPE.compareEquals(), Project.SYSTEM);
      
      libraryProjects.add(query.select().toSingleton(ProjectsTable.PROJECT_ID));
    }
    
    task = new TaskProgressLogger(taskName);
    doImport();
    task.finish();
    
    IOUtils.close(newEntitiesQuery);
    IOUtils.close(externalEntitiesQuery);
    
    Logging.removeThreadLogger();
  }
  
  protected abstract void doImport();
  
  protected final void initializeQueries(Collection<Integer> projectIDs) {
    externalEntitiesQuery = exec.makeSelectQuery(EntitiesTable.TABLE);
    externalEntitiesQuery.addSelects(EntitiesTable.ENTITY_ID, EntitiesTable.PROJECT_ID);
    externalEntitiesQueryFqn = EntitiesTable.FQN.compareEquals();
    externalEntitiesQuery.addWhere(externalEntitiesQueryFqn);
    externalEntitiesQuery.addWhere(EntitiesTable.PROJECT_ID.compareIn(projectIDs));
    
    externalErasedEntitiesQuery = exec.makeSelectQuery(EntitiesTable.TABLE);
    externalErasedEntitiesQuery.addSelects(EntitiesTable.ENTITY_ID, EntitiesTable.PROJECT_ID, EntitiesTable.FQN);
    externalErasedEntitiesQueryFqn = EntitiesTable.FQN.compareLike();
    externalErasedEntitiesQuery.addWhere(externalErasedEntitiesQueryFqn);
    externalErasedEntitiesQuery.addWhere(EntitiesTable.PROJECT_ID.compareIn(projectIDs));
    externalErasedEntitiesQuery.addWhere(EntitiesTable.ENTITY_TYPE.compareIn(EnumSet.of(Entity.CONSTRUCTOR, Entity.METHOD)));
    
    newEntitiesQuery = exec.makeSelectQuery(EntitiesTable.TABLE);
    newEntitiesQuery.addSelects(EntitiesTable.FQN, EntitiesTable.ENTITY_ID, EntitiesTable.ENTITY_TYPE);
    newEntitiesQueryProjectID = EntitiesTable.PROJECT_ID.compareEquals();
    newEntitiesQuery.addWhere(newEntitiesQueryProjectID);
    newEntitiesQuery.addWhere(EntitiesTable.ENTITY_TYPE.compareIn(EnumSet.of(Entity.ARRAY, Entity.WILDCARD, Entity.TYPE_VARIABLE, Entity.PARAMETERIZED_TYPE, Entity.DUPLICATE)));
    
    localVariablesQuery = exec.makeSelectQuery(EntitiesTable.TABLE);
    localVariablesQuery.addSelects(EntitiesTable.ENTITY_ID);
    localVariablesQueryProjectID = EntitiesTable.PROJECT_ID.compareEquals();
    localVariablesQuery.addWhere(localVariablesQueryProjectID);
    localVariablesQuery.addWhere(EntitiesTable.ENTITY_TYPE.compareIn(EnumSet.of(Entity.PARAMETER, Entity.LOCAL_VARIABLE)));
    localVariablesQuery.addOrderBy(EntitiesTable.ENTITY_ID, true);
  }
  
  protected void clearMaps() {
    fileMap.clear();
    entityMap.clear();
  }
  
  protected void insertFiles(ReaderBundle reader, Integer projectID) {
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
  
  protected void loadFileMap(Integer projectID) {
    task.start("Populating file map", "files loaded");
    
    SelectQuery query = exec.makeSelectQuery(FilesTable.TABLE);
    query.addSelects(FilesTable.FILE_ID, FilesTable.PATH);
    query.addWhere(FilesTable.FILE_TYPE.compareNotEquals(), File.JAR);
    query.addWhere(FilesTable.PROJECT_ID.compareEquals(), projectID);
    
    TypedQueryResult result = query.select();
    while (result.next()) {
      fileMap.put(result.getResult(FilesTable.PATH), result.getResult(FilesTable.FILE_ID));
      task.progress();
    }
    task.finish();
  }
  
//  protected void insertFileMetrics(Extracted extracted, Integer projectID) {
//    logger.info("  Inserting file metrics...");
//    
//    TimeCounter counter = new TimeCounter();
//    
//    logger.info("    Processing file metrics...");
//    
//    Map<Metric, Counter<Metric>> projectMetrics = Helper.newEnumMap(Metric.class);
//    
//    fileMetricsTable.initializeInserter(tempDir);
//    for (FileEX file : extracted.getFileReader()) {
//      if (file.getType() == File.SOURCE) {
//        Integer fileID = fileMap.get(file.getPath());
//        if (fileID == null) {
//          logger.log(Level.SEVERE, "Unknown file: " + file.getPath());
//        } else {
//          for (Map.Entry<Metric, Integer> metric : file.getMetrics().getMetricValues()) {
//            fileMetricsTable.insert(projectID, fileID, metric.getKey(), metric.getValue());
//            Counter<Metric> count = projectMetrics.get(metric.getKey());
//            if (count == null) {
//              count = new Counter<Metric>(metric.getKey());
//              projectMetrics.put(metric.getKey(), count);
//            }
//            count.add(metric.getValue());
//            counter.increment();
//          }
//        }
//      }
//    }
//    
//    logger.info(counter.reportTimeAndCount(6, "file metrics processed"));
//    
//    counter.lap();
//    
//    logger.info("    Performing db insert...");
//    fileMetricsTable.flushInserts();
//    logger.info(counter.reportTimeAndTotalCount(6, "file metrics inserted"));
//
//    counter.lap();
//    
//    logger.info("    Inserting project metrics...");
//    projectMetricsTable.initializeInserter(tempDir);
//    for (Counter<Metric> count : projectMetrics.values()) {
//      projectMetricsTable.insert(projectID, count.getObject(), count.getCount());
//    }
//    projectMetricsTable.flushInserts();
//    logger.info(counter.reportTime(6, "Project metrics inserted"));
//    
//    logger.info(counter.reportTotalTimeAndCount(4, "files processed and inserted"));
//  }
  
  protected void insertProblems(ReaderBundle reader, Integer projectID) {
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
  
  protected void insertEntities(ReaderBundle reader, Integer projectID) {
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
  
//  protected void insertEntityMetrics(Extracted extracted, Integer projectID) {
//    logger.info("  Inserting entity metrics...");
//    
//    TimeCounter counter = new TimeCounter();
//    
//    logger.info("    Processing entity metrics...");
//    
//    entityMetricsTable.initializeInserter(tempDir);
//    for (EntityEX entity: extracted.getEntityReader()) {
//      Integer fileID = fileMap.get(entity.getPath());
//      SmallEntityDB eid = getEid(entity.getFqn(), projectID);
//      if (fileID == null) {
//        logger.log(Level.SEVERE, "Unknown file: " + entity.getPath());
//      } else if (eid == null) {
//        logger.log(Level.SEVERE, "Unknown entity: " + entity.getFqn());
//      } else if (!eid.getProjectID().equals(projectID)) {
//        logger.log(Level.SEVERE, "Incorrect project: " + entity.getFqn());
//      } else {
//        for (Map.Entry<Metric, Integer> metric : entity.getMetrics().getMetricValues()) {
//          entityMetricsTable.insert(projectID, fileID, eid.getEntityID(), metric.getKey(), metric.getValue());
//          counter.increment();
//        }
//      } 
//    }
//    
//    logger.info(counter.reportTimeAndCount(6, "entity metrics processed"));
//    
//    counter.lap();
//    
//    logger.info("    Performing db insert...");
//    entityMetricsTable.flushInserts();
//    logger.info(counter.reportTimeAndTotalCount(6, "entity metrics inserted"));
//
//    logger.info(counter.reportTotalTimeAndCount(4, "files processed and inserted"));
//  }
  
//  task.start("Populating file map", "files loaded");
//  
//  SelectQuery query = exec.makeSelectQuery(FilesTable.TABLE);
//  query.addSelects(FilesTable.FILE_ID, FilesTable.PATH);
//  query.addWhere(FilesTable.FILE_TYPE.compareNotEquals(), File.JAR);
//  query.addWhere(FilesTable.PROJECT_ID.compareEquals(), projectID);
//  
//  TypedQueryResult result = query.select();
//  while (result.next()) {
//    fileMap.put(result.getResult(FilesTable.PATH), result.getResult(FilesTable.FILE_ID));
//    task.progress();
//  }
//  task.finish();
  public void loadEntityMap(Integer projectID) {
    task.start("Populating entity map", "entities loaded");
    
    try (SelectQuery query = exec.makeSelectQuery(EntitiesTable.TABLE)) {
      query.addSelects(EntitiesTable.ENTITY_ID, EntitiesTable.FQN);
      query.addWhere(EntitiesTable.PROJECT_ID.compareEquals(), projectID);
  
      TypedQueryResult result = query.select();
      
      while (result.next()) {
        String fqn = result.getResult(EntitiesTable.FQN);
        DatabaseEntity entity = entityMap.get(fqn);
        if (entity == null) {
          entity = DatabaseEntity.makeInternal(result.getResult(EntitiesTable.ENTITY_ID));
          entityMap.put(fqn, entity);
        } else {
          logger.severe("FQN conflict: " + fqn);
        }
        task.progress();
      }
    }
    task.finish();
  }
  
  protected void insertRemainingEntities(ReaderBundle reader, Integer projectID) {
    task.start("Inserting local variables, parameters and type entities", "entities inserted");
   
    BatchInserter inserter = exec.makeInFileInserter(tempDir, EntitiesTable.TABLE);
    
    TaskProgressLogger processTask = task.spawnChild();
    processTask.start("Processing local variables and parameters file", "variables processed");
    for (LocalVariableEX var : reader.getTransientLocalVariables()) {
      // Get the file
      Integer fileID = getFileID(var.getLocation());
      
      if (fileID != null) {
        // Add the entity
        inserter.addInsert(EntitiesTable.makeInsert(var, projectID, fileID));
        task.progress();
        
        // Resolve the type fqn
        resolveEid(inserter, var.getTypeFqn(), projectID);
      }
      processTask.progress();
    }
    processTask.finish();
    
    
    processTask.start("Processing relations file", "relations processed");
    for (RelationEX relation : reader.getTransientRelations()) {
      // Resolve the rhs fqn
      resolveEid(inserter, relation.getRhs(), projectID);
      processTask.progress();
    }
    processTask.finish();
    
    processTask.start("Processing imports file", "imports processed");
    for (ImportEX imp : reader.getTransientImports()) {
      // Resolve the type fqn
      resolveEid(inserter, imp.getImported(), projectID);
      processTask.progress();
    }
    processTask.finish();
    
    task.start("Performing db insert");
    inserter.insert();
    task.finish();
    
    task.finish();
  }
  
  private void resolveEid(BatchInserter inserter, String fqn, Integer projectID) {
    if (pendingEntities.contains(fqn)) {
      return;
    }
    // Maybe it's in the map
    if (entityMap.containsKey(fqn)) {
//      if (entityMap.get(fqn).resolveDuplicates(projectID)) {
//        counter.increment();
//      }
//      pendingEntities.add(fqn);
      return; 
    }
    
    // If it's a method, skip the type entities
    if (TypeUtils.isMethod(fqn)) {
      // TODO REMOVE THIS!
      pendingEntities.add(fqn);
      return;
    } else {
      if (TypeUtils.isArray(fqn)) {
        Pair<String, Integer> arrayInfo = TypeUtils.breakArray(fqn);
        
        // Insert the array entity
        inserter.addInsert(EntitiesTable.makeInsert(Entity.ARRAY, fqn, arrayInfo.getSecond(), projectID));
        pendingEntities.add(fqn);
        task.progress();
        
        // Add has elements of relation
        resolveEid(inserter, arrayInfo.getFirst(), projectID);
        newTypeRelations.add(new RelationEX(Relation.HAS_ELEMENTS_OF, fqn, arrayInfo.getFirst(), null));

        return;
      }
      
      if (TypeUtils.isWildcard(fqn)) {
        // Insert the wildcard entity
        inserter.addInsert(EntitiesTable.makeInsert(Entity.WILDCARD, fqn, projectID));
        pendingEntities.add(fqn);
        task.progress();
      
        // If it's bounded, add the bound relation
        if (!TypeUtils.isUnboundedWildcard(fqn)) {
          String bound = TypeUtils.getWildcardBound(fqn);
          resolveEid(inserter, bound, projectID);
          if (TypeUtils.isLowerBound(fqn)) {
            newTypeRelations.add(new RelationEX(Relation.HAS_LOWER_BOUND, fqn, bound, null));
          } else {
            newTypeRelations.add(new RelationEX(Relation.HAS_UPPER_BOUND, fqn, bound, null));
          }
        }
        
        return;
      }
      
      if (TypeUtils.isTypeVariable(fqn)) {
        // Insert the type variable entity
        inserter.addInsert(EntitiesTable.makeInsert(Entity.TYPE_VARIABLE, fqn, projectID));
        pendingEntities.add(fqn);
        task.progress();
        
        // Insert the bound relations
        for (String bound : TypeUtils.breakTypeVariable(fqn)) {
          resolveEid(inserter, bound, projectID);
          newTypeRelations.add(new RelationEX(Relation.HAS_UPPER_BOUND, fqn, bound, null));
        }
        
        return;
      }
      
      if (TypeUtils.isParametrizedType(fqn)) {
        // Insert the parametrized type entity
        inserter.addInsert(EntitiesTable.makeInsert(Entity.PARAMETERIZED_TYPE, fqn, projectID));
        pendingEntities.add(fqn);
        task.progress();
        
        // Add the has base type relation
        String baseType = TypeUtils.getBaseType(fqn);
        resolveEid(inserter, baseType, projectID);
        newTypeRelations.add(new RelationEX(Relation.HAS_BASE_TYPE, fqn, baseType, null));
        
        // Insert the type arguments
        for (String arg : TypeUtils.breakParametrizedType(fqn)) {
          resolveEid(inserter, arg, projectID);
          newTypeRelations.add(new RelationEX(Relation.HAS_TYPE_ARGUMENT, fqn, arg, null));
        }
        
        return; 
      }
    }

    // Some external reference?
    externalEntitiesQuery.updateWhere(externalEntitiesQueryFqn, fqn);
    try (TypedQueryResult result = externalEntitiesQuery.select()) {
      if (result.hasNext()) {
        DuplicatedDatabaseEntity entity = new DuplicatedDatabaseEntity();
        while (result.next()) {
          entity.addInstance(result.getResult(EntitiesTable.ENTITY_ID), result.getResult(EntitiesTable.PROJECT_ID));
        }
        if (entity.hasMultipleInstances()) {
          // Insert the duplicate entity
          inserter.addInsert(EntitiesTable.makeInsert(Entity.DUPLICATE, fqn, projectID));
        } else {
          
        }
        entityMap.put(fqn, entity);
        pendingEntities.add(fqn);
        return;
      }
    }
    
    // External reference that's been erased?
    if (TypeUtils.isMethod(fqn)) {
      externalErasedEntitiesQuery.updateWhere(externalErasedEntitiesQueryFqn, TypeUtils.getMethodName(fqn) + "(%");
      try (TypedQueryResult result = externalErasedEntitiesQuery.select()) {
        if (result.hasNext()) {
          DuplicatedDatabaseEntity entity = new DuplicatedDatabaseEntity();
          while (result.next()) {
            if (fqn.equals(TypeUtils.erase(result.getResult(EntitiesTable.FQN)))) {
              entity.addInstance(result.getResult(EntitiesTable.ENTITY_ID), result.getResult(EntitiesTable.PROJECT_ID));
            }
          }
          if (entity.hasMultipleInstances()) {
            inserter.addInsert(EntitiesTable.makeInsert(Entity.DUPLICATE, fqn, projectID));
          }
          if (entity.hasInstances()) {
            entityMap.put(fqn, entity);
            pendingEntities.add(fqn);
            return;
          }
        }
      }
    }
    
    // TODO check if it's a call to a non-static instance constructor
    
    // Give up
    // Check if it's an already known unknown
    if (!unknowns.contains(fqn)) {
      unknowns.add(exec, fqn);
      task.progress();
    }
    pendingEntities.add(fqn);
  }

  protected void loadRemainingEntityMap(Integer projectID) {
    task.start("Updating entity map");
    
    BatchInserter inserter = exec.makeInFileInserter(tempDir, RelationsTable.TABLE);
    
    task.start("Processing new entities", "new entities processed");
    newEntitiesQuery.updateWhere(newEntitiesQueryProjectID, projectID);
    TypedQueryResult result = newEntitiesQuery.selectStreamed();
    while (result.next()) {
      String fqn = result.getResult(EntitiesTable.FQN);
      DatabaseEntity entity = entityMap.get(fqn);
      if (result.getResult(EntitiesTable.ENTITY_TYPE) == Entity.DUPLICATE) {
        if (entity == null) {
          logger.severe("Missing fqn for duplicate " + fqn);
        } else {
          DuplicatedDatabaseEntity dupEntity = (DuplicatedDatabaseEntity) entity;
          dupEntity.updateDuplicate(inserter, result.getResult(EntitiesTable.ENTITY_ID), projectID);
        }
      } else {
        if (entity == null) {
          entity = DatabaseEntity.makeNotApplicable(result.getResult(EntitiesTable.ENTITY_ID));
          entityMap.put(fqn, entity);
        } else {
          logger.severe("FQN conflict! " + fqn);
        }
      }
      task.progress();
    }
    task.finish();

    task.start("Inserting duplicate relations");
    inserter.insert();
    task.finish();

    pendingEntities.clear();
    
    task.finish();
  }
  
  protected void insertRelations(ReaderBundle reader, Integer projectID) {
    task.start("Inserting relations");
    
    BatchInserter inserter = exec.makeInFileInserter(tempDir, RelationsTable.TABLE);
    
    task.start("Processing local variables & parameters", "variables processed");
    localVariablesQuery.updateWhere(localVariablesQueryProjectID, projectID);
    try (TypedQueryResult result = localVariablesQuery.selectStreamed()) {
      for (LocalVariableEX var : reader.getTransientLocalVariables()) {
        if (result.next()) {
          Integer entityID = result.getResult(EntitiesTable.ENTITY_ID);
          Integer fileID = getFileID(var.getLocation());
          
          if (var.getType() == LocalVariable.PARAM) {
            entityMap.put(var.getParent() + "#" + var.getPosition(), DatabaseEntity.makeInternal(entityID));
          }
          
          // Add the holds relation
          DatabaseEntity type = getRHS(var.getTypeFqn());
          if (type != null) {
            if (fileID == null) {
              inserter.addInsert(RelationsTable.makeInsert(Relation.HOLDS, type.getRelationClass(), entityID, type.getEntityID(), projectID));
            } else {
              inserter.addInsert(RelationsTable.makeInsert(Relation.HOLDS, type.getRelationClass(), entityID, type.getEntityID(), projectID, fileID, var.getLocation()));
            }
          }
          
          // Add the inside relation
          DatabaseEntity parent = getRHS(var.getParent());
          if (parent != null) {
            inserter.addInsert(RelationsTable.makeInsert(Relation.INSIDE, parent.getRelationClass(), entityID, parent.getEntityID(), projectID, fileID));
          }
        } else {
          logger.log(Level.SEVERE, "Missing db local variable for " + var);
        }
        task.progress();
      }
    }
    task.finish();
    
    task.start("Processing type relations", "type relations processed");
    for (RelationEX relation : newTypeRelations) {
      Integer lhs = getLHS(relation.getLhs());
      DatabaseEntity rhs = getRHS(relation.getRhs()); 
      
      if (lhs != null && rhs != null) {
        inserter.addInsert(RelationsTable.makeInsert(relation.getType(), rhs.getRelationClass(), lhs, rhs.getEntityID(), projectID));
        task.progress();
      }
    }
    newTypeRelations.clear();
    task.finish();
      
    task.start("Processing non-calls relations", "relations processed");
    for (RelationEX relation : reader.getTransientRelations()) {
      if (relation.getType() != Relation.CALLS) {
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
  
  protected void insertCallsRelations(ReaderBundle reader, Integer projectID) {
    BatchInserter inserter = exec.makeInFileInserter(tempDir, RelationsTable.TABLE);
    task.start("Processing calls relations", "relations processed");
    for (RelationEX relation : reader.getTransientRelations()) {
      if (relation.getType() == Relation.CALLS) {
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
  
//  protected void insertImports(Extracted extracted, Integer projectID) {
//    logger.info("  Inserting imports...");
//    
//    TimeCounter counter = new TimeCounter();
//    
//    importsTable.initializeInserter(tempDir);
//    
//    logger.info("    Processing imports file...");
//    for (ImportEX imp : extracted.getImportReader()) {
//      Integer fileID = getFileID(imp.getPath(), imp);
//      
//      if (fileID != null) {
//        // Look up the imported entity
//        SmallEntityDB imported = getEid(imp.getImported(), projectID);
//        
//        // Add the import
//        if (imported != null) {
//          importsTable.insert(imp.isStatic(), imp.isOnDemand(), imported.getEntityID(), projectID, fileID, imp.getOffset(), imp.getLength());
//          counter.increment();
//        }
//      }
//    }
//    logger.info(counter.reportTimeAndCount(6, "imports processed"));
//    
//    counter.lap();
//    
//    logger.info("    Performing db insert...");
//    importsTable.flushInserts();
//    logger.info(counter.reportTimeAndTotalCount(6, "imports inserted"));
//  }
  
//  protected void insertComments(Extracted extracted, Integer projectID) {
//    logger.info("  Inserting comments...");
//    
//    TimeCounter counter = new TimeCounter();
//
//    commentsTable.initializeInserter(tempDir);
//    
//    logger.info("    Processing comments file...");
//    for (CommentEX comment : extracted.getCommentReader()) {
//      Integer fileID = getFileID(comment.getPath(), comment);
//      
//      if (fileID != null) {
//        if (comment.getType() == Comment.JAVADOC) {
//          // Look up the entity
//          SmallEntityDB commented = getEid(comment.getFqn(), projectID);
//          
//          // Add the comment
//          if (commented != null) {
//            commentsTable.insertJavadoc(commented.getEntityID(), projectID, fileID, comment.getOffset(), comment.getLength());
//            counter.increment();
//          }
//        } else if (comment.getType() == Comment.UJAVADOC) {
//          // Add the comment
//          commentsTable.insertUnassociatedJavadoc(projectID, fileID, comment.getOffset(), comment.getLength());
//          counter.increment();
//        } else {
//          commentsTable.insertComment(comment.getType(), projectID, fileID, comment.getOffset(), comment.getLength());
//          counter.increment();
//        }
//      }
//    }
//    logger.info(counter.reportTimeAndCount(6, "comments processed"));
//    
//    
//    logger.info("    Performing db insert...");
//    commentsTable.flushInserts();
//    logger.info(counter.reportTimeAndTotalCount(6, "comments inserted"));
//  }
  
  private Integer getFileID(Location location) {
    if (location == null) {
      return null;
    } else {
      String path = location.getPath();
      if (path == null) {
        path = location.getClassFile();
      }
      if (path == null) {
        return null;
      } else {
        Integer fileID = fileMap.get(path);
        if (fileID == null) {
          logger.log(Level.SEVERE, "Unknown file: " + location);
        }
        return fileID;
      }
    }
  }
  
  public Integer getLHS(String fqn) {
    DatabaseEntity entity = entityMap.get(fqn);
    if (entity == null) {
      logger.severe("Missing lhs entity: " + fqn);
      return null;
    } else {
      return entity.getEntityID();
    }
  }
  
  public DatabaseEntity getRHS(String fqn) {
    DatabaseEntity entity = entityMap.get(fqn);
    if (entity == null) {
      entity = unknowns.getUnknown(fqn);
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
//  public Integer getEntityID(String fqn, boolean lhsEntity) {
//    DatabaseEntity entity = entityMap.get(fqn);
//    if (entity == null) {
//      if (lhsEntity) {
//        logger.severe("Missing lhs entity: " + fqn);
//        return null;
//      } else {
//        Integer entityID = unknowns.getUnknown(fqn);
//        if (entityID == null) {
//          logger.severe("Unknown entity: " + fqn);
//          return null;
//        } else {
//          return entityID;
//        }
//      }
//    } else {
//      return entity.getEntityID();
//    }
//  }
  
//  protected void buildInClause(Collection<Integer> projectIDs, Extracted extracted) {
//    for (UsedJarEX usedJar : extracted.getUsedJarReader()) {
//      projectIDs.add(projectQueries.getProjectIDByHash(usedJar.getHash()));
//    }
//    for (FileEX file : extracted.getFileReader()) {
//      if (file.getType() == File.JAR) {
//        projectIDs.add(projectQueries.getProjectIDByHash(file.getHash()));
//      }
//    }
//    buildInClause(projectIDs);
//  }
//  
//  protected void buildInClause(Collection<Integer> projectIDs) {
//    StringBuilder builder = new StringBuilder("(");
//    for (Integer projectID : projectIDs) {
//      builder.append(projectID).append(',');
//    }
//    builder.setCharAt(builder.length() - 1, ')');
//    inClause = builder.toString();
//  }
 
  private class DuplicatedDatabaseEntity extends DatabaseEntity {
    private Collection<ClassedEntity> instances;

    private class ClassedEntity {
      Integer entityID;
      RelationClass rClass;
      
      ClassedEntity(Integer entityID, RelationClass rClass) {
        this.entityID = entityID;
        this.rClass = rClass;
      }
    }
    
    public DuplicatedDatabaseEntity() {
      super();
    }
    
    public void addInstance(Integer entityID, Integer projectID) {
      if (instances == null) {
        instances = new ArrayList<>();
      }
      RelationClass newClass = libraryProjects.contains(projectID) ? RelationClass.JAVA_LIBRARY : RelationClass.EXTERNAL;
      instances.add(new ClassedEntity(entityID, newClass));
      if (instances.size() == 1) {
        this.entityID = entityID;
        rClass = newClass;
      } else {
        this.entityID = null;
        if (rClass != newClass) {
          rClass = RelationClass.MIXED_EXTERNAL;
        }
      }
    }
    
    public boolean hasInstances() {
      return instances != null && !instances.isEmpty();
    }
    
    public boolean hasMultipleInstances() {
      return instances != null && instances.size() > 1; 
    }
    
//    public void addPair(SmallEntityDB entity) {
//      if (entities == null && main == null) {
//        main = entity;
//      } else {
//        if (entities == null) {
//          entities = Helper.newHashMap();
//          main = null;
//        }
//        entities.put(entity.getProjectID(), entity);
//      }
//    }
    
    public void updateDuplicate(BatchInserter inserter, Integer entityID, Integer projectID) {
      this.entityID = entityID;
      for (ClassedEntity entity : instances) {
        inserter.addInsert(RelationsTable.makeInsert(Relation.MATCHES, entity.rClass, entityID, entity.entityID, projectID));
      }
    }
    
//    public void updateDuplicate(Integer eid, Integer projectID) {
//      for (SmallEntityDB entity : entities.values()) {
//        if (!entity.getType().isDuplicate()) {
//          relationsTable.insert(Relation.MATCHES, RelationClass.NOT_APPLICABLE, eid, entity.getEntityID(), projectID);
//        }
//      }
//      entities.put(projectID, new SmallEntityDB(eid, Entity.DUPLICATE, projectID));
//    }
//    
//    public SmallEntityDB getEntity(Integer projectID) {
//      if (entities == null) {
//        return main;
//      } else {
//        return entities.get(projectID);
//      }
//    }
  }
}
