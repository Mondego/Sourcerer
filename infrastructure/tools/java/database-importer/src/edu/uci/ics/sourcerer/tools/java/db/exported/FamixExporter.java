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
package edu.uci.ics.sourcerer.tools.java.db.exported;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class FamixExporter {
//public static final IOFilePropertyFactory FAMIX_FILE = new IOFilePropertyFactory("moose-file-name", "test.mse", "File name for output file.");
//public static final Property<Integer> PROJECT_ID = new IntegerProperty("project-id", "Project ID to use for command-line version.");
//
//private static TimeoutManager<FamixExporterDatabaseAccessor> accessorManager = 
//  new TimeoutManager<FamixExporterDatabaseAccessor>(new TimeoutManager.Instantiator<FamixExporterDatabaseAccessor>() {
//    @Override
//    public FamixExporterDatabaseAccessor create() {
//      DatabaseConnection conn = new DatabaseConnection();
//      conn.open();
//      return new FamixExporterDatabaseAccessor(conn);
//    }
//  }, 10 * 60 * 1000);
//
//public static Collection<LargeProjectDB> getProjects() {
//  return accessorManager.get().getProjects();
//}
//
//public static Collection<ProjectMetricDB> getProjectMetrics() {
//  return accessorManager.get().getProjectMetrics();
//}
//
//public static void writeFamixModelToFile() {
//  try {
//    FileUtils.writeByteArrayToFile(getFamixModel(PROJECT_ID.getValue()), FAMIX_FILE.asOutput().getValue());
//  } catch (IOException e) {
//    logger.log(Level.SEVERE, "Errog writing to file.", e);
//  }
//}
//
//public static byte[] getFamixModel(Integer projectID) {
//  FamixExporterDatabaseAccessor db = accessorManager.get();
//  
//  StringBuilder builder = new StringBuilder();
//  
//  builder.append("(Moose.Model (sourceLanguage 'Java') (entity\n");
//  
//  Map<String, Integer> pkgMap = Helper.newHashMap();
//  Map<String, Integer> classMap = Helper.newHashMap();
//  Map<Integer, String> methodNames = Helper.newHashMap();
//  
//  for (EntityDB entity : db.getEntities(projectID, Entity.PACKAGE)) {
//    pkgMap.put(entity.getFqn(), entity.getEntityID());
//    builder.append("(FAMIX.Namespace\n");
//    builder.append("\t(id: ").append(entity.getEntityID()).append(")\n");
//    builder.append("\t(name '").append(entity.getFqn().replace(".","::")).append("')\n");
//    builder.append(")\n");
//  }
//  
//  int largestID = 0;
//  
//  Map<Integer, Integer> locMap = Helper.newHashMap();
//  
//  for (EntityMetricDB metric : db.getEntityMetrics(projectID)) {
//    if (metric.getMetric() == Metric.NON_WHITESPACE_LINES_OF_CODE) {
//      locMap.put(metric.getEntityID(), metric.getValue());
//    }
//  }
//  
//  for (EntityDB entity : db.getEntities(projectID, Entity.CLASS, Entity.INTERFACE, Entity.ENUM)) {
//    largestID = Math.max(largestID, entity.getEntityID());
//    classMap.put(entity.getFqn(), entity.getEntityID());
//    
//    String fqn = entity.getFqn();
//    String pkg = null;
//    String name = null;
//    int dollarIdx = fqn.lastIndexOf('$');
//    int dotIdx = fqn.lastIndexOf('.');
//    if (dollarIdx >= 0) {
//      name = fqn.substring(dollarIdx + 1);
//    } else {
//      name = fqn.substring(dotIdx + 1);
//    }
//    pkg = fqn.substring(0, dotIdx);
//    Integer pkgId = pkgMap.get(pkg);
//    
//    int nom = db.getMethodCount(entity.getEntityID());
//    int loc = 0;
//    if (locMap.containsKey(entity.getEntityID())) {
//      loc = locMap.get(entity.getEntityID());
//    } else {
//      logger.log(Level.SEVERE, "Missing loc for: " + entity.getEntityID());
//    }
//    
//    builder.append("(FAMIX.Class\n");
//    builder.append("\t(id: ").append(entity.getEntityID()).append(")\n");
//    builder.append("\t(name '").append(name).append("')\n");
//    builder.append("\t(belongsTo (idref: ").append(pkgId).append("))\n");
//    builder.append("\t(isInterface ").append(entity.getType() == Entity.INTERFACE).append(")\n");
//    builder.append("\t(stub false)\n");
//    builder.append("\t(WLOC ").append(loc).append(")\n");
//    builder.append("\t(NOM ").append(nom).append(")\n");
//    builder.append("\t(sourcererID ").append(entity.getEntityID()).append(")\n");
//    builder.append(")\n");
//  }
//  
//  for (EntityDB entity : db.getEntities(projectID, Entity.METHOD, Entity.CONSTRUCTOR)) {
//    largestID = Math.max(largestID, entity.getEntityID());
//    
//    String fqn = entity.getFqn();
//    String klass = null;
//    String name = null;
//
//    
//    int parenIdx = fqn.indexOf('(');
//    klass = fqn.substring(0, parenIdx);
//    int dotIdx = klass.lastIndexOf('.');
//    name = klass.substring(dotIdx + 1);
//    klass = klass.substring(0, dotIdx);
//
//    methodNames.put(entity.getEntityID(), name);
//    Integer classID = classMap.get(klass);
//    
//    builder.append("(FAMIX.Method\n");
//    builder.append("\t(id: ").append(entity.getEntityID()).append(")\n");
//    builder.append("\t(name '").append(name).append("')\n");
//    builder.append("\t(belongsTo (idref: ").append(classID).append("))\n");
//    builder.append("\t(isConstructor ").append(entity.getType() == Entity.CONSTRUCTOR).append(")\n");
//    builder.append("\t(sourcererID ").append(entity.getEntityID()).append(")\n");
//    builder.append(")\n");
//  }
//  
//  for (RelationDB relation : db.getRelations(projectID, Relation.IMPLEMENTS, Relation.EXTENDS)) {
//    if (relation.getRelationClass() == RelationClass.INTERNAL) {
//      builder.append("(FAMIX.InheritanceDefinition\n");
//      builder.append("\t(id: ").append(relation.getRelationID() + largestID).append(")\n");
//      builder.append("\t(subclass (idref: ").append(relation.getLhsEid()).append("))\n");
//      builder.append("\t(superclass (idref: ").append(relation.getRhsEid()).append("))\n");
//      builder.append(")\n");
//    }
//  }
//  
//  for (RelationDB relation : db.getRelations(projectID, Relation.CALLS, Relation.INSTANTIATES)) {
//    if (relation.getRelationClass() == RelationClass.INTERNAL) {
//      builder.append("(FAMIX.Invocation\n");
//      builder.append("\t(id: ").append(relation.getRelationID() + largestID).append(")\n");
//      builder.append("\t(invokedBy (idref: ").append(relation.getLhsEid()).append("))\n");
//      builder.append("\t(candidate (idref: ").append(relation.getRhsEid()).append("))\n");
//      builder.append("\t(invokes '").append(methodNames.get(relation.getRhsEid())).append("()')\n");
//      builder.append(")\n");
//    }
//  }
//  
//  builder.append("))");
//  return builder.toString().getBytes();
//}
//
//private static class FamixExporterDatabaseAccessor extends DatabaseAccessor {
//  protected FamixExporterDatabaseAccessor(DatabaseConnection connection) {
//    super(connection);
//  }
//  
//  public Collection<LargeProjectDB> getProjects() {
//    return projectQueries.getLargeByType(Project.CRAWLED);
//  }
//  
//  public Collection<ProjectMetricDB> getProjectMetrics() {
//    return projectMetricQueries.getMetrics();
//  }
//  
//  public Collection<EntityMetricDB> getEntityMetrics(Integer projectID) {
//    return entityMetricQueries.getMetricsByProjectID(projectID, Metric.NON_WHITESPACE_LINES_OF_CODE);
//  }
//  
//  public Collection<EntityDB> getEntities(Integer projectID, Entity ... types) {
//    return entityQueries.getByProjectID(projectID, types);
//  }
//  
//  public int getMethodCount(Integer classID) {
//    return relationQueries.getRelationCountBy(classID, Relation.INSIDE);
//  }
//  
//  public Collection<RelationDB> getRelations(Integer projectID, Relation ... types) {
//    return relationQueries.getRelationsByProject(projectID, types);
//  }
//}
}
