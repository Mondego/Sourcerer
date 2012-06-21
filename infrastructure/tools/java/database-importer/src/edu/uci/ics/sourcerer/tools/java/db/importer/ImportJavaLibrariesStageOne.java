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
//import edu.uci.ics.sourcerer.tools.java.db.schema.ProjectsTable;
//import edu.uci.ics.sourcerer.tools.java.model.extracted.io.ReaderBundle;
//import edu.uci.ics.sourcerer.tools.java.repo.model.extracted.ExtractedJarFile;
//
///**
// * @author Joel Ossher (jossher@uci.edu)
// */
//public class ImportJavaLibrariesStageOne extends DatabaseImporter {
//  private Iterable<? extends ExtractedJarFile> libraries;
//  
//  protected ImportJavaLibrariesStageOne(Iterable<? extends ExtractedJarFile> libraries) {
//    super("Stage one Java Library import");
//    this.libraries = libraries;
//  }
//
//  @Override
//  public void doImport() {
//    for (ExtractedJarFile library : libraries) {
//      task.start("Importing " + library.getProperties().NAME.getValue());
//      
//      task.start("Inserting project");
//      Integer projectID = exec.insertWithKey(ProjectsTable.TABLE.makeInsert(library));
//      task.finish();
//      
//      ReaderBundle reader = new ReaderBundle(library.getExtractionDir().toFile());
//      
//      insertFiles(reader, projectID);
//      loadFileMap(projectID);
//      insertProblems(reader, projectID);
//      insertEntities(reader, projectID);
//      
//      clearMaps();
//      task.finish();
//    }
//  }
//}
