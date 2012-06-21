///* 
// * Sourcerer: an infrastructure for large-scale source code analysis.
// * Copyright (C) by contributors. See CONTRIBUTORS.txt for full list.
// *
// * This program is free software: you can redistribute it and/or modify
// * it under the terms of the GNU General Public License as published by
// * the Free Software Foundation, either version 3 of the License, or
// * (at your option) any later version.
//
// * This program is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// * GNU General Public License for more details.
//
// * You should have received a copy of the GNU General Public License
// * along with this program. If not, see <http://www.gnu.org/licenses/>.
// */
//package edu.uci.ics.sourcerer.tools.java.db.importer;
//
//import java.util.Collection;
//
//import edu.uci.ics.sourcerer.tools.java.db.schema.ProjectsTable;
//import edu.uci.ics.sourcerer.tools.java.model.extracted.io.ReaderBundle;
//import edu.uci.ics.sourcerer.tools.java.model.types.Project;
//import edu.uci.ics.sourcerer.tools.java.repo.model.extracted.ExtractedJarFile;
//import edu.uci.ics.sourcerer.utils.db.sql.ConstantCondition;
//import edu.uci.ics.sourcerer.utils.db.sql.SelectQuery;
//
///**
// * @author Joel Ossher (jossher@uci.edu)
// */
//public class ImportJavaLibrariesStageTwo extends DatabaseImporter {
//  private Iterable<? extends ExtractedJarFile> libraries;
//  
//  protected ImportJavaLibrariesStageTwo(Iterable<? extends ExtractedJarFile> libraries, SynchronizedUnknownsMap unknowns) {
//    super("Stage two Java Library import", unknowns);
//    this.libraries = libraries;
//  }
//
//  @Override
//  public void doImport() {
//    initializeQueries(libraryProjects);
//    
//    try (SelectQuery query = exec.makeSelectQuery(ProjectsTable.TABLE)) {
//      query.addSelect(ProjectsTable.PROJECT_ID);
//      ConstantCondition<String> nameCond = ProjectsTable.NAME.compareEquals();
//      query.andWhere(nameCond, ProjectsTable.PRIMITIVES_PROJECT);
//      query.andWhere(ProjectsTable.PROJECT_TYPE.compareEquals(), Project.JAVA_LIBRARY);
//        
//      for (ExtractedJarFile library : libraries) {
//        task.start("Importing " + library.getProperties().NAME.getValue());
//        query.setWhereValue(nameCond, library.getProperties().NAME.getValue());
//        Integer projectID = query.select().toSingleton(ProjectsTable.PROJECT_ID);
//        
//        loadEntityMap(projectID);
//        loadFileMap(projectID);
//        
//        ReaderBundle reader = new ReaderBundle(library.getExtractionDir().toFile());
//        
//        insertRemainingEntities(reader, projectID);
//        loadRemainingEntityMap(projectID);
//        insertRelations(reader, projectID);
////        insertImports(library, projectID);
////        insertComments(library, projectID);
//        
//        clearMaps();
//        task.finish();
//      }
//    }
//  }
//
//}
