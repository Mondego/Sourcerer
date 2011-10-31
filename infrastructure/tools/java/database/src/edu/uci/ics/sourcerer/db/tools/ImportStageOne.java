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
//package edu.uci.ics.sourcerer.db.tools;
//
//import static edu.uci.ics.sourcerer.util.io.Logging.logger;
//import edu.uci.ics.sourcerer.model.db.SmallProjectDB;
//import edu.uci.ics.sourcerer.repo.extracted.Extracted;
//import edu.uci.ics.sourcerer.tools.java.db.importer.DatabaseImporter;
//import edu.uci.ics.sourcerer.util.TimeCounter;
//import edu.uci.ics.sourcerer.util.db.DatabaseConnection;
//
///**
// * @author Joel Ossher (jossher@uci.edu)
// */
//public class ImportStageOne extends DatabaseImporter {
//  private Iterable<Extracted> extracted;
//  
//  protected ImportStageOne(DatabaseConnection connection, Iterable<Extracted> extracted) {
//    super(connection);
//    this.extracted = extracted;
//  }
//
//  @Override
//  public void doImport() {
//    TimeCounter counter = new TimeCounter();
//    
//    for (Extracted item : extracted) {
//      logger.info("Stage one import of " + item.getName() + "(" + item.getRelativePath() + ")");
//      
//      logger.info("  Verifying that item should be imported...");
//      if (!item.extracted()) {
//        logger.info("    Extraction not completed... skipping");
//        continue;
//      } else if (!item.reallyExtracted()) {
//        logger.info("    Extraction empty... skipping");
//        continue;
//      }
//      SmallProjectDB project;
//      if (item.getHash() != null) {
//        project = projectQueries.getSmallByHash(item.getHash());
//      } else {
//        project = projectQueries.getSmallByPath(item.getRelativePath());
//      }
//      if (project != null) {
//        if (project.firstStageCompleted()) {
//          logger.info("    Import already completed... skipping");
//          continue;
//        } else {
//          logger.info("    Import not completed... deleting");
//          deleteByProject(project.getProjectID());
//        }
//      }
//      
//      logger.info("  Inserting project...");
//      Integer projectID = projectsTable.insert(item);
//
//      insertFiles(item, projectID);
//      loadFileMap(projectID);
//      insertFileMetrics(item, projectID);
//      insertProblems(item, projectID);
//      insertEntities(item, projectID);
//      
//      
//      if (item.getHash() != null) {
//        projectsTable.endFirstStageJarProjectInsert(projectID);
//      } else {
//        projectsTable.endFirstStageCrawledProjectInsert(projectID);
//      }
//      clearMaps();
//      counter.increment();
//    }
//    logger.info(counter.reportTimeAndCount(2, "items completed stage one of import"));
//  }
//}
