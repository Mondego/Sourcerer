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
import edu.uci.ics.sourcerer.db.util.DatabaseConnection;
import edu.uci.ics.sourcerer.repo.extracted.ExtractedLibrary;
import edu.uci.ics.sourcerer.util.TimeCounter;

/**
 * @author Joel Ossher (jossher@uci.edu)
 */
public class ImportJavaLibrariesStageOne extends ExtractedImporterThread {
  private Iterable<ExtractedLibrary> libraries;
  
  protected ImportJavaLibrariesStageOne(DatabaseConnection connection, Iterable<ExtractedLibrary> libraries) {
    super(connection);
    this.libraries = libraries;
  }

  @Override
  public void doImport() {
    TimeCounter counter = new TimeCounter();
    
    for (ExtractedLibrary library : libraries) {
      logger.info("Stage one import of " + library.getName() + "(" + library.getRelativePath() + ")");
      
      logger.info("  Inserting project...");
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
      
      clearMaps();
      counter.increment();
    }
    logger.info(counter.reportTimeAndCount(2, "libraries completed stage one of import"));
  }
}
